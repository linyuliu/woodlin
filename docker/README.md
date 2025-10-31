# Woodlin Docker 多架构部署方案

本目录包含 Woodlin 多租户管理系统及其依赖中间件的多架构 Docker 镜像构建文件。

## 📋 目录结构

```
docker/
├── app/                      # Woodlin 应用服务
│   ├── Dockerfile           # 标准 BellSoft JDK 25 镜像
│   ├── Dockerfile.distroless # Distroless 极简镜像
│   └── docker-compose.yml   # 应用服务编排配置
├── mysql/                    # MySQL 8.0 数据库
│   ├── Dockerfile           # MySQL 镜像
│   └── docker-compose.yml   # MySQL 服务编排配置
├── redis/                    # Redis 7.x 缓存
│   ├── Dockerfile           # Redis 镜像
│   └── docker-compose.yml   # Redis 服务编排配置
├── minio/                    # MinIO 对象存储
│   ├── Dockerfile           # MinIO 镜像
│   └── docker-compose.yml   # MinIO 服务编排配置
├── postgresql/               # PostgreSQL 16 数据库 (可选)
│   ├── Dockerfile           # PostgreSQL 镜像
│   └── docker-compose.yml   # PostgreSQL 服务编排配置
└── README.md                # 本文件
```

## 🎯 特性

### 通用特性

- ✅ **多架构支持**: linux/amd64, linux/arm64, linux/arm/v7
- ✅ **LTS 版本**: 使用长期支持版本确保稳定性
- ✅ **常用工具**: 预装诊断和调试工具
- ✅ **中文支持**: 包含中文字体和本地化配置
- ✅ **性能优化**: 优化内核参数和资源限制
- ✅ **安全加固**: 非 root 用户运行，最小权限原则
- ✅ **层缓存优化**: 多阶段构建，加速构建过程
- ✅ **健康检查**: 内置健康检查机制
- ✅ **日志管理**: 配置日志轮转和持久化

### 各服务特性

#### Woodlin 应用 (app/)

- **标准镜像** (Dockerfile):
  - 基于 BellSoft Liberica JDK 25 Alpine
  - 包含完整的诊断工具集
  - 支持 JMX 监控
  - 适合开发和测试环境

- **Distroless 镜像** (Dockerfile.distroless):
  - 基于 Google Distroless Java 25
  - 极简镜像体积 (< 200MB)
  - 最小攻击面
  - 适合生产环境部署

#### MySQL (mysql/)

- 基于 MySQL 8.0 LTS
- 中文字符集配置 (utf8mb4)
- 性能优化参数
- 慢查询日志
- 支持主从复制

#### Redis (redis/)

- 基于 Redis 7.x Alpine
- AOF + RDB 双重持久化
- 内存淘汰策略优化
- 支持主从复制和 Sentinel

#### MinIO (minio/)

- 基于 MinIO 2025 LTS
- S3 兼容 API
- Web 管理控制台
- 支持分布式部署

#### PostgreSQL (postgresql/)

- 基于 PostgreSQL 16
- 中文字符集配置
- 性能优化参数
- 支持主从复制

## 🚀 快速开始

### 前置要求

- Docker 20.10+
- Docker Compose v2.0+
- Docker Buildx (用于多架构构建)

### 单服务部署

#### 1. Woodlin 应用

```bash
cd docker/app

# 构建镜像
docker compose build

# 启动服务
docker compose up -d

# 查看日志
docker compose logs -f

# 访问应用
# API: http://localhost:8080/api
# 健康检查: http://localhost:8080/api/actuator/health
```

#### 2. MySQL

```bash
cd docker/mysql

# 构建镜像
docker compose build

# 启动服务
docker compose up -d

# 连接数据库
mysql -h 127.0.0.1 -P 3306 -u root -p
```

#### 3. Redis

```bash
cd docker/redis

# 构建镜像
docker compose build

# 启动服务
docker compose up -d

# 连接 Redis
redis-cli -h 127.0.0.1 -p 6379 -a password
```

#### 4. MinIO

```bash
cd docker/minio

# 构建镜像
docker compose build

# 启动服务
docker compose up -d

# 访问控制台: http://localhost:9001
# 默认账号: minioadmin / minioadmin
```

### 多架构构建

使用 Docker Buildx 构建多架构镜像:

```bash
# 创建 buildx builder (首次使用)
docker buildx create --name multiarch --use

# 构建 Woodlin 应用 (多架构)
cd docker/app
docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t woodlin-app:latest \
  -f Dockerfile \
  --load \
  ../..

# 构建 MySQL (多架构)
cd docker/mysql
docker buildx build --platform linux/amd64,linux/arm64 \
  -t woodlin-mysql:8.0 \
  -f Dockerfile \
  --load \
  ../..

# 构建 Redis (多架构)
cd docker/redis
docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t woodlin-redis:7 \
  -f Dockerfile \
  --load \
  ../..

# 构建 MinIO (多架构)
cd docker/minio
docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t woodlin-minio:latest \
  -f Dockerfile \
  --load \
  ../..
```

### 完整系统部署

使用根目录的 `docker-compose.yml` 部署完整系统:

```bash
# 返回项目根目录
cd /path/to/woodlin

# 复制环境变量配置
cp .env.example .env

# 修改配置 (重要：修改所有密码)
vim .env

# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f woodlin-app

# 停止服务
docker compose down

# 停止服务并删除数据卷 (谨慎使用)
docker compose down -v
```

## ⚙️ 环境变量配置

每个服务的环境变量配置在对应的 `.env` 文件中:

### Woodlin 应用

```bash
# 服务器配置
SERVER_PORT=8080

# 数据库配置
DATABASE_URL=jdbc:mysql://mysql:3306/woodlin
DATABASE_USERNAME=root
DATABASE_PASSWORD=Passw0rd

# Redis 配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# MinIO 配置
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

### MySQL

```bash
MYSQL_PORT=3306
MYSQL_ROOT_PASSWORD=Passw0rd
MYSQL_DATABASE=woodlin
MYSQL_USER=woodlin
MYSQL_PASSWORD=Passw0rd
```

### Redis

```bash
REDIS_PORT=6379
REDIS_PASSWORD=
```

### MinIO

```bash
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
MINIO_REGION=cn-north-1
```

## 📊 资源限制建议

### 开发环境

| 服务 | CPU | 内存 |
|------|-----|------|
| Woodlin App | 1.0 | 1G |
| MySQL | 0.5 | 512M |
| Redis | 0.25 | 256M |
| MinIO | 0.25 | 256M |

### 生产环境

| 服务 | CPU | 内存 |
|------|-----|------|
| Woodlin App | 2.0 | 2G |
| MySQL | 2.0 | 2G |
| Redis | 1.0 | 1G |
| MinIO | 1.0 | 1G |

## 🔐 安全建议

### 1. 修改默认密码

**重要**: 生产环境必须修改所有默认密码！

```bash
# 生成强密码
openssl rand -base64 32

# 修改 .env 文件中的密码
DATABASE_PASSWORD=<生成的强密码>
REDIS_PASSWORD=<生成的强密码>
MINIO_ROOT_PASSWORD=<生成的强密码>
```

### 2. 限制网络访问

```yaml
# docker-compose.yml 中限制端口映射
ports:
  - "127.0.0.1:3306:3306"  # 只允许本地访问
```

### 3. 使用 Secrets

```yaml
# 使用 Docker Secrets 管理敏感信息
secrets:
  db_password:
    file: ./secrets/db_password.txt

services:
  mysql:
    secrets:
      - db_password
    environment:
      MYSQL_PASSWORD_FILE: /run/secrets/db_password
```

### 4. 定期更新

```bash
# 定期更新镜像
docker compose pull
docker compose up -d
```

## 🛠️ 故障排查

### 查看服务状态

```bash
# 查看所有容器状态
docker compose ps

# 查看服务日志
docker compose logs -f <service_name>

# 进入容器 shell
docker compose exec <service_name> bash
```

### 常见问题

#### 1. 容器启动失败

```bash
# 查看详细日志
docker compose logs <service_name>

# 检查配置文件
docker compose config

# 检查端口占用
netstat -tuln | grep <port>
```

#### 2. 数据库连接失败

```bash
# 检查数据库是否就绪
docker compose exec mysql mysqladmin ping -h localhost -u root -p

# 检查网络连接
docker compose exec woodlin-app nc -zv mysql 3306
```

#### 3. 内存不足

```bash
# 查看容器资源使用
docker stats

# 调整资源限制
# 编辑 docker-compose.yml 中的 deploy.resources
```

#### 4. 磁盘空间不足

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune
```

## 📦 数据备份与恢复

### MySQL 备份

```bash
# 备份数据库
docker compose exec mysql mysqldump -u root -p woodlin > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker compose exec -T mysql mysql -u root -p woodlin < backup_20250101.sql
```

### Redis 备份

```bash
# 创建 RDB 快照
docker compose exec redis redis-cli --no-auth-warning -a password BGSAVE

# 复制 RDB 文件
docker compose cp redis:/data/dump.rdb ./backup/dump.rdb

# 恢复数据
docker compose cp ./backup/dump.rdb redis:/data/dump.rdb
docker compose restart redis
```

### MinIO 备份

```bash
# 使用 mc (MinIO Client) 备份
mc mirror myminio/woodlin /backup/minio/woodlin

# 恢复数据
mc mirror /backup/minio/woodlin myminio/woodlin
```

## 🔍 监控与日志

### 日志管理

所有服务配置了日志轮转:

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 健康检查

所有服务都配置了健康检查:

```bash
# 查看健康状态
docker compose ps

# 手动执行健康检查
docker compose exec woodlin-app curl -f http://localhost:8080/api/actuator/health
```

### 性能监控

推荐使用以下工具:

- **Prometheus + Grafana**: 指标监控
- **ELK Stack**: 日志分析
- **Jaeger**: 分布式追踪

## 🚢 生产部署建议

### 1. 使用外部数据库

生产环境建议使用云厂商的数据库服务:

- AWS RDS / Aurora
- Azure Database
- 阿里云 RDS
- 腾讯云 CDB

### 2. 使用容器编排

推荐使用 Kubernetes 或 Docker Swarm:

```bash
# 生成 Kubernetes 配置
kompose convert -f docker-compose.yml

# 部署到 Kubernetes
kubectl apply -f ./kubernetes/
```

### 3. 配置 CI/CD

集成到 CI/CD 流程:

```yaml
# .github/workflows/docker-build.yml
name: Build Docker Images

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: docker/setup-buildx-action@v2
      - name: Build and push
        run: |
          docker buildx build --platform linux/amd64,linux/arm64 \
            -t myregistry/woodlin-app:latest \
            --push \
            -f docker/app/Dockerfile .
```

### 4. 使用镜像扫描

集成安全扫描工具:

```bash
# 使用 Trivy 扫描镜像
trivy image woodlin-app:latest

# 使用 Snyk 扫描
snyk container test woodlin-app:latest
```

## 📚 参考文档

- [BellSoft Liberica JDK](https://bell-sw.com/pages/downloads/)
- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [Redis Documentation](https://redis.io/documentation)
- [MinIO Documentation](https://min.io/docs/minio/linux/index.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/16/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Security](https://docs.docker.com/engine/security/)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目遵循项目根目录的 LICENSE 文件。
