-- =============================================
-- Woodlin 默认密码重置迁移脚本
-- 版本: 20260505
-- 描述: 将 admin 和 demo 用户密码从 Passw0rd 改为 Aa@12345
-- 作者: yulin
-- 时间: 2026-05-05
-- =============================================

-- 更新默认用户密码为 Aa@12345 的 BCrypt 哈希
-- 新密码: Aa@12345
-- BCrypt Hash: $2a$10$Wv7noGb1lxadWCFIWdJEIO8mYl3uNgFXgUmtGgR3IVA7RznBEZtxS
UPDATE sys_user
SET password = '$2a$10$Wv7noGb1lxadWCFIWdJEIO8mYl3uNgFXgUmtGgR3IVA7RznBEZtxS',
    update_by = 'system',
    update_time = CURRENT_TIMESTAMP
WHERE username IN ('admin', 'demo');

-- 验证更新结果
SELECT user_id, username, nickname, 
       LEFT(password, 20) AS password_prefix,
       update_by, update_time
FROM sys_user
WHERE username IN ('admin', 'demo');
