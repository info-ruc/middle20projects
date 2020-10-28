#include "db.h"
#include "utils.h"

#ifndef __LOG_FLAG
#define __LOG_FLAG

#define LOG_DEBUG      0
#define LOG_INFO       1
#define LOG_WARN       2
#define LOG_ERR        3

#define MAXLOGLEN      1024
#define MAXTIMEBUF     64

#endif

using namespace std;

int epfd;

void* handleRequests(void* arg);
RcvData getResponse(SendData* sdata);
bool isAccessOK(char* user, char* dbname, int accessbits, int* ap);
bool isTableExist(char* tablename);
bool isUserExist(char* user);
int createTable(char* user, char* tablename);
void grant_access(char* user, char* tablename, int getbit, int setbit, int replbit, int delbit);
void revoke_access(char* user, char* tablename, int minusgb, int minussb, int minusrb, int minusdb);

int main(int argc, char* argv[]) {
    int					listenfd, connfd;
	socklen_t			len;
	struct sockaddr_in	servaddr, cliaddr;
    pid_t               childpid;
    pthread_t           pid;
    char                *cmd;
    struct sigaction    sa;

    /*
     * Daemonize the server
     */
    if ((cmd = strrchr(argv[0], '/')) == NULL)
        cmd = argv[0];
    else
        cmd++;
    daemonize(cmd);

    /*
     * Create a listenfd and bind it to the ip:port
     */
    if ((listenfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        log(LOG_ERR, "socket error");
        exit(1);
    }

	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family      = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servaddr.sin_port        = htons(23790);	/* daytime server */

	if(bind(listenfd, (SA *) &servaddr, sizeof(servaddr)) < 0) {
        log(LOG_ERR, "main: bind error");
        exit(1);
	}

	if(listen(listenfd, LISTENQ) < 0) {
        log(LOG_ERR, "main: listen error");
        exit(1);
	}

	/*
	 * Epoll_create a epollfd
	 */
    epfd = epoll_create(1);
    if (epfd == -1) {
        log(LOG_ERR, "main: epoll_create error");
        exit(1);
    }

	/*
	 * Child thread for handling sockfd request
	 */
    pthread_create(&pid, NULL, handleRequests, NULL);

	for ( ; ; ) {
		len = sizeof(cliaddr);
		if((connfd = accept(listenfd, (SA *) &cliaddr, &len)) < 0) {
            log(LOG_ERR, "accept error");
            exit(1);
		}

        struct epoll_event ev;
        ev.events = EPOLLIN | EPOLLRDHUP | EPOLLHUP | EPOLLERR;
        ev.data.fd = connfd;
        if (epoll_ctl(epfd, EPOLL_CTL_ADD, connfd, &ev) == -1) {
            log(LOG_ERR, "main: epoll_ctl error");
            exit(1);
        }
	}

    return 0;
}


void* handleRequests(void* arg) {
    int ready, n;
    struct epoll_event evlist[MAX_EVENTS];
    char rcvline[MAXLINE], loginfo[MAXLOGLEN];
    SendData sdata;

    while (true) {
        /*
         * ready is the number of ready events
         */
        ready = epoll_wait(epfd, evlist, MAX_EVENTS, -1);
        if (ready == -1) {
            if (errno == EINTR) {
                continue;
            } else {
                log(LOG_ERR, "main: epoll_wait");
                exit(1);
            }
        }

        /*
         * handle each ready sockfd sequencially
         */
        for (int j = 0; j < ready; ++j) {
            if (evlist[j].events & EPOLLIN) {
                if ((n = readn(evlist[j].data.fd, (char*)&sdata, sizeof(SendData))) < 0) {
                    log(LOG_ERR, "main: read error");
                    exit(1);
                } else if (n == 0) {
                    if(close(evlist[j].data.fd) == -1) {
                        log(LOG_ERR, "main: close error");
                        exit(1);
                    }
                }

                RcvData rdata = getResponse(&sdata);
                write(evlist[j].data.fd, (char*)&rdata, sizeof(rdata));
                /*
                 * log the input/output info
                 */
                sprintf(loginfo, "%s:%s:%s=>%s", sdata.user, sdata.cmd, sdata.dbname, rdata.result);
                log(LOG_INFO, loginfo);
            } else if (evlist[j].events & (EPOLLHUP | EPOLLERR | EPOLLRDHUP)) {
                if (close(evlist[j].data.fd) == -1) {
                    log(LOG_ERR, "main: close error");
                    exit(1);
                }
            } else {
                log(LOG_INFO, "not handled EPOLL event\n");
            }
        }
    }

    return NULL;
}

RcvData getResponse(SendData* sdatap) {
    char* cmd = sdatap->cmd;
    char path[PATHLEN];

    sprintf(path, "db/%s", sdatap->dbname);
    SimpleDB sdb = SimpleDB(path, OFLAG, MODE);
    RcvData rdata;

    /*
     * TODO: for login user, also check every 10 min.
     */
    if (strcmp(cmd, "login") == 0) {
        int rc = verify(sdatap->user, sdatap->passwd);
        if (rc < 0) {
            strcpy(rdata.result, "login failed.\n");
            rdata.id = 1;
            return rdata;
        }
        strcpy(rdata.result, "login succeed.\n");
        rdata.id = 0;
        return rdata;

    } else if (strcmp(cmd, "use") == 0) {
        int hasbits = 0;
        if (!isAccessOK(sdatap->user, sdatap->key, (1<<3) + (1<<2) + (1<<1) + 1, &hasbits)) {
            strcpy(rdata.result, "table change fail.\n");
            rdata.id = 1;
            return rdata;
        }
        char rs[MAXLINE + MAXDBNAMELEN];
        sprintf(rs, "table has changed to %s\n", sdatap->key);
        strcpy(rdata.result, rs);
        rdata.id = 0;
        rdata.accessbits = hasbits;
        return rdata;

    } else if (strcmp(cmd, "ctable") == 0) {
        if (isTableExist(sdatap->key)) {  /* table has existed, so not create*/
            strncpy(rdata.result, "table has already existed.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }

        if(createTable(sdatap->user, sdatap->key) < 0) {
            strncpy(rdata.result, "create table failed.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }

        strncpy(rdata.result, "create table successfully.\n", MAXLINE);
        rdata.id = 0;
        return rdata;

    } else if (strcmp(cmd, "grant") == 0) {
        if (!isUserExist(sdatap->value) || (strcmp(sdatap->user, sdatap->value) == 0)) {
            strncpy(rdata.result, "user not exist OR grant to yourself fail.\n", MAXLINE);
            log(LOG_ERR, sdatap->value);
            log(LOG_ERR, sdatap->user);
            rdata.id = 1;
            return rdata;
        }
        if (!isTableExist(sdatap->value2)) {
            strncpy(rdata.result, "table not exist.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }

        int getbit = 0, setbit = 0, replbit = 0, delbit = 0;
        if (strcmp(sdatap->key, "get") == 0)        getbit  = 1;
        else if (strcmp(sdatap->key, "set") == 0)   setbit  = 1;
        else if (strcmp(sdatap->key, "repl") == 0)  replbit = 1;
        else if (strcmp(sdatap->key, "del") == 0)   delbit  = 1;
        grant_access(sdatap->value, sdatap->value2, getbit, setbit, replbit, delbit);

        strncpy(rdata.result, "grant access successfully.\n", MAXLINE);
        rdata.id = 0;
        return rdata;
    } else if (strcmp(cmd, "revoke") == 0) {
        if (!isUserExist(sdatap->value) || (strcmp(sdatap->user, sdatap->value) == 0)) {
            strncpy(rdata.result, "user not exist OR grant to yourself fail.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }
        if (strcmp(sdatap->user, "simpledb") != 0) {
            strncpy(rdata.result, "have no previledge to revoke.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }
        if (!isTableExist(sdatap->value2)) {
            strncpy(rdata.result, "table not exist.\n", MAXLINE);
            rdata.id = 1;
            return rdata;
        }

        int minusgb = 0, minussb = 0, minusrb = 0, minusdb = 0;
        if (strcmp(sdatap->key, "get") == 0)        minusgb  = 1;
        else if (strcmp(sdatap->key, "set") == 0)   minussb  = 1;
        else if (strcmp(sdatap->key, "repl") == 0)  minusrb = 1;
        else if (strcmp(sdatap->key, "del") == 0)   minusdb  = 1;
        revoke_access(sdatap->value, sdatap->value2, minusgb, minussb, minusrb, minusdb);

        strncpy(rdata.result, "revoke access successfully.\n", MAXLINE);
        rdata.id = 0;
        return rdata;
    } else if (strcmp(cmd, "get") == 0) {
        char *res = sdb.fetch(sdatap->key);
        strcpy(rdata.result, res == NULL ? "(WARN) key not exist." : res);

    } else if (strcmp(cmd, "set") == 0) {
        int rc = sdb.store(sdatap->key, sdatap->value, DB_STORE);
        strcpy(rdata.result, rc < 0 ? "(ERROR) store failed." : "(OK)");

    } else if (strcmp(cmd, "del") == 0) {
        int rc = sdb.del(sdatap->key);
        strcpy(rdata.result, rc < 0 ? "(ERROR) del failed." : "(OK)");

    } else if (strcmp(cmd, "repl") == 0) {
        int rc = sdb.store(sdatap->key, sdatap->value, DB_REPLACE);
        strcpy(rdata.result, rc < 0 ? "(ERROR) replace failed." : "(OK)");
    }
    int len = strlen(rdata.result);
    rdata.result[len] = '\n';
    rdata.result[len+1] = 0;
    rdata.id = 0;

    return rdata;
}
