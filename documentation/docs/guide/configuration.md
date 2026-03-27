# 配置说明

本文档说明当前 Woodlin 的配置入口和推荐配置方式。重构后，主运行入口已经移动到 `woodlin-apps/woodlin-admin`，应用基础配置与业务环境配置也分成了不同层次。

## 配置入口

## 主应用基础配置

基础入口文件位于：

```text
woodlin-apps/woodlin-admin/src/main/resources/
├── application.yml
├── .env.example
└── logback-spring.xml
```

其中：

- `application.yml`：应用名、运行 profile、Nacos 配置入口
- `.env.example`：环境变量示例
- `logback-spring.xml`：日志配置

## 当前 `application.yml`

当前主应用仅保留基础引导配置，不再像旧结构那样堆放大量 `application-{module}.yml`：

```yaml
spring:
  application:
    name: ${APP_NAME:woodlin-admin}

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  cloud:
    nacos:
      server-addr: ${NACOS_SERVER_ADDR:mumuhk.oldletter.cn:8848}
      config:
        namespace: ${NACOS_NAMESPACE:mumu}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        refresh-enabled: ${NACOS_REFRESH_ENABLED:true}

  config:
    import:
      - optional:nacos:application-${spring.profiles.active}.yml
```

含义是：

- `woodlin-admin` 自身只保留“如何启动”和“去哪里拿配置”
- 业务环境配置默认从 Nacos 导入
- 配置文件按环境拆成 `application-dev.yml`、`application-prod.yml` 这类公共配置，而不是继续在 app 目录里按模块拆文件

## 推荐配置层次

推荐把配置分成三类：

1. `woodlin-apps/woodlin-admin/src/main/resources/application.yml`
   只保留应用名、环境变量入口、Nacos 导入配置。
2. Nacos 或外部配置中心中的 `application-{profile}.yml`
   存放数据库、Redis、Sa-Token、业务开关等运行配置。
3. 环境变量
   覆盖部署时差异项，例如 `SPRING_PROFILES_ACTIVE`、`NACOS_SERVER_ADDR`、数据库密码等。

## 主数据源：Druid

主数据源和 `dynamic-datasource` 的核心链路固定走 Druid。  
推荐配置如下：

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          type: com.alibaba.druid.pool.DruidDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/woodlin
          username: root
          password: Passw0rd
          druid:
            initial-size: 5
            min-idle: 5
            max-active: 20
            max-wait: 60000
            validation-query: SELECT 1
            test-while-idle: true
            stat-view-servlet:
              enabled: true
              url-pattern: /druid/*
            web-stat-filter:
              enabled: true
              url-pattern: /*
              exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
```

当前 app 层中与 Druid 相关的运行时代码都保留在：

- `woodlin-apps/woodlin-admin/src/main/java/com/mumu/woodlin/admin/config/DruidConfiguration.java`
- `woodlin-apps/woodlin-admin/src/main/java/com/mumu/woodlin/admin/interceptor/DruidAdRemovalConfiguration.java`

因此 `/druid/*` 监控页仍然由 `woodlin-admin` 托管。

## 子模块小连接池：HikariCP

以下场景继续使用 HikariCP，而不是复用主链路 Druid：

- 外部数据源连通性校验
- 元数据提取
- SQL2API 的短生命周期目标库连接
- ETL 执行时的临时连接

当前可见实现位置：

- `woodlin-modules/woodlin-module-datasource/src/main/java/.../DatabaseMetadataService.java`
- `woodlin-modules/woodlin-module-datasource/src/main/java/.../InfraDatasourceService.java`
- `woodlin-plugins/woodlin-plugin-sql2api/src/main/java/.../Sql2ApiDataSourceService.java`

这类连接池由模块内部代码按需创建和关闭，不作为主应用默认池暴露到公共配置里。

## MyBatis-Plus 配置

当前 ORM 仍然固定为 MyBatis-Plus，但专属能力已经下沉到 `woodlin-common-mp`。  
推荐的公共配置示例：

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/**/*Mapper.xml
```

约束：

- Controller / Service 对外返回 `PageResult`
- 不再继续把 `IPage` 暴露到公共接口
- MP 专属适配集中在 `woodlin-common-mp`

## Redis 与 Redisson

Redis 和 Redisson 相关公共配置已经集中到 `woodlin-common-db`，常见配置如下：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 10s
```

如果使用 Redisson，可补充：

```yaml
woodlin:
  redisson:
    enabled: true
```

具体连接属性可继续交给 Spring Redis 配置或 Redisson 自身参数承接。

## Nacos 配置

当前 `woodlin-admin` 会通过 `spring.config.import` 按 profile 拉取 Nacos 公共配置。常用环境变量：

```bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=public
export NACOS_GROUP=DEFAULT_GROUP
export NACOS_REFRESH_ENABLED=true
```

建议把以下配置放到 Nacos 的 `application-{profile}.yml`：

- 主数据源 Druid
- Redis / Redisson
- Sa-Token
- 多租户、文件、任务等业务开关
- OpenAPI / Knife4j

## OpenAPI 与文档

OpenAPI / Knife4j 的公共 Web 能力已经归入 `woodlin-common-web`，app 入口只负责实际运行时依赖。  
常见访问地址：

- `/api/doc.html`
- `/api/v3/api-docs`

## 配置建议

1. 主应用资源目录只放基础启动配置，不再往 app 模块里堆业务配置文件。
2. 主链路数据库统一 Druid，子模块内部短连接继续 HikariCP。
3. MyBatis-Plus 专属参数只在 `woodlin-common-mp` 及外部配置中维护，不向 `common-core` 扩散。
4. 敏感信息优先走环境变量或配置中心，不直接写死在仓库配置里。

::: tip
如果后续新增 `woodlin-app-gateway`、`woodlin-app-auth` 等入口，建议沿用同样的配置策略：app 只引导，业务配置集中到外部配置中心。
:::
