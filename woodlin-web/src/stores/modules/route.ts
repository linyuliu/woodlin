/**
 * @file stores/modules/route.ts
 * @description 动态路由仓库：保存后端原始菜单列表、构建 Vue Router 路由记录并注入路由实例
 * @author yulin
 * @since 2026-01-01
 */
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteItem } from '@/types/global'
import { MenuType } from '@/constants'
import { buildAsyncRoutes } from '@/router/asyncRoutes'
import { getRouter, LAYOUT_ROUTE_NAME } from '@/router'

interface RouteState {
  /** 后端返回的原始菜单列表（含按钮，按需用于面包屑/权限查询） */
  routeList: RouteItem[]
  /** 已注册到 Router 的动态路由记录（顶层） */
  asyncRoutes: RouteRecordRaw[]
  /** 是否已加载并注入 */
  isRoutesLoaded: boolean
}

/** 递归过滤掉按钮以及隐藏菜单，仅保留侧边栏可见项 */
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
    routeList: [],
    asyncRoutes: [],
    isRoutesLoaded: false,
  }),
  getters: {
    /** 用于渲染侧边栏的菜单项（已剔除按钮 / 隐藏） */
    menuItems(state): RouteItem[] {
      return filterMenuItems(state.routeList)
    },
  },
  actions: {
    /**
     * 根据后端返回的菜单生成 Vue Router 路由并挂载到 Layout 节点下
     * @param items 后端菜单
     */
    generateRoutes(items: RouteItem[]): RouteRecordRaw[] {
      this.routeList = items
      const records = buildAsyncRoutes(items)
      const router = getRouter()
      records.forEach((r) => {
        if (r.name && router.hasRoute(r.name)) {
          router.removeRoute(r.name)
        }
        router.addRoute(LAYOUT_ROUTE_NAME, r)
      })
      this.asyncRoutes = records
      this.isRoutesLoaded = true
      return records
    },
    /** 清空动态路由（登出/切换租户时调用） */
    resetRoutes(): void {
      const router = getRouter()
      this.asyncRoutes.forEach((r) => {
        if (r.name && router.hasRoute(r.name)) {
          router.removeRoute(r.name)
        }
      })
      this.routeList = []
      this.asyncRoutes = []
      this.isRoutesLoaded = false
    },
  },
})
