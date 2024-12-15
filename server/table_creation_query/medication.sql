CREATE TABLE `medication` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增 ID
    `user_id` INTEGER NOT NULL,               -- 用户 ID（外键，关联 `user` 表）
    `medication_name` TEXT NOT NULL,          -- 药物名称
    `patient_name` TEXT NOT NULL,             -- 病人名称
    `dosage` TEXT NOT NULL,                   -- 药物剂量（如：2片）
    `remaining_amount` INTEGER NOT NULL,      -- 药物余量（使用 INTEGER 类型）
    `frequency` TEXT NOT NULL,                -- 用药频率（如：每日一次）
    `week_mode` TEXT NOT NULL,
    `reminder_type` TEXT NOT NULL,            -- 提醒方式的编码，如 001,100,110,000
    `expiration_date` TEXT NOT NULL,     -- 药物过期日期
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
