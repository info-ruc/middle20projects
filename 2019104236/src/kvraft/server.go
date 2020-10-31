package raftkv

import (
	"bytes"
	"encoding/gob"
	"labrpc"
	"raft"
	"sync"
	"time"
	"github.com/sirupsen/logrus"
)

type Op struct {
	Key       string
	Value     string
	Op        string
	ClientId  int
	ClientSeq int
}

type callBackChan struct {
	ch      chan string
	mu      sync.Mutex
	listen  bool
	wantSeq int
}

type RaftKV struct {
	mu      sync.Mutex
	me      int
	rf      *raft.Raft
	applyCh chan raft.ApplyMsg

	maxraftstate int // snapshot if log grows this big

	db       map[string]string
	callBack map[int]*callBackChan
	seqCheck map[int]int
}

func (kv *RaftKV) Get(args *GetArgs, reply *GetReply) {
	log.WithFields(log.Fields{
		"serverId": kv.me,
		"clientId": args.Id,
		"clientSeq": args.Seq,
		"key": args.Key,
	}).Debug("Receive get request.")
	if _, isLeader := kv.rf.GetState(); !isLeader {
		reply.WrongLeader = true
		log.WithFields(log.Fields{
			"serverId": kv.me,
			"clientId": args.Id,
			"clientSeq": args.Seq,
			"key": args.Key,
			"error": "WrongLeader",
		}).Debug("Reject get request.")
		return
	}
	kv.mu.Lock()
	id := args.Id
	if _, ok := kv.callBack[id]; !ok {
		kv.callBack[id] = &callBackChan{make(chan string), *new(sync.Mutex), false, 0}
	}
	log.WithFields(log.Fields{
		"serverId": kv.me,
		"clientId": args.Id,
		"clientSeq": args.Seq,
		"key": args.Key,
	}).Debug("Start get request.")
	kv.rf.Start(Op{args.Key, "", "Get", id, args.Seq})
	kv.mu.Unlock()
	kv.listenCallBack(id, args.Seq)
	select {
	case value := <-kv.callBack[id].ch:
		log.WithFields(log.Fields{
			"serverId": kv.me,
			"clientId": args.Id,
			"clientSeq": args.Seq,
			"key": args.Key,
			"value": value,
		}).Debug("Get success.")
		reply.Err = OK
		reply.Value = value
		return
	case <-time.After(2000 * time.Millisecond):
		kv.unlistenCallBack(id)
		reply.WrongLeader = true
		return
	}
}

func (kv *RaftKV) PutAppend(args *PutAppendArgs, reply *PutAppendReply) {
	log.WithFields(log.Fields{
		"serverId": kv.me,
		"clientId": args.Id,
		"clientSeq": args.Seq,
		"key": args.Key,
		"value": args.Value,
		"method": args.Op,
	}).Debug("Receive putAppend request.")
	if _, isLeader := kv.rf.GetState(); !isLeader {
		reply.WrongLeader = true
		log.WithFields(log.Fields{
			"serverId": kv.me,
			"clientId": args.Id,
			"clientSeq": args.Seq,
			"key": args.Key,
			"value": args.Value,
			"method": args.Op,
			"error": "WrongLeader",
		}).Debug("Reject putAppend request.")
		return
	}
	kv.mu.Lock()
	id := args.Id
	if _, ok := kv.callBack[id]; !ok {
		kv.callBack[id] = &callBackChan{make(chan string), *new(sync.Mutex), false, 0}
	}
	log.WithFields(log.Fields{
		"serverId": kv.me,
		"clientId": args.Id,
		"clientSeq": args.Seq,
		"key": args.Key,
		"value": args.Value,
		"method": args.Op,
	}).Debug("Start putAppend request.")
	kv.rf.Start(Op{args.Key, args.Value, args.Op, id, args.Seq})
	kv.mu.Unlock()
	kv.listenCallBack(id, args.Seq)
	select {
	case <-kv.callBack[id].ch:
		log.WithFields(log.Fields{
			"serverId": kv.me,
			"clientId": args.Id,
			"clientSeq": args.Seq,
			"key": args.Key,
			"value": args.Value,
			"method": args.Op,
		}).Debug("PutAppend success.")
		reply.Err = OK
		return
	case <-time.After(2000 * time.Millisecond):
		kv.unlistenCallBack(id)
		reply.WrongLeader = true
		return
	}
}

//
// the tester calls Kill() when a RaftKV instance won't
// be needed again. you are not required to do anything
// in Kill(), but it might be convenient to (for example)
// turn off debug output from this instance.
//
func (kv *RaftKV) Kill() {
	kv.rf.Kill()
	// Your code here, if desired.
}

//
// servers[] contains the ports of the set of
// servers that will cooperate via Raft to
// form the fault-tolerant key/value service.
// me is the index of the current server in servers[].
// the k/v server should store snapshots with persister.SaveSnapshot(),
// and Raft should save its state (including log) with persister.SaveRaftState().
// the k/v server should snapshot when Raft's saved state exceeds maxraftstate bytes,
// in order to allow Raft to garbage-collect its log. if maxraftstate is -1,
// you don't need to snapshot.
// StartKVServer() must return quickly, so it should start goroutines
// for any long-running work.
//
func StartKVServer(servers []*labrpc.ClientEnd, me int, persister *raft.Persister, maxraftstate int) *RaftKV {
	// call gob.Register on structures you want
	// Go's RPC library to marshall/unmarshall.
	gob.Register(Op{})

	kv := new(RaftKV)
	kv.me = me
	kv.maxraftstate = maxraftstate

	// You may need initialization code here.

	kv.applyCh = make(chan raft.ApplyMsg)
	kv.rf = raft.Make(servers, me, persister, kv.applyCh)
	kv.db = make(map[string]string)
	kv.callBack = make(map[int]*callBackChan)
	kv.seqCheck = make(map[int]int)

	if persister.SnapshotSize() != 0 {
		r := bytes.NewBuffer(persister.ReadSnapshot())
		d := gob.NewDecoder(r)
		d.Decode(&kv.seqCheck)
		d.Decode(&kv.db)
	}

	// You may need initialization code here.

	go kv.runServer(servers, me, persister, maxraftstate)

	return kv
}

func (kv *RaftKV) runServer(servers []*labrpc.ClientEnd, me int, persister *raft.Persister, maxraftstate int) {
	for {
		select {
		case m := <-kv.applyCh:
			if m.UseSnapshot {
				log.WithField("serverId", kv.me).Debug("Install snapshot.")
				r := bytes.NewBuffer(m.Snapshot)
				d := gob.NewDecoder(r)
				d.Decode(&kv.seqCheck)
				d.Decode(&kv.db)
				break
			}

			method := m.Command.(Op).Op
			key := m.Command.(Op).Key
			id := m.Command.(Op).ClientId
			seq := m.Command.(Op).ClientSeq
			exec := true
			callback := true

			if lastSeq, ok := kv.seqCheck[id]; ok && seq < lastSeq+1 {
				exec = false
			} else {
				kv.seqCheck[id] = seq
			}
			if _, ok := kv.callBack[id]; !ok {
				callback = false
			} else {
				kv.callBack[id].mu.Lock()
				if seq != kv.callBack[id].wantSeq {
					callback = false
				}
				if !kv.callBack[id].listen {
					callback = false
				} else {
					if callback {
						kv.callBack[id].listen = false
					}
				}
			}
			if method == "Get" {
				if callback {
					reply := kv.db[key]
					//DPrintf("server %d Get read: key %s, value %s, clientId %d, seq %d", kv.me, key, reply, id, seq)
					kv.callBack[m.Command.(Op).ClientId].ch <- reply
				}
			} else if method == "Put" {
				if exec {
					kv.db[key] = m.Command.(Op).Value
				}
				if callback {
					kv.callBack[m.Command.(Op).ClientId].ch <- ""
					//DPrintf("server %d putAppend ok, key %s, clientId %d, seq %d", kv.me, key, id, seq)
				}
			} else {
				if exec {
					kv.db[key] = kv.db[key] + m.Command.(Op).Value
				}
				if callback {
					kv.callBack[m.Command.(Op).ClientId].ch <- ""
					//DPrintf("server %d putAppend ok, key %s, clientId %d, seq %d", kv.me, key, id, seq)
				}
			}
			if _, ok := kv.callBack[id]; ok {
				kv.callBack[id].mu.Unlock()
			}

			if kv.maxraftstate != -1 && persister.RaftStateSize() >= kv.maxraftstate {
				w := new(bytes.Buffer)
				e := gob.NewEncoder(w)
				e.Encode(kv.seqCheck)
				e.Encode(kv.db)
				data := w.Bytes()
				persister.SaveSnapshot(data)
				log.WithFields(log.Fields{
					"serverId": kv.me,
					"size of log": persister.RaftStateSize(),
				}).Debug("Server starts to compaction.")
				kv.rf.Compaction(m.Index)
			}
		}
	}
}

func (kv *RaftKV) listenCallBack(id int, wantSeq int) {
	kv.callBack[id].mu.Lock()
	kv.callBack[id].listen = true
	kv.callBack[id].wantSeq = wantSeq
	kv.callBack[id].mu.Unlock()
}

func (kv *RaftKV) unlistenCallBack(id int) {
	kv.callBack[id].mu.Lock()
	kv.callBack[id].listen = false
	kv.callBack[id].mu.Unlock()
}