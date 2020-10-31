package raftkv

import "labrpc"
import "crypto/rand"
import (
	"math/big"
	"sync"
	"time"
)

var clientId int = 0
var mu sync.Mutex

type Clerk struct {
	servers []*labrpc.ClientEnd
	// You will have to modify this struct.
	leader int
	seq int
	id int
	mu  sync.Mutex
}

func nrand() int64 {
	max := big.NewInt(int64(1) << 62)
	bigx, _ := rand.Int(rand.Reader, max)
	x := bigx.Int64()
	return x
}

func MakeClerk(servers []*labrpc.ClientEnd) *Clerk {
	ck := new(Clerk)
	ck.servers = servers
	// You'll have to add code here.
	ck.leader = -1
	mu.Lock()
	ck.id = clientId
	clientId++
	mu.Unlock()
	return ck
}

//
// fetch the current value for a key.
// returns "" if the key does not exist.
// keeps trying forever in the face of all other errors.
//
// you can send an RPC with code like this:
// ok := ck.servers[i].Call("RaftKV.Get", &args, &reply)
//
// the types of args and reply (including whether they are pointers)
// must match the declared types of the RPC handler function's
// arguments. and reply must be passed as a pointer.
//
func (ck *Clerk) Get(key string) string {

	// You will have to modify this function.
	ck.mu.Lock()
	seq := ck.seq
	ck.seq++
	ck.mu.Unlock()
	args := GetArgs{key, ck.id, seq}

	for  {
		var addr int
		if ck.leader == -1 {
			addr = int(int(nrand()) % (len(ck.servers)))
		} else {
			addr = ck.leader
		}
		reply := GetReply{}
		ok := ck.servers[addr].Call("RaftKV.Get", &args, &reply)
		if ok && reply.WrongLeader == false {
			ck.leader = addr
			return reply.Value
		} else {
			time.Sleep(100 * time.Millisecond)
			ck.leader = -1
		}
	}
}

//
// shared by Put and Append.
//
// you can send an RPC with code like this:
// ok := ck.servers[i].Call("RaftKV.PutAppend", &args, &reply)
//
// the types of args and reply (including whether they are pointers)
// must match the declared types of the RPC handler function's
// arguments. and reply must be passed as a pointer.
//
func (ck *Clerk) PutAppend(key string, value string, op string) {
	// You will have to modify this function.
	ck.mu.Lock()
	seq := ck.seq
	ck.seq++
	ck.mu.Unlock()
	args := PutAppendArgs{key, value, op, ck.id, seq}

	for {
		var addr int
		if ck.leader == -1 {
			addr = int(int(nrand()) % (len(ck.servers)))
		} else {
			addr = ck.leader
		}
		reply := PutAppendReply{}
		ok := ck.servers[addr].Call("RaftKV.PutAppend", &args, &reply)
		if ok && reply.WrongLeader == false {
			ck.leader = addr
			return
		} else {
			time.Sleep(100 * time.Millisecond)
			ck.leader = -1
		}
	}
}

func (ck *Clerk) Put(key string, value string) {
	ck.PutAppend(key, value, "Put")
}
func (ck *Clerk) Append(key string, value string) {
	ck.PutAppend(key, value, "Append")
}
