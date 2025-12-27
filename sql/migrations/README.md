# Database Migration Scripts

This directory contains SQL migration scripts for updating existing Woodlin installations.

## Migration Files

### fix_component_paths.sql
**Date**: 2025-12-27  
**Issue**: Fixes 404 routing errors after login  
**Description**: Updates component paths in sys_permission table to match actual Vue component file structure

**When to use**:
- If you have an existing Woodlin installation
- If users are experiencing 404 errors after login
- If you're upgrading from an older version with incorrect component paths

**How to use**:

For MySQL:
```bash
mysql -u username -p database_name < fix_component_paths.sql
```

For PostgreSQL:
```bash
psql -U username -d database_name -f fix_component_paths.sql
```

For Oracle:
```bash
sqlplus username/password@database @fix_component_paths.sql
```

Or execute manually in your database client:
```sql
UPDATE sys_permission SET component = 'system/UserView' WHERE component = 'system/user/index';
UPDATE sys_permission SET component = 'system/RoleView' WHERE component = 'system/role/index';
UPDATE sys_permission SET component = 'system/permission/PermissionManagementView' WHERE component = 'system/menu/index';
UPDATE sys_permission SET component = 'system/DeptView' WHERE component = 'system/dept/index';
```

**Verification**:
```sql
SELECT permission_id, permission_name, component 
FROM sys_permission 
WHERE permission_type = 'C' AND parent_id = 1;
```

Expected results should show:
- `system/UserView`
- `system/RoleView`
- `system/permission/PermissionManagementView`
- `system/DeptView`

## Notes

1. **Backup First**: Always backup your database before running migration scripts
2. **Test Environment**: Test migrations in a development/staging environment first
3. **Clear Cache**: After migration, users should clear browser cache and localStorage
4. **Re-login Required**: Users need to log out and log back in for changes to take effect

## Getting Help

For more details, see:
- `docs/fixes/routing-404-fix.md` - Comprehensive documentation (Chinese)
- GitHub Issues - Report problems or ask questions
