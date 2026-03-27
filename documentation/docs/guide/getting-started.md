# 快速开始

本指南面向当前仓库结构，默认后端运行入口是 `woodlin-apps/woodlin-admin`，前端入口是 `woodlin-web`。

## 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 20+ |
| npm | 10+ |
| 可选 | Nacos 2.x |

## 第一步：克隆项目

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin
```

## 第二步：初始化数据库

```bash
mysql -u root -p
CREATE DATABASE woodlin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

mysql -u root -p woodlin < sql/mysql/woodlin_schema.sql
mysql -u root -p woodlin < sql/mysql/woodlin_data.sql
```

## 第三步：准备运行配置

主应用基础配置位于：

```text
woodlin-apps/woodlin-admin/src/main/resources/application.yml
```

当前 `woodlin-admin` 默认通过 `spring.config.import` 从 Nacos 拉取环境配置，因此需要至少准备：

```bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=public
export NACOS_GROUP=DEFAULT_GROUP
```

建议在 Nacos 的 `application-dev.yml` 中提供主数据源、Redis、Sa-Token 等配置。典型主数据源配置如下：

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

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

## 第四步：构建后端

```bash
mvn clean install -DskipTests
```

如果只想先验证编译链路，也可以：

```bash
mvn -q -DskipTests compile
```

## 第五步：启动后端

### Maven 启动

```bash
mvn spring-boot:run -pl woodlin-apps/woodlin-admin -Dspring-boot.run.profiles=dev
```

### JAR 启动

```bash
java -jar woodlin-apps/woodlin-admin/target/woodlin-admin-1.0.0.jar
```

## 第六步：启动前端

```bash
cd woodlin-web
npm install
npm run dev
```

前端开发服务器默认地址：

```text
http://localhost:5173
```

## 默认访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080/api` |
| OpenAPI / Knife4j | `http://localhost:8080/api/doc.html` |
| Druid 监控 | `http://localhost:8080/api/druid` |
| 前端开发服务器 | `http://localhost:5173` |

默认账号：

- 用户名：`admin`
- 密码：`Passw0rd`

## 配置说明

当前运行策略请特别注意两点：

1. 主业务数据源固定使用 Druid。
2. `datasource`、`sql2api`、`etl` 等模块内部的临时外部库连接继续使用 HikariCP。

这意味着你在环境配置里主要维护的是主链路 Druid；模块内部的小连接池由代码按需创建。

## 验证方式

### 验证后端

```bash
curl http://localhost:8080/api/actuator/health
```

预期返回：

```json
{
  "status": "UP"
}
```

### 验证文档页

浏览器访问：

- `http://localhost:8080/api/doc.html`
- `http://localhost:8080/api/druid`

### 验证前端

浏览器访问：

- `http://localhost:5173`

## 常用命令

```bash
# 后端全量构建
mvn clean install -DskipTests

# 后端测试
mvn test

# 单模块测试示例
mvn -pl woodlin-modules/woodlin-module-system test

# 前端开发
cd woodlin-web && npm install && npm run dev

# 前端构建
cd woodlin-web && npm run build

# 质量检查脚本
./scripts/quality-check.sh
```

## 开发提示

- 业务模块不要再依赖主应用全局扫包，新增模块时请提供自己的 `AutoConfiguration.imports`。
- 对外分页返回统一用 `PageResult`，不要继续在 Controller 或 Service 接口暴露 `IPage`。
- `woodlin-common-*` 只放基础能力，不要回填业务代码。

## 下一步

- [目录结构](/guide/directory-structure)
- [配置说明](/guide/configuration)
- [技术架构](/guide/architecture)
- [模块总览](/modules/overview)
