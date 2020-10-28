
#include <cassert>
#include <cstdio>
#include <fcntl.h>
#include <stdarg.h>
#include <errno.h>
#include <sys/uio.h>
#include <iostream>
#include <unistd.h>
#include <sys/epoll.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <time.h>
#include <sys/types.h>
#include <string.h>
#include <signal.h>


#define LISTENQ 128
#define	SA	struct sockaddr
#define MAX_EVENTS 5

#ifndef __FILE_PARAM
#define __FILE_PARAM

#define OFLAG (O_RDWR | O_CREAT)
#define MODE  (S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH)

#endif

#define MAXLINE       128
#define MAXDBNAMELEN  128
#define MAXKEYLEN     128
#define MAXVALUELEN   128
#define MAXUSERLEN    64
#define MAXPASSWDLEN  64
#define MAXCMDLEN     10
#define IPLEN    64
#define	SA	struct sockaddr

#define UPDATE        1
#define DELETE        2
#define GET           3
#define SET           4

#define SPACE         ' '


#ifndef _APUE_DB_H
#define _APUE_DB_H

#define PATHLEN      256

/*
 * Internal index file constants
 * These are used to construct records in the
 * index file and data file
 */
#define IDXLEN_SZ  4    /*index record length (ASCII chars)*/
#define SEP        ':'  /*separator char in index record*/
#define SPACE      ' '  /*space character*/
#define NEWLINE    '\n' /*newline character*/

/*
 * the following definitions are for hash chains and free
 * list chain in the index file.
 */
#define PTR_SZ     7      /*size of ptr filed in hash chain*/
#define PTR_MAX    999999 /*max file offset = 10 ** PTR_SZ -1*/
#define NHASH_DEF  137    /*default hash table*/
#define FREE_OFF   0      /*free list offset in index file*/
#define HASH_OFF   PTR_SZ /*hash table offset in index file*/

typedef unsigned long DBHASH;  /* hash values */
typedef unsigned long COUNT;   /* unsigned counter*/

/*
 * Library's private representation of the database
 *
 */
typedef struct DB {
    int      idxfd;    /* fd for index file */
    int      datfd;    /* fd for data file */
    char     *idxbuf;  /* malloc'ed buffer for index record */
    char     *datbuf;  /* malloc'ed buffer for data record */
    char     *name;    /* name db was opened under */
    off_t    idxoff;   /* offset in index file of index record */
    size_t   idxlen;   /* length of index record */
    off_t    datoff;   /* offset in data file of data record */
    size_t   datlen;   /* length of data record */
    off_t    ptrval;   /* contents of chain ptr in index record */
    off_t    ptroff;   /* chain ptr offset pointing to this idx record */
    off_t    chainoff; /* offset of hash chain for this index record */
    off_t    hashoff;  /* offset in index file of hash table */
    DBHASH   nhash;    /* current hash table size */
    COUNT    cnt_delok;    /* delete ok */
    COUNT    cnt_delerr;   /* delete error */
    COUNT    cnt_fetchok;  /* fetch ok */
    COUNT    cnt_fetcherr; /* fetch error*/
    COUNT    cnt_nextrec;  /* nextrec */
    COUNT    cnt_stor1;    /* store: DB_INSERT, no empty, appended */
    COUNT    cnt_stor2;    /* store: DB_INSERT, found empty, reused */
    COUNT    cnt_stor3;    /* store: DB_REPLACE, diff len, appended */
    COUNT    cnt_stor4;    /* store: DB_REPLACE, same len, overwrote */
    COUNT    cnt_storerr;  /* store err */
} DB;

/*
 * Internal functions
 */
static DB    *_db_alloc(int);
static void   _db_dodelete(DB*);
static int    _db_find(DB*, const char*);
static int    _db_findfree(DB*, int, int);
static void    _db_free(DB*);
static DBHASH _db_hash(DB*, const char*);
static char  *_db_readdat(DB*);
static off_t  _db_readidx(DB*, off_t);
static off_t  _db_readptr(DB*, off_t);
static void   _db_writedat(DB*, const char*, off_t, int);
static void   _db_writeidx(DB*, const char*, off_t, int, off_t);
static void   _db_writeptr(DB*, off_t, off_t);




typedef	void *	DBHANDLE;

/*
 * Flags for db_store().
 */
#define DB_INSERT	   1	/* insert new record only */
#define DB_REPLACE	   2	/* replace existing record */
#define DB_STORE	   3	/* replace or insert */

/*
 * Implementation limits.
 */
#define IDXLEN_MIN	   6	/* key, sep, start, sep, length, \n */
#define IDXLEN_MAX	1024	/* arbitrary */
#define DATLEN_MIN	   2	/* data byte, newline */
#define DATLEN_MAX	1024	/* arbitrary */

#endif /* _APUE_DB_H */


typedef struct SendData {
    char  cmd[MAXCMDLEN];      /* repl; get; del; set; */
    char  dbname[MAXDBNAMELEN];
    char  key[MAXKEYLEN];
    char  value[MAXVALUELEN];
    char  value2[MAXVALUELEN];
    char  user[MAXUSERLEN];
    char  passwd[MAXPASSWDLEN];
    int   accessbits;
} SendData;


typedef struct RcvData {
    int   id;
    char  result[MAXLINE];
    int   accessbits;
} RcvData;


class SimpleDB {
public:
    SimpleDB(const char* pathname, int oflag, ...);

    ~SimpleDB();

    int store(const char* key, const char* data, int flag = DB_STORE);

    char* fetch(const char* key);

    int del(const char* key);

    void rewind(DBHANDLE db);

private:
    DBHANDLE handle;   /* db handle*/

    /*
     *  db_open is used to open the database
     *  @pathname: specify the name of pathname.idx, pathname.dat,
                    which used to store index and data
     *  @oflag: the parameter for open(), the open mode: readonly/readwrite...
     *  @...: we could specify another parameter: int mode
     *  return: the dbhandle which is used to operate the db
     */
    DBHANDLE db_open(const char* pathname, int oflag, ...);

    /*
     *  db_close is used to close the database
     *  @handle: specify the db handle to close
     */
    void db_close(DBHANDLE handle);

    /*
     *  @db: the db handle
     *  @key: the key
     *  @data: the record value
     *  @flag: specify the operation: DB_INSERT, DB_REPLACE, DB_STORE
     *  return -1 if error; 1 if DB_INSERT not insert because key is already exist
     */
    int db_store(DBHANDLE db, const char* key, const char* data, int flag);

    /*
     *  @db: the db handle
     *  @key: the record key in db
     *  return: if success, return the pointer to data; else return nullptr
     */
    char* db_fetch(DBHANDLE db, const char* key);

    /*
     *  @db: the db handle
     *  @key: the key in db to delete
     *  return if success, return 0; else return -1
     */
    int db_delete(DBHANDLE db, const char* key);

    /*
     *  db_rewind is used to roll to the first record in db
     *  @db: the db handle
     */
    void db_rewind(DBHANDLE db);
};



















