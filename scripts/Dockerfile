# 使用多阶段构建
FROM openjdk:17-jdk-slim as builder

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 安装Maven
RUN apt-get update && apt-get install -y maven

# 编译项目
RUN mvn clean package -DskipTests -q

# 运行时镜像
FROM openjdk:17-jre-slim

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

# 创建应用目录
WORKDIR /app

# 复制jar包
COPY --from=builder /app/woodlin-admin/target/woodlin-admin-1.0.0.jar app.jar

# 创建非root用户
RUN addgroup --system woodlin && adduser --system --ingroup woodlin woodlin
RUN chown -R woodlin:woodlin /app
USER woodlin

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", \
    "-server", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-Duser.timezone=Asia/Shanghai", \
    "-jar", \
    "app.jar"]