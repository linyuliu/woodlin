/**
 * @file stores/index.ts
 * @description Pinia 实例创建与持久化插件注册
 * @author yulin
 * @since 2026-05-04
 */
import type { App } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

/** 安装 Pinia 到应用 */
export function setupStore(app: App): void {
  const pinia = createPinia()
  pinia.use(piniaPluginPersistedstate)
  app.use(pinia)
}
