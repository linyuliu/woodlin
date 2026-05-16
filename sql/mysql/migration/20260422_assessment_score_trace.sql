SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_assessment_result'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_assessment_result' AND COLUMN_NAME = 'score_trace_json'
        ),
        IF(
            EXISTS (
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_assessment_result' AND COLUMN_NAME = 'report_json'
            ),
            'ALTER TABLE `sys_assessment_result` ADD COLUMN `score_trace_json` longtext DEFAULT NULL COMMENT ''计分审计轨迹 JSON'' AFTER `report_json`',
            'ALTER TABLE `sys_assessment_result` ADD COLUMN `score_trace_json` longtext DEFAULT NULL COMMENT ''计分审计轨迹 JSON'''
        ),
        'SELECT ''skip sys_assessment_result.score_trace_json'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
