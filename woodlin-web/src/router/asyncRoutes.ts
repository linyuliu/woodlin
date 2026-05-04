/**
 * @file router/asyncRoutes.ts
 * @description 后端菜单 → Vue Router 路由记录的转换器，使用 import.meta.glob 动态收集 view 组件
 * @author yulin
 * @since 2026-01-01
 */
import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteItem } from '@/types/global'
import { MenuType } from '@/constants'

/** 视图组件加载器签名 */
type ViewLoader = () => Promise<Component>

/**
 * 收集 src/views 下所有 *.vue 视图。
 * 键格式：'/src/views/system/user/index.vue'
 */
const modules = import.meta.glob('/src/views/**/*.vue') as Record<string, ViewLoader>

/** Layout 与 ParentView 通过静态导入，确保始终可用 */
const Layout: ViewLoader = () => import('@/layouts/DefaultLayout/index.vue')
const ParentView: ViewLoader = () => import('@/components/ParentView/index.vue')
const NotFound: ViewLoader = () => import('@/views/error/404.vue')

/**
 * 解析 component 字段：
 * - 'Layout' → DefaultLayout
 * - 'ParentView' → 嵌套路由占位
 * - 'system/user/index' → /src/views/system/user/index.vue
 * @param component 后端字符串
 */
function resolveComponent(component: string | undefined): ViewLoader {
  if (!component || component === 'Layout') {return Layout}
  if (component === 'ParentView') {return ParentView}
  const key = `/src/views/${component.replace(/^\/+/, '')}.vue`
  const loader = modules[key]
  if (!loader) {
    console.warn(`[asyncRoutes] Component not found: ${key}, fallback to 404`)
    return NotFound
  }
  return loader
}

/** 单节点转换；按钮节点返回 null 以便上层过滤 */
function toRecord(item: RouteItem): RouteRecordRaw | null {
  if (item.type === MenuType.BUTTON) {return null}
  const record: RouteRecordRaw = {
    path: item.path,
    name: item.name,
    component: resolveComponent(item.component),
    redirect: item.redirect,
    meta: {
      title: item.title,
      icon: item.icon,
      hidden: item.isHidden,
      keepAlive: item.isCache,
      isFrame: item.isFrame,
      showInTabs: item.showInTabs ?? true,
      activeMenu: item.activeMenu,
      permission: item.permission,
      sort: item.sort,
      breadcrumb: true,
    },
  } as RouteRecordRaw
  if (item.children && item.children.length) {
    record.children = item.children
      .map(toRecord)
      .filter((c): c is RouteRecordRaw => c !== null)
  }
  return record
}

/**
 * 构建动态路由列表
 * @param items 后端菜单列表
 * @returns Vue Router 路由记录数组
 */
export function buildAsyncRoutes(items: RouteItem[]): RouteRecordRaw[] {
  return items.map(toRecord).filter((c): c is RouteRecordRaw => c !== null)
}
