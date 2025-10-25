# 配置说明

本文档详细介绍 Woodlin 项目的配置项，包括后端 Spring Boot 配置和前端环境配置。

## 后端配置

### 配置文件结构

Woodlin 采用 Spring Boot 的多环境配置方案：

```
woodlin-admin/src/main/resources/
├── application.yml              # 主配置文件
├── application-dev.yml          # 开发环境配置
├── application-test.yml         # 测试环境配置
├── application-prod.yml         # 生产环境配置
├── application-system.yml       # 系统模块配置
├── application-security.yml     # 安全模块配置
├── application-tenant.yml       # 租户模块配置
├── application-file.yml         # 文件模块配置
├── application-task.yml         # 任务模块配置
└── logback-spring.xml           # 日志配置
```

### 主配置文件

**`application.yml`**

```yaml
# Spring Boot 配置
spring:
  application:
    name: woodlin-admin
  
  # 环境配置
  profiles:
    active: @spring.profiles.active@  # Maven 动态替换
    include:
      - system    # 引入系统模块配置
      - security  # 引入安全模块配置
      - tenant    # 引入租户模块配置
      - file      # 引入文件模块配置
      - task      # 引入任务模块配置
      - generator # 引入生成器模块配置
      - sql2api   # 引入 SQL2API 模块配置
  
  # Jackson 配置
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
  
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB
      max-request-size: 100MB

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api
  compression:
    enabled: true
  http2:
    enabled: true

# 日志配置
logging:
  level:
    root: INFO
    com.mumu.woodlin: DEBUG
  file:
    name: logs/woodlin.log
    max-size: 10MB
    max-history: 30

# Spring Doc (OpenAPI) 配置
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /doc.html
    tags-sorter: alpha
    operations-sorter: alpha
  group-configs:
    - group: 系统管理
      paths-to-match:
        - /system/**
    - group: 租户管理
      paths-to-match:
        - /tenant/**
    - group: 文件管理
      paths-to-match:
        - /file/**
    - group: 任务调度
      paths-to-match:
        - /task/**
```

### 开发环境配置

**`application-dev.yml`**

```yaml
# 数据源配置
spring:
  datasource:
    dynamic:
      primary: master  # 设置默认数据源
      strict: false    # 严格模式
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
          username: root
          password: root
          # Druid 配置
          druid:
            initial-size: 5
            min-idle: 5
            max-active: 20
            max-wait: 60000
            time-between-eviction-runs-millis: 60000
            min-evictable-idle-time-millis: 300000
            validation-query: SELECT 1
            test-while-idle: true
            test-on-borrow: false
            test-on-return: false
            pool-prepared-statements: false
            max-pool-prepared-statement-per-connection-size: 20
            # 监控配置
            stat-view-servlet:
              enabled: true
              url-pattern: /druid/*
              reset-enable: false
              login-username: admin
              login-password: admin
            web-stat-filter:
              enabled: true
              url-pattern: /*
              exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
            filter:
              stat:
                enabled: true
                log-slow-sql: true
                slow-sql-millis: 1000
              wall:
                enabled: true
                config:
                  multi-statement-allow: true
  
  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 10s
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0

# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.mumu.woodlin.*.entity

# 日志级别
logging:
  level:
    com.mumu.woodlin: DEBUG
    com.mumu.woodlin.*.mapper: DEBUG
```

### 生产环境配置

**`application-prod.yml`**

```yaml
# 数据源配置
spring:
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://${DATABASE_HOST:localhost}:${DATABASE_PORT:3306}/${DATABASE_NAME:woodlin}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: ${DATABASE_USERNAME:root}
          password: ${DATABASE_PASSWORD:root}
          druid:
            initial-size: 10
            min-idle: 10
            max-active: 50
            max-wait: 60000
            # 监控关闭
            stat-view-servlet:
              enabled: false
            web-stat-filter:
              enabled: false
  
  # Redis 配置
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 10s
      lettuce:
        pool:
          max-active: 20
          max-wait: -1ms
          max-idle: 10
          min-idle: 5

# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

# 日志级别
logging:
  level:
    root: INFO
    com.mumu.woodlin: INFO
```

### 模块配置

#### 系统模块配置

**`application-system.yml`**

```yaml
woodlin:
  system:
    # 默认密码
    default-password: Passw0rd
    # 密码加密算法
    password-encoder: bcrypt
    # 密码策略
    password-policy:
      min-length: 8
      max-length: 32
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: false
      expire-days: 90
    # 用户锁定策略
    lock-policy:
      enabled: true
      max-retry-count: 5
      lock-duration: 30  # 分钟
```

#### 安全模块配置

**`application-security.yml`**

```yaml
# Sa-Token 配置
sa-token:
  # Token 名称（同时也是 Cookie 名称）
  token-name: Authorization
  # Token 有效期（单位：秒）-1 代表永不过期
  timeout: 2592000  # 30 天
  # Token 临时有效期（指定时间内无操作就视为 Token 过期）-1 代表禁用
  active-timeout: 1800  # 30 分钟
  # 是否允许同一账号并发登录（为 false 时新登录挤掉旧登录）
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个 Token（为 false 时每次登录新建一个 Token）
  is-share: false
  # Token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
  token-style: uuid
  # 是否输出操作日志
  is-log: false
  # 是否在初始化配置时打印版本字符画
  is-print: false

woodlin:
  security:
    # API 加密配置
    api-encrypt:
      enabled: false
      algorithm: AES  # AES, RSA, SM4
      key: your-secret-key-32-characters!!
    # CORS 配置
    cors:
      allowed-origins:
        - http://localhost:5173
        - http://localhost:3000
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers:
        - "*"
      allow-credentials: true
      max-age: 3600
```

#### 租户模块配置

**`application-tenant.yml`**

```yaml
woodlin:
  tenant:
    # 是否启用多租户
    enabled: true
    # 租户字段名
    tenant-id-column: tenant_id
    # 忽略租户的表
    ignore-tables:
      - sys_dict
      - sys_config
      - sys_log
```

#### 文件模块配置

**`application-file.yml`**

```yaml
woodlin:
  file:
    # 存储类型：local, oss, minio
    storage-type: local
    # 本地存储配置
    local:
      base-path: /data/woodlin/files
      url-prefix: /api/file/download
    # OSS 配置
    oss:
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      access-key-id: ${OSS_ACCESS_KEY_ID:}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET:}
      bucket-name: woodlin
    # MinIO 配置
    minio:
      endpoint: http://localhost:9000
      access-key: ${MINIO_ACCESS_KEY:minioadmin}
      secret-key: ${MINIO_SECRET_KEY:minioadmin}
      bucket-name: woodlin
    # 文件上传限制
    upload:
      max-size: 100MB  # 单文件最大大小
      allowed-extensions:
        - jpg
        - jpeg
        - png
        - gif
        - pdf
        - doc
        - docx
        - xls
        - xlsx
        - zip
```

#### 任务模块配置

**`application-task.yml`**

```yaml
# Quartz 配置
spring:
  quartz:
    job-store-type: jdbc  # 使用数据库存储
    properties:
      org:
        quartz:
          scheduler:
            instanceName: WoodlinScheduler
            instanceId: AUTO
          jobStore:
            class: org.quartz.impl.jdbcjobstore.JobStoreTX
            driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
            tablePrefix: QRTZ_
            isClustered: true  # 开启集群
            clusterCheckinInterval: 10000
            useProperties: false
          threadPool:
            class: org.quartz.simpl.SimpleThreadPool
            threadCount: 10
            threadPriority: 5
            threadsInheritContextClassLoaderOfInitializingThread: true

woodlin:
  task:
    # 是否启用任务调度
    enabled: true
    # 线程池配置
    pool:
      core-size: 10
      max-size: 20
      queue-capacity: 200
```

## 前端配置

### 环境变量

前端使用 Vite 的环境变量配置：

**.env（通用配置）**

```properties
# 应用标题
VITE_APP_TITLE=Woodlin 管理系统

# API 基础路径
VITE_API_BASE_URL=/api

# 是否启用 Mock
VITE_USE_MOCK=false
```

**.env.development（开发环境）**

```properties
# 开发环境配置
NODE_ENV=development

# API 地址
VITE_API_BASE_URL=http://localhost:8080/api

# 是否开启代理
VITE_USE_PROXY=true

# 代理配置
VITE_PROXY_TARGET=http://localhost:8080
```

**.env.production（生产环境）**

```properties
# 生产环境配置
NODE_ENV=production

# API 地址
VITE_API_BASE_URL=/api

# 是否开启代理
VITE_USE_PROXY=false

# 构建路径
VITE_PUBLIC_PATH=/
```

### Vite 配置

**`vite.config.ts`**

```typescript
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  
  return {
    plugins: [vue()],
    
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: false,
      proxy: env.VITE_USE_PROXY === 'true' ? {
        '/api': {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      } : undefined
    },
    
    build: {
      outDir: 'dist',
      sourcemap: false,
      chunkSizeWarningLimit: 1500,
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor': ['vue', 'vue-router', 'pinia'],
            'naive-ui': ['naive-ui']
          }
        }
      }
    }
  }
})
```

## Docker 配置

### Docker Compose

**`docker-compose.yml`**

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: woodlin-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DATABASE_PASSWORD:-root}
      MYSQL_DATABASE: ${DATABASE_NAME:-woodlin}
    ports:
      - "${DATABASE_PORT:-3306}:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    networks:
      - woodlin-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
  
  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: woodlin-redis
    ports:
      - "${REDIS_PORT:-6379}:6379"
    volumes:
      - redis-data:/data
    command: redis-server --requirepass ${REDIS_PASSWORD:-}
    networks:
      - woodlin-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
  
  # 后端应用
  backend:
    build:
      context: .
      dockerfile: scripts/Dockerfile
    container_name: woodlin-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_HOST: mysql
      DATABASE_PORT: 3306
      DATABASE_NAME: ${DATABASE_NAME:-woodlin}
      DATABASE_USERNAME: root
      DATABASE_PASSWORD: ${DATABASE_PASSWORD:-root}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD:-}
      SERVER_PORT: ${SERVER_PORT:-8080}
    ports:
      - "${SERVER_PORT:-8080}:8080"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - woodlin-network
    volumes:
      - app-logs:/app/logs
      - app-data:/data/woodlin
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
  
  # 前端应用
  frontend:
    image: nginx:alpine
    container_name: woodlin-frontend
    ports:
      - "${FRONTEND_PORT:-3000}:80"
    volumes:
      - ./woodlin-web/dist:/usr/share/nginx/html
      - ./scripts/nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - backend
    networks:
      - woodlin-network

networks:
  woodlin-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
  app-logs:
  app-data:
```

### 环境变量文件

**`.env.example`**

```properties
# 数据库配置
DATABASE_NAME=woodlin
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password
DATABASE_HOST=localhost
DATABASE_PORT=3306

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# 应用配置
SERVER_PORT=8080
FRONTEND_PORT=3000

# 文件存储配置
STORAGE_TYPE=local

# OSS 配置（如果使用 OSS）
OSS_ACCESS_KEY_ID=
OSS_ACCESS_KEY_SECRET=
OSS_BUCKET_NAME=

# MinIO 配置（如果使用 MinIO）
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=woodlin
```

## 配置优先级

配置加载优先级（从高到低）：

```mermaid
graph LR
    A[命令行参数] --> B[环境变量]
    B --> C[application-{profile}.yml]
    C --> D[application.yml]
    D --> E[application-{module}.yml]
    E --> F[默认值]
    
    style A fill:#ff6b6b
    style B fill:#ffa500
    style C fill:#ffd700
    style D fill:#90ee90
    style E fill:#87ceeb
    style F fill:#dda0dd
```

1. **命令行参数**：`--server.port=8081`
2. **环境变量**：`SERVER_PORT=8081`
3. **Profile 配置文件**：`application-dev.yml`
4. **主配置文件**：`application.yml`
5. **模块配置文件**：`application-system.yml`
6. **默认值**：代码中的 `@Value` 默认值

## 配置最佳实践

### 1. 敏感信息使用环境变量

::: code-tabs

@tab ❌ 错误做法

```yaml
spring:
  datasource:
    password: my_secret_password  # 不要直接写在配置文件中
```

@tab ✅ 正确做法

```yaml
spring:
  datasource:
    password: ${DATABASE_PASSWORD}  # 使用环境变量
```

:::

### 2. 区分环境配置

- **开发环境**：详细日志、SQL 输出、Druid 监控
- **生产环境**：简洁日志、关闭调试功能、优化连接池

### 3. 使用配置中心（可选）

对于大型项目，建议使用配置中心：

- Spring Cloud Config
- Nacos
- Apollo

### 4. 配置加密

敏感配置可以使用 Jasypt 加密：

```yaml
spring:
  datasource:
    password: ENC(encrypted_password)  # 加密后的密码
```

## 总结

合理的配置管理能够：

1. **提高安全性**：敏感信息不写入代码
2. **便于维护**：集中管理配置
3. **灵活切换环境**：一套代码多环境部署
4. **支持动态配置**：无需重启即可更新配置

---

::: tip 配置建议
- 开发环境使用本地配置文件
- 生产环境使用环境变量
- 敏感配置必须加密
- 定期审查配置项
:::

::: info 相关文档
- [快速开始](/guide/getting-started) - 了解如何配置项目
- [部署指南](/deployment/overview) - 学习生产环境配置
- [Docker 部署](/deployment/docker) - 容器化部署配置
:::
