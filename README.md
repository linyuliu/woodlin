# Woodlin 多租户中后台管理系统

> 注重设计与代码细节的高质量多租户中后台管理系统框架

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
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
- 🚀 **SQL2API**: 通过配置 SQL 直接生成 RESTful API（新增）
- 🔐 **API 加密**: 支持 AES、RSA、SM4 多种加密算法
- 🔑 **密码策略**: 灵活的密码安全策略配置
- 👁️ **活动监控**: 用户活动监控和会话管理
- ⚙️ **系统设置**: 统一的前端配置管理界面

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | 编程语言 |
| Spring Boot | 3.4.1 | 基础框架 |
| MyBatis Plus | 3.5.9 | ORM 框架 |
| Sa-Token | 1.39.0 | 认证授权框架 |
| Dynamic DataSource | 4.3.1 | 动态数据源 |
| EasyExcel | 3.3.4 | Excel 处理 |
| Redisson | 3.37.0 | Redis 客户端 |
| Hutool | 5.8.34 | Java 工具库 |
| SpringDoc | 2.7.0 | API 文档 |

### 模块结构

```text
woodlin
├── woodlin-dependencies     # 依赖管理模块（BOM 统一版本管理）
├── woodlin-common          # 通用模块（工具类、常量、配置）
├── woodlin-security        # 安全认证模块（Sa-Token 集成）
├── woodlin-system          # 系统核心模块（用户、角色、权限）
├── woodlin-tenant          # 多租户模块（租户隔离）
├── woodlin-file            # 文件管理模块（文件上传、下载）
├── woodlin-task            # 任务调度模块（Quartz 定时任务）
├── woodlin-generator       # 代码生成模块（智能代码生成）
├── woodlin-sql2api         # SQL2API 动态接口模块（SQL 转 API）
├── woodlin-admin           # 管理后台应用（主应用入口）
└── sql                     # 数据库脚本（DDL 和初始化数据）
```

## 🚀 快速开始

### 环境要求

- **JDK**: 25 或更高版本
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
   - 默认账号: admin / Passw0rd

### 环境变量配置

系统支持通过环境变量配置各项参数，便于容器化部署：

::: code-tabs#env

@tab 服务器配置

```bash
# ========== 服务器配置 ==========
export SERVER_PORT=8080                        # 服务端口
export SERVER_CONTEXT_PATH=/api                # 应用上下文路径
export SPRING_PROFILES_ACTIVE=prod             # 运行环境：dev/test/prod
```

@tab 数据库配置

```bash
# ========== 数据库配置 ==========
# 主数据源
export DATABASE_URL="jdbc:mysql://localhost:3306/woodlin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
export DATABASE_USERNAME=root                   # 数据库用户名
export DATABASE_PASSWORD=Passw0rd               # 数据库密码
export DATABASE_DRIVER=com.mysql.cj.jdbc.Driver # 数据库驱动

# Druid 连接池配置
export DATABASE_DRUID_INITIAL_SIZE=5            # 初始连接数
export DATABASE_DRUID_MIN_IDLE=5                # 最小空闲连接数
export DATABASE_DRUID_MAX_ACTIVE=20             # 最大活动连接数

# Druid 监控配置
export DATABASE_DRUID_USERNAME=admin            # Druid 监控用户名
export DATABASE_DRUID_PASSWORD=Passw0rd         # Druid 监控密码
```

@tab Redis 配置

```bash
# ========== Redis 配置 ==========
export REDIS_HOST=localhost                     # Redis 主机地址
export REDIS_PORT=6379                          # Redis 端口
export REDIS_DATABASE=0                         # Redis 数据库索引（0-15）
export REDIS_PASSWORD=                          # Redis 密码（可选）
export REDIS_TIMEOUT=10s                        # 连接超时时间
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

详细的 Docker 部署文档请参考: [Docker 部署文档](docs/DOCKER.md)

```bash
# 快速启动
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f woodlin-app
```

**特性**:
- ✅ 基于 Eclipse Temurin JDK 25 (LTS OpenJDK 发行版)
- ✅ 支持中文字体，防止乱码 (文泉驿正黑、微米黑)
- ✅ 内置诊断工具 (curl, wget, netcat, telnet, ping, jq 等)
- ✅ 完整的系统优化 (ulimit, 资源限制, 健康检查)
- ✅ 详细的参数注释和使用说明

**使用环境检查脚本**:
```bash
# 运行环境检查
./scripts/check-docker-env.sh
```

### 🚀 一键部署

项目提供了完整的部署脚本和Docker配置：

#### 快速部署
```bash
# 克隆项目
git clone https://github.com/linyuliu/woodlin.git
cd woodlin

# 一键部署（包含前后端）
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 或使用Docker Compose
cp .env.example .env  # 修改环境变量
docker-compose up -d
```

#### 开发环境启动
```bash
# 后端开发
mvn spring-boot:run -pl woodlin-admin

# 前端开发
cd woodlin-web
npm install
npm run dev
```

### 📦 项目结构

#### 前端技术栈
- **Vue 3** + **TypeScript** + **Vite**
- **Naive UI** - 现代化的Vue3组件库
- **Pinia** - Vue状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端
- **ESLint** + **Prettier** - 代码规范

#### 后端改进
- **Druid连接池** - 替换HikariCP，提供更好的监控和性能
- **分页溢出防护** - 防止大页码查询导致的性能问题
- **SonarQube兼容** - 符合代码质量扫描规范

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

## 📚 文档

- [API 加密配置文档](docs/API_ENCRYPTION.md)
- [系统配置管理文档](docs/SYSTEM_CONFIG.md)
- [实现总结](docs/IMPLEMENTATION_SUMMARY.md)
- [贡献指南](CONTRIBUTING.md)

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
          password: Passw0rd
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
  active-timeout: -1
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

## 🚀 SQL2API 功能

SQL2API 是 Woodlin 系统的动态 API 生成模块，允许通过配置 SQL 语句快速生成 RESTful API 接口。

### 核心特性

- ✅ **零代码开发**: 通过配置 SQL 直接生成 API，无需编写代码
- ✅ **多数据库支持**: 支持 MySQL、PostgreSQL、Oracle 等 20+ 种数据库
- ✅ **简化 DSL 语法**: 提供比 MyBatis 更简单的参数绑定语法
- ✅ **元数据自动提取**: 自动提取数据库表结构、字段注释等信息
- ✅ **API 编排**: 支持多个 API 之间的编排和数据流转
- ✅ **安全认证**: 支持 Token、API Key 等多种认证方式
- ✅ **性能优化**: 内置 Redis 缓存和 Sentinel 流控
- ✅ **加密支持**: 可配置 AES、RSA、SM4 等加密算法
- ✅ **SPI 扩展**: 支持通过 SPI 机制扩展数据库支持

### 快速示例

1. **创建 SQL API 配置**

```sql
INSERT INTO sql2api_config (
    api_name, api_path, http_method, 
    datasource_name, sql_type, sql_content, 
    params_config, result_type, enabled
) VALUES (
    '查询用户列表', '/api/users', 'GET',
    'master', 'SELECT', 
    'SELECT * FROM sys_user WHERE status = #{status}',
    '[{"name":"status","type":"Integer","required":true}]',
    'list', 1
);
```

2. **访问生成的 API**

```bash
curl "http://localhost:8080/api/users?status=0"
```

3. **支持动态 SQL**

```sql
SELECT * FROM users 
WHERE 1=1
<if test="username != null">
  AND username LIKE CONCAT('%', #{username}, '%')
</if>
<if test="status != null">
  AND status = #{status}
</if>
```

### 详细文档

完整的 SQL2API 使用指南请查看：[SQL2API 功能文档](docs/SQL2API_GUIDE.md)

## 📘 完整文档

Woodlin 提供了基于 VitePress 构建的完整文档系统，详细说明每个模块的功能、开发和部署等所有细节。

### 在线文档

访问完整文档：[Woodlin 文档站点](https://woodlin.example.com)

### 本地查看文档

```bash
# 进入文档目录
cd documentation

# 安装依赖
npm install

# 启动文档开发服务器
npm run docs:dev

# 访问 http://localhost:5173
```

### 构建文档

```bash
cd documentation
npm run docs:build
```

### 文档特性

- ✅ **VitePress 驱动**：基于 Vue 3 的现代化文档系统
- ✅ **中文字体优化**：使用 LXGW WenKai（霞鹜文楷）字体，阅读体验优异
- ✅ **代码高亮**：支持 Java、TypeScript、Vue、LaTeX 等多种语言
- ✅ **完整详细**：每个模块都有详尽的文档说明，不省略任何细节
- ✅ **全文搜索**：内置本地搜索功能
- ✅ **响应式设计**：完美支持移动端和桌面端

### 文档内容

- **快速开始**：5 分钟快速上手指南
- **模块文档**：11 个模块的完整说明（dependencies、common、security、system、tenant、file、task、generator、sql2api、admin、web）
- **开发指南**：代码规范、环境搭建、调试技巧、测试指南
- **部署指南**：本地部署、Docker 部署、K8s 部署、生产环境配置
- **API 文档**：所有 API 接口的详细说明

## 📄 开源协议

本项目基于 [MIT 协议](LICENSE) 开源，你可以自由使用、修改和分发本项目。

## 🙋‍♂️ 联系我们

- **作者**: mumu
- **邮箱**: mumu@woodlin.com
- **GitHub**: [https://github.com/linyuliu/woodlin](https://github.com/linyuliu/woodlin)

---

⭐ 如果这个项目对你有帮助，请给我们一个星标支持！
