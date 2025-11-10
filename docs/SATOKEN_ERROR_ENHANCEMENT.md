# SaToken 错误提示增强说明

## 概述

为了提升开发体验，特别是在首次启动应用时，Woodlin 增强了 SaToken 认证失败时的错误提示信息。现在，当用户访问受保护的接口但未提供认证凭证时，系统会提供更详细和有用的指导信息。

## 功能特性

### 1. 智能错误提示

系统会根据当前配置状态，提供不同的错误提示：

#### 开发环境（dev-token 已启用）

当开发令牌功能启用时，错误提示会包含：
- 提示用户在请求头中添加 Authorization 字段
- 告知开发令牌已在启动时生成（如果自动生成已启用）
- 或提示访问 `/auth/dev-token` 端点获取令牌

#### 生产环境（dev-token 未启用）

当开发令牌功能未启用时，错误提示会：
- 提示用户先登录获取访问令牌
- 说明如何在请求头中添加 Authorization 字段

### 2. 错误消息示例

#### 开发环境下的错误响应

```json
{
  "code": 401,
  "message": "未提供登录凭证。请在请求头中添加 Authorization 字段。开发环境下，令牌已在启动时自动生成并输出到控制台。",
  "timestamp": "2025-01-10T08:00:00"
}
```

#### 生产环境下的错误响应

```json
{
  "code": 401,
  "message": "未提供登录凭证。请先登录获取访问令牌，然后在请求头中添加 Authorization 字段。",
  "timestamp": "2025-01-10T08:00:00"
}
```

## 配置说明

### 开发令牌配置

在 `application.yml` 中配置开发令牌功能：

```yaml
woodlin:
  security:
    dev-token:
      # 是否启用开发令牌功能（生产环境请设置为false）
      enabled: true
      # 开发令牌的默认用户名
      username: admin
      # 是否在应用启动时自动生成令牌
      auto-generate-on-startup: true
      # 是否在控制台输出令牌信息
      print-to-console: true
      # 令牌提示信息的显示格式（banner或simple）
      display-format: banner
```

### 环境变量配置

```bash
# 启用开发令牌
export DEV_TOKEN_ENABLED=true

# 配置默认用户
export DEV_TOKEN_USERNAME=admin

# 自动生成令牌
export DEV_TOKEN_AUTO_GENERATE=true

# 在控制台打印令牌
export DEV_TOKEN_PRINT=true
```

## 使用指南

### 方法1: 使用自动生成的令牌

1. 启动应用时，控制台会输出类似以下的令牌信息：

```
╔═══════════════════════════════════════════════════════════════════════════════╗
║                          开发调试令牌 (Development Token)                      ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║ 用户名 (Username)    : admin                                                  ║
║ 令牌 (Token)         : a1b2c3d4-e5f6-7890-abcd-ef1234567890                    ║
║ 生成时间 (Time)       : 2025-01-10T08:00:00                                   ║
║ 过期时间 (Expires)    : 30天0小时                                             ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║ 使用方法 (Usage):                                                              ║
║   1. 在请求头中添加: Authorization: a1b2c3d4-e5f6-7890-abcd-ef1234567890      ║
║   2. 或访问 /auth/dev-token 端点重新生成                                       ║
╚═══════════════════════════════════════════════════════════════════════════════╝
```

2. 复制令牌值，在请求时添加到请求头：

```bash
curl -H "Authorization: a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
     http://localhost:8080/api/system/users
```

### 方法2: 手动获取开发令牌

访问 `/auth/dev-token` 端点：

```bash
curl http://localhost:8080/api/auth/dev-token
```

返回：
```json
{
  "code": 200,
  "message": "开发令牌生成成功",
  "data": {
    "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "admin",
    "expiresIn": 2592000
  }
}
```

### 方法3: 正常登录

调用登录接口获取令牌：

```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"Passw0rd"}'
```

## 错误类型说明

系统会针对不同的认证错误提供相应的提示：

| 错误类型 | 描述 | HTTP 状态码 |
|---------|------|-----------|
| NOT_TOKEN | 未提供登录凭证 | 401 |
| INVALID_TOKEN | 登录凭证无效 | 401 |
| TOKEN_TIMEOUT | 登录凭证已过期 | 401 |
| BE_REPLACED | 账号已在其他地方登录 | 401 |
| KICK_OUT | 账号已被强制下线 | 401 |

## 安全建议

1. **生产环境**: 必须将 `dev-token.enabled` 设置为 `false`
2. **自动生成**: 生产环境中应禁用 `auto-generate-on-startup`
3. **控制台输出**: 生产环境中应禁用 `print-to-console`
4. **令牌管理**: 定期更换生产环境的令牌

## 故障排除

### 问题1: 控制台没有显示令牌

检查配置：
- `dev-token.enabled` 是否为 `true`
- `dev-token.auto-generate-on-startup` 是否为 `true`
- `dev-token.print-to-console` 是否为 `true`

### 问题2: 令牌无效

可能原因：
- 令牌已过期（检查 `timeout` 配置）
- 应用重启后令牌失效（Redis 数据丢失）
- 令牌格式错误（确保包含完整令牌）

解决方法：
1. 重新获取令牌（访问 `/auth/dev-token`）
2. 检查 Redis 连接状态
3. 确认 Authorization 请求头格式正确

### 问题3: 首次启动报错

如果在首次启动时，开发令牌生成失败（用户不存在等），应用仍会继续启动，只是会在日志中显示警告。

解决方法：
1. 检查数据库中是否存在配置的用户（默认 `admin`）
2. 运行数据库初始化脚本
3. 或访问 `/auth/dev-token` 手动生成令牌

## 技术实现

### 核心组件

1. **SaTokenExceptionHandler**: 增强的异常处理器
2. **DevTokenProperties**: 开发令牌配置属性
3. **DevTokenStartupListener**: 启动时自动生成令牌的监听器

### 扩展性

如果需要自定义错误消息，可以：

1. 继承 `SaTokenExceptionHandler` 并重写 `buildNotTokenMessage` 方法
2. 注册自己的 `@RestControllerAdvice`
3. 根据业务需求调整错误消息内容

## 相关文件

- `woodlin-system/woodlin-system-security/src/main/java/com/mumu/woodlin/security/handler/SaTokenExceptionHandler.java`
- `woodlin-system/woodlin-system-security/src/main/java/com/mumu/woodlin/security/config/DevTokenProperties.java`
- `woodlin-admin/src/main/java/com/mumu/woodlin/admin/service/DevTokenStartupListener.java`
- `woodlin-admin/src/main/resources/application.yml`
