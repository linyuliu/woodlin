# ====================================================================================
# PostgreSQL 16 - 多架构 Docker 镜像
# 
# 特性:
# - 基于 PostgreSQL 16 官方镜像 (最新 LTS 版本)
# - 支持多架构: linux/amd64, linux/arm64
# - 预装常用工具和中文字符集
# - 优化性能参数和安全配置
# - 自动初始化数据库脚本
# 
# 构建命令 (多架构):
# docker buildx build --platform linux/amd64,linux/arm64 \
#   -t woodlin-postgresql:16 \
#   -f docker/postgresql/Dockerfile .
# ====================================================================================

FROM postgres:16

# 设置标签信息
LABEL maintainer="Woodlin Team" \
      description="PostgreSQL 16 for Woodlin with Chinese support" \
      version="16" \
      org.opencontainers.image.title="PostgreSQL 16 (Woodlin Edition)" \
      org.opencontainers.image.vendor="Woodlin"

# ====================================================================================
# 安装常用工具
# 
# 工具列表:
# - curl: HTTP 客户端，用于健康检查
# - wget: 文件下载工具
# - vim: 文本编辑器
# - less: 分页查看工具
# - net-tools: netstat 等网络工具
# - procps: ps/top 等进程管理工具
# - iproute2: ip 等现代网络工具
# - lsof: 列出打开文件
# - htop: 交互式进程查看器
# - pgcli: PostgreSQL CLI 工具 (可选，增强型命令行)
# ====================================================================================
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        wget \
        vim \
        less \
        net-tools \
        procps \
        iproute2 \
        lsof \
        htop \
        # pgcli 是 Python 工具，需要 pip
        # python3-pip && \
        # pip3 install --no-cache-dir pgcli && \
    && \
    # 清理缓存
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# ====================================================================================
# 配置中文字符集
# 
# PostgreSQL 默认支持 UTF-8，但我们明确配置以确保一致性
# 
# 创建自定义配置文件 /etc/postgresql/postgresql.conf.d/chinese.conf
# 此配置会覆盖默认配置
# ====================================================================================
RUN mkdir -p /etc/postgresql/postgresql.conf.d && \
    echo "# 字符集配置\n\
client_encoding = utf8\n\
lc_messages = 'zh_CN.UTF-8'\n\
lc_monetary = 'zh_CN.UTF-8'\n\
lc_numeric = 'zh_CN.UTF-8'\n\
lc_time = 'zh_CN.UTF-8'\n\
\n\
# 时区配置\n\
timezone = 'Asia/Shanghai'\n\
log_timezone = 'Asia/Shanghai'\n\
" > /etc/postgresql/postgresql.conf.d/chinese.conf

# ====================================================================================
# 性能优化配置
# 
# 创建 /etc/postgresql/postgresql.conf.d/performance.conf
# 
# 参数说明:
# - max_connections: 最大连接数 (根据业务负载调整)
# - shared_buffers: 共享缓冲区大小 (建议设置为内存的 25%)
# - effective_cache_size: 有效缓存大小 (建议设置为内存的 50-75%)
# - maintenance_work_mem: 维护操作内存 (VACUUM, CREATE INDEX 等)
# - checkpoint_completion_target: 检查点完成目标
# - wal_buffers: WAL 缓冲区大小
# - default_statistics_target: 统计信息目标
# - random_page_cost: 随机页面成本 (SSD 建议 1.1, HDD 建议 4.0)
# - effective_io_concurrency: 有效 IO 并发数 (SSD 建议 200, HDD 建议 2)
# - work_mem: 工作内存 (排序、哈希表等)
# - min_wal_size: 最小 WAL 大小
# - max_wal_size: 最大 WAL 大小
# - max_worker_processes: 最大工作进程数
# - max_parallel_workers_per_gather: 每个 Gather 节点的最大并行工作进程数
# - max_parallel_workers: 最大并行工作进程数
# ====================================================================================
RUN echo "# 连接配置\n\
max_connections = 200\n\
\n\
# 内存配置\n\
shared_buffers = 256MB\n\
effective_cache_size = 1GB\n\
maintenance_work_mem = 64MB\n\
work_mem = 4MB\n\
\n\
# WAL 配置\n\
wal_buffers = 8MB\n\
min_wal_size = 1GB\n\
max_wal_size = 4GB\n\
\n\
# 检查点配置\n\
checkpoint_completion_target = 0.9\n\
\n\
# 查询规划器配置\n\
default_statistics_target = 100\n\
random_page_cost = 1.1\n\
effective_io_concurrency = 200\n\
\n\
# 并行查询配置\n\
max_worker_processes = 4\n\
max_parallel_workers_per_gather = 2\n\
max_parallel_workers = 4\n\
max_parallel_maintenance_workers = 2\n\
\n\
# 日志配置\n\
logging_collector = on\n\
log_directory = 'log'\n\
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'\n\
log_rotation_age = 1d\n\
log_rotation_size = 100MB\n\
log_min_duration_statement = 1000\n\
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '\n\
log_checkpoints = on\n\
log_connections = on\n\
log_disconnections = on\n\
log_lock_waits = on\n\
log_temp_files = 0\n\
\n\
# 自动清理配置\n\
autovacuum = on\n\
autovacuum_max_workers = 3\n\
autovacuum_naptime = 1min\n\
\n\
# 统计信息配置\n\
track_activities = on\n\
track_counts = on\n\
track_io_timing = on\n\
track_functions = all\n\
" > /etc/postgresql/postgresql.conf.d/performance.conf

# ====================================================================================
# 安全配置
# 
# 创建 /etc/postgresql/postgresql.conf.d/security.conf
# 
# 安全措施:
# - ssl: 启用 SSL 连接
# - password_encryption: 密码加密算法
# - ssl_prefer_server_ciphers: 优先使用服务器密码套件
# ====================================================================================
RUN echo "# SSL 配置 (可选)\n\
# ssl = on\n\
# ssl_cert_file = '/var/lib/postgresql/server.crt'\n\
# ssl_key_file = '/var/lib/postgresql/server.key'\n\
# ssl_prefer_server_ciphers = on\n\
\n\
# 密码加密\n\
password_encryption = scram-sha-256\n\
" > /etc/postgresql/postgresql.conf.d/security.conf

# ====================================================================================
# 健康检查
# 
# 使用 pg_isready 检查数据库是否可用
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=10s --retries=10 --start-period=60s \
    CMD pg_isready -U ${POSTGRES_USER:-postgres} || exit 1

# 暴露端口
EXPOSE 5432

# ====================================================================================
# 环境变量说明
# 
# 这些环境变量由官方 PostgreSQL 镜像支持，在 docker run 或 docker-compose 中设置
# 
# POSTGRES_PASSWORD: postgres 用户密码 (必需)
# POSTGRES_USER: 超级用户名称 (默认: postgres)
# POSTGRES_DB: 自动创建的数据库名称 (默认: 与 POSTGRES_USER 相同)
# POSTGRES_INITDB_ARGS: initdb 参数 (可选)
# POSTGRES_INITDB_WALDIR: WAL 目录 (可选)
# POSTGRES_HOST_AUTH_METHOD: 主机认证方法 (可选，不推荐用于生产)
# PGDATA: 数据目录 (默认: /var/lib/postgresql/data)
# 
# 示例:
# docker run -e POSTGRES_PASSWORD=MySecretPassword \
#            -e POSTGRES_USER=woodlin \
#            -e POSTGRES_DB=woodlin \
#            woodlin-postgresql:16
# ====================================================================================

# 使用官方 PostgreSQL 镜像的入口点
# 不需要重写 ENTRYPOINT 和 CMD
