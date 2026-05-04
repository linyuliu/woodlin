-- Rollback: Remove dynamic routing fields from sys_permission
-- Date: 2026-05-05

ALTER TABLE `sys_permission`
    DROP COLUMN `show_in_tabs`,
    DROP COLUMN `active_menu`,
    DROP COLUMN `redirect`;
