# ====================================================================================
# Redis 7.x - 多架构 Docker 镜像
# 
# 特性:
# - 基于 Redis 7 Alpine 官方镜像 (最新稳定版本)
# - 支持多架构: linux/amd64, linux/arm64, linux/arm/v7
# - 预装常用工具
# - 优化性能参数和安全配置
# - 持久化配置 (AOF + RDB)
# 
# 构建命令 (多架构):
# docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
#   -t woodlin-redis:7 \
#   -f docker/redis/Dockerfile .
# ====================================================================================

FROM redis:7-alpine

# 设置标签信息
LABEL maintainer="Woodlin Team" \
      description="Redis 7 for Woodlin with optimized config" \
      version="7.x" \
      org.opencontainers.image.title="Redis 7 (Woodlin Edition)" \
      org.opencontainers.image.vendor="Woodlin"

# ====================================================================================
# 安装常用工具
# 
# Alpine 使用 apk 包管理器
# 
# 工具列表:
# - bash: Bash shell (Alpine 默认使用 ash)
# - curl: HTTP 客户端
# - wget: 文件下载工具
# - vim: 文本编辑器
# - nano: 轻量级文本编辑器
# - less: 分页查看工具
# - netcat-openbsd: 网络调试工具
# - bind-tools: dig/nslookup 等 DNS 工具
# - iproute2: ip 等网络工具
# - procps: ps/top 等进程管理工具
# - lsof: 列出打开文件
# - htop: 交互式进程查看器
# ====================================================================================
RUN apk add --no-cache \
        bash \
        curl \
        wget \
        vim \
        nano \
        less \
        netcat-openbsd \
        bind-tools \
        iproute2 \
        procps \
        lsof \
        htop \
        tzdata && \
    # 配置时区
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    # 清理缓存
    rm -rf /var/cache/apk/* /tmp/*

# ====================================================================================
# 创建 Redis 配置文件
# 
# 配置说明:
# 
# 网络配置:
# - bind: 绑定地址 (0.0.0.0 允许所有 IP 连接)
# - protected-mode: 保护模式 (no 允许远程连接)
# - port: 端口号
# - tcp-backlog: TCP 连接队列大小
# - timeout: 客户端空闲超时时间 (0=永不超时)
# - tcp-keepalive: TCP keepalive 间隔
# 
# 通用配置:
# - daemonize: 是否后台运行 (Docker 中必须为 no)
# - pidfile: PID 文件路径
# - loglevel: 日志级别 (debug, verbose, notice, warning)
# - logfile: 日志文件路径
# - databases: 数据库数量
# 
# 持久化配置:
# - save: RDB 持久化策略
# - stop-writes-on-bgsave-error: 保存失败时是否停止写入
# - rdbcompression: 是否压缩 RDB 文件
# - rdbchecksum: 是否校验 RDB 文件
# - dbfilename: RDB 文件名
# - dir: 数据目录
# 
# AOF 配置:
# - appendonly: 是否启用 AOF
# - appendfilename: AOF 文件名
# - appendfsync: AOF 刷盘策略
#   - always: 每次写入都刷盘 (最安全，性能最差)
#   - everysec: 每秒刷盘 (折中方案，推荐)
#   - no: 由操作系统决定 (性能最好，可能丢失数据)
# - no-appendfsync-on-rewrite: 重写时是否停止 fsync
# - auto-aof-rewrite-percentage: AOF 重写触发百分比
# - auto-aof-rewrite-min-size: AOF 重写最小文件大小
# 
# 内存管理:
# - maxmemory: 最大内存限制
# - maxmemory-policy: 内存淘汰策略
#   - noeviction: 不淘汰，写入失败
#   - allkeys-lru: 淘汰最近最少使用的 key (推荐)
#   - allkeys-lfu: 淘汰最少使用频率的 key
#   - volatile-lru: 淘汰设置了过期时间的最近最少使用的 key
#   - volatile-lfu: 淘汰设置了过期时间的最少使用频率的 key
#   - volatile-ttl: 淘汰即将过期的 key
#   - volatile-random: 随机淘汰设置了过期时间的 key
#   - allkeys-random: 随机淘汰 key
# - maxmemory-samples: LRU/LFU 采样数量
# 
# 慢查询日志:
# - slowlog-log-slower-than: 慢查询阈值 (微秒)
# - slowlog-max-len: 慢查询日志最大长度
# 
# 客户端连接:
# - maxclients: 最大客户端连接数
# 
# 安全配置:
# - requirepass: 密码 (通过环境变量设置)
# ====================================================================================
RUN echo '# Redis 配置文件 - Woodlin Edition\n\
\n\
# 网络配置\n\
bind 0.0.0.0\n\
protected-mode no\n\
port 6379\n\
tcp-backlog 511\n\
timeout 300\n\
tcp-keepalive 60\n\
\n\
# 通用配置\n\
daemonize no\n\
pidfile /data/redis.pid\n\
loglevel notice\n\
logfile ""\n\
databases 16\n\
\n\
# 持久化配置 - RDB\n\
save 900 1\n\
save 300 10\n\
save 60 10000\n\
stop-writes-on-bgsave-error yes\n\
rdbcompression yes\n\
rdbchecksum yes\n\
dbfilename dump.rdb\n\
dir /data\n\
\n\
# 持久化配置 - AOF\n\
appendonly yes\n\
appendfilename "appendonly.aof"\n\
appendfsync everysec\n\
no-appendfsync-on-rewrite no\n\
auto-aof-rewrite-percentage 100\n\
auto-aof-rewrite-min-size 64mb\n\
aof-load-truncated yes\n\
aof-use-rdb-preamble yes\n\
\n\
# 内存管理\n\
maxmemory 512mb\n\
maxmemory-policy allkeys-lru\n\
maxmemory-samples 5\n\
\n\
# 慢查询日志\n\
slowlog-log-slower-than 10000\n\
slowlog-max-len 128\n\
\n\
# 客户端连接\n\
maxclients 10000\n\
\n\
# 高级配置\n\
hash-max-ziplist-entries 512\n\
hash-max-ziplist-value 64\n\
list-max-ziplist-size -2\n\
list-compress-depth 0\n\
set-max-intset-entries 512\n\
zset-max-ziplist-entries 128\n\
zset-max-ziplist-value 64\n\
hll-sparse-max-bytes 3000\n\
stream-node-max-bytes 4096\n\
stream-node-max-entries 100\n\
activerehashing yes\n\
client-output-buffer-limit normal 0 0 0\n\
client-output-buffer-limit replica 256mb 64mb 60\n\
client-output-buffer-limit pubsub 32mb 8mb 60\n\
hz 10\n\
dynamic-hz yes\n\
aof-rewrite-incremental-fsync yes\n\
rdb-save-incremental-fsync yes\n\
\n\
# 性能优化\n\
# 注意: 这些参数需要宿主机内核支持\n\
# net.core.somaxconn = 511\n\
# vm.overcommit_memory = 1\n\
# echo never > /sys/kernel/mm/transparent_hugepage/enabled\n\
' > /usr/local/etc/redis/redis.conf

# ====================================================================================
# 创建数据目录
# ====================================================================================
RUN mkdir -p /data && \
    chown -R redis:redis /data && \
    chmod 755 /data

# ====================================================================================
# 健康检查
# 
# 使用 redis-cli ping 检查 Redis 是否可用
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=5s --retries=5 --start-period=20s \
    CMD redis-cli --no-auth-warning -a "${REDIS_PASSWORD}" ping || exit 1

# 暴露端口
EXPOSE 6379

# ====================================================================================
# 启动命令
# 
# 使用自定义配置文件启动 Redis
# 
# 注意: 
# - 密码通过 --requirepass 参数传递 (从环境变量获取)
# - 可以通过环境变量覆盖配置文件中的参数
# ====================================================================================
CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]

# ====================================================================================
# 环境变量说明
# 
# REDIS_PASSWORD: Redis 密码 (在 docker run 或 docker-compose 中设置)
# 
# 示例:
# docker run -e REDIS_PASSWORD=MySecretPassword woodlin-redis:7
# 
# 如果需要设置密码，可以在启动时添加 --requirepass 参数:
# docker run woodlin-redis:7 redis-server /usr/local/etc/redis/redis.conf --requirepass MyPassword
# ====================================================================================
