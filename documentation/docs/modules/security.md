# woodlin-security - 安全认证模块

## 模块概述

woodlin-security 是 Woodlin 系统的安全认证模块，基于 Sa-Token 实现，提供统一的用户认证、授权、加密解密、权限控制等安全相关功能。

## Maven 坐标

```xml
<dependency>
    <groupId>com.mumu</groupId>
    <artifactId>woodlin-security</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 核心功能

### 1. 用户认证

- **登录认证**：用户名密码登录、手机号登录、第三方登录
- **Token 管理**：Token 生成、验证、刷新、注销
- **会话管理**：单点登录、踢人下线、账号封禁
- **记住我**：支持记住我功能，长期免登录

### 2. 权限控制

- **RBAC 模型**：基于角色的访问控制
- **注解权限**：`@SaCheckPermission`、`@SaCheckRole`
- **编程式权限**：`StpUtil.checkPermission()`
- **数据权限**：基于部门的数据权限控制

### 3. 加密解密

- **AES 加密**：适合大数据量加密
- **RSA 加密**：适合敏感信息加密
- **SM4 加密**：国密算法支持
- **密码加密**：BCrypt 密码哈希

### 4. 安全防护

- **XSS 防护**：跨站脚本攻击防护
- **CSRF 防护**：跨站请求伪造防护
- **SQL 注入防护**：参数验证和转义
- **限流控制**：接口访问频率限制

## 技术实现

基于 **Sa-Token 1.39.0** 实现：

- 轻量级权限认证框架
- 功能全面：登录认证、权限验证、Session 会话、踢人下线、模拟他人账号等
- 简单易用：一行代码实现登录认证
- 扩展性强：支持自定义扩展

## 使用指南

### Token 格式说明

本系统遵循 OAuth2 标准，使用 Bearer Token 认证。客户端在发送请求时，需要在请求头中添加：

```
Authorization: Bearer {token}
```

**示例**：
```
Authorization: Bearer 761010b5-cf1a-4ec5-8624-70ffa4c3bb4b
```

Sa-Token 配置了 `token-prefix: Bearer`，会自动从 Authorization 头中提取 Bearer 之后的 token 值进行验证。

### 登录认证

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        // 1. 验证用户名密码
        User user = userService.authenticate(
            request.getUsername(), 
            request.getPassword()
        );
        
        // 2. 登录成功，创建 Token
        StpUtil.login(user.getId());
        
        // 3. 返回 Token（客户端需要添加 Bearer 前缀）
        String token = StpUtil.getTokenValue();
        return Result.success(token);
    }
    
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
```

### 权限验证

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    // 方式一：注解验证
    @SaCheckPermission("user:add")
    @PostMapping
    public Result<Void> addUser(@RequestBody User user) {
        userService.save(user);
        return Result.success();
    }
    
    // 方式二：编程式验证
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 检查权限
        StpUtil.checkPermission("user:delete");
        
        userService.removeById(id);
        return Result.success();
    }
}
```

### 角色验证

```java
@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @SaCheckRole("admin")
    @PostMapping("/config")
    public Result<Void> updateConfig(@RequestBody ConfigDTO config) {
        configService.update(config);
        return Result.success();
    }
}
```

### 密码加密

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public void register(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        
        // 使用 BCrypt 加密密码
        String encryptedPassword = BCrypt.hashpw(
            dto.getPassword(), 
            BCrypt.gensalt()
        );
        user.setPassword(encryptedPassword);
        
        userMapper.insert(user);
    }
    
    @Override
    public User authenticate(String username, String password) {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        return user;
    }
}
```

## 配置说明

### application.yml 配置

```yaml
# Sa-Token 配置
sa-token:
  # Token 名称（同时也是 Cookie 名称）
  token-name: Authorization
  
  # Token 前缀（符合 OAuth2 标准的 Bearer 前缀）
  token-prefix: Bearer
  
  # Token 有效期（单位：秒），默认 30 天，-1 代表永不过期
  timeout: 2592000
  
  # Token 最低活跃频率（单位：秒），如果 Token 超过此时间没有访问系统就会被冻结，默认 -1 代表不限制，永不冻结
  active-timeout: -1
  
  # 是否允许同一账号多地同时登录（为 true 时允许一起登录，为 false 时新登录挤掉旧登录）
  is-concurrent: true
  
  # 在多人登录同一账号时，是否共用一个 Token（为 true 时所有登录共用一个 Token，为 false 时每次登录新建一个 Token）
  is-share: false
  
  # Token 风格（uuid、simple-uuid、random-32、random-64、random-128、tik）
  token-style: uuid
  
  # 是否输出操作日志
  is-log: true
```

## 最佳实践

### 1. 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        return Result.error(401, "未登录");
    }
    
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        return Result.error(403, "无权限");
    }
}
```

### 2. 自定义权限验证

```java
@Component
public class StpInterfaceImpl implements StpInterface {
    
    @Autowired
    private UserService userService;
    
    /**
     * 返回指定账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userService.getPermissionsByUserId((Long) loginId);
    }
    
    /**
     * 返回指定账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userService.getRolesByUserId((Long) loginId);
    }
}
```

## 相关文档

- [Sa-Token 官方文档](https://sa-token.cc/)
- [woodlin-system - 系统管理](./system)
- [开发指南 - 权限控制](/development/security)

