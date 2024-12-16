CREATE TABLE `medication_time` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,     -- 自增 ID
    `user_id` INTEGER NOT NULL,                 -- 用户 ID（外键，关联 `user` 表）
    `medication_id` INTEGER NOT NULL,           -- 药物 ID（外键，关联 `medications` 表）
    `date_id` INTEGER NOT NULL,
    `time` TIME NOT NULL,                       -- 用药时间（例如：08:00）
    `state` INTEGER NOT NULL,                   -- 用药状态
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`medication_id`) REFERENCES `medication` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`date_id`) REFERENCES `calendar_medication` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
