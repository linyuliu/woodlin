/**
 * @file settings.ts
 * @description 应用全局配置（标题、Logo、默认主题、缓存键等）
 * @author yulin
 * @since 2026-05-04
 */

export interface AppSettings {
  /** 应用标题 */
  title: string
  /** Logo 路径 */
  logo: string
  /** 默认主题 'light' | 'dark' */
  defaultTheme: 'light' | 'dark'
  /** 默认语言 */
  defaultLocale: 'zh-CN' | 'en-US'
  /** Token 存储键 */
  tokenKey: string
  /** 是否启用多标签 */
  enableTabs: boolean
  /** 默认首页路径 */
  homePath: string
}

/** 默认配置 */
export const settings: AppSettings = {
  title: 'Woodlin 管理系统',
  logo: '/src/assets/logo.svg',
  defaultTheme: 'light',
  defaultLocale: 'zh-CN',
  tokenKey: 'woodlin_token',
  enableTabs: true,
  homePath: '/dashboard/workplace',
}
