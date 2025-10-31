# ====================================================================================
# Woodlin 多租户管理系统 - 多架构 Docker 镜像
# 
# 特性:
# - 基于 BellSoft Liberica JDK 21 LTS (长期支持版本，带 CDS 优化)
# - 支持多架构: linux/amd64, linux/arm64, linux/arm/v7
# - 包含常用诊断工具和中文字体
# - 优化内核参数 (打开文件数等)
# - 多阶段构建优化镜像大小和层缓存
# - 安全加固，非 root 用户运行
# - CDS (Class Data Sharing) 优化启动速度
# 
# 构建命令 (多架构):
# docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 \
#   -t woodlin-app:latest \
#   -f docker/app/Dockerfile .
# 
# 构建命令 (单架构):
# docker build -t woodlin-app:latest -f docker/app/Dockerfile .
# ====================================================================================

# ====================================================================================
# 阶段 1: 依赖缓存层
# 目的: 将依赖下载单独作为一层，利用 Docker 缓存加速构建
# ====================================================================================
FROM bellsoft/liberica-openjdk-debian:21-cds AS deps

WORKDIR /build

# 只复制 pom.xml 文件，利用 Docker 缓存
COPY pom.xml .
COPY woodlin-dependencies/pom.xml ./woodlin-dependencies/
COPY woodlin-common/pom.xml ./woodlin-common/
COPY woodlin-security/pom.xml ./woodlin-security/
COPY woodlin-system/pom.xml ./woodlin-system/
COPY woodlin-tenant/pom.xml ./woodlin-tenant/
COPY woodlin-file/pom.xml ./woodlin-file/
COPY woodlin-task/pom.xml ./woodlin-task/
COPY woodlin-generator/pom.xml ./woodlin-generator/
COPY woodlin-dsl/pom.xml ./woodlin-dsl/
COPY woodlin-sql2api/pom.xml ./woodlin-sql2api/
COPY woodlin-admin/pom.xml ./woodlin-admin/

# 安装 Maven
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# 下载依赖 (这一层会被缓存，除非 pom.xml 改变)
RUN mvn dependency:go-offline -B -q || true

# ====================================================================================
# 阶段 2: 构建阶段
# 使用 BellSoft Liberica JDK 21 完整版进行编译
# ====================================================================================
FROM bellsoft/liberica-openjdk-debian:21-cds AS builder

WORKDIR /build

# 从依赖缓存层复制 Maven 本地仓库
COPY --from=deps /root/.m2 /root/.m2

# 安装 Maven
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# 复制所有源代码
COPY . .

# 编译项目 (跳过测试以加快构建速度)
# -q: 安静模式，减少日志输出
# -T 1C: 使用 1 倍 CPU 核心数进行并行编译
RUN mvn clean package -DskipTests -q -T 1C

# ====================================================================================
# 阶段 3: 运行时镜像
# 使用 BellSoft Liberica JRE 21 Alpine 精简版作为运行时基础镜像 (带 CDS 优化)
# ====================================================================================
FROM bellsoft/liberica-openjre-alpine:21-cds

# 设置标签信息
LABEL maintainer="Woodlin Team" \
      description="Woodlin Multi-tenant Management System" \
      version="1.0.0" \
      org.opencontainers.image.title="Woodlin Application" \
      org.opencontainers.image.description="Spring Boot 3 + Java 21 LTS multi-tenant system with CDS optimization" \
      org.opencontainers.image.vendor="Woodlin" \
      org.opencontainers.image.url="https://github.com/linyuliu/woodlin"

# 设置环境变量
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8 \
    TZ=Asia/Shanghai

# ====================================================================================
# 安装系统依赖和工具
# 
# 分层安装策略: 将不常变化的包放在前面，利用 Docker 缓存
# 
# 基础工具层:
# - bash: Bash shell (Alpine 默认使用 ash)
# - curl: HTTP 客户端，用于健康检查和 API 调用
# - wget: 文件下载工具
# - ca-certificates: SSL/TLS 证书
# - tzdata: 时区数据
# 
# 网络诊断工具层:
# - netcat-openbsd (nc): 网络调试工具
# - bind-tools: dig/nslookup 等 DNS 工具
# - iputils: ping/traceroute 等网络工具
# - net-tools: netstat/ifconfig 等网络工具 (Alpine 可能不可用，使用 iproute2)
# - iproute2: ip/ss 等现代网络工具
# - tcpdump: 网络抓包工具
# 
# 系统诊断工具层:
# - procps: ps/top 等进程管理工具
# - htop: 交互式进程查看器
# - lsof: 列出打开文件
# - strace: 系统调用追踪工具
# 
# 文本处理工具层:
# - vim: 文本编辑器
# - nano: 轻量级文本编辑器
# - less: 分页查看工具
# - jq: JSON 处理工具
# - grep: 文本搜索工具
# - sed: 流编辑器
# - awk: 文本处理工具
# 
# 中文字体支持层:
# - fontconfig: 字体配置库
# - ttf-dejavu: DejaVu 字体 (英文字体)
# - wqy-zenhei: 文泉驿正黑体 (中文黑体字体)
# 
# 注意: Alpine 包管理器是 apk，Debian/Ubuntu 是 apt-get
# ====================================================================================
RUN apk add --no-cache \
    # 基础工具
    bash \
    curl \
    wget \
    ca-certificates \
    tzdata \
    # 网络诊断工具
    netcat-openbsd \
    bind-tools \
    iputils \
    iproute2 \
    tcpdump \
    # 系统诊断工具
    procps \
    htop \
    lsof \
    strace \
    # 文本处理工具
    vim \
    nano \
    less \
    jq \
    grep \
    sed \
    gawk \
    # 中文字体支持
    fontconfig \
    ttf-dejavu && \
    # 下载并安装文泉驿字体
    # 注意: 如果网络不可达，可以将字体文件添加到项目并使用 COPY 命令
    mkdir -p /usr/share/fonts/wqy && \
    cd /usr/share/fonts/wqy && \
    (wget -q --timeout=30 https://github.com/anthonyfok/fonts-wqy-zenhei/raw/master/wqy-zenhei.ttc || \
     echo "Warning: Could not download font, continuing without it") && \
    # 配置时区
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    # 更新字体缓存
    fc-cache -fv && \
    # 清理缓存
    rm -rf /var/cache/apk/* /tmp/*

# ====================================================================================
# 内核参数优化
# 
# 说明: 这些参数在容器内设置可能不生效，需要在宿主机或 docker-compose 中设置
# 但我们在 Dockerfile 中记录推荐配置
# 
# 容器运行时需要设置的参数:
# 
# docker run --ulimit nofile=65536:65536 --ulimit nproc=65536:65536 ...
# 
# 或在 docker-compose.yml 中:
# ulimits:
#   nofile:
#     soft: 65536
#     hard: 65536
#   nproc:
#     soft: 65536
#     hard: 65536
# 
# 宿主机系统优化 (需要 root 权限):
# 
# 1. 编辑 /etc/security/limits.conf
# * soft nofile 65536
# * hard nofile 65536
# * soft nproc 65536
# * hard nproc 65536
# 
# 2. 编辑 /etc/sysctl.conf
# fs.file-max = 2097152
# fs.nr_open = 2097152
# net.core.somaxconn = 32768
# net.ipv4.tcp_max_syn_backlog = 8192
# net.ipv4.tcp_tw_reuse = 1
# net.ipv4.ip_local_port_range = 10000 65535
# vm.max_map_count = 262144
# 
# 3. 应用配置
# sysctl -p
# ====================================================================================

# 创建应用目录和日志目录
WORKDIR /app
RUN mkdir -p /app/logs /app/temp /app/config

# 复制编译好的 jar 包
COPY --from=builder /build/woodlin-admin/target/woodlin-admin-*.jar /app/app.jar

# ====================================================================================
# 安全加固: 创建非 root 用户运行应用
# 
# 为什么使用非 root 用户:
# - 降低安全风险，即使应用被攻击也无法获得 root 权限
# - 符合容器安全最佳实践
# - 符合 Kubernetes Pod Security Standards
# - 通过 CIS Docker Benchmark 安全检查
# ====================================================================================
RUN addgroup -g 1000 woodlin && \
    adduser -D -u 1000 -G woodlin -h /app -s /bin/bash woodlin && \
    chown -R woodlin:woodlin /app

# 切换到非 root 用户
USER woodlin

# ====================================================================================
# 健康检查配置
# 
# 参数说明:
# --interval=30s: 每 30 秒执行一次健康检查
# --timeout=10s: 健康检查超时时间 10 秒
# --start-period=90s: 容器启动后 90 秒内的健康检查失败不计入重试次数
# --retries=3: 连续 3 次健康检查失败则认为容器不健康
# 
# 健康检查端点: Spring Boot Actuator 的 health 端点
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# ====================================================================================
# JVM 启动参数说明
# 
# 容器感知配置 (Java 10+):
# -XX:+UseContainerSupport: 启用容器资源感知
# -XX:MaxRAMPercentage=75.0: 使用容器内存的 75% 作为堆内存
# -XX:InitialRAMPercentage=50.0: 初始堆内存为容器内存的 50%
# -XX:MinRAMPercentage=50.0: 最小堆内存为容器内存的 50%
# 
# GC 配置:
# -XX:+UseG1GC: 使用 G1 垃圾回收器 (适合大堆内存)
# -XX:MaxGCPauseMillis=200: GC 最大暂停时间目标 200ms
# -XX:+UseStringDeduplication: 启用字符串去重 (G1GC 特性)
# -XX:+ParallelRefProcEnabled: 并行处理引用对象
# -XX:+DisableExplicitGC: 禁用显式 GC 调用 (System.gc())
# -XX:G1HeapRegionSize=16M: G1 堆区域大小 16MB
# -XX:G1ReservePercent=10: G1 预留空间百分比 10%
# -XX:G1NewSizePercent=30: 新生代最小占比 30%
# -XX:G1MaxNewSizePercent=50: 新生代最大占比 50%
# 
# OOM 处理:
# -XX:+HeapDumpOnOutOfMemoryError: OOM 时生成堆转储文件
# -XX:HeapDumpPath=/app/logs/: 堆转储文件路径
# -XX:+ExitOnOutOfMemoryError: OOM 时退出 JVM (让容器重启)
# 
# GC 日志 (Java 9+ 统一日志):
# -Xlog:gc*,gc+age=trace,safepoint:file=/app/logs/gc.log:utctime,pid,tags:filecount=10,filesize=10M
# 
# 性能优化:
# -server: 使用服务器模式 JVM
# -XX:+AlwaysPreTouch: 启动时预分配内存 (减少运行时页面错误)
# -XX:+UseNUMA: 启用 NUMA 感知内存分配
# -XX:+OptimizeStringConcat: 优化字符串连接
# 
# 诊断和监控:
# -XX:+UnlockDiagnosticVMOptions: 解锁诊断选项
# -XX:+PrintFlagsFinal: 打印所有 JVM 参数
# -XX:NativeMemoryTracking=summary: 启用本地内存跟踪
# -Dcom.sun.management.jmxremote: 启用 JMX 远程监控
# -Dcom.sun.management.jmxremote.port=9010: JMX 端口
# -Dcom.sun.management.jmxremote.authenticate=false: 禁用 JMX 认证 (仅开发环境)
# -Dcom.sun.management.jmxremote.ssl=false: 禁用 JMX SSL (仅开发环境)
# 
# 系统配置:
# -Djava.security.egd=file:/dev/./urandom: 使用非阻塞随机数生成器
# -Dfile.encoding=UTF-8: 文件编码 UTF-8
# -Duser.timezone=Asia/Shanghai: 时区设置为上海
# -Djava.io.tmpdir=/app/temp: 临时文件目录
# -Djava.awt.headless=true: 无头模式 (服务器环境)
# -Dnetworkaddress.cache.ttl=60: DNS 缓存时间 60 秒
# -Dsun.net.inetaddr.ttl=60: DNS 缓存时间 60 秒
# 
# Spring Boot 配置:
# -Dspring.config.location=classpath:/application.yml,file:/app/config/: 配置文件路径
# -Dlogging.file.path=/app/logs: 日志文件路径
# ====================================================================================
ENTRYPOINT ["java", \
    # JVM 模式
    "-server", \
    # 容器资源感知
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:MinRAMPercentage=50.0", \
    # GC 配置
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+UseStringDeduplication", \
    "-XX:+ParallelRefProcEnabled", \
    "-XX:+DisableExplicitGC", \
    "-XX:G1HeapRegionSize=16M", \
    "-XX:G1ReservePercent=10", \
    "-XX:G1NewSizePercent=30", \
    "-XX:G1MaxNewSizePercent=50", \
    # OOM 处理
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/", \
    "-XX:+ExitOnOutOfMemoryError", \
    # GC 日志
    "-Xlog:gc*,gc+age=trace,safepoint:file=/app/logs/gc.log:utctime,pid,tags:filecount=10,filesize=10M", \
    # 性能优化
    "-XX:+AlwaysPreTouch", \
    "-XX:+UseNUMA", \
    "-XX:+OptimizeStringConcat", \
    # 系统配置
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-Duser.timezone=Asia/Shanghai", \
    "-Djava.io.tmpdir=/app/temp", \
    "-Djava.awt.headless=true", \
    "-Dnetworkaddress.cache.ttl=60", \
    "-Dsun.net.inetaddr.ttl=60", \
    # Spring Boot 配置
    "-Dspring.config.location=classpath:/application.yml,file:/app/config/", \
    "-Dlogging.file.path=/app/logs", \
    # 应用 JAR
    "-jar", \
    "app.jar"]
