# Woodlin 多租户中后台管理系统

> 注重设计与代码细节的高质量多租户中后台管理系统框架

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.9-red.svg)](https://baomidou.com/)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.39.0-blue.svg)](https://sa-token.cc/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🌟 项目介绍

Woodlin 是一个基于 Spring Boot 3.4.x 的现代化多租户中后台管理系统框架，专注于提供高质量的代码实现和完善的功能模块。系统采用模块化架构设计，支持多租户数据隔离，提供完整的用户权限管理、文件管理、任务调度等企业级功能。

### 核心特性

- 🏢 **多租户架构**: 完善的租户数据隔离和管理
- 👥 **用户权限管理**: 精细化的 RBAC 权限控制
- 🌳 **部门树形管理**: 支持无限层级的组织架构
- 📁 **文件管理**: 支持多种存储方式和文件预览
- ⏰ **任务调度**: 基于 Quartz 的定时任务管理
- 📊 **Excel 导入导出**: 便捷的数据批量操作
- 🔧 **代码生成**: 智能化的开发工具
- 📝 **操作审计**: 完整的操作日志记录
- 🎨 **统一响应**: 标准化的 API 响应格式

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 编程语言 |
| Spring Boot | 3.4.1 | 基础框架 |
| Spring Cloud | 2024.0.0 | 微服务框架 |
| MyBatis Plus | 3.5.9 | ORM 框架 |
| Sa-Token | 1.39.0 | 认证授权框架 |
| Dynamic DataSource | 4.3.1 | 动态数据源 |
| EasyExcel | 3.3.4 | Excel 处理 |
| Redisson | 3.37.0 | Redis 客户端 |
| Hutool | 5.8.34 | Java 工具库 |
| SpringDoc | 2.7.0 | API 文档 |

### 模块结构

```
woodlin
├── woodlin-dependencies     # 依赖管理模块
├── woodlin-common          # 通用模块
├── woodlin-security        # 安全认证模块
├── woodlin-system          # 系统核心模块
├── woodlin-tenant          # 多租户模块
├── woodlin-file            # 文件管理模块
├── woodlin-task            # 任务调度模块
├── woodlin-generator       # 代码生成模块
├── woodlin-admin           # 管理后台应用
└── sql                     # 数据库脚本
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/linyuliu/woodlin.git
   cd woodlin
   ```

2. **创建数据库**
   ```bash
   # 创建数据库
   mysql -u root -p
   source sql/woodlin_schema.sql
   source sql/woodlin_data.sql
   ```

3. **修改配置**
   ```yaml
   # woodlin-admin/src/main/resources/application.yml
   spring:
     datasource:
       dynamic:
         datasource:
           master:
             url: jdbc:mysql://localhost:3306/woodlin
             username: your_username
             password: your_password
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **编译运行**
   ```bash
   # 编译项目
   mvn clean package -DskipTests
   
   # 运行项目
   java -jar woodlin-admin/target/woodlin-admin-1.0.0.jar
   ```

5. **访问系统**
   - 后台地址: http://localhost:8080/api
   - API 文档: http://localhost:8080/api/doc.html
   - 默认账号: admin / 123456

### 环境变量配置

为了更好的部署体验，系统支持通过环境变量配置各项参数：

#### 🌐 服务器配置
```bash
export SERVER_PORT=8080                        # 服务端口
export SERVER_CONTEXT_PATH=/api                # 应用上下文路径
```

#### 🗄️ 数据库配置
```bash
export DATABASE_URL="jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8"
export DATABASE_USERNAME=root                   # 数据库用户名
export DATABASE_PASSWORD=123456                 # 数据库密码
export DATABASE_DRIVER=com.mysql.cj.jdbc.Driver # 数据库驱动
export DATABASE_HIKARI_MIN_IDLE=5              # 连接池最小空闲连接数
export DATABASE_HIKARI_MAX_POOL_SIZE=20        # 连接池最大连接数
```

#### 📦 Redis 配置
```bash
export REDIS_HOST=localhost                     # Redis 主机
export REDIS_PORT=6379                          # Redis 端口
export REDIS_DATABASE=0                         # Redis 数据库索引
export REDIS_PASSWORD=                          # Redis 密码(可选)
export REDIS_TIMEOUT=10s                        # Redis 超时时间
```

#### 🔐 Sa-Token 安全配置
```bash
export SA_TOKEN_NAME=Authorization              # Token 名称
export SA_TOKEN_TIMEOUT=2592000                 # Token 有效期(秒)
export SA_TOKEN_ACTIVITY_TIMEOUT=-1             # Token 活跃超时时间(秒)
export SA_TOKEN_IS_CONCURRENT=true              # 是否允许并发登录
export SA_TOKEN_IS_SHARE=false                  # 是否共用Token
export SA_TOKEN_STYLE=uuid                      # Token 风格
```

#### ⚡ Redisson 配置
```bash
export REDISSON_ADDRESS=redis://localhost:6379  # Redisson 地址
export REDISSON_DATABASE=0                      # Redisson 数据库索引
export REDISSON_PASSWORD=                       # Redisson 密码(可选)
export REDISSON_CONNECTION_POOL_SIZE=64         # 连接池大小
```

#### 📅 任务调度配置
```bash
export SNAIL_JOB_ENABLED=true                   # 是否启用任务调度
export SNAIL_JOB_SERVER_ADDRESS=localhost:8888  # 任务调度服务地址
export SNAIL_JOB_NAMESPACE=woodlin              # 命名空间
export SNAIL_JOB_GROUP_NAME=woodlin-admin       # 组名称
```

#### 🐳 Docker 部署示例
```bash
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:mysql://mysql-server:3306/woodlin" \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=yourpassword \
  -e REDIS_HOST=redis-server \
  -e REDIS_PORT=6379 \
  woodlin:latest
```

## 📚 功能介绍

### 核心功能模块

#### 1. 用户管理
- ✅ 用户增删改查
- ✅ 用户状态管理
- ✅ 密码重置
- ✅ Excel 批量导入导出
- ✅ 数据唯一性校验

#### 2. 角色权限管理
- ✅ 角色管理
- ✅ 权限管理
- ✅ 菜单管理
- ✅ 数据权限控制

#### 3. 部门管理
- ✅ 树形部门结构
- ✅ 部门增删改查
- ✅ 部门层级管理

#### 4. 多租户管理
- ✅ 租户数据隔离
- ✅ 租户配置管理
- ✅ 租户用户限制

#### 5. 系统配置
- ✅ 参数配置管理
- ✅ 系统监控
- ✅ 操作日志

#### 6. 文件管理
- 🔧 文件上传下载
- 🔧 文件预览
- 🔧 多存储支持

#### 7. 任务调度
- 🔧 定时任务管理
- 🔧 任务执行监控
- 🔧 任务日志

#### 8. 代码生成
- 🔧 表结构分析
- 🔧 代码模板生成
- 🔧 CRUD 代码生成

> ✅ 已完成  🔧 开发中  📋 计划中

## 🎯 设计理念

### 代码质量

- **统一基类**: 通过 `BaseEntity` 统一处理公共字段，减少重复代码
- **自动填充**: MyBatis Plus 自动填充创建人、更新人等字段
- **参数校验**: 完善的参数校验和异常处理机制
- **响应统一**: 统一的 API 响应格式和分页结构

### 架构设计

- **模块化**: 清晰的模块边界，便于独立开发和维护
- **分层架构**: Controller → Service → Mapper 经典三层架构
- **依赖管理**: 统一的版本管理和依赖声明
- **配置化**: 灵活的配置管理机制

### 开发体验

- **代码生成**: 智能化的 CRUD 代码生成工具
- **文档完善**: 详细的 API 文档和开发指南
- **规范统一**: 统一的代码规范和注释标准
- **工具齐全**: 完整的开发和调试工具集

## 📖 开发指南

### 实体类开发

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("your_table")
@Schema(description = "实体描述")
public class YourEntity extends BaseEntity {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    
    @TableField("name")
    @Schema(description = "名称")
    private String name;
}
```

### 服务类开发

```java
@Service
public class YourServiceImpl extends ServiceImpl<YourMapper, YourEntity> implements IYourService {
    
    @Override
    public IPage<YourEntity> selectPage(YourEntity entity, Integer pageNum, Integer pageSize) {
        Page<YourEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<YourEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        return this.page(page, queryWrapper);
    }
}
```

### 控制器开发

```java
@RestController
@RequestMapping("/your/path")
@RequiredArgsConstructor
@Tag(name = "模块名称", description = "模块描述")
public class YourController {
    
    private final IYourService yourService;
    
    @GetMapping("/list")
    @Operation(summary = "分页查询")
    public Result<PageResult<YourEntity>> list(YourEntity entity, Integer pageNum, Integer pageSize) {
        IPage<YourEntity> page = yourService.selectPage(entity, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }
}
```

## 🛠️ 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
```

### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 10s
```

### Sa-Token 配置

```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000
  activity-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
```

## 🤝 参与贡献

我们欢迎所有形式的贡献，包括但不限于：

- 🐛 Bug 报告
- 💡 功能建议
- 📝 文档改进
- 🔧 代码优化

### 贡献流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 开源协议

本项目基于 [MIT 协议](LICENSE) 开源，你可以自由使用、修改和分发本项目。

## 🙋‍♂️ 联系我们

- **作者**: mumu
- **邮箱**: mumu@woodlin.com
- **GitHub**: [https://github.com/linyuliu/woodlin](https://github.com/linyuliu/woodlin)

---

⭐ 如果这个项目对你有帮助，请给我们一个星标支持！
