"""
SQLite 数据库对象.
"""

import os
import sqlite3
from typing import Tuple

# 数据库路径
DATABASE_PATH = os.path.join(os.path.dirname(__file__), "data.db")

class SQLiteConnection:
    """
    SQLite 数据库对象.
    支持使用上下文管理器 with 来确保操作完成后关闭连接.
    """
    def __init__(self, db_path: str = DATABASE_PATH) -> None:
        """
        初始化 SQLite 类.
        
        :param db_path (str)：SQLite 数据库的路径.
        """
        self.db_path: str = db_path

    def __enter__(self) -> Tuple[sqlite3.Connection, sqlite3.Cursor]:
        """
        连接数据库，返回连接对象和游标（cursor）对象.
        若数据库不存在，将自动创建并连接数据库.

        :return (sqlite3.Connection, sqlite3.Cursor)
        """
        # 连接数据库（如果数据库不存在将自动创建）
        self.conn: sqlite3.Connection = sqlite3.connect(self.db_path)
        self.cursor: sqlite3.Cursor = self.conn.cursor()

        # 启用外键支持
        self.cursor.execute("PRAGMA foreign_keys = ON;")

        return self.conn, self.cursor

    def __exit__(self, exc_type, exc_value, traceback) -> None:
        """
        关闭数据库连接.

        发生异常时：
        :param exc_type：异常信息.
        :param exc_value：异常值.
        :param traceback：回溯信息.
        """
        # 关闭游标和连接对象
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.close()