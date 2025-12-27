-- =============================================
-- Migration: Fix Component Paths for Dynamic Routing
-- Date: 2025-12-27
-- Description: Updates component paths in sys_permission table to match actual Vue component file structure
-- Issue: Users were getting 404 errors after login because component paths didn't match actual file locations
-- =============================================

-- For MySQL
UPDATE sys_permission SET component = 'system/UserView' WHERE component = 'system/user/index';
UPDATE sys_permission SET component = 'system/RoleView' WHERE component = 'system/role/index';
UPDATE sys_permission SET component = 'system/permission/PermissionManagementView' WHERE component = 'system/menu/index';
UPDATE sys_permission SET component = 'system/DeptView' WHERE component = 'system/dept/index';

-- Note: If you're using PostgreSQL or Oracle, the same UPDATE statements apply
-- Just make sure to run them in your respective database environment

-- To verify the changes:
-- SELECT permission_id, permission_name, component FROM sys_permission WHERE permission_type = 'C' AND parent_id = 1;
