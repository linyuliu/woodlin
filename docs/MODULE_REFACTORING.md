# Woodlin 模块重构说明

## 重构背景

原有的目录结构过于分散，多个功能模块在根目录下平铺，不利于项目的管理和部署。本次重构将所有系统功能模块统一整合到 `woodlin-system` 下，形成清晰的层级结构。

## 原有结构的问题

```text
woodlin/
├── woodlin-dependencies/
├── woodlin-common/
├── woodlin-security/          ❌ 分散在根目录
├── woodlin-system/            ❌ 与其他模块同级
├── woodlin-tenant/            ❌ 分散在根目录
├── woodlin-file/              ❌ 分散在根目录
├── woodlin-task/              ❌ 分散在根目录
├── woodlin-generator/         ❌ 分散在根目录
├── woodlin-sql2api/           ❌ 分散在根目录
├── woodlin-admin/
└── ...
```

**存在的问题**:
1. 模块过于分散，不便于统一管理
2. 根 POM 模块列表冗长（11个模块）
3. 单体部署时需要逐个引入模块
4. 微服务拆分时边界不清晰
5. 新成员理解项目结构困难

## 重构后的结构

```text
woodlin/
├── woodlin-dependencies/           # BOM 依赖管理
├── woodlin-common/                # 通用模块
├── woodlin-system/                ✅ 系统模块聚合器
│   ├── pom.xml                       # 父POM，管理所有子模块
│   ├── woodlin-system-security/      # 安全认证模块
│   ├── woodlin-system-core/          # 系统核心模块（原 woodlin-system）
│   ├── woodlin-system-tenant/        # 多租户模块
│   ├── woodlin-system-file/          # 文件管理模块
│   ├── woodlin-system-task/          # 任务调度模块
│   ├── woodlin-system-generator/     # 代码生成模块
│   └── woodlin-system-sql2api/       # SQL2API 模块
├── woodlin-dsl/                   # DSL 模块
├── woodlin-admin/                 # 主应用
└── woodlin-web/                   # 前端应用
```

**改进效果**:
1. ✅ 模块结构清晰，一目了然
2. ✅ 根 POM 简化到 5 个模块
3. ✅ 单体部署：直接依赖 `woodlin-system` 即可
4. ✅ 微服务拆分：每个子模块可独立部署
5. ✅ 新成员快速理解项目结构

## 重构细节

### 1. 模块移动

| 原模块名 | 新模块名 | 说明 |
|---------|---------|------|
| `woodlin-security` | `woodlin-system-security` | 移入 woodlin-system |
| `woodlin-system` | `woodlin-system-core` | 重命名并移入 |
| `woodlin-tenant` | `woodlin-system-tenant` | 移入 woodlin-system |
| `woodlin-file` | `woodlin-system-file` | 移入 woodlin-system |
| `woodlin-task` | `woodlin-system-task` | 移入 woodlin-system |
| `woodlin-generator` | `woodlin-system-generator` | 移入 woodlin-system |
| `woodlin-sql2api` | `woodlin-system-sql2api` | 移入 woodlin-system |

### 2. POM 文件更新

#### 2.1 创建 woodlin-system 父 POM

```xml
<project>
    <parent>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <artifactId>woodlin-system</artifactId>
    <packaging>pom</packaging>
    
    <modules>
        <module>woodlin-system-security</module>
        <module>woodlin-system-core</module>
        <module>woodlin-system-tenant</module>
        <module>woodlin-system-file</module>
        <module>woodlin-system-task</module>
        <module>woodlin-system-generator</module>
        <module>woodlin-system-sql2api</module>
    </modules>
</project>
```

#### 2.2 更新子模块 POM

每个子模块的 `parent` 由原来的根项目改为 `woodlin-system`:

```xml
<!-- 原来 -->
<parent>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin</artifactId>
    <version>1.0.0</version>
</parent>
<artifactId>woodlin-security</artifactId>

<!-- 改为 -->
<parent>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system</artifactId>
    <version>1.0.0</version>
</parent>
<artifactId>woodlin-system-security</artifactId>
```

#### 2.3 更新根 POM

```xml
<!-- 原来：11个模块 -->
<modules>
    <module>woodlin-dependencies</module>
    <module>woodlin-common</module>
    <module>woodlin-security</module>
    <module>woodlin-system</module>
    <module>woodlin-tenant</module>
    <module>woodlin-file</module>
    <module>woodlin-task</module>
    <module>woodlin-generator</module>
    <module>woodlin-sql2api</module>
    <module>woodlin-dsl</module>
    <module>woodlin-admin</module>
</modules>

<!-- 改为：5个模块 -->
<modules>
    <module>woodlin-dependencies</module>
    <module>woodlin-common</module>
    <module>woodlin-system</module>
    <module>woodlin-dsl</module>
    <module>woodlin-admin</module>
</modules>
```

#### 2.4 更新 woodlin-admin 依赖

```xml
<!-- 原来：逐个引入 -->
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-security</artifactId>
</dependency>
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system</artifactId>
</dependency>
<!-- ... 其他模块 ... -->

<!-- 改为：使用新的模块名 -->
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system-security</artifactId>
</dependency>
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-system-core</artifactId>
</dependency>
<!-- ... 其他 woodlin-system-* 模块 ... -->
```

### 3. 依赖关系说明

```
woodlin-system-security (基础模块)
    └── 依赖: woodlin-common

woodlin-system-core (核心模块)
    └── 依赖: woodlin-common, woodlin-system-security

woodlin-system-sql2api
    └── 依赖: woodlin-common, woodlin-system-security

woodlin-system-tenant
woodlin-system-file
woodlin-system-task
woodlin-system-generator
    └── 依赖: woodlin-common
```

### 4. 文档更新

- ✅ `README.md`: 更新模块结构图和说明
- ✅ `.github/copilot-instructions.md`: 更新开发指南
- ✅ `docker/app/Dockerfile`: 更新 Docker 构建脚本
- ✅ `docker/app/Dockerfile.distroless`: 更新精简镜像构建脚本

## 使用方式

### 单体应用部署

如果要构建完整的单体应用，只需依赖所有 `woodlin-system-*` 模块即可：

```xml
<dependencies>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-tenant</artifactId>
    </dependency>
    <!-- 其他 system 模块... -->
</dependencies>
```

### 微服务拆分

如果要拆分为微服务，可以为每个子模块创建独立的应用：

#### 用户服务（User Service）
```xml
<dependencies>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-core</artifactId>
    </dependency>
</dependencies>
```

#### 文件服务（File Service）
```xml
<dependencies>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mumu</groupId>
        <artifactId>woodlin-system-file</artifactId>
    </dependency>
</dependencies>
```

### 混合部署

也可以选择性地组合模块，实现灵活的部署策略：

- **核心服务**: security + core + tenant
- **业务服务**: file + task + generator + sql2api
- **工具服务**: generator + sql2api

## 构建和测试

### 构建整个项目
```bash
mvn clean install -DskipTests
```

### 构建 woodlin-system 模块
```bash
cd woodlin-system
mvn clean install -DskipTests
```

### 运行测试
```bash
mvn test
```

### Docker 构建
```bash
# 标准镜像
docker build -t woodlin-app:latest -f docker/app/Dockerfile .

# Distroless 镜像
docker build -t woodlin-app:distroless -f docker/app/Dockerfile.distroless .
```

## 迁移指南

对于已有的开发分支或功能代码，迁移步骤如下：

1. **更新依赖引用**: 将所有 `woodlin-security`、`woodlin-system` 等引用改为 `woodlin-system-*`
2. **更新 import 语句**: Java 包名没有变化，无需修改
3. **更新配置文件**: 如果配置中引用了模块名，需要更新
4. **重新构建**: `mvn clean install`
5. **运行测试**: 确保功能正常

## 兼容性说明

✅ **向后兼容**: 
- Java 包名保持不变（如 `com.mumu.woodlin.security.*`）
- 配置文件和代码无需修改
- 只需更新 Maven 依赖的 artifactId

⚠️ **不兼容**: 
- Maven 模块的 artifactId 已更改
- Docker 构建脚本需要更新（已完成）

## 优势总结

### 对开发者
1. **更清晰的项目结构**: 一目了然的模块组织
2. **更快的理解速度**: 新成员快速上手
3. **更灵活的开发**: 可以独立开发各个子模块

### 对架构师
1. **更好的模块化**: 清晰的模块边界
2. **灵活的部署策略**: 支持单体和微服务
3. **便于扩展**: 新增模块只需在 woodlin-system 下添加

### 对运维人员
1. **简化的依赖关系**: 统一的模块管理
2. **灵活的部署方式**: 可选择性部署模块
3. **更好的可维护性**: 清晰的服务边界

## 总结

本次重构成功地将分散的模块整合到统一的 `woodlin-system` 下，实现了更清晰的项目结构和更灵活的部署方式。这种架构设计既支持传统的单体应用部署，也为未来的微服务拆分提供了良好的基础。

**关键改进**:
- ✅ 模块数量从 11 个减少到 5 个（根 POM）
- ✅ 保持向后兼容（Java 代码无需修改）
- ✅ 支持灵活的部署策略
- ✅ 清晰的模块边界和依赖关系
- ✅ 完整的文档和迁移指南
