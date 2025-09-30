# ====================================================================================
# Woodlin 多租户管理系统 - Docker 镜像构建
# 
# 特性:
# - 基于 BellSoft Liberica JDK 17 (优化的 OpenJDK 发行版)
# - 支持中文字体，防止乱码
# - 内置诊断工具，便于排查问题
# - 多阶段构建，优化镜像大小
# - 安全加固，使用非 root 用户运行
# ====================================================================================

# ====================================================================================
# 阶段 1: 构建阶段 (Builder Stage)
# 使用 BellSoft Liberica JDK 17 完整版进行编译
# ====================================================================================
FROM bellsoft/liberica-openjdk-debian:17 as builder

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 安装 Maven 和必要的构建工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# 编译项目 (跳过测试以加快构建速度)
RUN mvn clean package -DskipTests -q

# ====================================================================================
# 阶段 2: 运行时镜像 (Runtime Stage)
# 使用 BellSoft Liberica JRE 17 精简版作为运行时基础镜像
# ====================================================================================
FROM bellsoft/liberica-openjre-debian:17

# 设置环境变量
ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    DEBIAN_FRONTEND=noninteractive

# ====================================================================================
# 安装系统依赖和工具
# 
# 字体支持:
# - fontconfig: 字体配置库
# - fonts-wqy-zenhei: 文泉驿正黑体 (中文黑体字体)
# - fonts-wqy-microhei: 文泉驿微米黑 (中文无衬线字体)
# - fonts-dejavu-core: DejaVu 字体 (英文字体)
# 
# 诊断工具:
# - curl: HTTP 客户端，用于健康检查和 API 调用
# - wget: 文件下载工具
# - netcat-openbsd (nc): 网络调试工具
# - telnet: 端口连接测试
# - iputils-ping: ping 命令
# - dnsutils: dig/nslookup 等 DNS 工具
# - net-tools: netstat/ifconfig 等网络工具
# - procps: ps/top 等进程管理工具
# - vim-tiny: 轻量级文本编辑器
# - jq: JSON 处理工具
# - less: 分页查看工具
# - tzdata: 时区数据
# - locales: 本地化支持
# ====================================================================================
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        # 中文字体支持
        fontconfig \
        fonts-wqy-zenhei \
        fonts-wqy-microhei \
        fonts-dejavu-core \
        # 诊断工具
        curl \
        wget \
        netcat-openbsd \
        telnet \
        iputils-ping \
        dnsutils \
        net-tools \
        procps \
        vim-tiny \
        jq \
        less \
        # 系统工具
        tzdata \
        locales && \
    # 配置中文本地化
    echo "zh_CN.UTF-8 UTF-8" > /etc/locale.gen && \
    locale-gen zh_CN.UTF-8 && \
    update-locale LANG=zh_CN.UTF-8 && \
    # 配置时区
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    # 更新字体缓存
    fc-cache -fv && \
    # 清理 apt 缓存，减小镜像体积
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 创建应用目录和日志目录
WORKDIR /app
RUN mkdir -p /app/logs /app/temp

# 复制编译好的 jar 包
COPY --from=builder /app/woodlin-admin/target/woodlin-admin-1.0.0.jar app.jar

# ====================================================================================
# 安全加固: 创建非 root 用户运行应用
# 
# 为什么使用非 root 用户:
# - 降低安全风险，即使应用被攻击也无法获得 root 权限
# - 符合容器安全最佳实践
# - 符合 Kubernetes Pod Security Standards
# ====================================================================================
RUN groupadd --system --gid 1000 woodlin && \
    useradd --system --uid 1000 --gid woodlin --shell /bin/bash --home /app woodlin && \
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
# ====================================================================================
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# ====================================================================================
# JVM 启动参数说明
# 
# 内存配置:
# -Xms512m: 初始堆内存 512MB
# -Xmx1024m: 最大堆内存 1GB
# -XX:MetaspaceSize=128m: Metaspace 初始大小 128MB
# -XX:MaxMetaspaceSize=256m: Metaspace 最大大小 256MB
# -XX:+UseG1GC: 使用 G1 垃圾回收器 (适合大堆内存)
# -XX:MaxGCPauseMillis=200: GC 最大暂停时间目标 200ms
# -XX:+HeapDumpOnOutOfMemoryError: OOM 时生成堆转储文件
# -XX:HeapDumpPath=/app/logs/: 堆转储文件路径
# 
# 性能优化:
# -server: 使用服务器模式 JVM
# -XX:+UseStringDeduplication: 启用字符串去重 (G1GC 特性)
# -XX:+ParallelRefProcEnabled: 并行处理引用对象
# -XX:+DisableExplicitGC: 禁用显式 GC 调用 (System.gc())
# 
# 诊断和监控:
# -XX:+PrintGCDetails: 打印 GC 详细信息
# -XX:+PrintGCDateStamps: 打印 GC 时间戳
# -Xloggc:/app/logs/gc.log: GC 日志文件路径
# -XX:+UseGCLogFileRotation: 启用 GC 日志滚动
# -XX:NumberOfGCLogFiles=10: GC 日志文件数量
# -XX:GCLogFileSize=10M: 单个 GC 日志文件大小
# 
# 系统配置:
# -Djava.security.egd=file:/dev/./urandom: 使用非阻塞随机数生成器
# -Dfile.encoding=UTF-8: 文件编码 UTF-8
# -Duser.timezone=Asia/Shanghai: 时区设置为上海
# -Djava.io.tmpdir=/app/temp: 临时文件目录
# -Djava.awt.headless=true: 无头模式 (服务器环境)
# ====================================================================================
ENTRYPOINT ["java", \
    # JVM 模式
    "-server", \
    # 内存配置
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:MetaspaceSize=128m", \
    "-XX:MaxMetaspaceSize=256m", \
    # GC 配置
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+UseStringDeduplication", \
    "-XX:+ParallelRefProcEnabled", \
    "-XX:+DisableExplicitGC", \
    # OOM 处理
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/", \
    # GC 日志
    "-XX:+PrintGCDetails", \
    "-XX:+PrintGCDateStamps", \
    "-Xloggc:/app/logs/gc.log", \
    "-XX:+UseGCLogFileRotation", \
    "-XX:NumberOfGCLogFiles=10", \
    "-XX:GCLogFileSize=10M", \
    # 系统配置
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-Duser.timezone=Asia/Shanghai", \
    "-Djava.io.tmpdir=/app/temp", \
    "-Djava.awt.headless=true", \
    # 应用 JAR
    "-jar", \
    "app.jar"]