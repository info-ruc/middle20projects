
pid=`ps -efj | grep bin/simpledb-svr | head -n 1 | awk '{print $2}'`

kill -9 $pid
