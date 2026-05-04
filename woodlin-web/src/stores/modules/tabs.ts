/**
 * @file stores/modules/tabs.ts
 * @description 多标签页导航状态
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabItem {
  path: string
  fullPath: string
  name: string
  title: string
  affix?: boolean
  keepAlive?: boolean
}

interface TabsState {
  tabs: TabItem[]
  active: string
}

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    tabs: [],
    active: '',
  }),
  actions: {
    /** 添加一个标签 */
    addTab(route: RouteLocationNormalized): void {
      const path = route.fullPath
      this.active = path
      if (this.tabs.find((t) => t.fullPath === path)) return
      this.tabs.push({
        path: route.path,
        fullPath: path,
        name: String(route.name ?? ''),
        title: (route.meta?.title as string) ?? String(route.name ?? ''),
        keepAlive: Boolean(route.meta?.keepAlive),
      })
    },
    /** 移除标签 */
    removeTab(path: string): void {
      this.tabs = this.tabs.filter((t) => t.fullPath !== path)
    },
    /** 移除其他 */
    removeOthers(path: string): void {
      this.tabs = this.tabs.filter((t) => t.fullPath === path || t.affix)
    },
    /** 全部关闭（保留固定标签） */
    removeAll(): void {
      this.tabs = this.tabs.filter((t) => t.affix)
    },
  },
})
