DROP TABLE IF EXISTS `sys_sensitive_data`;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_job'
        )
        AND EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_etl_job' AND COLUMN_NAME = 'column_mapping'
        ),
        'ALTER TABLE `sys_etl_job` DROP COLUMN `column_mapping`',
        'SELECT ''skip sys_etl_job.column_mapping'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
