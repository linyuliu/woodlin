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
 * 获取用户状态字典（旧接口，已废弃）
 * @deprecated 请使用 getDictData('user_status')
 */
export function getUserStatusDict(): Promise<DictItem[]> {
  return getDictData('user_status')
}

/**
 * 获取演示用户对象
 * 展示字典枚举在对象中的序列化效果
 */
export function getDemoUser(): Promise<DemoUser> {
  return request.get('/dict/demo-user')
}


/**
 * 获取用户状态字典
 */
export function getUserStatusDict(): Promise<DictItem[]> {
  return request.get('/dict/user-status')
}

/**
 * 获取性别字典
 */
export function getGenderDict(): Promise<DictItem[]> {
  return request.get('/dict/gender')
}

/**
 * 获取演示用户对象
 * 展示字典枚举在对象中的序列化效果
 */
export function getDemoUser(): Promise<DemoUser> {
  return request.get('/dict/demo-user')
}
