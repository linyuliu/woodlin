SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule' AND COLUMN_NAME = 'transform_params'
        ),
        'ALTER TABLE `sys_etl_column_mapping_rule` ADD COLUMN `transform_params` text DEFAULT NULL COMMENT ''转换参数(JSON)'' AFTER `mapping_action`',
        'SELECT ''skip sys_etl_column_mapping_rule.transform_params'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule' AND COLUMN_NAME = 'constant_value'
        ),
        'ALTER TABLE `sys_etl_column_mapping_rule` ADD COLUMN `constant_value` varchar(500) DEFAULT NULL COMMENT ''常量值'' AFTER `transform_params`',
        'SELECT ''skip sys_etl_column_mapping_rule.constant_value'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule' AND COLUMN_NAME = 'default_value'
        ),
        'ALTER TABLE `sys_etl_column_mapping_rule` ADD COLUMN `default_value` varchar(500) DEFAULT NULL COMMENT ''默认值'' AFTER `constant_value`',
        'SELECT ''skip sys_etl_column_mapping_rule.default_value'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_column_mapping_rule' AND COLUMN_NAME = 'empty_value_policy'
        ),
        'ALTER TABLE `sys_etl_column_mapping_rule` ADD COLUMN `empty_value_policy` varchar(50) DEFAULT NULL COMMENT ''空值处理策略'' AFTER `default_value`',
        'SELECT ''skip sys_etl_column_mapping_rule.empty_value_policy'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
