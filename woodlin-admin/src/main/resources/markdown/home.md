# Woodlin 多租户中后台管理系统 API 文档

欢迎使用 Woodlin 多租户中后台管理系统 API 文档！

## 系统简介

Woodlin 是一个高质量的多租户中后台管理系统框架，注重设计与代码细节。基于 Spring Boot 3.5.6 和 Java 25 开发，采用前后端分离架构。

## 技术栈

### 后端技术
- **Spring Boot 3.5.6** - 核心框架
- **Sa-Token** - 认证授权
- **MyBatis Plus** - 数据持久化
- **Redis** - 缓存中间件
- **MySQL 8.0+** - 关系型数据库
- **Knife4j** - API 文档增强

### 前端技术
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - JavaScript 超集
- **Naive UI** - Vue 3 组件库
- **Vite** - 前端构建工具

## 核心功能

### 1. 系统管理
- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 字典管理
- 配置管理
- 操作日志
- 登录日志

### 2. 租户管理
- 租户信息管理
- 租户套餐管理
- 租户数据隔离

### 3. 文件管理
- 文件上传下载
- 文件预览
- 多种存储方式支持（本地、MinIO、OSS）

### 4. 任务调度
- 定时任务管理
- 任务执行记录
- 支持 Cron 表达式

### 5. 代码生成
- 数据库表导入
- 代码模板配置
- 一键生成 CRUD 代码

### 6. SQL2API
- SQL 转 REST API
- 动态 API 管理

## 认证说明

本系统使用 **Bearer Token** 认证方式：

1. 首先调用登录接口获取 Token
2. 在后续请求的 Header 中添加：`Authorization: Bearer {token}`
3. Token 默认有效期为 30 天

## 快速开始

### 默认账号

- 用户名：`admin`
- 密码：`Passw0rd`

### 本地运行

```bash
# 后端
mvn spring-boot:run

# 前端
cd woodlin-web
npm run dev
```

### Docker 运行

```bash
docker compose up -d
```

## 接口分组说明

为了方便查看和测试，API 接口按功能模块进行了分组：

- **全部接口** - 所有可用的 API 接口
- **系统管理** - 用户、角色、菜单等系统管理接口
- **租户管理** - 租户相关的管理接口
- **文件管理** - 文件上传、下载等接口
- **任务调度** - 定时任务管理接口
- **代码生成** - 代码生成器接口
- **SQL2API** - SQL 动态 API 接口
- **认证授权** - 登录、登出等认证接口

## 常见问题

### Q: 如何获取 Token？
A: 调用 `/auth/login` 接口，使用用户名和密码登录，返回结果中包含 Token。

### Q: Token 失效怎么办？
A: Token 失效后需要重新登录获取新的 Token。可以通过 `/auth/refresh` 接口刷新 Token。

### Q: 如何测试需要认证的接口？
A: 点击页面右上角的 "Authorize" 按钮，在弹出框中输入 Token（格式：`Bearer {token}`），点击 "Authorize" 即可。

### Q: 接口返回 403 是什么原因？
A: 403 表示无权访问，可能是：
1. Token 未传递或格式错误
2. Token 已过期
3. 当前用户没有该接口的访问权限

## 联系我们

- **作者**: mumu
- **邮箱**: mumu@woodlin.com
- **GitHub**: [https://github.com/linyuliu/woodlin](https://github.com/linyuliu/woodlin)
- **许可证**: MIT License

---

**祝您使用愉快！**
