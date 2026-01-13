# Nacos 配置导入说明
# 作者: mumu
# 描述: 指导如何将配置导入到 Nacos 配置中心

## 配置文件说明

此目录包含了 Woodlin 项目在 Nacos 配置中心需要的配置文件。配置已简化为 2 个文件，便于维护。

### 配置文件列表

1. **woodlin-dev.yml** - 环境配置（开发环境示例）
   - Data ID: woodlin-{env}.yml（根据环境自动加载，如 woodlin-dev.yml, woodlin-test.yml, woodlin-prod.yml）
   - Group: DEFAULT_GROUP
   - 格式: YAML
   - 说明: 包含数据库、Redis、MyBatis 等基础设施配置，这些配置在不同环境可能需要修改

2. **woodlin-woodlin-admin.yml** - 应用配置
   - Data ID: woodlin-woodlin-admin.yml
   - Group: DEFAULT_GROUP
   - 格式: YAML
   - 说明: 包含 Sa-Token、Knife4j、业务功能等 woodlin-admin 应用特定配置

## 导入步骤

### 1. 启动 Nacos 服务器

**使用 Docker 启动**:
```bash
docker run -d \
  --name nacos-server \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.4.3
```

访问 Nacos 控制台: http://localhost:8848/nacos
默认账号密码: nacos/nacos

### 2. 手动导入配置

#### 方法一：通过 Web 界面导入（推荐）

1. 登录 Nacos 控制台
2. 点击左侧菜单 **配置管理 > 配置列表**
3. 点击右上角 **+** 按钮创建配置
4. 填写表单：
   - **Data ID**: 对应文件名（如 woodlin-dev.yml, woodlin-woodlin-admin.yml）
   - **Group**: DEFAULT_GROUP
   - **配置格式**: YAML
   - **配置内容**: 复制对应文件的内容
5. 点击 **发布** 按钮

对每个配置文件重复步骤 3-5。

#### 方法二：使用 Nacos Open API 批量导入

创建一个导入脚本 `import-configs.sh`:

```bash
#!/bin/bash

NACOS_SERVER="http://localhost:8848"
NACOS_NAMESPACE=""
NACOS_GROUP="DEFAULT_GROUP"
NACOS_USERNAME="nacos"
NACOS_PASSWORD="nacos"

# 配置文件列表
configs=(
  "woodlin-dev.yml"
  "woodlin-woodlin-admin.yml"
)

for config in "${configs[@]}"; do
  echo "导入配置: $config"
  
  # 读取配置文件内容
  content=$(cat "$config")
  
  # 调用 Nacos API 发布配置
  curl -X POST "$NACOS_SERVER/nacos/v1/cs/configs" \
    -d "dataId=$config" \
    -d "group=$NACOS_GROUP" \
    -d "content=$content" \
    -d "type=yaml" \
    -d "username=$NACOS_USERNAME" \
    -d "password=$NACOS_PASSWORD"
  
  echo -e "\n配置 $config 导入完成\n"
done

echo "所有配置导入完成！"
```

运行脚本:
```bash
chmod +x import-configs.sh
./import-configs.sh
```

### 3. 验证配置

1. 在 Nacos 控制台的 **配置管理 > 配置列表** 中，确认所有配置都已成功创建
2. 检查每个配置的内容是否正确
3. 确认配置格式为 YAML

### 4. 环境变量配置

在启动应用前，配置以下环境变量：

```bash
# Nacos 服务器地址
export NACOS_SERVER_ADDR=localhost:8848

# Nacos 用户名和密码（如果开启了鉴权）
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos

# Nacos 命名空间（可选，用于环境隔离）
export NACOS_NAMESPACE=

# Nacos 分组（可选）
export NACOS_GROUP=DEFAULT_GROUP

# Spring Profile
export SPRING_PROFILES_ACTIVE=dev
```

### 5. 启动应用

启动 Woodlin 应用，应用会自动从 Nacos 加载配置：

```bash
mvn spring-boot:run -pl woodlin-admin
```

或使用开发脚本：

```bash
./scripts/dev.sh backend
```

## 配置管理

### 更新配置

1. 在 Nacos 控制台中找到要更新的配置
2. 点击 **编辑** 按钮
3. 修改配置内容
4. 点击 **发布** 按钮

应用会自动接收配置更新（如果配置了 `refresh: true`）。

### 配置版本管理

Nacos 自动保存配置的历史版本：

1. 在配置列表中点击配置的 **详情**
2. 点击 **历史版本** 标签页
3. 可以查看、比较和回滚到任何历史版本

### 配置克隆

对于不同环境（dev, test, prod），可以克隆配置：

1. 在配置列表中找到要克隆的配置
2. 点击 **克隆** 按钮
3. 选择目标命名空间
4. 修改 Data ID（如 woodlin-admin-dev.yml -> woodlin-admin-prod.yml）
5. 修改配置内容（如数据库地址、Redis地址等）
6. 点击 **克隆** 按钮

## 环境隔离

建议为不同环境创建不同的命名空间：

- **开发环境**: 使用 public 命名空间或创建 dev 命名空间
- **测试环境**: 创建 test 命名空间
- **生产环境**: 创建 production 命名空间

在 bootstrap-{profile}.yml 中配置对应的命名空间即可。

## 注意事项

1. **敏感信息保护**: 数据库密码、Redis密码等敏感信息应使用环境变量或加密存储
2. **配置刷新**: 某些配置（如数据源、连接池）修改后需要重启应用才能生效
3. **配置备份**: 重要配置修改前应备份或记录历史版本
4. **权限控制**: 生产环境应开启 Nacos 的鉴权功能，并严格控制配置的访问权限
5. **配置格式**: 确保配置格式为 YAML，避免格式错误导致配置加载失败
6. **Token超时配置**: Sa-Token的 `activeTimeout` 默认设置为 `-1`（禁用活动超时），token只在 `timeout`（30天）后过期。如需启用活动超时，建议设置为86400秒（24小时）或更长，避免用户频繁需要重新登录

## 故障排查

如果配置加载失败，检查：

1. Nacos 服务器是否正常运行
2. NACOS_SERVER_ADDR 环境变量是否正确
3. Data ID、Group、命名空间是否匹配
4. 配置格式是否为 YAML
5. 查看应用启动日志获取详细错误信息

## 参考资料

- [Nacos 配置管理文档](https://nacos.io/zh-cn/docs/config.html)
- [Nacos Open API 文档](https://nacos.io/zh-cn/docs/open-api.html)
