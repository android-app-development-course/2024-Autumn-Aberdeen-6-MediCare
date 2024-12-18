"""
杂项
"""

from typing import Tuple
from datetime import datetime
from flask import jsonify

from server.database import SQLiteConnection

def build_message(code: int = 200,
                  success: bool = True,
                  message: str = None,
                  data: str | int | dict = None,
                  err_code: str = None,
                  err_description: str = None,
                  timestamp: datetime = datetime.now()) -> Tuple[str, int]:
    """
    服务器返回体构建器.

    返回体格式
    ```json
    {
        "code": 200,                    // HTTP 响应码
        "success": true,                // 请求是否成功
        "message": "Request success.",  // 描述信息
        "data": null,                   // 返回的具体数据（类型根据 API 决定）
        "error": {                      // 错误信息，如无错误为 null
            "code": null,               // 错误代码，字符串
            "description": null         // 错误描述，字符串
        },
        "timestamp": 1734495716         // 时间戳
    }
    ```
    """
    message = {
        "code": code,
        "success": success,
        "message": message,
        "data": data,
        "error": {
            "code": err_code,
            "description": err_description
        } if err_code else None,
        "timestamp": int(timestamp.replace(microsecond=0).timestamp())
    }

    return jsonify(message), code


def update_data_time(user_id: int, time: datetime = datetime.now()) -> datetime:
    """在数据库更新指定用户的数据更新时间，返回时间（datetime 对象）."""
    timestamp = int(time.replace(microsecond=0).timestamp())

    with SQLiteConnection() as (conn, cursor):
        query = "UPDATE user SET updated_at = ? WHERE user_id = ?"
        params = (timestamp, user_id)
        cursor.execute(query, params)

        conn.commit()
    
    return time