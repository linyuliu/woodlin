# woodlin-common - 通用模块

## 模块概述

woodlin-common 是 Woodlin 系统的基础通用模块，提供了系统中所有模块都会用到的通用功能、工具类、常量定义、异常处理和响应封装等基础设施。

## Maven 坐标

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 包结构

该模块包含以下核心包：

- **annotation** - 自定义注解（日志注解、数据权限注解、限流注解等）
- **config** - 公共配置（Jackson 配置、Web MVC 配置、线程池配置等）
- **constant** - 常量定义（系统常量、错误码、缓存常量、HTTP 状态码等）
- **domain** - 通用实体（基础实体类、树形实体类、分页查询参数等）
- **exception** - 异常定义（基础异常类、业务异常、系统异常、全局异常处理器等）
- **response** - 响应封装（统一响应结果、分页响应结果、表格数据响应等）
- **util** - 工具类（字符串工具、日期工具、JSON 工具、IP 工具、文件工具、树形结构工具等）

## 核心功能

### 1. 统一响应封装

所有 API 接口都使用统一的响应格式 Result 类，包含以下字段：

- **code** - 响应码（200 表示成功，其他表示失败）
- **message** - 响应消息
- **data** - 响应数据（泛型类型）
- **timestamp** - 响应时间戳

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员"
  },
  "timestamp": 1704067200000
}
```

### 2. 异常处理体系

提供完整的异常层次结构：

- **BaseException** - 基础异常类，所有自定义异常的父类
- **BusinessException** - 业务异常，用于业务逻辑错误
- **SystemException** - 系统异常，用于系统级错误
- **GlobalExceptionHandler** - 全局异常处理器，统一处理各种异常

### 3. 基础实体类

#### BaseEntity - 基础实体

所有实体类都应继承 BaseEntity，它包含以下通用字段：

- **id** - 主键 ID（自动递增）
- **createBy** - 创建人 ID（自动填充）
- **createTime** - 创建时间（自动填充）
- **updateBy** - 更新人 ID（自动填充）
- **updateTime** - 更新时间（自动填充）
- **delFlag** - 删除标志（逻辑删除）
- **remark** - 备注

#### TreeEntity - 树形实体

用于实现树形结构的实体，包含以下字段：

- **parentId** - 父级 ID
- **ancestors** - 祖级列表（逗号分隔）
- **orderNum** - 排序号
- **children** - 子节点列表（不持久化）

### 4. 常量定义

#### SystemConstant - 系统常量

定义了系统中使用的常量：

- **DEFAULT_PASSWORD** - 默认密码：`"Passw0rd"`
- **SUPER_ADMIN_ID** - 超级管理员 ID：`1L`
- **TOP_DEPT_ID** - 顶级部门 ID：`0L`
- **STATUS_NORMAL** - 正常状态：`0`
- **STATUS_DISABLE** - 停用状态：`1`
- **DEL_FLAG_NORMAL** - 未删除标志：`0`
- **DEL_FLAG_DELETED** - 已删除标志：`1`

#### ErrorCode - 错误码

定义了标准的错误码：

- **SUCCESS**(200) - 操作成功
- **ERROR**(500) - 操作失败
- **PARAM_ERROR**(400) - 参数错误
- **UNAUTHORIZED**(401) - 未认证
- **FORBIDDEN**(403) - 无权限
- **NOT_FOUND**(404) - 资源不存在

### 5. 工具类

提供了丰富的工具类：

- **StringUtil** - 字符串工具（判空、驼峰转下划线等）
- **DateUtil** - 日期工具（格式化、解析、计算等）
- **JsonUtil** - JSON 工具（序列化、反序列化）
- **IpUtil** - IP 工具（获取客户端 IP、IP 地址解析等）
- **FileUtil** - 文件工具（文件上传、下载、删除等）
- **TreeUtil** - 树形结构工具（构建树形结构）
- **ServletUtil** - Servlet 工具（获取请求参数、响应操作等）

### 6. 自定义注解

#### @Log - 操作日志注解

用于自动记录操作日志，包含以下属性：

- **module** - 模块名称
- **type** - 操作类型（INSERT、UPDATE、DELETE、SELECT、OTHER）
- **description** - 操作说明
- **isSaveRequestData** - 是否保存请求参数
- **isSaveResponseData** - 是否保存响应参数

## 配置类

### JacksonConfig - Jackson 配置

统一配置 JSON 序列化和反序列化规则：

- 日期格式化为 `yyyy-MM-dd HH:mm:ss`
- 支持 Java 8 时间类型（LocalDateTime、LocalDate 等）
- 忽略未知属性
- Long 类型转 String（解决前端精度丢失问题）

### WebMvcConfig - Web MVC 配置

配置 Web MVC 相关功能：

- 跨域配置（CORS）
- 静态资源映射
- 拦截器配置
- 消息转换器配置

## 使用建议

### 1. 异常处理

- 业务逻辑错误使用 `BusinessException`
- 系统错误使用 `SystemException`
- 参数验证错误交给 `@Valid` 注解处理
- 所有异常都会被 `GlobalExceptionHandler` 统一处理

### 2. 响应格式

- 所有 API 统一返回 `Result` 类型
- 分页查询返回 `PageResult` 类型
- 不要在 Controller 中直接返回实体或集合

### 3. 实体类设计

- 所有实体继承 `BaseEntity`
- 树形实体继承 `TreeEntity`
- 使用 Lombok 简化代码
- 合理使用 MyBatis Plus 注解

### 4. 工具类使用

- 优先使用 Hutool 工具类
- common 模块的工具类是对 Hutool 的补充
- 不要重复造轮子

## 最佳实践

### 统一响应格式

推荐做法：所有 Controller 方法返回 `Result` 类型

```java
@GetMapping("/users")
public Result getUsers() {
    List users = userService.list();
    return Result.success(users);
}
```

### 异常处理

推荐做法：使用 `BusinessException` 抛出业务异常

```java
public User getById(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new BusinessException("用户不存在");
    }
    return user;
}
```

### 常量使用

推荐做法：使用常量代替魔法数字

```java
if (user.getStatus().equals(SystemConstant.STATUS_NORMAL)) {
    // 处理逻辑
}
```

## 相关文档

- [woodlin-dependencies - 依赖管理](./dependencies)
- [woodlin-security - 安全模块](./security)
- [开发指南 - 代码规范](/development/code-style)

---

::: tip 提示
woodlin-common 是所有模块的基础，深入理解这个模块有助于更好地开发业务功能。
:::
