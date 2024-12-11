"""
修饰器
"""
from flask import Flask, request, jsonify
from functools import wraps

from message_builder import build_message
from token_manager import validate_token, update_token_expire_time

"""要求 JSON"""
def json_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if not request.is_json:
            return build_message(code=500, success=False, err_code="BAD_REQUEST", err_description="Request must be JSON.")
        return f(*args, **kwargs)
    return wrap

"""要求 Token，该操作会同时延长 Token 有效期"""
def token_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        token = request.headers.get("Authorization", None)

        if token is None:
            return build_message(code=401, success=False, err_code="TOKEN_REQUIRED", err_description="Authorization token must included in the request.")
        elif not validate_token(token):
            return build_message(code=401, success=False, err_code="INVALID_TOKEN", err_description="Token is invalid or expired.")
        else:
            update_token_expire_time(token)
            return f(*args, **kwargs)
    return wrap