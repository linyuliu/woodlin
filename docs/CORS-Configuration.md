# CORS 跨域配置说明

## 概述

Woodlin 系统已经实现了优雅的 CORS（跨域资源共享）配置，支持根据不同环境（dev、test、prod）灵活配置跨域策略。

## 配置结构

### 1. 配置类

- **CorsProperties.java**: CORS 配置属性类，使用 `@ConfigurationProperties` 注解自动绑定配置
- **CorsConfig.java**: CORS 配置类，实现 `WebMvcConfigurer` 接口，配置跨域映射

### 2. 配置文件

#### 开发环境（application-dev.yml）
```yaml
woodlin:
  cors:
    enabled: true
    allowed-origins:
      - "*"  # 允许所有源
    allow-credentials: false  # 使用通配符时必须为 false
```

#### 测试环境（application-test.yml）
```yaml
woodlin:
  cors:
    enabled: true
    allowed-origins:
      - "*"  # 允许所有源
    allow-credentials: false  # 使用通配符时必须为 false
```

#### 生产环境（application-prod.yml）
```yaml
woodlin:
  cors:
    enabled: true
    allowed-origin-patterns:
      - ${CORS_ALLOWED_ORIGIN_1:https://example.com}
      - ${CORS_ALLOWED_ORIGIN_2:https://www.example.com}
    allow-credentials: true  # 配置具体域名时可以为 true
```

## 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | Boolean | true | 是否启用 CORS 跨域 |
| `allowed-origins` | List<String> | - | 允许的源（Origin），支持通配符 `*` |
| `allowed-origin-patterns` | List<String> | - | 允许的源模式，支持通配符（如 `https://*.example.com`） |
| `allowed-methods` | List<String> | GET, POST, PUT, DELETE, OPTIONS, PATCH | 允许的 HTTP 方法 |
| `allowed-headers` | List<String> | * | 允许的请求头 |
| `exposed-headers` | List<String> | Authorization, Content-Type | 暴露的响应头 |
| `allow-credentials` | Boolean | true | 是否允许携带凭证（cookies） |
| `max-age` | Long | 3600 | 预检请求的缓存时间（秒） |
| `path-pattern` | String | /** | CORS 配置应用的路径模式 |

## 使用指南

### 开发和测试环境

开发和测试环境使用通配符 `*` 允许所有源，便于开发调试：

```bash
# 启动开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用脚本
./scripts/dev.sh backend
```

**注意**：使用通配符 `*` 时，`allow-credentials` 必须设置为 `false`。

### 生产环境

生产环境配置具体的域名，保证安全性。可以通过环境变量或 `.env` 文件配置：

```bash
# .env 文件配置
CORS_ENABLED=true
CORS_ALLOWED_ORIGIN_1=https://example.com
CORS_ALLOWED_ORIGIN_2=https://www.example.com
CORS_ALLOW_CREDENTIALS=true
CORS_MAX_AGE=3600
```

然后启动应用：

```bash
# 使用 Docker Compose
docker compose up -d

# 或直接运行
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 动态添加域名

如果需要添加更多允许的域名，可以在 `application-prod.yml` 中添加：

```yaml
woodlin:
  cors:
    allowed-origin-patterns:
      - ${CORS_ALLOWED_ORIGIN_1:https://example.com}
      - ${CORS_ALLOWED_ORIGIN_2:https://www.example.com}
      - ${CORS_ALLOWED_ORIGIN_3:https://admin.example.com}  # 新增
      - ${CORS_ALLOWED_ORIGIN_4:https://api.example.com}    # 新增
```

然后在 `.env` 文件中配置：

```bash
CORS_ALLOWED_ORIGIN_3=https://admin.example.com
CORS_ALLOWED_ORIGIN_4=https://api.example.com
```

### 禁用 CORS

如果需要禁用 CORS，可以在配置文件中设置：

```yaml
woodlin:
  cors:
    enabled: false
```

或通过环境变量：

```bash
CORS_ENABLED=false
```

## 安全建议

1. **生产环境禁止使用通配符 `*`**：明确配置允许的域名，避免安全风险。

2. **使用 HTTPS**：生产环境的域名应使用 HTTPS 协议。

3. **合理配置 allow-credentials**：
   - 使用 `allowed-origins` 配置通配符 `*` 时，必须将 `allow-credentials` 设置为 `false`
   - 使用 `allowed-origin-patterns` 配置具体域名时，可以将 `allow-credentials` 设置为 `true`

4. **限制暴露的响应头**：只暴露必要的响应头，避免泄露敏感信息。

5. **合理设置预检缓存时间**：`max-age` 设置过长可能导致配置更新不及时，设置过短会增加预检请求次数。

## 测试

已提供 `CorsPropertiesTest` 测试类，可以验证 CORS 配置是否正确加载：

```bash
# 运行测试
mvn test -Dtest=CorsPropertiesTest
```

## 日志

CORS 配置加载时会输出日志，可以通过日志查看配置是否生效：

```
[INFO] 正在配置CORS跨域设置...
[INFO] CORS配置: 允许所有源 (*)
[INFO] CORS配置: 允许的HTTP方法 = [GET, POST, PUT, DELETE, OPTIONS, PATCH]
[INFO] CORS配置: 路径模式 = /**, 预检缓存时间 = 3600秒
[INFO] CORS跨域配置完成
```

## 常见问题

### 1. 前端请求仍然被 CORS 阻止？

检查以下几点：
- 确认 `woodlin.cors.enabled=true`
- 确认前端请求的源（Origin）在允许列表中
- 确认请求方法在 `allowed-methods` 中
- 检查浏览器控制台的详细错误信息

### 2. 使用通配符 `*` 时无法携带凭证？

这是浏览器的安全限制。使用通配符 `*` 时，必须将 `allow-credentials` 设置为 `false`。如果需要携带凭证，请使用 `allowed-origin-patterns` 配置具体域名。

### 3. 如何支持子域名通配符？

使用 `allowed-origin-patterns` 而不是 `allowed-origins`：

```yaml
woodlin:
  cors:
    allowed-origin-patterns:
      - https://*.example.com  # 匹配所有 example.com 的子域名
```

## 相关文件

- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/CorsProperties.java`
- `woodlin-common/src/main/java/com/mumu/woodlin/common/config/CorsConfig.java`
- `woodlin-admin/src/main/resources/application-dev.yml`
- `woodlin-admin/src/main/resources/application-test.yml`
- `woodlin-admin/src/main/resources/application-prod.yml`
- `woodlin-common/src/test/java/com/mumu/woodlin/common/config/CorsPropertiesTest.java`
