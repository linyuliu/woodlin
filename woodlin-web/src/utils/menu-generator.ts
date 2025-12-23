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
 * 渲染图标
 * @param icon 图标组件或图标名称
 */
function renderIcon(icon: Component | string) {
  if (typeof icon === 'string') {
    // 根据图标名称动态加载图标
    const IconComponent = (Icons as any)[toPascalCase(icon)]
    if (IconComponent) {
      return () => h(NIcon, null, { default: () => h(IconComponent) })
    }
    return undefined
  }
  return () => h(NIcon, null, { default: () => h(icon) })
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
  parentPath: string = ''
): MenuOption[] {
  const menuOptions: MenuOption[] = []

  routes.forEach((route) => {
    // 跳过隐藏的菜单项
    if (route.meta?.hideInMenu) {
      return
    }

    const fullPath = route.path.startsWith('/')
      ? route.path
      : `${parentPath}/${route.path}`.replace(/\/+/g, '/')

    const option: MenuOption = {
      key: fullPath,
      label: (route.meta?.title as string) || route.name?.toString() || route.path
    }

    // 添加图标
    if (route.meta?.icon) {
      option.icon = renderIcon(route.meta.icon as string)
    }

    // 递归处理子路由
    if (route.children && route.children.length > 0) {
      const childMenus = generateMenuFromRoutes(route.children, fullPath)
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
