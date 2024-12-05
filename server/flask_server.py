import os
import logging
from flask import Flask, request
from database import SQLiteConnection
from message_builder import build_message

# 数据库路径
DATABASE_PATH = os.path.join(os.path.dirname(__file__), "data.db")

# logger
logging.basicConfig(level=logging.INFO,
                    format="[%(asctime)s] %(filename)s - %(levelname)s - %(message)s",
                    datefmt="%Y-%m-%d %H-%M-%S")
logger = logging.getLogger(__name__)

# 创建 Flask 应用
app = Flask(__name__)

# 定义路由
"""
/ping - Ping-pong
"""
@app.route("/ping", methods=["POST", "GET"])
def ping():
    logger.info("Received Ping.")
    return build_message(message="Pong")