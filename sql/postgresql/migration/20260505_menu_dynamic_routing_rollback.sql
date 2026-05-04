-- Rollback: Remove dynamic routing fields from sys_permission
-- Date: 2026-05-05

ALTER TABLE sys_permission
    DROP COLUMN IF EXISTS show_in_tabs,
    DROP COLUMN IF EXISTS active_menu,
    DROP COLUMN IF EXISTS redirect;
