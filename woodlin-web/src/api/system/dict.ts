/**
 * @file api/system/dict.ts
 * @description 字典管理 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { DictItem, PageResult } from '@/types/global'

export interface SysDictType {
  id?: number
  name: string
  code: string
  remark?: string
}

export function pageDictTypes(params: Record<string, unknown>): Promise<PageResult<SysDictType>> {
  return get('/system/dict/type/page', params)
}

export function listDictTypes(): Promise<SysDictType[]> {
  return get('/system/dict/type/list')
}

export function createDictType(data: SysDictType): Promise<void> {
  return post('/system/dict/type', data)
}

export function updateDictType(data: SysDictType): Promise<void> {
  return put('/system/dict/type', data)
}

export function deleteDictType(id: number): Promise<void> {
  return del(`/system/dict/type/${id}`)
}

/** 根据字典类型 code 获取字典项 */
export function getDictItems(code: string): Promise<DictItem[]> {
  return get(`/system/dict/data/${code}`)
}
