/**
 * @file stores/modules/app.ts
 * @description 应用全局状态：主题、语言、侧边栏折叠
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import { settings } from '@/config/settings'

interface AppState {
  theme: 'light' | 'dark'
  locale: 'zh-CN' | 'en-US'
  collapsed: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    theme: settings.defaultTheme,
    locale: settings.defaultLocale,
    collapsed: false,
  }),
  getters: {
    isDark: (state) => state.theme === 'dark',
  },
  actions: {
    /** 切换深色/浅色主题 */
    toggleTheme(): void {
      this.theme = this.theme === 'dark' ? 'light' : 'dark'
    },
    /** 设置语言 */
    setLocale(locale: 'zh-CN' | 'en-US'): void {
      this.locale = locale
    },
    /** 切换侧边栏折叠 */
    toggleCollapsed(): void {
      this.collapsed = !this.collapsed
    },
  },
  persist: true,
})
