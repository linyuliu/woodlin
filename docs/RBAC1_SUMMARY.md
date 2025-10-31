# RBAC1 角色继承功能 - 实现总结

## 项目概述

本次更新将 Woodlin 系统从 RBAC0（基础角色访问控制）升级到 RBAC1（支持角色继承），提供更灵活、更强大的权限管理能力。

## 主要变更

### 1. 数据库层 (Database)

**新增表:**
- `sys_role_hierarchy`: 角色层次关系表（闭包表模式）
- `sys_role_inherited_permission`: 继承权限缓存表

**表结构变更:**
```sql
-- sys_role 新增字段
ALTER TABLE sys_role ADD COLUMN parent_role_id bigint(20);      -- 父角色ID
ALTER TABLE sys_role ADD COLUMN role_level int(11);             -- 角色层级
ALTER TABLE sys_role ADD COLUMN role_path varchar(500);         -- 角色路径
ALTER TABLE sys_role ADD COLUMN is_inheritable char(1);         -- 是否可继承
```

**存储过程:**
- `check_role_hierarchy_cycle`: 检查循环依赖
- `refresh_role_hierarchy`: 刷新角色层次关系
- `refresh_role_inherited_permissions`: 刷新继承权限缓存

### 2. 实体层 (Entity)

**新增实体:**
- `SysRoleHierarchy`: 角色层次关系实体
- `SysRoleInheritedPermission`: 继承权限缓存实体

**更新实体:**
- `SysRole`: 添加 `parentRoleId`, `roleLevel`, `rolePath`, `isInheritable` 字段

**DTO:**
- `RoleTreeDTO`: 角色树展示数据传输对象

### 3. 数据访问层 (Mapper)

**新增 Mapper:**
- `SysRoleMapper`: 角色基础数据访问
- `SysRoleHierarchyMapper`: 层次关系查询
- `SysRoleInheritedPermissionMapper`: 权限缓存操作
- `SysPermissionMapper`: 权限查询

**MyBatis XML:**
- `SysRoleMapper.xml`: 角色查询SQL
- `SysRoleHierarchyMapper.xml`: 层次关系SQL
- `SysRoleInheritedPermissionMapper.xml`: 权限缓存SQL
- `SysPermissionMapper.xml`: 权限查询SQL

### 4. 服务层 (Service)

**新增服务:**
- `ISysPermissionService` / `SysPermissionServiceImpl`: 权限服务

**更新服务:**
- `ISysRoleService`: 添加角色层次相关方法
  - `selectAncestorRoles`: 查询祖先角色
  - `selectDescendantRoles`: 查询后代角色
  - `selectDirectChildRoles`: 查询直接子角色
  - `selectAllRolesByUserId`: 查询用户所有角色（含继承）
  - `refreshRoleHierarchy`: 刷新层次关系
  - `checkCircularDependency`: 检查循环依赖
  - `selectAllPermissionsByRoleId`: 查询所有权限（含继承）
  - `buildRoleTree`: 构建角色树

- `SysRoleServiceImpl`: 完整实现所有RBAC1功能

**安全服务更新:**
- `AuthenticationServiceImpl`: 登录时加载继承的角色和权限

### 5. 控制层 (Controller)

**新增控制器:**
- `SysRoleController`: 角色管理控制器，提供15个API端点
  - 基础CRUD操作
  - 角色层次查询（祖先、后代、子角色）
  - 角色树获取
  - 循环依赖检查
  - 层次关系刷新
  - 权限查询

### 6. 工具类 (Utility)

**新增工具类:**
- `RoleHierarchyUtil`: 角色层次结构工具类
  - 路径构建和解析
  - 层级计算
  - 循环依赖检测
  - 角色树构建

### 7. 文档 (Documentation)

**新增文档:**
- `rbac1-migration-guide.md`: 完整的迁移指南
  - 升级步骤
  - 数据迁移
  - 验证方法
  - 回滚方案
  
- `rbac1-usage-guide.md`: 详细的使用指南
  - 核心概念
  - 使用场景
  - API参考
  - 最佳实践
  - 故障排查

## 技术亮点

### 1. 闭包表模式 (Closure Table)

使用闭包表存储所有祖先-后代关系：

```
角色层次：A -> B -> C

闭包表记录：
ancestor | descendant | distance
---------|------------|----------
   A     |     A      |    0
   B     |     B      |    0
   C     |     C      |    0
   A     |     B      |    1
   B     |     C      |    1
   A     |     C      |    2
```

**优势:**
- O(1) 查询任意两角色的关系
- 简化祖先/后代查询
- 支持任意层级

### 2. 路径物化 (Materialized Path)

使用路径字符串快速判断祖先关系：

```
超级管理员: /1/
部门总监: /1/2/
部门经理: /1/2/3/
```

**优势:**
- 快速祖先检查（字符串包含）
- 计算层级深度
- 支持前缀查询

### 3. 权限缓存 (Permission Cache)

预计算所有继承权限：

```
角色 A (权限: p1, p2)
  └── 角色 B (直接权限: p3)
  
缓存表：
role_id | permission_id | is_inherited | inherited_from
--------|---------------|--------------|---------------
   B    |      p1       |      1       |       A
   B    |      p2       |      1       |       A
   B    |      p3       |      0       |     NULL
```

**优势:**
- 避免实时计算
- 提升查询性能
- 支持权限来源追踪

### 4. 循环依赖检测

多层防护机制：

1. **服务层检查**: `checkCircularDependency()`
2. **数据库约束**: 存储过程验证
3. **前端验证**: API端点支持

### 5. 多租户隔离

所有操作自动过滤租户：

```java
// 自动注入租户ID
wrapper.eq(StrUtil.isNotBlank(tenantId), SysRole::getTenantId, tenantId);

// 层次关系也包含租户
hierarchy.setTenantId(tenantId);
```

## API 端点总览

### 基础操作
- `POST /system/role` - 创建角色
- `PUT /system/role` - 更新角色
- `DELETE /system/role/{roleIds}` - 删除角色
- `GET /system/role/{roleId}` - 获取角色详情
- `GET /system/role/list` - 分页查询角色

### RBAC1 功能
- `GET /system/role/tree` - 获取角色树
- `GET /system/role/top-level` - 获取顶级角色
- `GET /system/role/{roleId}/ancestors` - 获取祖先角色
- `GET /system/role/{roleId}/descendants` - 获取后代角色
- `GET /system/role/{roleId}/children` - 获取直接子角色
- `GET /system/role/{roleId}/all-permissions` - 获取所有权限
- `GET /system/role/check-circular` - 检查循环依赖
- `POST /system/role/{roleId}/refresh-hierarchy` - 刷新层次关系

## 使用示例

### 创建角色层次

```bash
# 1. 创建顶级角色
curl -X POST http://localhost:8080/api/system/role \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "超级管理员",
    "roleCode": "super_admin",
    "isInheritable": "1"
  }'

# 2. 创建子角色
curl -X POST http://localhost:8080/api/system/role \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "部门经理",
    "roleCode": "dept_manager",
    "parentRoleId": 1,
    "isInheritable": "1"
  }'
```

### 查询角色树

```bash
curl http://localhost:8080/api/system/role/tree
```

响应：
```json
{
  "code": 200,
  "data": [
    {
      "roleId": 1,
      "roleName": "超级管理员",
      "roleLevel": 0,
      "children": [
        {
          "roleId": 2,
          "roleName": "部门经理",
          "roleLevel": 1,
          "children": []
        }
      ]
    }
  ]
}
```

## 性能优化

### 索引策略

```sql
-- 角色表索引
CREATE INDEX idx_parent_role_id ON sys_role(parent_role_id);
CREATE INDEX idx_role_level ON sys_role(role_level);

-- 层次关系表索引
CREATE INDEX idx_descendant ON sys_role_hierarchy(descendant_role_id);
CREATE INDEX idx_ancestor ON sys_role_hierarchy(ancestor_role_id);
CREATE INDEX idx_distance ON sys_role_hierarchy(distance);

-- 权限缓存表索引
CREATE INDEX idx_permission_id ON sys_role_inherited_permission(permission_id);
CREATE INDEX idx_inherited_from ON sys_role_inherited_permission(inherited_from);
```

### 缓存建议

1. **应用层缓存**: 使用Redis缓存常用角色树
2. **数据库缓存**: 定期刷新权限缓存表
3. **会话缓存**: 用户登录信息包含完整权限列表

## 兼容性

### 向后兼容

✅ **完全兼容现有代码**
- 不设置父角色时，行为与RBAC0一致
- 现有API全部保留
- 数据结构向下兼容

### 迁移路径

1. **阶段1**: 数据库升级（添加新表和字段）
2. **阶段2**: 应用部署（支持RBAC1）
3. **阶段3**: 逐步配置角色层次（可选）
4. **阶段4**: 完全启用RBAC1功能

### 回滚支持

提供完整的回滚SQL脚本，可安全回退到RBAC0。

## 安全考虑

### 1. 权限提升防护
- 只有拥有父角色权限的用户才能创建子角色
- 防止用户自我提权

### 2. 循环依赖防护
- 多层检查机制
- 前端+后端双重验证
- 数据库约束

### 3. 多租户隔离
- 严格的租户过滤
- 不允许跨租户角色继承
- 自动注入租户ID

### 4. 审计支持
- 所有变更记录创建者和更新者
- 支持操作日志追踪
- 权限来源可追溯

## 测试建议

### 单元测试

```java
@Test
public void testRoleHierarchy() {
    // 测试角色层次创建
    // 测试权限继承
    // 测试循环依赖检测
}

@Test
public void testMultiTenantIsolation() {
    // 测试租户隔离
    // 测试跨租户访问防护
}
```

### 集成测试

```java
@Test
public void testPermissionInheritance() {
    // 创建角色层次
    // 分配权限
    // 验证权限继承
    // 验证用户登录后权限正确
}
```

## 性能基准

基于闭包表和缓存的设计，预期性能：

| 操作 | 复杂度 | 预期时间 |
|------|--------|----------|
| 查询祖先 | O(1) | < 1ms |
| 查询后代 | O(1) | < 1ms |
| 权限检查 | O(1) | < 1ms |
| 创建角色 | O(n) | < 100ms |
| 更新层次 | O(n) | < 100ms |

其中 n 为该角色的后代数量。

## 总结

本次RBAC1升级提供了：

✅ **功能完整**: 支持完整的角色继承功能
✅ **性能优秀**: 使用缓存和优化的数据结构
✅ **安全可靠**: 多层防护机制
✅ **易于使用**: 丰富的API和文档
✅ **向后兼容**: 不影响现有功能
✅ **多租户安全**: 完整的租户隔离

系统现在支持更灵活、更强大的权限管理，同时保持了良好的性能和安全性。
