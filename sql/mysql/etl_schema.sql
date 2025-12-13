-- =============================================
-- Woodlin ETL Module Database Schema
-- 作者: mumu
-- 描述: ETL模块数据库表结构（MySQL版本）
-- 版本: 1.0.0
-- 时间: 2025-01-01
-- =============================================

USE `woodlin`;

-- =============================================
-- ETL任务表
-- =============================================
DROP TABLE IF EXISTS `sys_etl_job`;
CREATE TABLE `sys_etl_job` (
    `job_id` bigint(20) NOT NULL COMMENT '任务ID',
    `job_name` varchar(100) NOT NULL COMMENT '任务名称',
    `job_group` varchar(50) DEFAULT 'DEFAULT' COMMENT '任务组名',
    `job_description` varchar(500) DEFAULT NULL COMMENT '任务描述',
    `source_datasource` varchar(100) NOT NULL COMMENT '源数据源名称',
    `source_table` varchar(100) DEFAULT NULL COMMENT '源表名',
    `source_schema` varchar(100) DEFAULT NULL COMMENT '源Schema名',
    `source_query` text DEFAULT NULL COMMENT '源查询SQL',
    `target_datasource` varchar(100) NOT NULL COMMENT '目标数据源名称',
    `target_table` varchar(100) NOT NULL COMMENT '目标表名',
    `target_schema` varchar(100) DEFAULT NULL COMMENT '目标Schema名',
    `sync_mode` varchar(20) DEFAULT 'FULL' COMMENT '同步模式（FULL-全量，INCREMENTAL-增量）',
    `incremental_column` varchar(100) DEFAULT NULL COMMENT '增量字段',
    `column_mapping` text DEFAULT NULL COMMENT '字段映射配置（JSON格式）',
    `transform_rules` text DEFAULT NULL COMMENT '数据转换规则（JSON格式）',
    `filter_condition` varchar(500) DEFAULT NULL COMMENT '过滤条件',
    `batch_size` int(11) DEFAULT 1000 COMMENT '批处理大小',
    `cron_expression` varchar(100) NOT NULL COMMENT 'cron执行表达式',
    `status` char(1) DEFAULT '0' COMMENT '任务状态（1-启用，0-禁用）',
    `concurrent` char(1) DEFAULT '0' COMMENT '是否并发执行（1-允许，0-禁止）',
    `retry_count` int(11) DEFAULT 3 COMMENT '失败重试次数',
    `retry_interval` int(11) DEFAULT 60 COMMENT '重试间隔（秒）',
    `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
    `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
    `last_execute_status` varchar(20) DEFAULT NULL COMMENT '上次执行状态',
    `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`job_id`),
    KEY `idx_job_name` (`job_name`),
    KEY `idx_job_group` (`job_group`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_next_execute_time` (`next_execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETL任务表';

-- =============================================
-- ETL执行历史表
-- =============================================
DROP TABLE IF EXISTS `sys_etl_execution_log`;
CREATE TABLE `sys_etl_execution_log` (
    `log_id` bigint(20) NOT NULL COMMENT '执行记录ID',
    `job_id` bigint(20) NOT NULL COMMENT '任务ID',
    `job_name` varchar(100) NOT NULL COMMENT '任务名称',
    `execution_status` varchar(20) NOT NULL COMMENT '执行状态（RUNNING-运行中，SUCCESS-成功，FAILED-失败，PARTIAL_SUCCESS-部分成功）',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `duration` bigint(20) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `extracted_rows` bigint(20) DEFAULT 0 COMMENT '提取记录数',
    `transformed_rows` bigint(20) DEFAULT 0 COMMENT '转换记录数',
    `loaded_rows` bigint(20) DEFAULT 0 COMMENT '加载记录数',
    `failed_rows` bigint(20) DEFAULT 0 COMMENT '失败记录数',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `execution_detail` text DEFAULT NULL COMMENT '执行详情（JSON格式）',
    `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`log_id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_execution_status` (`execution_status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETL执行历史表';

-- =============================================
-- 创建索引以优化查询性能
-- =============================================

-- ETL任务表索引
CREATE INDEX idx_etl_job_composite ON sys_etl_job(status, next_execute_time);

-- ETL执行历史表索引
CREATE INDEX idx_etl_log_composite ON sys_etl_execution_log(job_id, start_time DESC);
