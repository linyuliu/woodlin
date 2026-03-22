ALTER TABLE `sys_etl_column_mapping_rule`
    ADD COLUMN `transform_params` text DEFAULT NULL COMMENT '转换参数(JSON)' AFTER `mapping_action`,
    ADD COLUMN `constant_value` varchar(500) DEFAULT NULL COMMENT '常量值' AFTER `transform_params`,
    ADD COLUMN `default_value` varchar(500) DEFAULT NULL COMMENT '默认值' AFTER `constant_value`,
    ADD COLUMN `empty_value_policy` varchar(50) DEFAULT NULL COMMENT '空值处理策略' AFTER `default_value`;
