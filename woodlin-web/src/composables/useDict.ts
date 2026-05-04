/**
 * @file composables/useDict.ts
 * @description 字典加载 composable
 * @author yulin
 * @since 2026-05-04
 */
import { computed, onMounted } from 'vue'
import type { DictItem } from '@/types/global'
import { useDictStore } from '@/stores/modules/dict'

/**
 * 加载并获取指定字典
 * @param code 字典 code
 */
export function useDict(code: string) {
  const store = useDictStore()
  onMounted(() => {
    void store.loadDict(code)
  })
  const items = computed<DictItem[]>(() => store.cache[code] ?? [])
  /** 通过 value 获取 label */
  function labelOf(value: string | number | null | undefined): string {
    const it = items.value.find((i) => String(i.value) === String(value))
    return it?.label ?? String(value ?? '')
  }
  return { items, labelOf }
}
