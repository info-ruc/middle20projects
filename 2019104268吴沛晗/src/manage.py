#encoding: utf-8

from flask_script import Manager
from flask_migrate import Migrate,MigrateCommand
from bbs import app
from exts import db
import config
from models import UserModel,Question

app.config.from_object(config)
db.init_app(app)

manager=Manager(app)
#绑定apph和db
migrate=Migrate(app,db)
manager.add_command('db',MigrateCommand)

if __name__=='__main__':
    manager.run()