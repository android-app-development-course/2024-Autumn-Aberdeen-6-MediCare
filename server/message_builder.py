"""
服务器返回体构建器.

返回体格式
{
    "code": 200,                    // HTTP 响应码
    "success": true,                // 请求是否成功
    "message": "Request success.",  // 描述信息
    "data": null,                   // 返回的具体数据（类型根据 API 决定）
    "error": {                      // 错误信息
        "code": null,               // 错误代码，如有错误为字符串
        "description": null         // 错误描述，如有错误为字符串
    },
    "timestamp": "2024-12-05T12:34:56+08:00" // 返回的时间戳（北京时间 UTC+8，ISO8601 格式）
}
"""

from typing import Tuple
from datetime import datetime
from pytz import timezone
from flask import jsonify

def build_message(code: int = 200,
                  success: bool = True,
                  message: str = None,
                  data: str | int | dict = None,
                  err_code: str = None,
                  err_description: str = None) -> Tuple[str, int]:
    if code == 204:
        return "", 204

    message = {
        "code": code,
        "success": success,
        "message": message,
        "data": data,
        "error": {
            "code": err_code,
            "description": err_description
        },
        "timestamp": datetime.now(timezone('Asia/Shanghai')).replace(microsecond=0).isoformat()
    }

    return jsonify(message), code