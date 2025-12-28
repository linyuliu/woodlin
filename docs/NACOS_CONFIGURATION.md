# Nacos 配置中心集成指南

## 概述

本项目已集成 Nacos 配置中心，支持动态配置管理和多环境配置隔离。通过 Nacos 配置中心，可以实现配置的集中管理、动态刷新和版本控制。

## 版本信息

- Spring Boot: 3.5.6
- Spring Cloud: 2023.0.5
- Spring Cloud Alibaba: 2023.0.3.3
- Nacos: 2.x

## 配置文件结构

### 本地配置文件（bootstrap.yml）

bootstrap.yml 文件包含 Nacos 连接信息和应用启动的基础配置，在应用启动时最先加载。

**位置**: `woodlin-admin/src/main/resources/bootstrap.yml`

**主要配置项**:
- Nacos 服务器地址
- Nacos 命名空间和分组
- 共享配置列表
- 扩展配置列表

### Nacos 配置中心配置

将大部分业务配置迁移到 Nacos 配置中心，按功能模块划分为多个配置文件：

1. **woodlin-datasource.yml** - 数据库配置
2. **woodlin-redis.yml** - Redis 配置
3. **woodlin-mybatis.yml** - MyBatis Plus 配置
4. **woodlin-sa-token.yml** - Sa-Token 认证配置
5. **woodlin-knife4j.yml** - API 文档配置
6. **woodlin-business.yml** - 业务配置（安全、缓存、加密等）
7. **woodlin-admin-{profile}.yml** - 环境特定配置

## 环境配置

### 开发环境 (dev)

使用本地 Nacos 服务器，默认端口 8848。

**环境变量**:
```bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=
export NACOS_GROUP=DEFAULT_GROUP
```

### 测试环境 (test)

使用专用测试命名空间，实现环境隔离。

**环境变量**:
```bash
export SPRING_PROFILES_ACTIVE=test
export NACOS_SERVER_ADDR=nacos-test.example.com:8848
export NACOS_NAMESPACE=test
export NACOS_GROUP=TEST_GROUP
```

### 生产环境 (prod)

使用专用生产命名空间，开启鉴权，支持 Nacos 集群。

**环境变量**:
```bash
export SPRING_PROFILES_ACTIVE=prod
export NACOS_SERVER_ADDR=nacos1.example.com:8848,nacos2.example.com:8848,nacos3.example.com:8848
export NACOS_NAMESPACE=production
export NACOS_GROUP=PROD_GROUP
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=your_secure_password
```

## 配置迁移说明

### 保留在本地的配置

以下配置保留在本地 application.yml 中：

1. **服务器基础配置** - 端口、上下文路径
2. **Spring 核心配置** - banner、主配置等
3. **Jackson 序列化配置** - 时区、日期格式
4. **Spring Boot Actuator 配置** - 健康检查端点
5. **分页默认配置** - 默认页码、页面大小

### 迁移到 Nacos 的配置

以下配置迁移到 Nacos 配置中心：

1. **数据库配置** - 连接池、数据源
2. **Redis 配置** - 连接信息、连接池
3. **MyBatis Plus 配置** - mapper 路径、全局配置
4. **Sa-Token 配置** - token 策略、超时时间
5. **Knife4j 配置** - API 文档、认证配置
6. **Woodlin 业务配置** - 安全策略、缓存配置、加密配置等

## Nacos 配置示例

详细的配置示例请参见：

- `docs/nacos-configs/woodlin-datasource.yml` - 数据库配置示例
- `docs/nacos-configs/woodlin-redis.yml` - Redis 配置示例
- `docs/nacos-configs/woodlin-mybatis.yml` - MyBatis Plus 配置示例
- `docs/nacos-configs/woodlin-sa-token.yml` - Sa-Token 配置示例
- `docs/nacos-configs/woodlin-knife4j.yml` - Knife4j 配置示例
- `docs/nacos-configs/woodlin-business.yml` - 业务配置示例
- `docs/nacos-configs/woodlin-admin-dev.yml` - 开发环境特定配置示例

## 快速开始

### 1. 启动 Nacos 服务器

**使用 Docker 启动**:
```bash
docker run -d \
  --name nacos-server \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.4.3
```

访问 Nacos 控制台: http://localhost:8848/nacos
默认账号密码: nacos/nacos

### 2. 导入配置到 Nacos

在 Nacos 控制台中，依次创建以下配置文件（Data ID）:

1. 进入 **配置管理 > 配置列表**
2. 点击 **+** 创建配置
3. 按照 `docs/nacos-configs/` 目录下的示例文件创建配置

**注意**: 
- Data ID 必须与 bootstrap.yml 中配置的一致
- Group 选择 DEFAULT_GROUP（或自定义）
- 配置格式选择 YAML

### 3. 配置环境变量

```bash
# 必需的环境变量
export NACOS_SERVER_ADDR=localhost:8848
export SPRING_PROFILES_ACTIVE=dev

# 可选的环境变量（如果 Nacos 开启了鉴权）
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos
```

### 4. 启动应用

```bash
# 使用 Maven
mvn spring-boot:run -pl woodlin-admin

# 或使用开发脚本
./scripts/dev.sh backend
```

应用启动时会自动从 Nacos 获取配置。

## 配置刷新

Nacos 支持配置的动态刷新，当配置在 Nacos 控制台中修改后，应用会自动接收最新配置。

**注意**: 
- 在 bootstrap.yml 的共享配置和扩展配置中，`refresh: true` 表示该配置支持动态刷新
- 使用 `@RefreshScope` 注解的 Bean 会在配置更新后自动刷新
- 某些配置（如数据源、连接池等）可能需要重启应用才能生效

## 配置优先级

配置的加载优先级（从高到低）：

1. 命令行参数
2. 环境变量
3. Nacos 扩展配置（环境特定配置）
4. Nacos 共享配置
5. 本地 application-{profile}.yml
6. 本地 application.yml
7. bootstrap-{profile}.yml
8. bootstrap.yml

## 禁用 Nacos 配置中心

如果需要临时禁用 Nacos 配置中心（例如在本地开发调试时），可以设置环境变量：

```bash
export NACOS_CONFIG_ENABLED=false
```

或在启动命令中添加参数：

```bash
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.arguments="--spring.cloud.nacos.config.enabled=false"
```

禁用后，应用将只使用本地配置文件。

## 最佳实践

### 1. 环境隔离

- **开发环境**: 使用 public 命名空间或不设置命名空间
- **测试环境**: 使用 test 命名空间
- **生产环境**: 使用 production 命名空间

### 2. 配置分组

使用 Group 来区分不同的应用或服务：
- DEFAULT_GROUP: 默认分组
- TEST_GROUP: 测试分组
- PROD_GROUP: 生产分组

### 3. 敏感信息加密

对于数据库密码、密钥等敏感信息，建议：
- 使用 Nacos 的配置加密功能
- 或使用 Spring Cloud Config 的加密功能
- 或使用专用的密钥管理服务（如 Vault）

### 4. 配置版本管理

- 在 Nacos 控制台中修改配置前，使用历史版本功能备份当前配置
- 重要配置变更应该进行测试后再发布到生产环境
- 利用 Nacos 的灰度发布功能进行配置的逐步更新

### 5. 监控和告警

- 监控 Nacos 服务器的健康状态
- 配置 Nacos 的告警规则，及时发现配置问题
- 在应用中添加配置刷新的日志记录

## 故障排查

### 问题 1: 应用启动失败，提示无法连接 Nacos

**解决方案**:
1. 检查 Nacos 服务器是否正常运行
2. 验证 NACOS_SERVER_ADDR 配置是否正确
3. 检查网络连接和防火墙设置
4. 查看应用日志获取详细错误信息

### 问题 2: 配置未生效

**解决方案**:
1. 确认 Data ID、Group、命名空间配置正确
2. 检查配置格式是否为 YAML
3. 确认 bootstrap.yml 中的配置列表是否包含该配置
4. 重启应用以强制重新加载配置

### 问题 3: 配置无法动态刷新

**解决方案**:
1. 确认配置中 `refresh: true` 已设置
2. 检查使用配置的 Bean 是否添加了 `@RefreshScope` 注解
3. 某些系统级配置（如数据源）无法动态刷新，需要重启应用

## 参考资料

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba Nacos Config](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config)
- [Spring Cloud Bootstrap 配置](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#the-bootstrap-application-context)

## 技术支持

如有问题或建议，请提交 Issue 或联系项目维护者。
