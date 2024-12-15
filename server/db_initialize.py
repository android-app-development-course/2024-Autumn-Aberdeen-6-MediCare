"""
初始化数据库.
"""

import os

from database import SQLiteConnection

# 当前包所在目录
_package_dir = os.path.dirname(__file__)


def init_database(database_path: str) -> None:
    """
    初始化 SQLite 数据库.
    连接（若不存在则创建并连接）数据库，然后检查数据库中的表格是否完整.

    :param database_dir (str)：数据库路径.
    """

    with SQLiteConnection(database_path) as (conn, cursor):
        # 开始事务
        cursor.execute("BEGIN;")

        # 检查数据库中的表是否存在，不存在则创建
        table_names = [
            "user",
            "token",
            "medication",
            "calendar_medication",
            "medication_time",
        ]
        for table_name in table_names:
            cursor.execute(
                f"SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                (table_name,),
            )
            result = cursor.fetchone()

            # 若表不存在，创建表（并根据需要插入内容）
            if not result:
                with open(
                    os.path.join(
                        _package_dir, "table_creation_query", f"{table_name}.sql"
                    ),
                    encoding="utf-8",
                ) as f:
                    cursor.executescript(f.read())

        # 提交事务
        conn.commit()
