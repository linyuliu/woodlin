---
layout: home

hero:
  name: "Woodlin"
  text: "多租户中后台管理系统"
  tagline: 注重设计与代码细节的高质量企业级框架
  image:
    src: /logo.svg
    alt: Woodlin Logo
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 查看模块
      link: /modules/overview
    - theme: alt
      text: GitHub
      link: https://github.com/linyuliu/woodlin

features:
  - icon: 🏢
    title: 多租户架构
    details: 完善的租户数据隔离和管理机制，支持 SaaS 模式部署，每个租户拥有独立的数据空间和配置
    
  - icon: 👥
    title: RBAC 权限控制
    details: 精细化的角色基于访问控制（RBAC），支持用户、角色、权限、菜单的灵活配置和管理
    
  - icon: 🌳
    title: 部门树形管理
    details: 支持无限层级的组织架构管理，树形结构清晰展示部门关系，便于权限继承和数据隔离
    
  - icon: 📁
    title: 文件管理
    details: 支持多种存储方式（本地、OSS、MinIO），文件上传下载、在线预览、权限控制一应俱全
    
  - icon: ⏰
    title: 任务调度
    details: 基于 Quartz 的强大任务调度系统，支持 Cron 表达式、动态任务管理、执行日志查看
    
  - icon: 📊
    title: Excel 导入导出
    details: 集成 EasyExcel，支持大数据量的 Excel 导入导出，模板定义简单，使用便捷高效
    
  - icon: 🔧
    title: 代码生成
    details: 智能化的代码生成工具，根据数据库表结构自动生成前后端代码，大幅提升开发效率
    
  - icon: 🚀
    title: SQL2API 动态接口
    details: 通过配置 SQL 语句快速生成 RESTful API，无需编写代码，支持参数验证、加密、限流
    
  - icon: 🔐
    title: API 加密
    details: 支持 AES、RSA、SM4 多种加密算法，保护 API 数据传输安全，灵活配置加密策略
    
  - icon: 📝
    title: 操作审计
    details: 完整的操作日志记录和审计功能，追踪用户操作轨迹，满足合规性要求
    
  - icon: 🎨
    title: 统一响应格式
    details: 标准化的 API 响应格式，全局异常处理，让前后端交互更加规范和友好
    
  - icon: ⚙️
    title: 系统配置管理
    details: 统一的前端配置管理界面，支持加密配置、密码策略、活动监控等多种系统设置
---

## 技术栈

### 后端技术

- **Java 17+**: 最新的 LTS 版本，性能优异
- **Spring Boot 3.4.1**: 最新的 Spring Boot 框架
- **MyBatis Plus 3.5.9**: 强大的 ORM 框架
- **Sa-Token 1.39.0**: 轻量级 Java 权限认证框架
- **Dynamic DataSource 4.3.1**: 动态数据源切换
- **EasyExcel 3.3.4**: Excel 处理工具
- **Redisson 3.37.0**: Redis 客户端
- **Hutool 5.8.34**: Java 工具库
- **SpringDoc 2.7.0**: OpenAPI 文档生成

### 前端技术

- **Vue 3.5**: 渐进式 JavaScript 框架
- **TypeScript 5.8**: 类型安全的 JavaScript 超集
- **Vite 7.0**: 下一代前端构建工具
- **Naive UI 2.43**: 优质的 Vue 3 组件库
- **Pinia 3.0**: Vue 3 状态管理
- **Axios 1.12**: HTTP 客户端
- **Vue Router 4.5**: 路由管理

## 快速开始

```bash
# 克隆项目
git clone https://github.com/linyuliu/woodlin.git
cd woodlin

# 后端构建
mvn clean package -DskipTests
mvn install -DskipTests

# 前端构建
cd woodlin-web
npm install
npm run build

# 使用 Docker Compose 启动
docker compose up -d
```

访问地址：
- 后台管理: http://localhost:8080/api
- API 文档: http://localhost:8080/api/doc.html
- 前端页面: http://localhost:3000

默认账号: `admin` / `Passw0rd`

## 核心模块

| 模块 | 说明 |
|------|------|
| [woodlin-dependencies](/modules/dependencies) | BOM 依赖管理 |
| [woodlin-common](/modules/common) | 通用工具和配置 |
| [woodlin-security](/modules/security) | 安全认证模块 |
| [woodlin-system](/modules/system) | 系统管理模块 |
| [woodlin-tenant](/modules/tenant) | 多租户模块 |
| [woodlin-file](/modules/file) | 文件管理模块 |
| [woodlin-task](/modules/task) | 任务调度模块 |
| [woodlin-generator](/modules/generator) | 代码生成模块 |
| [woodlin-sql2api](/modules/sql2api) | SQL2API 动态接口 |
| [woodlin-admin](/modules/admin) | 管理后台应用 |
| [woodlin-web](/modules/web) | Vue 3 前端 |

## 文档导航

::: tip 开始使用
- [项目介绍](/guide/introduction) - 了解 Woodlin 项目的背景和特性
- [快速开始](/guide/getting-started) - 5 分钟快速上手
- [技术架构](/guide/architecture) - 深入理解系统架构设计
:::

::: info 开发指南
- [代码规范](/development/code-style) - Java 和 TypeScript 代码规范
- [开发环境搭建](/development/environment-setup) - 配置开发环境
- [调试技巧](/development/debugging) - 高效调试方法
- [测试指南](/development/testing) - 单元测试和集成测试
:::

::: warning 部署运维
- [本地部署](/deployment/local) - 本地开发环境部署
- [Docker 部署](/deployment/docker) - 使用 Docker 容器化部署
- [生产环境配置](/deployment/production) - 生产环境最佳实践
- [监控与运维](/deployment/monitoring) - 系统监控和故障排查
:::

## 许可证

[MIT License](https://github.com/linyuliu/woodlin/blob/main/LICENSE)

Copyright © 2024-present mumu
