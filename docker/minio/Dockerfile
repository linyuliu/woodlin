# ====================================================================================
# MinIO RELEASE.2025 - 多架构 Docker 镜像
# 
# 特性:
# - 基于 MinIO 官方镜像 (2025 LTS 版本)
# - 支持多架构: linux/amd64, linux/arm64, linux/arm/v7
# - 预装常用工具
# - 优化性能参数
# - S3 兼容的对象存储服务
# 
# 构建命令 (多架构):
# docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
#   -t woodlin-minio:latest \
#   -f docker/minio/Dockerfile .
# 
# 注意: MinIO 官方镜像使用 RELEASE tag 格式，如 RELEASE.2025-01-01T00-00-00Z
# 这里使用 latest 标签指向最新的 2025 版本
# ====================================================================================

FROM minio/minio:latest

# 设置标签信息
LABEL maintainer="Woodlin Team" \
      description="MinIO Object Storage for Woodlin" \
      version="2025" \
      org.opencontainers.image.title="MinIO (Woodlin Edition)" \
      org.opencontainers.image.description="S3-compatible object storage" \
      org.opencontainers.image.vendor="Woodlin"

# 切换到 root 用户以安装工具
USER root

# ====================================================================================
# 安装常用工具
# 
# MinIO 基础镜像使用较简化的系统，需要安装常用工具
# 
# 工具列表:
# - curl: HTTP 客户端
# - wget: 文件下载工具
# - vim: 文本编辑器
# - nano: 轻量级文本编辑器
# - less: 分页查看工具
# - netcat/nc: 网络调试工具
# - bind-tools: dig/nslookup 等 DNS 工具
# - procps: ps/top 等进程管理工具
# - htop: 交互式进程查看器
# - jq: JSON 处理工具
# ====================================================================================
RUN microdnf install -y \
        curl \
        wget \
        vim \
        nano \
        less \
        nc \
        bind-utils \
        procps-ng \
        htop \
        jq \
        # 时区数据
        tzdata && \
    # 配置时区
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    # 清理缓存
    microdnf clean all && \
    rm -rf /var/cache/yum /tmp/*

# ====================================================================================
# 创建数据目录
# 
# MinIO 数据目录结构:
# - /data: 默认数据目录
# - /data/.minio.sys: MinIO 系统元数据
# ====================================================================================
RUN mkdir -p /data && \
    chown -R minio-user:minio-user /data && \
    chmod 755 /data

# ====================================================================================
# 切换回 minio-user 用户
# 
# MinIO 默认使用 minio-user 用户运行 (UID 1000)
# 符合安全最佳实践
# ====================================================================================
USER minio-user

# 设置工作目录
WORKDIR /opt/minio

# ====================================================================================
# 健康检查
# 
# 使用 MinIO 的健康检查端点
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=30s \
    CMD curl -f http://localhost:9000/minio/health/live || exit 1

# ====================================================================================
# 暴露端口
# 
# - 9000: MinIO API 端口 (S3 兼容 API)
# - 9001: MinIO Console 端口 (Web 管理控制台)
# ====================================================================================
EXPOSE 9000 9001

# ====================================================================================
# 启动命令
# 
# MinIO 启动参数:
# server: 启动服务器模式
# /data: 数据目录
# --console-address ":9001": Console 端口
# 
# 注意:
# - MINIO_ROOT_USER 和 MINIO_ROOT_PASSWORD 通过环境变量设置
# - 可以通过环境变量配置更多参数
# ====================================================================================
ENTRYPOINT ["/usr/bin/docker-entrypoint.sh"]

CMD ["minio", "server", "/data", "--console-address", ":9001"]

# ====================================================================================
# 环境变量说明
# 
# 必需的环境变量:
# - MINIO_ROOT_USER: MinIO 管理员用户名 (默认: minioadmin)
# - MINIO_ROOT_PASSWORD: MinIO 管理员密码 (默认: minioadmin，生产环境必须修改)
# 
# 可选的环境变量:
# - MINIO_VOLUMES: 存储卷路径 (默认: /data)
# - MINIO_BROWSER: 是否启用 Web 控制台 (on/off，默认: on)
# - MINIO_DOMAIN: 域名配置
# - MINIO_REGION: 区域配置 (默认: us-east-1)
# - MINIO_STORAGE_CLASS_STANDARD: 标准存储类配置
# - MINIO_CACHE: 缓存配置
# - MINIO_PROMETHEUS_AUTH_TYPE: Prometheus 认证类型
# - MINIO_PROMETHEUS_URL: Prometheus 端点
# 
# 示例:
# docker run -e MINIO_ROOT_USER=admin \
#            -e MINIO_ROOT_PASSWORD=MySecretPassword \
#            -e MINIO_BROWSER=on \
#            -e MINIO_REGION=cn-north-1 \
#            woodlin-minio:latest
# 
# 高级配置:
# - MINIO_SERVER_URL: 外部访问 URL
# - MINIO_BROWSER_REDIRECT_URL: Console 重定向 URL
# - MINIO_NOTIFY_WEBHOOK: Webhook 通知配置
# - MINIO_NOTIFY_REDIS: Redis 通知配置
# - MINIO_NOTIFY_MYSQL: MySQL 通知配置
# - MINIO_NOTIFY_POSTGRES: PostgreSQL 通知配置
# - MINIO_NOTIFY_KAFKA: Kafka 通知配置
# - MINIO_NOTIFY_RABBITMQ: RabbitMQ 通知配置
# - MINIO_NOTIFY_ELASTICSEARCH: Elasticsearch 通知配置
# 
# 加密配置:
# - MINIO_KMS_SECRET_KEY: KMS 加密密钥
# - MINIO_SSE_MASTER_KEY: 服务端加密主密钥
# 
# 性能优化:
# - MINIO_API_REQUESTS_MAX: 最大 API 请求数
# - MINIO_API_REQUESTS_DEADLINE: API 请求超时时间
# - MINIO_API_CORS_ALLOW_ORIGIN: CORS 允许的源
# 
# 完整文档: https://min.io/docs/minio/linux/reference/minio-server/minio-server.html
# ====================================================================================
