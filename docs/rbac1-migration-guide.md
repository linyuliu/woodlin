# RBAC1 迁移指南

本文档介绍如何将 Woodlin 系统从 RBAC0 升级到 RBAC1（角色继承）。

## 概述

### RBAC0 vs RBAC1

**RBAC0（基础角色访问控制）:**
- 用户直接分配角色
- 角色直接关联权限
- 扁平的角色结构
- 无角色继承

**RBAC1（角色继承）:**
- 支持角色层次结构
- 子角色自动继承父角色的权限
- 多层级角色关系（最多10层）
- 灵活的权限管理

### 升级优势

1. **减少权限配置工作量**: 通过继承，子角色自动获得父角色的权限
2. **更清晰的权限结构**: 角色层次反映组织架构
3. **灵活的权限扩展**: 可以在父角色基础上扩展权限
4. **向后兼容**: 不设置父角色时，行为与 RBAC0 完全一致

## 迁移步骤

### 1. 数据库升级

执行 SQL 迁移脚本：

```bash
# 连接到数据库
mysql -u root -p woodlin

# 执行迁移脚本
source sql/rbac1_upgrade.sql
```

迁移脚本会：
- 为 `sys_role` 表添加新字段（`parent_role_id`, `role_level`, `role_path`, `is_inheritable`）
- 创建 `sys_role_hierarchy` 层次关系表
- 创建 `sys_role_inherited_permission` 权限缓存表
- 初始化现有角色的层次数据
- 创建管理存储过程

### 2. 验证升级

检查表结构：

```sql
-- 检查 sys_role 表新增字段
DESC sys_role;

-- 检查层次关系表
DESC sys_role_hierarchy;

-- 检查权限缓存表
DESC sys_role_inherited_permission;

-- 查看所有角色的初始状态
SELECT role_id, role_name, parent_role_id, role_level, role_path 
FROM sys_role 
WHERE deleted = '0';
```

### 3. 应用代码部署

升级到包含 RBAC1 支持的新版本：

```bash
# 停止应用
./scripts/dev.sh stop

# 拉取最新代码
git pull origin main

# 构建应用
mvn clean package -DskipTests

# 启动应用
./scripts/dev.sh
```

### 4. 数据迁移（可选）

如果要为现有角色设置层次结构：

```sql
-- 示例：设置角色继承关系
-- 假设有：超级管理员 -> 部门管理员 -> 普通员工

-- 1. 设置部门管理员的父角色为超级管理员
UPDATE sys_role 
SET parent_role_id = (SELECT role_id FROM sys_role WHERE role_code = 'super_admin' LIMIT 1)
WHERE role_code = 'dept_admin';

-- 2. 设置普通员工的父角色为部门管理员  
UPDATE sys_role
SET parent_role_id = (SELECT role_id FROM sys_role WHERE role_code = 'dept_admin' LIMIT 1)
WHERE role_code = 'normal_user';

-- 3. 刷新角色层次关系
CALL refresh_role_hierarchy((SELECT role_id FROM sys_role WHERE role_code = 'dept_admin' LIMIT 1));
CALL refresh_role_hierarchy((SELECT role_id FROM sys_role WHERE role_code = 'normal_user' LIMIT 1));
```

## 使用 RBAC1

### 创建角色层次

**通过 API:**

```bash
# 创建父角色
curl -X POST http://localhost:8080/api/system/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "roleName": "部门管理员",
    "roleCode": "dept_admin",
    "isInheritable": "1",
    "status": "1"
  }'

# 创建子角色（继承父角色权限）
curl -X POST http://localhost:8080/api/system/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "roleName": "部门员工",
    "roleCode": "dept_staff",
    "parentRoleId": 123,
    "isInheritable": "1",
    "status": "1"
  }'
```

**通过管理界面:**

1. 进入"角色管理"页面
2. 点击"新增角色"
3. 填写角色信息
4. 选择"父角色"（可选）
5. 设置"是否可继承"
6. 保存

### 查询角色层次

```bash
# 获取角色树
curl http://localhost:8080/api/system/role/tree?tenantId={tenantId}

# 查询角色的祖先
curl http://localhost:8080/api/system/role/{roleId}/ancestors

# 查询角色的后代
curl http://localhost:8080/api/system/role/{roleId}/descendants

# 查询角色的直接子角色
curl http://localhost:8080/api/system/role/{roleId}/children
```

### 权限继承示例

```
角色层次：
  超级管理员 (权限: A, B, C, D, E)
    └── 部门管理员 (直接权限: F, G)
          └── 部门员工 (直接权限: H)

实际权限：
  - 超级管理员: A, B, C, D, E
  - 部门管理员: A, B, C, D, E, F, G (继承 + 直接)
  - 部门员工: A, B, C, D, E, F, G, H (继承 + 直接)
```

## 多租户支持

RBAC1 完全支持多租户：

- 每个租户的角色层次独立
- 租户之间的角色不会互相影响
- 所有查询自动过滤租户
- 继承关系仅在同一租户内有效

## 性能优化

### 缓存机制

系统使用多层缓存优化性能：

1. **闭包表**: `sys_role_hierarchy` 预计算所有祖先-后代关系
2. **权限缓存**: `sys_role_inherited_permission` 缓存继承的权限
3. **路径字段**: `role_path` 加速祖先检查

### 手动刷新缓存

如果遇到权限不一致，可以手动刷新：

```bash
# 刷新单个角色的层次关系
curl -X POST http://localhost:8080/api/system/role/{roleId}/refresh-hierarchy

# 刷新所有角色（SQL）
DELIMITER $$
CREATE PROCEDURE refresh_all_roles()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_role_id BIGINT;
    DECLARE role_cursor CURSOR FOR 
        SELECT role_id FROM sys_role WHERE deleted = '0';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN role_cursor;
    read_loop: LOOP
        FETCH role_cursor INTO v_role_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL refresh_role_hierarchy(v_role_id);
    END LOOP;
    CLOSE role_cursor;
END$$
DELIMITER ;

CALL refresh_all_roles();
```

## 注意事项

### 循环依赖

系统自动防止循环依赖：

```bash
# 检查是否会造成循环依赖
curl "http://localhost:8080/api/system/role/check-circular?roleId=1&parentRoleId=2"
```

不允许的操作：
- 将角色 A 的父角色设置为其后代
- 创建循环引用（A -> B -> C -> A）

### 删除角色

不能删除有子角色的角色：

```sql
-- 错误：角色 123 有子角色
DELETE FROM sys_role WHERE role_id = 123;

-- 正确：先删除或重新分配子角色
UPDATE sys_role SET parent_role_id = NULL WHERE parent_role_id = 123;
DELETE FROM sys_role WHERE role_id = 123;
```

### 角色层级限制

最大层级深度为 10 层，超过会报错。

## 回滚方案

如果需要回滚到 RBAC0：

```sql
-- 1. 备份新增表数据（可选）
CREATE TABLE sys_role_hierarchy_backup AS SELECT * FROM sys_role_hierarchy;
CREATE TABLE sys_role_inherited_permission_backup AS SELECT * FROM sys_role_inherited_permission;

-- 2. 删除新增表
DROP TABLE IF EXISTS sys_role_hierarchy;
DROP TABLE IF EXISTS sys_role_inherited_permission;

-- 3. 删除新增字段
ALTER TABLE sys_role 
DROP COLUMN parent_role_id,
DROP COLUMN role_level,
DROP COLUMN role_path,
DROP COLUMN is_inheritable;

-- 4. 删除存储过程
DROP PROCEDURE IF EXISTS refresh_role_hierarchy;
DROP PROCEDURE IF EXISTS refresh_role_inherited_permissions;
DROP PROCEDURE IF EXISTS check_role_hierarchy_cycle;
```

## 常见问题

### Q: 升级后现有角色会受影响吗？

A: 不会。所有现有角色自动成为顶级角色（`parent_role_id = NULL`），行为与 RBAC0 完全一致。

### Q: 如何禁用角色继承？

A: 将角色的 `is_inheritable` 设置为 `0`，该角色的权限将不会被子角色继承。

### Q: 权限继承是实时的吗？

A: 是的。修改父角色的权限后，系统会自动刷新所有后代角色的权限缓存。

### Q: 多租户环境下安全吗？

A: 是的。所有操作都经过租户隔离检查，租户A的角色不会影响租户B。

### Q: 性能如何？

A: 通过闭包表和缓存优化，查询性能接近 RBAC0。典型的权限检查在 1ms 以内。

## 技术支持

如有问题，请：

1. 查看日志：`logs/woodlin.log`
2. 提交 Issue：https://github.com/linyuliu/woodlin/issues
3. 查看 API 文档：http://localhost:8080/api/doc.html
