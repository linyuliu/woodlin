-- =============================================================
-- Woodlin MySQL Migration Rollback
-- Name: 20260601_sys_tenant_package_rollback
-- Desc: 回滚 sys_tenant_package 表
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_tenant_package`;
