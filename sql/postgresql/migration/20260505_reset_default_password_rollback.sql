-- =============================================
-- Woodlin 默认密码重置迁移回滚脚本
-- 版本: 20260505
-- 描述: 将 admin 和 demo 用户密码从 Aa@12345 回滚到 Passw0rd
-- 作者: yulin
-- 时间: 2026-05-05
-- =============================================

-- 回滚用户密码为 Passw0rd 的 BCrypt 哈希
-- 旧密码: Passw0rd
-- BCrypt Hash: {bcrypt}$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu
UPDATE sys_user
SET password = '{bcrypt}$2a$10$7JB720yubVSO6yk5rYYepOkLHlD7VKXfMsZHVsGLQvAgvAG.1dONu',
    update_by = 'system',
    update_time = CURRENT_TIMESTAMP
WHERE username IN ('admin', 'demo');

-- 验证回滚结果
SELECT user_id, username, nickname, 
       LEFT(password, 20) AS password_prefix,
       update_by, update_time
FROM sys_user
WHERE username IN ('admin', 'demo');
