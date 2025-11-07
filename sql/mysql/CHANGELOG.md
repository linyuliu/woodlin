# MySQL 脚本更新日志

## 版本 2.0.0 (2025-11-07)

### 🎉 重大更新

重新整理了所有 MySQL 数据库脚本，提供更清晰、更易用的导入方案。

### ✨ 新增文件

| 文件名 | 说明 | 行数 | 大小 |
|--------|------|------|------|
| **woodlin_complete_schema.sql** | 完整表结构脚本，包含所有模块 | 672 | 33KB |
| **woodlin_complete_data.sql** | 完整初始数据脚本 | 237 | 16KB |
| **README.md** | 详细使用文档 | 334 | 7KB |
| **QUICKSTART.md** | 快速开始指南（中文） | - | 3KB |
| **CHANGELOG.md** | 本更新日志 | - | - |

### 🔄 主要变更

#### 1. 脚本整合

**之前**：多个分散的脚本文件，导入顺序不明确
```
woodlin_schema.sql          → 基础表结构
woodlin_data.sql            → 基础数据
rbac1_upgrade.sql           → RBAC1 升级（含存储过程）
password_policy_update.sql  → 密码策略
system_config_data.sql      → 系统配置
oss_management_schema.sql   → OSS 管理
sql2api_schema.sql          → SQL2API
```

**现在**：两个完整脚本，一步到位
```
woodlin_complete_schema.sql → 所有表结构（含 RBAC1）
woodlin_complete_data.sql   → 所有初始数据
```

#### 2. 移除存储过程

**之前**：`rbac1_upgrade.sql` 包含 3 个 MySQL 存储过程
- `check_role_hierarchy_cycle` - 检查循环依赖
- `refresh_role_hierarchy` - 刷新角色层次
- `refresh_role_inherited_permissions` - 刷新继承权限

**现在**：已移除所有存储过程
- 业务逻辑应在 Java Service 层实现
- 提高可维护性和跨数据库兼容性
- 符合现代应用架构最佳实践

#### 3. RBAC1 功能整合

**sys_role 表新增字段**：
- `parent_role_id` - 父角色 ID
- `role_level` - 角色层级
- `role_path` - 角色路径
- `is_inheritable` - 是否可继承

**新增表**：
- `sys_role_hierarchy` - 角色继承关系表（闭包表）
- `sys_role_inherited_permission` - 角色继承权限缓存表

#### 4. 密码策略整合

**sys_user 表新增字段**：
- `pwd_change_time` - 密码最后修改时间
- `is_first_login` - 是否首次登录
- `pwd_expire_days` - 密码过期天数

**系统配置新增**：
- 12 项密码策略配置（config_id: 200-212）

#### 5. 文件管理增强

**sys_file 表增强**：
- 新增 SHA256 哈希字段
- 新增 OSS 相关字段（storage_config_id, upload_policy_id, bucket_name, object_key）
- 新增图片属性字段（width, height, thumbnail）
- 新增访问统计字段（access_count, last_access_time）

**新增表**：
- `sys_storage_config` - 存储平台配置
- `sys_upload_policy` - 上传策略
- `sys_upload_token` - 上传令牌

### 📊 统计对比

| 项目 | 之前 | 现在 | 变化 |
|------|------|------|------|
| 脚本文件数 | 8+ 个 | 2 个核心 | 简化 75% |
| 表数量 | 15 个 | 23 个 | +8 个 |
| 存储过程 | 3 个 | 0 个 | -100% |
| 配置项 | ~20 项 | 40+ 项 | +100% |
| 文档页数 | 分散 | 3 个完整文档 | 系统化 |

### 🗂️ 表结构清单

#### 基础系统表 (8)
1. sys_tenant - 租户表
2. sys_dept - 部门表
3. sys_user - 用户表
4. sys_role - 角色表（含 RBAC1 字段）
5. sys_permission - 权限表
6. sys_user_role - 用户角色关联
7. sys_role_permission - 角色权限关联
8. sys_role_dept - 角色部门关联

#### RBAC1 扩展表 (2)
9. sys_role_hierarchy - 角色继承关系
10. sys_role_inherited_permission - 继承权限缓存

#### 系统功能表 (3)
11. sys_config - 系统配置
12. sys_oper_log - 操作日志
13. sys_job - 定时任务

#### 文件管理表 (4)
14. sys_storage_config - 存储配置
15. sys_upload_policy - 上传策略
16. sys_file - 文件信息
17. sys_upload_token - 上传令牌

#### SQL2API 表 (4)
18. sql2api_config - API 配置
19. sql2api_orchestration - API 编排
20. sql2api_datasource - 动态数据源
21. sql2api_execute_log - API 执行日志

#### 其他表 (2)
22. gen_table - 代码生成
23. sys_sensitive_data - 敏感数据（可选）

### 📝 配置数据清单

#### 基础配置 (6 项)
- 皮肤样式、初始密码、侧边栏主题、验证码、用户注册、IP 黑名单

#### API 加密配置 (16 项)
- 启用开关、算法选择（AES/RSA/SM4）
- 密钥配置、加密模式、路径过滤

#### 密码策略配置 (13 项)
- 启用开关、过期策略、锁定策略
- 强密码要求（长度、数字、大小写、特殊字符）

#### 活动监控配置 (6 项)
- 启用开关、超时设置、检查间隔
- API 请求监控、用户交互监控

#### OSS 存储配置 (2 项)
- 默认本地存储配置
- 通用上传策略 + 图片上传策略

### 🔧 迁移指南

#### 对于新项目
直接使用新的完整脚本即可，无需任何额外步骤。

#### 对于已有项目

**选项 1：推荐 - 创建新库迁移**
1. 备份现有数据
2. 创建新数据库，导入完整脚本
3. 迁移业务数据

**选项 2：原地升级（不推荐）**
1. 备份数据库
2. 执行缺失的 ALTER TABLE 语句
3. 创建新表
4. 插入新配置数据
5. 详细步骤见 README.md

### ⚠️ 不兼容变更

1. **存储过程已移除**
   - 如果你的代码调用了存储过程，需要重构为 Java 实现

2. **表结构变化**
   - `sys_role` 表增加了 4 个字段
   - `sys_user` 表增加了 3 个字段
   - `sys_file` 表大幅增强

3. **配置键名变化**
   - 部分配置键名已标准化（如密码策略配置）

### 🎯 下一步计划

- [ ] 添加 PostgreSQL 完整脚本
- [ ] 添加 Oracle 完整脚本
- [ ] 提供数据迁移工具
- [ ] 添加数据库版本管理（Flyway/Liquibase）

### 📚 参考文档

- [README.md](README.md) - 完整使用文档
- [QUICKSTART.md](QUICKSTART.md) - 快速开始指南
- [GitHub Issues](https://github.com/linyuliu/woodlin/issues) - 问题反馈

---

**发布日期**: 2025-11-07  
**维护者**: mumu  
**版本**: 2.0.0
