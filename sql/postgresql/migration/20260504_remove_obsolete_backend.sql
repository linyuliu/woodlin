DROP TABLE IF EXISTS sys_sensitive_data CASCADE;

ALTER TABLE sys_etl_job
  DROP COLUMN IF EXISTS column_mapping;
