/**
 * @file composables/useTabs.ts
 * @description 多标签页操作 composable
 * @author yulin
 * @since 2026-05-04
 */
import { useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/modules/tabs'

/** 多标签操作 hook */
export function useTabs() {
  const router = useRouter()
  const store = useTabsStore()

  /** 关闭指定 tab，并跳转到剩余最后一个 */
  function closeTab(path: string): void {
    store.removeTab(path)
    if (store.active === path) {
      const next = store.tabs[store.tabs.length - 1]
      if (next) {
        store.active = next.fullPath
        void router.push(next.fullPath)
      }
    }
  }

  return { tabs: store.tabs, active: store.active, closeTab, store }
}
