# 系统配置管理文档

## 概述

系统配置管理功能提供了统一的前端界面来配置和管理各类系统设置，包括：
- API 加密配置
- 密码策略配置
- 用户活动监控配置

所有配置都存储在 `sys_config` 数据库表中，通过前端界面可以直观地修改和保存这些配置。

## 功能特性

### 1. API 加密配置

支持配置 API 请求和响应的加密功能：

**基础配置**
- 启用/禁用 API 加密
- 选择加密算法（AES、RSA、SM4）

**AES 配置**（推荐用于大数据量）
- AES 密钥（Base64 编码）
- AES 初始化向量 IV（Base64 编码）
- 加密模式（CBC、ECB、CFB、OFB、CTR）
- 填充方式（PKCS5Padding、PKCS7Padding、NoPadding）

**RSA 配置**（推荐用于敏感信息）
- RSA 公钥（Base64 编码）
- RSA 私钥（Base64 编码）
- 密钥长度（1024、2048、4096）

**SM4 配置**（国密标准）
- SM4 密钥（Base64 编码）
- SM4 初始化向量 IV（Base64 编码）
- 加密模式（CBC、ECB）

**接口配置**
- 包含路径：需要加密的接口路径模式，支持 Ant 风格通配符
- 排除路径：排除加密的接口路径模式
- 加密请求体：是否加密请求数据
- 加密响应体：是否加密响应数据

### 2. 密码策略配置

支持配置用户密码相关的安全策略：

**基础配置**
- 启用/禁用密码策略
- 首次登录修改密码：要求用户首次登录时修改密码
- 密码过期天数：密码有效期，0 表示永不过期
- 提醒天数：密码过期前提醒天数

**安全配置**
- 最大错误次数：密码输入错误次数上限，超过将锁定账号
- 锁定时长：账号锁定的时长（分钟）

**强密码策略**
- 启用/禁用强密码策略
- 最小密码长度
- 最大密码长度
- 要求包含数字
- 要求包含小写字母
- 要求包含大写字母
- 要求包含特殊字符

### 3. 活动监控配置

支持配置用户活动监控相关设置：

- 启用/禁用活动监控
- 超时时间：用户无活动超时时间（秒），-1 表示不限制
- 检查间隔：监控检查间隔（秒）
- 监控 API 请求：是否监控用户的 API 请求活动
- 监控用户交互：是否监控用户的键盘、鼠标交互
- 警告提前时间：超时前多久开始警告（秒）

## 使用方法

### 前端访问

1. 登录系统后，在左侧导航菜单找到"系统设置"
2. 点击进入系统设置页面
3. 选择相应的配置选项卡（API加密配置、密码策略配置、活动监控配置）
4. 修改所需的配置项
5. 点击"保存配置"按钮保存更改

### 后端 API

#### 获取配置列表

```http
GET /system/config/list
```

#### 根据配置分类获取配置

```http
GET /system/config/category/{category}
```

参数：
- `category`: 配置分类，如 `api.encryption`、`password.policy`、`activity.monitoring`

#### 批量更新配置

```http
PUT /system/config/batch
```

请求体：
```json
{
  "category": "api.encryption",
  "configs": {
    "api.encryption.enabled": "true",
    "api.encryption.algorithm": "AES",
    "api.encryption.aes-key": "your-base64-key"
  }
}
```

## 数据库结构

配置数据存储在 `sys_config` 表中：

```sql
CREATE TABLE `sys_config` (
    `config_id` bigint(20) NOT NULL COMMENT '参数主键',
    `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
    `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
    `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
    `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y-是，N-否）',
    `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';
```

## 配置项说明

### API 加密配置键名

| 配置键名 | 说明 | 默认值 |
|---------|------|--------|
| api.encryption.enabled | 是否启用加密 | false |
| api.encryption.algorithm | 加密算法 | AES |
| api.encryption.aes-key | AES密钥 | - |
| api.encryption.aes-iv | AES向量 | - |
| api.encryption.aes-mode | AES模式 | CBC |
| api.encryption.aes-padding | AES填充 | PKCS5Padding |
| api.encryption.rsa-public-key | RSA公钥 | - |
| api.encryption.rsa-private-key | RSA私钥 | - |
| api.encryption.rsa-key-size | RSA密钥长度 | 2048 |
| api.encryption.sm4-key | SM4密钥 | - |
| api.encryption.sm4-iv | SM4向量 | - |
| api.encryption.sm4-mode | SM4模式 | CBC |
| api.encryption.include-patterns | 包含路径 | - |
| api.encryption.exclude-patterns | 排除路径 | - |
| api.encryption.encrypt-request | 加密请求 | true |
| api.encryption.encrypt-response | 加密响应 | true |

### 密码策略配置键名

| 配置键名 | 说明 | 默认值 |
|---------|------|--------|
| password.policy.enabled | 是否启用密码策略 | true |
| password.policy.require-change-on-first-login | 首次登录修改密码 | false |
| password.policy.expire-days | 密码过期天数 | 0 |
| password.policy.warning-days | 提醒天数 | 7 |
| password.policy.max-error-count | 最大错误次数 | 5 |
| password.policy.lock-duration-minutes | 锁定时长 | 30 |
| password.policy.strong-password-required | 强密码要求 | false |
| password.policy.min-length | 最小长度 | 6 |
| password.policy.max-length | 最大长度 | 20 |
| password.policy.require-digits | 要求数字 | false |
| password.policy.require-lowercase | 要求小写字母 | false |
| password.policy.require-uppercase | 要求大写字母 | false |
| password.policy.require-special-chars | 要求特殊字符 | false |

### 活动监控配置键名

| 配置键名 | 说明 | 默认值 |
|---------|------|--------|
| activity.monitoring.enabled | 是否启用活动监控 | true |
| activity.monitoring.timeout-seconds | 超时时间（秒） | 1800 |
| activity.monitoring.check-interval-seconds | 检查间隔（秒） | 60 |
| activity.monitoring.monitor-api-requests | 监控API请求 | true |
| activity.monitoring.monitor-user-interactions | 监控用户交互 | true |
| activity.monitoring.warning-before-timeout-seconds | 警告提前时间（秒） | 300 |

## 初始化数据

系统配置的初始数据存储在 `sql/system_config_data.sql` 文件中。在首次部署或升级时，需要执行该 SQL 脚本来插入默认配置：

```bash
mysql -u root -p woodlin < sql/system_config_data.sql
```

## 注意事项

1. **配置生效时间**：修改配置后，某些配置可能需要重启应用才能生效。
2. **密钥安全**：加密密钥应妥善保管，不要泄露给未授权人员。
3. **备份配置**：在修改配置前建议先备份当前配置。
4. **权限控制**：系统配置功能应仅限管理员访问。
5. **配置验证**：保存配置前系统会进行基本验证，但仍需确保配置值的正确性。

## 常见问题

### Q: 修改配置后为什么没有生效？

A: 某些配置需要重启应用才能生效。建议在非业务高峰期修改配置并重启应用。

### Q: 如何生成加密密钥？

A: 可以参考 `docs/API_ENCRYPTION.md` 文档中的密钥生成方法，或使用系统提供的密钥生成工具。

### Q: 配置丢失了怎么办？

A: 可以从数据库备份中恢复，或重新执行 `sql/system_config_data.sql` 脚本恢复默认配置。

### Q: 能否针对不同租户设置不同的配置？

A: 当前版本的配置是全局的。如果需要租户级别的配置，可以通过 `tenant_id` 字段实现。

## 相关文档

- [API 加密配置文档](./API_ENCRYPTION.md)
- [密码策略实现说明](./IMPLEMENTATION_SUMMARY.md)
