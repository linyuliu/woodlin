# 菜单权限修复说明

## 问题描述

数据库初始化SQL中只包含了"系统管理"模块的菜单权限，缺少以下模块：
- 仪表板
- 数据源管理
- 租户管理
- 文件管理
- 任务管理
- 开发工具

这导致用户登录后只能看到"系统管理"一个菜单，无法访问其他功能模块。

## 解决方案

已更新 `sql/mysql/woodlin_complete_data.sql` 文件，添加了所有缺失的菜单权限。

### 新增的菜单权限

1. **仪表板** (ID: 1000)
   - 仪表板页面

2. **数据源管理** (ID: 2000-2002)
   - 数据源管理目录
   - 数据源列表
   - 数据源监控

3. **租户管理** (ID: 3000-3001)
   - 租户管理目录
   - 租户列表

4. **文件管理** (ID: 4000-4002)
   - 文件管理目录
   - 文件列表
   - 存储配置

5. **任务管理** (ID: 5000-5002)
   - 任务管理目录
   - 任务列表
   - 任务日志

6. **开发工具** (ID: 6000-6002)
   - 开发工具目录
   - SQL转API
   - 代码生成

7. **系统管理补充** (ID: 6-8)
   - 字典管理
   - 配置管理
   - 系统设置

## 应用方法

### 方式一：全新安装（推荐）

如果是第一次部署，直接使用更新后的SQL文件：

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS woodlin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行架构脚本
mysql -uroot -p woodlin < sql/mysql/woodlin_complete_schema.sql

# 3. 执行数据脚本（包含所有菜单权限）
mysql -uroot -p woodlin < sql/mysql/woodlin_complete_data.sql

# 4. 验证权限数量
mysql -uroot -p woodlin -e "SELECT permission_type, COUNT(*) as count FROM sys_permission WHERE deleted='0' GROUP BY permission_type;"
```

预期输出：
```
+-----------------+-------+
| permission_type | count |
+-----------------+-------+
| C               |    17 | (菜单)
| M               |     6 | (目录)
| F               |    20 | (按钮)
+-----------------+-------+
```

### 方式二：已有数据库（使用补丁）

如果数据库已经存在，可以使用补丁脚本：

```bash
# 执行补丁脚本
mysql -uroot -p woodlin < sql/mysql/patch_add_missing_menus.sql
```

**注意**：补丁脚本会：
1. 删除ID > 500的旧权限（如果有）
2. 添加所有缺失的菜单权限
3. 更新超级管理员和普通角色的权限映射
4. 重建权限继承缓存

### 方式三：Docker Compose自动初始化

使用Docker Compose时，数据库会自动初始化：

```bash
# 1. 清理旧数据（如果需要）
docker compose down -v mysql

# 2. 启动服务
docker compose up -d mysql

# 3. 等待初始化完成（约30-60秒）
docker compose logs -f mysql | grep "ready for connections"

# 4. 验证
docker compose exec mysql mysql -uroot -p123456 woodlin -e "SELECT COUNT(*) FROM sys_permission WHERE deleted='0';"
```

## 验证步骤

### 1. 检查数据库权限

```sql
-- 查看所有顶级菜单
SELECT 
    permission_id,
    permission_name,
    permission_code,
    permission_type,
    icon,
    sort_order
FROM sys_permission
WHERE parent_id = 0 AND deleted = '0'
ORDER BY sort_order;
```

预期输出应包含：
- 系统管理 (system)
- 仪表板 (dashboard)
- 数据源管理 (datasource)
- 租户管理 (tenant)
- 文件管理 (file)
- 任务管理 (task)
- 开发工具 (dev)

### 2. 检查管理员权限

```sql
-- 查看admin用户的菜单权限数量
SELECT 
    COUNT(*) as menu_count
FROM sys_user_role ur
INNER JOIN sys_role_permission rp ON ur.role_id = rp.role_id
INNER JOIN sys_permission p ON rp.permission_id = p.permission_id
WHERE ur.user_id = 1 
  AND p.permission_type IN ('M', 'C')
  AND p.deleted = '0';
```

预期结果：menu_count应该大于20（包含所有目录和菜单）

### 3. 测试登录

1. 使用 `admin` / `Passw0rd` 登录系统
2. 检查左侧菜单栏
3. 应该能看到所有7个顶级菜单：
   - ✅ 仪表板
   - ✅ 系统管理（展开后有8个子菜单）
   - ✅ 数据源管理（展开后有2个子菜单）
   - ✅ 租户管理（展开后有1个子菜单）
   - ✅ 文件管理（展开后有2个子菜单）
   - ✅ 任务管理（展开后有2个子菜单）
   - ✅ 开发工具（展开后有2个子菜单）

## 常见问题

### Q1: 执行补丁后仍然只有"系统管理"菜单？

**原因**：可能是缓存问题

**解决方案**：
1. 清除浏览器缓存和Local Storage
2. 重新登录
3. 或者重启后端应用清除Redis缓存

### Q2: 部分菜单显示但无法访问？

**原因**：前端路由组件路径不匹配

**解决方案**：
检查 `woodlin-web/src/router/routes.ts` 中的路由配置，确保：
- `path` 和数据库中的 `path` 字段一致
- `component` 和数据库中的 `component` 字段一致
- 组件文件确实存在

### Q3: 新部署后仍然缺少菜单？

**原因**：使用了旧的SQL文件

**解决方案**：
1. 确认使用的是更新后的 `woodlin_complete_data.sql`
2. 或者执行补丁脚本 `patch_add_missing_menus.sql`
3. 重新初始化数据库

### Q4: Docker Compose启动后菜单不全？

**原因**：数据卷中有旧数据

**解决方案**：
```bash
# 删除MySQL数据卷
docker compose down -v mysql

# 重新启动（会自动重新初始化）
docker compose up -d mysql

# 等待初始化完成
sleep 60

# 验证
docker compose exec mysql mysql -uroot -p123456 woodlin -e "SELECT COUNT(*) FROM sys_permission;"
```

## 技术说明

### 权限ID分配规则

- **1-99**: 系统管理顶级菜单和子菜单
- **100-499**: 系统管理按钮权限
- **1000-1999**: 仪表板相关权限
- **2000-2999**: 数据源管理相关权限
- **3000-3999**: 租户管理相关权限
- **4000-4999**: 文件管理相关权限
- **5000-5999**: 任务管理相关权限
- **6000-6999**: 开发工具相关权限

### 权限类型说明

- **M (Module)**: 目录，用于组织菜单结构，本身不对应页面
- **C (Component)**: 菜单，对应具体的页面组件
- **F (Function)**: 按钮，对应页面内的操作权限

### RBAC1 权限继承

系统使用RBAC1（角色继承）模型：
- `sys_role_permission`: 存储角色的直接权限
- `sys_role_inherited_permission`: 存储角色的所有权限（包括继承的）
- `sys_role_hierarchy`: 存储角色继承关系

补丁脚本会自动重建权限继承缓存。

## 相关文件

- `sql/mysql/woodlin_complete_schema.sql` - 数据库架构（表结构）
- `sql/mysql/woodlin_complete_data.sql` - 数据库初始数据（已更新）
- `sql/mysql/patch_add_missing_menus.sql` - 菜单权限补丁脚本
- `woodlin-web/src/router/routes.ts` - 前端路由定义

## 联系支持

如果遇到问题，请提供以下信息：

1. 数据库权限数量：
```sql
SELECT permission_type, COUNT(*) FROM sys_permission WHERE deleted='0' GROUP BY permission_type;
```

2. 管理员可见菜单：
```sql
SELECT p.permission_name, p.permission_code, p.permission_type 
FROM sys_user_role ur
INNER JOIN sys_role_permission rp ON ur.role_id = rp.role_id
INNER JOIN sys_permission p ON rp.permission_id = p.permission_id
WHERE ur.user_id = 1 AND p.permission_type IN ('M', 'C') AND p.deleted = '0'
ORDER BY p.sort_order;
```

3. 浏览器控制台错误日志
4. 后端日志相关错误

---

最后更新：2025-12-27
作者：mumu
