/**
 * @file stores/modules/route.ts
 * @description 动态路由仓库
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteItem } from '@/types/global'
import { MenuType } from '@/constants'
import { buildAsyncRoutes } from '@/router/asyncRoutes'

interface RouteState {
  routes: RouteItem[]
  asyncRoutes: RouteRecordRaw[]
  loaded: boolean
}

/** 递归过滤掉按钮类型节点，仅保留目录与菜单 */
function filterMenuItems(items: RouteItem[]): RouteItem[] {
  return items
    .filter((i) => i.type !== MenuType.BUTTON && !i.isHidden)
    .map((i) => ({
      ...i,
      children: i.children ? filterMenuItems(i.children) : undefined,
    }))
}

export const useRouteStore = defineStore('route', {
  state: (): RouteState => ({
    routes: [],
    asyncRoutes: [],
    loaded: false,
  }),
  getters: {
    /** 用于渲染侧边栏的菜单项（已剔除按钮 / 隐藏） */
    menuItems(state): RouteItem[] {
      return filterMenuItems(state.routes)
    },
  },
  actions: {
    /** 根据后端返回的路由项生成 Vue Router 路由 */
    generateRoutes(items: RouteItem[]): RouteRecordRaw[] {
      this.routes = items
      this.asyncRoutes = buildAsyncRoutes(items)
      this.loaded = true
      return this.asyncRoutes
    },
    /** 重置 */
    resetRoutes(): void {
      this.routes = []
      this.asyncRoutes = []
      this.loaded = false
    },
  },
})
