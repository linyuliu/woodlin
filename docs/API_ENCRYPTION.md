# API 加密配置文档

## 概述

Woodlin 系统支持灵活的 API 接口加密功能，可以对特定的接口进行请求和响应数据的加密处理，保障敏感数据的传输安全。

## 特性

- ✅ 支持多种加密算法（AES、RSA、SM4）
- ✅ 灵活的接口匹配规则（Ant 风格通配符）
- ✅ 可配置请求/响应加密
- ✅ 支持排除特定接口
- ✅ 国密算法支持（SM4）
- ✅ 完全可配置化

## 加密算法说明

### AES（推荐用于大数据量）

**优点**：对称加密，速度快，适合大数据量加密，CPU 消耗较低

**推荐场景**：接口数据加密、文件加密、大批量数据传输

**配置示例**：
```yaml
woodlin:
  api:
    encryption:
      enabled: true
      algorithm: AES
      aes-key: "your-base64-encoded-key"
      aes-iv: "your-base64-encoded-iv"
      aes-mode: CBC
      aes-padding: PKCS5Padding
```

### RSA（推荐用于敏感信息）

**优点**：非对称加密，安全性高，适合密钥交换

**推荐场景**：密码传输、敏感信息加密、数字签名

**配置示例**：
```yaml
woodlin:
  api:
    encryption:
      enabled: true
      algorithm: RSA
      rsa-public-key: "your-public-key-base64"
      rsa-private-key: "your-private-key-base64"
      rsa-key-size: 2048
```

### SM4（国密标准）

**优点**：符合国密标准，适合政府及金融行业

**推荐场景**：需要国密合规的场景、政府项目、金融行业

**配置示例**：
```yaml
woodlin:
  api:
    encryption:
      enabled: true
      algorithm: SM4
      sm4-key: "your-base64-encoded-key"
      sm4-iv: "your-base64-encoded-iv"
      sm4-mode: CBC
```

## 完整配置示例

```yaml
woodlin:
  api:
    encryption:
      # 是否启用加密功能
      enabled: false
      # 加密算法类型：AES, RSA, SM4
      algorithm: AES
      # 需要加密的接口路径模式（支持 Ant 风格通配符）
      include-patterns:
        - /api/user/**
        - /api/payment/**
      # 排除加密的接口（优先级高于 include-patterns）
      exclude-patterns:
        - /auth/login
        - /auth/logout
      # 是否加密请求体
      encrypt-request: true
      # 是否加密响应体
      encrypt-response: true
```

详细文档请参考项目 Wiki。
