-- 添加 role 字段到 users 表
-- 如果字段已存在，此脚本会报错，可以忽略

-- 检查并添加 role 字段
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'role'
    ) THEN
        ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
        -- 为现有用户设置默认角色
        UPDATE users SET role = 'USER' WHERE role IS NULL;
    END IF;
END $$;

