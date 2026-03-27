/**
 * 动态菜单生成工具
 * 
 * @author mumu
 * @description 根据路由配置动态生成菜单
 * @since 2025-01-01
 */

import { h, type Component } from 'vue'
import { NIcon, type MenuOption } from 'naive-ui'
import type { RouteRecordRaw } from 'vue-router'
import {
  ArchiveOutline,
  BookOutline,
  BusinessOutline,
  CodeSlashOutline,
  CogOutline,
  ConstructOutline,
  FolderOutline,
  GitCompareOutline,
  HomeOutline,
  KeyOutline,
  LayersOutline,
  ListOutline,
  OptionsOutline,
  PeopleOutline,
  ReceiptOutline,
  ServerOutline,
  SettingsOutline,
  ShieldCheckmarkOutline,
  ShuffleOutline,
  SpeedometerOutline,
  StatsChartOutline,
  SwapHorizontalOutline,
  TerminalOutline,
  TimeOutline,
  TimerOutline
} from '@vicons/ionicons5'
type MenuOptionWithRoute = MenuOption & {route?: RouteRecordRaw}
type IconRenderer = NonNullable<MenuOption['icon']>

/**
 * 菜单图标注册表。
 *
 * 这里显式维护项目当前会用到的菜单图标，
 * 避免 `import * as Icons` 把整个 ionicons 图标包打进主产物。
 */
const ICON_COMPONENTS = {
  ArchiveOutline,
  BookOutline,
  BusinessOutline,
  CodeSlashOutline,
  CogOutline,
  ConstructOutline,
  FolderOutline,
  GitCompareOutline,
  HomeOutline,
  KeyOutline,
  LayersOutline,
  ListOutline,
  OptionsOutline,
  PeopleOutline,
  ReceiptOutline,
  ServerOutline,
  SettingsOutline,
  ShieldCheckmarkOutline,
  ShuffleOutline,
  SpeedometerOutline,
  StatsChartOutline,
  SwapHorizontalOutline,
  TerminalOutline,
  TimeOutline,
  TimerOutline
} satisfies Record<string, Component>

/**
 * 内置的图标别名映射，兼容后端返回的简短/旧标识
 */
const FALLBACK_ICONS: Record<string, keyof typeof ICON_COMPONENTS> = {
  dashboard: 'SpeedometerOutline',
  home: 'HomeOutline',
  system: 'SettingsOutline',
  setting: 'CogOutline',
  user: 'PeopleOutline',
  users: 'PeopleOutline',
  role: 'ShieldCheckmarkOutline',
  menu: 'ListOutline',
  permission: 'KeyOutline',
  dept: 'BusinessOutline',
  department: 'BusinessOutline',
  dict: 'BookOutline',
  config: 'OptionsOutline',
  datasource: 'ServerOutline',
  tenant: 'HomeOutline',
  file: 'FolderOutline',
  storage: 'ArchiveOutline',
  task: 'TimerOutline',
  log: 'ReceiptOutline',
  dev: 'CodeSlashOutline',
  generator: 'ConstructOutline',
  sql2api: 'TerminalOutline',
  monitor: 'StatsChartOutline',
  etl: 'GitCompareOutline',
  offline: 'SwapHorizontalOutline'
}

/**
 * 渲染图标
 * @param icon 图标组件或图标名称
 */
function renderIcon(icon?: Component | string, fallbackKey?: string) {
  const candidates = collectIconCandidates(icon, fallbackKey)

  for (const candidate of candidates) {
    if (typeof candidate !== 'string') {
      return toIconRenderer(candidate)
    }
    const resolved = resolveStringIcon(candidate)
    if (resolved) {
      return toIconRenderer(resolved)
    }
  }

  return undefined
}

/**
 * 组装图标候选集合
 */
function collectIconCandidates(icon?: Component | string, fallbackKey?: string): Array<string | Component> {
  const candidates: Array<string | Component> = []

  if (icon) {
    candidates.push(icon)
  }

  if (fallbackKey) {
    const parts = fallbackKey.split('/').filter(Boolean)
    if (parts.length > 0) {
      candidates.push(parts[parts.length - 1])
      candidates.push(parts[0])
    }
  }

  return candidates
}

/**
 * 将图标组件包装为 NaiveUI 渲染函数
 */
function toIconRenderer(iconComponent: Component): IconRenderer {
  return () => h(NIcon, null, { default: () => h(iconComponent) })
}

/**
 * 根据字符串图标名称解析实际组件
 */
function resolveStringIcon(candidate: string): Component | undefined {
  const raw = candidate.trim()
  if (!raw) {
    return undefined
  }

  const direct = ICON_COMPONENTS[raw as keyof typeof ICON_COMPONENTS]
  if (direct) {
    return direct
  }

  const iconName = toPascalCase(raw)
  const pascal = ICON_COMPONENTS[iconName as keyof typeof ICON_COMPONENTS]
  if (pascal) {
    return pascal
  }

  const alias = FALLBACK_ICONS[raw.toLowerCase()] || FALLBACK_ICONS[iconName.toLowerCase()]
  if (!alias) {
    return undefined
  }
  return ICON_COMPONENTS[alias]
}

/**
 * 将 kebab-case 或 camelCase 转换为 PascalCase
 * @param str 字符串
 */
function toPascalCase(str: string): string {
  return str
    .replace(/-([a-z])/g, (_, letter) => letter.toUpperCase())
    .replace(/^[a-z]/, (letter) => letter.toUpperCase())
}

/**
 * 从路由生成菜单选项
 * @param routes 路由配置
 * @param parentPath 父路径
 */
export function generateMenuFromRoutes(
  routes: RouteRecordRaw[],
  parentPath = ''
): MenuOption[] {
  return sortRoutesByOrder(routes)
    .filter((route) => !route.meta?.hideInMenu)
    .map((route) => buildMenuOption(route, parentPath))
}

/**
 * 按菜单顺序排序路由
 */
function sortRoutesByOrder(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  return [...routes].sort((a, b) => {
    const orderA = (a.meta?.order as number | undefined) ?? 1000
    const orderB = (b.meta?.order as number | undefined) ?? 1000
    return orderA - orderB
  })
}

/**
 * 构建单个菜单项
 */
function buildMenuOption(route: RouteRecordRaw, parentPath: string): MenuOption {
  const fullPath = buildRouteFullPath(route.path, parentPath)
  const option = buildBaseMenuOption(route, fullPath)
  appendChildMenus(option, route, fullPath)
  return option
}

/**
 * 构建菜单基础项
 */
function buildBaseMenuOption(route: RouteRecordRaw, fullPath: string): MenuOptionWithRoute {
  return {
    key: fullPath,
    label: (route.meta?.title as string) || route.name?.toString() || route.path,
    route,
    icon: renderIcon(route.meta?.icon as string, (route.name as string) || route.path)
  }
}

/**
 * 为菜单项追加子节点
 */
function appendChildMenus(option: MenuOptionWithRoute, route: RouteRecordRaw, fullPath: string) {
  if (route.meta?.hideChildrenInMenu) {
    return
  }
  const visibleChildren = (route.children || []).filter((child) => !child.meta?.hideInMenu)
  const childMenus = generateMenuFromRoutes(visibleChildren, fullPath)
  if (childMenus.length > 0) {
    option.children = childMenus
  }
}

/**
 * 计算完整路由路径
 */
function buildRouteFullPath(path: string, parentPath: string): string {
  if (path.startsWith('/')) {
    return path
  }
  return `${parentPath}/${path}`.replace(/\/+/g, '/')
}

/**
 * TODO: 使用示例
 * 
 * ```typescript
 * import { usePermissionStore } from '@/stores'
 * import { generateMenuFromRoutes } from '@/utils/menu-generator'
 * 
 * const permissionStore = usePermissionStore()
 * const menuOptions = generateMenuFromRoutes(permissionStore.menuRoutes)
 * ```
 */
