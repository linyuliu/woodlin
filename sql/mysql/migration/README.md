# MySQL Migration Notes

## 20260315\_rbac\_super\_admin\_and\_datasource\_permission

- 正向脚本：`20260315_rbac_super_admin_and_datasource_permission.sql`
- 回滚脚本：`20260315_rbac_super_admin_and_datasource_permission_rollback.sql`

### 作用

1. 备份受影响的 RBAC 数据。
2. 统一 `role_id=1` 的角色编码为 `admin`。
3. 补齐数据源按钮/API权限：
   - `datasource:add`
   - `datasource:edit`
   - `datasource:remove`
   - `datasource:test`
   - `datasource:metadata`
4. 为超级管理员补齐全量权限并刷新继承权限缓存。

### 执行建议

1. 先在测试库执行并校验。
2. 生产执行前确认备份表存在并已写入数据。
3. 如需回退，执行对应 rollback 脚本。

## 20260315_datasource_menu_simplify

- 正向脚本：`20260315_datasource_menu_simplify.sql`
- 回滚脚本：`20260315_datasource_menu_simplify_rollback.sql`

### 作用

1. 下线“数据源监控”菜单（`permission_id=2002`）。
2. 数据源按钮 `2101-2105` 统一挂载到“数据源列表”菜单（`parent_id=2001`）。
3. 清理监控菜单在角色授权与继承缓存中的残留关联。
