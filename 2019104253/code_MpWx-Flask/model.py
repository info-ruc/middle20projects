# -*- coding:utf-8 -*-

from BaseModel import dBSession
from sqlalchemy import desc, asc
from BaseModel import BaseModel
from werkzeug.security import generate_password_hash, check_password_hash
from functools import wraps
from Models import User, Article, Comment


def classTransaction(func): #sql事务提交和回滚操作
    @wraps(func)
    def wrappar(self, *args, **kwargs):
        try:
            func(self, *args, **kwargs)
            dBSession.commit()
            return True
        except Exception as e:
            print(e)
            dBSession.rollback()
            return False
    return wrappar


class Users(User, BaseModel):

    # 校验密码
    @staticmethod #当某个方法不需要用到对象中的任何资源,将这个方法改为一个静态方法, 加一个@staticmethod
    def check_password(hash_password, password):
        return check_password_hash(hash_password, password)

    # 获取用户信息
    @staticmethod
    def get(uid):
        return dBSession.query(Users).filter_by(id=uid).first()

    # 增加用户
    @classTransaction
    def add(self, cls_, data):
        user = cls_(**data)
        return dBSession.add(user)


class Articles(Article, BaseModel):

    # 获取文章信息a
    @staticmethod
    def get(aid):
        return dBSession.query(Article).filter_by(id=aid).first()

    # 增加文章
    @classTransaction
    def add(self, cls_, data): #cls_是model模型的class对象，cls只是个形参
        art = cls_(**data)
        return dBSession.add(art)


class Comments(Comment, BaseModel):

    # 获取评论信息a
    @staticmethod
    def get(aid):
        return dBSession.query(Comment).filter_by(id=aid).first()

    # 增加评论
    @classTransaction
    def add(self, cls_, data):
        com = cls_(**data)
        return dBSession.add(com)

