# coding: utf-8
from sqlalchemy import Column, Integer, String, Text, ForeignKey
from sqlalchemy.schema import FetchedValue
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship


def to_dict(self, only: tuple = ()):
    if only:
        return {c.name: getattr(self, c.name, None) for c in self.__table__.columns if c.name in only}
    else:
        return {c.name: getattr(self, c.name, None) for c in self.__table__.columns}


Base = declarative_base()
Base.to_dict = to_dict
# metadata = Base.metadata


# 用户表orm模型
class User(Base):
    __tablename__ = 'user'

    id = Column(Integer, primary_key=True)
    email = Column(String(64), nullable=False, unique=True)
    nick = Column(String(64), unique=True, nullable=False)
    pwd = Column(String(128), nullable=False)
    utype = Column(Integer, nullable=False, default=0)
    head = Column(String(80))


class Article(Base):
    __tablename__ = 'article'
    id = Column(Integer, primary_key=True)
    title = Column(String(80), nullable=False)  # 标题
    t_img = Column(String(80))  # 标题图片
    b_img = Column(String(80))  # 文章图片
    template = Column(String(1), nullable=False)  # 模板
    source = Column(String(80), nullable=False)  # 来源
    b_desc = Column(String(80), nullable=False)  # 描述
    b_time = Column(String(32), nullable=False)  # 发表时间
    body = Column(Text(), nullable=False)  # 内容
    b_read = Column(Integer, default=0)  # 阅读人数
    b_like = Column(Integer, default=0)  # 点赞人数
    is_del = Column(Integer, default=0)  # 是否删除
    b_uid = Column(Integer, ForeignKey('user.id'))
    user = relationship("User", backref='u2a')


# 评论表ORM模型
class Comment(Base):
    __tablename__ = 'comments'

    id = Column(Integer, primary_key=True)
    c_nick = Column(String(64), nullable=False)
    c_time = Column(String(32), nullable=False)  # 发表时间
    c_retime = Column(String(32), nullable=False)  # 回复时间
    c_body = Column(String(255), nullable=False)  # 评论内容
    c_rebody = Column(String(255), nullable=False)  # 回复内容
    is_del = Column(Integer, default=0)  # 是否删除
    c_aid = Column(Integer, ForeignKey('article.id'))
    article = relationship("Article", backref='a2c')

