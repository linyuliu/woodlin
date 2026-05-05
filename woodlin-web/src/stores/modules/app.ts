/**
 * @file stores/modules/app.ts
 * @description 应用全局状态：主题、语言、侧边栏折叠
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import { settings } from '@/config/settings'

export type AppLocale = 'zh-CN' | 'en-US'
export type AppTheme = 'light' | 'dark'
export type LayoutMode = 'default' | 'mix' | 'top'

interface AppState {
  theme: AppTheme
  locale: AppLocale
  collapsed: boolean
  layoutMode: LayoutMode
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    theme: settings.defaultTheme,
    locale: settings.defaultLocale,
    collapsed: false,
    layoutMode: settings.defaultLayoutMode,
  }),
  getters: {
    /** 是否处于深色模式 */
    isDark: (state) => state.theme === 'dark',
    /** 侧边栏是否折叠（语义别名） */
    sidebarCollapsed: (state) => state.collapsed,
    /** 当前布局模式 */
    currentLayoutMode: (state) => state.layoutMode,
  },
  actions: {
    /** 切换深色/浅色主题 */
    toggleTheme(): void {
      this.theme = this.theme === 'dark' ? 'light' : 'dark'
    },
    /** 切换深色/浅色（语义别名） */
    toggleDark(): void {
      this.toggleTheme()
    },
    /** 设置语言 */
    setLocale(locale: AppLocale): void {
      this.locale = locale
    },
    /** 设置布局模式 */
    setLayoutMode(layoutMode: LayoutMode): void {
      this.layoutMode = layoutMode
    },
    /** 切换侧边栏折叠 */
    toggleCollapsed(): void {
      this.collapsed = !this.collapsed
    },
    /** 切换侧边栏折叠（语义别名） */
    toggleSidebar(): void {
      this.toggleCollapsed()
    },
  },
  persist: true,
})
