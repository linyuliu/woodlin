/**
 * @file api/system/dict.ts
 * @description 字典管理 API（字典类型 + 字典项）
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { DictItem, PageResult } from '@/types/global'

/** 字典类型 */
export interface SysDictType {
  id?: number
  dictName: string
  dictType: string
  status?: string
  remark?: string
}

/** 字典项 */
export interface SysDictData {
  id?: number
  dictType: string
  dictLabel: string
  dictValue: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  sort?: number
  status?: string
  remark?: string
}

/** 字典类型查询参数 */
export interface DictTypeQuery {
  page?: number
  size?: number
  dictName?: string
  dictType?: string
}

/** 字典项查询参数 */
export interface DictDataQuery {
  page?: number
  size?: number
  dictType?: string
  dictLabel?: string
}

/** 分页查询字典类型 */
export function pageDictTypes(params: DictTypeQuery): Promise<PageResult<SysDictType>> {
  return get('/system/dict/types', params as Record<string, unknown>)
}

/** 新增字典类型 */
export function createDictType(data: SysDictType): Promise<void> {
  return post('/system/dict/types', data)
}

/** 更新字典类型 */
export function updateDictType(id: number, data: SysDictType): Promise<void> {
  return put(`/system/dict/types/${id}`, data)
}

/** 删除字典类型 */
export function deleteDictType(id: number): Promise<void> {
  return del(`/system/dict/types/${id}`)
}

/** 分页查询字典项 */
export function pageDictData(params: DictDataQuery): Promise<PageResult<SysDictData>> {
  return get('/system/dict/data', params as Record<string, unknown>)
}

/** 根据字典类型 code 获取字典项（用于下拉等） */
export function getDictItems(dictType: string): Promise<DictItem[]> {
  return get('/system/dict/data', { dictType })
}

/** 新增字典项 */
export function createDictData(data: SysDictData): Promise<void> {
  return post('/system/dict/data', data)
}

/** 更新字典项 */
export function updateDictData(id: number, data: SysDictData): Promise<void> {
  return put(`/system/dict/data/${id}`, data)
}

/** 删除字典项 */
export function deleteDictData(id: number): Promise<void> {
  return del(`/system/dict/data/${id}`)
}
