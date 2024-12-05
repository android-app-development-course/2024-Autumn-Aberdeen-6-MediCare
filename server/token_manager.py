"""
用户登录凭据（Token）管理.

注意：包中的所有函数均不会验证传入的参数，调用函数前***必须***先验证用户 ID/Token 是否存在。
"""

import hashlib
from typing import Tuple
from datetime import datetime, timedelta
from pytz import timezone
from database import SQLiteConnection

EXPIRE_TIMEDELTA = timedelta(days=7)    # Token 过期时间
MAX_LOGINS = 3                          # 允许最大有效 Token 数

def new_token(user_id: int, username: str) -> str:
    """
    新建用户凭据，返回 Token.
    """
    token = _generate_token(username)
    expire_time = (datetime.now(timezone('Asia/Shanghai')).replace(microsecond=0) + EXPIRE_TIMEDELTA).strftime("%Y-%m-%d %H:%M:%S")
    
    with SQLiteConnection() as (conn, cursor):
        # 检查有效的 Token 数
        query = ("SELECT token FROM token "
                 "WHERE (user_id = ? AND expire_time > (DATETIME(CURRENT_TIMESTAMP, '+8 hours')))")
        params = (user_id, )
        cursor.execute(query, params)
        results = cursor.fetchall()

        # 登录数超出允许的 Token 数时，使最旧的一个或多个 Token 过期
        delta = len(results) + 1 - MAX_LOGINS   # +1：当前 Token
        if delta > 0:
            for result in results[:delta]:
                invalidate_token(result[0])

        query = ("INSERT INTO token (user_id, token, expire_time) "
                 "VALUES (?, ?, ?)")
        params = (user_id, token, expire_time)
        cursor.execute(query, params)
        conn.commit()
    
    return token

def validate_token(token: str) -> Tuple[int, str | None]:
    """
    验证用户凭据，返回元组（第一个值为用户 ID [错误返回 -1]，第二个值为错误信息.
    错误信息：None - 无错误，EXPIRED - 凭据已过期，NOT_EXIST - 凭据不存在.
    """
    with SQLiteConnection() as (conn, cursor):
        query = "SELECT user_id, expire_time FROM token WHERE token = ?"
        params = (token, )
        cursor.execute(query, params)
        result = cursor.fetchone()
    
    if result:
        user_id = result[0]
        expire_time = datetime.strptime(result[1], "%Y-%m-%d %H:%M:%S")

        if expire_time < datetime.now():
            return (-1, "EXPIRED")
        else:
            return (user_id, None)
    else:
        return (-1, "NOT_EXIST")

def update_token_expire_time(token: str,
                             expire_time: datetime = None,
                             expire_timedelta: timedelta = EXPIRE_TIMEDELTA):
    """
    更新某一凭据的过期时间.
    """
    if not expire_time:
        expire_time = datetime.now() + expire_timedelta

    with SQLiteConnection() as (conn, cursor):
        query = "UPDATE token SET expire_time = ? WHERE token = ?"
        params = (expire_time.strftime("%Y-%m-%d %H:%M:%S"), token)
        cursor.execute(query, params)
        conn.commit()

def invalidate_token(token: str):
    """
    使指定的凭证失效.
    """
    with SQLiteConnection() as (conn, cursor):
        query = "UPDATE token SET expire_time = ? WHERE token = ?"
        params = (datetime.now().strftime("%Y-%m-%d %H:%M:%S"), token)
        cursor.execute(query, params)
        conn.commit()

def invalidate_all_token(user_id: int):
    """
    使指定用户的全部登录凭证失效.
    """
    with SQLiteConnection() as (conn, cursor):
        query = "UPDATE token SET expire_time = ? WHERE user_id = ?"
        params = (datetime.now().strftime("%Y-%m-%d %H:%M:%S"), user_id)
        cursor.execute(query, params)
        conn.commit()

def _generate_token(username: str):
    """基于用户名和当前时间戳（整数）使用 SHA256 算法生成 Token."""
    timestamp = str(int(datetime.now().timestamp()))
    raw_data = username + timestamp
    token = hashlib.sha256(raw_data.encode()).hexdigest()
    return token
