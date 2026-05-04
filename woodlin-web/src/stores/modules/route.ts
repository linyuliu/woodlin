/**
 * @file stores/modules/route.ts
 * @description 动态路由仓库
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteItem } from '@/types/global'
import { buildAsyncRoutes } from '@/router/asyncRoutes'

interface RouteState {
  routes: RouteItem[]
  asyncRoutes: RouteRecordRaw[]
  loaded: boolean
}

export const useRouteStore = defineStore('route', {
  state: (): RouteState => ({
    routes: [],
    asyncRoutes: [],
    loaded: false,
  }),
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
