# Docker 部署文档

## 概述

Woodlin 项目提供了完善的 Docker 部署方案，包括：

- **优化的 Dockerfile**: 基于 BellSoft Liberica JDK，支持中文字体，内置诊断工具
- **完整的 docker-compose**: 包含 MySQL、Redis、应用服务的完整编排
- **系统优化配置**: ulimit、资源限制、健康检查等完善配置

## 快速开始

### 1. 准备环境

确保已安装 Docker 和 Docker Compose：

```bash
# 检查 Docker 版本 (要求 20.10+)
docker --version

# 检查 Docker Compose 版本 (要求 v2.0+)
docker compose version
```

### 2. 配置环境变量

```bash
# 复制环境配置文件
cp .env.example .env

# 编辑配置文件，修改密码等敏感信息
vim .env
```

### 3. 启动服务

```bash
# 启动所有服务 (MySQL + Redis + 应用)
docker compose up -d

# 启动包含前端的完整服务
docker compose --profile frontend up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f woodlin-app
```

### 4. 访问服务

- **后端 API**: http://localhost:8080/api
- **API 文档**: http://localhost:8080/api/doc.html
- **数据库监控**: http://localhost:8080/api/druid
- **前端页面**: http://localhost:3000 (需启用 frontend profile)

默认登录账号：
- 用户名: `admin`
- 密码: `123456`

## Dockerfile 特性

### 1. 基于 BellSoft Liberica JDK

BellSoft Liberica 是一个经过全面测试和优化的 OpenJDK 发行版，相比标准 OpenJDK 具有以下优势：

- ✅ 完全符合 Java SE 标准
- ✅ 包含 JFR (Java Flight Recorder) 和 JMC (Java Mission Control) 支持
- ✅ 更好的性能优化
- ✅ 及时的安全更新
- ✅ 提供长期支持版本

```dockerfile
# 构建阶段使用完整 JDK
FROM bellsoft/liberica-openjdk-debian:17 as builder

# 运行时使用精简 JRE
FROM bellsoft/liberica-openjre-debian:17
```

### 2. 中文字体支持

安装了完整的中文字体包，防止 PDF 生成、图片处理等场景出现乱码：

- **fontconfig**: 字体配置库
- **fonts-wqy-zenhei**: 文泉驿正黑体 (黑体字体)
- **fonts-wqy-microhei**: 文泉驿微米黑 (无衬线字体)
- **fonts-dejavu-core**: DejaVu 字体 (英文字体)

配置了中文本地化环境：

```dockerfile
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8
```

### 3. 内置诊断工具

镜像中包含了丰富的诊断工具，便于排查问题：

| 工具 | 用途 | 使用示例 |
|-----|------|---------|
| curl | HTTP 请求测试 | `curl http://localhost:8080/api/actuator/health` |
| wget | 文件下载 | `wget http://example.com/file` |
| netcat (nc) | 网络连接测试 | `nc -zv mysql 3306` |
| telnet | 端口连接测试 | `telnet redis 6379` |
| ping | 网络连通性测试 | `ping -c 4 mysql` |
| dig/nslookup | DNS 解析测试 | `dig mysql` |
| netstat | 网络连接查看 | `netstat -tunlp` |
| ps/top | 进程监控 | `ps aux`, `top` |
| vim | 文本编辑 | `vim /app/logs/app.log` |
| jq | JSON 处理 | `curl ... | jq .` |
| less | 日志查看 | `less /app/logs/app.log` |

### 4. JVM 优化配置

#### 内存配置

```bash
-Xms512m                    # 初始堆内存 512MB
-Xmx1024m                   # 最大堆内存 1GB
-XX:MetaspaceSize=128m      # Metaspace 初始大小
-XX:MaxMetaspaceSize=256m   # Metaspace 最大大小
```

#### 垃圾回收器配置

```bash
-XX:+UseG1GC                      # 使用 G1 垃圾回收器
-XX:MaxGCPauseMillis=200          # GC 最大暂停时间 200ms
-XX:+UseStringDeduplication       # 启用字符串去重
-XX:+ParallelRefProcEnabled       # 并行处理引用对象
-XX:+DisableExplicitGC            # 禁用显式 GC
```

#### OOM 处理

```bash
-XX:+HeapDumpOnOutOfMemoryError   # OOM 时生成堆转储
-XX:HeapDumpPath=/app/logs/       # 堆转储文件路径
```

#### GC 日志

```bash
-XX:+PrintGCDetails               # 打印 GC 详细信息
-XX:+PrintGCDateStamps            # 打印 GC 时间戳
-Xloggc:/app/logs/gc.log          # GC 日志路径
-XX:+UseGCLogFileRotation         # 启用日志滚动
-XX:NumberOfGCLogFiles=10         # 日志文件数量
-XX:GCLogFileSize=10M             # 单个日志文件大小
```

### 5. 安全加固

- 使用非 root 用户运行 (uid=1000, gid=1000)
- 启用 `no-new-privileges` 安全选项
- 只暴露必要的端口

## docker-compose 配置详解

### 服务依赖关系

```
woodlin-app
  ├─> mysql (condition: service_healthy)
  └─> redis (condition: service_healthy)

nginx (optional)
  └─> woodlin-app
```

### 健康检查配置

所有服务都配置了健康检查，确保服务正常启动后才被依赖：

| 服务 | 检查命令 | 间隔 | 超时 | 重试 | 启动等待 |
|-----|---------|------|------|------|---------|
| MySQL | mysqladmin ping | 30s | 10s | 10 | 40s |
| Redis | redis-cli ping | 30s | 5s | 5 | 20s |
| App | curl actuator/health | 30s | 10s | 5 | 90s |
| Nginx | wget localhost | 30s | 5s | 3 | 10s |

### ulimit 配置

每个服务都配置了合适的文件描述符和进程数限制：

```yaml
ulimits:
  nofile:
    soft: 65536  # 软限制
    hard: 65536  # 硬限制
  nproc:
    soft: 65536
    hard: 65536
```

**为什么需要这些配置？**

- **nofile**: 限制打开文件数，包括 socket 连接、日志文件、类文件等
- **nproc**: 限制进程/线程数，Java 应用会创建大量线程

### 资源限制

使用 `deploy.resources` 限制容器资源使用：

```yaml
deploy:
  resources:
    limits:      # 资源上限
      cpus: '2.0'
      memory: 2G
    reservations: # 资源预留
      cpus: '1.0'
      memory: 1G
```

各服务资源配置：

| 服务 | CPU 上限 | 内存上限 | CPU 预留 | 内存预留 |
|-----|---------|---------|---------|---------|
| MySQL | 2.0 | 1G | 0.5 | 512M |
| Redis | 1.0 | 768M | 0.25 | 256M |
| App | 2.0 | 2G | 1.0 | 1G |
| Nginx | 0.5 | 256M | 0.1 | 64M |

### 数据持久化

使用命名卷持久化数据：

| 卷名 | 用途 | 容器挂载路径 |
|-----|------|------------|
| mysql_data | MySQL 数据库文件 | /var/lib/mysql |
| redis_data | Redis 持久化数据 | /data |
| app_logs | 应用日志 | /app/logs |
| app_temp | 临时文件 | /app/temp |
| nginx_cache | Nginx 缓存 | /var/cache/nginx |

## 系统优化建议

### 宿主机系统配置

为了让容器的 ulimit 配置生效，需要配置宿主机系统限制。

#### 1. 配置文件描述符限制

编辑 `/etc/security/limits.conf`：

```bash
# 编辑配置文件
sudo vim /etc/security/limits.conf

# 添加以下内容
* soft nofile 65536
* hard nofile 65536
* soft nproc 65536
* hard nproc 65536
root soft nofile 65536
root hard nofile 65536
root soft nproc 65536
root hard nproc 65536
```

#### 2. 配置系统内核参数

编辑 `/etc/sysctl.conf`：

```bash
# 编辑配置文件
sudo vim /etc/sysctl.conf

# 添加以下内容

# 最大文件句柄数
fs.file-max = 2097152
fs.nr_open = 2097152

# TCP 连接队列
net.core.somaxconn = 32768
net.ipv4.tcp_max_syn_backlog = 8192

# TIME_WAIT 优化
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30

# 本地端口范围
net.ipv4.ip_local_port_range = 10000 65535

# 虚拟内存
vm.swappiness = 10
vm.max_map_count = 262144

# 连接跟踪
net.netfilter.nf_conntrack_max = 1000000
```

#### 3. 使配置生效

```bash
# 使内核参数生效
sudo sysctl -p

# 重启 Docker 服务
sudo systemctl restart docker

# 验证配置
ulimit -a
sysctl -a | grep -E 'file-max|nr_open|somaxconn'
```

#### 4. 验证容器内的限制

```bash
# 进入容器
docker exec -it woodlin-app bash

# 查看限制
ulimit -a

# 应该看到类似输出:
# open files                      (-n) 65536
# max user processes              (-u) 65536
```

### Docker 守护进程配置

编辑 `/etc/docker/daemon.json`：

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 65536,
      "Soft": 65536
    }
  }
}
```

重启 Docker：

```bash
sudo systemctl restart docker
```

## 常用命令

### 服务管理

```bash
# 启动所有服务
docker compose up -d

# 启动指定服务
docker compose up -d mysql redis

# 停止所有服务
docker compose down

# 停止并删除卷 (⚠️ 会删除数据)
docker compose down -v

# 重启服务
docker compose restart woodlin-app

# 查看服务状态
docker compose ps

# 查看服务资源使用
docker stats
```

### 日志查看

```bash
# 查看所有服务日志
docker compose logs

# 查看指定服务日志
docker compose logs woodlin-app

# 实时查看日志
docker compose logs -f woodlin-app

# 查看最近 100 行日志
docker compose logs --tail=100 woodlin-app

# 查看带时间戳的日志
docker compose logs -t woodlin-app
```

### 容器操作

```bash
# 进入容器
docker exec -it woodlin-app bash

# 在容器内执行命令
docker exec woodlin-app curl http://localhost:8080/api/actuator/health

# 查看容器详情
docker inspect woodlin-app

# 查看容器资源使用
docker stats woodlin-app
```

### 数据管理

```bash
# 查看所有卷
docker volume ls

# 查看卷详情
docker volume inspect woodlin_mysql_data

# 备份 MySQL 数据
docker run --rm \
  -v woodlin_mysql_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/mysql_backup_$(date +%Y%m%d).tar.gz -C /data .

# 恢复 MySQL 数据
docker run --rm \
  -v woodlin_mysql_data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/mysql_backup.tar.gz -C /data

# 清理未使用的卷
docker volume prune
```

### 网络管理

```bash
# 查看网络
docker network ls

# 查看网络详情
docker network inspect woodlin_woodlin-network

# 查看连接的容器
docker network inspect --format='{{range .Containers}}{{.Name}} {{end}}' \
  woodlin_woodlin-network
```

### 镜像管理

```bash
# 构建镜像
docker compose build

# 不使用缓存重新构建
docker compose build --no-cache

# 拉取最新镜像
docker compose pull

# 查看镜像
docker images | grep woodlin

# 删除未使用的镜像
docker image prune
```

## 故障排查

### 1. 容器启动失败

**检查日志**:
```bash
docker compose logs woodlin-app
```

**常见原因**:
- 端口被占用: 检查 `netstat -tunlp | grep 8080`
- 依赖服务未就绪: 检查 MySQL 和 Redis 状态
- 配置错误: 检查 .env 文件配置

### 2. 数据库连接失败

**进入应用容器测试连接**:
```bash
docker exec -it woodlin-app bash

# 测试 MySQL 连接
nc -zv mysql 3306
telnet mysql 3306

# 测试 Redis 连接
nc -zv redis 6379
telnet redis 6379
```

**检查 DNS 解析**:
```bash
# 在应用容器内
dig mysql
nslookup redis
```

### 3. 应用性能问题

**查看 JVM 堆内存使用**:
```bash
docker exec woodlin-app jps -l
docker exec woodlin-app jmap -heap <pid>
```

**查看 GC 日志**:
```bash
docker exec woodlin-app less /app/logs/gc.log
```

**查看线程堆栈**:
```bash
docker exec woodlin-app jstack <pid>
```

### 4. 磁盘空间不足

**查看磁盘使用**:
```bash
# 查看 Docker 磁盘使用
docker system df

# 详细信息
docker system df -v
```

**清理未使用的资源**:
```bash
# 清理未使用的容器、网络、镜像、卷 (⚠️ 谨慎使用)
docker system prune -a --volumes

# 只清理容器和网络
docker system prune

# 清理镜像
docker image prune -a

# 清理卷
docker volume prune
```

### 5. 中文乱码问题

**检查容器内字体安装**:
```bash
docker exec woodlin-app fc-list :lang=zh

# 应该看到中文字体列表
```

**检查本地化配置**:
```bash
docker exec woodlin-app locale

# 应该看到 LANG=zh_CN.UTF-8
```

**检查 Java 字体配置**:
```bash
docker exec woodlin-app java -Dfile.encoding=UTF-8 -version
```

## 生产环境部署建议

### 1. 安全加固

- ✅ 修改所有默认密码
- ✅ 使用强密码 (至少 16 位，包含大小写字母、数字、特殊字符)
- ✅ 启用 HTTPS (使用 Nginx 反向代理 + Let's Encrypt 证书)
- ✅ 配置防火墙规则，只开放必要端口
- ✅ 使用 Docker secrets 管理敏感信息
- ✅ 定期更新镜像和系统补丁

### 2. 高可用配置

- ✅ MySQL 主从复制或集群
- ✅ Redis 主从复制或集群
- ✅ 应用多实例部署 + 负载均衡
- ✅ 使用 Docker Swarm 或 Kubernetes

### 3. 监控和告警

- ✅ 集成 Prometheus + Grafana 监控
- ✅ 配置告警规则 (CPU、内存、磁盘、错误率等)
- ✅ 日志聚合 (ELK Stack 或 Loki)
- ✅ APM 性能监控 (Skywalking、Pinpoint 等)

### 4. 备份策略

- ✅ 数据库每日全量备份 + 增量备份
- ✅ 配置文件版本控制
- ✅ 备份文件异地存储
- ✅ 定期测试恢复流程

### 5. 资源规划

根据实际负载调整资源配置：

| 场景 | CPU | 内存 | 磁盘 | 并发用户 |
|-----|-----|------|------|---------|
| 开发测试 | 2 核 | 4G | 50G | < 10 |
| 小型生产 | 4 核 | 8G | 100G | < 100 |
| 中型生产 | 8 核 | 16G | 200G | < 1000 |
| 大型生产 | 16+ 核 | 32G+ | 500G+ | > 1000 |

## 参考资料

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
- [BellSoft Liberica JDK](https://bell-sw.com/libericajdk/)
- [Spring Boot Docker 最佳实践](https://spring.io/guides/topicals/spring-boot-docker/)
- [MySQL Docker 配置](https://hub.docker.com/_/mysql)
- [Redis Docker 配置](https://hub.docker.com/_/redis)
