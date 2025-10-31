# RBAC1 角色继承使用指南

本文档详细介绍如何在 Woodlin 系统中使用 RBAC1 角色继承功能。

## 核心概念

### 角色层次结构

RBAC1 引入了角色的父子关系，形成树形层次结构：

```
企业架构示例：
组织结构                      对应角色层次
董事会                       超级管理员
  └── CEO                      └── 高级管理员
        ├── CTO                      ├── 技术总监
        │     ├── 研发经理                │     ├── 研发主管
        │     │     └── 工程师              │     │     └── 开发工程师
        │     └── 测试经理                │     └── 测试主管
        │           └── 测试员              │           └── 测试工程师
        └── CFO                      └── 财务总监
              └── 会计                      └── 会计
```

### 权限继承原则

1. **自动继承**: 子角色自动获得父角色的所有权限
2. **权限叠加**: 子角色 = 父角色权限 + 自身权限
3. **多层继承**: 支持多级继承，权限从顶层向下传递
4. **可控继承**: 通过 `is_inheritable` 标志控制是否允许继承

### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| `parent_role_id` | 父角色ID，NULL表示顶级角色 | `123` |
| `role_level` | 角色层级，0为顶级 | `0`, `1`, `2` |
| `role_path` | 角色路径，便于快速查找祖先 | `/1/2/3/` |
| `is_inheritable` | 是否允许子角色继承权限 | `1`-是，`0`-否 |

## 使用场景

### 场景一：企业组织架构

**需求**: 按照公司组织架构设置角色

```
超级管理员（全部权限）
  ├── 部门总监（部门管理权限）
  │     ├── 部门经理（团队管理权限）
  │     │     └── 部门员工（基础权限）
  │     └── 部门助理（协助权限）
  └── 财务总监（财务权限）
        └── 会计（记账权限）
```

**实现步骤**:

1. 创建顶级角色"超级管理员"
2. 创建"部门总监"，设置父角色为"超级管理员"
3. 依次创建下级角色，逐级设置父角色

**代码示例**:

```bash
# 1. 创建超级管理员（顶级）
POST /api/system/role
{
  "roleName": "超级管理员",
  "roleCode": "super_admin",
  "isInheritable": "1",
  "status": "1"
}

# 2. 创建部门总监（继承超级管理员）
POST /api/system/role
{
  "roleName": "部门总监",
  "roleCode": "dept_director",
  "parentRoleId": 1,
  "isInheritable": "1",
  "status": "1"
}

# 3. 创建部门经理（继承部门总监）
POST /api/system/role
{
  "roleName": "部门经理",
  "roleCode": "dept_manager",
  "parentRoleId": 2,
  "isInheritable": "1",
  "status": "1"
}
```

### 场景二：权限渐进式分配

**需求**: 基础角色 + 扩展权限

```
基础角色（读取权限）
  ├── 编辑角色（+ 编辑权限）
  │     └── 管理角色（+ 删除权限）
  └── 审核角色（+ 审核权限）
```

**权限分布**:
- 基础角色: `read`
- 编辑角色: `read`, `edit` (继承 + 自身)
- 管理角色: `read`, `edit`, `delete` (继承链)
- 审核角色: `read`, `audit` (继承 + 自身)

### 场景三：临时权限委托

**需求**: 临时提升某用户权限

1. 创建"临时管理员"角色，继承"普通管理员"
2. 为临时管理员添加额外权限
3. 将用户分配到"临时管理员"角色
4. 任务完成后，移除角色分配或禁用角色

## API 使用

### 角色管理

#### 创建角色

```http
POST /api/system/role
Content-Type: application/json
Authorization: Bearer {token}

{
  "roleName": "部门经理",
  "roleCode": "dept_manager",
  "parentRoleId": 2,           # 父角色ID，可选
  "isInheritable": "1",        # 是否可继承
  "sortOrder": 1,
  "status": "1",
  "remark": "部门经理角色"
}
```

#### 更新角色

```http
PUT /api/system/role
Content-Type: application/json
Authorization: Bearer {token}

{
  "roleId": 3,
  "parentRoleId": 5,           # 修改父角色
  "roleName": "高级经理",
  "isInheritable": "0"         # 禁止继承
}
```

#### 删除角色

```http
DELETE /api/system/role/1,2,3
Authorization: Bearer {token}
```

**注意**: 不能删除有子角色的角色

### 层次查询

#### 获取角色树

```http
GET /api/system/role/tree?tenantId=tenant001
Authorization: Bearer {token}
```

**响应示例**:

```json
{
  "code": 200,
  "data": [
    {
      "roleId": 1,
      "roleName": "超级管理员",
      "roleCode": "super_admin",
      "roleLevel": 0,
      "rolePath": "/1/",
      "children": [
        {
          "roleId": 2,
          "roleName": "部门总监",
          "roleCode": "dept_director",
          "roleLevel": 1,
          "rolePath": "/1/2/",
          "children": [
            {
              "roleId": 3,
              "roleName": "部门经理",
              "roleCode": "dept_manager",
              "roleLevel": 2,
              "rolePath": "/1/2/3/",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

#### 查询祖先角色

```http
GET /api/system/role/3/ancestors
Authorization: Bearer {token}
```

获取角色ID为3的所有上级角色。

#### 查询后代角色

```http
GET /api/system/role/1/descendants
Authorization: Bearer {token}
```

获取角色ID为1的所有下级角色（包括子孙）。

#### 查询直接子角色

```http
GET /api/system/role/2/children
Authorization: Bearer {token}
```

仅获取直接子角色，不包括孙角色。

### 权限查询

#### 查询角色的所有权限

```http
GET /api/system/role/3/all-permissions
Authorization: Bearer {token}
```

返回角色的所有权限（包括继承的）。

### 验证和维护

#### 检查循环依赖

```http
GET /api/system/role/check-circular?roleId=3&parentRoleId=1
Authorization: Bearer {token}
```

检查将角色3的父角色设置为1是否会造成循环依赖。

#### 刷新层次关系

```http
POST /api/system/role/3/refresh-hierarchy
Authorization: Bearer {token}
```

手动刷新角色3的层次关系和权限缓存。

## 最佳实践

### 1. 设计合理的角色层次

**推荐**:
```
顶级角色（管理员）
  └── 二级角色（部门主管）
        └── 三级角色（普通员工）
```

**不推荐**:
```
过深的层次（> 5层）：
  超管 → 总监 → 经理 → 主管 → 组长 → 员工 → 实习生
```

### 2. 合理使用 `is_inheritable`

**场景**: 特殊权限不应被继承

```
管理员（is_inheritable: 1）
  ├── 部门经理（is_inheritable: 1）
  │     └── 员工（is_inheritable: 1）
  └── 临时管理员（is_inheritable: 0）  # 临时权限不继承
```

### 3. 顶级角色最小化

建议每个租户只有少数几个顶级角色：
- 系统管理员
- 业务管理员
- 普通用户

### 4. 权限分配原则

1. **最小权限原则**: 基础角色只给最少的权限
2. **按需继承**: 通过继承获得更多权限
3. **显式授权**: 特殊权限直接分配，不依赖继承

### 5. 定期审计

```sql
-- 查看角色层次深度
SELECT role_id, role_name, role_level 
FROM sys_role 
WHERE role_level > 3
ORDER BY role_level DESC;

-- 查看孤立角色（没有用户的角色）
SELECT r.role_id, r.role_name
FROM sys_role r
LEFT JOIN sys_user_role ur ON r.role_id = ur.role_id
WHERE ur.role_id IS NULL 
  AND r.deleted = '0';

-- 查看权限分布
SELECT r.role_id, r.role_name, COUNT(rip.permission_id) as perm_count
FROM sys_role r
LEFT JOIN sys_role_inherited_permission rip ON r.role_id = rip.role_id
WHERE r.deleted = '0'
GROUP BY r.role_id, r.role_name
ORDER BY perm_count DESC;
```

## 故障排查

### 权限不生效

**可能原因**:
1. 缓存未刷新
2. 父角色设置了 `is_inheritable = 0`
3. 用户登录信息未更新

**解决方法**:
```bash
# 1. 刷新角色层次
POST /api/system/role/{roleId}/refresh-hierarchy

# 2. 检查角色配置
GET /api/system/role/{roleId}

# 3. 用户重新登录
```

### 角色树显示不正确

**检查数据一致性**:

```sql
-- 检查 role_path 是否正确
SELECT role_id, role_name, parent_role_id, role_path
FROM sys_role
WHERE deleted = '0';

-- 检查层次关系表
SELECT * FROM sys_role_hierarchy
WHERE descendant_role_id = {roleId};

-- 重建层次关系
CALL refresh_role_hierarchy({roleId});
```

### 性能问题

**优化建议**:

1. **确保索引存在**:
```sql
SHOW INDEX FROM sys_role;
SHOW INDEX FROM sys_role_hierarchy;
```

2. **定期清理缓存**:
```sql
-- 清理无效的层次关系
DELETE rh FROM sys_role_hierarchy rh
LEFT JOIN sys_role r ON rh.descendant_role_id = r.role_id
WHERE r.role_id IS NULL OR r.deleted = '1';
```

3. **使用应用缓存**: 在 Redis 中缓存常用角色树

## 安全考虑

### 1. 防止权限提升

- 只有拥有父角色权限的用户才能创建子角色
- 不允许用户将自己提升为更高权限的角色

### 2. 审计日志

所有角色层次变更应记录：
- 谁修改了角色
- 修改了什么（父角色变更）
- 什么时间修改

### 3. 多租户隔离

- 严格隔离不同租户的角色
- 不允许跨租户的角色继承
- API 自动过滤租户

## 进阶功能

### 动态角色调整

根据业务需求动态调整角色层次：

```java
@Service
public class RoleDynamicAdjustService {
    
    @Autowired
    private ISysRoleService roleService;
    
    // 提升角色层级
    public void promoteRole(Long roleId, Long newParentId) {
        SysRole role = roleService.getById(roleId);
        role.setParentRoleId(newParentId);
        roleService.updateRole(role);
    }
    
    // 降级角色
    public void demoteRole(Long roleId) {
        SysRole role = roleService.getById(roleId);
        SysRole parent = roleService.getById(role.getParentRoleId());
        role.setParentRoleId(parent.getParentRoleId());
        roleService.updateRole(role);
    }
}
```

### 权限差异分析

分析不同角色之间的权限差异：

```java
public class RoleCompareService {
    
    public List<String> getPermissionDiff(Long roleId1, Long roleId2) {
        List<String> perms1 = roleService.selectAllPermissionsByRoleId(roleId1);
        List<String> perms2 = roleService.selectAllPermissionsByRoleId(roleId2);
        
        // 返回在角色1但不在角色2的权限
        return perms1.stream()
            .filter(p -> !perms2.contains(p))
            .collect(Collectors.toList());
    }
}
```

## 总结

RBAC1 角色继承为 Woodlin 系统提供了强大而灵活的权限管理能力。通过合理设计角色层次和权限继承，可以：

1. 简化权限配置
2. 提高权限管理效率
3. 反映组织架构
4. 支持灵活的权限扩展

建议在实际使用中：
- 从简单开始，逐步优化
- 定期审查和调整角色层次
- 关注性能和安全
- 充分利用缓存机制
