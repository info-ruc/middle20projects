#encoding:utf-8
from flask import Flask,render_template,request,redirect,url_for,session
import flask
import config
from models import UserModel,Question,Answer
from exts import db
from functools import wraps
from sqlalchemy import or_


app=Flask(__name__)
app.config.from_object(config)
db.init_app(app)

def login_required(func):

    @wraps(func)
    def wrapper(*args,**kwargs):
        if session.get('user_id'):
            return func(*args,**kwargs)
        else:
            return redirect(url_for('login'))
    return wrapper


@app.route('/')
def index():
    context={
        'questions':Question.query.all()
    }
    print(context)
    return render_template('index.html',**context)

@app.route('/login/',methods=['GET','POST'])
def login():
    if flask.request.method == 'GET':
        return render_template('login.html')
    else:
        telephone=request.form.get('telephone')
        password=request.form.get('password')
        user=UserModel.query.filter(UserModel.telephone==telephone,UserModel.password==password).first()

        if user:
            session['user_id']=user.id
            print(user.username)
            session.permanent= True
            return redirect(url_for('index'))
        else:
            return u'手机号码或密码错误'
#     if flask.request.method == 'GET':
#         return flask.render_template('login.html')
#     else:
#         telephone = flask.request.form.get('telephone')
#         password = flask.request.form.get('password')
#         user = UserModel.query.filter_by(telephone=telephone).first()
#         if user and user.check_password(password):
#             flask.session['id'] = user.id
#             flask.g.user = user
#             return flask.redirect(flask.url_for('index'))
#         else:
#             return u'用户名或密码错误！'
# @app.route('/login/',methods=['GET','POST'])
# def login():
#     if flask.request.method == 'GET':
#         return flask.render_template('login.html')
#     else:
#         telephone = flask.request.form.get('telephone')
#         password = flask.request.form.get('password')
#         user = UserModel.query.filter(UserModel.telephone==telephone,UserModel.password==password).first()
#         if user: #user.check_password(password):
#             flask.session['id'] = user.id
#             flask.g.user = user
#             return flask.redirect(flask.url_for('index'))
#         else:
#             return u'用户名或密码错误！'


@app.route('/register/',methods=['GET','POST'])
def register():
    if flask.request.method == 'GET':
        return render_template('register.html')
    else:
        telephone= request.form.get('telephone')
        username = request.form.get('username')
        password1 = request.form.get('password1')
        password2 = request.form.get('password2')

        user=UserModel.query.filter(UserModel.telephone==telephone).first()
        if user:
            return u'手机号码已被注册'
        else:
            #验证密码
            if password1 !=password2:
                return u'确认密码错误'
            else:
                user=UserModel(telephone=telephone,username=username,password=password1)
                db.session.add(user)
                db.session.commit()
                return redirect(url_for('login'))

@app.route('/logout/',methods=['GET'])
def logout():
    flask.session.clear()
    return flask.redirect(flask.url_for('login'))

@app.route('/question/',methods=['GET','POST'])
@login_required
def question():
    if request.method=='GET':
        return render_template('question.html')
    else:
        title=request.form.get('title')
        content=request.form.get('content')
        user_id=session.get('user_id')
        question=Question(title=title,content=content)
        user=UserModel.query.filter(UserModel.id==user_id).first()
        question.author=user
        db.session.add(question)
        db.session.commit()
        return redirect(url_for('index'))


@app.route('/detail/<id>')
def detail(id):
    question_model=Question.query.filter(Question.id==id).first()

    return render_template('detail.html',question=question_model)

@app.route('/add_comment',methods=['POST'])
@login_required
def add_answer():
    content=request.form.get('answer_content')
    question_id=request.form.get('question_id')
    answer=Answer(content=content)
    user_id=session['user_id']
    user=UserModel.query.filter(UserModel.id==user_id).first()
    answer.author =user
    question=Question.query.filter(Question.id==question_id).first()
    answer.question=question
    db.session.add(answer)
    db.session.commit()
    return redirect(url_for('detail',id=question_id))
    # question_id = flask.request.form.get('question_id')
    # content = flask.request.form.get('content')
    # answer_model = Answer(content=content)
    # answer_model.author = flask.g.user
    # answer_model.question = Question.query.get(question_id)
    # db.session.add(answer_model)
    # db.session.commit()
    # return redirect(flask.url_for('detail', id=question_id))

@app.route('/search/')
def search():
    q=request.args.get('q')
    question=Question.query.filter(or_(Question.title.contains(q),Question.content.contains(q)))
    return render_template('index.html',questions=question)
@app.context_processor
def my_context_processor():
    user_id= session.get('user_id')
    print('user:',user_id)
    if user_id:
        user=UserModel.query.filter(UserModel.id==user_id).first()
        if user:
            return {'user':user}
    else:
        return {}

# @app.context_processor
# def context_processor():
#     if hasattr(flask.g,'user'):
#         return {"user":flask.g.user}
#     else:
#         return {}

if __name__=='__main__':
    app.run()