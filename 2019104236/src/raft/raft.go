package raft

//
// this is an outline of the API that raft must expose to
// the service (or tester). see comments below for
// each of these functions for more details.
//
// rf = Make(...)
//   create a new Raft server.
// rf.Start(command interface{}) (index, term, isleader)
//   start agreement on a new log entry
// rf.GetState() (term, isLeader)
//   ask a Raft for its current term, and whether it thinks it is leader
// ApplyMsg
//   each time a new entry is committed to the log, each Raft peer
//   should send an ApplyMsg to the service (or tester)
//   in the same server.
//

import "sync"
import (
	"bytes"
	"encoding/gob"
	"github.com/sirupsen/logrus"
	"labrpc"
	"math/rand"
	"time"
)

const (
	follower  = 1
	candidate = 2
	leader    = 3
	quit      = 4
)

//
// as each Raft peer becomes aware that successive log entries are
// committed, the peer should send an ApplyMsg to the service (or
// tester) on the same server, via the applyCh passed to Make().
//
type ApplyMsg struct {
	Index       int
	Command     interface{}
	UseSnapshot bool   // ignore for lab2; only used in lab3
	Snapshot    []byte // ignore for lab2; only used in lab3
}

type logEntry struct {
	Term    int
	Index   int
	Command interface{}
}

func (rf *Raft) discardEntireLog() {
	rf.log = nil
	rf.addLogEntry(rf.lastIncludedLogEntry)
}

func (rf *Raft) addLogEntry(entry logEntry) {
	rf.log = append(rf.log, entry)
}

func (rf *Raft) getLogEntry(index int) (logEntry, bool) {
	var le logEntry
	if rf.getLogLen() == 0 {
		return le, false
	}
	firstIndex := rf.log[0].Index
	if rf.getLastLogIndex() < index || firstIndex > index {
		return le, false
	} else {
		return rf.log[index-firstIndex], true
	}
}

func (rf *Raft) getLogEntryOk(index int) logEntry {
	firstIndex := rf.log[0].Index
	return rf.log[index-firstIndex]
}

func (rf *Raft) getIndexOfLogEntryFirstTerm(term int) int {
	for _, e := range rf.log {
		if e.Term == term {
			return e.Index
		}
	}
	return 0
}

func (rf *Raft) deleteEntryFrom(index int) {
	firstIndex := rf.log[0].Index
	if rf.getLastLogIndex() < index {
		return
	} else {
		sliceIndex := index - firstIndex
		rf.log = rf.log[:sliceIndex]
	}
}

func (rf *Raft) getEntryFrom(index int) []logEntry {
	firstIndex := rf.log[0].Index
	sliceIndex := index - firstIndex
	return rf.log[sliceIndex:]
}

func (rf *Raft) getLastLogIndex() int {
	return rf.log[len(rf.log)-1].Index
}

func (rf *Raft) getLastLogTerm() int {
	return rf.log[len(rf.log)-1].Term
}

func (rf *Raft) getLogLen() int {
	return len(rf.log)
}

//
// A Go object implementing a single Raft peer.
//
type Raft struct {
	mu        sync.Mutex          // Lock to protect shared access to this peer's state
	peers     []*labrpc.ClientEnd // RPC end points of all peers
	persister *Persister          // Object to hold this peer's persisted state
	me        int                 // this peer's index into peers[]

	state int

	// Persistent states
	currentTerm int
	votedFor    int
	log         []logEntry

	// Volatile states
	commitIndex int
	lastApplied int

	// Volatile states on leader
	nextIndex  map[int]int
	matchIndex map[int]int

	applyCh         chan ApplyMsg
	convertState    chan int
	electionTimeout time.Duration

	// Snapshot
	lastIncludedLogEntry logEntry

	killServer chan int
}

func (rf *Raft) Compaction(index int) {
	go func() {
		rf.mu.Lock()
		defer rf.mu.Unlock()

		if index <= rf.lastIncludedLogEntry.Index {
			return
		}
		rf.lastIncludedLogEntry = rf.getLogEntryOk(index)
		rf.log = rf.getEntryFrom(index)
		rf.persist()
	}()
}

// return currentTerm and whether this server
// believes it is the leader.
func (rf *Raft) GetState() (int, bool) {
	var term int
	var isLeader bool
	rf.mu.Lock()
	defer rf.mu.Unlock()
	term = rf.currentTerm
	if rf.state == leader {
		isLeader = true
	}
	return term, isLeader
}

//
// save Raft's persistent state to stable storage,
// where it can later be retrieved after a crash and restart.
// see paper's Figure 2 for a description of what should be persistent.
//
func (rf *Raft) persist() {
	w := new(bytes.Buffer)
	e := gob.NewEncoder(w)
	e.Encode(rf.currentTerm)
	e.Encode(rf.votedFor)
	e.Encode(rf.log)
	data := w.Bytes()
	rf.persister.SaveRaftState(data)
}

//
// restore previously persisted state.
//
func (rf *Raft) readPersist(data []byte) {
	if data == nil || len(data) < 1 { // bootstrap without any state?
		return
	}
	r := bytes.NewBuffer(data)
	d := gob.NewDecoder(r)
	d.Decode(&rf.currentTerm)
	d.Decode(&rf.votedFor)
	d.Decode(&rf.log)
}

//
// field names must start with capital letters!
//
type RequestVoteArgs struct {
	Term         int
	CandidateId  int
	LastLogIndex int
	LastLogTerm  int
}

//
// field names must start with capital letters!
//
type RequestVoteReply struct {
	Term        int
	VoteGranted bool
}

//
// example RequestVote RPC handler.
//
func (rf *Raft) RequestVote(args *RequestVoteArgs, reply *RequestVoteReply) {
	raftLog.WithFields(log.Fields{
		"senderId":   args.CandidateId,
		"receiverId": rf.me,
	}).Debug("Receive RequestVoteRPC request.")
	rf.mu.Lock()
	defer rf.mu.Unlock()

	rf.checkCurrentTerm(args.Term)

	reply.Term = rf.currentTerm
	if args.Term < rf.currentTerm {
		reply.VoteGranted = false
		raftLog.WithFields(log.Fields{
			"src":    rf.me,
			"dst":    args.CandidateId,
			"reason": "term < currentTerm",
		}).Debug("Reject RequestRpc vote.")
		return
	}
	if rf.votedFor == -1 || rf.votedFor == args.CandidateId {
		lastLogIndex := rf.getLastLogIndex()

		// Make sure candidate’s log is at least as up-to-date as receiver’s log.
		if args.LastLogTerm > rf.getLastLogTerm() {
			rf.grantVote(args, reply)
		} else if args.LastLogTerm == rf.getLastLogTerm() && args.LastLogIndex >= lastLogIndex {
			rf.grantVote(args, reply)
		} else {
			raftLog.WithFields(log.Fields{
				"src":    rf.me,
				"dst":    args.CandidateId,
				"reason": "log is not up-to-date",
			}).Debug("Reject RequestRpc vote.")
			reply.VoteGranted = false
		}
	} else {
		raftLog.WithFields(log.Fields{
			"src":    rf.me,
			"dst":    args.CandidateId,
			"reason": "votedFor != -1 and votedFor != candidateId",
		}).Debug("Reject RequestRpc vote.")
		reply.VoteGranted = false
	}
}

type AppendEntriesArgs struct {
	Term         int
	LeaderId     int
	PrevLogIndex int
	PrevLogTerm  int
	Entries      []logEntry
	LeaderCommit int
}

type AppendEntriesReply struct {
	Term          int
	Success       bool
	ConflictIndex int
	ConflictTerm  int
}

func (rf *Raft) AppendEntries(args *AppendEntriesArgs, reply *AppendEntriesReply) {
	raftLog.WithFields(log.Fields{
		"senderId":   args.LeaderId,
		"receiverId": rf.me,
	}).Debug("Receive AppendEntriesRPC request.")

	rf.mu.Lock()
	defer rf.mu.Unlock()

	rf.checkCurrentTerm(args.Term)

	reply.Term = rf.currentTerm
	if args.Term < rf.currentTerm {
		reply.Success = false
		raftLog.WithFields(log.Fields{
			"src":    rf.me,
			"dst":    args.LeaderId,
			"reason": "term < currentTerm",
		}).Debug("Reply false to AppendEntriesRPC request.")
		return
	}

	if rf.state == follower || rf.state == candidate {
		if len(rf.convertState) == 0 {
			rf.state = follower
			rf.convertState <- follower
		} else {
			log.Debug("xxxxxxxxxxxxxxxxxxxxxxxxxx")
		}
	}

	if args.PrevLogIndex == 0 {
		reply.Success = true
	} else if e, ok := rf.getLogEntry(args.PrevLogIndex); ok && e.Term == args.PrevLogTerm {
		reply.Success = true
	} else {
		reply.Success = false
		if e, ok := rf.getLogEntry(args.PrevLogIndex); !ok {
			reply.ConflictIndex = rf.getLastLogIndex() + 1
			reply.ConflictTerm = -1
		} else {
			reply.ConflictTerm = e.Term
			reply.ConflictIndex = rf.getIndexOfLogEntryFirstTerm(e.Term)
		}
		return
	}
	for _, entry := range args.Entries {
		if e, ok := rf.getLogEntry(entry.Index); ok && e.Term != entry.Term {
			rf.deleteEntryFrom(entry.Index)
		}
	}
	for _, entry := range args.Entries {
		if rf.getLastLogIndex() < entry.Index {
			rf.addLogEntry(entry)
		}
	}
	rf.persist()
	if args.LeaderCommit > rf.commitIndex {
		lastNewLogIndex := 0
		if args.Entries == nil {
			lastNewLogIndex = args.PrevLogIndex
		} else {
			lastNewLogIndex = args.Entries[len(args.Entries)-1].Index
		}
		if args.LeaderCommit > lastNewLogIndex {
			rf.commitIndex = lastNewLogIndex
		} else {
			rf.commitIndex = args.LeaderCommit
		}
		rf.applyLogToStateMachine()
	}
}

type InstallSnapshotArgs struct {
	Term         int
	LeaderId     int
	LastLogEntry logEntry
	Data         []byte
}

type InstallSnapshotReply struct {
	Term int
}

func (rf *Raft) InstallSnapshot(args *InstallSnapshotArgs, reply *InstallSnapshotReply) {
	raftLog.WithFields(log.Fields{
		"senderId":   args.LeaderId,
		"receiverId": rf.me,
	}).Debug("Receive InstallSnapshotRPC request.")

	rf.mu.Lock()
	defer rf.mu.Unlock()

	rf.checkCurrentTerm(args.Term)

	reply.Term = rf.currentTerm

	if args.Term < rf.currentTerm {
		raftLog.WithFields(log.Fields{
			"src":    rf.me,
			"dst":    args.LeaderId,
			"reason": "term < currentTerm",
		}).Debug("Reply false to InstallSnapshotRPC request.")
		return
	}

	if rf.state == follower || rf.state == candidate {
		if len(rf.convertState) == 0 {
			rf.state = follower
			rf.convertState <- follower
		} else {
			log.Debug("xxxxxxxxxxxxxxxxxxxxxxxxxx")
		}
	}

	rf.persister.SaveSnapshot(args.Data)
	rf.lastIncludedLogEntry = args.LastLogEntry
	rf.discardEntireLog()
	rf.lastApplied = rf.lastIncludedLogEntry.Index
	rf.persist()
	rf.applyCh <- ApplyMsg{-1, -1, true, args.Data}
}

//
// example code to send a RequestVote RPC to a server.
// server is the index of the target server in rf.peers[].
// expects RPC arguments in args.
// fills in *reply with RPC reply, so caller should
// pass &reply.
// the types of the args and reply passed to Call() must be
// the same as the types of the arguments declared in the
// handler function (including whether they are pointers).
//
// The labrpc package simulates a lossy network, in which servers
// may be unreachable, and in which requests and replies may be lost.
// Call() sends a request and waits for a reply. If a reply arrives
// within a timeout interval, Call() returns true; otherwise
// Call() returns false. Thus Call() may not return for a while.
// A false return can be caused by a dead server, a live server that
// can't be reached, a lost request, or a lost reply.
//
// Call() is guaranteed to return (perhaps after a delay) *except* if the
// handler function on the server side does not return.  Thus there
// is no need to implement your own timeouts around Call().
//
// look at the comments in ../labrpc/labrpc.go for more details.
//
// if you're having trouble getting RPC to work, check that you've
// capitalized all field names in structs passed over RPC, and
// that the caller passes the address of the reply struct with &, not
// the struct itself.
//
func (rf *Raft) sendRequestVote(server int, args *RequestVoteArgs, reply *RequestVoteReply) bool {
	ok := rf.peers[server].Call("Raft.RequestVote", args, reply)
	return ok
}

func (rf *Raft) sendAppendEntries(server int, args *AppendEntriesArgs, reply *AppendEntriesReply) bool {
	ok := rf.peers[server].Call("Raft.AppendEntries", args, reply)
	return ok
}

func (rf *Raft) sendInstallSnapshot(server int, args *InstallSnapshotArgs, reply *InstallSnapshotReply) bool {
	ok := rf.peers[server].Call("Raft.InstallSnapshot", args, reply)
	return ok
}

//
// the service using Raft (e.g. a k/v server) wants to start
// agreement on the next command to be appended to Raft's log. if this
// server isn't the leader, returns false. otherwise start the
// agreement and return immediately. there is no guarantee that this
// command will ever be committed to the Raft log, since the leader
// may fail or lose an election.
//
// the first return value is the index that the command will appear at
// if it's ever committed. the second return value is the current
// term. the third return value is true if this server believes it is
// the leader.
//
func (rf *Raft) Start(command interface{}) (int, int, bool) {
	index := -1
	term := -1
	isLeader := true

	rf.mu.Lock()
	index = rf.getLastLogIndex() + 1
	term = rf.currentTerm
	isLeader = rf.state == leader
	if isLeader {
		rf.addLogEntry(logEntry{rf.currentTerm, index, command})
		rf.persist()
	}
	rf.mu.Unlock()

	return index, term, isLeader
}

//
// the tester calls Kill() when a Raft instance won't
// be needed again. you are not required to do anything
// in Kill(), but it might be convenient to (for example)
// turn off debug output from this instance.
//
func (rf *Raft) Kill() {
	rf.killServer <- 1
}

//
// the service or tester wants to create a Raft server. the ports
// of all the Raft servers (including this one) are in peers[]. this
// server's port is peers[me]. all the servers' peers[] arrays
// have the same order. persister is a place for this server to
// save its persistent state, and also initially holds the most
// recent saved state, if any. applyCh is a channel on which the
// tester or service expects Raft to send ApplyMsg messages.
// Make() must return quickly, so it should start goroutines
// for any long-running work.
//
func Make(peers []*labrpc.ClientEnd, me int,
	persister *Persister, applyCh chan ApplyMsg) *Raft {
	rf := &Raft{}
	rf.peers = peers
	rf.persister = persister
	rf.me = me

	rf.state = follower
	rf.currentTerm = 0
	rf.votedFor = -1
	rf.addLogEntry(logEntry{0, 0, nil})

	rf.commitIndex = 0
	rf.lastApplied = 0
	rf.applyCh = applyCh
	rf.convertState = make(chan int, 1)
	rf.electionTimeout = time.Duration(rand.Int63n(16)*20+300) * time.Millisecond
	rf.killServer = make(chan int)
	// initialize from state persisted before a crash
	rf.readPersist(persister.ReadRaftState())

	go rf.runService()
	return rf
}

type voteInfo struct {
	mu         sync.Mutex
	voteRecord map[int]bool
	voteCount  int
}

func (rf *Raft) runAsFollower() int {
	rf.mu.Lock()
	rf.state = follower
	rf.mu.Unlock()
	timer := time.NewTimer(rf.electionTimeout)
	for {
		select {
		case <-timer.C:
			raftLog.WithFields(log.Fields{
				"id":          rf.me,
				"currentTerm": rf.currentTerm,
			}).Debug("Follower election timeout.")
			return candidate
		case <-rf.convertState:
			timer.Reset(rf.electionTimeout)
			raftLog.WithFields(log.Fields{
				"id":          rf.me,
				"currentTerm": rf.currentTerm,
			}).Debug("Follower reset election timeout.")
		case <-rf.killServer:
			return quit
		}
	}
}

func (rf *Raft) runAsCandidate() int {
	rf.mu.Lock()
	rf.state = candidate
	rf.mu.Unlock()

	// Initialize vote
	vote := voteInfo{}
	vote.mu.Lock()
	vote.voteRecord = make(map[int]bool)
	for i := 0; i < len(rf.peers); i++ {
		vote.voteRecord[i] = false
	}
	vote.voteCount = 1
	vote.mu.Unlock()

	// Start election
	rf.mu.Lock()
	rf.state = candidate
	rf.currentTerm++
	rf.votedFor = rf.me
	rf.persist()
	lastLogIndex := rf.getLastLogIndex()
	lastLogTerm := 0
	if lastLogIndex != 0 {
		lastLogTerm = rf.getLastLogTerm()
	}
	args := RequestVoteArgs{rf.currentTerm, rf.me, lastLogIndex, lastLogTerm}
	rf.mu.Unlock()

	for n := range rf.peers {
		if n != rf.me {
			go rf.sendRequestVoteAndGetResponse(n, &args, len(rf.peers), &vote)
		}
	}

	select {
	case <-time.After(rf.electionTimeout):
		return candidate
	case state := <-rf.convertState:
		if state == follower {
			return follower
		} else if state == leader {
			return leader
		} else {
			raftLog.WithField("id", rf.me).Panic("Candidate convert to candidate.")
			panic("")
		}
	case <-rf.killServer:
		return quit
	}
}

func (rf *Raft) runAsLeader() int {
	rf.mu.Lock()
	rf.state = leader
	rf.nextIndex = make(map[int]int)
	rf.matchIndex = make(map[int]int)
	index := rf.getLastLogIndex() + 1
	for n := range rf.peers {
		rf.nextIndex[n] = index
		rf.matchIndex[n] = 0
	}
	rf.mu.Unlock()

	for n := range rf.peers {
		if n != rf.me {
			go rf.sendAppendEntriesTo(n)
		}
	}

	for {
		select {
		case <-time.Tick(100 * time.Millisecond):
			for n := range rf.peers {
				if n != rf.me {
					go rf.sendAppendEntriesTo(n)
				}
			}
			raftLog.WithFields(log.Fields{
				"id":           rf.me,
				"currentTerm":  rf.currentTerm,
				"commitIndex":  rf.commitIndex,
				"lastLogIndex": rf.getLastLogIndex(),
			}).Debug("Leader stat.")
		case state := <-rf.convertState:
			if state == follower {
				return follower
			} else {
				raftLog.WithField("id", rf.me).Panic("Leader convert to candidate or leader.")
				panic("")
			}
		case <-rf.killServer:
			return quit
		}
	}
}

func (rf *Raft) runService() {
	nextState := follower
	for {
		if nextState == follower {
			raftLog.WithFields(log.Fields{
				"id":   rf.me,
				"term": rf.currentTerm,
			}).Debug("Convert to follower.")
			nextState = rf.runAsFollower()
		} else if nextState == candidate {
			raftLog.WithFields(log.Fields{
				"id":   rf.me,
				"term": rf.currentTerm,
			}).Debug("Convert to candidate.")
			nextState = rf.runAsCandidate()
		} else if nextState == leader {
			raftLog.WithFields(log.Fields{
				"id":   rf.me,
				"term": rf.currentTerm,
			}).Debug("Convert to leader.")
			nextState = rf.runAsLeader()
		} else {
			return
		}
	}
}

func (rf *Raft) sendAppendEntriesTo(serverIndex int) {
	rf.mu.Lock()
	if rf.state != leader {
		rf.mu.Unlock()
		return
	}
	var isHeartBeat bool
	if rf.getLastLogIndex() >= rf.nextIndex[serverIndex] {
		isHeartBeat = false
	} else {
		isHeartBeat = true
	}

	prevLogTerm := 0
	prevLogIndex := rf.nextIndex[serverIndex] - 1

	if prevLogIndex != 0 {
		e, ok := rf.getLogEntry(prevLogIndex)
		if ok {
			prevLogTerm = e.Term
		} else {
			args := InstallSnapshotArgs{rf.currentTerm, rf.me, rf.lastIncludedLogEntry, rf.persister.ReadSnapshot()}
			reply := InstallSnapshotReply{}
			ok := rf.sendInstallSnapshot(serverIndex, &args, &reply)
			if ok {
				rf.checkCurrentTerm(reply.Term)
				rf.nextIndex[serverIndex] = rf.lastIncludedLogEntry.Index + 1
				rf.matchIndex[serverIndex] = rf.lastIncludedLogEntry.Index
			}
			rf.mu.Unlock()
			return
		}
	}

	var args AppendEntriesArgs
	if isHeartBeat {
		raftLog.WithFields(log.Fields{
			"leaderId":   rf.me,
			"receiverId": serverIndex,
			"term":       rf.currentTerm,
			"len of log": rf.getLogLen(),
		}).Debug("Leader send heartBeat.")
		args = AppendEntriesArgs{rf.currentTerm, rf.me, prevLogIndex, prevLogTerm, nil, rf.commitIndex}
	} else {
		if _, ok := rf.getLogEntry(prevLogIndex + 1); !ok {
			args := InstallSnapshotArgs{rf.currentTerm, rf.me, rf.lastIncludedLogEntry, rf.persister.ReadSnapshot()}
			reply := InstallSnapshotReply{}
			ok := rf.sendInstallSnapshot(serverIndex, &args, &reply)
			if ok {
				rf.checkCurrentTerm(reply.Term)
				rf.nextIndex[serverIndex] = rf.lastIncludedLogEntry.Index + 1
				rf.matchIndex[serverIndex] = rf.lastIncludedLogEntry.Index
			}
			rf.mu.Unlock()
			return
		}

		raftLog.WithFields(log.Fields{
			"leaderId":   rf.me,
			"receiverId": serverIndex,
			"term":       rf.currentTerm,
		}).Debug("Leader send entries.")
		args = AppendEntriesArgs{rf.currentTerm, rf.me, prevLogIndex, prevLogTerm, rf.getEntryFrom(prevLogIndex + 1), rf.commitIndex}
	}
	rf.mu.Unlock()
	reply := AppendEntriesReply{}

	ok := rf.sendAppendEntries(serverIndex, &args, &reply)
	if !ok {
		return
	}
	rf.mu.Lock()
	// Make sure the reply is not obsolete.
	if args.Term < rf.currentTerm {
		rf.mu.Unlock()
		return
	}
	rf.checkCurrentTerm(reply.Term)
	if rf.state == follower {
		rf.mu.Unlock()
		return
	}
	if reply.Success {
		//DPrintf("appendEntries success")
		rf.nextIndex[serverIndex] = args.PrevLogIndex + len(args.Entries) + 1
		rf.matchIndex[serverIndex] = args.PrevLogIndex + len(args.Entries)
		// Try to update commitIndex
		n := rf.matchIndex[serverIndex]
		if e, _ := rf.getLogEntry(n); n > rf.commitIndex && e.Term == rf.currentTerm {
			countOfServer := len(rf.peers)
			countOfMatch := 1
			for i := range rf.peers {
				if rf.matchIndex[i] >= n {
					countOfMatch++
				}
			}
			if countOfMatch > countOfServer/2 {
				rf.commitIndex = n
				rf.applyLogToStateMachine()
			}
		}
		rf.mu.Unlock()
	} else {
		if args.Term >= reply.Term {
			if reply.ConflictTerm == -1 {
				rf.nextIndex[serverIndex] = reply.ConflictIndex
			} else {
				find := false
				index := 0
				for i := 1; i < rf.getLastLogIndex()+1; i++ {
					if e, _ := rf.getLogEntry(i); e.Term == reply.ConflictTerm {
						find = true
						index = i
						break
					}
				}
				if find {
					i := index
					for ; i < rf.getLastLogIndex()+1; i++ {
						if rf.getLogEntryOk(i).Term != reply.ConflictTerm {
							break
						}
					}
					rf.nextIndex[serverIndex] = i
				} else {
					rf.nextIndex[serverIndex] = reply.ConflictIndex
				}
			}
			rf.mu.Unlock()
			rf.sendAppendEntriesTo(serverIndex)
		} else {
			rf.mu.Unlock()
		}
	}
}

func (rf *Raft) sendRequestVoteAndGetResponse(serverIndex int, args *RequestVoteArgs, countOfServer int, vote *voteInfo) {
	reply := RequestVoteReply{}
	ok := rf.sendRequestVote(serverIndex, args, &reply)

	if !ok {
		return
	}
	rf.mu.Lock()
	rf.checkCurrentTerm(reply.Term)
	if rf.state == candidate && reply.VoteGranted == true {
		if args.Term == rf.currentTerm {
			vote.mu.Lock()
			if vote.voteRecord[serverIndex] == false {
				vote.voteRecord[serverIndex] = true
				vote.voteCount++
				raftLog.WithFields(log.Fields{
					"candidateId": rf.me,
					"from":        serverIndex,
				}).Debug("Candidate receives vote.")
				if vote.voteCount > countOfServer/2 {
					if len(rf.convertState) == 0 {
						rf.state = leader
						rf.convertState <- leader
					} else {
						<-rf.convertState
						rf.state = leader
						rf.convertState <- leader
					}
				}
			}
			vote.mu.Unlock()
		}
	}
	rf.mu.Unlock()
}

func (rf *Raft) checkCurrentTerm(term int) {
	if term > rf.currentTerm {
		rf.currentTerm = term
		rf.votedFor = -1
		rf.persist()
		if len(rf.convertState) == 0 {
			if rf.state != follower {
				rf.state = follower
				rf.convertState <- follower
			}
		}
	}
}

func (rf *Raft) grantVote(args *RequestVoteArgs, reply *RequestVoteReply) {
	reply.VoteGranted = true
	rf.votedFor = args.CandidateId
	rf.persist()
	if rf.state == follower {
		if len(rf.convertState) == 0 {
			rf.convertState <- follower
		}
	}
}

// Caller need to lock the rf
func (rf *Raft) applyLogToStateMachine() {
	for rf.commitIndex > rf.lastApplied {
		rf.lastApplied++
		e, ok := rf.getLogEntry(rf.lastApplied)
		if ok {
			rf.applyCh <- ApplyMsg{e.Index, e.Command, false, nil}
		}
	}
}
