/**
 * @file router/asyncRoutes.ts
 * @description 后端菜单 → Vue Router 路由记录的转换器
 * @author yulin
 * @since 2026-05-04
 */
import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteItem } from '@/types/global'
import { MenuType } from '@/constants'
import ParentView from '@/components/ParentView/index.vue'

/** 通过 import.meta.glob 收集所有 view */
const modules = import.meta.glob('@/views/**/*.vue')

/** 占位 Layout：项目实际 Layout 在后续 Commit 中提供 */
const Layout: Component = ParentView

/**
 * 解析 component 字段对应的组件
 * @param component 后端字符串
 */
function resolveComponent(component: string): Component | (() => Promise<Component>) {
  if (!component || component === 'Layout') return Layout
  if (component === 'ParentView') return ParentView
  const key = `/src/views/${component.replace(/^\/+/, '')}.vue`
  const loader = modules[key]
  if (loader) {
    return loader as () => Promise<Component>
  }
  // 兜底：返回 ParentView，避免在菜单尚未对齐时构建失败
  return ParentView
}

/** 单个节点转换 */
function toRecord(item: RouteItem): RouteRecordRaw | null {
  if (item.type === MenuType.BUTTON) return null
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
      permission: item.permission,
      activeMenu: item.activeMenu,
      showInTabs: item.showInTabs ?? true,
      sort: item.sort,
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
 * 构建动态路由
 * @param items 后端菜单
 */
export function buildAsyncRoutes(items: RouteItem[]): RouteRecordRaw[] {
  return items.map(toRecord).filter((c): c is RouteRecordRaw => c !== null)
}
