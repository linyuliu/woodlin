# Nacos 配置中心快速开始指南

## 概述

本指南帮助您快速开始使用 Nacos 配置中心管理 Woodlin 项目的配置。

## 前提条件

- Java 17+
- Maven 3.8+
- Docker（用于快速启动 Nacos）

## 步骤 1: 启动 Nacos 服务器

使用 Docker 快速启动 Nacos 服务器：

```bash
docker run -d \
  --name nacos-server \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.4.3
```

访问 Nacos 控制台：http://localhost:8848/nacos

默认用户名和密码：`nacos` / `nacos`

## 步骤 2: 导入配置到 Nacos

### 方法一：使用 Web 界面（推荐新手）

1. 登录 Nacos 控制台
2. 点击左侧菜单 **配置管理 > 配置列表**
3. 点击右上角 **+** 按钮
4. 依次创建以下配置：

#### 配置 1: woodlin-dev.yml（环境配置）

- **Data ID**: `woodlin-dev.yml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `docs/nacos-configs/woodlin-dev.yml` 的内容
- **说明**: 包含数据库、Redis、MyBatis 等基础设施配置，切换环境时修改 Data ID（如 woodlin-test.yml, woodlin-prod.yml）

#### 配置 2: woodlin-woodlin-admin.yml（应用配置）

- **Data ID**: `woodlin-woodlin-admin.yml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `docs/nacos-configs/woodlin-woodlin-admin.yml` 的内容
- **说明**: 包含 Sa-Token、Knife4j、业务功能等 woodlin-admin 应用特定配置

### 方法二：使用脚本批量导入

在 `docs/nacos-configs/` 目录下创建导入脚本：

```bash
cd docs/nacos-configs
cat > import-all.sh << 'EOF'
#!/bin/bash

NACOS_SERVER="http://localhost:8848"
NACOS_GROUP="DEFAULT_GROUP"

configs=(
  "woodlin-dev.yml"
  "woodlin-woodlin-admin.yml"
)
configs=(
  "woodlin-basic.yml"
  "woodlin-application.yml"
)

for config in "${configs[@]}"; do
  echo "导入配置: $config"
  content=$(cat "$config")
  curl -X POST "$NACOS_SERVER/nacos/v1/cs/configs" \
    -d "dataId=$config" \
    -d "group=$NACOS_GROUP" \
    -d "content=$content" \
    -d "type=yaml"
  echo -e "\n配置 $config 导入完成\n"
done

echo "所有配置导入完成！"
EOF

chmod +x import-all.sh
./import-all.sh
```

## 步骤 3: 配置环境变量

```bash
# Nacos 服务器地址
export NACOS_SERVER_ADDR=localhost:8848

# Spring Profile
export SPRING_PROFILES_ACTIVE=dev

# 数据库配置（根据实际情况修改）
export DATABASE_URL="jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=Passw0rd

# Redis 配置（根据实际情况修改）
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
```

## 步骤 4: 启动应用

```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
mvn spring-boot:run -pl woodlin-admin
```

或使用开发脚本：

```bash
./scripts/dev.sh backend
```

## 步骤 5: 验证配置

1. 查看应用启动日志，确认从 Nacos 加载配置成功：

```
INFO  c.a.c.n.c.NacosConfigService - [fixed-localhost_8848] [subscribe] woodlin-dev.yml+DEFAULT_GROUP
INFO  c.a.c.n.c.NacosConfigService - [fixed-localhost_8848] [subscribe] woodlin-woodlin-admin.yml+DEFAULT_GROUP
...
```

2. 访问 API 文档：http://localhost:8080/api/doc.html

3. 测试 API 接口是否正常工作

## 禁用 Nacos（可选）

如果需要在不使用 Nacos 的情况下运行（使用本地配置）：

```bash
export NACOS_CONFIG_ENABLED=false
mvn spring-boot:run -pl woodlin-admin
```

或在启动命令中添加参数：

```bash
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.arguments="--spring.cloud.nacos.config.enabled=false"
```

## 配置更新

在 Nacos 控制台修改配置后，应用会自动接收更新（支持动态刷新的配置）。

某些配置（如数据源）需要重启应用才能生效。

## 常见问题

### 1. 连接不到 Nacos 服务器

**检查**:
- Nacos 服务器是否正常运行：`docker ps | grep nacos`
- 端口 8848 是否可访问：`curl http://localhost:8848/nacos`
- 环境变量 `NACOS_SERVER_ADDR` 是否正确

### 2. 配置未加载

**检查**:
- Data ID、Group 是否正确
- 配置格式是否为 YAML
- bootstrap.yml 中的配置列表是否包含该配置
- 查看应用日志获取详细错误信息

### 3. 应用启动失败

**可能原因**:
- Nacos 服务器未启动
- 数据库或 Redis 连接失败
- 配置格式错误

**解决方案**:
- 临时禁用 Nacos 使用本地配置启动
- 检查数据库和 Redis 是否可访问
- 验证配置文件格式

## 下一步

- 阅读完整文档：[docs/NACOS_CONFIGURATION.md](NACOS_CONFIGURATION.md)
- 了解配置详情：[docs/nacos-configs/README.md](nacos-configs/README.md)
- 配置生产环境：使用专用命名空间和集群部署

## 技术支持

如有问题，请查看：
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- 项目 Issues：https://github.com/linyuliu/woodlin/issues
