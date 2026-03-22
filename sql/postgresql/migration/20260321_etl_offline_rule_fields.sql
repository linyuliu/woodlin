ALTER TABLE sys_etl_column_mapping_rule
    ADD COLUMN transform_params text DEFAULT NULL,
    ADD COLUMN constant_value varchar(500) DEFAULT NULL,
    ADD COLUMN default_value varchar(500) DEFAULT NULL,
    ADD COLUMN empty_value_policy varchar(50) DEFAULT NULL;
