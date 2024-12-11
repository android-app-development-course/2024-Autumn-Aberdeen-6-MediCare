import os
import logging
from flask import Flask, request
from database import SQLiteConnection
from message_builder import build_message
from function_decorator import json_required, token_required
from token_manager import new_token, validate_token, update_token_expire_time, invalidate_token, invalidate_all_token

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

"""
/register - 注册账号
请求 - POST {"username": "Username", "passwordHash": "password"}（哈希密码）
响应 - 成功：返回 200，失败：USERNAME_ALREADY_EXIST 用户名已注册
"""
@app.route("/register", methods=["POST"])
@json_required
def register():
    data = request.get_json()
    logger.info(f"Received /register request: {data}")

    username = data["username"]
    password_hash = data["passwordHash"]

    with SQLiteConnection() as (conn, cursor):
        # 检查用户名是否已存在
        query = "SELECT username FROM user WHERE username = ?"
        params = (username, )
        cursor.execute(query, params)

        if cursor.fetchone():
            logger.info(f"User \"{username}\" register fail, reason: USERNAME_ALREADY_EXIST.")
            return build_message(code=409, success=False, err_code="USERNAME_ALREADY_EXIST", err_description=f"The username \"{username}\" has already registered.")

        # 插入用户数据
        query = "INSERT INTO user (username, password_hash) VALUES (?, ?)"
        params = (username, password_hash)
        cursor.execute(query, params)
        conn.commit()

    logger.info(f"User \"{username}\" register success.")
    return build_message(message="Register success")


"""
/login - 登录账号
请求 - POST {"username": "Username", "passwordHash": "password"}（哈希密码）
响应 - 成功：返回 200 和登录 Token（字符串），失败：INVALID_USERNAME_OR_PASSWORD 无效的用户名或密码
"""
@app.route("/login", methods=["POST"])
@json_required
def login():
    data = request.get_json()
    logger.info(f"Received /login request: {data}")

    username = data["username"]
    password_hash = data["passwordHash"]

    # 验证密码
    with SQLiteConnection() as (conn, cursor):
        query = "SELECT id, password_hash FROM user WHERE username = ?"
        params = (username, )
        cursor.execute(query, params)
        result = cursor.fetchone()

    # 校验登陆结果
    if result:
        if password_hash == result[1]:
            user_id = result[0]
            token = new_token(user_id, username)
            data = {"token": token}
            logger.info(f"User \"{username}\" login success.")
            return build_message(message="Login success.", data=data)
    
    logger.info(f"User \"{username}\" login failed, reason: invalid username or password.")
    return build_message(code=401, success=False, err_code="INVALID_USERNAME_OR_PASSWORD", err_description="Invalid username or password.")

# ----------------------------------------------------------
# 以下请求需要在 Headers 中添加 Authorization: token 参数，
# 否则返回 401，Body 中错误信息为 TOKEN_REQUIRED
# ----------------------------------------------------------

"""
/logout - 登出账号
请求：null
响应 - 成功：返回 200，失败：INVALID_TOKEN 无效的 Token（客户端无论如何都应该登出）
"""
@app.route("/logout", methods=["POST"])
@token_required
def logout():
    token = request.headers.get("Authorization", None)
    logger.info(f"Received /logout request.")

    user_id, invalid_reason = validate_token(token)

    if user_id != -1:
        invalidate_token(token)
        logger.info(f"User logout success.")
        return build_message(message="Logout success.")
    else:
        logger.info(f"User logout failed, reason: token is invalid or already expired.")
        return build_message(code=410, success=False, err_code="INVALID_TOKEN", err_description="Token is invalid or already expired.")