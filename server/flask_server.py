import os
import logging
from traceback import format_exc
from flask import Flask, request
from database import SQLiteConnection
from message_builder import build_message
from function_decorator import json_required, token_required
from token_manager import new_token, validate_token, invalidate_token, invalidate_all_token

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
/addMedication - 添加药品
请求：{
    "medicationName": "string",     // 药品名称
    "patientName": "string",        // 用药人
    "dosage": "string",             // 剂量（如2片）
    "remainingAmount": 1,           // 余量（整数）
    "frequency": "string",          // 用药频率
    "weekMode": "0000010",          // 周一至周日，1服用0不服用，如此处为周六服用
    "reminderType": "00",           // 提醒方式，第一个闹钟，第二个应用
    "expirationDate": "string",     // 过期日期
    "dateList": ["2024-12-14", "2024-12-16"],  // 服用日期
    "timeList": ["12:30", "14:30"]  // 服用时间
}
响应 - 成功：返回 200，失败：ADD_MEDICATION_ERROR 药品添加失败
"""
@app.route("/addMedication", methods=["POST"])
@token_required
@json_required
def add_medication(user_id):
    data = request.get_json()
    logger.info(f"Received /addMedication request: {data}")

    medication_name = data["medicationName"]
    patient_name = data["patientName"]
    dosage = data["dosage"]
    remaining_amount = data["remainingAmount"]
    frequency = data["frequency"]
    week_mode = data["weekMode"]
    reminder_type = data["reminderType"]
    expiration_date = data["expirationDate"]
    date_list = data["dateList"]  # 用药日期列表
    time_list = data["timeList"]  # 用药时间列表

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
                        user_id, medication_id, date_id, status,time
                    ) VALUES (?, ?, ?, ?, ?)
                    """,
                    (user_id, medication_id, date_id, -1, time),
                )
        conn.commit()

    logger.info(f'Add_medication "{medication_name}" success.')
    return build_message(message="Successfully added medication", data=medication_id)


"""
/getMedicationTimes - 获取用药时间
请求：{
    "medicationId": 1,      // 药物 ID
    "date": "2024-12-16"    // 用药时间
}
响应 - 成功：返回 200 和用药时间，或 204 无用药时间
"""
@app.route("/getMedicationTimes", methods=["POST"])
@token_required
@json_required
def get_medication_times(user_id):
    data = request.get_json()
    logger.info(f"Received /getMedicationTimes request: {data}")

    medication_id = data["medicationId"]
    date = data["date"]
    results = []

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
            SELECT status, time
            FROM medication_time
            WHERE user_id = ? AND medication_id = ? AND date_id = ?
        """,
            (user_id, medication_id, date_id),
        )
        result = cursor.fetchall()

        if not result:
            logger.log(
                f"No medication time data found for user {user_id} for medicine {medication_id} on {date}"
            )
            return build_message(code=204)
        for row in result:
            results.append({"status": row[0], "time": row[1]})
        conn.commit()
    logger.info(
        f"Successfully found medication time data for user {user_id} for medicine {medication_id} on {date}: {results}."
    )
    return build_message(message="Successfully get medication time data.", data=results)


"""
/getMedicationRecords - 获取指定日期的用药记录
请求：{"date": "2024-12-16"}
响应 - 成功：返回 200 和用药的 ID，或 204 无用药记录
"""
@app.route("/getMedicationRecords", methods=["POST"])
@token_required
@json_required
def get_medication_records(user_id):
    data = request.get_json()
    logger.info(f"Received /getMedicationRecords request: {data}")

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
            logger.warning(f"No medication record data found for user {user_id} for medicin on {date}")
            return build_message(code=204)

        medic_list = [row[0] for row in medications]
        conn.commit()
    logger.info(f"Successfully found medication time data for user {user_id} on {date}: {medic_list}.")
    return build_message(message="Successfully get medication data", data=medic_list)


"""
/getMedicationInfo - 获取用药信息
请求：{"medicationId": 0}
响应 - 成功：返回 200 和用药信息，失败：MEDICATION_NOT_FOUND 未找到药物
"""
@app.route("/getMedicationInfo", methods=["POST"])
@token_required
@json_required
def get_medication_info(user_id):
    data = request.get_json()
    logger.info(f"Received /getMedicationInfo request: {data}")

    medication_id = data["medicationId"]

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
            logger.warning(f"No medication found of ID {medication_id}.")
            return build_message(err_description=f"No medication data found about the medication {medication_id}",err_code="MEDICATION_NOT_FOUND", code=404, success=False)

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
    logger.info(f"Successfully found medication data of ID {medication_id}: {medication_info}")
    return build_message(message="Successfully get medication data.", data=medication_info, code=200)


"""
/getAllOnDate - 根据用户日期获取在这个日期下的全部药物信息
请求：GET {"date": "2024-12-16"}
响应 - 成功：返回 200 和全部药物信息，或 204 未找到该日期下的信息
"""
@app.route("/getAllOnDate", methods=["POST"])
@json_required
@token_required
def get_all_on_date(user_id):
    data = request.get_json()
    logger.info(f"Received /getAllOnDate request: {data}")

    date = data["date"]

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
            logger.info(f"No medication data found for user {user_id} on {date}.")
            return build_message(code=204)
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
                SELECT status, time
                FROM medication_time
                WHERE user_id = ? AND medication_id = ? AND date_id = ?
            """,
                (user_id, i, date_id),
            )
            timeInfo = cursor.fetchall()
            times = []
            status = []
            for row in timeInfo:
                status.append(row[0])
                times.append(row[1])
            medicationInfo = {
                "medication_id": result[0],
                "medication_name": result[1],
                "patient_name": result[2],
                "dosage": result[3],
                "remaining_amount": result[4],
                "frequency": result[5],
                "status": status,
                "times": times,
                "week_mode": result[6],
                "reminder_mode": result[7],
                "expiry_date": result[8],
            }
            results.append(medicationInfo)
        conn.commit()

    logger.info(f"Successfully get all medication data for user {user_id} on date {date}: {results}")
    return build_message(message=f"Successfully get all medication data on date {date}", data=results)


"""
/deleteMedicationRecord - 根据用户日期和药物信息 ID 删除用药信息（在具体日期下药物信息id唯一）
请求：{"date": "2024-12-16", "medicationId": 1}
响应 - 成功：返回 200，失败：RECORD_NOT_FOUND 未找到用药信息，INTERNAL_SERVER_ERROR 服务器内部错误
"""
@app.route("/deleteMedicationRecord", methods=["POST"])
@token_required
def delete_record(user_id):
    data = request.get_json()
    logger.info(f"Received /getAllOnDate request: {data}")

    date = data["date"]
    medication_id = data["medicationId"]

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
                logger.info(f"Successfully delete record {medication_id} on date {date}")
                return build_message(message="Successfully deleted record.")
            else:
                logger.info(f"Falied to delete record {medication_id} on date {date}: record not found")
                return build_message(err_code="RECORD_NOT_FOUND", err_description=f"Record {medication_id} not found on date {date}", code=404, success=False)
    except Exception as e:
        logger.error(f"An error occurred while deleting records {medication_id} on {date}: {format_exc()}")
        return build_message(err_code="INTERNAL_SERVER_ERROR",err_descriptin=f"An error occurred while trying to process", code=500, success=False)

"""
/getLastUpdateTime - 获取用户服务端数据的上次更新时间（时间戳）
请求：null
响应 - 成功：返回 200 和 {"lastUpdateTime": "2024-12-17 18:36:00"}
"""
@app.route("/getLastUpdateTime", methods=["GET"])
@token_required
def get_last_update_time(user_id):
    logger.info(f"Received /getLastUpdateTime from user {user_id}")

    with SQLiteConnection() as (conn, cursor):
        query = "SELECT last_update FROM user WHERE user_id = ?"
        params = (user_id, )
        cursor.execute(query, params)

        last_update_time = cursor.fetchone()[0]

    data = {"lastUpdateTime": last_update_time}
    return build_message(message="Successfully get last update time.", data=data)

"""
/getAllData - 从服务器获取全部信息
请求：null
响应：{
    "medication": [
        {
            "uuid": "客户端 UUID",
            "medicationName": "药品名称",
            "patientName": "病人名称",
            "dosage": "剂量，如2片",
            "remainingAmount": 1, // 余量
            "frequency": "用药频率",
            "weekMode": "",
            "reminderType": "提醒方式的编码",
            "expirationDate": "过期日期，如 2024-12-17"
        },
        ...
    ],
    "calendarMedication": [
        {
            "uuid": "客户端 UUID",
            "medicationUuid": "medication 表中的 UUID",
            "date": "日期，如 2024-12-17"
        },
        ...
    ],
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
@app.route("/getAllData", methods=["GET"])
@token_required
def get_all_data(user_id):
    logger.info(f"Received /getAllData request from user {user_id}")
    medication = []
    calendar_medication = []
    medication_time = []

    with SQLiteConnection() as (conn, cursor):
        query1 = "SELECT client_uuid, medication_name, patient_name, dosage, remaining_amount, frequency, week_mode, reminder_type, expiration_date FROM medication WHERE user_id = ?"
        params1 = (user_id, )
        cursor.execute(query1, params1)

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
        
        query2 = "SELECT c.client_uuid, m.client_uuid, c.date FROM calendar_medication c, medication m WHERE m.uuid = "