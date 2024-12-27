# MediCare 服务器  
- MediCare App 服务器，存储用户信息  
- 默认端口为 5000，可在 `main.py` 中修改  
- Python 版本为 `3.11.0`  
- 关于服务器数据结构及接口文档，详见 [MediCare 技术细节文档](/doc/11%20MediCare%20技术细节.md)  

### 服务器目录  
- [**/main.py**](/server/main.py) - 服务器主函数  
- [**/table_creation_query**](/server/table_creation_query/) - 服务器数据库构建查询语句  
- [**/database.py**](/server/database.py) - 数据库访问对象，使用方法：  
    ```py
    with SQLiteConnection() as (conn, cursor):
        cursor.execute('INSERT INTO table VALUES ?, ?, ?', (value1, value2, value3))
        conn.commit()
    ```
- [**/db_initialize.py**](/server/db_initialize.py) - 初始化数据库功能，调用 `init_database()` 函数后会检查指定的表是否存在，若不存在则执行 [/table_creation_query](/server/table_creation_query/) 中的创建查询  
- [**/flask_server.py**](/server/flask_server.py) - 服务端 API 实现  
- [**/function_decorator.py**](/server/function_decorator.py) - 函数装饰器，在函数执行前执行的语句（如判断请求体是否为 JSON，是否有有效的 Token 等）  
- [**/token_manager.py**](/server/token_manager.py) - Token 管理，可检查 Token 是否有效，创建、删除和延长 Token 有效期  
- [**/utils.py**](/server/utils.py) - 工具函数，构建返回体和更新“更新时间”  

### 安装依赖  
```
pip install -r requirements.txt
```
- 所需的依赖  
    - Flask，服务端应用框架，版本 `2.2.5`  
    - pytz，用于时区转换，版本 `2023.3`  
    - 其他内置包  

### 运行  
```
python main.py
```