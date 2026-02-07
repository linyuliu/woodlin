# Spring Boot Docker Compose 本地开发

为减少本地环境复杂度，项目新增了根目录 `compose.yaml`（仅 MySQL + Redis），配合 `spring-boot-docker-compose` 使用。

## 1) 一条命令启动后端（自动拉起依赖）

在项目根目录执行：

```bash
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=compose
```

说明：
- Spring Boot 会读取 `compose.yaml`，自动启动 MySQL / Redis。
- 应用使用 `application-compose.yml` 的本地配置，不依赖 Nacos。

## 2) 仅手动管理依赖服务

```bash
docker compose -f compose.yaml up -d
docker compose -f compose.yaml ps
docker compose -f compose.yaml down
```

## 3) 默认连接信息

- MySQL: `localhost:3306`，库 `woodlin`，用户 `woodlin`，密码 `123456`
- Redis: `localhost:6379`，密码 `123456`

如需自定义，直接修改 `compose.yaml` 或通过环境变量覆盖 `application-compose.yml` 中的 `DATABASE_*`、`REDIS_*`。
