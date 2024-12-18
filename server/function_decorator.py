"""
修饰器
"""
import logging
from flask import request
from functools import wraps

from server.utils import build_message
from token_manager import validate_token, update_token_expire_time

logger = logging.getLogger(__name__)

"""要求 JSON"""
def json_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if not request.is_json:
            logger.warning("Recived a request that it's body not in JSON format.")
            return build_message(code=500, success=False, err_code="BAD_REQUEST", err_description="Request must be JSON.")
        return f(*args, **kwargs)
    return wrap

"""
要求 Token，该操作会验证 Token 是否有效
若有效，延长 Token 有效期并返回用户 ID（可在函数中通过 user_id）获取
"""
def token_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        token = request.headers.get("Authorization", None)

        if token is None:
            logger.warning("Recived a request that did not provide token.")
            return build_message(code=401, success=False, err_code="TOKEN_REQUIRED", err_description="Authorization token must included in the request.")
        
        user_id, invalid_reason = validate_token(token)
        if invalid_reason:
            logger.warning(f"Recived a request that it's token not valid, reason: {invalid_reason}")
            return build_message(code=401, success=False, err_code="INVALID_TOKEN", err_description="Token is invalid or expired.")
        else:
            logger.info("Token expire time updated.")
            update_token_expire_time(token)
            kwargs["user_id"] = user_id
            return f(*args, **kwargs)
    return wrap