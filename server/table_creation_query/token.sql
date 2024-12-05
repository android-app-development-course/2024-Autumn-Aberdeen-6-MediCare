-- token 表
-- 用户登录凭据

CREATE TABLE `token` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,     -- 自增 ID
    `user_id` INTEGER NOT NULL,                 -- 用户 ID
    `token` TEXT UNIQUE NOT NULL,               -- 登录凭据 Token
    `expire_time` TIMESTAMP NOT NULL,           -- 凭据过期的时间
    FOREIGN KEY (user_id) REFERENCES user (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
        DEFERRABLE INITIALLY DEFERRED
);