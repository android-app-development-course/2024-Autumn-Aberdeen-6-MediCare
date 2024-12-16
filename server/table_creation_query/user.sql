-- user 表

-- 创建绑定信息表并设置触发器

CREATE TABLE `user` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,             -- 自增 ID
    `username` TEXT UNIQUE NOT NULL,                    -- 用户名（登陆凭证，唯一）
    `password_hash` TEXT NOT NULL,                      -- 以哈希存储的密码
    `created_at` TIMESTAMP NOT NULL DEFAULT (DATETIME(CURRENT_TIMESTAMP, '+8 hours'))
                                                        -- 注册时间（北京时间）
    `last_update` TIMESTAMP NOT NULL DEFAULT (DATETIME(CURRENT_TIMESTAMP, '+8 hours'))
                                                        -- 上次修改时间（北京时间）
);