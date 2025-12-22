# CORS Configuration Implementation Summary

## 实现概述

已为 Woodlin 系统实现了优雅的跨域资源共享（CORS）配置，支持根据不同环境自动切换跨域策略。

## 实现特性

✅ **环境差异化配置**
- **开发环境（dev）**：使用通配符 `*` 允许所有源，方便本地开发和调试
- **测试环境（test）**：使用通配符 `*` 允许所有源，便于集成测试
- **生产环境（prod）**：配置具体域名，保证生产安全性

✅ **优雅的配置结构**
- 使用 `@ConfigurationProperties` 自动绑定配置
- 支持环境变量覆盖配置
- 配置文件清晰易懂，注释完善

✅ **灵活的配置选项**
- 支持 `allowedOrigins`（固定源列表）
- 支持 `allowedOriginPatterns`（支持通配符的源模式，如 `https://*.example.com`）
- 支持配置 HTTP 方法、请求头、响应头
- 支持配置预检请求缓存时间
- 支持启用/禁用凭证携带

✅ **完善的日志输出**
- 应用启动时输出 CORS 配置详情
- 便于排查跨域问题

✅ **单元测试**
- 提供 `CorsPropertiesTest` 测试类
- 验证配置正确加载

## 文件清单

### 核心代码
1. `woodlin-common/src/main/java/com/mumu/woodlin/common/config/CorsProperties.java`
   - CORS 配置属性类

2. `woodlin-common/src/main/java/com/mumu/woodlin/common/config/CorsConfig.java`
   - CORS 配置实现类

### 配置文件
3. `woodlin-admin/src/main/resources/application.yml`
   - 添加默认 CORS 配置节

4. `woodlin-admin/src/main/resources/application-dev.yml`
   - 开发环境 CORS 配置（使用 `*`）

5. `woodlin-admin/src/main/resources/application-test.yml`
   - 测试环境 CORS 配置（使用 `*`）

6. `woodlin-admin/src/main/resources/application-prod.yml`
   - 生产环境 CORS 配置（配置具体域名）

### 测试和文档
7. `woodlin-common/src/test/java/com/mumu/woodlin/common/config/CorsPropertiesTest.java`
   - 单元测试

8. `docs/CORS-Configuration.md`
   - 详细的使用文档

9. `.env.example`
   - 添加 CORS 环境变量配置示例

## 使用示例

### 开发环境启动
```bash
# 使用开发配置（允许所有源）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用脚本
./scripts/dev.sh backend
```

### 生产环境启动
```bash
# 配置环境变量
export CORS_ALLOWED_ORIGIN_1=https://example.com
export CORS_ALLOWED_ORIGIN_2=https://www.example.com

# 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 或使用 Docker Compose（推荐）
docker compose up -d
```

### 动态配置域名
在 `.env` 文件中配置：
```bash
CORS_ENABLED=true
CORS_ALLOWED_ORIGIN_1=https://example.com
CORS_ALLOWED_ORIGIN_2=https://www.example.com
CORS_ALLOW_CREDENTIALS=true
CORS_MAX_AGE=3600
```

## 配置说明

### 开发/测试环境
```yaml
woodlin:
  cors:
    enabled: true
    allowed-origins:
      - "*"  # 允许所有源
    allow-credentials: false  # 使用通配符时必须为 false
```

### 生产环境
```yaml
woodlin:
  cors:
    enabled: true
    allowed-origin-patterns:
      - https://example.com
      - https://www.example.com
      - https://*.example.com  # 支持子域名通配符
    allow-credentials: true  # 配置具体域名时可以为 true
```

## 日志示例

应用启动时会输出 CORS 配置信息：

```
[INFO] 正在配置CORS跨域设置...
[INFO] CORS配置: 允许所有源 (*)
[INFO] CORS配置: 允许的HTTP方法 = [GET, POST, PUT, DELETE, OPTIONS, PATCH]
[INFO] CORS配置: 路径模式 = /**, 预检缓存时间 = 3600秒
[INFO] CORS跨域配置完成
```

## 安全建议

⚠️ **重要提示**

1. **生产环境禁止使用通配符 `*`**
   - 必须配置具体的域名
   - 使用 HTTPS 协议

2. **合理配置 allow-credentials**
   - `allowedOrigins` 使用 `*` 时，`allow-credentials` 必须为 `false`
   - `allowedOriginPatterns` 配置具体域名时，`allow-credentials` 可以为 `true`

3. **限制暴露的响应头**
   - 只暴露必要的响应头
   - 避免泄露敏感信息

4. **合理设置预检缓存时间**
   - 默认 3600 秒（1 小时）
   - 根据实际需求调整

## 验证测试

运行单元测试验证配置：
```bash
mvn test -Dtest=CorsPropertiesTest -pl woodlin-common
```

## 详细文档

完整的使用文档请参考：[docs/CORS-Configuration.md](../docs/CORS-Configuration.md)

## 技术实现

- **Spring Framework**: 使用 `WebMvcConfigurer.addCorsMappings()` 配置 CORS
- **Spring Boot**: 使用 `@ConfigurationProperties` 自动绑定配置
- **条件装配**: 使用 `@ConditionalOnProperty` 支持启用/禁用 CORS
- **日志输出**: 使用 SLF4J 输出配置详情

## 兼容性

- ✅ Spring Boot 3.5.6
- ✅ Java 17+
- ✅ 所有现代浏览器
- ✅ Docker 部署环境
- ✅ 多环境配置（dev/test/prod）

## 后续扩展

如需扩展 CORS 配置，可以：

1. **添加更多环境**：创建 `application-{profile}.yml` 文件
2. **自定义响应头**：修改 `exposedHeaders` 配置
3. **自定义路径模式**：修改 `pathPattern` 配置
4. **集成 API Gateway**：在网关层面配置 CORS

---

**作者**: mumu  
**日期**: 2025-12-22  
**版本**: 1.0.0
