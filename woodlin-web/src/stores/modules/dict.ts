/**
 * @file stores/modules/dict.ts
 * @description 字典缓存仓库
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import type { DictItem } from '@/types/global'
import { getDictItems } from '@/api/system/dict'

interface DictState {
  cache: Record<string, DictItem[]>
}

export const useDictStore = defineStore('dict', {
  state: (): DictState => ({
    cache: {},
  }),
  actions: {
    /** 加载字典（带缓存） */
    async loadDict(code: string, force = false): Promise<DictItem[]> {
      if (!force && this.cache[code]) {return this.cache[code]}
      const items = await getDictItems(code)
      this.cache[code] = items ?? []
      return this.cache[code]
    },
    /** 清空缓存 */
    clear(): void {
      this.cache = {}
    },
  },
})
