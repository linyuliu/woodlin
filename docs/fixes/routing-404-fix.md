# 路由404问题修复说明

## 问题描述

用户登录后，除了主页面外，其他所有路由都返回404错误。

## 问题原因

数据库 `sys_permission` 表中存储的组件路径与实际Vue组件文件结构不匹配：

| 数据库中的路径 | 实际文件路径 |
|--------------|-------------|
| `system/user/index` | `system/UserView.vue` |
| `system/role/index` | `system/RoleView.vue` |
| `system/menu/index` | `system/permission/PermissionManagementView.vue` |
| `system/dept/index` | `system/DeptView.vue` |

当用户登录后，后端从数据库返回这些错误的组件路径，前端无法找到对应的Vue组件文件，导致所有路由显示404页面。

## 解决方案

### 对于新安装

使用更新后的SQL脚本初始化数据库：
- `sql/mysql/woodlin_complete_data.sql`
- `sql/postgresql/woodlin_data.sql`
- `sql/oracle/woodlin_data.sql`

### 对于已有数据库

运行迁移脚本更新现有数据：

```sql
-- 执行迁移脚本
source sql/migrations/fix_component_paths.sql;
```

或者手动执行以下SQL语句：

```sql
UPDATE sys_permission SET component = 'system/UserView' WHERE component = 'system/user/index';
UPDATE sys_permission SET component = 'system/RoleView' WHERE component = 'system/role/index';
UPDATE sys_permission SET component = 'system/permission/PermissionManagementView' WHERE component = 'system/menu/index';
UPDATE sys_permission SET component = 'system/DeptView' WHERE component = 'system/dept/index';
```

## 验证修复

1. 清除浏览器缓存和本地存储
2. 重新登录系统
3. 尝试访问各个菜单项（用户管理、角色管理、菜单管理、部门管理）
4. 确认所有页面都能正常加载，不再显示404错误

## 技术细节

### 动态路由加载流程

1. 用户登录成功
2. 前端调用 `/auth/routes` API获取用户的路由菜单
3. 后端从 `sys_permission` 表查询用户权限对应的路由
4. 前端 `usePermissionStore` 接收路由数据
5. `loadComponent()` 函数根据 `component` 字段动态加载Vue组件
6. 路由守卫将动态路由注入到Vue Router

### 组件路径规则

前端使用 `import.meta.glob('@/views/**/*.vue')` 预加载所有组件，查找规则：

```javascript
const componentKey = `/src/views/${path}${path.endsWith('.vue') ? '' : '.vue'}`
```

因此数据库中的 `component` 字段应该是相对于 `src/views/` 的路径，例如：
- `system/UserView` → `/src/views/system/UserView.vue`
- `system/permission/PermissionManagementView` → `/src/views/system/permission/PermissionManagementView.vue`

## 预防措施

1. 新增菜单时，确保 `component` 字段与实际文件路径一致
2. 创建新组件后，更新相应的数据库记录
3. 建议在开发环境中启用详细日志，及时发现组件加载问题

## 相关文件

- 前端路由配置: `woodlin-web/src/router/routes.ts`
- 权限Store: `woodlin-web/src/stores/permission.ts`
- 路由守卫: `woodlin-web/src/router/guards.ts`
- 后端路由服务: `woodlin-system/woodlin-system-core/src/main/java/com/mumu/woodlin/system/service/impl/SysPermissionServiceImpl.java`
