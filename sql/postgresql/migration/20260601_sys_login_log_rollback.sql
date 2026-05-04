-- =============================================================
-- Woodlin PostgreSQL Migration Rollback
-- Name: 20260601_sys_login_log_rollback
-- Desc: 回滚 sys_login_log 表
-- Author: yulin
-- Date: 2026-06-01
-- =============================================================

DROP TABLE IF EXISTS sys_login_log;
