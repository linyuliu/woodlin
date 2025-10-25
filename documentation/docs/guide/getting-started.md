# 快速开始

本指南将帮助您在 5 分钟内快速搭建并运行 Woodlin 项目。

## 环境要求

在开始之前，请确保您的开发环境满足以下要求：

### 必需环境

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 25 或更高 | 推荐使用 Eclipse Temurin JDK 25 |
| **Maven** | 3.8 或更高 | Apache Maven 3.9+ 更佳 |
| **MySQL** | 8.0 或更高 | 需要支持 utf8mb4 字符集 |
| **Redis** | 6.0 或更高 | 用于缓存和会话管理 |

### 前端开发环境（可选）

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| **Node.js** | 20.19+ 或 22.12+ | 推荐使用 LTS 版本 |
| **npm** | 10.8 或更高 | Node.js 自带 |

### 容器环境（可选）

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| **Docker** | 20+ | 用于容器化部署 |
| **Docker Compose** | v2+ | 编排多容器应用 |

## 安装步骤

### 方式一：Docker Compose（推荐）

使用 Docker Compose 是最简单快速的启动方式，它会自动配置所有依赖服务。

#### 1. 克隆项目

::: code-tabs#shell

@tab HTTPS

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin
```

@tab SSH

```bash
git clone git@github.com:linyuliu/woodlin.git
cd woodlin
```

@tab GitHub CLI

```bash
gh repo clone linyuliu/woodlin
cd woodlin
```

:::

#### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，修改必要的配置
vim .env
```

**.env 关键配置**：

```properties
# 数据库配置
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password

# Redis 配置
REDIS_PASSWORD=your_redis_password

# 应用配置
SERVER_PORT=8080
```

#### 3. 启动服务

```bash
# 启动所有服务（MySQL、Redis、后端、前端）
docker compose up -d

# 查看日志
docker compose logs -f
```

#### 4. 等待服务启动

首次启动需要约 60-90 秒来初始化数据库和启动所有服务。

#### 5. 访问应用

服务启动完成后，您可以访问：

- **后台管理**：http://localhost:8080/api
- **API 文档**：http://localhost:8080/api/doc.html
- **数据库监控**：http://localhost:8080/api/druid
- **前端页面**：http://localhost:3000

**默认账号**：
- 用户名：`admin`
- 密码：`Passw0rd`

### 方式二：本地开发（推荐开发者使用）

如果您需要进行代码开发，建议使用本地开发方式。

#### 1. 克隆项目

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin
```

#### 2. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE woodlin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出 MySQL
exit
```

#### 3. 导入数据库脚本

```bash
# 导入数据库结构
mysql -u root -p woodlin < sql/woodlin_schema.sql

# 导入初始数据
mysql -u root -p woodlin < sql/woodlin_data.sql
```

#### 4. 配置数据库和 Redis 连接

编辑 `woodlin-admin/src/main/resources/application.yml`：

::: code-tabs#yaml

@tab 数据库配置

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: your_username
          password: your_password
```

@tab Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如果 Redis 没有密码，请注释此行
      database: 0
```

@tab 完整配置

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
          username: your_username
          password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
      database: 0
```

:::

#### 5. 构建后端项目

::: code-tabs#shell

@tab Maven 构建

```bash
# 清理并编译项目（首次构建需要 1-2 分钟）
mvn clean package -DskipTests

# 安装到本地 Maven 仓库
mvn install -DskipTests
```

@tab 开发脚本

```bash
# 使用开发脚本一键构建
./scripts/dev.sh build
```

:::

::: tip 构建时间
首次构建需要下载依赖，可能需要 1-2 分钟。后续构建通常在 15-20 秒内完成。
:::

#### 6. 启动后端服务

::: code-tabs#shell

@tab Maven 启动

```bash
# 使用 Maven 启动
mvn spring-boot:run -pl woodlin-admin
```

@tab JAR 包启动

```bash
# 使用 jar 包启动
java -jar woodlin-admin/target/woodlin-admin-1.0.0.jar
```

@tab 开发脚本

```bash
# 使用开发脚本启动
./scripts/dev.sh backend
```

:::

::: tip 启动时间
后端服务启动通常需要 30-45 秒。
:::

#### 7. 构建并启动前端（可选）

::: code-tabs#shell

@tab 开发模式

```bash
# 进入前端目录
cd woodlin-web

# 安装依赖（首次需要，约 15-20 秒）
npm install

# 启动开发服务器（约 600ms 启动）
npm run dev
```

@tab 生产构建

```bash
# 进入前端目录
cd woodlin-web

# 安装依赖
npm install

# 构建生产版本（约 8-9 秒）
npm run build
```

@tab 开发脚本

```bash
# 使用开发脚本启动前端
./scripts/dev.sh frontend
```

:::

前端开发服务器默认运行在 http://localhost:5173

#### 8. 访问应用

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端 API | http://localhost:8080/api | 后端服务接口 |
| API 文档 | http://localhost:8080/api/doc.html | Swagger UI 接口文档 |
| 前端开发 | http://localhost:5173 | Vite 开发服务器 |
| 数据库监控 | http://localhost:8080/api/druid | Druid 监控面板 |

## 验证安装

### 检查后端服务

#### 1. 健康检查

```bash
curl http://localhost:8080/api/actuator/health
```

预期输出：

```json
{
  "status": "UP"
}
```

#### 2. 访问 API 文档

在浏览器中打开 http://localhost:8080/api/doc.html，您应该能看到完整的 API 文档界面。

#### 3. 测试登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Passw0rd"
  }'
```

预期返回包含 token 的 JSON 响应。

### 检查前端服务

在浏览器中访问：
- Docker 部署：http://localhost:3000
- 开发服务器：http://localhost:5173

您应该能看到登录界面，使用默认账号登录即可。

### 检查数据库

```bash
# 登录 MySQL
mysql -u root -p woodlin

# 查看表
SHOW TABLES;

# 查看用户数据
SELECT * FROM sys_user;
```

您应该能看到系统预置的管理员用户。

## 使用开发脚本

Woodlin 提供了一组便捷的开发脚本来简化常见操作。

### 开发脚本（scripts/dev.sh）

```bash
# 启动后端服务
./scripts/dev.sh backend

# 启动前端服务
./scripts/dev.sh frontend

# 同时启动后端和前端（使用 tmux 或 screen）
./scripts/dev.sh

# 构建项目
./scripts/dev.sh build

# 清理构建产物
./scripts/dev.sh clean
```

### 部署脚本（scripts/deploy.sh）

```bash
# 一键部署（构建并使用 Docker Compose 部署）
./scripts/deploy.sh
```

::: tip 脚本使用建议
- 开发时使用 `./scripts/dev.sh` 快速启动服务
- 部署时使用 `./scripts/deploy.sh` 进行完整构建和部署
:::

## 常见问题

### 1. Maven 构建失败

**问题**：Maven 构建时提示依赖下载失败。

**解决方法**：

```bash
# 清理 Maven 缓存
rm -rf ~/.m2/repository

# 使用国内镜像加速（编辑 ~/.m2/settings.xml）
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>

# 重新构建
mvn clean package -DskipTests
```

### 2. 数据库连接失败

**问题**：应用启动时提示无法连接数据库。

**检查清单**：

1. MySQL 服务是否启动：`systemctl status mysql`
2. 数据库是否创建：`SHOW DATABASES;`
3. 用户名密码是否正确
4. 时区设置是否正确：`serverTimezone=GMT%2B8`

### 3. Redis 连接失败

**问题**：应用启动时提示无法连接 Redis。

**解决方法**：

```bash
# 检查 Redis 是否运行
redis-cli ping
# 应该返回 PONG

# 如果未运行，启动 Redis
# Linux
systemctl start redis

# macOS
brew services start redis

# Docker
docker run -d -p 6379:6379 redis:7-alpine
```

### 4. 端口冲突

**问题**：启动时提示端口已被占用。

**解决方法**：

```bash
# 查看端口占用情况
# Linux/macOS
lsof -i :8080
netstat -anp | grep 8080

# Windows
netstat -ano | findstr :8080

# 杀死占用端口的进程
kill -9 <PID>

# 或者修改应用端口（application.yml）
server:
  port: 8081
```

### 5. 前端依赖安装失败

**问题**：npm install 失败或非常慢。

**解决方法**：

```bash
# 使用国内镜像
npm config set registry https://registry.npmmirror.com

# 清理缓存
npm cache clean --force

# 重新安装
rm -rf node_modules package-lock.json
npm install
```

### 6. 权限问题

**问题**：Linux 下执行脚本提示权限不足。

**解决方法**：

```bash
# 给脚本添加执行权限
chmod +x scripts/*.sh

# 执行脚本
./scripts/dev.sh
```

## 性能期望

基于实际测试，各操作的预期耗时：

### 构建时间

| 操作 | 首次 | 后续 |
|------|------|------|
| Maven clean compile | 66s | 8-17s |
| Maven clean package | - | 17s |
| Maven install | - | 5s |
| npm install | 16s | - |
| npm run build | - | 8-9s |
| 完整构建脚本 | - | 18-20s |

### 启动时间

| 服务 | 启动时间 |
|------|----------|
| 后端应用 | 30-45s |
| 前端开发服务器 | 616ms |
| Docker Compose 全栈 | 60-90s |

### 测试和检查

| 操作 | 耗时 |
|------|------|
| Maven tests | 4-6s |
| npm run lint | 1.7s |

::: warning 重要提示
**NEVER CANCEL** 长时间运行的构建命令！Maven 首次构建需要下载大量依赖，可能需要 1-2 分钟，请耐心等待。
:::

## 下一步

恭喜！您已经成功搭建并运行了 Woodlin 项目。

接下来您可以：

1. **了解架构**：查看 [技术架构](/guide/architecture) 深入理解系统设计
2. **浏览模块**：查看 [模块文档](/modules/overview) 了解各个模块功能
3. **开始开发**：查看 [开发指南](/development/code-style) 学习开发规范
4. **测试功能**：
   - 登录系统：使用 `admin` / `Passw0rd`
   - 浏览用户管理、角色管理等功能
   - 查看 API 文档：http://localhost:8080/api/doc.html
   - 查看数据库监控：http://localhost:8080/api/druid
5. **配置系统**：查看 [配置说明](/guide/configuration) 了解配置项

## 获取帮助

如果您在使用过程中遇到问题：

- 查看 [常见问题](/guide/faq)
- 查看 [故障排查](/deployment/troubleshooting)
- 提交 [GitHub Issue](https://github.com/linyuliu/woodlin/issues)
- 在 [讨论区](https://github.com/linyuliu/woodlin/discussions) 提问

---

::: tip 开发建议
- 开发时建议使用本地开发方式，便于调试
- 首次启动建议使用 Docker Compose，确保环境一致
- 熟悉项目后可以根据需要选择启动方式
:::
