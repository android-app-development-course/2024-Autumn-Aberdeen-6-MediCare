from db_initialize import init_database
from flask_server import app, DATABASE_PATH


if __name__ == "__main__":
    # 初始化数据库
    init_database(DATABASE_PATH)

    # 启动服务
    app.run(host='0.0.0.0', port=5000)