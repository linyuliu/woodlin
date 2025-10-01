# Docker 改进总结

## 概述

根据需求对 Dockerfile 和 docker-compose.yml 进行了全面优化和完善，主要包括：

1. **基于 BellSoft Liberica JDK**: 使用更优化的 OpenJDK 发行版
2. **中文字体支持**: 防止 PDF 生成、图片处理等场景乱码
3. **内置诊断工具**: 便于在容器内排查问题
4. **详细的配置注释**: 每个参数都有清晰的说明
5. **系统优化**: ulimit、资源限制、健康检查等

## 主要改进内容

### 1. Dockerfile 优化

#### 基础镜像变更
```diff
- FROM openjdk:17-jdk-slim as builder
+ FROM bellsoft/liberica-openjdk-debian:17 as builder

- FROM openjdk:17-jre-slim
+ FROM bellsoft/liberica-openjre-debian:17
```

**优势**:
- BellSoft Liberica 是经过全面测试和优化的 OpenJDK 发行版
- 包含 JFR (Java Flight Recorder) 和 JMC 支持
- 更好的性能优化和及时的安全更新

#### 中文字体支持
新增安装：
- `fontconfig`: 字体配置库
- `fonts-wqy-zenhei`: 文泉驿正黑体
- `fonts-wqy-microhei`: 文泉驿微米黑
- `fonts-dejavu-core`: DejaVu 字体

配置本地化环境：
```dockerfile
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8
```

#### 诊断工具包
新增以下工具：
- **网络工具**: curl, wget, netcat, telnet, ping, dig, nslookup, netstat
- **进程工具**: ps, top
- **文本工具**: vim-tiny, less
- **数据工具**: jq (JSON 处理)

使用示例：
```bash
# 测试数据库连接
docker exec woodlin-app nc -zv mysql 3306

# 测试 API
docker exec woodlin-app curl http://localhost:8080/api/actuator/health

# 查看日志
docker exec woodlin-app less /app/logs/app.log
```

#### JVM 参数优化
新增配置：
```dockerfile
# 内存配置
-Xms512m
-Xmx1024m
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=256m

# GC 配置
-XX:+UseG1GC                      # 使用 G1 垃圾回收器
-XX:MaxGCPauseMillis=200          # GC 最大暂停时间
-XX:+UseStringDeduplication       # 字符串去重
-XX:+ParallelRefProcEnabled       # 并行处理引用
-XX:+DisableExplicitGC            # 禁用显式 GC

# OOM 处理
-XX:+HeapDumpOnOutOfMemoryError   # OOM 时生成堆转储
-XX:HeapDumpPath=/app/logs/

# GC 日志
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/app/logs/gc.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=10
-XX:GCLogFileSize=10M
```

### 2. docker-compose.yml 优化

#### MySQL 配置增强
新增启动参数：
```yaml
--max_connections=500              # 最大连接数
--max_allowed_packet=64M           # 最大数据包
--innodb_buffer_pool_size=256M     # InnoDB 缓冲池
--innodb_log_file_size=128M        # 日志文件大小
--innodb_flush_log_at_trx_commit=2 # 刷盘策略
--slow_query_log=1                 # 慢查询日志
--long_query_time=2                # 慢查询阈值
```

#### Redis 配置增强
新增启动参数：
```yaml
--maxmemory 512mb                  # 最大内存
--maxmemory-policy allkeys-lru     # 淘汰策略
--save "900 1"                     # RDB 持久化
--tcp-backlog 511                  # TCP 连接队列
--timeout 300                      # 空闲超时
--tcp-keepalive 60                 # Keepalive 间隔
```

#### ulimit 配置
所有服务增加：
```yaml
ulimits:
  nofile:
    soft: 65536  # 最大打开文件数
    hard: 65536
  nproc:
    soft: 65536  # 最大进程/线程数
    hard: 65536
```

**为什么需要这些限制**:
- Java 应用需要大量文件描述符（类文件、socket、日志等）
- 线程池、连接池会创建大量线程
- 防止 "Too many open files" 错误

#### 资源限制
每个服务配置资源限制：
```yaml
deploy:
  resources:
    limits:        # 资源上限
      cpus: '2.0'
      memory: 2G
    reservations:  # 资源预留
      cpus: '1.0'
      memory: 1G
```

各服务配置：
| 服务 | CPU 上限 | 内存上限 | CPU 预留 | 内存预留 |
|-----|---------|---------|---------|---------|
| MySQL | 2.0 | 1G | 0.5 | 512M |
| Redis | 1.0 | 768M | 0.25 | 256M |
| App | 2.0 | 2G | 1.0 | 1G |
| Nginx | 0.5 | 256M | 0.1 | 64M |

#### 健康检查优化
优化所有服务的健康检查配置：
```yaml
healthcheck:
  test: [...健康检查命令...]
  interval: 30s      # 检查间隔
  timeout: 10s       # 超时时间
  retries: 5         # 重试次数
  start_period: 90s  # 启动等待时间
```

#### 详细注释
为每个配置项添加了详细的中文注释，包括：
- 参数用途和作用
- 推荐值和调整建议
- 注意事项和最佳实践
- 相关配置的关联说明

总计 **400+ 行注释**，确保配置清晰易懂。

### 3. .env.example 增强

#### 新增配置项
```bash
# 系统限制
ULIMIT_NOFILE_SOFT=65536
ULIMIT_NOFILE_HARD=65536
ULIMIT_NPROC_SOFT=65536
ULIMIT_NPROC_HARD=65536

# 资源限制
MYSQL_MAX_CPU=2.0
MYSQL_MAX_MEMORY=1G
REDIS_MAX_CPU=1.0
REDIS_MAX_MEMORY=768M
APP_MAX_CPU=2.0
APP_MAX_MEMORY=2G
```

#### 系统优化指南
添加了详细的宿主机系统优化建议：

1. **文件描述符限制** (`/etc/security/limits.conf`)
2. **内核参数优化** (`/etc/sysctl.conf`)
3. **验证方法**
4. **Docker 守护进程配置**

### 4. 新增文档

#### docs/DOCKER.md (10,000+ 字)
完整的 Docker 部署文档，包括：
- Dockerfile 特性详解
- docker-compose 配置说明
- 系统优化建议
- 常用命令参考
- 故障排查指南
- 生产环境部署检查清单

#### docs/DOCKER_QUICK_REFERENCE.md (5,000+ 字)
快速参考指南，包括：
- 常用命令速查
- 故障排查命令
- 数据备份恢复
- 性能监控
- 最佳实践

#### scripts/check-docker-env.sh
环境检查脚本，自动检查：
- Docker 和 Docker Compose 是否安装
- 系统资源是否充足
- ulimit 配置是否正确
- 内核参数是否优化
- 端口是否被占用
- 配置文件是否存在

使用方法：
```bash
./scripts/check-docker-env.sh
```

## 使用示例

### 1. 环境检查
```bash
# 运行环境检查脚本
./scripts/check-docker-env.sh

# 根据提示优化系统配置
```

### 2. 快速启动
```bash
# 复制环境配置
cp .env.example .env

# 修改配置（生产环境必须修改密码）
vim .env

# 启动服务
docker compose up -d

# 查看日志
docker compose logs -f woodlin-app
```

### 3. 验证部署
```bash
# 检查服务状态
docker compose ps

# 检查健康状态
docker inspect woodlin-app | grep -A 10 Health

# 测试 API
curl http://localhost:8080/api/actuator/health
```

### 4. 故障排查
```bash
# 进入容器
docker exec -it woodlin-app bash

# 测试数据库连接
nc -zv mysql 3306

# 测试 Redis 连接
nc -zv redis 6379

# 查看应用日志
less /app/logs/app.log

# 查看 GC 日志
less /app/logs/gc.log

# 查看中文字体
fc-list :lang=zh

# 查看本地化配置
locale
```

## 技术亮点

### 1. 多阶段构建
- 构建阶段使用完整 JDK
- 运行时使用精简 JRE
- 显著减小镜像体积

### 2. 字体完整支持
- 文泉驿正黑 + 微米黑
- 完整的中文本地化环境
- 支持 PDF 生成和图片处理

### 3. 诊断工具齐全
- 网络诊断（nc, telnet, ping, dig）
- 进程监控（ps, top）
- 日志查看（vim, less）
- 数据处理（jq）

### 4. JVM 深度优化
- G1 垃圾回收器
- 字符串去重
- GC 日志滚动
- OOM 自动堆转储
- 详细的性能参数

### 5. 系统限制优化
- 文件描述符：65536
- 进程数：65536
- 完整的宿主机配置指南

### 6. 资源精细控制
- CPU 限制和预留
- 内存限制和预留
- 按服务类型差异化配置

### 7. 安全加固
- 非 root 用户运行
- no-new-privileges 选项
- 最小权限原则

### 8. 全面的健康检查
- 依赖服务健康检查
- 合理的启动等待时间
- 自动重试机制

## 对比总结

| 项目 | 优化前 | 优化后 |
|-----|--------|--------|
| 基础镜像 | OpenJDK | BellSoft Liberica |
| 中文支持 | ❌ | ✅ (3种字体) |
| 诊断工具 | 仅 curl | 11+ 种工具 |
| JVM 参数 | 基础配置 | 深度优化 (20+ 参数) |
| 配置注释 | 简单 | 详细 (400+ 行) |
| ulimit | ❌ | ✅ (65536) |
| 资源限制 | ❌ | ✅ (CPU + 内存) |
| 健康检查 | 基础 | 完善 (超时+重试) |
| 文档 | README | 15,000+ 字专项文档 |
| 环境检查 | ❌ | ✅ (自动化脚本) |

## 预期效果

1. **稳定性提升**
   - 更好的 JVM 参数配置
   - 合理的资源限制
   - 完善的健康检查

2. **易用性增强**
   - 详细的配置注释
   - 完整的操作文档
   - 便捷的诊断工具

3. **维护性改善**
   - 清晰的参数说明
   - 系统化的优化建议
   - 标准化的故障排查流程

4. **生产就绪**
   - 安全加固措施
   - 性能优化配置
   - 完整的部署检查清单

## 后续建议

1. **监控集成**
   - 集成 Prometheus + Grafana
   - 配置告警规则
   - 日志聚合（ELK/Loki）

2. **高可用**
   - MySQL 主从复制
   - Redis 集群
   - 应用多实例 + 负载均衡

3. **自动化**
   - CI/CD 流水线
   - 自动化测试
   - 自动化部署

4. **安全增强**
   - HTTPS 配置
   - 密钥管理（Vault）
   - 漏洞扫描

## 参考资料

- [BellSoft Liberica JDK](https://bell-sw.com/libericajdk/)
- [Docker 最佳实践](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Docker 指南](https://spring.io/guides/topicals/spring-boot-docker/)
- [Java 容器化最佳实践](https://developers.redhat.com/blog/2017/03/14/java-inside-docker)
