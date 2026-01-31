import { RouterView, type RouteComponent } from 'vue-router'
import { LAYOUT } from './route-constants'

// 自动收集 views 下的页面组件，支持 .vue / .tsx
const viewModules: Record<string, () => Promise<any>> = import.meta.glob('@/views/**/*.{vue,tsx}')

/**
 * 后端常用 component 标识到实际视图文件的别名映射
 * 兼容 ruoyi 等约定的 "system/user/index" 写法
 */
const componentAlias: Record<string, string> = {
  'dashboard/index': '/src/views/DashboardView.vue',
  'home/index': '/src/views/HomeView.vue',
  'system/user/index': '/src/views/system/UserView.vue',
  'system/role/index': '/src/views/system/RoleView.vue',
  'system/dept/index': '/src/views/system/DeptView.vue',
  'system/menu/index': '/src/views/system/MenuView.vue',
  'system/dict/index': '/src/views/system/DictView.vue',
  'system/config/index': '/src/views/system/ConfigView.vue',
  'system/permission/index': '/src/views/system/permission/PermissionManagementView.vue',
  'system/settings/index': '/src/views/system/SystemSettingsView.vue',
  'datasource/list': '/src/views/datasource/DatasourceList.vue',
  'datasource/monitor': '/src/views/datasource/DatasourceMonitor.vue',
  'tenant/list': '/src/views/tenant/TenantView.vue',
  'file/list': '/src/views/file/FileList.vue',
  'file/storage': '/src/views/file/FileStorage.vue',
  'task/list': '/src/views/task/TaskList.vue',
  'task/log': '/src/views/task/TaskLog.vue',
  'dev/sql2api': '/src/views/sql2api/SqlApiEditor.vue',
  'dev/generator': '/src/views/dev/CodeGenerator.vue'
}

/**
 * 根据后端返回的 component 标识解析为实际组件。
 * 约定：component 传入相对路径（不含后缀），例如 "system/UserView" -> src/views/system/UserView.vue
 * 同时兼容 ruoyi 风格的 "system/user/index" 大小写不敏感写法。
 */
export function resolveRouteComponent(component?: string): RouteComponent {
  if (!component) {
    return RouterView
  }

  if (component === 'LAYOUT' || component === 'AdminLayout') {
    return LAYOUT
  }

  // 兼容以 / 开头或不带前缀的路径
  const normalized = component.replace(/^\/+/, '')
  const possibleKeys = [
    `/src/views/${normalized}.vue`,
    `/src/views/${normalized}.tsx`,
    `/src/views/${normalized}/index.vue`,
    `/src/views/${normalized}/index.tsx`
  ]

  // 1) 精确匹配大小写
  const matched = possibleKeys.find(key => viewModules[key])
  if (matched) {
    return viewModules[matched]
  }

  // 2) 别名映射（忽略大小写）
  const aliasKey = normalized.toLowerCase()
  const aliasPath = componentAlias[aliasKey]
  if (aliasPath && viewModules[aliasPath]) {
    return viewModules[aliasPath]
  }

  // 3) 模糊匹配：尝试忽略大小写查找
  const looseMatch = Object.keys(viewModules).find(key =>
    key.toLowerCase() === `/src/views/${aliasKey}.vue` ||
    key.toLowerCase().endsWith(`/${aliasKey}.vue`) ||
    key.toLowerCase().endsWith(`/${aliasKey}/index.vue`)
  )
  if (looseMatch) {
    return viewModules[looseMatch]
  }

  // 找不到匹配时用 RouterView 兜底，避免路由生成失败
  return RouterView
}
