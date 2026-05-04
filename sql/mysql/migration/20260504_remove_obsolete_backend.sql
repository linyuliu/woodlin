DROP TABLE IF EXISTS `sys_sensitive_data`;

ALTER TABLE `sys_etl_job`
  DROP COLUMN `column_mapping`;
