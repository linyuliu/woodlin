# Woodlin 注释规范

## 概述

本文档定义了Woodlin项目中的注释规范,确保代码注释的一致性、简洁性和可读性。

## 核心原则

1. **简洁明了**: 注释应简短精炼,直接表达意图
2. **块注释优先**: 使用块注释而非行尾注释
3. **中文注释**: 使用简体中文,便于团队理解
4. **避免冗余**: 不写显而易见的注释
5. **及时更新**: 代码变更时同步更新注释

## Java注释规范

### 1. 类注释 (必须)

**格式**: JavaDoc块注释

```java
/**
 * 用户服务实现类
 * 
 * @author mumu
 * @description 提供用户管理的核心业务逻辑
 * @since 2025-01-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 实现代码
}
```

**规则**:
- ✅ 所有public类必须有类注释
- ✅ 包含@author、@description、@since
- ✅ 简短描述类的职责(一句话)
- ❌ 不要写过长的描述

### 2. 方法注释 (public方法必须)

**格式**: JavaDoc块注释

```java
/**
 * 创建用户
 * 
 * @param user 用户信息
 * @return 是否成功
 */
public boolean createUser(User user) {
    return this.save(user);
}
```

**简化版本**(无参数时):
```java
/**
 * 查询所有用户
 */
public List<User> listAll() {
    return this.list();
}
```

**规则**:
- ✅ public方法必须有注释
- ✅ protected方法推荐有注释
- ⚠️ private方法可选
- ❌ 不要注释getter/setter(Lombok自动生成的)

### 3. 字段注释

**格式**: JavaDoc单行或块注释

```java
/**
 * 用户ID
 */
private Long userId;

/**
 * 用户名
 */
private String username;
```

**Entity类中的字段**:
```java
@TableField("username")
@Schema(description = "用户名")
private String username;
```

**规则**:
- ✅ Entity字段使用@Schema注解描述
- ✅ 普通类字段使用JavaDoc注释
- ⚠️ 常量字段必须注释
- ❌ 不要在注释中重复字段名

### 4. 代码块注释 (推荐)

**格式**: 块注释

```java
/**
 * 验证用户名唯一性
 */
if (!checkUsernameUnique(user)) {
    throw new BusinessException("用户名已存在");
}

/**
 * 密码加密处理
 */
String encryptedPassword = passwordEncoder.encode(user.getPassword());
user.setPassword(encryptedPassword);
```

**规则**:
- ✅ 复杂逻辑前使用块注释
- ✅ 注释与代码之间无空行
- ❌ 不使用行尾注释
- ❌ 不注释简单明了的代码

### 5. 禁止的注释方式

❌ **行尾注释**:
```java
// 错误示例
int count = 0;  // 计数器
String name = "admin";  // 用户名
```

✅ **正确方式**:
```java
/**
 * 计数器
 */
int count = 0;

/**
 * 用户名
 */
String name = "admin";
```

❌ **过长的注释**:
```java
// 错误示例
/**
 * 这个方法用于创建一个新的用户账户。首先会验证用户名是否已经存在，
 * 如果用户名已经存在则抛出异常。然后会对用户密码进行加密处理，
 * 最后将用户信息保存到数据库中。整个过程都在事务中进行，
 * 如果出现异常会自动回滚。
 */
```

✅ **正确方式**:
```java
/**
 * 创建用户账户
 * 
 * @param user 用户信息
 * @return 是否成功
 */
```

❌ **废弃代码注释**:
```java
// 错误示例
// public void oldMethod() {
//     // 旧的实现方式
// }
```

✅ **正确方式**: 删除废弃代码,使用Git管理版本历史

## TypeScript/Vue注释规范

### 1. 文件头注释 (推荐)

```typescript
/**
 * HTTP请求工具模块
 * 
 * @author mumu
 * @description 基于axios封装的HTTP请求工具
 * @since 2025-01-01
 */
```

### 2. 函数注释

```typescript
/**
 * 发送POST请求
 * 
 * @param url 请求地址
 * @param data 请求数据
 * @returns Promise<T>
 */
export function post<T>(url: string, data?: any): Promise<T> {
    return request.post(url, data)
}
```

### 3. 接口/类型注释

```typescript
/**
 * 用户信息
 */
interface User {
    /** 用户ID */
    id: number
    /** 用户名 */
    username: string
    /** 邮箱 */
    email: string
}
```

### 4. Vue组件注释

```vue
<!--
  用户列表组件
  
  @author mumu
  @description 显示用户列表,支持搜索和分页
  @since 2025-01-01
-->
<script setup lang="ts">
// 组件逻辑
</script>
```

### 5. 代码块注释

```typescript
/**
 * 验证表单
 */
const validateForm = async () => {
    await formRef.value?.validate()
}

/**
 * 提交数据
 */
const submit = async () => {
    await validateForm()
    // 提交逻辑
}
```

## 特殊注释标记

### TODO注释

```java
/**
 * TODO: 实现用户头像上传功能
 */
public void uploadAvatar() {
    throw new UnsupportedOperationException("功能待实现");
}
```

### FIXME注释

```java
/**
 * FIXME: 性能问题需要优化
 */
public List<User> getAllUsers() {
    return userMapper.selectList(null);
}
```

### 废弃标记

```java
/**
 * 获取用户列表
 * 
 * @deprecated 使用 {@link #queryUserPage(User, Integer, Integer)} 代替
 */
@Deprecated
public List<User> getUsers() {
    return this.list();
}
```

## 注释长度规范

### 类注释
- 标题: 1行,不超过50个字符
- 描述: 1-2行,不超过100个字符
- 总计: 不超过5行

### 方法注释
- 标题: 1行,不超过50个字符
- 参数: 每个1行
- 返回值: 1行
- 总计: 不超过10行

### 字段注释
- 单行: 不超过30个字符
- 简洁描述字段用途

### 代码块注释
- 1-3行
- 简要说明代码块的作用

## 常见场景示例

### 场景1: Controller类

```java
/**
 * 用户管理控制器
 * 
 * @author mumu
 * @description 提供用户增删改查接口
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final IUserService userService;
    
    /**
     * 分页查询用户
     * 
     * @param user 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public R<PageResult<User>> list(User user, Integer pageNum, Integer pageSize) {
        IPage<User> page = userService.queryUserPage(user, pageNum, pageSize);
        return R.ok(PageResult.of(page));
    }
}
```

### 场景2: Service类

```java
/**
 * 用户服务实现
 * 
 * @author mumu
 * @description 用户管理核心业务逻辑
 * @since 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    
    private final IPasswordEncoder passwordEncoder;
    
    @Override
    public boolean createUser(User user) {
        /**
         * 验证用户名唯一性
         */
        if (!checkUsernameUnique(user)) {
            throw new BusinessException("用户名已存在");
        }
        
        /**
         * 加密密码
         */
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        return this.save(user);
    }
}
```

### 场景3: Entity类

```java
/**
 * 用户实体
 * 
 * @author mumu
 * @description 系统用户基本信息
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    @Schema(description = "用户ID")
    private Long userId;
    
    @TableField("username")
    @Schema(description = "用户名")
    private String username;
    
    @TableField("password")
    @Schema(description = "密码")
    private String password;
}
```

### 场景4: 工具类

```java
/**
 * 字符串工具类
 * 
 * @author mumu
 * @description 提供字符串处理相关方法
 * @since 2025-01-01
 */
public final class StringUtils {
    
    private StringUtils() {
        /**
         * 禁止实例化
         */
    }
    
    /**
     * 判断字符串是否为空
     * 
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
```

## IDE配置

### IDEA模板配置

1. **类注释模板**:
Settings → Editor → File and Code Templates → Includes → File Header

```
/**
 * ${NAME}
 * 
 * @author ${USER}
 * @description ${description}
 * @since ${YEAR}-${MONTH}-${DAY}
 */
```

2. **方法注释模板**:
Settings → Editor → Live Templates

```
/**
 * $description$
 * $params$
 * @return $return$
 */
```

### ESLint配置 (TypeScript)

```json
{
  "rules": {
    "jsdoc/require-description": "warn",
    "jsdoc/require-param-description": "warn",
    "jsdoc/require-returns-description": "warn"
  }
}
```

## 注释检查清单

在提交代码前检查:

- [ ] 所有public类有注释
- [ ] 所有public方法有注释
- [ ] Entity字段使用@Schema注解
- [ ] 没有行尾注释
- [ ] 没有过长的注释
- [ ] 没有注释掉的废弃代码
- [ ] TODO/FIXME已记录待处理事项
- [ ] 中文注释语法正确

## 最佳实践

1. **先写注释,后写代码**: 帮助理清思路
2. **代码即文档**: 好的命名减少注释需求
3. **注释解释why,不是what**: 代码本身说明what
4. **定期清理**: 删除过时和无用注释
5. **Code Review**: 检查注释质量

## 反例分析

### 反例1: 显而易见的注释

```java
// ❌ 错误
// 设置用户ID为1
user.setUserId(1L);

// ✅ 正确
// 直接写代码,无需注释
user.setUserId(1L);
```

### 反例2: 过度注释

```java
// ❌ 错误
// 获取用户名
String username = user.getUsername();
// 判断用户名是否为空
if (username == null) {
    // 用户名为空,返回false
    return false;
}

// ✅ 正确
if (user.getUsername() == null) {
    return false;
}
```

### 反例3: 中英混杂

```java
// ❌ 错误
/**
 * Save user information
 * 保存用户信息
 */

// ✅ 正确
/**
 * 保存用户信息
 */
```

## 更新日志

- 2025-10-28: 初始版本
- 规范化注释风格
- 统一中文注释要求
- 禁止行尾注释
