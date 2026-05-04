# Woodlin 多租户中后台管理系统

> 基于 Spring Boot 3.5.x 的多租户后台框架，按“单体先跑、以后好拆”组织模块。

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.16-red.svg)](https://baomidou.com/)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.44.0-blue.svg)](https://sa-token.cc/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 项目定位

Woodlin 是一个面向后台管理场景的多模块仓库，当前开发基线固定在 Spring Boot 3.x 和 Java 17。  
仓库默认提供一个单体运行入口 `woodlin-apps/woodlin-admin`，但业务能力已经按模块拆开，后续如果要拆成微服务，只需要新增 app 入口，而不是重新拆业务代码。

核心特点：

- 多租户、权限、文件、任务、数据源等能力齐备
- `common` 只保留基础能力，不再承载业务 Controller / Service / Mapper
- 模块通过 Spring Boot `AutoConfiguration` 自注册，`woodlin-admin` 不再全局扫包
- 主链路连接池使用 Druid，临时/外部数据源继续使用 HikariCP
- 当前 ORM 固定为 MyBatis-Plus，不做 MyBatis-Flex 双栈并行

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.5.12 |
| Spring Cloud | 2025.0.1 |
| Spring Cloud Alibaba | 2025.0.0.0 |
| MyBatis-Plus | 3.5.16 |
| Sa-Token | 1.44.0 |
| Dynamic DataSource | 4.3.1 |
| Hutool | 5.8.43 |
| SpringDoc | 2.8.15 |
| JustAuth | 1.16.7 |
| Snail Job | 1.9.0 |
| Redisson | 3.52.0 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 组合式 API + `<script setup>` |
| TypeScript | 5.x | 全量 TS，开启 strict |
| Naive UI | 2.x | 主 UI 组件库 |
| Pinia | 2.x | 状态管理（含 `pinia-plugin-persistedstate`） |
| Vue Router | 4.x | 路由 + 动态路由守卫 |
| vue-i18n | 9.x | 国际化（zh-CN / en-US） |
| VueUse | 11.x | 组合式工具函数 |
| Axios | 1.x | HTTP 客户端 |
| ECharts | 5.x | 图表 |
| dayjs | 1.x | 日期时间 |
| lodash-es | 4.x | 工具函数 |
| unplugin-vue-components | - | 组件按需自动注册 |
| unplugin-auto-import | - | API 自动导入 |

## 当前目录结构

```text
woodlin
├── woodlin-dependencies
├── woodlin-common
│   ├── woodlin-common-core
│   ├── woodlin-common-web
│   ├── woodlin-common-db
│   ├── woodlin-common-mp
│   └── woodlin-common-cloud
├── woodlin-modules
│   ├── woodlin-module-security
│   ├── woodlin-module-system
│   ├── woodlin-module-tenant
│   ├── woodlin-module-file
│   ├── woodlin-module-task
│   └── woodlin-module-datasource
├── woodlin-plugins
│   ├── woodlin-plugin-generator
│   ├── woodlin-plugin-etl
│   ├── woodlin-plugin-sql2api
│   ├── woodlin-plugin-dsl
│   └── woodlin-plugin-z3
├── woodlin-apps
│   └── woodlin-admin
├── woodlin-web
│   └── src
│       ├── api          # 后端接口封装（按模块拆分）
│       ├── assets       # 静态资源
│       ├── components   # 全局通用组件（W 前缀 + PermissionButton/ParentView）
│       ├── composables  # 组合式函数
│       ├── config       # 全局配置（运行时常量、菜单元数据）
│       ├── constants    # 静态常量、枚举
│       ├── directives   # 自定义指令（v-permission、v-copy 等）
│       ├── layouts      # 布局（DefaultLayout 含 Header/Sidebar/Tabs/Breadcrumb）
│       ├── locales      # vue-i18n 语言包（zh-CN、en-US）
│       ├── router       # 路由实例、动态路由、全局守卫
│       ├── stores       # Pinia 模块（user/app/route/tabs/permission/dict/tenant）
│       ├── styles       # 全局样式与主题变量
│       ├── types        # 全局 TypeScript 类型
│       ├── utils        # 通用工具（请求、加密、格式化等）
│       └── views        # 业务页面（按模块组织）
├── sql
├── scripts
└── documentation
```

模块职责固定为：

- `woodlin-common-*`：常量、异常、统一响应、Web 公共配置、DB 支撑、MP 适配、Nacos/Snowflake 等基础能力
- `woodlin-modules/*`：系统、安全、租户、文件、任务、数据源等主业务能力
- `woodlin-plugins/*`：生成器、ETL、SQL2API、DSL、Z3 等可选扩展
- `woodlin-apps/woodlin-admin`：单体运行入口，只负责装配和运行时配置

## 结构约束

### 模块内分层

业务模块和插件模块统一使用：

```text
controller/
service/
service/impl/
model/entity
model/dto
model/vo
model/query
mapper/
config/
convert/
```

约束如下：

- `controller` 只处理 HTTP 入参与返回
- `service` 是跨模块调用边界，禁止模块间直接互调 `mapper`
- `mapper` 只在本模块内部使用
- `model/entity` 视为持久化模型，跨模块优先传 `dto/vo`
- `config` 放自动装配和条件配置
- `convert` 收口对象映射，避免散落在 controller / service

### 单体装配方式

`woodlin-apps/woodlin-admin` 现在是一个纯启动模块：

- 启动类只保留 `@SpringBootApplication`
- 模块通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 自注册
- 不再使用全局 `scanBasePackages`
- 不再使用全局 `@MapperScan`

这意味着当前仍然是一键单体启动，但将来要拆服务时，只需要新增新的 app 模块引用同一批业务模块。

## 连接池与 ORM 策略

### 连接池

- 主业务数据源、dynamic-datasource 的 `master`：`Druid`
- `datasource`、`sql2api`、`etl` 等短生命周期或外部库探测：`HikariCP`
- `/druid/*` 监控页继续挂在 `woodlin-admin`

当前仓库中可直接看到的 Hikari 使用点：

- `woodlin-modules/woodlin-module-datasource`
- `woodlin-plugins/woodlin-plugin-sql2api`

### ORM

当前固定使用 MyBatis-Plus，但做了切换预留：

- 上层 Controller / Service 不再直接暴露 `IPage`
- 公共分页对象统一为 `PageResult`
- MP 专属适配放到 `woodlin-common-mp`
- `PageResult.of(IPage)` 已收口为 MP 适配器 `MyBatisPlusPageResults`

本轮不引入 MyBatis-Flex 双实现。后续如果团队要迁移 Flex，优先在 `woodlin-common-mp` 的边界上替换，而不是把双 ORM 同时铺进业务模块。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Redis 6+
- 可选：Nacos 2.x

### 1. 克隆项目

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin
```

### 2. 初始化数据库

```bash
mysql -u root -p
source sql/mysql/woodlin_schema.sql
source sql/mysql/woodlin_data.sql
```

### 3. 准备配置

应用基础入口配置位于：

- `woodlin-apps/woodlin-admin/src/main/resources/application.yml`

当前默认通过 `spring.config.import` 按环境从 Nacos 读取公共配置，至少需要准备：

- `NACOS_SERVER_ADDR`
- `NACOS_NAMESPACE`
- `NACOS_GROUP`
- 对应环境的 `application-{profile}.yml`

典型的主数据源配置建议如下，可放在 Nacos 或外部配置中心：

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
            stat-view-servlet:
              enabled: true
              url-pattern: /druid/*
```

### 4. 编译与运行

```bash
# 后端构建
mvn clean install -DskipTests

# 直接启动单体应用
mvn spring-boot:run -pl woodlin-apps/woodlin-admin -Dspring-boot.run.profiles=dev

# 或启动打包产物
java -jar woodlin-apps/woodlin-admin/target/woodlin-admin-1.0.0.jar
```

### 5. 启动前端

```bash
cd woodlin-web
# 部分依赖（如 Naive UI、unplugin-* 系列）peer 范围较严，需要 --legacy-peer-deps
npm install --legacy-peer-deps
npm run dev
```

前端页面覆盖以下模块：

- `system/`：用户、角色、菜单、部门、字典、参数、通知、地区
- `tenant/`：租户管理与套餐
- `openapi/`：开放 API 总览、应用、凭证、策略
- `datasource/`：数据源管理与连接监控
- `sql2api/`：SQL → API 在线编排
- `file/`：文件管理与存储配置
- `schedule/`：任务调度与日志
- `code/`：代码生成器
- `monitor/`：在线用户、缓存、服务器监控
- `assessment/`：在线考核
- `etl/`：数据集成 / ETL
- `user/profile`：个人中心
- `about`：关于页
- `dashboard` / `login` / `error`：首页、登录、403/404/500

默认访问地址：

- 后端 API：`http://localhost:8080/api`
- OpenAPI / Knife4j：`http://localhost:8080/api/doc.html`
- Druid 监控：`http://localhost:8080/api/druid`
- 前端开发服务器：`http://localhost:5173`

## 开发约定

### 分页返回

服务层对外返回 `PageResult<T>`，不要继续把 MP 类型泄露到上层。

```java
@Service
public class YourServiceImpl extends ServiceImpl<YourMapper, YourEntity> implements IYourService {

    @Override
    public PageResult<YourEntity> selectPage(YourQuery query, Integer pageNum, Integer pageSize) {
        Page<YourEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<YourEntity> wrapper = new LambdaQueryWrapper<>();
        return MyBatisPlusPageResults.of(this.page(page, wrapper));
    }
}
```

```java
@RestController
@RequestMapping("/your/path")
@RequiredArgsConstructor
public class YourController {

    private final IYourService yourService;

    @GetMapping("/list")
    public Result<PageResult<YourEntity>> list(YourQuery query, Integer pageNum, Integer pageSize) {
        return Result.success(yourService.selectPage(query, pageNum, pageSize));
    }
}
```

### 模块注册

新模块不要依赖主应用全局扫包。  
应在模块内部提供：

- `config/*AutoConfiguration.java`
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### Common 边界

`woodlin-common-*` 只允许放基础能力，不允许继续放：

- 业务 Controller
- 业务 Mapper
- 业务表实体
- 业务 Service

## 常用命令

```bash
# 后端快速编译
mvn clean install -DskipTests

# 后端测试
mvn test

# 单模块测试示例
mvn -pl woodlin-modules/woodlin-module-system test

# 前端开发（首次安装使用 --legacy-peer-deps）
cd woodlin-web && npm install --legacy-peer-deps && npm run dev

# 前端构建
cd woodlin-web && npm run build

# 前端 Lint / 类型检查
cd woodlin-web && npm run lint
cd woodlin-web && npm run type-check

# 代码质量脚本
./scripts/quality-check.sh
```

## 文档

- [目录结构](documentation/docs/guide/directory-structure.md)
- [配置说明](documentation/docs/guide/configuration.md)
- [技术架构](documentation/docs/guide/architecture.md)
- [模块总览](documentation/docs/modules/overview.md)

## 许可证

[MIT](LICENSE)
