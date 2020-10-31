package raftkv

import "github.com/sirupsen/logrus"

const (
	OK       = "OK"
	ErrNoKey = "ErrNoKey"
)

type Err string

// Put or Append
type PutAppendArgs struct {
	Key   string
	Value string
	Op    string // "Put" or "Append"
	Id    int
	Seq   int
}

type PutAppendReply struct {
	WrongLeader bool
	Err         Err
}

type GetArgs struct {
	Key string
	Id    int
	Seq   int
}

type GetReply struct {
	WrongLeader bool
	Err         Err
	Value       string
}

func init() {
	log.SetLevel(log.DebugLevel)
}