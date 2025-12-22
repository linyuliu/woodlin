# Woodlin Web - 前端管理系统

基于 Vue 3 + TypeScript + Naive UI 构建的现代化多租户中后台管理系统前端。

## 技术栈

- **框架**: Vue 3.5.18 (Composition API)
- **语言**: TypeScript 5.8
- **构建工具**: Vite 7.0
- **UI库**: Naive UI 2.43
- **图标**: @vicons/ionicons5, @vicons/antd
- **HTTP客户端**: Axios 1.12
- **状态管理**: Pinia 3.0
- **路由**: Vue Router 4.5

## 功能特性

### 已实现功能

✅ **用户认证**
- 登录页面（支持用户名密码登录）
- Token认证机制
- 密码修改功能

✅ **仪表板**
- 系统概览统计
- 关键指标展示
- 欢迎页面

✅ **系统管理**
- 用户管理（列表展示、搜索、CRUD操作）
- 角色管理（列表展示、搜索、CRUD操作）
- 部门管理（树形结构展示）
- 租户管理（多租户列表管理）
- 系统设置（配置管理）

✅ **布局系统**
- 侧边栏导航
- 顶部导航栏
- 面包屑导航
- 响应式设计

✅ **工具功能**
- 统一的HTTP请求封装
- 错误处理机制
- 加载状态管理

## 项目结构

```
woodlin-web/
├── src/
│   ├── api/              # API接口定义
│   │   ├── config.ts     # 系统配置API
│   │   ├── user.ts       # 用户管理API
│   │   ├── role.ts       # 角色管理API
│   │   └── tenant.ts     # 租户管理API
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── composables/      # 组合式函数
│   ├── layouts/          # 布局组件
│   │   ├── AdminLayout.vue       # 管理后台布局
│   │   ├── components/
│   │   │   ├── AppSidebar.vue    # 侧边栏
│   │   │   ├── AppHeader.vue     # 顶部导航
│   │   │   └── AppContent.vue    # 内容区域
│   │   └── menu-options.ts       # 菜单配置
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── utils/            # 工具函数
│   │   └── request.ts    # HTTP请求封装
│   ├── views/            # 页面组件
│   │   ├── LoginView.vue         # 登录页
│   │   ├── DashboardView.vue     # 仪表板
│   │   ├── system/               # 系统管理页面
│   │   │   ├── UserView.vue      # 用户管理
│   │   │   ├── RoleView.vue      # 角色管理
│   │   │   ├── DeptView.vue      # 部门管理
│   │   │   └── SystemSettingsView.vue  # 系统设置
│   │   └── tenant/               # 租户管理页面
│   │       └── TenantView.vue    # 租户管理
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── public/               # 公共静态资源
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── index.html            # HTML入口
├── package.json          # 项目依赖
├── tsconfig.json         # TypeScript配置
└── vite.config.ts        # Vite配置
```

## 快速开始

### 环境要求

- Node.js: ^20.19.0 或 >=22.12.0
- npm: 10.8+

### 安装依赖

```sh
npm install
```

### 开发模式

启动开发服务器（支持热更新）：

```sh
npm run dev
```

服务将运行在 http://localhost:5173/

### 构建生产版本

```sh
npm run build
```

构建产物将输出到 `dist/` 目录。

### 预览生产构建

```sh
npm run preview
```

### 代码检查

```sh
npm run lint
```

### 类型检查

```sh
npm run type-check
```

## 环境变量配置

### 开发环境 (`.env.development`)

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 生产环境 (`.env.production`)

```env
VITE_API_BASE_URL=/api
```

## API对接说明

所有API请求通过 `src/utils/request.ts` 统一处理：

- 自动添加Token认证头
- 统一错误处理（401自动跳转登录）
- 请求/响应日志记录
- 支持自定义超时时间

### 使用示例

```typescript
import { getUserList } from '@/api/user'

// 获取用户列表
const users = await getUserList({
  pageNum: 1,
  pageSize: 20,
  username: 'admin'
})
```

## 默认账号

- 用户名: `admin`
- 密码: `Passw0rd`

## 页面路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录页 | 用户登录 |
| `/dashboard` | 仪表板 | 系统概览 |
| `/user` | 用户管理 | 用户CRUD |
| `/role` | 角色管理 | 角色CRUD |
| `/dept` | 部门管理 | 部门树形管理 |
| `/tenant-list` | 租户管理 | 多租户管理 |
| `/system-settings` | 系统设置 | 系统配置 |

## 开发指南

### 添加新页面

1. 在 `src/views/` 创建新的Vue组件
2. 在 `src/router/index.ts` 添加路由配置
3. 在 `src/layouts/menu-options.ts` 添加菜单项

### 添加新API

1. 在 `src/api/` 创建对应的API文件
2. 定义接口类型和请求方法
3. 在组件中导入并使用

### 样式规范

- 使用 Naive UI 组件库提供的组件
- 响应式设计支持移动端
- 使用 scoped CSS 避免样式污染

## IDE推荐配置

推荐使用 [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar)

请禁用 Vetur 以避免冲突。

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 相关文档

- [Vue 3 文档](https://cn.vuejs.org/)
- [Vite 文档](https://cn.vitejs.dev/)
- [Naive UI 文档](https://www.naiveui.com/)
- [TypeScript 文档](https://www.typescriptlang.org/)

## 许可证

MIT License
