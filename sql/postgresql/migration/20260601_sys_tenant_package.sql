-- =============================================================
-- Woodlin PostgreSQL Migration
-- Name: 20260601_sys_tenant_package
-- Desc: 创建租户套餐表 sys_tenant_package
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================

DROP TABLE IF EXISTS sys_tenant_package;
CREATE TABLE sys_tenant_package
(
    package_id   bigint        NOT NULL,
    package_name varchar(100)  NOT NULL,
    menu_ids     text          DEFAULT NULL,
    status       char(1)       DEFAULT '1',
    remark       varchar(255)  DEFAULT NULL,
    create_by    varchar(64)   DEFAULT NULL,
    create_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
    update_by    varchar(64)   DEFAULT NULL,
    update_time  timestamp     DEFAULT CURRENT_TIMESTAMP,
    deleted      char(1)       DEFAULT '0',
    PRIMARY KEY (package_id)
);

CREATE INDEX idx_sys_tenant_package_name ON sys_tenant_package (package_name);
CREATE INDEX idx_sys_tenant_package_status ON sys_tenant_package (status);

COMMENT ON TABLE sys_tenant_package IS '租户套餐表';
COMMENT ON COLUMN sys_tenant_package.package_id IS '套餐ID';
COMMENT ON COLUMN sys_tenant_package.package_name IS '套餐名称';
COMMENT ON COLUMN sys_tenant_package.menu_ids IS '关联菜单ID集合（逗号分隔）';
COMMENT ON COLUMN sys_tenant_package.status IS '套餐状态（1-启用，0-禁用）';
COMMENT ON COLUMN sys_tenant_package.remark IS '备注';
