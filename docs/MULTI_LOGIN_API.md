# 多登录方式API文档

## 概述

Woodlin系统支持6种登录方式，以满足不同场景下的认证需求。所有登录接口使用统一的 `/auth/login` 端点，通过 `loginType` 字段区分不同的登录方式。

## 统一登录端点

**端点**: `POST /auth/login`

**Content-Type**: `application/json`

## 登录方式详解

### 1. 密码登录 (Password Login)

最传统和基础的登录方式，使用用户名和密码进行认证。

**LoginType**: `password`

**请求体**:
```json
{
  "loginType": "password",
  "username": "admin",
  "password": "Passw0rd",
  "rememberMe": false
}
```

**必需字段**:
- `username`: 用户名（1-30字符）
- `password`: 密码（1-100字符）

**可选字段**:
- `rememberMe`: 是否记住登录状态

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "requirePasswordChange": false,
    "passwordExpiringSoon": false,
    "daysUntilPasswordExpiration": 30,
    "message": "登录成功"
  }
}
```

**特性**:
- ✅ 支持密码策略验证
- ✅ 支持账号锁定保护
- ✅ 支持密码错误次数限制
- ✅ 支持密码过期提醒

---

### 2. 验证码登录 (Captcha Login)

使用用户名和图形验证码进行认证，适用于需要额外安全验证但不需要输入密码的快速登录场景。

**LoginType**: `captcha`

**步骤1: 获取验证码**

**端点**: `GET /auth/captcha`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..."
  }
}
```

**步骤2: 提交登录**

**请求体**:
```json
{
  "loginType": "captcha",
  "username": "admin",
  "captcha": "ABCD",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**必需字段**:
- `username`: 用户名
- `captcha`: 图形验证码（不区分大小写）
- `uuid`: 验证码UUID

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "message": "验证码登录成功"
  }
}
```

**特性**:
- ✅ 验证码5分钟有效
- ✅ 一次性使用，验证后自动失效
- ✅ 不进行密码策略检查

---

### 3. 手机号登录 (Mobile SMS Login)

使用手机号和短信验证码进行认证，适用于移动端和快速登录场景。

**LoginType**: `mobile_sms`

**步骤1: 发送短信验证码**

**端点**: `POST /auth/sms/send?mobile=13800138000`

**响应示例**:
```json
{
  "code": 200,
  "message": "短信验证码已发送"
}
```

**步骤2: 提交登录**

**请求体**:
```json
{
  "loginType": "mobile_sms",
  "mobile": "13800138000",
  "smsCode": "123456"
}
```

**必需字段**:
- `mobile`: 手机号（已注册）
- `smsCode`: 6位数字短信验证码

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "message": "手机号登录成功"
  }
}
```

**特性**:
- ✅ 短信验证码5分钟有效
- ✅ 一次性使用，验证后自动清除
- ✅ 无需记住密码
- ⚠️ 当前为模拟发送，需集成第三方短信服务

---

### 4. SSO单点登录 (SSO Login)

使用第三方认证服务进行登录，适用于企业内部统一认证场景（OAuth2、SAML、CAS等）。

**LoginType**: `sso`

**状态**: 🚧 框架已实现，需要配置SSO服务提供商

**请求体**:
```json
{
  "loginType": "sso",
  "ssoToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "ssoProvider": "oauth2"
}
```

**必需字段**:
- `ssoToken`: 从SSO服务获取的令牌
- `ssoProvider`: SSO提供商（oauth2, saml, cas等）

**待实现功能**:
1. SSO Token验证
2. 从SSO服务获取用户信息
3. 本地用户映射或自动创建
4. 完成登录流程

---

### 5. Passkey登录 (Passkey Login)

使用WebAuthn/FIDO2标准进行无密码认证，支持生物识别和硬件密钥。

**LoginType**: `passkey`

**状态**: 🚧 框架已实现，需要集成WebAuthn服务端库

**请求体**:
```json
{
  "loginType": "passkey",
  "passkeyCredentialId": "credential-id-base64",
  "passkeyAuthResponse": "{...webauthn-response...}"
}
```

**必需字段**:
- `passkeyCredentialId`: WebAuthn凭证ID
- `passkeyAuthResponse`: WebAuthn认证响应（JSON字符串）

**待实现功能**:
1. 集成webauthn4j或类似库
2. 验证WebAuthn认证响应
3. 验证凭证签名
4. 查找关联用户账号
5. 完成登录流程

---

### 6. TOTP双因素认证 (TOTP Login)

使用时间基准的一次性密码进行二次认证，通常与密码登录配合使用。

**LoginType**: `totp`

**状态**: 🚧 框架已实现，需要集成TOTP库

**请求体**:
```json
{
  "loginType": "totp",
  "username": "admin",
  "password": "Passw0rd",
  "totpCode": "123456"
}
```

**必需字段**:
- `username`: 用户名
- `password`: 密码
- `totpCode`: 6位数字TOTP验证码

**可选字段（用于首次绑定）**:
- `totpSecret`: TOTP密钥

**待实现功能**:
1. 集成google-authenticator或类似库
2. TOTP绑定流程
3. TOTP验证逻辑
4. 防重放检查
5. 完成二次认证流程

---

## 错误响应

### 通用错误

```json
{
  "code": 400,
  "message": "不支持的登录类型"
}
```

### 认证失败

```json
{
  "code": 1006,
  "message": "用户名或密码错误"
}
```

### 验证码错误

```json
{
  "code": 1014,
  "message": "验证码错误或已过期"
}
```

### 账号异常

```json
{
  "code": 1007,
  "message": "账号已被禁用"
}
```

```json
{
  "code": 1009,
  "message": "账号已被锁定，请稍后再试"
}
```

## 其他认证相关接口

### 登出

**端点**: `POST /auth/logout`

**响应**:
```json
{
  "code": 200,
  "message": "退出成功"
}
```

### 获取当前用户信息

**端点**: `GET /auth/userinfo`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "realName": "张三",
    "email": "admin@example.com",
    "mobile": "13800138000",
    "avatar": "https://...",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["system:user:list", "system:user:add"]
  }
}
```

### 修改密码

**端点**: `POST /auth/change-password`

**请求体**:
```json
{
  "oldPassword": "OldPassw0rd",
  "newPassword": "NewPassw0rd",
  "confirmPassword": "NewPassw0rd"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

## 安全建议

1. **HTTPS**: 生产环境必须使用HTTPS
2. **Token管理**: 将token存储在HttpOnly Cookie或安全存储中
3. **Token过期**: 默认2小时，可配置
4. **刷新Token**: 建议实现refresh token机制
5. **速率限制**: 对登录接口实施速率限制
6. **日志审计**: 记录所有登录尝试
7. **IP白名单**: 敏感账号可配置IP白名单

## 客户端示例

### JavaScript (Axios)

```javascript
// 密码登录
const loginWithPassword = async (username, password) => {
  const response = await axios.post('/auth/login', {
    loginType: 'password',
    username,
    password
  });
  const { token } = response.data.data;
  localStorage.setItem('token', token);
  return token;
};

// 手机号登录
const loginWithMobile = async (mobile, smsCode) => {
  const response = await axios.post('/auth/login', {
    loginType: 'mobile_sms',
    mobile,
    smsCode
  });
  const { token } = response.data.data;
  localStorage.setItem('token', token);
  return token;
};

// 发送短信验证码
const sendSmsCode = async (mobile) => {
  await axios.post(`/auth/sms/send?mobile=${mobile}`);
};
```

### cURL

```bash
# 密码登录
curl -X POST https://api.example.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "password",
    "username": "admin",
    "password": "Passw0rd"
  }'

# 手机号登录
# 步骤1: 发送验证码
curl -X POST "https://api.example.com/auth/sms/send?mobile=13800138000"

# 步骤2: 登录
curl -X POST https://api.example.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "mobile_sms",
    "mobile": "13800138000",
    "smsCode": "123456"
  }'
```

## 总结

Woodlin系统提供了灵活且安全的多种登录方式，当前已完全实现密码登录、验证码登录和手机号登录三种方式。SSO、Passkey和TOTP三种方式的框架已就绪，可根据实际需求进行具体实现和配置。
