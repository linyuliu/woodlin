-- =============================================
-- 系统配置数据 - API加密和密码策略
-- =============================================

-- API 加密配置
INSERT INTO `sys_config` VALUES 
(100, 'API加密-启用开关', 'api.encryption.enabled', 'false', 'Y', 'default', '是否启用API加密功能（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(101, 'API加密-加密算法', 'api.encryption.algorithm', 'AES', 'Y', 'default', '加密算法类型（AES、RSA、SM4）', 'system', NOW(), 'system', NOW(), '0'),
(102, 'API加密-AES密钥', 'api.encryption.aes-key', '', 'Y', 'default', 'AES加密密钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(103, 'API加密-AES向量', 'api.encryption.aes-iv', '', 'Y', 'default', 'AES初始化向量IV（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(104, 'API加密-AES模式', 'api.encryption.aes-mode', 'CBC', 'Y', 'default', 'AES加密模式（CBC、ECB、CFB、OFB、CTR）', 'system', NOW(), 'system', NOW(), '0'),
(105, 'API加密-AES填充', 'api.encryption.aes-padding', 'PKCS5Padding', 'Y', 'default', 'AES填充方式（PKCS5Padding、PKCS7Padding、NoPadding）', 'system', NOW(), 'system', NOW(), '0'),
(106, 'API加密-RSA公钥', 'api.encryption.rsa-public-key', '', 'Y', 'default', 'RSA公钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(107, 'API加密-RSA私钥', 'api.encryption.rsa-private-key', '', 'Y', 'default', 'RSA私钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(108, 'API加密-RSA密钥长度', 'api.encryption.rsa-key-size', '2048', 'Y', 'default', 'RSA密钥长度（1024、2048、4096）', 'system', NOW(), 'system', NOW(), '0'),
(109, 'API加密-SM4密钥', 'api.encryption.sm4-key', '', 'Y', 'default', 'SM4加密密钥（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(110, 'API加密-SM4向量', 'api.encryption.sm4-iv', '', 'Y', 'default', 'SM4初始化向量IV（Base64编码）', 'system', NOW(), 'system', NOW(), '0'),
(111, 'API加密-SM4模式', 'api.encryption.sm4-mode', 'CBC', 'Y', 'default', 'SM4加密模式（CBC、ECB）', 'system', NOW(), 'system', NOW(), '0'),
(112, 'API加密-包含路径', 'api.encryption.include-patterns', '', 'Y', 'default', '需要加密的接口路径模式（多个用逗号分隔）', 'system', NOW(), 'system', NOW(), '0'),
(113, 'API加密-排除路径', 'api.encryption.exclude-patterns', '', 'Y', 'default', '排除加密的接口路径模式（多个用逗号分隔）', 'system', NOW(), 'system', NOW(), '0'),
(114, 'API加密-加密请求', 'api.encryption.encrypt-request', 'true', 'Y', 'default', '是否加密请求体（true加密，false不加密）', 'system', NOW(), 'system', NOW(), '0'),
(115, 'API加密-加密响应', 'api.encryption.encrypt-response', 'true', 'Y', 'default', '是否加密响应体（true加密，false不加密）', 'system', NOW(), 'system', NOW(), '0');

-- 密码策略配置
INSERT INTO `sys_config` VALUES 
(200, '密码策略-启用开关', 'password.policy.enabled', 'true', 'Y', 'default', '是否启用密码策略（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(201, '密码策略-首次登录修改', 'password.policy.require-change-on-first-login', 'false', 'Y', 'default', '是否要求首次登录修改密码（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(202, '密码策略-过期天数', 'password.policy.expire-days', '0', 'Y', 'default', '密码过期天数（0表示永不过期）', 'system', NOW(), 'system', NOW(), '0'),
(203, '密码策略-提醒天数', 'password.policy.warning-days', '7', 'Y', 'default', '密码过期前提醒天数', 'system', NOW(), 'system', NOW(), '0'),
(204, '密码策略-最大错误次数', 'password.policy.max-error-count', '5', 'Y', 'default', '最大密码错误次数（超过将锁定账号）', 'system', NOW(), 'system', NOW(), '0'),
(205, '密码策略-锁定时长', 'password.policy.lock-duration-minutes', '30', 'Y', 'default', '账号锁定时长（分钟）', 'system', NOW(), 'system', NOW(), '0'),
(206, '密码策略-强密码要求', 'password.policy.strong-password-required', 'false', 'Y', 'default', '是否启用强密码策略（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(207, '密码策略-最小长度', 'password.policy.min-length', '6', 'Y', 'default', '最小密码长度', 'system', NOW(), 'system', NOW(), '0'),
(208, '密码策略-最大长度', 'password.policy.max-length', '20', 'Y', 'default', '最大密码长度', 'system', NOW(), 'system', NOW(), '0'),
(209, '密码策略-要求数字', 'password.policy.require-digits', 'false', 'Y', 'default', '是否要求包含数字（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(210, '密码策略-要求小写字母', 'password.policy.require-lowercase', 'false', 'Y', 'default', '是否要求包含小写字母（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(211, '密码策略-要求大写字母', 'password.policy.require-uppercase', 'false', 'Y', 'default', '是否要求包含大写字母（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0'),
(212, '密码策略-要求特殊字符', 'password.policy.require-special-chars', 'false', 'Y', 'default', '是否要求包含特殊字符（true要求，false不要求）', 'system', NOW(), 'system', NOW(), '0');

-- 用户活动监控配置
INSERT INTO `sys_config` VALUES 
(300, '活动监控-启用开关', 'activity.monitoring.enabled', 'true', 'Y', 'default', '是否启用用户活动监控（true启用，false禁用）', 'system', NOW(), 'system', NOW(), '0'),
(301, '活动监控-超时时间', 'activity.monitoring.timeout-seconds', '1800', 'Y', 'default', '用户无活动超时时间（秒），-1表示不限制', 'system', NOW(), 'system', NOW(), '0'),
(302, '活动监控-检查间隔', 'activity.monitoring.check-interval-seconds', '60', 'Y', 'default', '监控检查间隔（秒）', 'system', NOW(), 'system', NOW(), '0'),
(303, '活动监控-API请求监控', 'activity.monitoring.monitor-api-requests', 'true', 'Y', 'default', '是否监控API请求活动（true监控，false不监控）', 'system', NOW(), 'system', NOW(), '0'),
(304, '活动监控-用户交互监控', 'activity.monitoring.monitor-user-interactions', 'true', 'Y', 'default', '是否监控前端用户交互（true监控，false不监控）', 'system', NOW(), 'system', NOW(), '0'),
(305, '活动监控-警告提前时间', 'activity.monitoring.warning-before-timeout-seconds', '300', 'Y', 'default', '活动监控警告提前时间（秒）', 'system', NOW(), 'system', NOW(), '0');
