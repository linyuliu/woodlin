-- =============================================================
-- Woodlin MySQL Migration Rollback
-- Name: 20260601_sys_notice_rollback
-- Desc: 回滚 sys_notice 表
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================
USE `woodlin`;

DROP TABLE IF EXISTS `sys_notice`;
