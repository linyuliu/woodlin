# 前端配置说明

## 自动导入配置

本项目使用 `unplugin-auto-import` 和 `unplugin-vue-components` 实现自动导入功能，无需手动导入常用的 Vue API 和组件。

### 自动导入的 API

以下 API 可以直接使用，无需手动导入：

#### Vue 核心 API
- `ref`, `reactive`, `computed`, `watch`, `watchEffect` 等
- `onMounted`, `onUnmounted`, `onBeforeMount` 等生命周期钩子
- `inject`, `provide` 等依赖注入
- `nextTick`, `toRefs`, `toRef` 等工具函数

#### Vue Router API
- `useRouter`, `useRoute`
- `onBeforeRouteLeave`, `onBeforeRouteUpdate`

#### Pinia API
- `defineStore`, `storeToRefs`
- `createPinia`, `getActivePinia`, `setActivePinia`

#### Naive UI API
- `useDialog` - 对话框
- `useMessage` - 消息提示
- `useNotification` - 通知
- `useLoadingBar` - 加载条

#### 自定义工具
- `request`, `api` - HTTP 请求工具（来自 `src/utils/request.ts`）
- 所有 `src/composables/` 目录下的组合式函数
- 所有 `src/stores/` 目录下的 store
- 所有 `src/utils/` 目录下的工具函数

### 自动导入的组件

#### Naive UI 组件
所有 Naive UI 组件都会自动导入，可以直接在模板中使用：
```vue
<template>
  <n-button type="primary">按钮</n-button>
  <n-input v-model:value="inputValue" />
  <n-data-table :columns="columns" :data="data" />
</template>
```

#### 自定义组件
`src/components/` 目录下的所有组件也会自动导入：
```vue
<template>
  <PasswordChangeDialog v-model:show="showDialog" />
</template>
```

### 使用示例

**之前（需要手动导入）：**
```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { api } from '@/utils/request'

const message = useMessage()
const router = useRouter()
const count = ref(0)
// ...
</script>
```

**现在（自动导入）：**
```vue
<script setup lang="ts">
const message = useMessage()
const router = useRouter()
const count = ref(0)
// ...
</script>
```

## 开发服务器代理配置

### 配置说明

在开发环境中，前端开发服务器（Vite）会自动将 `/api` 路径的请求代理到后端服务器，解决跨域问题。

### 环境变量

#### 开发环境 (`.env.development`)
```env
# API基础URL - 使用相对路径，通过Vite代理转发
VITE_API_BASE_URL=/api

# 后端服务地址 - 用于Vite代理配置
VITE_BACKEND_URL=http://localhost:8080
```

#### 生产环境 (`.env.production`)
```env
# API基础URL - 生产环境使用相对路径，由Nginx等反向代理处理
VITE_API_BASE_URL=/api
```

### 工作原理

1. **开发环境**：
   - 前端请求：`http://localhost:5173/api/user/list`
   - Vite 代理转发到：`http://localhost:8080/api/user/list`
   - 无需配置 CORS，因为代理在同一个开发服务器上

2. **生产环境**：
   - 前端请求：`/api/user/list`
   - 由 Nginx 或其他反向代理处理
   - 转发到后端服务器

### 修改后端地址

如果需要连接到不同的后端服务器，修改 `.env.development` 文件：

```env
# 连接到远程开发服务器
VITE_BACKEND_URL=http://dev.example.com:8080

# 或连接到本地其他端口
VITE_BACKEND_URL=http://localhost:9090
```

## ESLint 配置

ESLint 已配置为识别自动导入的全局变量，不会报告 `'ref' is not defined` 等错误。

配置文件会自动生成：
- `.eslintrc-auto-import.json` - ESLint 全局变量配置
- `src/auto-imports.d.ts` - TypeScript 类型定义
- `src/components.d.ts` - 组件类型定义

这些文件已添加到 `.gitignore`，不会提交到版本控制。

## TypeScript 支持

自动导入的 API 和组件都有完整的 TypeScript 类型支持，IDE 会提供代码补全和类型检查。

## 构建优化

### 代码分割

构建时会自动进行代码分割：
- `vue-vendor.js` - Vue 核心库（Vue、Vue Router、Pinia）
- `ui-vendor.js` - UI 库（Naive UI）
- 其他按路由分割的代码块

### 压缩优化

生产构建时会：
- 使用 Terser 压缩代码
- 移除所有 `console.log` 和 `debugger` 语句
- 生成压缩后的资源文件

## 开发命令

```bash
# 启动开发服务器（带代理）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行 ESLint 检查并自动修复
npm run lint

# TypeScript 类型检查
npm run type-check
```

## 注意事项

1. **首次构建**：首次运行时会自动生成类型定义文件，可能会看到一些 TypeScript 错误，重新运行即可。

2. **IDE 配置**：
   - VSCode 建议安装 `Vue - Official` 插件
   - 确保 TypeScript 版本与项目一致

3. **性能**：自动导入在开发时按需编译，不会影响开发体验。

4. **调试**：如果遇到自动导入问题，可以查看生成的 `src/auto-imports.d.ts` 文件。
