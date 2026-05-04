/**
 * @file locales/index.ts
 * @description vue-i18n 初始化
 * @author yulin
 * @since 2026-05-04
 */
import type { App } from 'vue'
import { createI18n, type I18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import { settings } from '@/config/settings'

let i18n: I18n | null = null

/** 安装 i18n */
export function setupI18n(app: App): I18n {
  i18n = createI18n({
    legacy: false,
    locale: settings.defaultLocale,
    fallbackLocale: 'zh-CN',
    messages: {
      'zh-CN': zhCN,
      'en-US': enUS,
    },
  })
  app.use(i18n)
  return i18n
}

/** 获取实例 */
export function getI18n(): I18n {
  if (!i18n) throw new Error('i18n not initialized')
  return i18n
}
