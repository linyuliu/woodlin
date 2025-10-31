-- =============================================
-- Oracle version - Auto-converted from MySQL
-- Source: password_policy_update.sql
-- Database: Oracle 12c+
-- =============================================

-- =============================================
-- 密码策略功能数据库更新脚本
-- 作者: mumu
-- 描述: 为密码策略功能添加必要的数据库字段
-- 版本: 1.0.1
-- 时间: 2025-01-01
-- =============================================

-- ALTER SESSION SET CURRENT_SCHEMA = woodlin;

-- 为sys_user表添加密码策略相关字段
ALTER TABLE sys_user
ADD COLUMN pwd_change_time datetime DEFAULT NULL COMMENT '密码最后修改时间' AFTER lock_time,
ADD COLUMN is_first_login tinyint(1) DEFAULT 1 COMMENT '是否首次登录（0-否，1-是）' AFTER pwd_change_time,
ADD COLUMN pwd_expire_days int(11) DEFAULT NULL COMMENT '密码过期天数（0表示永不过期，优先于系统配置）' AFTER is_first_login;

-- 为已有用户设置默认值（将密码修改时间设为用户创建时间）
UPDATE sys_user SET pwd_change_time = create_time WHERE pwd_change_time IS NULL;

-- 添加索引以提高查询性能
ALTER TABLE sys_user ADD INDEX idx_pwd_change_time (pwd_change_time);
ALTER TABLE sys_user ADD INDEX idx_is_first_login (is_first_login);

-- 在sys_config表中添加密码策略相关配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark) VALUES
(1001, '密码策略开关', 'password.policy.enabled', 'true', 'Y', '是否启用密码策略'),
(1002, '首次登录修改密码', 'password.require.first.change', 'false', 'Y', '是否要求首次登录修改密码'),
(1003, '密码过期天数', 'password.expire.days', '0', 'Y', '密码过期天数，0表示永不过期'),
(1004, '密码过期提醒天数', 'password.warning.days', '7', 'Y', '密码过期前提醒天数'),
(1005, '最大密码错误次数', 'password.max.error.count', '5', 'Y', '最大密码错误次数'),
(1006, '账号锁定时长', 'password.lock.duration.minutes', '30', 'Y', '账号锁定时长（分钟）'),
(1007, '强密码策略', 'password.strong.required', 'false', 'Y', '是否启用强密码策略'),
(1008, '最小密码长度', 'password.min.length', '6', 'Y', '最小密码长度'),
(1009, '最大密码长度', 'password.max.length', '20', 'Y', '最大密码长度'),
(1010, '要求包含数字', 'password.require.digits', 'false', 'Y', '是否要求包含数字'),
(1011, '要求包含小写字母', 'password.require.lowercase', 'false', 'Y', '是否要求包含小写字母'),
(1012, '要求包含大写字母', 'password.require.uppercase', 'false', 'Y', '是否要求包含大写字母'),
(1013, '要求包含特殊字符', 'password.require.special.chars', 'false', 'Y', '是否要求包含特殊字符');