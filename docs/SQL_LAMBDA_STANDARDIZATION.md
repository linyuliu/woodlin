# SQL查询Lambda标准化文档

## 概述

本项目采用MyBatis-Plus框架，统一使用Lambda查询方式编写SQL，以实现以下目标：
1. **数据库方言兼容性** - Lambda查询能自动适配MySQL、PostgreSQL、Oracle等不同数据库
2. **类型安全** - 编译时检查，避免字段名拼写错误
3. **代码可维护性** - 代码即文档，更易理解和重构

## Lambda查询规范

### 基本查询示例

```java
// 单条件查询
SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>()
    .eq(SysUser::getUsername, username)
    .last("LIMIT 1"));

// 多条件查询
List<SysUser> users = userService.list(new LambdaQueryWrapper<SysUser>()
    .like(StrUtil.isNotBlank(name), SysUser::getNickname, name)
    .eq(StrUtil.isNotBlank(status), SysUser::getStatus, status)
    .orderByDesc(SysUser::getCreateTime));

// 分页查询
Page<SysUser> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StrUtil.isNotBlank(username), SysUser::getUsername, username)
    .eq(StrUtil.isNotBlank(status), SysUser::getStatus, status)
    .orderByDesc(SysUser::getCreateTime);
IPage<SysUser> result = userService.page(page, wrapper);
```

### 更新操作示例

```java
// 单个实体更新
SysUser user = userService.getById(userId);
user.setStatus("1");
user.setUpdateTime(LocalDateTime.now());
userService.updateById(user);

// 条件更新
LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.set(SysUser::getLoginCount, loginCount + 1)
    .set(SysUser::getLastLoginTime, LocalDateTime.now())
    .eq(SysUser::getUserId, userId);
userService.update(updateWrapper);
```

### 删除操作示例

```java
// 逻辑删除（推荐）
userService.removeById(userId);

// 条件删除
LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(SysUser::getStatus, "0")
    .lt(SysUser::getCreateTime, LocalDateTime.now().minusDays(30));
userService.remove(wrapper);
```

## 已转换为Lambda的查询

### SysUserMapper
- ✅ `selectByUsername` - 使用 `LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)`
- ✅ `selectUserPage` - 使用 `LambdaQueryWrapper` 配合 `page(Page, wrapper)`
- ✅ `checkUsernameUnique`, `checkPhoneUnique`, `checkEmailUnique` - 使用 Lambda查询

### SysRoleMapper (部分)
- ✅ `checkRoleNameUnique` - 使用 `LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleName, roleName)`
- ✅ `checkRoleCodeUnique` - 使用 `LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode)`

## 保留XML映射的复杂查询

以下查询因涉及多表JOIN或递归查询，保留XML映射实现：

### SysRoleMapper.xml
1. **selectRolesByUserId** - 用户角色查询（JOIN sys_user_role）
   ```xml
   SELECT r.* FROM sys_role r
   INNER JOIN sys_user_role ur ON r.role_id = ur.role_id
   WHERE ur.user_id = #{userId}
   ```

2. **selectRolePage** - 角色分页（包含动态条件）

3. **selectDescendantRoles** - 查询子孙角色（JOIN sys_role_hierarchy）
   ```xml
   SELECT r.* FROM sys_role r
   INNER JOIN sys_role_hierarchy rh ON r.role_id = rh.descendant_role_id
   WHERE rh.ancestor_role_id = #{roleId}
   ```

4. **selectAncestorRoles** - 查询祖先角色（JOIN sys_role_hierarchy）

5. **selectDirectChildRoles** - 查询直接子角色

6. **selectTopLevelRoles** - 查询顶级角色

### SysPermissionMapper.xml
1. **selectPermissionsByRoleId** - 角色权限查询（JOIN sys_role_permission）
   ```xml
   SELECT p.* FROM sys_permission p
   INNER JOIN sys_role_permission rp ON p.permission_id = rp.permission_id
   WHERE rp.role_id = #{roleId}
   ```

2. **selectPermissionsByRoleIds** - 多角色权限查询（JOIN + IN子句）

3. **selectPermissionsByUserId** - 用户权限查询（多层JOIN，支持RBAC1）
   ```xml
   SELECT p.* FROM sys_permission p
   INNER JOIN sys_role_inherited_permission rip ON p.permission_id = rip.permission_id
   INNER JOIN sys_user_role ur ON rip.role_id = ur.role_id
   WHERE ur.user_id = #{userId}
   ```

### SysRoleHierarchyMapper.xml
所有查询保留XML实现，因为涉及复杂的层次关系查询和递归操作。

### SysRoleInheritedPermissionMapper.xml
所有查询保留XML实现，用于RBAC1权限缓存管理。

## 迁移指南

### 何时使用Lambda查询
- ✅ 单表查询
- ✅ 简单条件筛选
- ✅ 排序和分页
- ✅ 简单的统计查询
- ✅ 批量插入/更新/删除

### 何时保留XML映射
- ❌ 涉及多表JOIN的复杂查询
- ❌ 需要手写SQL的性能优化查询
- ❌ 递归查询或层次查询
- ❌ 使用数据库特定函数的查询
- ❌ 复杂的子查询

## 数据库方言支持

Lambda查询通过MyBatis-Plus自动适配以下数据库：
- MySQL 5.7+, 8.0+
- PostgreSQL 10+
- Oracle 11g+
- SQL Server 2012+
- MariaDB 10.2+

## 最佳实践

1. **优先使用Lambda** - 对于简单查询，始终使用Lambda方式
2. **避免硬编码字符串** - 使用方法引用代替字段名字符串
3. **合理使用条件** - 使用 `StrUtil.isNotBlank()` 等工具类进行条件判断
4. **注意性能** - 对于复杂查询，评估Lambda查询的性能是否满足要求
5. **保持一致性** - 在同一个Service中，尽量保持查询风格的一致性

## 示例：从XML迁移到Lambda

### 迁移前（XML）
```xml
<!-- SysUserMapper.xml -->
<select id="selectByUsername" resultType="SysUser">
    SELECT * FROM sys_user 
    WHERE username = #{username} 
    AND deleted = '0'
    LIMIT 1
</select>
```

### 迁移后（Lambda）
```java
// SysUserServiceImpl.java
public SysUser selectUserByUsername(String username) {
    return this.getOne(new LambdaQueryWrapper<SysUser>()
        .eq(SysUser::getUsername, username)
        .eq(SysUser::getDeleted, "0")
        .last("LIMIT 1"));
}
```

## 总结

本项目采用"简单查询用Lambda，复杂查询用XML"的混合策略，既享受Lambda查询的类型安全和代码简洁性，又保留XML映射处理复杂查询的灵活性。这种方式既满足了多数据库方言兼容的需求，又不牺牲性能和功能。
