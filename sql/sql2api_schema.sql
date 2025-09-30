-- SQL2API 功能相关表结构

-- ===================================================================
-- 表名：sql2api_config
-- 说明：SQL API配置表，存储动态API的配置信息
-- ===================================================================
CREATE TABLE IF NOT EXISTS `sql2api_config` (
    `api_id` BIGINT NOT NULL COMMENT 'API ID',
    `api_name` VARCHAR(100) NOT NULL COMMENT 'API名称',
    `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径',
    `http_method` VARCHAR(10) NOT NULL DEFAULT 'GET' COMMENT '请求方法(GET,POST,PUT,DELETE)',
    `datasource_name` VARCHAR(50) NOT NULL DEFAULT 'master' COMMENT '数据源名称',
    `sql_type` VARCHAR(20) NOT NULL COMMENT 'SQL类型(SELECT,INSERT,UPDATE,DELETE)',
    `sql_content` TEXT NOT NULL COMMENT 'SQL语句(支持MyBatis动态SQL)',
    `params_config` TEXT COMMENT '参数配置(JSON格式)',
    `result_type` VARCHAR(20) NOT NULL DEFAULT 'list' COMMENT '返回结果类型(single,list,page)',
    `cache_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用缓存',
    `cache_expire` INT DEFAULT 300 COMMENT '缓存过期时间(秒)',
    `encrypt_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用加密',
    `encrypt_algorithm` VARCHAR(20) COMMENT '加密算法(AES,RSA,SM4)',
    `auth_required` TINYINT(1) DEFAULT 1 COMMENT '是否需要认证',
    `auth_type` VARCHAR(20) DEFAULT 'TOKEN' COMMENT '认证类型(TOKEN,API_KEY,NONE)',
    `flow_limit` INT DEFAULT 0 COMMENT '流控配置(QPS限制,0表示不限制)',
    `api_desc` VARCHAR(500) COMMENT 'API描述',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `status` INT DEFAULT 0 COMMENT '状态(0=正常,1=禁用)',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` DATETIME COMMENT '更新时间',
    PRIMARY KEY (`api_id`),
    UNIQUE KEY `uk_api_path` (`api_path`),
    KEY `idx_datasource` (`datasource_name`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL API配置表';

-- ===================================================================
-- 表名：sql2api_orchestration
-- 说明：API编排配置表，支持多API组合调用
-- ===================================================================
CREATE TABLE IF NOT EXISTS `sql2api_orchestration` (
    `orchestration_id` BIGINT NOT NULL COMMENT '编排ID',
    `orchestration_name` VARCHAR(100) NOT NULL COMMENT '编排名称',
    `orchestration_path` VARCHAR(200) NOT NULL COMMENT '编排路径',
    `orchestration_config` TEXT COMMENT '编排配置(JSON格式)',
    `execution_order` TEXT COMMENT '执行顺序配置(JSON数组)',
    `field_mapping` TEXT COMMENT '字段映射配置(JSON格式)',
    `validation_rules` TEXT COMMENT '校验规则配置(JSON格式)',
    `error_strategy` VARCHAR(20) DEFAULT 'STOP' COMMENT '错误处理策略(STOP,CONTINUE,RETRY)',
    `timeout` INT DEFAULT 30000 COMMENT '超时时间(毫秒)',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `orchestration_desc` VARCHAR(500) COMMENT '编排描述',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` DATETIME COMMENT '更新时间',
    PRIMARY KEY (`orchestration_id`),
    UNIQUE KEY `uk_orchestration_path` (`orchestration_path`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API编排配置表';

-- ===================================================================
-- 表名：sql2api_datasource
-- 说明：动态数据源配置表
-- ===================================================================
CREATE TABLE IF NOT EXISTS `sql2api_datasource` (
    `datasource_id` BIGINT NOT NULL COMMENT '数据源ID',
    `datasource_name` VARCHAR(50) NOT NULL COMMENT '数据源名称',
    `database_type` VARCHAR(50) NOT NULL COMMENT '数据库类型(MySQL,PostgreSQL,Oracle,DM8等)',
    `driver_class` VARCHAR(200) NOT NULL COMMENT '驱动类名',
    `jdbc_url` VARCHAR(500) NOT NULL COMMENT 'JDBC连接URL',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(加密存储)',
    `initial_size` INT DEFAULT 5 COMMENT '初始连接数',
    `min_idle` INT DEFAULT 5 COMMENT '最小空闲连接数',
    `max_active` INT DEFAULT 20 COMMENT '最大活动连接数',
    `max_wait` INT DEFAULT 60000 COMMENT '最大等待时间(毫秒)',
    `test_on_borrow` TINYINT(1) DEFAULT 1 COMMENT '获取连接时检测',
    `test_query` VARCHAR(100) DEFAULT 'SELECT 1' COMMENT '测试查询',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `datasource_desc` VARCHAR(500) COMMENT '数据源描述',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` DATETIME COMMENT '更新时间',
    PRIMARY KEY (`datasource_id`),
    UNIQUE KEY `uk_datasource_name` (`datasource_name`),
    KEY `idx_database_type` (`database_type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态数据源配置表';

-- ===================================================================
-- 表名：sql2api_execute_log
-- 说明：API执行日志表
-- ===================================================================
CREATE TABLE IF NOT EXISTS `sql2api_execute_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `api_id` BIGINT COMMENT 'API ID',
    `orchestration_id` BIGINT COMMENT '编排ID',
    `request_path` VARCHAR(200) COMMENT '请求路径',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `execute_status` VARCHAR(20) COMMENT '执行状态(SUCCESS,FAILED,ERROR)',
    `execute_time` BIGINT COMMENT '执行耗时(毫秒)',
    `error_message` TEXT COMMENT '错误信息',
    `client_ip` VARCHAR(50) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `create_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_api_id` (`api_id`),
    KEY `idx_orchestration_id` (`orchestration_id`),
    KEY `idx_execute_status` (`execute_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API执行日志表';
