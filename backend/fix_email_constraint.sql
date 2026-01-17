-- 修复 users 表的 email 字段约束
-- 执行此 SQL 后，email 字段将允许为 NULL，注册时不再需要邮箱

-- 连接到数据库后执行：
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;

-- 验证修改是否成功：
SELECT column_name, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'email';

