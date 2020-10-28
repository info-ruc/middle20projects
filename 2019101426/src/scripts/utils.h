#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/resource.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>
#include <string.h>

#ifndef __LOG_FLAG
#define __LOG_FLAG

#define LOG_DEBUG      0
#define LOG_INFO       1
#define LOG_WARN       2
#define LOG_ERR        3

#define MAXLOGLEN      1024
#define MAXTIMEBUF     64

#endif // __LOG_FLAG


#ifndef __FILE_PARAM
#define __FILE_PARAM

#define OFLAG (O_RDWR | O_CREAT)
#define MODE  (S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH)

#endif

extern int
writen(int fd, const char* buffer, size_t n);

extern int
readn(int fd, char* buffer, size_t n);

extern void
daemonize(const char* cmd);

extern void
log(int loglevel, const char* msg);


extern int
verify(char* name, char*passwd);

extern void
initializeAccessbits(char* cmd);

extern int
isAccessOK(char* user, char* tablename);
