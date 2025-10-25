# Woodlin 项目贡献指南

感谢您对 Woodlin 项目的关注！我们欢迎任何形式的贡献。

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [文档编写规范](#文档编写规范)
- [测试规范](#测试规范)

## 🤝 行为准则

本项目遵循贡献者公约（Contributor Covenant）行为准则。参与本项目即表示您同意遵守其条款。

## 🎯 如何贡献

### 报告问题

如果您发现了 bug 或有功能建议：

1. 在提交 issue 前，请先搜索现有 issue，避免重复
2. 使用清晰描述性的标题
3. 提供详细的问题描述，包括：
   - 复现步骤
   - 预期行为
   - 实际行为
   - 环境信息（操作系统、Java 版本、浏览器等）
   - 相关日志或截图

### 提交代码

1. **Fork 仓库**
   ```bash
   # Fork 后克隆您的仓库
   git clone https://github.com/YOUR_USERNAME/woodlin.git
   cd woodlin
   ```

2. **创建分支**
   ```bash
   # 为您的功能创建新分支
   git checkout -b feature/your-feature-name
   # 或修复 bug
   git checkout -b fix/your-bug-fix
   ```

3. **开发和测试**
   ```bash
   # 编写代码并确保测试通过
   mvn clean test
   
   # 检查代码格式
   mvn clean compile
   ```

4. **提交更改**
   ```bash
   git add .
   git commit -m "feat: 添加某某功能"
   git push origin feature/your-feature-name
   ```

5. **创建 Pull Request**
   - 在 GitHub 上创建 PR
   - 填写 PR 模板，描述您的更改
   - 链接相关的 issue
   - 等待代码审查

## 🛠️ 开发环境设置

### 环境要求

- **JDK**: 25 或更高版本
- **Maven**: 3.8 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本
- **Node.js**: 20.19+ 或 22.12+（前端开发）
- **IDE**: IntelliJ IDEA 推荐（已配置 .editorconfig）

### 快速启动

```bash
# 1. 安装依赖
mvn clean install -DskipTests

# 2. 初始化数据库
mysql -u root -p < sql/woodlin_schema.sql
mysql -u root -p < sql/woodlin_data.sql

# 3. 配置环境变量（复制 .env.example 为 .env）
cp .env.example .env
# 编辑 .env 文件，配置数据库和 Redis

# 4. 启动后端
mvn spring-boot:run -pl woodlin-admin

# 5. 启动前端（可选）
cd woodlin-web
npm install
npm run dev
```

## 📝 代码规范

### Java 代码规范

1. **命名规范**
   - 类名：大驼峰命名法（PascalCase）
   - 方法名：小驼峰命名法（camelCase）
   - 常量：全大写，下划线分隔（UPPER_SNAKE_CASE）
   - 变量：小驼峰命名法（camelCase）

2. **注释规范**
   ```java
   /**
    * 类功能简述
    * 
    * @author 作者名
    * @description 详细描述
    * @since 版本号
    */
   public class Example {
       
       /**
        * 方法功能简述
        * 
        * @param param1 参数1说明
        * @param param2 参数2说明
        * @return 返回值说明
        * @throws Exception 异常说明
        */
       public String method(String param1, String param2) throws Exception {
           // 实现
       }
   }
   ```

3. **代码格式**
   - 使用 4 个空格缩进（不使用 Tab）
   - 每行最多 120 个字符
   - 左大括号不换行
   - if/for/while 等语句必须使用大括号，即使只有一行代码

4. **Swagger 文档注解**
   ```java
   @Tag(name = "模块名称", description = "模块描述")
   @RestController
   @RequestMapping("/api/example")
   public class ExampleController {
       
       @Operation(
           summary = "接口简述",
           description = "接口详细说明"
       )
       @Parameter(name = "id", description = "参数说明", required = true)
       @GetMapping("/{id}")
       public R<Example> getById(@PathVariable Long id) {
           // 实现
       }
   }
   ```

### 前端代码规范

1. **使用 TypeScript**，避免使用 `any` 类型
2. **使用 ESLint** 进行代码检查
3. **组件命名**：大驼峰命名法
4. **样式**：使用 SCSS，遵循 BEM 命名规范

## 📦 提交规范

我们使用语义化提交信息（Semantic Commit Messages）：

### 提交格式

```text
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

- **feat**: 新功能
- **fix**: 修复 bug
- **docs**: 仅文档更改
- **style**: 不影响代码含义的更改（空格、格式化、缺少分号等）
- **refactor**: 既不修复 bug 也不添加功能的代码更改
- **perf**: 提高性能的代码更改
- **test**: 添加或修正测试
- **build**: 影响构建系统或外部依赖的更改（Maven、Gradle、npm）
- **ci**: CI 配置文件和脚本的更改
- **chore**: 其他不修改 src 或测试文件的更改
- **revert**: 回滚先前的提交

### Scope 范围

- **common**: 公共模块
- **security**: 安全模块
- **system**: 系统模块
- **tenant**: 租户模块
- **file**: 文件模块
- **task**: 任务模块
- **generator**: 代码生成模块
- **admin**: 管理后台
- **web**: 前端
- **docs**: 文档

### 示例

```bash
# 新功能
git commit -m "feat(system): 添加用户导出功能"

# 修复 bug
git commit -m "fix(security): 修复密码策略验证问题"

# 文档更新
git commit -m "docs(readme): 更新安装说明"

# 重构
git commit -m "refactor(common): 优化 Redis 工具类"

# 性能优化
git commit -m "perf(system): 优化用户列表查询性能"
```

## 📚 文档编写规范

### API 文档

1. **使用 Swagger/OpenAPI 注解**
   - 每个 Controller 必须有 `@Tag` 注解
   - 每个接口必须有 `@Operation` 注解
   - 参数必须有 `@Parameter` 注解
   - DTO 类必须有 `@Schema` 注解

2. **示例**：
   ```java
   @Schema(description = "用户信息")
   public class UserDTO {
       @Schema(description = "用户ID", example = "1")
       private Long id;
       
       @Schema(description = "用户名", example = "admin")
       private String username;
   }
   ```

### README 文档

- 保持 README.md 简洁明了
- 包含快速开始指南
- 包含常见问题解答
- 包含技术栈说明

### 代码注释

- 所有 public 方法必须有 JavaDoc 注释
- 复杂逻辑必须添加注释说明
- 注释必须与代码保持同步

## 🧪 测试规范

### 单元测试

1. **测试覆盖率**：新代码的测试覆盖率应达到 80% 以上
2. **命名规范**：测试方法名格式 `test方法名_场景_预期结果`
3. **测试结构**：使用 Given-When-Then 模式

```java
@Test
void testCreateUser_WhenUsernameExists_ShouldThrowException() {
    // Given - 准备测试数据
    UserDTO user = new UserDTO();
    user.setUsername("existingUser");
    
    // When - 执行测试
    Exception exception = assertThrows(
        BusinessException.class,
        () -> userService.createUser(user)
    );
    
    // Then - 验证结果
    assertEquals("用户名已存在", exception.getMessage());
}
```

### 集成测试

- 使用 `@SpringBootTest` 注解
- 测试完整的业务流程
- 使用测试数据库（避免污染生产数据）

## 🔍 代码审查

代码审查是确保代码质量的重要环节：

### 审查要点

1. **功能性**：代码是否实现了预期功能
2. **可读性**：代码是否清晰易懂
3. **可维护性**：代码是否易于维护和扩展
4. **性能**：是否存在性能问题
5. **安全性**：是否存在安全隐患
6. **测试**：是否有足够的测试覆盖

### 审查流程

1. PR 提交后自动触发 CI 构建
2. 至少一位核心维护者审查代码
3. 所有评论必须得到解决
4. CI 必须通过
5. 合并到主分支

## 📞 联系方式

如有任何问题，可以通过以下方式联系我们：

- **Issue**: [GitHub Issues](https://github.com/linyuliu/woodlin/issues)
- **Email**: mumu@woodlin.com
- **讨论**: [GitHub Discussions](https://github.com/linyuliu/woodlin/discussions)

## 📄 许可证

贡献的代码将遵循项目的 [MIT License](LICENSE)。

---

再次感谢您的贡献！🎉
