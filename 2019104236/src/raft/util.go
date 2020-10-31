package raft

import "github.com/sirupsen/logrus"

var raftLog = log.New()

func init() {
	raftLog.SetLevel(log.InfoLevel)
}