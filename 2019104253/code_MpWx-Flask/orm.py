# coding:utf-8
import logging
import sqlite3


__conn = None


def log(sql, args=()):
    logging.info('SQL: %s' % sql)


def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d


def create_pool(database):
    logging.info('create database connection pool...')
    global __conn
    __conn = sqlite3.connect(database)
    __conn.row_factory = dict_factory


def select(sql, args, size=None):
    log(sql, args)
    global __conn
    conn = __conn
    cur = conn.cursor()
    cur.execute(sql, args or ())
    if size:
        rs = cur.fetchmany(size)
    else:
        rs = cur.fetchall()
    logging.info('rows returned: %s' % len(rs))
    cur.close()
    return rs


def execute(sql, args, autocommit=True):
    global __conn
    conn = __conn
    cur = conn.cursor()
    try:
        cur.execute(sql, args)
        affected = cur.rowcount
        if autocommit:
            conn.commit()
    except BaseException as e:
        if autocommit:
            conn.rollback()
        raise
    finally:
        cur.close()
    return affected
