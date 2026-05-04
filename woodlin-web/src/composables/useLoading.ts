/**
 * @file composables/useLoading.ts
 * @description 简单 loading 状态 composable
 * @author yulin
 * @since 2026-05-04
 */
import { ref } from 'vue'

/** 创建一个 loading 状态及包装函数 */
export function useLoading(initial = false) {
  const loading = ref(initial)
  /** 包装异步函数自动设置 loading */
  async function withLoading<T>(fn: () => Promise<T>): Promise<T> {
    loading.value = true
    try {
      return await fn()
    } finally {
      loading.value = false
    }
  }
  return { loading, withLoading }
}
