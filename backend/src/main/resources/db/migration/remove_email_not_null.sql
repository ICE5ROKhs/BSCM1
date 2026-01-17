-- 修改 users 表的 email 字段，允许为 NULL
-- 这样注册登录就不需要邮箱了

DO $$
BEGIN
    -- 检查 email 字段是否存在
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'email'
    ) THEN
        -- 如果字段存在且是 NOT NULL，则修改为允许 NULL
        ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
    END IF;
END $$;

