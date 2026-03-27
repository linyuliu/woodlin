# 目录结构

本文档描述当前仓库的真实目录结构。Woodlin 已从旧的单层模块布局调整为 `common / modules / plugins / apps` 四类聚合器，目的是让单体和未来微服务拆分共用同一批业务模块。

## 项目根目录

```text
woodlin/
├── documentation/                  # VitePress 文档站
├── docker/                         # 容器相关资源
├── scripts/                        # 构建、部署、质量检查脚本
├── sql/                            # MySQL / PostgreSQL 脚本
├── woodlin-apps/                   # 应用入口聚合器
│   └── woodlin-admin/              # 当前唯一单体运行入口
├── woodlin-common/                 # 通用基础能力聚合器
│   ├── woodlin-common-core/
│   ├── woodlin-common-web/
│   ├── woodlin-common-db/
│   ├── woodlin-common-mp/
│   └── woodlin-common-cloud/
├── woodlin-dependencies/           # BOM 与版本管理
├── woodlin-modules/                # 主业务模块聚合器
│   ├── woodlin-module-security/
│   ├── woodlin-module-system/
│   ├── woodlin-module-tenant/
│   ├── woodlin-module-file/
│   ├── woodlin-module-task/
│   └── woodlin-module-datasource/
├── woodlin-plugins/                # 可选插件聚合器
│   ├── woodlin-plugin-generator/
│   ├── woodlin-plugin-etl/
│   ├── woodlin-plugin-sql2api/
│   ├── woodlin-plugin-dsl/
│   └── woodlin-plugin-z3/
├── woodlin-web/                    # Vue 3 + TypeScript 前端
├── AGENTS.md
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 后端结构

## `woodlin-dependencies`

负责统一第三方依赖版本，不承载业务代码。所有子模块都通过它继承 Spring Boot、Spring Cloud、MyBatis-Plus、Sa-Token 等版本。

## `woodlin-common`

`woodlin-common` 已拆为五个子模块：

```text
woodlin-common/
├── woodlin-common-core/            # 常量、枚举、异常、统一响应、基础 DTO/VO、工具类
├── woodlin-common-web/             # MVC/Jackson/OpenAPI 公共配置、全局异常、BuildInfo 等
├── woodlin-common-db/              # Redis、分页模型、DB 工具、数据源 SPI、元数据支撑
├── woodlin-common-mp/              # MyBatis-Plus 配置、分页适配、字段填充
└── woodlin-common-cloud/           # Nacos、Snowflake、分布式锁/租约等云侧支撑
```

边界约束：

- `common-*` 只放基础能力
- 不再放业务 Controller / Service / Mapper / 业务表实体
- MyBatis-Plus 专属能力只允许放在 `woodlin-common-mp`

## `woodlin-modules`

主业务模块聚合器：

```text
woodlin-modules/
├── woodlin-module-security/
├── woodlin-module-system/
├── woodlin-module-tenant/
├── woodlin-module-file/
├── woodlin-module-task/
└── woodlin-module-datasource/
```

职责示例：

- `woodlin-module-security`：认证、鉴权、密码策略、会话相关能力
- `woodlin-module-system`：用户、角色、菜单、字典、系统配置等
- `woodlin-module-tenant`：租户管理和数据隔离
- `woodlin-module-datasource`：外部数据源、元数据提取、小连接池管理

## `woodlin-plugins`

插件聚合器：

```text
woodlin-plugins/
├── woodlin-plugin-generator/
├── woodlin-plugin-etl/
├── woodlin-plugin-sql2api/
├── woodlin-plugin-dsl/
└── woodlin-plugin-z3/
```

这些模块默认按“可选能力”设计，由 app 入口决定是否装配。

## `woodlin-apps`

当前只有一个运行入口：

```text
woodlin-apps/
└── woodlin-admin/
    ├── src/main/java/com/mumu/woodlin/admin/
    │   ├── config/                # 运行时配置（如 Druid、WebMvc）
    │   ├── interceptor/           # Druid 页面相关拦截/过滤
    │   └── WoodlinAdminApplication.java
    ├── src/main/resources/
    │   ├── application.yml
    │   ├── .env.example
    │   └── logback-spring.xml
    └── pom.xml
```

这里不再承载全局业务扫描逻辑，只负责：

- Spring Boot 启动
- 主运行时依赖装配
- 主数据源 Druid 配置与监控页
- Web 运行时补充配置

## 业务模块内目录约定

`woodlin-modules/*` 与 `woodlin-plugins/*` 统一采用传统三层 + 模型分包，不使用 `infrastructure`：

```text
src/main/java/com/mumu/woodlin/{module}/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── model/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   └── query/
├── config/
└── convert/
```

含义如下：

- `controller/`：HTTP 接口边界
- `service/`：业务编排与跨模块调用边界
- `mapper/`：本模块内的数据访问
- `model/entity`：持久化对象
- `model/dto`：入参模型
- `model/vo`：出参模型
- `model/query`：分页与查询对象
- `config/`：模块自动装配与条件配置
- `convert/`：对象转换逻辑

## 模块自注册

当前各公共模块、业务模块和插件模块都通过 Spring Boot 自动装配自注册：

```text
src/main/resources/META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

每个模块会在自己的 `config/*AutoConfiguration.java` 中完成：

- `@ComponentScan`
- `@MapperScan(basePackageClasses = ...)`
- 必要的 `@Import`

因此主应用不再负责全局扫包。

## 前端目录

`woodlin-web` 保持 Vue 3 单体前端结构：

```text
woodlin-web/
├── src/
│   ├── api/
│   ├── components/
│   ├── composables/
│   ├── layouts/
│   ├── router/
│   ├── stores/
│   ├── utils/
│   └── views/
├── package.json
└── vite.config.ts
```

前端不参与当前后端模块拆分，仍通过统一 `/api` 基址访问后端。

## 目录调整后的收益

1. 单体运行入口更清晰，`woodlin-admin` 只做装配，不再混入业务。
2. `common` 边界收紧，后续拆服务时不用从公共模块里捞业务代码。
3. `modules` 和 `plugins` 职责明确，便于按需打包。
4. MyBatis-Plus 专属代码集中在 `woodlin-common-mp`，后续如果迁移 Flex，切点更集中。

::: tip
新增业务模块时，优先放到 `woodlin-modules` 或 `woodlin-plugins`，不要再往 `woodlin-common-*` 里塞业务逻辑。
:::
