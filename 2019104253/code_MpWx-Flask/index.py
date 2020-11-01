# coding:utf-8
from gevent import monkey
monkey.patch_all()
from gevent.pywsgi import WSGIServer
import sys
import time
import os
import uuid
from flask import Flask, session, redirect, url_for, request, flash, render_template, jsonify, abort
from model import Users, User, Articles, Article,  Comments, Comment
from BaseModel import baseDir, dBSession, desc
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from werkzeug.security import generate_password_hash
import datetime
import copy
import random
from io import BytesIO
app = Flask(__name__)
app.config.update(
    DEBUG=True
)
app.secret_key = r'\xb9\xdbOXO\x8c\x1e\x0c-\x7f5\xcb\xebzMI;c\xe0\xa6;\x16\x8b\x9b'  # 添加APP安全码
# 模板颜色
templateColors = [['#f2dede', '#fcf8e3'], ['#fcf8e3', '#dff0d8'], ['#dff0d8', '#d9edf7'], ['#d9edf7', '#f2dede']]
# from Models import Base
# Base.metadata.create_all(engine)


# 自定义模板过滤器
def get_tmp_color(cv, sub):
    return templateColors[int(cv)-1][sub]


# 注册过滤器
app.add_template_filter(get_tmp_color, "get_tmp_color")


# 生成图片文件名
def next_id():
    return '%015d%s000' % (int(time.time() * 1000), uuid.uuid4().hex)


# model query转dict
def querys_to_dict(d, only: tuple = ()): #model对象转字典
    datas = []
    for self in d:
        data = {}
        if only:
            data.update({c.name: getattr(self, c.name, None) for c in self.__table__.columns if c.name in only})
        else:
            data.update({c.name: getattr(self, c.name, None) for c in self.__table__.columns})
        if self.u2a and self.u2a[-1].is_del == 0:
            data['title'] = self.u2a[-1].title
            data['b_time'] = self.u2a[-1].b_time
        datas.append(data)
    return datas


@app.route("/")
@app.route("/index")
@app.route("/mplist")
def mplist():  # 首页 也是公众号列表页
    mps = dBSession.query(User).filter_by(utype=1)
    mps = querys_to_dict(mps)
    return render_template("mplist.html", mps=mps)


@app.route("/articlelist/<int:uid>")
def articlelist(uid):  # 文章列表
    userinfo = dBSession.query(User).filter_by(id=uid).first() #filter_by，过滤器，过滤掉不符合条件的元素
    #上一句：orm查询语句，查询用户表 用户id=uid的数据  只取一条结果，换成sql语句是  select * form user where id=uid limit 1
    #表内部精确查询，filter全局查询
    #.first()：返回queryset中匹配到的第一个对象，如果没有匹配到对象则为None，如果queryset没有定义排序，则按主键自动排序。
    if userinfo:
        articles = dBSession.query(Article).filter_by(b_uid=uid).filter_by(is_del=0).order_by(desc("id"))
        return render_template("articlelist.html", articles=articles, userinfo=userinfo)
        #render_template的功能是对先引入html，同时根据后面传入的参数，对html进行修改渲染。
    else:
        abort(404) #abort() : 立即停止视图函数的执行,并且把相对应的信息返回到前端中


@app.route("/articledetail/<int:bid>")
def articledetail(bid):  # 文章详情
    articleinfo = dBSession.query(Article).filter_by(id=bid).filter_by(is_del=0).first()
    if articleinfo:
        articleinfo.b_read = articleinfo.b_read + 1 #阅读人数+1
        dBSession.commit() #提交数据
        comment_count = len(articleinfo.a2c) #这个变量来自Model里面的关系，是sqlalchemy的关系，关联两张表的
        return render_template("articledetail.html", a=articleinfo, c=comment_count)
    else:
        abort(404)


# 获取数学验证码
@app.route("/getCode")
def get_code():  #
    vacode, png = create_code()
    session['code'] = vacode #session类似于会话
    return png

#1
@app.route("/like", methods=["post", ])
#使用场景：如果只对服务器获取数据，并没有对服务器产生任何影响，那么这时候使用get请求
#使用场景：如果要对服务器产生影响，那么使用post请求
def atriclelike():  # 文章点赞
    datas = {"code": "0", "msg": "文章没找到"}
    aid = request.form.get('id') #获取文章信息
    if aid.isnumeric(): # isnumeric() 方法检测字符串是否只由数字组成。这种方法是只针对unicode对象。
        likequery = Articles().get(aid) #相当于文章信息给了他
        if likequery:
            likequery.b_like += 1 #点赞的次数，b_like在Model.py中有定义，相当于文章信息中的点赞数+1
            dBSession.commit()
            datas['code'] = "1" #修改datas
            datas['msg'] = ""
    return jsonify(datas) #jsonify是flask自带的对字典转化为json对象的方法


#2
@app.route("/delarticle", methods=["post", ])
def delarticle():  # 文章删除
    datas = {"code": "0", "msg": "文章没找到"}
    aid = request.form.get('id') #获取文章id，跟前端交互
    if aid.isnumeric():
        likequery = Articles().get(aid)
        if likequery:
            if likequery.b_uid == session['id']: # session['id']是加密的cookie，这个是存的用户的id，session只保存用户相关的
                #b_uid在Model中有定义，上一句是先核实一下是不是作者本人
                likequery.is_del = 1  #逻辑删除，并不是物理删除，只是查询的时候 带个这个字段  is_del=0 就查不到这条数据了
                dBSession.commit()
                datas['code'] = "1"
                datas['msg'] = ""
            else:#不是作者本人
                datas['msg'] = "非法访问, 您不是该文章的作者,无法删除!"
    return jsonify(datas)

#3
@app.route("/comment", methods=["post", ])
def atriclecomment():  # 文章评论
    datas = {"code": "0", "msg": "文章没找到"}
    aid = request.form.get('id')  #取前端表单的值，form表单（html form），跟前端交互的意思
    code = request.form.get("code") #取我提交的值（验证码）
    if aid.isnumeric():
        if code == str(session.get("code")): #核实验证码对不对
            if session.get("logged_in"): #核实是否已经登录
                commentquery = Articles.get(aid) #获取文章信息
                if commentquery:
                    res = Comments().add(Comment, {"c_aid": aid, "c_nick": session["nick"],
                                                   "c_time": str(datetime.datetime.today())[:16],
                                                   "c_body": request.form.get("body")}) #评论体
                    if res:
                        datas['code'] = "1"
                        datas['msg'] = ""
                    else:
                        datas['msg'] = "评论失败!,原因未知!"
            else:
                datas['msg'] = "登录后才可评论!请先登录!"
        else:
            datas['msg'] = "验证码错误!"
    return jsonify(datas)

#4
@app.route("/recomment", methods=["post", ])
def recomment():  # 回复评论
    datas = {"code": "0", "msg": "评论没找到"}
    if session.get('logged_in'): #核实是否登录
        cid = request.form.get('id') #获取文章id
        body = request.form.get("body") #获取"用户提交"的评论体
        if cid.isnumeric():
            cominfo = Comments.get(cid) #获取评论信息
            if cominfo:
                if cominfo.article: #评论对应的文章也存在
                    if cominfo.article.b_uid == session['id']: #核实是不是作者的id
                        cominfo.c_rebody = body  #c_rebody：评论内容
                        cominfo.c_retime = str(datetime.datetime.today())[:16]  #c_retime：回复时间
                        dBSession.commit()
                        datas['code'] = "1"
                        datas['msg'] = ""
                    else:
                        datas['msg'] = "您不是该文章的作者,没有权限回复"
                else:
                    datas['msg'] = "该条评论所属的文章或者已删除!"
    else:
        datas['msg'] = "请先登录!"
    return jsonify(datas)

#5
@app.route("/articlemanage", methods=['get', 'post'])
def articlemanage():  #管理文章
    if session.get('utype') == 1:  # 检查是不是公众号作者
        if request.method == 'POST':
            title = request.form.get('article-title').strip()  # 标题
            t_img = request.files.get('article-title-img')  # 标题图片
            b_img = request.files.get('article-body-img')  # 文章图片
            template = request.form.get('article-template')  # 模板
            source = request.form.get('article-source').strip()  # 来源
            b_desc = request.form.get('article-body-des').strip()  # 描述
            b_time = str(datetime.datetime.today())[:16]  # 发表时间
            body = request.form.get('article-body').strip()  # 内容
            b_uid = session['id']  # 关联用户id
            form_data = {"title": title, 't_img': t_img, 'b_img': b_img, 'template': template, 'source': source,
                         "b_desc": b_desc, "b_time": b_time, "body": body, "b_uid": b_uid}
            add_article(form_data)  # 发表文章
            return redirect(url_for('articlemanage'))
        else:
            datas = Articles().getAll(Article, {"b_uid={}".format(session['id']), "is_del=0"}, field=('id', 'title', 'template'))
            return render_template("articlemanage.html", datas=datas)
    else:
        return redirect(url_for('mplist'))

#6
# 登录和注册视图
@app.route("/login/<act>", methods=["post", "get"])
def login(act):  #
    if session.get('logged_in'):
        if session.get('utype') == 1:
            return redirect(url_for('articlemanage'))
        return redirect(url_for('mplist'))
    else:
        if request.method == 'POST':
            if act == "login":  # 登录验证
                email = request.form.get('log_email').strip()
                pwd = request.form.get('log_pwd').strip()
                form_data = {"email": email,  'pwd': pwd}
                res = check_login(form_data)
                data = res.pop('data')
                if data:
                    session["logged_in"] = True
                    session['id'] = data['id']
                    session['nick'] = data['nick']
                    session['utype'] = res['utype'] = data['utype']
                return jsonify(res)

            # 注册 验证
            elif act == "register":
                email = request.form.get('reg_email').strip()
                nick = request.form.get('reg_nick').strip() #昵称
                pwd = request.form.get('reg_pwd')
                utype = request.form.get('reg_e').strip() #用户类型
                f_head = request.files.get('reg_head')
                pwd = generate_password_hash(pwd)  # 密码加密
                form_data = {"email": email, 'nick': nick, 'pwd': pwd, 'utype': utype, 'f_head': f_head}
                return jsonify(add_user(form_data))
        return render_template("login.html")

#7
# 检验用户登录
def check_login(form_data):
    datas = {"code": "0", "msg": "", 'data': {}}
    res = Users().getOne(User, {"{}='{}'".format("email", form_data['email'])})
    #getOne()在BaseModel.py第100行左右定义的，找到对应的用户
    if res: #是不是能找到一个用户
        if Users.check_password(res['pwd'], form_data['pwd']): #核实密码对不对，check_password在model中定义的
            datas['data'] = res
            datas['code'] = "1"
        else:
            datas["msg"] = "密码错误!"
    else:
        datas["msg"] = "用户不存在!"
    return datas

#8
# 发表文章
def add_article(form_data):
    # {"title", 't_img', 'b_img', 'template', 'source', "b_desc", "b_time", "body", "b_uid"}
    if not form_data['b_desc']:  # 没有描述 取文章前60个字符
        #b_desc：文章描述
        form_data['b_desc'] = form_data['body'][:60] #文章内容
    if form_data['t_img']:  # 检查"标题"图片
        if len(form_data['t_img'].read()) <= 1048576: #图片大小必须小于特定值
            img = Image.open(form_data['t_img'])
            if img.format in ("JPEG", "JPG", "PNG") and img.size[0] < 200 and img.size[1] < 200: #文件大小类型进行限制
                f_name = next_id() #生成图片文件名
                t_img = f_name + "." + img.format #文件名赋值
                img.save(baseDir + "/static/img/" + t_img)
                form_data["t_img"] = t_img
    if form_data['b_img']:  # 检查"文章"图片
        if len(form_data['b_img'].read()) <= 5242880:
            img = Image.open(form_data['b_img'])
            if img.format in ("JPEG", "JPG", "PNG") and img.size[0] < 2000 and img.size[1] < 2000:
                f_name = next_id()
                b_img = f_name + "." + img.format
                img.save(baseDir+"/static/img/"+b_img)
                form_data["b_img"] = b_img
    res = Articles().add(Article, form_data)
    return res

#9
# 注册 添加用户
def add_user(form_data):
    datas = {"code": "0", "msg": ""}
    if form_data['utype'] == "0":  # 这是普通用户注册
        form_data.pop("utype", None) #pop() 方法删除字典给定键 key 及对应的值，返回值为被删除的值。
        form_data.pop("f_head", None) #f_head：头像
        res = Users().add(User, form_data) #增加用户
        if res:
            datas["code"] = "1"
        else:
            datas["msg"] = "注册失败, 昵称或者邮箱已被注册!"
    else:
        form_data["utype"] = 1  # 这是公众号注册
        if form_data['f_head']:
            f_len = len(form_data['f_head'].read())
            if f_len <= 1048576: #头像大小小于1M
                img = Image.open(form_data['f_head'])
                if img.format not in ("JPEG", "JPG", "PNG"):
                    datas['msg'] = "上传的头像图片文件类型不正确!"
                elif img.size[0] > 100 or img.size[1] > 100:
                    datas['msg'] = "上传的头像图片像素超过100*100px!"
                else:
                    f_name = next_id() #生成图片文件名
                    head = f_name + "." + img.format
                    img.save(baseDir + "/static/head/" + head)
                    form_data.pop("f_head", None)
                    form_data["head"] = head #更名
                    res = Users().add(User, form_data)
                    if res:
                        datas["code"] = "1"
                    else:
                        datas["msg"] = "注册失败, 昵称或者邮箱已被注册!"
            else:
                datas['msg'] = "上传的头像图片文件大小超过1MB!"
        else:
            form_data.pop("f_head", None)
            form_data["head"] = ""
            res = Users().add(User, form_data)
            if res:
                datas["code"] = "1"
            else:
                datas["msg"] = "注册失败, 昵称或者邮箱已被注册!"
    return datas


# 注销 退出登录
@app.route("/logout")
def logout():
    session.pop('logged_in', None)
    session.pop('id', None)
    session.pop('nick', None)
    session.pop('utype', None)
    # flash('logout success！')
    return redirect(url_for('mplist'))

#10
# 生成图片验证码
def create_code():
    ys = ('+', '-', '*')
    code = ''
    ints = [i for i in range(10)]
    code = code + str(random.choice(ints)) + random.choice(ys) + str(random.choice(ints))

    # 随机颜色1:
    def rndColor():
        return random.randint(64, 255), random.randint(64, 255), random.randint(64, 255) #随机整数

    # 随机颜色2:
    def rndColor2():
        return random.randint(32, 127), random.randint(32, 127), random.randint(32, 127)

    # 数学运算
    def ysgs(v1, ysf, v2):
        if ysf == '+':
            return int(v1) + int(v2)
        if ysf == '-':
            return int(v1) - int(v2)
        if ysf == '*':
            return int(v1) * int(v2)

    # 240 x 60:
    width = 30 * 5
    height = 24
    image = Image.new('RGB', (width, height), (255, 255, 255))  #最后一个参数是颜色：白色
    # 创建Font对象:（设置一个字体）
    font = ImageFont.truetype(r'C:\Windows\Fonts\Arial.ttf', 24)
    # 创建Draw对象:
    draw = ImageDraw.Draw(image) #新建画布绘画对象

    # 填充每个像素:
    for x in range(width):
        for y in range(height):
            draw.point((x, y), fill=rndColor())  #画点
    # 输出文字:
    code1 = code + '=?'
    for t in range(5):
        draw.text((24 * t + 5, 0), code1[t], font=font, fill=(0, 0, 0))  # fill=rndColor2()
        #文字的绘制，第一个参数指定绘制的起始点（文本的左上角所在位置），第二个参数指定文本内容，
        #第三个参数指定文本的颜色，第四个参数指定字体（通过ImageFont类来定义）
        #fill=(0, 0, 0)：字体是黑色
    va = ysgs(code[0], code[1], code[2])
    # 模糊:
    # image = image.filter(ImageFilter.BLUR)
    fake_file = BytesIO() #在内存中读写bytes
    image.save(fake_file, "png")
    return va, fake_file.getvalue()


if __name__ == '__main__':
    http_server = WSGIServer(('0.0.0.0', 5000), app)
    http_server.log = sys.stdout
    http_server.error_log = sys.stderr
    http_server.serve_forever()
