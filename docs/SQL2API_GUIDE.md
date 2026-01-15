# SQL2API 功能文档

## 功能概述

SQL2API 是 Woodlin 系统的动态 API 生成模块，允许通过配置 SQL 语句和参数来快速生成 RESTful API 接口，无需编写代码。

### 核心特性

1. **SQL 直接构建 API**：通过配置增删改查 SQL 和参数即可生成 API
2. **多数据库支持**：支持 MySQL、PostgreSQL、Oracle 等 20+ 种数据库
3. **简化DSL语法**：提供比 MyBatis 更简单的参数绑定语法
4. **自动元数据提取**：自动提取数据库、表、字段等元数据信息
5. **API 编排**：支持多个 API 之间的编排和数据流转
6. **安全认证**：支持 Token、API Key 等多种认证方式
7. **性能优化**：支持 Redis 缓存和 Sentinel 流控
8. **加密支持**：可配置 AES、RSA、SM4 等加密算法

## 架构设计

### SPI 架构

SQL2API 采用 SPI（Service Provider Interface）机制实现数据库元数据提取，支持扩展：

```text
DatabaseMetadataExtractor (SPI接口)
├── MySQLMetadataExtractor (MySQL实现)
├── PostgreSQLMetadataExtractor (PostgreSQL实现)
├── OracleMetadataExtractor (Oracle实现)
└── 自定义数据库实现...
```

### 模块结构

```text
woodlin-sql2api/
├── spi/                    # SPI接口和实现
│   ├── DatabaseMetadataExtractor.java
│   └── impl/
│       └── MySQLMetadataExtractor.java
├── model/                  # 元数据模型
│   ├── DatabaseMetadata.java
│   ├── SchemaMetadata.java
│   ├── TableMetadata.java
│   └── ColumnMetadata.java
├── entity/                 # 配置实体
│   ├── SqlApiConfig.java
│   └── ApiOrchestration.java
├── dsl/                    # 简化DSL
│   ├── SqlParam.java
│   └── SqlDslParser.java
├── service/                # 核心服务
│   ├── DatabaseMetadataService.java
│   └── DynamicSqlService.java
└── config/                 # 配置类
    └── Sql2ApiConfiguration.java
```

## 快速开始

### 1. 数据库准备

执行 SQL 脚本创建必要的表：

```bash
mysql -u root -p woodlin < sql/mysql/woodlin_schema.sql
```

### 2. 配置数据源

在 `application.yml` 中配置动态数据源：

::: code-tabs#config

@tab application.yml

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
```

@tab 环境变量

```bash
export SPRING_DATASOURCE_DYNAMIC_PRIMARY=master
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL=jdbc:mysql://localhost:3306/woodlin
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_USERNAME=root
export SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD=123456
```

:::

### 3. 创建 SQL API 配置

通过管理界面或直接插入数据：

```sql
INSERT INTO sql2api_config (
    api_id, api_name, api_path, http_method, 
    datasource_name, sql_type, sql_content, 
    params_config, result_type, enabled
) VALUES (
    1, '查询用户列表', '/api/users', 'GET',
    'master', 'SELECT', 
    'SELECT * FROM sys_user WHERE status = #{status} <if test="username != null">AND username LIKE CONCAT(\'%\', #{username}, \'%\')</if>',
    '[
        {"name":"status","type":"Integer","required":true,"description":"用户状态"},
        {"name":"username","type":"String","required":false,"description":"用户名"}
    ]',
    'list', 1
);
```

### 4. 访问 API

::: code-tabs#request

@tab cURL

```bash
# 查询用户列表
curl "http://localhost:8080/api/users?status=0&username=admin"
```

@tab HTTPie

```bash
# 使用 HTTPie 工具
http GET "http://localhost:8080/api/users" status==0 username==admin
```

@tab JavaScript

```javascript
// 前端调用示例
fetch('/api/users?status=0&username=admin')
  .then(response => response.json())
  .then(data => console.log(data));
```

:::

## DSL 语法说明

### 参数绑定语法

SQL2API 支持三种参数占位符：

1. **`#{paramName}`** - 推荐使用，安全的参数绑定（自动转义）
2. **`${paramName}`** - 直接替换（注意 SQL 注入风险）
3. **`:paramName`** - 命名参数（JDBC 风格）

示例：

```sql
SELECT * FROM users WHERE id = #{userId} AND name = #{userName}
```

### 条件标签

简化的 MyBatis 动态 SQL 语法：

```sql
SELECT * FROM users 
WHERE 1=1
<if test="username != null">
  AND username = #{username}
</if>
<if test="status != null">
  AND status = #{status}
</if>
```

支持的条件：
- `paramName != null` - 参数不为空
- `paramName == null` - 参数为空
- `paramName` - 参数存在且不为 null

### 参数配置

JSON 格式的参数定义：

```json
[
  {
    "name": "userId",
    "type": "Long",
    "required": true,
    "description": "用户ID",
    "validation": "^[0-9]+$",
    "example": 1
  },
  {
    "name": "pageNum",
    "type": "Integer",
    "required": false,
    "defaultValue": 1,
    "description": "页码"
  }
]
```

参数属性：
- **name**: 参数名称
- **type**: 数据类型（String, Integer, Long, Double, Boolean, Date）
- **required**: 是否必填
- **defaultValue**: 默认值
- **description**: 参数描述
- **validation**: 验证规则（正则表达式）
- **example**: 示例值

## API 配置详解

### SQL API 配置字段

| 字段 | 类型 | 说明 |
|------|------|------|
| api_id | BIGINT | API ID（主键） |
| api_name | VARCHAR(100) | API 名称 |
| api_path | VARCHAR(200) | API 路径（唯一） |
| http_method | VARCHAR(10) | 请求方法（GET/POST/PUT/DELETE） |
| datasource_name | VARCHAR(50) | 数据源名称 |
| sql_type | VARCHAR(20) | SQL 类型（SELECT/INSERT/UPDATE/DELETE） |
| sql_content | TEXT | SQL 语句（支持动态 SQL） |
| params_config | TEXT | 参数配置（JSON 格式） |
| result_type | VARCHAR(20) | 返回类型（single/list/page） |
| cache_enabled | TINYINT | 是否启用缓存 |
| cache_expire | INT | 缓存过期时间（秒） |
| encrypt_enabled | TINYINT | 是否启用加密 |
| encrypt_algorithm | VARCHAR(20) | 加密算法（AES/RSA/SM4） |
| auth_required | TINYINT | 是否需要认证 |
| auth_type | VARCHAR(20) | 认证类型（TOKEN/API_KEY/NONE） |
| flow_limit | INT | 流控限制（QPS） |
| enabled | TINYINT | 是否启用 |

### 返回结果类型

::: code-tabs#result

@tab single - 单条记录

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "admin",
    "email": "admin@example.com"
  }
}
```

@tab list - 列表

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"id": 1, "name": "admin"},
    {"id": 2, "name": "user"}
  ]
}
```

@tab page - 分页

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "records": [
      {"id": 1, "name": "admin"},
      {"id": 2, "name": "user"}
    ]
  }
}
```

:::

## 数据库元数据提取

### 获取数据库元数据

```java
@Autowired
private DatabaseMetadataService metadataService;

// 获取数据库信息
DatabaseMetadata dbMeta = metadataService.getDatabaseMetadata("master");

// 获取所有表
List<TableMetadata> tables = metadataService.getTables("master");

// 获取表的列信息
List<ColumnMetadata> columns = metadataService.getColumns("master", "sys_user");
```

### 支持的数据库类型

- MySQL 5.x / 8.x
- MariaDB
- PostgreSQL
- Oracle（计划支持）
- 国产数据库（DM、KingBase 等，计划支持）

## API 编排

### 编排配置

API 编排允许组合多个 API 调用：

```json
{
  "orchestrationName": "用户注册流程",
  "executionOrder": [
    {
      "step": 1,
      "apiId": 100,
      "condition": "always"
    },
    {
      "step": 2,
      "apiId": 101,
      "condition": "step1.success"
    }
  ],
  "fieldMapping": {
    "step1.userId": "step2.uid"
  },
  "validationRules": [
    {
      "field": "step1.status",
      "operator": "equals",
      "value": "success"
    }
  ]
}
```

### 字段转换

支持的转换操作符：
- **uppercase** - 转大写
- **lowercase** - 转小写
- **trim** - 去除空格
- **format** - 格式化
- **encrypt** - 加密
- **decrypt** - 解密

## 性能优化

### 缓存配置

启用 Redis 缓存：

::: code-tabs#cache

@tab application.yml

```yaml
woodlin:
  sql2api:
    cache:
      enabled: true
      expire: 300  # 默认过期时间（秒）
      key-prefix: sql2api  # 缓存键前缀
```

@tab API 配置

```json
{
  "apiName": "查询用户",
  "cacheEnabled": true,
  "cacheExpire": 600,
  "cacheKey": "user:${userId}"
}
```

:::

### 流量控制

使用 Sentinel 进行流控：

```java
// 自动集成，通过配置启用
{
  "flowLimit": 100  // 每秒最多100次请求
}
```

## 安全认证

::: code-tabs#auth

@tab Token 认证

```yaml
woodlin:
  sql2api:
    auth:
      token:
        enabled: true
        header: Authorization
        # Token 验证方式
        validator: jwt  # jwt 或 custom
```

@tab API Key 认证

```yaml
woodlin:
  sql2api:
    auth:
      api-key:
        enabled: true
        header: X-API-Key
        # API Key 存储方式
        storage: redis  # redis 或 database
```

@tab 请求示例

```bash
# Token 认证
curl -H "Authorization: Bearer your-token" \
     "http://localhost:8080/api/users"

# API Key 认证
curl -H "X-API-Key: your-api-key" \
     "http://localhost:8080/api/users"
```

:::

## 扩展开发

### 自定义数据库元数据提取器

1. 实现 `DatabaseMetadataExtractor` 接口
2. 在 `META-INF/services` 目录创建 SPI 配置文件
3. 添加到 classpath

```java
public class DM8MetadataExtractor implements DatabaseMetadataExtractor {
    
    @Override
    public String getDatabaseType() {
        return "DM8";
    }
    
    @Override
    public boolean supports(Connection connection) throws SQLException {
        return connection.getMetaData()
            .getDatabaseProductName()
            .toLowerCase()
            .contains("dm");
    }
    
    // 实现其他方法...
}
```

SPI 配置文件 `META-INF/services/com.mumu.woodlin.sql2api.spi.DatabaseMetadataExtractor`：

```text
com.example.DM8MetadataExtractor
```

## 最佳实践

### SQL 编写建议

1. **使用参数化查询**：避免 SQL 注入
2. **添加必要的索引**：提高查询性能
3. **限制返回字段**：只查询需要的字段
4. **使用分页查询**：避免大结果集

### 性能优化建议

1. **启用缓存**：对于读多写少的数据
2. **设置合理的 QPS 限制**：保护系统稳定性
3. **使用连接池**：复用数据库连接
4. **监控慢查询**：及时优化 SQL

### 安全建议

1. **启用认证**：保护敏感 API
2. **使用加密**：对敏感数据加密传输
3. **参数验证**：严格验证输入参数
4. **权限控制**：基于角色的访问控制

## 故障排查

### 常见问题

1. **数据源连接失败**
   - 检查数据源配置
   - 确认网络连接
   - 验证数据库账号密码

2. **SQL 执行失败**
   - 检查 SQL 语法
   - 验证参数绑定
   - 查看错误日志

3. **性能问题**
   - 启用缓存
   - 优化 SQL 查询
   - 添加数据库索引

## 未来规划

- [ ] 支持更多数据库类型
- [ ] AI 辅助 SQL 生成
- [ ] 可视化 API 配置界面
- [ ] 更强大的 API 编排能力
- [ ] MCP 协议集成
- [ ] 实时 API 测试工具

## 参考资料

- [MyBatis 动态 SQL](https://mybatis.org/mybatis-3/dynamic-sql.html)
- [Spring Dynamic DataSource](https://github.com/baomidou/dynamic-datasource-spring-boot-starter)
- [Sentinel 流量控制](https://sentinelguard.io/)
