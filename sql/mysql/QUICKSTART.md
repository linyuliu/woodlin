# MySQL 数据库导入快速指南

## 问题背景

在 RBAC1 重构后，数据库脚本较为混乱，导入顺序不明确。现已重新整理为两个完整的 SQL 脚本，使用更加简单明了。

## 解决方案

### 新的完整脚本（推荐使用）

1. **woodlin_complete_schema.sql** - 完整表结构
   - 包含所有系统表
   - 支持 RBAC1 角色继承
   - 已移除存储过程（业务逻辑在 Java 层实现）

2. **woodlin_complete_data.sql** - 完整初始数据
   - 包含所有必需的初始数据
   - 默认用户：admin / Passw0rd

## 导入步骤

### 方式一：命令行导入（推荐）

```bash
# 1. 导入表结构
mysql -u root -p < sql/mysql/woodlin_complete_schema.sql

# 2. 导入初始数据
mysql -u root -p < sql/mysql/woodlin_complete_data.sql
```

### 方式二：MySQL 客户端导入

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行脚本
mysql> source /path/to/woodlin/sql/mysql/woodlin_complete_schema.sql;
mysql> source /path/to/woodlin/sql/mysql/woodlin_complete_data.sql;
```

### 方式三：使用 Docker 容器导入

```bash
# 假设 MySQL 容器名为 mysql_container
docker exec -i mysql_container mysql -u root -p123456 < sql/mysql/woodlin_complete_schema.sql
docker exec -i mysql_container mysql -u root -p123456 < sql/mysql/woodlin_complete_data.sql
```

### 方式四：使用 Navicat/MySQL Workbench 等图形工具

1. 连接到 MySQL 数据库
2. 点击 "执行 SQL 文件" 或 "运行 SQL 脚本"
3. 依次选择并执行：
   - woodlin_complete_schema.sql
   - woodlin_complete_data.sql

## 验证导入

```sql
USE woodlin;

-- 查看所有表
SHOW TABLES;

-- 验证用户数据
SELECT user_id, username, nickname FROM sys_user;

-- 验证角色数据
SELECT role_id, role_name, role_code FROM sys_role;
```

预期结果：
- 共 23 个表
- 2 个用户（admin, demo）
- 2 个角色（超级管理员，普通角色）
- 大量系统配置数据

## 关于 RBAC1 角色继承

### 什么变了？

1. **去除存储过程**：原 `rbac1_upgrade.sql` 中的存储过程已移除
2. **业务逻辑在 Java 层**：角色继承的维护逻辑应在 Service 层实现
3. **数据结构完整**：新脚本已包含 RBAC1 所需的所有表结构

### 需要在 Java 层实现的功能

1. 角色创建/更新时：
   - 检查循环依赖
   - 维护 `sys_role_hierarchy` 表
   - 刷新 `sys_role_inherited_permission` 缓存

2. 权限查询时：
   - 从 `sys_role_inherited_permission` 读取完整权限列表
   - 使用 Redis 缓存提升性能

### 为什么不用存储过程？

1. **可维护性**：Java 代码更易于调试和测试
2. **跨数据库兼容**：不依赖特定数据库的存储过程语法
3. **业务逻辑集中**：所有逻辑在应用层，便于管理
4. **最佳实践**：现代应用通常将业务逻辑放在应用层

## 旧脚本说明

以下脚本已不再需要单独导入（已整合到完整脚本中）：

- ~~woodlin_schema.sql~~ → 已整合
- ~~woodlin_data.sql~~ → 已整合
- ~~rbac1_upgrade.sql~~ → 已整合（不含存储过程）
- ~~password_policy_update.sql~~ → 已整合
- ~~system_config_data.sql~~ → 已整合
- ~~oss_management_schema.sql~~ → 已整合
- ~~sql2api_schema.sql~~ → 已整合

这些脚本保留仅供参考或了解系统演进历史。

## 常见问题

### Q1: 导入时报错字符集不支持？

```sql
ALTER DATABASE woodlin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q2: 需要重新导入怎么办？

```sql
DROP DATABASE IF EXISTS woodlin;
CREATE DATABASE woodlin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 然后重新执行导入步骤
```

### Q3: 我已经有旧数据库，如何升级？

**不建议直接升级**。建议：
1. 备份现有数据
2. 创建新数据库导入新脚本
3. 迁移业务数据到新库

如必须升级，参考 README.md 中的详细升级步骤。

### Q4: 默认密码是什么？

- 用户名：admin / demo
- 密码：Passw0rd（注意大小写）

**重要**：生产环境请立即修改默认密码！

### Q5: 如何确认 RBAC1 功能正常？

1. 检查表是否存在：
```sql
SHOW TABLES LIKE 'sys_role%';
```
应该看到：
- sys_role（包含 parent_role_id 等字段）
- sys_role_hierarchy
- sys_role_inherited_permission

2. 检查字段：
```sql
DESCRIBE sys_role;
```
应该包含 parent_role_id, role_level, role_path, is_inheritable 等字段。

## 更多信息

详细文档请查看：
- [完整文档](README.md) - 详细的使用说明
- [GitHub Issues](https://github.com/linyuliu/woodlin/issues) - 问题反馈

---

**最后更新**: 2025-11-07  
**维护者**: mumu
