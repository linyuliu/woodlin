/**
 * @file directives/index.ts
 * @description 注册全局指令
 * @author yulin
 * @since 2026-05-04
 */
import type { App } from 'vue'
import { permission } from './permission'
import { copy } from './copy'

/** 安装全部指令 */
export function setupDirectives(app: App): void {
  app.directive('permission', permission)
  app.directive('copy', copy)
}
