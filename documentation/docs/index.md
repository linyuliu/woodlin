---
home: true
icon: home
title: 主页
heroImage: /logo.svg
heroImageDark: /logo.svg
heroText: Woodlin
tagline: Spring Boot 3.5.x 基线的多租户后台框架，按“单体先跑、以后好拆”组织模块
actions:
  - text: 快速开始
    link: /guide/getting-started.md
    type: primary
  - text: 技术架构
    link: /guide/architecture.md
  - text: 模块总览
    link: /modules/overview.md
features:
  - icon: 🏢
    title: 多租户架构
    details: 内置租户管理与数据隔离能力，适合 SaaS 与后台管理场景。
  - icon: 🧱
    title: 模块清晰
    details: 仓库按 common、modules、plugins、apps 四类聚合器组织，单体和未来拆分共用同一批业务模块。
  - icon: 🔐
    title: 安全能力
    details: 基于 Sa-Token 1.44.0，提供认证、权限、密码策略和会话能力。
  - icon: 🗃️
    title: 双连接池策略
    details: 主链路固定 Druid，元数据提取和临时外部库连接继续使用 HikariCP。
  - icon: 📄
    title: ORM 预留切换
    details: 当前主 ORM 为 MyBatis-Plus，分页与配置耦合已收口到 woodlin-common-mp，后续切换 Flex 有明确边界。
  - icon: ⚙️
    title: 模块自注册
    details: 各模块通过 AutoConfiguration 自注册，woodlin-admin 不再依赖全局扫包。
copyright: false
footer: 基于 MIT 许可发布 | Copyright © 2024-present mumu
---

## 当前技术栈

### 后端

- Java 17+
- Spring Boot 3.5.12
- Spring Cloud 2025.0.1
- Spring Cloud Alibaba 2025.0.0.0
- MyBatis-Plus 3.5.16
- Sa-Token 1.44.0
- Dynamic DataSource 4.3.1
- SpringDoc 2.8.15

### 前端

- Vue 3
- TypeScript
- Vite
- Naive UI
- Pinia
- Vue Router

## 当前仓库结构

```text
woodlin
├── woodlin-dependencies
├── woodlin-common
├── woodlin-modules
├── woodlin-plugins
├── woodlin-apps
│   └── woodlin-admin
└── woodlin-web
```

其中：

- `woodlin-common-*`：基础能力
- `woodlin-modules/*`：主业务模块
- `woodlin-plugins/*`：插件模块
- `woodlin-apps/woodlin-admin`：当前单体入口

## 快速开始

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin

mvn clean install -DskipTests
mvn spring-boot:run -pl woodlin-apps/woodlin-admin -Dspring-boot.run.profiles=dev

cd woodlin-web
npm install
npm run dev
```

常用地址：

- 后端 API：`http://localhost:8080/api`
- API 文档：`http://localhost:8080/api/doc.html`
- Druid 监控：`http://localhost:8080/api/druid`
- 前端开发服务器：`http://localhost:5173`

## 核心模块

| 分类 | 模块 |
|------|------|
| 依赖管理 | `woodlin-dependencies` |
| 基础能力 | `woodlin-common-core` / `web` / `db` / `mp` / `cloud` |
| 主业务 | `woodlin-module-security` / `system` / `tenant` / `file` / `task` / `datasource` |
| 插件 | `woodlin-plugin-generator` / `etl` / `sql2api` / `dsl` / `z3` |
| 运行入口 | `woodlin-apps/woodlin-admin` |
| 前端 | `woodlin-web` |

## 文档导航

- [目录结构](/guide/directory-structure)
- [配置说明](/guide/configuration)
- [技术架构](/guide/architecture)
- [模块总览](/modules/overview)
