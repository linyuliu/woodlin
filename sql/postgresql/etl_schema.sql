-- =============================================
-- Woodlin ETL Module Database Schema
-- 作者: mumu
-- 描述: ETL模块数据库表结构（PostgreSQL版本）
-- 版本: 1.0.0
-- 时间: 2025-01-01
-- =============================================

-- =============================================
-- ETL任务表
-- =============================================
DROP TABLE IF EXISTS sys_etl_job CASCADE;
CREATE TABLE sys_etl_job (
    job_id BIGINT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_group VARCHAR(50) DEFAULT 'DEFAULT',
    job_description VARCHAR(500),
    source_datasource VARCHAR(100) NOT NULL,
    source_table VARCHAR(100),
    source_schema VARCHAR(100),
    source_query TEXT,
    target_datasource VARCHAR(100) NOT NULL,
    target_table VARCHAR(100) NOT NULL,
    target_schema VARCHAR(100),
    sync_mode VARCHAR(20) DEFAULT 'FULL',
    incremental_column VARCHAR(100),
    column_mapping TEXT,
    transform_rules TEXT,
    filter_condition VARCHAR(500),
    batch_size INTEGER DEFAULT 1000,
    cron_expression VARCHAR(100) NOT NULL,
    status CHAR(1) DEFAULT '0',
    concurrent CHAR(1) DEFAULT '0',
    retry_count INTEGER DEFAULT 3,
    retry_interval INTEGER DEFAULT 60,
    next_execute_time TIMESTAMP,
    last_execute_time TIMESTAMP,
    last_execute_status VARCHAR(20),
    tenant_id VARCHAR(64),
    remark VARCHAR(500),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE sys_etl_job IS 'ETL任务表';
COMMENT ON COLUMN sys_etl_job.job_id IS '任务ID';
COMMENT ON COLUMN sys_etl_job.job_name IS '任务名称';
COMMENT ON COLUMN sys_etl_job.job_group IS '任务组名';
COMMENT ON COLUMN sys_etl_job.job_description IS '任务描述';
COMMENT ON COLUMN sys_etl_job.source_datasource IS '源数据源名称';
COMMENT ON COLUMN sys_etl_job.source_table IS '源表名';
COMMENT ON COLUMN sys_etl_job.source_schema IS '源Schema名';
COMMENT ON COLUMN sys_etl_job.source_query IS '源查询SQL';
COMMENT ON COLUMN sys_etl_job.target_datasource IS '目标数据源名称';
COMMENT ON COLUMN sys_etl_job.target_table IS '目标表名';
COMMENT ON COLUMN sys_etl_job.target_schema IS '目标Schema名';
COMMENT ON COLUMN sys_etl_job.sync_mode IS '同步模式（FULL-全量，INCREMENTAL-增量）';
COMMENT ON COLUMN sys_etl_job.incremental_column IS '增量字段';
COMMENT ON COLUMN sys_etl_job.column_mapping IS '字段映射配置（JSON格式）';
COMMENT ON COLUMN sys_etl_job.transform_rules IS '数据转换规则（JSON格式）';
COMMENT ON COLUMN sys_etl_job.filter_condition IS '过滤条件';
COMMENT ON COLUMN sys_etl_job.batch_size IS '批处理大小';
COMMENT ON COLUMN sys_etl_job.cron_expression IS 'cron执行表达式';
COMMENT ON COLUMN sys_etl_job.status IS '任务状态（1-启用，0-禁用）';
COMMENT ON COLUMN sys_etl_job.concurrent IS '是否并发执行（1-允许，0-禁止）';
COMMENT ON COLUMN sys_etl_job.retry_count IS '失败重试次数';
COMMENT ON COLUMN sys_etl_job.retry_interval IS '重试间隔（秒）';
COMMENT ON COLUMN sys_etl_job.next_execute_time IS '下次执行时间';
COMMENT ON COLUMN sys_etl_job.last_execute_time IS '上次执行时间';
COMMENT ON COLUMN sys_etl_job.last_execute_status IS '上次执行状态';
COMMENT ON COLUMN sys_etl_job.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_etl_job.remark IS '备注';
COMMENT ON COLUMN sys_etl_job.create_by IS '创建者';
COMMENT ON COLUMN sys_etl_job.create_time IS '创建时间';
COMMENT ON COLUMN sys_etl_job.update_by IS '更新者';
COMMENT ON COLUMN sys_etl_job.update_time IS '更新时间';
COMMENT ON COLUMN sys_etl_job.deleted IS '删除标识（0-正常，1-删除）';

-- =============================================
-- ETL执行历史表
-- =============================================
DROP TABLE IF EXISTS sys_etl_execution_log CASCADE;
CREATE TABLE sys_etl_execution_log (
    log_id BIGINT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(100) NOT NULL,
    execution_status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT,
    extracted_rows BIGINT DEFAULT 0,
    transformed_rows BIGINT DEFAULT 0,
    loaded_rows BIGINT DEFAULT 0,
    failed_rows BIGINT DEFAULT 0,
    error_message TEXT,
    execution_detail TEXT,
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE sys_etl_execution_log IS 'ETL执行历史表';
COMMENT ON COLUMN sys_etl_execution_log.log_id IS '执行记录ID';
COMMENT ON COLUMN sys_etl_execution_log.job_id IS '任务ID';
COMMENT ON COLUMN sys_etl_execution_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_etl_execution_log.execution_status IS '执行状态（RUNNING-运行中，SUCCESS-成功，FAILED-失败，PARTIAL_SUCCESS-部分成功）';
COMMENT ON COLUMN sys_etl_execution_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_etl_execution_log.end_time IS '结束时间';
COMMENT ON COLUMN sys_etl_execution_log.duration IS '执行耗时（毫秒）';
COMMENT ON COLUMN sys_etl_execution_log.extracted_rows IS '提取记录数';
COMMENT ON COLUMN sys_etl_execution_log.transformed_rows IS '转换记录数';
COMMENT ON COLUMN sys_etl_execution_log.loaded_rows IS '加载记录数';
COMMENT ON COLUMN sys_etl_execution_log.failed_rows IS '失败记录数';
COMMENT ON COLUMN sys_etl_execution_log.error_message IS '错误信息';
COMMENT ON COLUMN sys_etl_execution_log.execution_detail IS '执行详情（JSON格式）';
COMMENT ON COLUMN sys_etl_execution_log.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_etl_execution_log.create_by IS '创建者';
COMMENT ON COLUMN sys_etl_execution_log.create_time IS '创建时间';
COMMENT ON COLUMN sys_etl_execution_log.update_by IS '更新者';
COMMENT ON COLUMN sys_etl_execution_log.update_time IS '更新时间';
COMMENT ON COLUMN sys_etl_execution_log.deleted IS '删除标识（0-正常，1-删除）';

-- =============================================
-- 创建索引以优化查询性能
-- =============================================

-- ETL任务表索引
CREATE INDEX idx_etl_job_name ON sys_etl_job(job_name);
CREATE INDEX idx_etl_job_group ON sys_etl_job(job_group);
CREATE INDEX idx_etl_job_status ON sys_etl_job(status);
CREATE INDEX idx_etl_job_tenant_id ON sys_etl_job(tenant_id);
CREATE INDEX idx_etl_job_next_execute_time ON sys_etl_job(next_execute_time);
CREATE INDEX idx_etl_job_composite ON sys_etl_job(status, next_execute_time);

-- ETL执行历史表索引
CREATE INDEX idx_etl_log_job_id ON sys_etl_execution_log(job_id);
CREATE INDEX idx_etl_log_execution_status ON sys_etl_execution_log(execution_status);
CREATE INDEX idx_etl_log_start_time ON sys_etl_execution_log(start_time);
CREATE INDEX idx_etl_log_tenant_id ON sys_etl_execution_log(tenant_id);
CREATE INDEX idx_etl_log_composite ON sys_etl_execution_log(job_id, start_time DESC);
