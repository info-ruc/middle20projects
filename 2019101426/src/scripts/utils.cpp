#include "utils.h"
#include "db.h"


const char *logname      = "log/simpledb.log";
const char *pwdfname     = "passwd";
const char *accessfname  = "access";
const char *tablesfname  = "tables";

extern int
writen(int fd, const char* buffer, size_t n) {
    int nwrite, twrite;
    const char *buf;

    buf = buffer;
    for (twrite = 0; twrite < n;) {
        nwrite = write(fd, buf, n - twrite);

        if (nwrite <= 0) {
            if (nwrite == -1 && errno == EINTR)
                continue;
            else
                return -1;
        }
        twrite += nwrite;
        buf += nwrite;
    }

    return twrite;
}

extern int
readn(int fd, char* buffer, size_t n) {
    int nread, tread;
    char* buf;

    buf = buffer;
    for (tread = 0; tread < n; ) {
        nread = read(fd, buf, n - tread);

        if (nread == 0) {
            return tread;
        }
        if (nread == -1) {
            if (errno == EINTR) {
                continue;
            } else {
                return -1;
            }
        }
        tread += nread;
        buf += nread;
    }
    return tread;
}

extern void
daemonize(const char* cmd) {
    int i, fd0, fd1, fd2;
    pid_t pid;
    struct rlimit r1;
    struct sigaction sa;

    /*
     * Clear file creation mask
     */
    umask(0);

    /*
     * Get maximum number of file descriptor
     */
    if (getrlimit(RLIMIT_NOFILE, &r1) < 0) {
        log(LOG_ERR, "can't get file limit");
        exit(1);
    }

    if ((pid = fork()) < 0) {
        log(LOG_ERR, "can't fork");
        exit(1);
    }
    else if (pid != 0)  /* parent */
        exit(0);

    setsid();

    /*
     * Ensure future opens won't allocate controlling TTYs
     */
    sa.sa_handler = SIG_IGN;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    if (sigaction(SIGHUP, &sa, NULL) < 0) {
        log(LOG_ERR, "can't ignore SIGHUP");
        exit(1);
    }

    if ((pid = fork()) < 0) {
        log(LOG_ERR, "can't fork");
        exit(1);
    }
    else if (pid != 0)
        exit(0);

    /*
     * Close all open file descriptors
     */
    if (r1.rlim_max == RLIM_INFINITY)
        r1.rlim_max = 1024;
    for (i = 0; i < r1.rlim_max; ++i) {
        close(i);
    }

    /*
     * Attach file descriptors 0, 1, 2 to /dev/null
     */
    fd0 = open("/dev/null", O_RDWR);
    fd1 = dup(0);
    fd2 = dup(0);
}


extern void
log(int loglevel, const char* msg) {
    int fd = open(logname, OFLAG, MODE);
    char loginfo[MAXLOGLEN];
    time_t t;
    struct tm *tmp;
    char timebuf[MAXTIMEBUF];

    time(&t);
    tmp = localtime(&t);
    strftime(timebuf, MAXTIMEBUF,  "%r, %a %b %d, %Y", tmp);

    sprintf(loginfo, "%s %d %s\n", timebuf, loglevel, msg);

    lseek(fd, 0, SEEK_END);
    write(fd, loginfo, strlen(loginfo));
    close(fd);
}


extern int
verify(char* name, char*passwd) {
    FILE* fp = fopen(pwdfname, "r");
    char namebuf[MAXUSERLEN], pwdbuf[MAXPASSWDLEN];
    char* buf = NULL;
    size_t len = MAXLINE;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }
    while (getline(&buf, &len, fp) > 0) {
        sscanf(buf, "%s %s", namebuf, pwdbuf);

        if ( (strcmp(name, namebuf) == 0) && (strcmp(passwd, pwdbuf) == 0) ) {
            return 0;
        }
    }

    free(buf);
    fclose(fp);

    return -1;
}


bool isAccessOK(char* user, char* tablename, int accessbits, int *ap)
{
    if (strcmp(tablename, "default") == 0) {
        *ap = (1<<3) | (1<<2) | (1<<1) | 1;
        return true; /* default table is open */
    }

    FILE* fp = fopen(accessfname, "r");
    char userbuf[MAXUSERLEN], tablebuf[MAXDBNAMELEN];
    char* buf = NULL;
    size_t len = MAXLINE;
    int usebit = 0, getbit = 0, setbit = 0, replbit = 0, delbit = 0;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }

    /* dataline: usebit username get_bit set_bit repl_bit del_bit tablename */
    while (getline(&buf, &len, fp) > 0) {
        int n;

        if((n = sscanf(buf, "%d %s %d %d %d %d %s\n",
                    &usebit, userbuf, &getbit, &setbit, &replbit, &delbit, tablebuf)) != 7){
            log(LOG_ERR, "n is not right\n");
            exit(1);
        }

        if ( usebit &&
             (strcmp(user, userbuf) == 0) &&
             (strcmp(tablename, tablebuf) == 0) ) {
            if ( accessbits | (getbit << 3) | (setbit << 2) | (replbit << 1) | delbit) {
                *ap = (getbit << 3) | (setbit << 2) | (replbit << 1) | delbit;
                return true;
            } else {
                return false;
            }
        }
    }

    free(buf);
    fclose(fp);

    return false;
}

bool isTableExist(char* tablename) {
    FILE* fp = fopen(tablesfname, "r");
    char tablebuf[MAXDBNAMELEN];
    char* buf = NULL;
    size_t len = MAXLINE;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }

    /* dataline: usebit username getbit setbit replbit delbit tablename */
    while (getline(&buf, &len, fp) > 0) {
        if(sscanf(buf, "%s\n", tablebuf) != 1){
            continue;
        }

        if (strcmp(tablename, tablebuf) == 0) {
            return true;
        }
    }

    free(buf);
    fclose(fp);

    return false;
}

int createTable(char* user, char* tablename)
{
    int fd;
    char buf[MAXDBNAMELEN + MAXUSERLEN + 10];

    /*
     * add a tablename in `tables` file.
     */
    if ((fd = open(tablesfname, OFLAG, MODE)) < 0) {
        log(LOG_ERR, "createTable: open fail.");
        exit(1);
    }
    if(lseek(fd, 0, SEEK_END) < 0) {
        log(LOG_ERR, "createTable: lseek fail.");
        exit(1);
    }

    if (strncpy(buf, tablename, MAXDBNAMELEN + MAXUSERLEN + 10) < 0) {
        log(LOG_ERR, "createTable: strncpy fail.");
        exit(1);
    }
    strcat(buf, "\n");
    write(fd, buf, strlen(buf));
    close(fd);

    /*
     * add a access line in `access` file.
     */
    if ((fd = open(accessfname, OFLAG, MODE)) < 0) {
        log(LOG_ERR, "createTable: open fail.");
        exit(1);
    }
    if (lseek(fd, 0, SEEK_END) < 0) {
        log(LOG_ERR, "createTable: lseek fail.");
        exit(1);
    }
    sprintf(buf, "%d %s %d %d %d %d %s\n", 1, user, 1, 1, 1, 1, tablename);
    write(fd, buf, strlen(buf));
    close(fd);

    return 0;
}

void grant_access(char* user, char* tablename, int gb, int sb, int rb, int db) {
    if (strcmp(tablename, "default") == 0) return; /* default table is open */

    FILE* fp = fopen(accessfname, "r+");
    char userbuf[MAXUSERLEN], tablebuf[MAXDBNAMELEN];
    char* buf = NULL;
    size_t len = MAXLINE;
    int usebit = 0, getbit = 0, setbit = 0, replbit = 0, delbit = 0;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }

    int flag = 0;
    int pos = ftell(fp);
    /* dataline: usebit username get_bit set_bit repl_bit del_bit tablename */
    while (getline(&buf, &len, fp) > 0) {
        int n;

        if((n = sscanf(buf, "%d %s %d %d %d %d %s\n",
                    &usebit, userbuf, &getbit, &setbit, &replbit, &delbit, tablebuf)) != 7){
            log(LOG_ERR, "n is not right\n");
            exit(1);
        }

        if ( (strcmp(user, userbuf) == 0) &&
             (strcmp(tablename, tablebuf) == 0) ) {
            if (fseek(fp, pos, SEEK_SET) < 0) {
                log(LOG_ERR, "grant_access: fseek failed.");
                exit(1);
            }
            if (usebit == 1) {
                getbit |= gb; setbit |= sb; replbit |= rb; delbit |= db;
                sprintf(buf, "%d %s %d %d %d %d %s\n",
                        usebit, user, getbit, setbit, replbit, delbit, tablebuf);
                fwrite(buf, strlen(buf), 1, fp);
                log(LOG_ERR, "flag-1\n");
            } else {
                usebit = 1;
                getbit = gb; setbit = sb; replbit = rb; delbit = db;
                sprintf(buf, "%d %s %d %d %d %d %s\n",
                        usebit, user, getbit, setbit, replbit, delbit, tablebuf);
                fwrite(buf, strlen(buf), 1, fp);
                log(LOG_ERR, "flag0\n");
            }
            flag = 1;
            break;
        }
        pos = ftell(fp);
    }

    if (flag == 0) {
        if (fseek(fp, 0, SEEK_END) < 0) {
            log(LOG_ERR, "grant_access: fseek failed.");
            exit(1);
        }
        usebit = 1;
        getbit = gb; setbit = sb; replbit = rb; delbit = db;
        sprintf(buf, "%d %s %d %d %d %d %s\n",
            usebit, user, getbit, setbit, replbit, delbit, tablename);
        fwrite(buf, strlen(buf), 1, fp);
        log(LOG_ERR, "flag1\n");
    }

    free(buf);
    fclose(fp);
}


bool isUserExist(char* user)
{
    FILE* fp = fopen(pwdfname, "r");
    char namebuf[MAXUSERLEN], passwdbuf[MAXPASSWDLEN];
    char* buf = NULL;
    size_t len = MAXLINE;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }

    /* dataline: usebit username getbit setbit replbit delbit tablename */
    while (getline(&buf, &len, fp) > 0) {
        if(sscanf(buf, "%s %s\n", namebuf, passwdbuf) != 2){
            continue;
        }

        if (strcmp(user, namebuf) == 0) {
            return true;
        }
    }

    free(buf);
    fclose(fp);

    return false;
}

void revoke_access(char* user, char* tablename, int minusgb, int minussb, int minusrb, int minusdb)
{
    if (strcmp(tablename, "default") == 0) return; /* default table is open */

    FILE* fp = fopen(accessfname, "r+");
    char userbuf[MAXUSERLEN], tablebuf[MAXDBNAMELEN];
    char* buf = NULL;
    size_t len = MAXLINE;
    int usebit = 0, getbit = 0, setbit = 0, replbit = 0, delbit = 0;

    if ( (buf = (char*) malloc(MAXLINE)) == NULL ) {
        perror("verify: malloc error.");
        exit(1);
    }

    int flag = 0;
    int pos = ftell(fp);
    /* dataline: usebit username get_bit set_bit repl_bit del_bit tablename */
    while (getline(&buf, &len, fp) > 0) {
        int n;

        if((n = sscanf(buf, "%d %s %d %d %d %d %s\n",
                    &usebit, userbuf, &getbit, &setbit, &replbit, &delbit, tablebuf)) != 7){
            log(LOG_ERR, "revoke_access: n is not right\n");
            exit(1);
        }

        if ( (strcmp(user, userbuf) == 0) &&
             (strcmp(tablename, tablebuf) == 0) ) {
            if (fseek(fp, pos, SEEK_SET) < 0) {
                log(LOG_ERR, "revoke_access: fseek failed.");
                exit(1);
            }
            if (usebit == 1) {
                getbit &= ~minusgb; setbit &= ~minussb; replbit &= ~minusrb; delbit &= ~minusdb;
                sprintf(buf, "%d %s %d %d %d %d %s\n",
                        usebit, user, getbit, setbit, replbit, delbit, tablebuf);
                fwrite(buf, strlen(buf), 1, fp);
                log(LOG_ERR, "flag-1\n");
            }
            flag = 1;
            break;
        }
        pos = ftell(fp);
    }

    free(buf);
    fclose(fp);
}
