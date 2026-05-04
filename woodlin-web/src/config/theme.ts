/**
 * @file theme.ts
 * @description Naive UI 主题覆盖配置
 * @author yulin
 * @since 2026-05-04
 */
import type { GlobalThemeOverrides } from 'naive-ui'

/** Naive UI 全局主题覆盖 */
export const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#1677ff',
    primaryColorHover: '#4096ff',
    primaryColorPressed: '#0958d9',
    primaryColorSuppl: '#1677ff',
    borderRadius: '4px',
  },
  Button: {
    fontWeight: '500',
  },
}
