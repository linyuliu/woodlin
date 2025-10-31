# ====================================================================================
# MySQL 8.0 LTS - 多架构 Docker 镜像
# 
# 特性:
# - 基于 MySQL 8.0 官方镜像 (Oracle 维护的 LTS 版本)
# - 支持多架构: linux/amd64, linux/arm64
# - 预装常用工具和中文字符集
# - 优化性能参数和安全配置
# - 自动初始化数据库脚本
# 
# 构建命令 (多架构):
# docker buildx build --platform linux/amd64,linux/arm64 \
#   -t woodlin-mysql:8.0 \
#   -f docker/mysql/Dockerfile .
# ====================================================================================

FROM mysql:8.0

# 设置标签信息
LABEL maintainer="Woodlin Team" \
      description="MySQL 8.0 for Woodlin with Chinese support" \
      version="8.0" \
      org.opencontainers.image.title="MySQL 8.0 (Woodlin Edition)" \
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
# - gzip/tar: 压缩/解压工具 (备份恢复)
# - mycli: MySQL CLI 工具 (可选，增强型命令行)
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
        gzip \
        tar \
        # mycli 是 Python 工具，需要 pip
        # python3-pip && \
        # pip3 install --no-cache-dir mycli && \
    && \
    # 清理缓存
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# ====================================================================================
# 配置中文字符集
# 
# MySQL 8.0 默认使用 utf8mb4，但我们明确配置以确保一致性
# 
# 创建自定义配置文件 /etc/mysql/conf.d/chinese.cnf
# 此配置会覆盖默认配置
# ====================================================================================
RUN echo '[client]\n\
default-character-set=utf8mb4\n\
\n\
[mysql]\n\
default-character-set=utf8mb4\n\
\n\
[mysqld]\n\
# 字符集配置\n\
character-set-server=utf8mb4\n\
collation-server=utf8mb4_unicode_ci\n\
init-connect="SET NAMES utf8mb4"\n\
\n\
# 时区配置\n\
default-time-zone="+08:00"\n\
\n\
# 大小写敏感配置 (0=区分大小写, 1=不区分大小写)\n\
# 建议在 Linux 上使用 1，便于跨平台开发\n\
lower_case_table_names=1\n\
\n\
# SQL 模式配置\n\
# 移除 ONLY_FULL_GROUP_BY (兼容老代码)\n\
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION\n\
' > /etc/mysql/conf.d/chinese.cnf

# ====================================================================================
# 性能优化配置
# 
# 创建 /etc/mysql/conf.d/performance.cnf
# 
# 参数说明:
# - max_connections: 最大连接数 (根据业务负载调整)
# - max_allowed_packet: 最大数据包大小 (支持大字段和批量导入)
# - innodb_buffer_pool_size: InnoDB 缓冲池大小 (建议设置为内存的 50-80%)
# - innodb_log_file_size: InnoDB 日志文件大小 (影响写入性能)
# - innodb_flush_log_at_trx_commit: 事务提交时的刷盘策略
#   - 0: 每秒刷盘，性能最好，但可能丢失 1 秒数据
#   - 1: 每次提交刷盘，最安全，性能较差
#   - 2: 每次提交写入 OS 缓存，每秒刷盘，折中方案 (推荐)
# - innodb_flush_method: InnoDB 刷盘方法 (O_DIRECT 避免双重缓存)
# - innodb_io_capacity: InnoDB IO 容量 (SSD 建议 1000-2000)
# - innodb_read_io_threads: InnoDB 读 IO 线程数
# - innodb_write_io_threads: InnoDB 写 IO 线程数
# - table_open_cache: 表缓存数量
# - thread_cache_size: 线程缓存数量
# - query_cache_type: 查询缓存类型 (MySQL 8.0 已移除此功能)
# - tmp_table_size: 内存临时表大小
# - max_heap_table_size: 内存表最大大小
# ====================================================================================
RUN echo '[mysqld]\n\
# 连接配置\n\
max_connections=500\n\
max_connect_errors=1000\n\
max_allowed_packet=64M\n\
\n\
# InnoDB 配置\n\
innodb_buffer_pool_size=512M\n\
innodb_buffer_pool_instances=4\n\
innodb_log_file_size=256M\n\
innodb_log_buffer_size=16M\n\
innodb_flush_log_at_trx_commit=2\n\
innodb_flush_method=O_DIRECT\n\
innodb_io_capacity=1000\n\
innodb_io_capacity_max=2000\n\
innodb_read_io_threads=4\n\
innodb_write_io_threads=4\n\
innodb_file_per_table=1\n\
innodb_open_files=2000\n\
\n\
# 缓存配置\n\
table_open_cache=2000\n\
table_definition_cache=2000\n\
thread_cache_size=100\n\
\n\
# 临时表配置\n\
tmp_table_size=64M\n\
max_heap_table_size=64M\n\
\n\
# 慢查询日志\n\
slow_query_log=1\n\
slow_query_log_file=/var/lib/mysql/slow.log\n\
long_query_time=2\n\
log_queries_not_using_indexes=1\n\
log_throttle_queries_not_using_indexes=10\n\
\n\
# 二进制日志 (用于主从复制和数据恢复)\n\
server-id=1\n\
log_bin=/var/lib/mysql/mysql-bin\n\
binlog_format=ROW\n\
binlog_expire_logs_seconds=604800\n\
max_binlog_size=100M\n\
\n\
# 其他优化\n\
back_log=500\n\
wait_timeout=28800\n\
interactive_timeout=28800\n\
skip-name-resolve=1\n\
' > /etc/mysql/conf.d/performance.cnf

# ====================================================================================
# 安全配置
# 
# 创建 /etc/mysql/conf.d/security.cnf
# 
# 安全措施:
# - skip-name-resolve: 跳过 DNS 解析，提高连接速度，防止 DNS 欺骗
# - local_infile=0: 禁用 LOAD DATA LOCAL INFILE (防止读取本地文件)
# - secure_file_priv: 限制文件导入导出路径
# - symbolic-links=0: 禁用符号链接 (防止文件系统攻击)
# ====================================================================================
RUN echo '[mysqld]\n\
# 跳过 DNS 解析\n\
skip-name-resolve=1\n\
\n\
# 禁用 LOCAL INFILE\n\
local_infile=0\n\
\n\
# 文件导入导出限制\n\
secure_file_priv=/var/lib/mysql-files\n\
\n\
# 禁用符号链接\n\
symbolic-links=0\n\
' > /etc/mysql/conf.d/security.cnf

# ====================================================================================
# 创建数据目录和权限
# ====================================================================================
RUN mkdir -p /var/lib/mysql-files && \
    chown -R mysql:mysql /var/lib/mysql-files && \
    chmod 750 /var/lib/mysql-files

# ====================================================================================
# 复制初始化脚本
# 
# 将 SQL 脚本复制到 /docker-entrypoint-initdb.d/
# MySQL 容器启动时会自动执行此目录下的 .sql, .sql.gz, .sh 文件
# 
# 注意: 
# - 脚本按文件名字典序执行
# - 建议使用数字前缀控制执行顺序，如: 01-schema.sql, 02-data.sql
# - 脚本只在数据库首次初始化时执行
# ====================================================================================
# COPY sql/*.sql /docker-entrypoint-initdb.d/
# 由于构建上下文是根目录，需要从根目录复制
# 在实际使用时，用户应该通过 volume 挂载 SQL 脚本

# ====================================================================================
# 健康检查
# 
# 使用 mysqladmin ping 检查数据库是否可用
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=10s --retries=10 --start-period=60s \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1

# 暴露端口
EXPOSE 3306

# ====================================================================================
# 环境变量说明
# 
# 这些环境变量由官方 MySQL 镜像支持，在 docker run 或 docker-compose 中设置
# 
# MYSQL_ROOT_PASSWORD: root 用户密码 (必需)
# MYSQL_DATABASE: 自动创建的数据库名称 (可选)
# MYSQL_USER: 自动创建的普通用户 (可选)
# MYSQL_PASSWORD: 普通用户密码 (可选)
# MYSQL_RANDOM_ROOT_PASSWORD: 生成随机 root 密码 (可选)
# MYSQL_ONETIME_PASSWORD: root 用户首次登录后必须修改密码 (可选)
# MYSQL_INITDB_SKIP_TZINFO: 跳过时区表初始化 (可选)
# 
# 示例:
# docker run -e MYSQL_ROOT_PASSWORD=MySecretPassword \
#            -e MYSQL_DATABASE=woodlin \
#            -e MYSQL_USER=woodlin \
#            -e MYSQL_PASSWORD=woodlin123 \
#            woodlin-mysql:8.0
# ====================================================================================

# 使用官方 MySQL 镜像的入口点
# 不需要重写 ENTRYPOINT 和 CMD
