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
import * as Icons from '@vicons/ionicons5'

/**
 * 内置的图标别名映射，兼容后端返回的简短/旧标识
 */
const FALLBACK_ICONS: Record<string, keyof typeof Icons> = {
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
  monitor: 'StatsChartOutline'
}

/**
 * 渲染图标
 * @param icon 图标组件或图标名称
 */
function renderIcon(icon?: Component | string, fallbackKey?: string) {
  const candidates: (string | Component | undefined)[] = []

  if (icon) {
    candidates.push(icon)
  }
  if (fallbackKey) {
    // 兼容路径/名称，例如 system/user -> user
    const parts = fallbackKey.split('/').filter(Boolean)
    candidates.push(parts[parts.length - 1])
    candidates.push(parts[0])
  }

  for (const candidate of candidates) {
    if (!candidate) {
      continue
    }

    if (typeof candidate === 'string') {
      const raw = candidate.trim()
      if (!raw) continue

      // 1) 直接尝试后端返回的组件名（可能已是 PascalCase）
      const direct = Icons[raw as keyof typeof Icons]
      if (direct) {
        return () => h(NIcon, null, { default: () => h(direct) })
      }

      // 2) 尝试 kebab/camel 转 PascalCase
      const iconName = toPascalCase(raw)
      const IconComponent = Icons[iconName as keyof typeof Icons] as Component | undefined
      if (IconComponent) {
        return () => h(NIcon, null, { default: () => h(IconComponent) })
      }

      // 3) 尝试内置别名映射（全部转小写匹配）
      const alias = FALLBACK_ICONS[raw.toLowerCase()] || FALLBACK_ICONS[iconName.toLowerCase()]
      if (alias && Icons[alias]) {
        const AliasComponent = Icons[alias] as Component
        return () => h(NIcon, null, { default: () => h(AliasComponent) })
      }
    } else {
      return () => h(NIcon, null, { default: () => h(candidate) })
    }
  }

  return undefined
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
  const menuOptions: MenuOption[] = []

  // 根据 meta.order 排序，默认 1000 保持稳定
  const sortedRoutes = [...routes].sort((a, b) => {
    const orderA = (a.meta?.order as number | undefined) ?? 1000
    const orderB = (b.meta?.order as number | undefined) ?? 1000
    return orderA - orderB
  })

  sortedRoutes.forEach((route) => {
    if (route.meta?.hideInMenu) {
      return
    }

    const fullPath = route.path.startsWith('/')
      ? route.path
      : `${parentPath}/${route.path}`.replace(/\/+/g, '/')

    const option: MenuOption = {
      key: fullPath,
      label: (route.meta?.title as string) || route.name?.toString() || route.path,
    }
    ;(option as any).route = route

    // 图标兼容：优先 meta.icon；否则用路由名称/路径的别名映射
    const fallbackKey = (route.name as string) || route.path
    option.icon = renderIcon(route.meta?.icon as string, fallbackKey)

    // hideChildrenInMenu: 只显示当前节点，不下钻
    const visibleChildren = (route.children || []).filter(
      child => !child.meta?.hideInMenu
    )
    if (!route.meta?.hideChildrenInMenu && visibleChildren.length > 0) {
      const childMenus = generateMenuFromRoutes(visibleChildren, fullPath)
      if (childMenus.length > 0) {
        option.children = childMenus
      }
    }

    menuOptions.push(option)
  })

  return menuOptions
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
