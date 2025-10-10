# 代码规范

本文档定义了 Woodlin 项目的代码规范，包括 Java 后端代码和 TypeScript 前端代码的编写规范。遵循统一的代码规范有助于提高代码可读性、可维护性和团队协作效率。

## Java 代码规范

### 命名规范

#### 包命名

- 全部小写，使用点号分隔
- 遵循域名反转规则
- 功能相关的类放在同一个包下

```java
// ✅ 正确
com.mumu.woodlin.system.controller
com.mumu.woodlin.system.service
com.mumu.woodlin.system.mapper

// ❌ 错误
com.mumu.Woodlin.System.Controller
com.mumu.woodlin_system.controller
```

#### 类命名

- 使用大驼峰命名法（PascalCase）
- 类名应该是名词
- 接口名不要加 `I` 前缀
- 实现类可以加 `Impl` 后缀
- 抽象类可以加 `Abstract` 或 `Base` 前缀

```java
// ✅ 正确
public class UserController { }
public class UserServiceImpl implements UserService { }
public abstract class BaseEntity { }

// ❌ 错误
public class userController { }  // 首字母小写
public interface IUserService { }  // 接口加 I 前缀
public class User_Service { }  // 使用下划线
```

#### 方法命名

- 使用小驼峰命名法（camelCase）
- 方法名应该是动词或动词短语
- 布尔类型的方法使用 `is`、`has`、`can` 等前缀

```java
// ✅ 正确
public User getUserById(Long id) { }
public void createUser(User user) { }
public boolean isActive() { }
public boolean hasPermission(String permission) { }

// ❌ 错误
public User GetUserById(Long id) { }  // 首字母大写
public void user_create(User user) { }  // 使用下划线
public boolean active() { }  // 布尔方法缺少前缀
```

#### 变量命名

- 使用小驼峰命名法（camelCase）
- 常量使用全大写，下划线分隔
- 避免使用单字母变量名（循环计数器除外）

```java
// ✅ 正确
private String userName;
private int userAge;
public static final String DEFAULT_PASSWORD = "123456";
public static final int MAX_RETRY_COUNT = 3;

for (int i = 0; i < list.size(); i++) { }

// ❌ 错误
private String UserName;  // 首字母大写
private String user_name;  // 使用下划线
private String un;  // 无意义缩写
public static final String defaultPassword = "123456";  // 常量使用小写
```

### 注释规范

#### 类注释

```java
/**
 * 用户服务实现类
 * 
 * <p>提供用户相关的业务逻辑处理，包括：
 * <ul>
 *   <li>用户的增删改查</li>
 *   <li>用户密码管理</li>
 *   <li>用户状态管理</li>
 * </ul>
 * 
 * @author mumu
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {
    // 类实现
}
```

#### 方法注释

```java
/**
 * 根据用户 ID 获取用户信息
 * 
 * @param userId 用户 ID，不能为空
 * @return 用户信息，如果用户不存在返回 null
 * @throws IllegalArgumentException 如果 userId 为空
 */
public User getUserById(Long userId) {
    // 方法实现
}

/**
 * 创建新用户
 * 
 * <p>该方法会进行以下操作：
 * <ol>
 *   <li>验证用户名是否重复</li>
 *   <li>加密用户密码</li>
 *   <li>保存用户信息到数据库</li>
 *   <li>发送欢迎邮件（异步）</li>
 * </ol>
 * 
 * @param user 用户信息，不能为空
 * @return 创建成功的用户信息（包含生成的 ID）
 * @throws BusinessException 如果用户名已存在
 */
public User createUser(User user) {
    // 方法实现
}
```

#### 字段注释

```java
/** 用户 ID（主键） */
private Long id;

/** 用户名（唯一） */
private String username;

/** 加密后的密码 */
private String password;

/**
 * 用户状态
 * 
 * <ul>
 *   <li>0 - 禁用</li>
 *   <li>1 - 正常</li>
 *   <li>2 - 锁定</li>
 * </ul>
 */
private Integer status;
```

#### 行内注释

```java
// 验证用户名是否重复
if (userMapper.existsByUsername(username)) {
    throw new BusinessException("用户名已存在");
}

// TODO: 后续需要添加邮箱验证功能
// FIXME: 密码加密算法需要升级为 BCrypt
// NOTE: 这里使用缓存可以提升性能
```

### 代码格式

#### 缩进

- 使用 **4 个空格** 进行缩进
- 不要使用 Tab 键

```java
// ✅ 正确
public class UserController {
    
    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        List<User> users = userService.listUsers();
        return Result.success(users);
    }
}
```

#### 大括号

- 左大括号不换行
- 右大括号独占一行
- `if`、`for`、`while` 等语句即使只有一行也要使用大括号

```java
// ✅ 正确
if (user != null) {
    return user.getName();
}

for (int i = 0; i < 10; i++) {
    System.out.println(i);
}

// ❌ 错误
if (user != null)
{
    return user.getName();
}

if (user != null) return user.getName();  // 缺少大括号
```

#### 空格

- 操作符前后加空格
- 逗号后面加空格
- 关键字后面加空格

```java
// ✅ 正确
int sum = a + b;
if (x > 0) { }
for (int i = 0; i < 10; i++) { }
method(a, b, c);

// ❌ 错误
int sum=a+b;
if(x>0){ }
method(a,b,c);
```

#### 空行

- 方法之间空一行
- 逻辑块之间空一行
- 导入语句和类定义之间空一行

```java
import java.util.List;
import java.util.ArrayList;

public class UserService {
    
    public List<User> listUsers() {
        // 查询数据库
        List<User> users = userMapper.selectList(null);
        
        // 过滤无效用户
        users = users.stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
        
        return users;
    }
    
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
```

### 最佳实践

#### Controller 层

```java
/**
 * 用户控制器
 * 
 * @author mumu
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表")
    public Result<PageResult<User>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username) {
        
        PageResult<User> result = userService.listUsers(page, size, username);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return Result.success(user);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
```

#### Service 层

```java
/**
 * 用户服务实现类
 * 
 * @author mumu
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public PageResult<User> listUsers(Integer page, Integer size, String username) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(username), User::getUsername, username)
               .orderByDesc(User::getCreateTime);
        
        // 分页查询
        Page<User> pageParam = new Page<>(page, size);
        Page<User> pageResult = userMapper.selectPage(pageParam, wrapper);
        
        // 构建返回结果
        return PageResult.<User>builder()
                .records(pageResult.getRecords())
                .total(pageResult.getTotal())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    public User getUserById(Long id) {
        // 先从缓存获取
        String cacheKey = "user:" + id;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        if (user != null) {
            return user;
        }
        
        // 从数据库查询
        user = userMapper.selectById(id);
        if (user != null) {
            // 写入缓存
            redisTemplate.opsForValue().set(cacheKey, user, 1, TimeUnit.HOURS);
        }
        
        return user;
    }
    
    @Override
    public User createUser(UserDTO userDTO) {
        // 验证用户名是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.exists(wrapper)) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
        user.setPassword(encryptedPassword);
        
        // 保存到数据库
        userMapper.insert(user);
        
        // 异步发送欢迎邮件
        asyncService.sendWelcomeEmail(user.getEmail());
        
        return user;
    }
}
```

#### Entity 层

```java
/**
 * 用户实体类
 * 
 * @author mumu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    /** 用户 ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** 用户名 */
    @TableField("username")
    private String username;
    
    /** 密码 */
    @TableField("password")
    private String password;
    
    /** 真实姓名 */
    @TableField("realname")
    private String realname;
    
    /** 邮箱 */
    @TableField("email")
    private String email;
    
    /** 手机号 */
    @TableField("phone")
    private String phone;
    
    /** 用户状态（0-禁用 1-正常 2-锁定） */
    @TableField("status")
    private Integer status;
    
    /** 部门 ID */
    @TableField("dept_id")
    private Long deptId;
    
    /** 租户 ID */
    @TableField("tenant_id")
    private Long tenantId;
}
```

## TypeScript 代码规范

### 命名规范

#### 文件命名

- 组件文件使用大驼峰：`UserList.vue`
- 普通文件使用小驼峰：`userService.ts`
- 类型文件使用小驼峰：`userTypes.ts`

```
src/
├── components/
│   ├── UserList.vue
│   └── UserForm.vue
├── api/
│   └── userApi.ts
├── types/
│   └── userTypes.ts
└── utils/
    └── formatUtil.ts
```

#### 变量和函数命名

```typescript
// ✅ 正确
const userName: string = 'admin';
const userAge: number = 25;
const isActive: boolean = true;

function getUserById(id: number): User {
  // 函数实现
}

// ❌ 错误
const UserName: string = 'admin';  // 首字母大写
const user_name: string = 'admin';  // 使用下划线
function GetUserById(id: number): User { }  // 首字母大写
```

#### 类型和接口命名

```typescript
// ✅ 正确
interface User {
  id: number;
  username: string;
}

type UserStatus = 'active' | 'inactive' | 'locked';

class UserService {
  // 类实现
}

// ❌ 错误
interface IUser { }  // 接口加 I 前缀
interface user { }  // 首字母小写
```

#### 常量命名

```typescript
// ✅ 正确
const DEFAULT_PAGE_SIZE = 10;
const MAX_RETRY_COUNT = 3;
const API_BASE_URL = '/api';

// ❌ 错误
const defaultPageSize = 10;  // 使用小驼峰
const max_retry_count = 3;  // 使用下划线
```

### 类型定义

#### 接口定义

```typescript
/**
 * 用户信息接口
 */
export interface User {
  /** 用户 ID */
  id: number;
  /** 用户名 */
  username: string;
  /** 真实姓名 */
  realname: string;
  /** 邮箱 */
  email?: string;
  /** 手机号 */
  phone?: string;
  /** 用户状态 */
  status: UserStatus;
  /** 创建时间 */
  createTime: string;
}

/**
 * 用户状态类型
 */
export type UserStatus = 'active' | 'inactive' | 'locked';

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  /** 页码 */
  page: number;
  /** 每页数量 */
  size: number;
  /** 用户名（模糊查询） */
  username?: string;
  /** 用户状态 */
  status?: UserStatus;
}
```

#### 函数类型

```typescript
/**
 * 获取用户列表的函数类型
 */
type GetUserListFunction = (params: UserQueryParams) => Promise<PageResult<User>>;

/**
 * 用户操作回调函数类型
 */
type UserActionCallback = (user: User) => void;
```

### Vue 3 组件规范

#### 组件结构

```vue
<template>
  <div class="user-list">
    <!-- 搜索表单 -->
    <n-form inline :model="queryParams" class="search-form">
      <n-form-item label="用户名">
        <n-input v-model:value="queryParams.username" placeholder="请输入用户名" />
      </n-form-item>
      <n-form-item>
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
      </n-form-item>
    </n-form>
    
    <!-- 数据表格 -->
    <n-data-table
      :columns="columns"
      :data="userList"
      :loading="loading"
      :pagination="pagination"
      @update:page="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { NForm, NFormItem, NInput, NButton, NDataTable } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { getUserList } from '@/api/userApi';
import type { User, UserQueryParams } from '@/types/userTypes';

/**
 * 查询参数
 */
const queryParams = reactive<UserQueryParams>({
  page: 1,
  size: 10,
  username: ''
});

/**
 * 用户列表数据
 */
const userList = ref<User[]>([]);

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 分页配置
 */
const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
});

/**
 * 表格列定义
 */
const columns: DataTableColumns<User> = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    key: 'username',
    width: 120
  },
  {
    title: '真实姓名',
    key: 'realname',
    width: 120
  },
  {
    title: '邮箱',
    key: 'email',
    width: 200
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h('div', [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
      ]);
    }
  }
];

/**
 * 加载用户列表
 */
const loadUserList = async () => {
  loading.value = true;
  try {
    const result = await getUserList(queryParams);
    userList.value = result.records;
    pagination.pageCount = Math.ceil(result.total / pagination.pageSize);
  } catch (error) {
    console.error('加载用户列表失败:', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 处理搜索
 */
const handleSearch = () => {
  queryParams.page = 1;
  loadUserList();
};

/**
 * 处理重置
 */
const handleReset = () => {
  queryParams.username = '';
  handleSearch();
};

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  queryParams.page = page;
  loadUserList();
};

/**
 * 处理编辑
 */
const handleEdit = (user: User) => {
  // 编辑逻辑
};

/**
 * 处理删除
 */
const handleDelete = (user: User) => {
  // 删除逻辑
};

// 组件挂载时加载数据
onMounted(() => {
  loadUserList();
});
</script>

<style scoped lang="scss">
.user-list {
  padding: 20px;
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
```

### API 层规范

```typescript
/**
 * 用户 API
 * 
 * @module api/userApi
 */

import request from '@/utils/request';
import type { User, UserQueryParams } from '@/types/userTypes';
import type { Result, PageResult } from '@/types/commonTypes';

/**
 * 获取用户列表
 * 
 * @param params 查询参数
 * @returns 用户列表
 */
export function getUserList(params: UserQueryParams): Promise<PageResult<User>> {
  return request.get<PageResult<User>>('/users', { params });
}

/**
 * 获取用户详情
 * 
 * @param id 用户 ID
 * @returns 用户详情
 */
export function getUserById(id: number): Promise<User> {
  return request.get<User>(`/users/${id}`);
}

/**
 * 创建用户
 * 
 * @param user 用户信息
 * @returns 创建结果
 */
export function createUser(user: Partial<User>): Promise<User> {
  return request.post<User>('/users', user);
}

/**
 * 更新用户
 * 
 * @param id 用户 ID
 * @param user 用户信息
 * @returns 更新结果
 */
export function updateUser(id: number, user: Partial<User>): Promise<void> {
  return request.put<void>(`/users/${id}`, user);
}

/**
 * 删除用户
 * 
 * @param id 用户 ID
 * @returns 删除结果
 */
export function deleteUser(id: number): Promise<void> {
  return request.delete<void>(`/users/${id}`);
}
```

## 代码检查工具

### ESLint 配置

项目使用 ESLint 进行代码检查：

```bash
# 运行 ESLint 检查
npm run lint

# 自动修复问题
npm run lint:fix
```

### Prettier 配置

使用 Prettier 格式化代码：

```bash
# 格式化所有文件
npm run format
```

## 总结

遵循统一的代码规范能够：

1. **提高代码质量**：减少潜在的 bug 和代码异味
2. **提升可读性**：让代码更容易理解和维护
3. **促进协作**：团队成员之间更容易理解彼此的代码
4. **降低维护成本**：规范的代码更容易修改和扩展

---

::: tip 相关工具
- [Checkstyle](https://checkstyle.sourceforge.io/) - Java 代码风格检查工具
- [ESLint](https://eslint.org/) - JavaScript/TypeScript 代码检查工具
- [Prettier](https://prettier.io/) - 代码格式化工具
:::

::: info 参考资料
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
:::
