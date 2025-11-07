# Woodlin MySQL 数据库脚本使用指南

## 概述

本目录包含 Woodlin 多租户中后台管理系统的 MySQL 数据库脚本。脚本已重新整理和优化，支持 RBAC1 角色继承功能，并移除了不必要的存储过程（业务逻辑已迁移到 Java 层）。

## 版本信息

- **版本**: 2.0.0
- **数据库**: MySQL 8.0+
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **更新日期**: 2025-11-07

## 脚本说明

### 核心脚本（推荐使用）

这两个脚本是最新整理的完整脚本，推荐所有新项目使用：

1. **woodlin_complete_schema.sql** - 完整表结构脚本
   - 包含所有系统表结构
   - 支持 RBAC1 角色继承功能
   - 包含多租户、用户管理、角色权限、文件管理、OSS 存储、SQL2API、任务调度等所有模块
   - 已优化索引，支持高性能查询
   - **必须先执行此脚本**

2. **woodlin_complete_data.sql** - 完整初始数据脚本
   - 包含系统初始化所需的所有数据
   - 默认租户、部门、用户、角色、权限数据
   - 系统配置（API 加密、密码策略、活动监控等）
   - OSS 存储默认配置
   - **在 schema 脚本执行后执行此脚本**

### 历史脚本（仅供参考）

以下脚本是历史版本，已被上述完整脚本整合，仅供参考或了解系统演进：

- `woodlin_schema.sql` - 基础表结构（1.0.0 版本）
- `woodlin_data.sql` - 基础初始数据（1.0.0 版本）
- `rbac1_upgrade.sql` - RBAC1 升级脚本（包含存储过程，不推荐使用）
- `password_policy_update.sql` - 密码策略更新脚本
- `system_config_data.sql` - 系统配置数据
- `oss_management_schema.sql` - OSS 管理表结构
- `sql2api_schema.sql` - SQL2API 表结构
- `searchable_encryption_example.sql` - 可搜索加密示例

**注意**: 历史脚本可能存在重复定义或不一致问题，请优先使用完整脚本。

## 快速开始

### 1. 创建数据库并导入（推荐方式）

```bash
# 方式一：使用 MySQL 命令行
mysql -u root -p < woodlin_complete_schema.sql
mysql -u root -p < woodlin_complete_data.sql

# 方式二：登录 MySQL 后执行
mysql -u root -p
source /path/to/woodlin_complete_schema.sql;
source /path/to/woodlin_complete_data.sql;

# 方式三：使用 Docker（推荐用于开发环境）
docker exec -i mysql_container mysql -u root -p123456 < woodlin_complete_schema.sql
docker exec -i mysql_container mysql -u root -p123456 < woodlin_complete_data.sql
```

### 2. 验证安装

```sql
-- 连接到数据库
USE woodlin;

-- 检查表是否创建成功
SHOW TABLES;

-- 检查默认用户是否创建
SELECT user_id, username, nickname FROM sys_user;

-- 检查角色和权限
SELECT role_id, role_name, role_code FROM sys_role;
SELECT permission_id, permission_name, permission_code FROM sys_permission;
```

### 3. 默认账号信息

系统提供了两个默认账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | Passw0rd | 超级管理员 | 拥有所有权限 |
| demo | Passw0rd | 普通用户 | 仅拥有查询权限 |

**安全提示**: 生产环境部署后，请立即修改默认密码！

## 数据库结构说明

### 核心模块

#### 1. 租户管理
- `sys_tenant` - 租户信息表

#### 2. 组织架构
- `sys_dept` - 部门表
- `sys_user` - 用户表

#### 3. RBAC 权限管理（支持 RBAC1）
- `sys_role` - 角色表（包含父角色、层级等字段）
- `sys_permission` - 权限表
- `sys_user_role` - 用户角色关联表
- `sys_role_permission` - 角色权限关联表
- `sys_role_dept` - 角色部门关联表（数据权限）
- `sys_role_hierarchy` - 角色继承层次关系表（闭包表）
- `sys_role_inherited_permission` - 角色继承权限缓存表

#### 4. 系统配置
- `sys_config` - 系统配置表
- `sys_oper_log` - 操作日志表

#### 5. 文件管理和 OSS 存储
- `sys_storage_config` - 存储平台配置表
- `sys_upload_policy` - 上传策略表
- `sys_file` - 文件信息表
- `sys_upload_token` - 上传令牌表

#### 6. 任务调度
- `sys_job` - 定时任务表

#### 7. 代码生成
- `gen_table` - 代码生成业务表

#### 8. SQL2API 动态接口
- `sql2api_config` - SQL API 配置表
- `sql2api_orchestration` - API 编排配置表
- `sql2api_datasource` - 动态数据源配置表
- `sql2api_execute_log` - API 执行日志表

#### 9. 数据加密（可选）
- `sys_sensitive_data` - 敏感数据表（可搜索加密示例）

## RBAC1 角色继承功能

### 什么是 RBAC1？

RBAC1 在 RBAC0 的基础上增加了角色继承功能，子角色可以继承父角色的所有权限。

### 核心表结构

1. **sys_role 表增强字段**：
   - `parent_role_id` - 父角色 ID
   - `role_level` - 角色层级
   - `role_path` - 角色路径（如 /1/2/3/）
   - `is_inheritable` - 是否可继承

2. **sys_role_hierarchy 表**（闭包表）：
   - 存储角色间的完整继承关系
   - 支持快速查询所有祖先和后代角色

3. **sys_role_inherited_permission 表**（缓存表）：
   - 缓存角色的所有权限（包括继承来的）
   - 提升权限查询性能

### 业务逻辑实现

**重要**: 为遵循最佳实践，RBAC1 的业务逻辑应在 Java 应用层实现，而非数据库存储过程。

需要在 Java Service 层实现以下功能：

1. **角色创建/更新**：
   - 设置父角色时，检查是否会形成循环依赖
   - 更新 `sys_role_hierarchy` 表
   - 刷新 `sys_role_inherited_permission` 缓存表

2. **权限查询**：
   - 查询用户权限时，从 `sys_role_inherited_permission` 读取
   - 包含直接权限和继承权限

3. **缓存刷新**：
   - 角色关系变更时，刷新相关的继承权限缓存
   - 建议使用 Redis 进一步缓存权限信息

### 使用示例

```sql
-- 创建角色层次：管理员 -> 部门主管 -> 普通员工

-- 1. 创建管理员角色（顶级角色）
INSERT INTO sys_role (role_id, parent_role_id, role_level, role_path, role_name, role_code)
VALUES (10, NULL, 0, '/10/', '系统管理员', 'sys_admin');

-- 2. 创建部门主管角色（继承管理员）
INSERT INTO sys_role (role_id, parent_role_id, role_level, role_path, role_name, role_code)
VALUES (11, 10, 1, '/10/11/', '部门主管', 'dept_manager');

-- 3. 创建普通员工角色（继承部门主管）
INSERT INTO sys_role (role_id, parent_role_id, role_level, role_path, role_name, role_code)
VALUES (12, 11, 2, '/10/11/12/', '普通员工', 'employee');

-- 4. 更新角色层次关系（在 Java Service 中实现）
-- 需要维护 sys_role_hierarchy 表
-- 需要刷新 sys_role_inherited_permission 缓存表
```

## 系统配置说明

系统配置存储在 `sys_config` 表中，包括：

### 基础配置（config_id: 1-99）
- 皮肤样式、初始密码、侧边栏主题等

### API 加密配置（config_id: 100-199）
- 支持 AES、RSA、SM4 多种加密算法
- 可配置加密的接口路径

### 密码策略配置（config_id: 200-299）
- 密码复杂度要求
- 密码过期策略
- 账号锁定策略

### 活动监控配置（config_id: 300-399）
- 用户活动超时检测
- 会话管理

## 性能优化建议

1. **索引优化**：
   - 所有关键查询字段都已建立索引
   - 多租户查询使用 `tenant_id` 索引

2. **缓存策略**：
   - 使用 Redis 缓存用户权限信息
   - 使用 Redis 缓存角色继承关系
   - 使用 `sys_role_inherited_permission` 作为数据库级缓存

3. **分区建议**：
   - 对于日志表（`sys_oper_log`, `sql2api_execute_log`），建议按时间分区
   - 例如：按月或按季度分区

4. **定期维护**：
   ```sql
   -- 优化表
   OPTIMIZE TABLE sys_role_hierarchy;
   OPTIMIZE TABLE sys_role_inherited_permission;
   
   -- 清理过期日志（建议保留 3-6 个月）
   DELETE FROM sys_oper_log WHERE create_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
   DELETE FROM sql2api_execute_log WHERE create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);
   ```

## 故障排除

### 1. 导入失败：字符集错误

```sql
-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';

-- 修改数据库字符集
ALTER DATABASE woodlin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 权限问题

```sql
-- 授予用户权限
GRANT ALL PRIVILEGES ON woodlin.* TO 'woodlin_user'@'%' IDENTIFIED BY 'your_password';
FLUSH PRIVILEGES;
```

### 3. 外键约束错误

完整脚本已移除外键约束，如遇到问题，可临时禁用：

```sql
SET FOREIGN_KEY_CHECKS = 0;
-- 执行导入
SET FOREIGN_KEY_CHECKS = 1;
```

### 4. 重复键错误

如需重新导入，先删除数据库：

```sql
DROP DATABASE IF EXISTS woodlin;
CREATE DATABASE woodlin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 升级说明

### 从 1.0.0 升级到 2.0.0

如果你已经使用了旧版本的脚本，**不建议**直接升级。建议：

1. **备份现有数据**
2. **创建新数据库**，使用完整脚本初始化
3. **迁移业务数据**到新数据库

如果必须升级现有数据库，请谨慎执行以下步骤：

```sql
-- 备份数据库
mysqldump -u root -p woodlin > woodlin_backup_$(date +%Y%m%d).sql

-- 为 sys_role 表添加 RBAC1 字段（如果还没有）
ALTER TABLE sys_role 
ADD COLUMN IF NOT EXISTS parent_role_id bigint(20) DEFAULT NULL COMMENT '父角色ID' AFTER role_id,
ADD COLUMN IF NOT EXISTS role_level int(11) DEFAULT 0 COMMENT '角色层级' AFTER parent_role_id,
ADD COLUMN IF NOT EXISTS role_path varchar(500) DEFAULT '' COMMENT '角色路径' AFTER role_level,
ADD COLUMN IF NOT EXISTS is_inheritable char(1) DEFAULT '1' COMMENT '是否可继承' AFTER data_scope;

-- 创建 RBAC1 相关表（如果还没有）
-- 参考 woodlin_complete_schema.sql 中的定义

-- 初始化角色层次关系
-- 参考 woodlin_complete_data.sql 中的数据
```

## 支持与反馈

如有问题或建议，请通过以下方式联系：

- GitHub Issues: https://github.com/linyuliu/woodlin/issues
- Email: admin@woodlin.com

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

---

**最后更新**: 2025-11-07  
**维护者**: mumu
