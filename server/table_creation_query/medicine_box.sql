-- 药箱

CREATE TABLE `medicine_box` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,     -- 自增 ID
    `user_id` INTEGER NOT NULL,                 -- 用户 ID
    `box_name` TEXT NOT NULL,                   -- 药箱名称
    `box_type` TEXT NOT NULL,                   -- 药箱类型
    `applicable_people` TEXT NOT NULL,          -- 适用人
    `medication_id` TEXT DEFAULT NULL,          -- 药品 ID
    `remark` TEXT DEFAULT NULL,                 -- 标记
    `client_uuid` TEXT NOT NULL,                -- 本条数据的客户端 UUID
    FOREIGN KEY ('medication_id') REFERENCES `medication` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);