CREATE TABLE `calendar_medication` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,     -- 自增主键
    `user_id` INTEGER NOT NULL,                 -- 用户 ID（外键，关联 `user` 表）
    `medication_id` INTEGER NOT NULL,           -- 药物 ID（外键，关联 `medications` 表）
    `date` DATE NOT NULL,                       -- 日期（例如：2024-12-13）
    `client_uuid` TEXT NOT NULL,                -- 本条数据的客户端 UUID
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`medication_id`) REFERENCES `medication` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
