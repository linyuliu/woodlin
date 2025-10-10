# API 加密配置文档

## 概述

Woodlin 系统提供灵活的 API 接口加密功能，支持对指定接口的请求和响应数据进行加密处理，确保敏感数据的传输安全。

## 功能特性

- ✅ 支持多种加密算法（AES、RSA、SM4 国密）
- ✅ 灵活的接口匹配规则（支持 Ant 风格通配符）
- ✅ 可独立配置请求和响应加密
- ✅ 支持排除特定接口（白名单机制）
- ✅ 完全配置化，无需修改代码
- ✅ 支持环境变量配置

## 加密算法说明

### AES（推荐用于大数据量）

**算法特点**：对称加密，加解密速度快，适合大数据量场景，CPU 消耗低

**适用场景**：
- API 接口数据加密
- 文件加密传输
- 大批量数据传输

**配置示例**：

::: code-tabs#config

@tab YAML 配置

```yaml
woodlin:
  api:
    encryption:
      # 启用加密功能
      enabled: true
      # 选择 AES 算法
      algorithm: AES
      # AES 密钥（Base64 编码）
      aes-key: "your-base64-encoded-key"
      # AES 初始化向量（Base64 编码）
      aes-iv: "your-base64-encoded-iv"
      # 加密模式：CBC（推荐）、ECB、CFB、OFB、CTR
      aes-mode: CBC
      # 填充方式：PKCS5Padding（推荐）、PKCS7Padding、NoPadding
      aes-padding: PKCS5Padding
```

@tab 环境变量

```bash
# 启用加密
export WOODLIN_API_ENCRYPTION_ENABLED=true
# 选择算法
export WOODLIN_API_ENCRYPTION_ALGORITHM=AES
# AES 密钥
export WOODLIN_API_ENCRYPTION_AES_KEY="your-base64-encoded-key"
# AES IV
export WOODLIN_API_ENCRYPTION_AES_IV="your-base64-encoded-iv"
```

:::

### RSA（推荐用于敏感信息）

**算法特点**：非对称加密，安全性高，适合密钥交换和数字签名

**适用场景**：
- 用户密码传输
- 敏感信息加密
- 数字签名验证
- 密钥协商

**配置示例**：

::: code-tabs#config

@tab YAML 配置

```yaml
woodlin:
  api:
    encryption:
      # 启用加密功能
      enabled: true
      # 选择 RSA 算法
      algorithm: RSA
      # RSA 公钥（Base64 编码）
      rsa-public-key: "your-public-key-base64"
      # RSA 私钥（Base64 编码）
      rsa-private-key: "your-private-key-base64"
      # 密钥长度：1024、2048（推荐）、4096
      rsa-key-size: 2048
```

@tab 环境变量

```bash
export WOODLIN_API_ENCRYPTION_ENABLED=true
export WOODLIN_API_ENCRYPTION_ALGORITHM=RSA
export WOODLIN_API_ENCRYPTION_RSA_PUBLIC_KEY="your-public-key-base64"
export WOODLIN_API_ENCRYPTION_RSA_PRIVATE_KEY="your-private-key-base64"
```

:::

### SM4（国密标准）

**算法特点**：中国商用密码标准，符合国家密码管理局要求

**适用场景**：
- 政府项目
- 金融行业
- 需要国密合规的场景
- 涉密信息系统

**配置示例**：

::: code-tabs#config

@tab YAML 配置

```yaml
woodlin:
  api:
    encryption:
      # 启用加密功能
      enabled: true
      # 选择 SM4 国密算法
      algorithm: SM4
      # SM4 密钥（Base64 编码）
      sm4-key: "your-base64-encoded-key"
      # SM4 初始化向量（Base64 编码）
      sm4-iv: "your-base64-encoded-iv"
      # 加密模式：CBC（推荐）、ECB
      sm4-mode: CBC
```

@tab 环境变量

```bash
export WOODLIN_API_ENCRYPTION_ENABLED=true
export WOODLIN_API_ENCRYPTION_ALGORITHM=SM4
export WOODLIN_API_ENCRYPTION_SM4_KEY="your-base64-encoded-key"
export WOODLIN_API_ENCRYPTION_SM4_IV="your-base64-encoded-iv"
```

:::

## 完整配置示例

```yaml
woodlin:
  api:
    encryption:
      # ========== 基础配置 ==========
      # 是否启用加密功能
      enabled: false
      # 加密算法类型：AES（推荐）、RSA、SM4
      algorithm: AES
      
      # ========== 接口配置 ==========
      # 需要加密的接口路径模式（支持 Ant 风格通配符）
      include-patterns:
        - /api/user/**       # 用户相关接口
        - /api/payment/**    # 支付相关接口
        - /api/sensitive/**  # 敏感数据接口
      
      # 排除加密的接口（优先级高于 include-patterns）
      exclude-patterns:
        - /api/auth/login    # 登录接口
        - /api/auth/logout   # 登出接口
        - /api/public/**     # 公开接口
      
      # ========== 加密范围 ==========
      # 是否加密请求体
      encrypt-request: true
      # 是否加密响应体
      encrypt-response: true
```

## 密钥生成

### Java 代码生成

```java
import com.mumu.woodlin.common.util.ApiEncryptionUtil;

// 生成 AES 密钥（256位）
String aesKey = ApiEncryptionUtil.generateAesKey(256);
String aesIv = ApiEncryptionUtil.generateAesKey(128);

// 生成 RSA 密钥对（2048位）
String[] rsaKeys = ApiEncryptionUtil.generateRsaKeyPair(2048);
String publicKey = rsaKeys[0];
String privateKey = rsaKeys[1];

// 生成 SM4 密钥（128位）
String sm4Key = ApiEncryptionUtil.generateSm4Key();
String sm4Iv = ApiEncryptionUtil.generateSm4Key();
```

### OpenSSL 命令生成

```bash
# 生成 AES 密钥（Base64 编码）
openssl rand -base64 32

# 生成 RSA 密钥对
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为 Base64 单行格式
cat private_key.pem | base64 | tr -d '\n'
cat public_key.pem | base64 | tr -d '\n'
```

## 最佳实践

### 安全建议

1. **密钥管理**
   - 密钥应存储在安全的配置中心或密钥管理系统
   - 不要将密钥硬编码在代码中
   - 定期轮换密钥（建议每 3-6 个月）
   - 使用强随机数生成器生成密钥

2. **算法选择**
   - 大数据量传输：选择 AES
   - 敏感信息传输：选择 RSA
   - 国密合规要求：选择 SM4
   - 混合使用：RSA 交换密钥 + AES 加密数据

3. **接口配置**
   - 仅对必要的接口启用加密
   - 使用 exclude-patterns 排除公开接口
   - 登录接口可使用单独的加密策略

### 性能优化

1. **缓存策略**
   - 对加密密钥进行缓存，避免重复初始化
   - 使用连接池复用加密对象

2. **异步处理**
   - 对大文件加密采用流式处理
   - 使用异步加密避免阻塞主线程

## 故障排查

### 常见问题

**问题 1：加密功能未生效**

```bash
# 检查配置是否正确
- 确认 enabled 设置为 true
- 确认 include-patterns 包含目标接口
- 检查是否在 exclude-patterns 中被排除
```

**问题 2：密钥格式错误**

```bash
# 确保密钥是有效的 Base64 编码
# 验证密钥格式
echo "your-key" | base64 -d > /dev/null && echo "Valid" || echo "Invalid"
```

**问题 3：解密失败**

```bash
# 检查以下配置
- AES/SM4: 确认 key 和 iv 与加密时一致
- RSA: 确认使用配对的公钥和私钥
- 确认加密模式和填充方式一致
```

## 相关文档

- [系统配置管理文档](./SYSTEM_CONFIG.md) - 统一的配置管理界面
- [实现总结文档](./IMPLEMENTATION_SUMMARY.md) - 加密功能的实现细节
