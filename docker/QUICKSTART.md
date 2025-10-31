# Woodlin Docker 快速入门指南

本指南帮助您快速启动和运行 Woodlin 系统。

## 🚀 5 分钟快速启动

### 前置条件

确保已安装：
- Docker 20.10+
- Docker Compose v2.0+
- 至少 4GB 可用内存

### 步骤 1: 克隆项目

```bash
git clone https://github.com/linyuliu/woodlin.git
cd woodlin/docker
```

### 步骤 2: 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件（建议修改所有密码）
vim .env
```

**重要**: 生产环境必须修改以下密码：
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `MINIO_ROOT_PASSWORD`

生成强密码：
```bash
openssl rand -base64 32
```

### 步骤 3: 启动完整系统

```bash
# 使用完整系统配置启动所有服务
docker compose -f docker-compose.full.yml up -d
```

### 步骤 4: 验证服务状态

```bash
# 查看所有服务状态
docker compose -f docker-compose.full.yml ps

# 查看应用日志
docker compose -f docker-compose.full.yml logs -f woodlin-app
```

### 步骤 5: 访问服务

| 服务 | URL | 默认账号 |
|------|-----|---------|
| Woodlin API | http://localhost:8080/api | admin / Passw0rd |
| API 文档 | http://localhost:8080/api/doc.html | - |
| 健康检查 | http://localhost:8080/api/actuator/health | - |
| MinIO 控制台 | http://localhost:9001 | minioadmin / minioadmin |

## 🔧 常用命令

### 服务管理

```bash
# 启动所有服务
docker compose -f docker-compose.full.yml up -d

# 停止所有服务
docker compose -f docker-compose.full.yml stop

# 重启服务
docker compose -f docker-compose.full.yml restart woodlin-app

# 删除服务（保留数据）
docker compose -f docker-compose.full.yml down

# 删除服务和数据（危险！）
docker compose -f docker-compose.full.yml down -v
```

### 日志查看

```bash
# 查看所有服务日志
docker compose -f docker-compose.full.yml logs

# 实时跟踪应用日志
docker compose -f docker-compose.full.yml logs -f woodlin-app

# 查看最近 100 行日志
docker compose -f docker-compose.full.yml logs --tail=100 woodlin-app
```

### 进入容器

```bash
# 进入应用容器
docker compose -f docker-compose.full.yml exec woodlin-app bash

# 进入 MySQL 容器
docker compose -f docker-compose.full.yml exec mysql bash

# 连接 MySQL 数据库
docker compose -f docker-compose.full.yml exec mysql mysql -u root -p
```

## 📦 仅启动特定服务

### 仅启动应用（需要外部数据库）

```bash
cd app
docker compose up -d
```

### 仅启动 MySQL

```bash
cd mysql
docker compose up -d
```

### 仅启动 Redis

```bash
cd redis
docker compose up -d
```

### 仅启动 MinIO

```bash
cd minio
docker compose up -d
```

## 🏗️ 构建自定义镜像

### 构建所有镜像

```bash
# 单架构构建（当前平台）
./build-all.sh

# 多架构构建（需要推送到 registry）
./build-all.sh --multi-arch --push --registry myregistry.com/woodlin
```

### 构建特定服务

```bash
# 只构建应用
./build-all.sh app

# 构建应用和数据库
./build-all.sh app mysql redis
```

### 使用自定义标签

```bash
# 构建并标记为 v1.0.0
./build-all.sh --tag v1.0.0 app

# 多架构构建并推送
./build-all.sh --multi-arch --push --registry myregistry.com/woodlin --tag v1.0.0
```

## 🔍 故障排查

### 问题 1: 容器无法启动

```bash
# 查看详细错误日志
docker compose -f docker-compose.full.yml logs <service_name>

# 检查容器状态
docker compose -f docker-compose.full.yml ps -a

# 查看容器资源使用
docker stats
```

### 问题 2: 端口被占用

```bash
# 检查端口占用
netstat -tuln | grep 8080
# 或
lsof -i :8080

# 修改 .env 文件中的端口配置
SERVER_PORT=8081
```

### 问题 3: 数据库连接失败

```bash
# 检查 MySQL 是否就绪
docker compose -f docker-compose.full.yml exec mysql mysqladmin ping -h localhost -u root -p

# 检查网络连接
docker compose -f docker-compose.full.yml exec woodlin-app nc -zv mysql 3306

# 查看 MySQL 日志
docker compose -f docker-compose.full.yml logs mysql
```

### 问题 4: 内存不足

```bash
# 查看容器资源使用
docker stats

# 调整资源限制（编辑 .env 文件）
APP_MAX_MEMORY=4G
MYSQL_MAX_MEMORY=2G
```

### 问题 5: 磁盘空间不足

```bash
# 查看 Docker 磁盘使用
docker system df

# 清理未使用的资源
docker system prune -a

# 清理数据卷（危险！会删除数据）
docker volume prune
```

## 🔐 安全配置

### 修改默认密码

```bash
# 生成强密码
openssl rand -base64 32

# 编辑 .env 文件，替换所有默认密码
vim .env
```

### 限制网络访问

编辑 `docker-compose.full.yml`，修改端口映射：

```yaml
ports:
  - "127.0.0.1:8080:8080"  # 只允许本地访问
```

### 启用 SSL/TLS

在生产环境中，建议：
1. 使用 Nginx 反向代理
2. 配置 SSL 证书
3. 启用 HTTPS

## 📊 监控和维护

### 健康检查

```bash
# 检查所有服务健康状态
docker compose -f docker-compose.full.yml ps

# 手动执行健康检查
curl http://localhost:8080/api/actuator/health
```

### 数据备份

```bash
# 备份 MySQL 数据库
docker compose -f docker-compose.full.yml exec mysql \
  mysqldump -u root -p woodlin > backup_$(date +%Y%m%d).sql

# 备份 Redis 数据
docker compose -f docker-compose.full.yml exec redis \
  redis-cli --no-auth-warning -a password BGSAVE
docker compose -f docker-compose.full.yml cp redis:/data/dump.rdb ./backup/
```

### 日志管理

日志自动轮转配置：
- 最大文件大小: 10MB
- 保留文件数: 3

查看日志位置：
```bash
docker inspect <container_id> | grep LogPath
```

## 🚢 生产部署建议

1. **使用外部数据库服务**
   - AWS RDS / Aurora
   - Azure Database
   - 阿里云 RDS

2. **配置持久化存储**
   - 使用命名卷或绑定挂载
   - 定期备份数据

3. **设置资源限制**
   - 根据实际负载调整 CPU 和内存
   - 配置合适的 ulimit

4. **启用监控**
   - Prometheus + Grafana
   - ELK Stack
   - 云监控服务

5. **实施安全措施**
   - 使用强密码
   - 限制网络访问
   - 定期更新镜像
   - 启用日志审计

## 📚 更多信息

- [完整文档](./README.md)
- [构建脚本说明](./build-all.sh --help)
- [环境变量配置](./.env.example)
- [GitHub 项目](https://github.com/linyuliu/woodlin)

## 💡 提示

- 首次启动可能需要 2-3 分钟等待所有服务就绪
- 应用启动后，等待约 90 秒完成初始化
- 确保有足够的磁盘空间（建议至少 10GB）
- 定期更新镜像以获取安全补丁

## 🆘 获取帮助

如遇问题，请：
1. 查看服务日志
2. 阅读[完整文档](./README.md)
3. 在 GitHub 提交 Issue

---

**祝使用愉快！** 🎉
