/**
 * @file composables/useTable.ts
 * @description 通用分页表格 composable
 * @author yulin
 * @since 2026-05-04
 */
import { reactive, ref } from 'vue'
import type { PageResult } from '@/types/global'

export interface UseTableOptions {
  /** 初始每页条数 */
  pageSize?: number
  /** 是否在挂载/调用 useTable 后立即拉取 */
  immediate?: boolean
}

/**
 * 分页表格通用 composable
 * @param fetchFn 拉取函数：接收 { current, size, ...search } 返回 PageResult
 * @param options 配置
 */
export function useTable<T extends Record<string, unknown>, P extends Record<string, unknown> = Record<string, unknown>>(
  fetchFn: (params: P & { current: number; size: number }) => Promise<PageResult<T>>,
  options: UseTableOptions = {},
) {
  const data = ref<T[]>([]) as { value: T[] }
  const loading = ref(false)
  const total = ref(0)
  const pagination = reactive({ page: 1, pageSize: options.pageSize ?? 10 })
  const search = reactive({}) as P

  /** 拉取数据 */
  async function refresh(): Promise<void> {
    loading.value = true
    try {
      const res = await fetchFn({
        ...(search as P),
        current: pagination.page,
        size: pagination.pageSize,
      })
      data.value = res?.records ?? []
      total.value = res?.total ?? 0
    } finally {
      loading.value = false
    }
  }

  /** 提交搜索（重置到第一页） */
  function handleSearch(params?: Partial<P>): void {
    if (params) {Object.assign(search, params)}
    pagination.page = 1
    void refresh()
  }

  /** 重置搜索条件 */
  function reset(): void {
    Object.keys(search).forEach((k) => delete (search as Record<string, unknown>)[k])
    pagination.page = 1
    void refresh()
  }

  if (options.immediate !== false) {
    void refresh()
  }

  return { data, loading, total, pagination, search, refresh, handleSearch, reset }
}
