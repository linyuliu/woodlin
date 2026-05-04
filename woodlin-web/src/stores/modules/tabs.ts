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
  icon?: string
  affix?: boolean
  keepAlive?: boolean
  showInTabs?: boolean
}

interface TabsState {
  tabs: TabItem[]
  active: string
}

const AFFIX_TAB: TabItem = {
  path: '/dashboard/workplace',
  fullPath: '/dashboard/workplace',
  name: 'Workplace',
  title: '工作台',
  icon: 'vicons:antd:DashboardOutlined',
  affix: true,
  keepAlive: true,
}

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    tabs: [AFFIX_TAB],
    active: AFFIX_TAB.fullPath,
  }),
  getters: {
    /** 当前激活标签 fullPath */
    activeTab: (state) => state.active,
  },
  actions: {
    /** 添加一个标签 */
    addTab(route: RouteLocationNormalized): void {
      const path = route.fullPath
      this.active = path
      if ((route.meta as Record<string, unknown>)?.showInTabs === false) return
      if (this.tabs.find((t) => t.fullPath === path)) return
      this.tabs.push({
        path: route.path,
        fullPath: path,
        name: String(route.name ?? ''),
        title: (route.meta?.title as string) ?? String(route.name ?? ''),
        icon: route.meta?.icon as string | undefined,
        keepAlive: Boolean(route.meta?.keepAlive),
        showInTabs: true,
      })
    },
    /** 移除标签（按 fullPath 或 name） */
    removeTab(key: string): void {
      this.tabs = this.tabs.filter((t) => !(t.fullPath === key || t.name === key) || t.affix)
    },
    /** 关闭其他（保留固定标签 + 指定标签） */
    removeOtherTabs(key: string): void {
      this.tabs = this.tabs.filter((t) => t.fullPath === key || t.name === key || t.affix)
    },
    /** 关闭其他（兼容旧 API） */
    removeOthers(key: string): void {
      this.removeOtherTabs(key)
    },
    /** 关闭全部（保留固定标签） */
    removeAllTabs(): void {
      this.tabs = this.tabs.filter((t) => t.affix)
    },
    /** 关闭全部（兼容旧 API） */
    removeAll(): void {
      this.removeAllTabs()
    },
    /** 设置当前激活标签 */
    setActive(key: string): void {
      this.active = key
    },
  },
  persist: false,
})
