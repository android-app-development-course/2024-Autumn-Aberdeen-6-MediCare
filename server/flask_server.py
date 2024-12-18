import os
import logging
from flask import Flask, request
from database import SQLiteConnection
from db_initialize import table_names
from server.utils import build_message
from function_decorator import json_required, token_required
from token_manager import new_token, validate_token, invalidate_token

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
响应 - 成功：返回 200 和 {"token": "登录 Token"} ，失败：INVALID_USERNAME_OR_PASSWORD 无效的用户名或密码
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
@app.route("/logout", methods=["POST"]) # 不使用 @token_required 以手动处理 Token 失效情形
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
/checkToken - 检测 Token 有效性
请求：null
响应 - 成功：返回 200，失败：INVALID_TOKEN 无效的 TOKEN
"""
@app.route("/checkToken", methods=["GET"])
@token_required
def check_token(user_id):
    token = request.headers.get("Authorization", None)
    logger.info(f"Received /checkToken request: {token}")
    # 失效 Token 检测由 @token_required 实现
    logger.info(f"User ID {user_id}'s token \"{token}\" is valid, and expire time updated.")
    return build_message(message="Token is valid.")

"""
/getLastUpdateTime - 获取用户服务端数据的上次更新时间（时间戳）
请求：null
响应 - 成功：返回 200 和 {"lastUpdateTime": 1734496054}
"""
@app.route("/getLastUpdateTime", methods=["GET"])
@token_required
def get_last_update_time(user_id):
    logger.info(f"Received /getLastUpdateTime from user {user_id}")

    with SQLiteConnection() as (conn, cursor):
        query = "SELECT updated_at FROM user WHERE user_id = ?"
        params = (user_id, )
        cursor.execute(query, params)

        last_update_time = cursor.fetchone()[0]

    data = {"lastUpdateTime": last_update_time}
    return build_message(message="Successfully get last update time.", data=data)

"""
/getMedicationData - 从服务器获取 medication 表中的信息
请求：null
响应：{
    "medication": [
        {
            "uuid": "客户端 UUID",
            "medicationName": "药品名称",
            "patientName": "病人名称",
            "dosage": "剂量，如2片",
            "remainingAmount": "1片", // 余量
            "frequency": "用药频率",
            "weekMode": "",
            "reminderType": "提醒方式的编码",
            "expirationDate": "过期日期，如 2024-12-17"
        },
        ...
    ]
}
"""
@app.route("/getMedicationData", methods=["GET"])
@token_required
def get_medication_data(user_id):
    logger.info(f"Received /getMedicationData request from user {user_id}")
    medication = []

    with SQLiteConnection() as (conn, cursor):
        # medication
        query = "SELECT client_uuid, medication_name, patient_name, dosage, remaining_amount, frequency, week_mode, reminder_type, expiration_date FROM medication WHERE user_id = ?"
        params = (user_id, )
        cursor.execute(query, params)

        results = cursor.fetchall()

        for result in results:
            medication.append({
                "uuid": result[0],
                "medicationName": result[1],
                "patientName": result[2],
                "dosage": result[3],
                "remainingAmount": result[4],
                "frequency": result[5],
                "weekMode": result[6],
                "reminderType": result[7],
                "expirationDate": result[8]
            })

    data = {
        "medication": medication
    }

    logger.info(f"Successfully get medication list data of user {user_id}: {data}")
    return build_message(message="Successfully get medication list data.", data=data)


"""
/getCalendarMedicationData - 从服务器获取 calendar_medication 表中的信息
请求：null
响应：{
    "calendarMedication": [
        {
            "uuid": "客户端 UUID",
            "medicationUuid": "medication 表中的 UUID",
            "date": "日期，如 2024-12-17"
        },
        ...
    ]
}
"""
@app.route("/getCalendarMedicationData", methods=["GET"])
@token_required
def get_calendar_medication_data(user_id):
    logger.info(f"Received /getCalendarMedicationData request from user {user_id}")
    calendar_medication = []

    with SQLiteConnection() as (conn, cursor):
        query = ("SELECT "
                 "c.client_uuid AS uuid, m.client_uuid AS medication_uuid, c.date "
                 "FROM calendar_medication AS c JOIN medication AS m "
                 "ON c.medication_id = m.id WHERE c.user_id = ?")
        params = (user_id,)
        cursor.execute(query, params)

        results = cursor.fetchall()
        for result in results:
            calendar_medication.append({
                "uuid": result[0],
                "medicationUuid": result[1],
                "date": result[2]
            })
    
    data = {
        "calendarMedication": calendar_medication,
    }

    logger.info(f"Successfully get calendar_medication list data of user {user_id}: {data}")
    return build_message(message="Successfully get calendar_medication list data.", data=data)

"""
/getMedicationTimeData - 从服务器获取 medication_time 表中的信息
请求：null
响应：{
    "medicationTime": [
        {
            "uuid": "客户端 UUID",
            "medicationUuid": "medication 表中的 UUID",
            "dateUuid": "calendar_medication 表中的 UUID",
            "status": 0,    // 用药状态
            "time": "用药时间"
        },
        ...
    ]
}
"""
@app.route("/getMedicationTimeData", methods=["GET"])
@token_required
def get_medication_time_data(user_id):
    logger.info(f"Received /getMedicationTimeData request from user {user_id}")
    medication_time = []

    with SQLiteConnection() as (conn, cursor):
        query = ("SELECT "
                 "  t.client_uuid AS uuid, "
                 "  m.client_uuid AS medication_uuid, "
                 "  c.client_uuid AS date_uuid, "
                 "  t.status AS status, "
                 "  t.time AS time "
                 "FROM medication_time AS t "
                 "JOIN medication AS m ON t.medication_id = m.id "
                 "JOIN calendar_medication AS c ON t.date_id = c.id"
                 "WHERE user_id = ?")
        params = (user_id, )
        cursor.execute(query, params)

        results = cursor.fetchall()
        for result in results:
            medication_time.append({
                "uuid": result[0],
                "medicationUuid": result[1],
                "dateUuid": result[2],
                "status": result[3],
                "time": result[4]
            })
    
    data = {
        "medicationTime": medication_time
    }

    logger.info(f"Successfully get medication_time list data of user {user_id}: {data}")
    return build_message(message="Successfully get medication_time list data.", data=data)

"""
/clearData - 删除用户的全部数据
请求：null
响应 - 成功：200
"""
@app.route("/clearData", methods=["DELETE"])
@token_required
def clear_data(user_id):
    logger.info(f"Received /clearData request from user {user_id}")

    table_names_ = table_names.copy()
    table_names_.remove("user")
    table_names_.remove("token")

    with SQLiteConnection() as (conn, cursor):
        for table in table_names_:
            query = f"DELETE FROM {table} WHERE user_id = ?"
            params = (user_id,)
            cursor.execute(query, params)
        
        conn.commit()


"""
/insertMedicationData - 插入数据至 medication 表
请求：{
    "medication": [
        {
            "uuid": "客户端 UUID",
            "medicationName": "药品名称",
            "patientName": "病人名称",
            "dosage": "剂量，如2片",
            "remainingAmount": "1片", // 余量
            "frequency": "用药频率",
            "weekMode": "",
            "reminderType": "提醒方式的编码",
            "expirationDate": "过期日期，如 2024-12-17"
        }
    ]
}
响应 - 成功：200
"""
@app.route("/insertMedicationData", methods=["POST"])
@token_required
@json_required
def insert_medication_data(user_id):
    data = request.get_json()
    logger.info(f"Received /insertMedicationData request from user {user_id}: {data}")

    medications = []
    for raw_data in data["medication"]:
        client_uuid = raw_data["uuid"]
        meidcation_name = raw_data["medicationName"]
        patient_name = raw_data["patientName"]
        dosage = raw_data["dosage"]
        remaining_amount = raw_data["remaining_amount"]
        frequency = raw_data["frequency"]
        week_mode = raw_data["weekMode"]
        reminder_type = raw_data["reminderType"]
        expiration_date = raw_data["expirationDate"]
        medications.insert((user_id, meidcation_name, patient_name, dosage, remaining_amount, frequency, week_mode, reminder_type, expiration_date, client_uuid))
    
    query = "INSERT INTO medication (user_id, medication_name, patient_name, dosage, remaining_amount, frequency, week_mode, reminder_type, expiration_date, client_uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    with SQLiteConnection() as (conn, cursor):
        cursor.executemany(query, medications)
        conn.commit()
    
    logger.info("Successfully inserted data.")
    return build_message(message="Successfully inserted data.")


"""
/insertCalendarMedicationData - 插入数据至 calendar_medication 表
请求：{
    "calendarMedication": [
        {
            "uuid": "客户端 UUID",
            "medicationUuid": "medication 表中的 UUID",
            "date": "日期，如 2024-12-17"
        },
        ...
    ]
}
响应 - 成功：200
"""
@app.route("/insertCalendarMedicationData", methods=["POST"])
@token_required
@json_required
def insert_calendar_medication_data(user_id):
    data = request.get_json()
    logger.info(f"Received /insertCalendarMedicationData request from user {user_id}: {data}")

    calendar_medications = []
    with SQLiteConnection() as (conn, cursor):
        query1 = "SELECT client_uuid, id FROM medication WHERE user_id = ?"
        params1 = (user_id, )
        cursor.execute(query1, params1)
        medication_uuid_map = cursor.fetchall()

        for raw_data in data["calendarMedication"]:
            client_uuid = raw_data["uuid"]
            date = raw_data["date"]

            for medication_uuid, id in medication_uuid_map:
                if raw_data["medicationUuid"] == medication_uuid:
                    medication_id = id
                    break

            calendar_medications.append((user_id, medication_id, date, client_uuid))
        
        query2 = "INSERT INTO calendar_medication (user_id, medication_id, date, client_uuid) VALUES (?, ?, ?, ?)"
        cursor.executemany(query2, calendar_medications)
        conn.commit()
    
    logger.info("Successfully inserted data.")
    return build_message(message="Successfully inserted data.")


"""
/insertMedicationTimeData - 插入数据至 medication_time 表
请求：{
    "medicationTime": [
        {
            "uuid": "客户端 UUID",
            "medicationUuid": "medication 表中的 UUID",
            "dateUuid": "calendar_medication 表中的 UUID",
            "status": 0,    // 用药状态
            "time": "用药时间"
        },
        ...
    ]
}
响应 - 成功：200
"""
@app.route("/insertMedicationTimeData", methods=["POST"])
@token_required
@json_required
def insert_medication_time_data(user_id):
    data = request.get_json()
    logger.info(f"Received /insertMedicationTimeData request from user {user_id}: {data}")

    medication_times = []

    with SQLiteConnection() as (conn, cursor):
        query1 = "SELECT client_uuid, id FROM medication WHERE user_id = ?"
        params1 = (user_id, )
        cursor.execute(query1, params1)
        medication_uuid_map = cursor.fetchall()

        query2 = "SELECT client_uuid, id FROM calendar_medication WHERE user_id = ?"
        params2 = (user_id, )
        cursor.execute(query2, params2)
        date_uuid_map = cursor.fetchall()

        for raw_data in data["calendarMedication"]:
            client_uuid = raw_data["uuid"]
            status = raw_data["status"]
            time = raw_data["time"]

            for medication_uuid, id in medication_uuid_map:
                if raw_data["medicationUuid"] == medication_uuid:
                    medication_id = id
                    break
            
            for date_uuid, id in date_uuid_map:
                if raw_data["dateUuid"] == date_uuid:
                    date_id = id
                    break
            
            medication_times.append((user_id, medication_id, date_id, status, time, client_uuid))
            
        query3 = "INSERT INTO medication_time (user_id, medication_id, date_id, status, time, client_uuid)"
        cursor.executemany(query3, medication_times)
        conn.commit()
    
    logger.info("Successfully inserted data.")
    return build_message(message="Successfully inserted data.")