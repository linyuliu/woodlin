# Woodlin Lombok 使用规范

## 概述

本文档定义了Woodlin项目中Lombok库的使用规范,确保代码一致性和可维护性。

## 核心原则

1. **保持简洁**: 使用Lombok减少样板代码
2. **明确意图**: 选择合适的注解表达设计意图
3. **避免过度使用**: 不要为了使用而使用
4. **考虑可读性**: 确保代码易于理解和调试

## 必须使用的注解

### 1. @Slf4j (日志)

**用途**: 所有需要日志记录的类

**示例**:
```java
@Slf4j
@Service
public class UserService {
    public void doSomething() {
        log.info("执行操作");
        log.error("发生错误", e);
    }
}
```

**规则**:
- ✅ 在Service、Controller、Component等类中使用
- ❌ 不在Entity、DTO、VO等数据类中使用

### 2. @Data (实体类)

**用途**: Entity、DTO、VO等数据传输对象

**示例**:
```java
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private Long userId;
    private String username;
}
```

**规则**:
- ✅ 用于所有Entity、DTO、VO类
- ✅ 自动生成getter、setter、toString、equals、hashCode
- ⚠️ 需要继承时必须配合@EqualsAndHashCode使用

### 3. @EqualsAndHashCode (继承场景)

**用途**: 继承BaseEntity的实体类

**示例**:
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    // fields
}
```

**规则**:
- ✅ callSuper=true: 继承自BaseEntity时必须使用
- ✅ callSuper=false: 不继承或继承Object时使用
- ❌ 不能省略callSuper参数

### 4. @Accessors(chain = true) (链式调用)

**用途**: 需要链式调用的Entity类

**示例**:
```java
@Data
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String email;
}

// 使用
SysUser user = new SysUser()
    .setUsername("admin")
    .setEmail("admin@example.com");
```

**规则**:
- ✅ 所有Entity类推荐使用
- ✅ DTO、VO类可选使用
- ❌ Service、Controller等业务类禁止使用

### 5. @RequiredArgsConstructor (依赖注入)

**用途**: Service、Controller等需要依赖注入的类

**示例**:
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final IUserMapper userMapper;
    private final IPasswordEncoder passwordEncoder;
    
    // Spring自动注入final字段
}
```

**规则**:
- ✅ 替代@Autowired注解
- ✅ 使用final字段确保不可变性
- ✅ 便于单元测试
- ❌ 不在Entity、DTO、VO中使用

## 可选使用的注解

### 1. @Builder (构建者模式)

**用途**: 复杂对象构建场景

**示例**:
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {
    private String username;
    private Integer pageNum;
    private Integer pageSize;
}

// 使用
QueryRequest request = QueryRequest.builder()
    .username("admin")
    .pageNum(1)
    .pageSize(20)
    .build();
```

**规则**:
- ✅ 用于参数较多的查询对象
- ✅ 需要同时添加@AllArgsConstructor和@NoArgsConstructor
- ⚠️ Entity类不推荐使用(影响MyBatis Plus功能)

### 2. @NoArgsConstructor / @AllArgsConstructor

**用途**: 需要特定构造函数的场景

**示例**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
```

**规则**:
- ✅ @Builder注解时必须同时使用
- ✅ 序列化/反序列化场景建议使用@NoArgsConstructor
- ⚠️ 单独使用@AllArgsConstructor可能导致无参构造丢失

### 3. @Getter / @Setter (细粒度控制)

**用途**: 需要单独控制getter/setter的场景

**示例**:
```java
public class User {
    @Getter
    @Setter
    private String username;
    
    @Getter  // 只读字段
    private LocalDateTime createTime;
    
    @Setter(AccessLevel.PRIVATE)  // 私有setter
    private String password;
}
```

**规则**:
- ✅ 需要只读/只写字段时使用
- ✅ 需要特定访问级别时使用
- ❌ 不要和@Data混用(除非有特殊需求)

## 禁止使用的注解

### 1. @ToString (使用@Data代替)

**原因**: @Data已包含@ToString功能

### 2. @EqualsAndHashCode (单独使用)

**原因**: 应使用@Data,除非需要自定义equals/hashCode逻辑

### 3. @Value (不可变对象)

**原因**: 
- 与Spring的@Value注解冲突
- 在实体类中不适用
- 如需不可变对象,建议使用record(Java 16+)

### 4. @SneakyThrows

**原因**: 
- 隐藏异常处理,降低代码可读性
- 违反异常处理最佳实践
- 建议显式处理或声明异常

### 5. @Cleanup

**原因**: 
- try-with-resources更加标准和清晰
- 建议使用标准Java语法

### 6. @Synchronized

**原因**: 
- 使用Java标准synchronized关键字更清晰
- 或使用并发工具类(ReentrantLock等)

## 标准注解组合

### Entity类标准组合

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("table_name")
@Schema(description = "实体描述")
public class SysEntity extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    
    @TableField("name")
    @Schema(description = "名称")
    private String name;
}
```

### DTO/VO类标准组合

```java
@Data
@Schema(description = "DTO描述")
public class UserDTO {
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
}
```

### Service类标准组合

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final IPasswordEncoder passwordEncoder;
    private final IUserMapper userMapper;
    
    @Override
    public boolean createUser(User user) {
        log.info("创建用户: {}", user.getUsername());
        return this.save(user);
    }
}
```

### Controller类标准组合

```java
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {
    private final IUserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public R<User> getUser(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }
}
```

### 查询请求对象标准组合

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户查询请求")
public class UserQueryRequest {
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "页码")
    private Integer pageNum;
    
    @Schema(description = "每页大小")
    private Integer pageSize;
}
```

## 常见错误和解决方案

### 错误1: Entity类缺少@EqualsAndHashCode(callSuper = true)

❌ **错误示例**:
```java
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    // fields
}
```

✅ **正确示例**:
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    // fields
}
```

### 错误2: Service类使用@Autowired

❌ **错误示例**:
```java
@Service
public class UserService {
    @Autowired
    private IUserMapper userMapper;
}
```

✅ **正确示例**:
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final IUserMapper userMapper;
}
```

### 错误3: @Builder缺少无参构造

❌ **错误示例**:
```java
@Data
@Builder
public class QueryRequest {
    private String username;
}
```

✅ **正确示例**:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private String username;
}
```

### 错误4: 混用@Data和@Getter/@Setter

❌ **错误示例**:
```java
@Data
public class User {
    @Getter
    @Setter
    private String username;  // 冗余,@Data已包含
}
```

✅ **正确示例**:
```java
@Data
public class User {
    private String username;
}
```

## 特殊场景处理

### 1. 敏感字段处理

对于密码等敏感字段,应在toString中排除:

```java
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password"})
public class SysUser extends BaseEntity {
    private String username;
    private String password;  // 不在toString中显示
}
```

### 2. 序列化场景

需要序列化的类应添加serialVersionUID:

```java
@Data
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String username;
}
```

### 3. 循环引用处理

存在双向关联时,应排除关联字段:

```java
@Data
@ToString(exclude = {"parent"})
@EqualsAndHashCode(exclude = {"parent"})
public class TreeNode {
    private Long id;
    private TreeNode parent;  // 排除以避免循环引用
    private List<TreeNode> children;
}
```

## IDE配置

### IDEA Lombok插件

1. 安装Lombok插件: Settings → Plugins → 搜索"Lombok"
2. 启用注解处理: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing
3. 添加Lombok依赖到项目

### Eclipse Lombok配置

1. 下载lombok.jar
2. 运行: `java -jar lombok.jar`
3. 选择Eclipse安装目录
4. 重启Eclipse

## 版本要求

- Lombok版本: 1.18.30+
- Java版本: 17+
- 构建工具: Maven 3.8+

## 参考资源

- [Lombok官方文档](https://projectlombok.org/)
- [Lombok注解速查表](https://projectlombok.org/features/)
- [MyBatis Plus与Lombok最佳实践](https://baomidou.com/)

## 变更历史

- 2025-10-28: 初始版本
