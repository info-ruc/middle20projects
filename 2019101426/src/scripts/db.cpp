
#include "apue.h"

#undef MAXLINE
#undef min
#undef max

#include "db.h"


/*
 * Open or create a databse. Same argument as open.
 */
DBHANDLE
SimpleDB::db_open(const char* pathname, int oflag, ...) {
    DB       *db;
    int      len, mode;
    int      i;
    char     asciiptr[PTR_SZ + 1], hashs[(NHASH_DEF + 1) * PTR_SZ + 2]; /* +2 FOR newline and null */
    struct stat statbuff;
    char     *dbname;

    /*
     * Allocate a DB structure, and the buffers it needs
     */
    if ((dbname = strrchr((char*)pathname, '/')) == NULL)
        dbname = (char*)pathname;
    else
        dbname++;

    len = strlen(dbname);
    if ((db = _db_alloc(len)) == nullptr) {
        err_dump("db_open: _db_alloc error for DB");
    }
    db->nhash   = NHASH_DEF;  /* hash table size */
    db->hashoff = HASH_OFF;   /* offset in index file of hash table */

    strcpy(db->name, dbname);
    strcat(db->name, ".idx");

    if (oflag & O_CREAT) {
        va_list ap;

        va_start(ap, oflag);
        mode = va_arg(ap, int);
        va_end(ap);

        /*
        * Open index file and data file
        */
        db->idxfd = open(db->name, oflag, mode);
        strcpy(db->name + len, ".dat");
        db->datfd = open(db->name, oflag, mode);
    } else {
        /*
         * Open index file and data file
         */
        db->idxfd = open(db->name, oflag);
        strcpy(db->name + len, ".dat");
        db->datfd = open(db->name, oflag);
    }

    if (db->idxfd < 0 || db->datfd < 0) {
        _db_free(db);
        return NULL;
    }

    if ((oflag & (O_CREAT)) == (O_CREAT)) {
        if (fstat(db->idxfd, &statbuff) < 0) {
            err_sys("db_open: fstat error");
        }

        if (statbuff.st_size == 0) {
            /*
             * We have to build a list of NHASH_DEF+1 chain
             * ptrs with value of 0. The +1 is for the free
             * list pointer that precedes the hash table.
             */
            sprintf(asciiptr, "%*d", PTR_SZ, 0);
            hashs[0] = 0;
            for (i = 0; i < NHASH_DEF + 1; i++) {
                strcat(hashs, asciiptr);
            }
            strcat(hashs, "\n");
            i = strlen(hashs);
            if (write(db->idxfd, hashs, i) != i) {
                err_dump("db_open: index file init write error");
            }
        }
    }

    db_rewind(db);

    return (db);
}

/*
 * Allocate & initialize a DB structure and its structure
 *
 */
static DB*
_db_alloc(int namelen) {
    DB* db;

    /*
     * Use calloc, to initialize the structure to zero
     */
    if ((db = (DB*)calloc(1, sizeof(DB))) == nullptr) {
        err_dump("_db_alloc: calloc error for DB");
    }
    db->idxfd = db->datfd = -1;

    /*
     * Allocate room for the name
     */
    if ((db->name = (char*)malloc(namelen + 5)) == nullptr) {
        err_dump("_db_alloc: malloc error for name");
    }

    if ((db->idxbuf = (char*)malloc(IDXLEN_MAX + 2)) == nullptr) {
        err_dump("_db_alloc: malloc error for index buffer");
    }

    /*
     * Allocate an index buffer and a data buffer.
     * +2 for newline and null at end
     */
    if ((db->idxbuf = (char*)malloc(IDXLEN_MAX + 2)) == nullptr) {
        err_dump("_db_alloc: malloc error for index buffer");
    }
    if ((db->datbuf = (char*)malloc(DATLEN_MAX + 2)) == nullptr) {
        err_dump("_db_alloc: malloc error for data buffer");
    }

    return db;
}

/*
 * Relinquish access to the database
 */
void
SimpleDB::db_close(DBHANDLE h)
{
    _db_free((DB*)h);  /* close fds, free buffers & struct */
}

/*
 * Free up a DB structure, and all the malloc'ed buffers it
 * may point to. Also close the file descriptors if still open.
 */
static void
_db_free(DB* db) {
    if (db->idxfd >= 0) {
        close(db->idxfd);
    }
    if (db->datfd >= 0) {
        close(db->datfd);
    }

    if (db->idxbuf != nullptr) {
        free(db->idxbuf);
    }
    if (db->datbuf != nullptr) {
        free(db->datbuf);
    }

    free(db);
}

/*
 * Fetch a record. Return a pointer to the null-terminated data.
 */
char *
SimpleDB::db_fetch(DBHANDLE h, const char* key)
{
    DB    *db = (DB*)h;
    char  *ptr;

    if (_db_find(db, key) < 0) {
        ptr = nullptr;
        db->cnt_fetcherr++;
    } else {
        ptr = _db_readdat(db);
        db->cnt_fetchok++;
    }

    return ptr;
}

/*
 * Find the specified record. Called by db_delete, db_fetch,
 * and db_store.
 */
 static int
 _db_find(DB* db, const char* key) {
    off_t offset, nextoffset;

    db->chainoff = (_db_hash(db, key) * PTR_SZ) + db->hashoff;
    db->ptroff = db->chainoff;

    /*
     * Get the offset in the index file of first record
     * on the hash chain (can be 0)
     */
    offset = _db_readptr(db, db->ptroff);
    while (offset != 0) {
        nextoffset = _db_readidx(db, offset);
        if (strcmp(db->idxbuf, key) == 0)
            break;
        db->ptroff = offset;
        offset = nextoffset;
    }
    /*
     * offset == 0 on error (record not found)
     */
    return (offset == 0 ? -1 : 0);
}

/*
 * Calculate the hash value for a key
 */
static DBHASH
_db_hash(DB* db, const char *key) {
    DBHASH     hval = 0;
    char       c;
    int        i;

    for (i = 1; i <= strlen(key); ++i) {
        c = *(key + i - 1);
        hval += c * i;
        key++;
    }

    return (hval % db->nhash);
}

/*
 * Read a chain ptr field from anywhere in the index file:
 * the free list pointer, a hash table chain ptr, or an
 * index record chain ptr.
 */
 static off_t
 _db_readptr(DB* db, off_t offset) {
    char asciiptr[PTR_SZ + 1];

    if (lseek(db->idxfd, offset, SEEK_SET) == -1) {
        err_dump("_db_readptr: lseek error to ptr field");
    }
    if (read(db->idxfd, asciiptr, PTR_SZ) != PTR_SZ) {
        err_dump("_db_readptr: read error of ptr field");
    }
    asciiptr[PTR_SZ] = 0;
    return (atol(asciiptr));
}

/*
 * Read the next index record. We start at the specified offset
 * in the index file. We read the index record into db->idxbuf
 * and replace the separators to null bytes. If all is ok we
 * set db->datoff and db->datlen to the offset and length of the
 * corresponding data record in the data file.
 */
static off_t
_db_readidx(DB* db, off_t offset) {
    size_t            i;
    char              *ptr1, *ptr2;
    char              asciiptr[PTR_SZ + 1], asciilen[IDXLEN_SZ + 1];
    struct iovec      iov[2];

    /*
     * Position index file and record the offset.
     * db_nextrec calls us with offset==0, meaning read from current offset.
     */
    if ((db->idxoff = lseek(db->idxfd, offset, offset == 0 ? SEEK_CUR : SEEK_SET)) == -1)
        err_dump("_db_readidx: lseek error");

    /*
     * Read the ascii chain ptr and the ascii length at the front of the index record.
     */
    iov[0].iov_base = asciiptr;
    iov[0].iov_len  = PTR_SZ;
    iov[1].iov_base = asciilen;
    iov[1].iov_len  = IDXLEN_SZ;

    if ((i = readv(db->idxfd, &iov[0], 2)) != PTR_SZ + IDXLEN_SZ) {
        if (i == 0 && offset == 0) {
            return -1;
        }
        err_dump("_db_readidx: readv error of index record.");
    }

    /*
     * This is our return value; always >= 0
     */
    asciiptr[PTR_SZ] = 0;
    db->ptrval = atol(asciiptr);

    asciilen[IDXLEN_SZ] = 0;
    if ((db->idxlen = atoi(asciilen)) < IDXLEN_MIN || db->idxlen > IDXLEN_MAX) {
        err_dump("_db_readidx: invalid length");
    }

    /*
     * Now read the actual index record. We read it into the key buffer
     * that we malloced when we opened the databse.
     */
    if ((i = read(db->idxfd, db->idxbuf, db->idxlen)) != db->idxlen) {
        err_dump("_db_readidx: read error of index record");
    }
    if (db->idxbuf[db->idxlen-1] != NEWLINE) {
        err_dump("_db_readidx: missing newline");
    }

    db->idxbuf[db->idxlen-1] = 0;

    /*
     * Find the separators in the index record
     */
    if ((ptr1 = strchr(db->idxbuf, SEP)) == NULL) {
        err_dump("_db_readidx: missing first separator");
    }
    *ptr1++ = 0;

    if ((ptr2 = strchr(ptr1, SEP)) == NULL) {
        err_dump("_db_readidx: missing second separator");
    }
    *ptr2++ = 0;

    if (strchr(ptr2, SEP) != NULL) {
        err_dump("_db_readidx: too many separators");
    }

    /*
     * Get the starting offset and length of the data record
     */
    if ((db->datoff = atol(ptr1)) < 0) {
        err_dump("_db_readidx: starting offset < 0");
    }
    if ((db->datlen = atol(ptr2)) <= 0 || db->datlen > DATLEN_MAX) {
        err_dump("_db_readidx: invalid length");
    }

    return db->ptrval;
}

/*
 * Read the current data record into the data buffer
 * Return a pointer to the null-terminated data buffer
 */
static char*
_db_readdat(DB* db) {
    if (lseek(db->datfd, db->datoff, SEEK_SET) == -1)
        err_dump("_db_readdat: lseek error");
    if (read(db->datfd, db->datbuf, db->datlen) != db->datlen)
        err_dump("_db_readdat: read error");
    if (db->datbuf[db->datlen-1] != NEWLINE) {
        err_dump("_db_readdat: missing newline");
    }

    db->datbuf[db->datlen-1] = 0;

    return db->datbuf;
}

/*
 * Delete the specified record
 */
int
SimpleDB::db_delete(DBHANDLE h, const char* key) {
    DB      *db = (DB*)h;
    int      rc = 0;

    if (_db_find(db, key) == 0) {
        _db_dodelete(db);
        db->cnt_delok++;
    } else {
        rc = -1;
        db->cnt_delerr++;
    }

    return rc;
}

/*
 * Delete the current record specified by the DB structure
 * This function is called by db_delete and db_store, after
 * the record has been located by _db_find
 */
static void
_db_dodelete(DB *db)
{
    int     i;
    char    *ptr;
    off_t   freeptr, saveptr;

    /*
     * Set data buffer and key to all blanks
     */
    for (ptr = db->datbuf, i = 0; i < db->datlen - 1; ++i) {
        *ptr++ = SPACE;
    }
    *ptr = 0;
    ptr = db->idxbuf;
    while (*ptr) {
        *ptr++ = SPACE;
    }

    /*
     * Write the data record with all blanks
     */
    _db_writedat(db, db->datbuf, db->datoff, SEEK_SET);

    /*
     * Read the free list pointer. Its value becomes the
     * chain ptr field of the deleted index record
     */
    freeptr = _db_readptr(db, FREE_OFF);

    /*
     * Save the contents of index record chain ptr,
     * before it's rewritten by _db_writeidx
     */
    saveptr = db->ptrval;

    /*
     * Rewrite the index record.
     */
    _db_writeidx(db, db->idxbuf, db->idxoff, SEEK_SET, freeptr);

    /*
     * Write the new free list pointer
     */
    _db_writeptr(db, FREE_OFF, db->idxoff);

    /*
     * Rewrite the chain ptr that pointed to this record being deleted.
     */
    _db_writeptr(db, db->ptroff, saveptr);
}

/*
 * Write a data record, Called by _db_dodelete (to write the
 * record with blanks) and db_store
 */
 static void
 _db_writedat(DB* db, const char* data, off_t offset, int whence) {
    struct iovec    iov[2];
    static char     newline = NEWLINE;

    if ((db->datoff = lseek(db->datfd, offset, whence)) == -1)
        err_dump("_db_writedat: lseek error");
    db->datlen = strlen(data) + 1;

    iov[0].iov_base  = (char*) data;
    iov[0].iov_len   = db->datlen - 1;
    iov[1].iov_base  = &newline;
    iov[1].iov_len   = 1;
    if (writev(db->datfd, &iov[0], 2) != db->datlen)
        err_dump("_db_writedat: writev error of data record");
}


/*
 * Write an index record. _db_writedat is called before this function
 * to set the datoff and datlen fields in the DB structure.
 */
static void
_db_writeidx(DB* db, const char* key, off_t offset, int whence, off_t ptrval) {
    struct iovec    iov[2];
    char            asciiptrlen[PTR_SZ + IDXLEN_SZ + 1];
    int             len;

    if ((db->ptrval = ptrval) < 0 || ptrval > PTR_MAX)
        err_quit("_db_writeidx: invalid ptr: %d", ptrval);
    sprintf(db->idxbuf, "%s%c%lld%c%ld\n", key, SEP, (long long)db->datoff, SEP, (long)db->datlen);
    len = strlen(db->idxbuf);
    if (len < IDXLEN_MIN || len > IDXLEN_MAX)
        err_dump("_db_writeidx: invalid length");
    sprintf(asciiptrlen, "%*d%*d", PTR_SZ, (int)ptrval, IDXLEN_SZ, len);

    /*
     * Position the index file and record the offset
     */
    if ((db->idxoff = lseek(db->idxfd, offset, whence)) == -1) {
        err_dump("_db_writeidx: lseek error");
    }

    iov[0].iov_base = asciiptrlen;
    iov[0].iov_len  = PTR_SZ + IDXLEN_SZ;
    iov[1].iov_base = db->idxbuf;
    iov[1].iov_len  = len;
    if (writev(db->idxfd, &iov[0], 2) != PTR_SZ + IDXLEN_SZ + len) {
        err_dump("_db_writeidx: writev error of index record");
    }
}

/*
 * Write a chain ptr field somewhere in the index file:
 * the free list, the hash table, or in an index record.
 */
static void
_db_writeptr(DB* db, off_t offset, off_t ptrval)
{
    char asciiptr[PTR_SZ + 1];

    if (ptrval < 0 || ptrval > PTR_MAX) {
        err_quit("_db_writeptr: invalid ptr: %d", ptrval);
    }

    //snprintf(asciiptr, PTR_SZ, "%d", ptrval);
    sprintf(asciiptr, "%*lld", PTR_SZ, (long long)ptrval);
    if (lseek(db->idxfd, offset, SEEK_SET) == -1) {
        err_dump("_db_writeptr: lseek error to ptr failed.");
    }
    if (write(db->idxfd, asciiptr, PTR_SZ) != PTR_SZ) {
        err_dump("_db_writeptr: write error of ptr field");
    }
}

/*
 * Store a record in the database. Return 0 if OK, 1 if record
 * exist and DB_INSERT specified, -1 on error
 */
int
SimpleDB::db_store(DBHANDLE h, const char* key, const char* data, int flag = DB_STORE) {
    DB *db = (DB*)h;
    int rc, keylen, datlen;
    off_t ptrval;

    if (flag != DB_INSERT && flag != DB_REPLACE && flag != DB_STORE) {
        errno = EINVAL;
        return -1;
    }

    keylen = strlen(key);
    datlen = strlen(data) + 1;
    if (datlen < DATLEN_MIN || datlen > DATLEN_MAX) {
        err_dump("db_store: invalid data length");
    }

    /*
     * _db_find calculates which hash value this new record goes into
     * (db->chainoff)
     */
    if (_db_find(db, key) < 0) {
        if (flag == DB_REPLACE) {
            rc = -1;
            db->cnt_storerr++;
            errno = ENOENT;
            goto doreturn;
        }

        /*
         * read the chain
         * ptr to the first index record on hash chain.
         */
        ptrval = _db_readptr(db, db->chainoff);

        if (_db_findfree(db, keylen, datlen) < 0) {
            /*
             * cant find an empty record big enough. Append the new
             * record to the ends of the index and data files.
             */
            _db_writedat(db, data, 0, SEEK_END);
            _db_writeidx(db, key, 0, SEEK_END, ptrval);

            /*
             * db->idxoff was set by _db_writeidx. The new
             * record goes to the front of the hash chain.
             */
            _db_writeptr(db, db->chainoff, db->idxoff);
            db->cnt_stor1++;
        } else {
            /*
             * Reuse an empty record. _db_findfree removed it from
             * the free list and set both db->datoff and db->idxoff.
             * Reused record goes to the front of the hash chain.
             */
            _db_writedat(db, data, db->datoff, SEEK_SET);
            _db_writeidx(db, key, db->idxoff, SEEK_SET, ptrval);
            _db_writeptr(db, db->chainoff, db->idxoff);
            db->cnt_stor2++;
        }
    } else {                // record found
        if (flag == DB_INSERT) {
            rc = 1;
            db->cnt_storerr++;
            goto doreturn;
        }

        /*
         * We are replacing an existing record. We know the new key equals the existing
         * key, but we need to check if the data records are the same size;
         */
        if (datlen != db->datlen) {
            _db_dodelete(db);   /* delete the existing record */

            /*
             * Reread the chain ptr in the hash table
             */
            ptrval = _db_readptr(db, db->chainoff);

            /*
             * Append new index and data records to end of file
             */
            _db_writedat(db, data, 0, SEEK_END);
            _db_writeidx(db, key, 0, SEEK_END, ptrval);

            /*
             * New record goes to the front of the hash chain
             */
            _db_writeptr(db, db->chainoff, db->idxoff);
            db->cnt_stor3++;
        } else {
            /*
             * Same size data, just replace data record
             */
            _db_writedat(db, data, db->datoff, SEEK_SET);
            db->cnt_stor4++;
        }
    }
    rc = 0;

doreturn:
    return rc;
}

/*
 * Try to find a free index record and accompanying data record
 * of the correct sizes. We're only called by db_store.
 */
static int
_db_findfree(DB* db, int keylen, int datlen) {
    int         rc;
    off_t       offset, nextoffset, saveoffset;

    /*
     * Read the free list pointer
     */
    saveoffset = FREE_OFF;
    offset = _db_readptr(db, saveoffset);

    while (offset != 0) {
        nextoffset = _db_readidx(db, offset);
        if (strlen(db->idxbuf) == (size_t)keylen && db->datlen == (size_t)datlen) {
            break;
        }
        saveoffset = offset;
        offset = nextoffset;
    }

    if (offset == 0) {
        rc = -1;     /* no match found */
    } else {
        _db_writeptr(db, saveoffset, db->ptrval);
        rc = 0;
    }

    return rc;
}

/*
 * Rewind the index file for db_nextrec
 * Automatically called by db_open
 * Must be called before first db_nextrec
 */
void
SimpleDB::db_rewind(DBHANDLE h) {
    DB      *db = (DB*)h;
    off_t   offset;

    offset = (db->nhash + 1) * PTR_SZ;

    /*
     * We're just setting the file offset for this process
     * to the start of the index records;
     * +1 below for newline at end of hash table.
     */
    if ((db->idxoff = lseek(db->idxfd, offset+1, SEEK_SET)) == -1) {
        err_dump("db_dump: lseek error");
    }
}

/*
 * Return the next sequential record.
 * We just step our way through the index file, ignoring deleted
 * records. db_rewind must be called before this function is
 * called the first time.
 */
 char*
 db_nextrec(DBHANDLE h, char *key) {
    DB *db = (DB*)h;
    char c;
    char *ptr;

    do {
        /*
         * Read next sequential index record
         */
        if (_db_readidx(db, 0) < 0) {
            ptr = nullptr;
            goto doreturn;
        }

        /*
         * Check if key is all blank (empty record)
         */
        ptr = db->idxbuf;
        while ((c = *ptr++) != 0 && c == SPACE)
            ;
    } while (c == 0);

    if (key != nullptr) {
        strcpy(key, db->idxbuf);
    }
    ptr = _db_readdat(db);
    db->cnt_nextrec++;

doreturn:
    return ptr;
}



SimpleDB::SimpleDB(const char* pathname, int oflag, ...) {
    va_list valist;

    va_start(valist, oflag);
    int mode = va_arg(valist, int);
    handle = db_open(pathname, oflag, mode);
    assert(handle != nullptr);
}

SimpleDB::~SimpleDB() {
    db_close(handle);
}

int SimpleDB::store(const char* key, const char* data, int flag) {
    return db_store(handle, key, data, flag);
}

char* SimpleDB::fetch(const char* key) {
    return db_fetch(handle, key);
}

int SimpleDB::del(const char* key) {
    return db_delete(handle, key);
}

void SimpleDB::rewind(DBHANDLE db) {
    db_rewind(db);
}
















