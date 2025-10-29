# 代码质量检查指南 (Code Quality Checks Guide)

本文档描述了 Woodlin 项目中配置的代码质量检查工具和 GitHub Actions 工作流。

## 目录 (Table of Contents)

1. [代码质量工具 (Code Quality Tools)](#代码质量工具)
2. [GitHub Actions 工作流 (GitHub Actions Workflows)](#github-actions-工作流)
3. [本地运行检查 (Running Checks Locally)](#本地运行检查)
4. [编辑器配置 (Editor Configuration)](#编辑器配置)

## 代码质量工具

### 1. Checkstyle - Java 代码规范检查

Checkstyle 用于检查 Java 代码是否符合编码规范，特别是 JavaDoc 注释的完整性和正确性。

**配置文件**: `checkstyle.xml`

**检查项目**:
- JavaDoc 完整性（方法、类、字段的注释）
- JavaDoc 格式（首句以句号结尾、HTML 标签正确性）
- 命名规范（类名、方法名、变量名等）
- 代码风格（空格、换行、大括号位置等）
- 导入语句规范

**本地运行**:
```bash
# 检查整个项目
mvn checkstyle:checkstyle

# 检查特定模块
mvn checkstyle:checkstyle -pl woodlin-common

# 生成 HTML 报告
mvn checkstyle:checkstyle site:site
# 报告位置: target/site/checkstyle.html
```

### 2. SpotBugs - 静态代码分析

SpotBugs 用于检测 Java 代码中的潜在缺陷和错误模式。

**配置**: 在 `pom.xml` 中已配置

**本地运行**:
```bash
# 运行 SpotBugs 分析
mvn spotbugs:check

# 生成报告
mvn spotbugs:spotbugs
# 报告位置: target/spotbugsXml.xml
```

### 3. JaCoCo - 代码覆盖率

JaCoCo 用于测量单元测试的代码覆盖率。

**本地运行**:
```bash
# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 查看报告
# 报告位置: target/site/jacoco/index.html
```

### 4. Maven Javadoc - JavaDoc 验证

Maven Javadoc 插件用于验证 JavaDoc 注释的正确性并生成 API 文档。

**本地运行**:
```bash
# 验证 JavaDoc
mvn javadoc:javadoc

# 生成 JavaDoc JAR
mvn javadoc:jar
# 输出: target/*-javadoc.jar
```

### 5. ESLint - 前端代码检查 (Frontend)

ESLint 用于检查 TypeScript/Vue 代码的质量和一致性。

**本地运行**:
```bash
cd woodlin-web

# 运行 ESLint
npm run lint

# 自动修复问题
npm run lint -- --fix

# 检查代码格式
npx prettier --check src/
```

## GitHub Actions 工作流

本项目配置了三个主要的 GitHub Actions 工作流：

### 1. CI Build and Test (`ci.yml`)

**触发条件**:
- 推送到 `main` 或 `develop` 分支
- 对 `main` 或 `develop` 分支的 Pull Request
- 手动触发 (workflow_dispatch)

**功能**:
- 构建后端 (Java 21 + Maven)
- 运行单元测试
- 构建前端 (Node.js + npm)
- 运行前端类型检查和 ESLint
- 上传构建产物

**作业 (Jobs)**:
1. `build-backend` - 后端构建和测试
2. `build-frontend` - 前端构建和检查
3. `build-summary` - 构建结果汇总

### 2. Code Quality Checks (`code-quality.yml`)

**触发条件**:
- 推送到 `main` 或 `develop` 分支
- 对 `main` 或 `develop` 分支的 Pull Request
- 手动触发

**功能**:
- 运行 Checkstyle 检查
- 运行 SpotBugs 静态分析
- 验证 JavaDoc 完整性
- 生成代码覆盖率报告
- 前端代码质量检查

**作业 (Jobs)**:
1. `checkstyle` - Checkstyle 分析
2. `spotbugs` - SpotBugs 分析
3. `javadoc` - JavaDoc 验证
4. `code-coverage` - 代码覆盖率
5. `frontend-quality` - 前端质量检查
6. `quality-summary` - 质量检查汇总

### 3. Pull Request Checks (`pr-checks.yml`)

**触发条件**:
- Pull Request 被打开、同步或重新打开

**功能**:
- 快速构建检查
- 运行 Checkstyle 检查变更的文件
- 运行测试
- 在 PR 中添加结果评论

**作业 (Jobs)**:
1. `pr-validation` - PR 验证（后端）
2. `frontend-pr-check` - PR 验证（前端）

## 本地运行检查

### 完整的代码质量检查流程

```bash
# 1. 清理并编译项目
mvn clean compile -DskipTests

# 2. 运行所有代码质量检查
mvn checkstyle:checkstyle spotbugs:check javadoc:javadoc

# 3. 运行测试并生成覆盖率报告
mvn test jacoco:report

# 4. 检查前端代码
cd woodlin-web
npm run lint
npm run type-check
npm run build
cd ..
```

### 快速检查脚本

创建一个脚本 `scripts/quality-check.sh`:

```bash
#!/bin/bash
set -e

echo "=== 运行代码质量检查 ==="

echo "1. 编译项目..."
mvn clean compile -DskipTests --batch-mode

echo "2. 运行 Checkstyle..."
mvn checkstyle:checkstyle --batch-mode

echo "3. 运行 SpotBugs..."
mvn spotbugs:check --batch-mode

echo "4. 验证 JavaDoc..."
mvn javadoc:javadoc --batch-mode

echo "5. 检查前端代码..."
cd woodlin-web
npm run lint
npm run type-check
cd ..

echo "=== 所有检查完成 ==="
```

使用方法:
```bash
chmod +x scripts/quality-check.sh
./scripts/quality-check.sh
```

## 编辑器配置

### EditorConfig

项目根目录包含 `.editorconfig` 文件，定义了跨编辑器的代码格式规范：

- **字符编码**: UTF-8
- **换行符**: LF (Unix 风格)
- **文件末尾**: 插入新行
- **Java 文件**: 4 个空格缩进，最大行长 120
- **XML/YAML/JSON**: 2-4 个空格缩进
- **自动移除行尾空格**

### 推荐的 IDE 插件

#### IntelliJ IDEA / WebStorm
1. **EditorConfig** - 自动应用代码格式规范
2. **Checkstyle-IDEA** - 实时 Checkstyle 检查
   - 配置: Settings → Tools → Checkstyle → 添加 `checkstyle.xml`
3. **SonarLint** - 实时代码质量反馈
4. **SpotBugs** - 静态分析插件

#### VS Code
1. **EditorConfig for VS Code** - EditorConfig 支持
2. **Checkstyle for Java** - Checkstyle 检查
3. **ESLint** - JavaScript/TypeScript 检查
4. **Prettier** - 代码格式化
5. **Volar** - Vue 3 支持

### IDE 配置建议

#### IntelliJ IDEA

1. **导入 Checkstyle 配置**:
   - File → Settings → Tools → Checkstyle
   - 添加配置文件: `checkstyle.xml`
   - 设置为活动配置

2. **启用保存时格式化**:
   - File → Settings → Tools → Actions on Save
   - 勾选 "Reformat code"
   - 勾选 "Optimize imports"

3. **配置 JavaDoc 模板**:
   - File → Settings → Editor → File and Code Templates
   - 选择 "Includes" → "File Header"
   - 添加标准的 JavaDoc 头部模板

## JavaDoc 标准

### 类级别注释

```java
/**
 * 类的简要描述（首句以句号结尾）.
 * 
 * <p>详细描述（可选）</p>
 * 
 * @author mumu
 * @since 2025-01-01
 */
public class ExampleClass {
}
```

### 方法级别注释

```java
/**
 * 方法的简要描述（首句以句号结尾）.
 * 
 * @param <T> 泛型参数说明
 * @param param1 参数1说明
 * @param param2 参数2说明
 * @return 返回值说明
 * @throws ExceptionType 异常说明（如有）
 */
public <T> T exampleMethod(String param1, int param2) throws ExceptionType {
    // 实现
}
```

### 字段级别注释

```java
/**
 * 字段说明（public 字段需要注释）.
 */
public static final String CONSTANT = "value";
```

### JavaDoc 最佳实践

1. **首句必须以句号结尾** - Checkstyle 会检查
2. **使用完整的句子** - 避免片段式描述
3. **所有 public 方法必须有注释** - 除了简单的 getter/setter
4. **参数和返回值必须有说明** - 使用 `@param` 和 `@return`
5. **泛型参数需要说明** - 使用 `@param <T>`
6. **HTML 标签要正确** - Checkstyle 会验证
7. **避免使用非标准标签** - 如 `@description`（使用标准描述即可）

## 常见问题

### Q: Checkstyle 报告 "First sentence should end with a period"

**A**: 确保 JavaDoc 的第一句话以句号（`.`）结尾。

错误示例:
```java
/**
 * 获取用户信息
 */
```

正确示例:
```java
/**
 * 获取用户信息.
 */
```

### Q: Checkstyle 报告 "Expected @param tag for '<T>'"

**A**: 泛型方法需要在 JavaDoc 中添加 `@param <T>` 说明。

```java
/**
 * 通用查询方法.
 * 
 * @param <T> 返回类型
 * @param id 主键ID
 * @return 查询结果
 */
public <T> T findById(Long id) {
}
```

### Q: 如何在 CI 中查看详细的检查报告？

**A**: 
1. 进入 GitHub Actions 运行页面
2. 选择相应的工作流运行
3. 在 "Artifacts" 部分下载报告
4. 解压并在浏览器中打开 HTML 报告

### Q: 本地如何快速修复 ESLint 问题？

**A**:
```bash
cd woodlin-web
npm run lint -- --fix
```

## 持续改进

### 建议的改进路线图

1. **短期**:
   - [ ] 修复现有的 Checkstyle 警告
   - [ ] 提高测试覆盖率至 60%+
   - [ ] 添加前端单元测试

2. **中期**:
   - [ ] 集成 SonarQube 进行深度代码分析
   - [ ] 添加性能测试
   - [ ] 配置依赖安全扫描

3. **长期**:
   - [ ] 实现自动化代码审查
   - [ ] 建立代码质量度量看板
   - [ ] 集成契约测试

## 联系与反馈

如有问题或建议，请：
1. 提交 GitHub Issue
2. 联系开发团队
3. 参考 `CONTRIBUTING.md` 文档

---

**最后更新**: 2025-10-29
**维护者**: mumu (yulin.1996@foxmail.com)
