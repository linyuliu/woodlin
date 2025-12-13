-- =============================================
-- ETL模块示例数据
-- 作者: mumu
-- 描述: ETL任务配置示例数据
-- 版本: 1.0.0
-- =============================================

USE `woodlin`;

-- =============================================
-- 示例1: 全量数据同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`, 
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`, 
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `create_by`, `create_time`
) VALUES (
    1, '用户数据全量同步', 'DATA_SYNC', '每天凌晨2点全量同步用户数据到备份库',
    'source_mysql', 'users',
    'target_mysql', 'users_backup',
    'FULL', 1000, '0 0 2 * * ?',
    '0', '0', 3, 60,
    'system', NOW()
);

-- =============================================
-- 示例2: 增量数据同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`, `incremental_column`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `create_by`, `create_time`
) VALUES (
    2, '订单增量同步', 'ORDER_SYNC', '每30分钟同步新增和修改的订单数据',
    'source_mysql', 'orders', 'update_time',
    'target_mysql', 'orders_mirror',
    'INCREMENTAL', 2000, '0 */30 * * * ?',
    '0', '0', 3, 60,
    'system', NOW()
);

-- =============================================
-- 示例3: 自定义SQL查询同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, 
    `source_query`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `create_by`, `create_time`
) VALUES (
    3, '活跃用户统计', 'ANALYTICS', '每天凌晨3点提取活跃用户数据进行分析',
    'source_mysql',
    'SELECT user_id, username, email, login_count, last_login_time, DATE_FORMAT(create_time, "%Y-%m") as reg_month FROM users WHERE status = "1" AND login_count > 10 AND last_login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)',
    'target_mysql', 'active_users_stats',
    'FULL', 1000, '0 0 3 * * ?',
    '0', '0', 3, 60,
    'system', NOW()
);

-- =============================================
-- 示例4: 带过滤条件的同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`, `filter_condition`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `create_by`, `create_time`
) VALUES (
    4, 'VIP用户同步', 'VIP_SYNC', '每天凌晨4点同步VIP会员数据',
    'source_mysql', 'users', 'vip_level > 0 AND expire_time > NOW() AND status = "1"',
    'target_mysql', 'vip_users',
    'FULL', 500, '0 0 4 * * ?',
    '0', '0', 3, 60,
    'system', NOW()
);

-- =============================================
-- 示例5: 跨数据库类型同步（MySQL to PostgreSQL）
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`,
    `target_datasource`, `target_schema`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `create_by`, `create_time`
) VALUES (
    5, '产品数据跨库同步', 'CROSS_DB', 'MySQL到PostgreSQL的产品数据同步',
    'source_mysql', 'products',
    'target_pg', 'public', 'products',
    'FULL', 1000, '0 0 1 * * ?',
    '0', '0', 3, 60,
    'system', NOW()
);

-- =============================================
-- 示例6: 大表分批同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `remark`,
    `create_by`, `create_time`
) VALUES (
    6, '日志数据归档', 'ARCHIVE', '每周一凌晨归档日志数据',
    'source_mysql', 'system_logs',
    'target_mysql', 'system_logs_archive',
    'FULL', 10000, '0 0 0 * * MON',
    '0', '0', 2, 120,
    '大表同步，使用较大的批处理大小以提高性能',
    'system', NOW()
);

-- =============================================
-- 示例7: 准实时同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`, `source_table`, `incremental_column`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `remark`,
    `create_by`, `create_time`
) VALUES (
    7, '库存实时同步', 'REALTIME', '每5分钟同步库存变化',
    'source_mysql', 'inventory', 'update_time',
    'target_mysql', 'inventory_mirror',
    'INCREMENTAL', 500, '0 */5 * * * ?',
    '0', '0', 5, 30,
    '高频率同步，注意性能影响',
    'system', NOW()
);

-- =============================================
-- 示例8: 数据聚合同步
-- =============================================
INSERT INTO `sys_etl_job` (
    `job_id`, `job_name`, `job_group`, `job_description`,
    `source_datasource`,
    `source_query`,
    `target_datasource`, `target_table`,
    `sync_mode`, `batch_size`, `cron_expression`,
    `status`, `concurrent`, `retry_count`, `retry_interval`,
    `remark`,
    `create_by`, `create_time`
) VALUES (
    8, '每日销售统计', 'REPORT', '每天凌晨生成销售统计报表',
    'source_mysql',
    'SELECT DATE(order_time) as order_date, COUNT(*) as order_count, SUM(total_amount) as total_sales, AVG(total_amount) as avg_order_value FROM orders WHERE order_time >= DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND order_time < CURDATE() AND status = "completed" GROUP BY DATE(order_time)',
    'target_mysql', 'daily_sales_report',
    'FULL', 100, '0 0 5 * * ?',
    '0', '0', 3, 60,
    '聚合查询，数据量较小',
    'system', NOW()
);

-- =============================================
-- 查询示例数据
-- =============================================

-- 查看所有ETL任务
SELECT 
    job_id,
    job_name,
    job_group,
    source_datasource,
    source_table,
    target_datasource,
    target_table,
    sync_mode,
    cron_expression,
    status
FROM sys_etl_job
ORDER BY job_id;

-- 查看启用的ETL任务
SELECT * FROM sys_etl_job WHERE status = '1';

-- 查看各组的任务数量
SELECT 
    job_group,
    COUNT(*) as job_count,
    SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) as enabled_count
FROM sys_etl_job
GROUP BY job_group;
