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
                    datefmt="%Y-%m-%d %H:%M:%S")
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
        else:
            fail_reason = "password is incorrect."
    else:
        fail_reason = "username not exist."
    
    logger.info(f"User \"{username}\" login failed, reason: {fail_reason}")
    return build_message(code=401,
                         success=False,
                         err_code="INVALID_USERNAME_OR_PASSWORD",   # 为保安全，服务端不返回具体失败原因
                         err_description="Invalid username or password.")

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
def logout():
    token = request.headers.get("Authorization", None)
    logger.info(f"Received /logout request.")

    if not token:
        return build_message(code=401, success=False, err_code="TOKEN_REQUIRED", err_description="Authorization token must included in the request.")

    _, invalid_reason = validate_token(token)

    if invalid_reason:
        logger.info(f"User logout failed, reason: token is invalid or already expired.")
        return build_message(code=410, success=False, err_code="INVALID_TOKEN", err_description="Token is invalid or expired.")
    else:
        invalidate_token(token)
        logger.info(f"User logout success.")
        return build_message(message="Logout success.")

"""
/check_token - 检测 Token 有效性
请求：null
响应 - 成功：返回 200，失败：INVALID_TOKEN 无效的 TOKEN
"""
@app.route("/check_token", methods=["GET"])
@token_required
def check_token(user_id):
    token = request.headers.get("Authorization", None)
    logger.info(f"Received /check_token request: {token}")
    # 失效 Token 检测由 @token_required 实现
    logger.info(f"User ID {user_id}'s token \"{token}\" is valid, and expire time updated.")
    return build_message(message="Token is valid.")


"""
/add_medication - 添加使用药品
请求：POST {"medication_name", "patient_name", "dosage", "remaining_amount", "frequency", "week_mode", "reminder_type", "expiration_date", "date_list", "time_list"}
响应 - 成功：返回 200，失败：ADD_Medication_ERROR 添加药品失败
"""
@app.route("/add_medication", methods=["POST"])
@token_required
@json_required
def add_medication(user_id):
    data = request.get_json()
    logger.info(f"Received /add_medication request: {data}")

    medication_name = data["medication_name"]
    patient_name = data["patient_name"]
    dosage = data["dosage"]
    remaining_amount = data["remaining_amount"]
    frequency = data["frequency"]
    week_mode = data["week_mode"]
    reminder_type = data["reminder_type"]
    expiration_date = data["expiration_date"]
    date_list = data["date_list"]  # 用药日期列表
    time_list = data["time_list"]  # 用药时间列表

    with SQLiteConnection() as (conn, cursor):
        # 将药物的基本信息导入到 medication 库中
        # 根据选定的 date_list 更新 calendar_medication 库
        # 根据选定的 time_list 和 date_list 更新 medication_time 库

        cursor.execute(
            """
            INSERT INTO medication (
                user_id, medication_name, patient_name, dosage,
                remaining_amount, frequency, week_mode, reminder_type, expiration_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                user_id,
                medication_name,
                patient_name,
                dosage,
                remaining_amount,
                frequency,
                week_mode,
                reminder_type,
                expiration_date,
            ),
        )
        medication_id = cursor.lastrowid  # 获取插入的药物 ID

        # 插入日期列表到 calendar_medication 表
        for date in date_list:
            cursor.execute(
                """
                INSERT INTO calendar_medication (
                    user_id, medication_id, date
                ) VALUES (?, ?, ?)
                """,
                (user_id, medication_id, date),
            )
            date_id = cursor.lastrowid
            for time in time_list:
                cursor.execute(
                    """
                    INSERT INTO medication_time (
                        user_id, medication_id, date_id, time
                    ) VALUES (?, ?, ?, ?)
                    """,
                    (user_id, medication_id, date_id, time),
                )
        conn.commit()

    logger.info(f'Add_medication "{medication_name}" success.')
    return build_message(message="Add_medication success", data=medication_id)


"""
/check_time - 获取用药时间
请求：GET {"medication_id", "date"}
响应 - 成功：返回 200 和用药时间，失败：NULL_TIME 未找到用药时间
"""
@app.route("/check_time", methods=["GET"])
@token_required
@json_required
def check_time(user_id):
    data = request.get_json()
    logger.info(f"Received /check_time request: {data}")

    medication_id = data["medication_id"]
    date = data["date"]

    with SQLiteConnection() as (conn, cursor):
        cursor.execute(
            """SELECT id
            FROM calendar_medication
            WHERE user_id = ? AND medication_id = ? AND date = ?""",
            (user_id, medication_id, date),
        )
        date_id = cursor.fetchone()

        cursor.execute(
            """
            SELECT time
            FROM medication_time
            WHERE user_id = ? AND medication_id = ? AND date_id = ?
        """,
            (user_id, medication_id, date_id),
        )
        times = cursor.fetchall()

        if not times:
            logger.warning("NULL_TIME: No medication times found.")
            return build_message(message="NULL_TIME", code=404)

        time_list = [row[0] for row in times]
        conn.commit()
    logger.info(f"Check_time success: {time_list}")
    return build_message(message="Check_time success", data=time_list, code=200)


"""
/check_date - 获取用药记录
请求：GET {"date"}
响应 - 成功：返回 200 和用药的id，失败：NULL_Record 未找到用药记录
"""
@app.route("/check_date", methods=["GET"])
@token_required
@json_required
def check_date(user_id):
    data = request.get_json()
    logger.info(f"Received /check_date request: {data}")

    date = data["date"]

    with SQLiteConnection() as (conn, cursor):
        cursor.execute(
            """
            SELECT medication_id
            FROM calendar_medication
            WHERE user_id = ? AND date = ?
        """,
            (user_id, date),
        )
        medications = cursor.fetchall()

        if not medications:
            logger.warning("NULL_Record: No medication records found.")
            return build_message(message="NULL_Record", code=404)

        medic_list = [row[0] for row in medications]
        conn.commit()
    logger.info(f"Check_date success: {medic_list}")
    return build_message(message="Check_date success", data=medic_list, code=200)


"""
/check_medic - 获取药物信息
请求：GET {"medication_id"}
响应 - 成功：返回 200 和药物信息，失败：NULL_Medic 未找到药物
"""
@app.route("/check_medic", methods=["GET"])
@token_required
@json_required
def check_medic(user_id):
    data = request.get_json()
    logger.info(f"Received /check_medic request: {data}")

    medication_id = data["medication_id"]

    with SQLiteConnection() as (conn, cursor):
        cursor.execute(
            """
                SELECT id, medication_name, patient_name, dosage, remaining_amount,
                frequency, week_mode, reminder_type, expiration_date
                FROM medication
                WHERE user_id = ? AND id = ?
            """,
            (user_id, medication_id),
        )
        result = cursor.fetchone()

        if not result:
            logger.warning("NULL_Medic: No medication found.")
            return build_message(message="NULL_Medic", code=404)

        medication_info = {
            "medication_id": result[0],
            "medication_name": result[1],
            "patient_name": result[2],
            "dosage": result[3],
            "remaining_amount": result[4],
            "frequency": result[5],
            "week_mode": result[6],
            "reminder_type": result[7],
            "expiration_date": result[8],
        }
        conn.commit()
    logger.info(f"Check_medic success: {medication_info}")
    return build_message(message="Check_medic success", data=medication_info, code=200)


"""
/check_all - 根据用户日期获取在这个日期下的全部药物信息
请求：GET {"date"}
响应 - 成功：返回 200 和全部药物信息，失败：NULL_Record 未找到药物
"""
@app.route("/check_all", methods=["GET"])
@token_required
def check_all(user_id):
    date = request.args.get("date")
    logger.info(f"Received /check_all request: {date}")

    results = []

    with SQLiteConnection() as (conn, cursor):
        cursor.execute(
            """
            SELECT medication_id
            FROM calendar_medication
            WHERE user_id = ? AND date = ?
        """,
            (user_id, date),
        )
        medications = cursor.fetchall()

        if not medications:
            logger.warning("NULL_Record: No medication records found.")
            return build_message(message="NULL_Record", code=404)
        medic_list = [row[0] for row in medications]

        for i in medic_list:
            cursor.execute(
                """
                SELECT id
                FROM calendar_medication
                WHERE user_id = ? AND medication_id = ? AND date = ?
            """,
                (user_id, i, date),
            )
            date_id = cursor.fetchone()[0]
            cursor.execute(
                """
                SELECT id, medication_name, patient_name, dosage, remaining_amount,
                frequency, week_mode, reminder_type, expiration_date
                FROM medication
                WHERE user_id = ? AND id = ?
            """,
                (user_id, i),
            )
            result = cursor.fetchone()
            cursor.execute(
                """
                SELECT time
                FROM medication_time
                WHERE user_id = ? AND medication_id = ? AND date_id = ?
            """,
                (user_id, i, date_id),
            )
            times = [row[0] for row in cursor.fetchall()]
            medicationInfo = {
                "medication_id": result[0],
                "medication_name": result[1],
                "patient_name": result[2],
                "dosage": result[3],
                "remaining_amount": result[4],
                "frequency": result[5],
                "times": times,
                "week_mode": result[6],
                "reminder_mode": result[7],
                "expiry_date": result[8],
            }
            results.append(medicationInfo)
        conn.commit()

    logger.info(f"Check_all success: {results}")
    return build_message(message="Check_all success", data=results, code=200)


"""
/delete_record - 根据用户日期和药物信息id(在具体日期下药物信息id唯一)
请求：DELETE {"date", "medication_id"}
响应 - 成功：返回 200，失败：数据库同步问题/ 显示问题
"""
@app.route("/delete_record", methods=["DELETE"])
@token_required
def delete_record(user_id):
    date = request.args.get("date")
    medication_id = request.args.get("medication_id")

    try:
        with SQLiteConnection() as (conn, cursor):
            cursor.execute(
                """
                DELETE FROM calendar_medication
                WHERE
                date = ? AND medication_id = ? AND user_id = ?
            """,
                (date, medication_id, user_id),
            )
            conn.commit()
            if cursor.rowcount > 0:
                return build_message(message="Record deleted successfully", code=200)
            else:
                return build_message(message="Record not found", code=404)
    except Exception as e:
        return build_message(message=f"Error occurred: {str(e)}", code=501)
