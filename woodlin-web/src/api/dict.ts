/**
 * 字典管理API服务
 * 
 * @author mumu
 * @description 字典数据管理相关的API接口调用（重构版）
 *              采用"先查类型，再查数据"的设计模式
 *              支持客户端缓存以减少重复请求
 * @since 2025-12-27
 */

import request from '@/utils/request'
import dictCache, { CACHE_KEY, TTL, getCachedData } from '@/utils/dictCache'

/**
 * 字典项
 */
export interface DictItem {
  label: string
  value: string | number
  desc?: string
  sort?: number
  cssClass?: string
  listClass?: string
  isDefault?: string
  extra?: string
  [key: string]: unknown
}

/**
 * 字典类型
 */
export interface DictType {
  dictType: string
  dictName: string
  dictCategory: string
}

/**
 * 管理端字典类型实体
 */
export interface DictTypeRecord extends DictType {
  dictId?: number
  status?: string
  remark?: string
  tenantId?: string
  createTime?: string
  updateTime?: string
}

/**
 * 区划节点
 */
export interface RegionNode {
  code: string
  name: string
  parentCode?: string
  level: number
  type: string
  shortName?: string
  pinyin?: string
  pinyinAbbr?: string
  longitude?: number
  latitude?: number
  isMunicipality: boolean
  children?: RegionNode[]
}

/**
 * 管理端字典数据实体
 */
export interface DictDataRecord {
  dataId?: number
  dictType: string
  dictLabel: string
  dictValue: string
  dictDesc?: string
  dictSort?: number
  status?: string
  isDefault?: string
  cssClass?: string
  listClass?: string
  extraData?: string
  tenantId?: string
  createTime?: string
  updateTime?: string
  deleted?: string
}

// ==================== 新版动态字典API ====================

/**
 * 查询所有字典类型
 * @param useCache 是否使用缓存（默认true）
 * @returns 字典类型列表
 */
export function getDictTypes(useCache: boolean = true): Promise<DictType[]> {
  if (!useCache) {
    dictCache.delete(CACHE_KEY.DICT_TYPES)
  }
  
  return getCachedData(
    CACHE_KEY.DICT_TYPES,
    () => request.get('/common/dict/types'),
    TTL.LONG // 字典类型变化不频繁，使用较长缓存
  )
}

/**
 * 根据字典类型查询字典数据
 * @param dictType 字典类型
 * @param useCache 是否使用缓存（默认true）
 * @returns 字典数据列表
 */
export function getDictData(dictType: string, useCache: boolean = true): Promise<DictItem[]> {
  const cacheKey = CACHE_KEY.DICT_DATA(dictType)
  
  if (!useCache) {
    dictCache.delete(cacheKey)
  }
  
  return getCachedData(
    cacheKey,
    () => request.get(`/common/dict/data/${dictType}`),
    TTL.MEDIUM // 字典数据使用中等缓存
  )
}

/**
 * 查询行政区划树
 * @param useCache 是否使用缓存（默认true）
 * @returns 行政区划树
 */
export function getRegionTree(useCache: boolean = true): Promise<RegionNode[]> {
  if (!useCache) {
    dictCache.delete(CACHE_KEY.REGION_TREE)
  }
  
  return getCachedData(
    CACHE_KEY.REGION_TREE,
    () => request.get('/common/dict/region/tree'),
    TTL.LONG // 行政区划变化不频繁，使用较长缓存
  )
}

/**
 * 根据父代码查询子区划
 * @param parentCode 父区划代码（为空则查询省级）
 * @param useCache 是否使用缓存（默认true）
 * @returns 子区划列表
 */
export function getRegionChildren(parentCode?: string, useCache: boolean = true): Promise<RegionNode[]> {
  const cacheKey = CACHE_KEY.REGION_CHILDREN(parentCode || '')
  
  if (!useCache) {
    dictCache.delete(cacheKey)
  }
  
  return getCachedData(
    cacheKey,
    () => request.get('/common/dict/region/children', { params: { parentCode } }),
    TTL.LONG
  )
}

// ==================== 管理端 CRUD 接口 ====================

/**
 * 管理端：查询字典类型列表
 */
export function listDictTypesAdmin(params?: Partial<DictTypeRecord>) {
  return request.get<DictTypeRecord[], DictTypeRecord[]>('/system/dict/types', { params })
}

/**
 * 管理端：新增字典类型
 */
export function createDictTypeAdmin(data: DictTypeRecord) {
  return request.post<DictTypeRecord, DictTypeRecord>('/system/dict/types', data)
}

/**
 * 管理端：更新字典类型
 */
export function updateDictTypeAdmin(data: DictTypeRecord) {
  if (!data.dictId) {
    throw new Error('dictId is required for updateDictTypeAdmin')
  }
  return request.put<DictTypeRecord, DictTypeRecord>(`/system/dict/types/${data.dictId}`, data)
}

/**
 * 管理端：删除字典类型
 */
export function deleteDictTypeAdmin(dictId: number) {
  return request.delete<void, void>(`/system/dict/types/${dictId}`)
}

/**
 * 管理端：查询字典项列表
 */
export function listDictDataAdmin(dictType: string) {
  return request.get<DictDataRecord[], DictDataRecord[]>('/system/dict/data', {
    params: { dictType }
  })
}

/**
 * 管理端：新增字典项
 */
export function createDictDataAdmin(data: DictDataRecord) {
  return request.post<DictDataRecord, DictDataRecord>('/system/dict/data', data)
}

/**
 * 管理端：更新字典项
 */
export function updateDictDataAdmin(data: DictDataRecord) {
  if (!data.dataId) {
    throw new Error('dataId is required for updateDictDataAdmin')
  }
  return request.put<DictDataRecord, DictDataRecord>(`/system/dict/data/${data.dataId}`, data)
}

/**
 * 管理端：删除字典项
 */
export function deleteDictDataAdmin(dataId: number) {
  return request.delete<void, void>(`/system/dict/data/${dataId}`)
}

// ==================== 便捷方法 ====================

/**
 * 获取性别字典
 */
export function getGenderDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('gender', useCache)
}

/**
 * 获取民族字典
 */
export function getEthnicityDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('ethnicity', useCache)
}

/**
 * 获取学历字典
 */
export function getEducationDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('education', useCache)
}

/**
 * 获取婚姻状况字典
 */
export function getMaritalDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('marital', useCache)
}

/**
 * 获取政治面貌字典
 */
export function getPoliticalDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('political', useCache)
}

/**
 * 获取证件类型字典
 */
export function getIdTypeDict(useCache: boolean = true): Promise<DictItem[]> {
  return getDictData('idtype', useCache)
}

/**
 * 获取省级行政区划
 */
export function getProvinces(useCache: boolean = true): Promise<RegionNode[]> {
  return getRegionChildren(undefined, useCache)
}

/**
 * 获取市级行政区划
 */
export function getCities(provinceCode: string, useCache: boolean = true): Promise<RegionNode[]> {
  return getRegionChildren(provinceCode, useCache)
}

/**
 * 获取区县级行政区划
 */
export function getDistricts(cityCode: string, useCache: boolean = true): Promise<RegionNode[]> {
  return getRegionChildren(cityCode, useCache)
}

/**
 * 批量预加载常用字典（用于应用初始化）
 * @returns Promise
 */
export async function preloadCommonDicts(): Promise<void> {
  const commonTypes = ['gender', 'ethnicity', 'education', 'marital', 'political', 'idtype', 'user_status']
  
  // 并行预加载
  await Promise.all([
    ...commonTypes.map(type => getDictData(type, true)),
    getProvinces(true)
  ])
}

/**
 * 清空所有字典缓存
 */
export function clearDictCache(): void {
  dictCache.clear()
}

// ==================== 兼容性接口（已废弃，保留用于演示） ====================

/**
 * 演示用户对象
 */
export interface DemoUser {
  id: number
  name: string
  gender: {
    label: string
    value: string
  }
  status: {
    label: string
    value: string
  }
}

/**
 * Get user status dictionary (deprecated)
 * @deprecated Use getDictData('user_status') instead
 */
export function getUserStatusDict(): Promise<DictItem[]> {
  return getDictData('user_status')
}

/**
 * Get demo user object
 * Demonstrates dictionary enum serialization in objects
 */
export function getDemoUser(): Promise<DemoUser> {
  return request.get('/dict/demo-user')
}
