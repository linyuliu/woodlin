# Docker 快速参考

## 常用命令

### 服务管理
```bash
# 启动所有服务
docker compose up -d

# 启动指定服务
docker compose up -d mysql redis

# 启动包含前端
docker compose --profile frontend up -d

# 停止所有服务
docker compose down

# 重启服务
docker compose restart woodlin-app

# 查看服务状态
docker compose ps

# 查看资源使用
docker stats
```

### 日志查看
```bash
# 查看实时日志
docker compose logs -f woodlin-app

# 查看最近 100 行
docker compose logs --tail=100 woodlin-app

# 查看所有服务日志
docker compose logs -f
```

### 容器操作
```bash
# 进入容器
docker exec -it woodlin-app bash

# 执行命令
docker exec woodlin-app curl http://localhost:8080/api/actuator/health

# 查看容器详情
docker inspect woodlin-app
```

### 故障排查

#### 1. 测试网络连接
```bash
# 进入应用容器
docker exec -it woodlin-app bash

# 测试 MySQL 连接
nc -zv mysql 3306
telnet mysql 3306
ping -c 4 mysql

# 测试 Redis 连接
nc -zv redis 6379
telnet redis 6379

# 测试 DNS 解析
dig mysql
nslookup redis
```

#### 2. 查看 Java 进程
```bash
# 查看 Java 进程
docker exec woodlin-app ps aux | grep java

# 查看 JVM 信息
docker exec woodlin-app java -version
```

#### 3. 查看日志
```bash
# 应用日志
docker exec woodlin-app less /app/logs/app.log

# GC 日志
docker exec woodlin-app less /app/logs/gc.log

# 查看最新日志
docker exec woodlin-app tail -f /app/logs/app.log
```

#### 4. 测试 API
```bash
# 健康检查
docker exec woodlin-app curl http://localhost:8080/api/actuator/health

# API 请求
docker exec woodlin-app curl -X GET http://localhost:8080/api/system/version
```

#### 5. 检查字体
```bash
# 查看已安装的中文字体
docker exec woodlin-app fc-list :lang=zh

# 查看本地化配置
docker exec woodlin-app locale
```

### 数据备份

#### 备份 MySQL
```bash
# 导出数据库
docker exec woodlin-mysql mysqldump -uroot -p123456 woodlin > backup.sql

# 导入数据库
docker exec -i woodlin-mysql mysql -uroot -p123456 woodlin < backup.sql

# 备份数据卷
docker run --rm \
  -v woodlin_mysql_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/mysql_backup_$(date +%Y%m%d).tar.gz -C /data .
```

#### 备份 Redis
```bash
# 触发 RDB 持久化
docker exec woodlin-redis redis-cli -a 123456 BGSAVE

# 备份数据卷
docker run --rm \
  -v woodlin_redis_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/redis_backup_$(date +%Y%m%d).tar.gz -C /data .
```

### 性能监控

#### 查看容器资源使用
```bash
# 实时资源使用
docker stats

# 指定容器
docker stats woodlin-app woodlin-mysql woodlin-redis
```

#### 查看 JVM 内存
```bash
# 获取 Java 进程 PID
PID=$(docker exec woodlin-app jps -l | grep woodlin-admin | awk '{print $1}')

# 查看堆内存使用
docker exec woodlin-app jmap -heap $PID

# 查看堆对象统计
docker exec woodlin-app jmap -histo $PID | head -20

# 生成堆转储
docker exec woodlin-app jmap -dump:format=b,file=/app/logs/heap.hprof $PID
```

#### 查看线程
```bash
# 获取 Java 进程 PID
PID=$(docker exec woodlin-app jps -l | grep woodlin-admin | awk '{print $1}')

# 查看线程堆栈
docker exec woodlin-app jstack $PID

# 查看线程数
docker exec woodlin-app jstack $PID | grep "java.lang.Thread.State" | wc -l
```

### 系统配置

#### 查看 ulimit 设置
```bash
# 在容器内查看
docker exec woodlin-app bash -c "ulimit -a"

# 文件描述符
docker exec woodlin-app bash -c "ulimit -n"

# 进程数
docker exec woodlin-app bash -c "ulimit -u"
```

#### 查看环境变量
```bash
docker exec woodlin-app env | grep -E 'DATABASE|REDIS|SPRING'
```

## 配置说明

### 环境变量 (.env)
```bash
# 数据库
DATABASE_NAME=woodlin
DATABASE_USERNAME=woodlin
DATABASE_PASSWORD=your_strong_password
DATABASE_PORT=3306

# Redis
REDIS_PASSWORD=your_redis_password
REDIS_PORT=6379

# 应用
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### 资源限制建议

| 环境 | MySQL CPU | MySQL 内存 | Redis CPU | Redis 内存 | App CPU | App 内存 |
|-----|-----------|------------|-----------|------------|---------|----------|
| 开发 | 1.0 | 512M | 0.5 | 256M | 1.0 | 1G |
| 测试 | 1.0 | 1G | 0.5 | 512M | 2.0 | 2G |
| 生产 | 2.0+ | 2G+ | 1.0+ | 1G+ | 4.0+ | 4G+ |

### ulimit 配置

所有容器默认配置：
- `nofile`: 65536 (文件描述符)
- `nproc`: 65536 (进程/线程数)

确保宿主机也配置了足够的限制。

## 最佳实践

### 生产环境
1. ✅ 修改所有默认密码
2. ✅ 配置 HTTPS (Nginx + Let's Encrypt)
3. ✅ 启用日志滚动和归档
4. ✅ 配置监控和告警
5. ✅ 定期备份数据
6. ✅ 限制资源使用
7. ✅ 配置防火墙规则

### 性能优化
1. ✅ 根据负载调整 JVM 内存
2. ✅ 调整数据库连接池大小
3. ✅ 配置 Redis 内存淘汰策略
4. ✅ 使用 SSD 存储
5. ✅ 优化系统内核参数

### 安全加固
1. ✅ 使用非 root 用户运行
2. ✅ 限制容器权限
3. ✅ 定期更新镜像
4. ✅ 扫描镜像漏洞
5. ✅ 加密敏感数据

## 问题排查

### 应用无法启动
1. 查看日志: `docker compose logs woodlin-app`
2. 检查依赖服务: `docker compose ps`
3. 检查网络连接: `docker network inspect woodlin_woodlin-network`
4. 检查健康检查: `docker inspect woodlin-app | grep -A 20 Health`

### 数据库连接失败
1. 检查 MySQL 状态: `docker compose ps mysql`
2. 测试连接: `docker exec woodlin-app nc -zv mysql 3306`
3. 查看 MySQL 日志: `docker compose logs mysql`
4. 检查密码配置: `docker exec woodlin-app env | grep DATABASE`

### Redis 连接失败
1. 检查 Redis 状态: `docker compose ps redis`
2. 测试连接: `docker exec woodlin-app nc -zv redis 6379`
3. 测试认证: `docker exec woodlin-redis redis-cli -a password ping`

### 内存溢出
1. 查看容器内存: `docker stats woodlin-app`
2. 查看 JVM 堆: `docker exec woodlin-app jmap -heap <pid>`
3. 分析堆转储: 复制 `/app/logs/*.hprof` 文件并使用 MAT 分析
4. 调整内存配置: 修改 Dockerfile 中的 `-Xmx` 参数

## 参考链接

- [完整 Docker 文档](DOCKER.md)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [BellSoft Liberica JDK](https://bell-sw.com/libericajdk/)
- [项目主页](https://github.com/linyuliu/woodlin)
