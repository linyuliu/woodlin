# Woodlin 前端架构文档

> 优雅的前端架构设计，参考 vue-vben-admin 最佳实践

## 📚 目录

- [架构概述](#架构概述)
- [项目配置](#项目配置)
- [HTTP请求](#http请求)
- [路由守卫](#路由守卫)
- [API代码生成](#api代码生成)
- [最佳实践](#最佳实践)

---

## 架构概述

本项目采用现代化的Vue 3 + TypeScript + Vite技术栈，参考vue-vben-admin的设计理念，构建了一套优雅、可维护的前端架构。

### 核心特性

✅ **统一配置管理** - 全局配置系统，支持系统、布局、主题、HTTP、路由等配置  
✅ **优雅的HTTP封装** - 支持请求重试、取消重复请求、自动Token管理  
✅ **完善的路由守卫** - 登录验证、权限检查、页面标题、加载进度  
✅ **Swagger API生成** - 从后端Swagger文档自动生成TypeScript API服务  
✅ **类型安全** - 完整的TypeScript类型定义，消除any类型  
✅ **详细注释** - 所有模块都有完整的中文注释和使用示例

---

## 项目配置

### 配置系统

位置: `src/config/index.ts`

提供统一的全局配置管理，包括系统、布局、主题、HTTP、路由等配置。

#### 使用示例

```typescript
import { getConfig, updateConfig } from '@/config'

// 获取配置
const config = getConfig()
console.log(config.system.title) // 'Woodlin'

// 更新配置
updateConfig({
  system: {
    title: '新系统名称'
  }
})
```

#### 配置项说明

**系统配置 (system)**
- `title`: 系统标题
- `subtitle`: 系统副标题
- `logo`: 系统Logo
- `version`: 系统版本
- `showVersion`: 是否显示版本信息
- `locale`: 默认语言 ('zh-CN' | 'en-US')

**布局配置 (layout)**
- `mode`: 布局模式 ('sidebar' | 'top' | 'mix')
- `fixedHeader`: 是否固定Header
- `fixedSider`: 是否固定Sider
- `showBreadcrumb`: 是否显示面包屑
- `showTabs`: 是否显示标签页
- `showFooter`: 是否显示页脚
- `contentMode`: 内容区域宽度模式 ('full' | 'fixed')
- `siderWidth`: 侧边栏宽度
- `siderCollapsedWidth`: 侧边栏折叠宽度

**主题配置 (theme)**
- `mode`: 主题模式 ('light' | 'dark' | 'auto')
- `primaryColor`: 主题色
- `successColor`: 成功色
- `warningColor`: 警告色
- `errorColor`: 错误色
- `infoColor`: 信息色

**HTTP请求配置 (http)**
- `baseURL`: API基础URL
- `timeout`: 请求超时时间（毫秒）
- `withCredentials`: 是否携带Cookie
- `retryCount`: 请求重试次数
- `retryDelay`: 请求重试延迟（毫秒）
- `tokenKey`: Token存储键名
- `tokenHeaderName`: Token请求头名称

**路由配置 (router)**
- `mode`: 路由模式 ('history' | 'hash')
- `base`: 基础路径
- `enablePermission`: 是否开启路由权限
- `loginPath`: 登录页路径
- `homePath`: 默认首页路径
- `notFoundPath`: 404页面路径
- `enableCache`: 是否开启路由缓存
- `transitionName`: 路由切换动画

---

## HTTP请求

### HTTP工具类

位置: `src/utils/http/request.ts`

优雅的HTTP请求封装，提供以下功能：

- ✅ 自动Token管理
- ✅ 请求重试机制
- ✅ 取消重复请求
- ✅ 统一错误处理
- ✅ 响应数据转换
- ✅ 类型安全的API调用

#### 使用示例

```typescript
import { http } from '@/utils/http'

// GET请求
const users = await http.get<User[]>('/system/user/list', {
  params: { pageNum: 1, pageSize: 20 }
})

// POST请求
const result = await http.post('/system/user', userData, {
  showLoading: true,
  showSuccessMsg: true,
  successMsg: '用户创建成功'
})

// PUT请求
await http.put('/system/user', updatedData)

// DELETE请求
await http.delete('/system/user/1')
```

#### 请求配置选项

```typescript
interface RequestOptions {
  /** 是否显示加载提示 */
  showLoading?: boolean
  /** 是否显示成功提示 */
  showSuccessMsg?: boolean
  /** 是否显示错误提示 */
  showErrorMsg?: boolean
  /** 自定义成功提示消息 */
  successMsg?: string
  /** 自定义错误提示消息 */
  errorMsg?: string
  /** 是否启用请求重试 */
  enableRetry?: boolean
  /** 重试次数 */
  retryCount?: number
  /** 重试延迟（毫秒） */
  retryDelay?: number
  /** 是否需要token认证 */
  requiresAuth?: boolean
}
```

### 类型定义

位置: `src/utils/http/types.ts`

完整的TypeScript类型定义，包括：

- HTTP请求方法枚举
- HTTP响应码枚举
- 业务响应码枚举
- 统一响应结构
- 分页请求参数
- 分页响应数据

---

## 路由守卫

### 守卫系统

位置: `src/router/guards.ts`

优雅的路由守卫实现，提供以下功能：

- ✅ 登录验证
- ✅ 权限检查
- ✅ 页面标题设置
- ✅ 加载进度显示
- ✅ 页面缓存管理

#### 守卫配置

在 `src/router/index.ts` 中自动配置：

```typescript
import { setupRouterGuards } from './guards'

const router = createRouter({ ... })

// 配置所有路由守卫
setupRouterGuards(router)
```

#### 路由权限配置

在路由meta中配置权限：

```typescript
{
  path: 'user',
  name: 'UserManagement',
  component: () => import('@/views/system/UserView.vue'),
  meta: {
    title: '用户管理',
    icon: 'people-outline',
    // 配置所需权限
    permissions: ['system:user:view']
  }
}
```

#### 白名单配置

不需要登录验证的路由：

```typescript
// 在 guards.ts 中配置
const WHITE_LIST = ['/login', '/register', '/forgot-password']
```

---

## API代码生成

### Swagger API生成

从后端Swagger/OpenAPI文档自动生成TypeScript API服务。

#### 使用步骤

1. 启动后端服务（确保 `http://localhost:8080/api/v3/api-docs` 可访问）

2. 运行生成命令：
   ```bash
   npm run generate:api
   ```

3. 生成的API文件位于 `src/api/generated/` 目录

#### 生成内容

- **类型定义** (`types.ts`): 从Swagger Schemas生成的TypeScript接口
- **API服务** (`*.service.ts`): 按Tag分组的API服务方法

#### 生成示例

从以下Swagger定义：

```java
@Tag(name = "用户管理", description = "系统用户管理相关接口")
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    public R<PageResult<SysUser>> list(/* ... */) {
        // ...
    }
}
```

生成TypeScript代码：

```typescript
/**
 * 用户管理 API服务（自动生成）
 */
import { http } from '@/utils/http'
import type * as Types from './types'

/**
 * 分页查询用户列表
 */
export function getUserList(params?: any) {
  return http.get<any>('/system/user/list', { params })
}
```

---

## 最佳实践

### 1. 配置管理

- 使用全局配置系统管理所有配置项
- 通过环境变量覆盖默认配置
- 避免在代码中硬编码配置值

### 2. HTTP请求

- 使用 `http` 工具类而不是直接使用axios
- 为API定义明确的TypeScript类型
- 使用请求配置选项控制行为

### 3. 路由守卫

- 在路由meta中配置权限和标题
- 使用白名单管理公开路由
- 实现用户权限检查逻辑

### 4. API服务

- 使用Swagger生成基础API代码
- 在生成的基础上进行二次封装
- 保持API服务的类型安全

### 5. 代码组织

```
src/
├── api/              # API服务
│   ├── generated/    # 自动生成的API
│   ├── user.ts       # 用户API（手动封装）
│   └── role.ts       # 角色API（手动封装）
├── config/           # 全局配置
│   └── index.ts      # 配置入口
├── router/           # 路由配置
│   ├── index.ts      # 路由入口
│   └── guards.ts     # 路由守卫
├── utils/            # 工具函数
│   ├── http/         # HTTP工具
│   │   ├── index.ts  # HTTP入口
│   │   ├── request.ts # 请求封装
│   │   └── types.ts  # 类型定义
│   └── ...
└── views/            # 页面组件
```

---

## 注意事项

1. **Token管理**: Token自动存储在localStorage，键名可通过配置修改
2. **请求重试**: 默认开启，超时请求自动重试3次
3. **重复请求**: 自动取消重复的pending请求
4. **权限系统**: 需要实现 `getUserPermissions()` 方法获取用户权限
5. **API生成**: 需要后端服务运行才能生成API代码

---

## 相关文档

- [Vue 3 文档](https://cn.vuejs.org/)
- [Vue Router 文档](https://router.vuejs.org/zh/)
- [Axios 文档](https://axios-http.com/)
- [vue-vben-admin](https://github.com/vbenjs/vue-vben-admin)

---

**作者**: mumu  
**更新日期**: 2025-01-01  
**版本**: 1.0.0
