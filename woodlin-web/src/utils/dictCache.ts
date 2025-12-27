/**
 * 字典缓存工具
 * 
 * @author mumu
 * @description 提供字典数据的客户端缓存功能，支持TTL（过期时间）
 * @since 2025-12-27
 */

import type { DictItem } from './dict'

/**
 * 缓存项接口
 */
interface CacheItem<T> {
  data: T
  timestamp: number
  ttl: number
}

/**
 * 字典缓存类
 */
class DictCache {
  private cache: Map<string, CacheItem<any>>
  private defaultTTL: number // 默认缓存时间（毫秒）

  constructor(defaultTTL: number = 5 * 60 * 1000) {
    this.cache = new Map()
    this.defaultTTL = defaultTTL // 默认5分钟
  }

  /**
   * 设置缓存
   * @param key 缓存键
   * @param data 缓存数据
   * @param ttl 过期时间（毫秒），不传则使用默认值
   */
  set<T>(key: string, data: T, ttl?: number): void {
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl: ttl || this.defaultTTL
    }
    this.cache.set(key, cacheItem)
  }

  /**
   * 获取缓存
   * @param key 缓存键
   * @returns 缓存数据，如果不存在或已过期则返回null
   */
  get<T>(key: string): T | null {
    const item = this.cache.get(key)
    
    if (!item) {
      return null
    }

    const now = Date.now()
    const isExpired = now - item.timestamp > item.ttl

    if (isExpired) {
      this.cache.delete(key)
      return null
    }

    return item.data as T
  }

  /**
   * 检查缓存是否存在且有效
   * @param key 缓存键
   * @returns 是否存在有效缓存
   */
  has(key: string): boolean {
    return this.get(key) !== null
  }

  /**
   * 删除缓存
   * @param key 缓存键
   */
  delete(key: string): void {
    this.cache.delete(key)
  }

  /**
   * 清空所有缓存
   */
  clear(): void {
    this.cache.clear()
  }

  /**
   * 清理过期缓存
   */
  cleanup(): void {
    const now = Date.now()
    for (const [key, item] of this.cache.entries()) {
      if (now - item.timestamp > item.ttl) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 获取缓存统计信息
   */
  stats(): { total: number; expired: number } {
    const now = Date.now()
    let expired = 0
    
    for (const item of this.cache.values()) {
      if (now - item.timestamp > item.ttl) {
        expired++
      }
    }

    return {
      total: this.cache.size,
      expired
    }
  }
}

// 创建全局字典缓存实例
const dictCache = new DictCache()

// 定期清理过期缓存（每分钟执行一次）
setInterval(() => {
  dictCache.cleanup()
}, 60 * 1000)

export default dictCache

/**
 * 字典类型缓存键前缀
 */
export const CACHE_KEY = {
  DICT_TYPES: 'dict:types',
  DICT_DATA: (type: string) => `dict:data:${type}`,
  REGION_TREE: 'region:tree',
  REGION_CHILDREN: (parentCode: string) => `region:children:${parentCode || 'root'}`
}

/**
 * 缓存时间常量（毫秒）
 */
export const TTL = {
  SHORT: 1 * 60 * 1000,      // 1分钟
  MEDIUM: 5 * 60 * 1000,     // 5分钟（默认）
  LONG: 30 * 60 * 1000,      // 30分钟
  PAGE: 0                     // 页面级别（直到页面刷新）
}

/**
 * 设置页面级别缓存（直到页面刷新）
 * 通过设置超长的TTL实现
 */
export function setPageCache<T>(key: string, data: T): void {
  dictCache.set(key, data, Number.MAX_SAFE_INTEGER)
}

/**
 * 获取字典数据（带缓存）
 * @param key 缓存键
 * @param fetcher 数据获取函数
 * @param ttl 缓存时间
 * @returns 字典数据
 */
export async function getCachedData<T>(
  key: string,
  fetcher: () => Promise<T>,
  ttl?: number
): Promise<T> {
  // 尝试从缓存获取
  const cached = dictCache.get<T>(key)
  if (cached !== null) {
    return cached
  }

  // 缓存未命中，从服务器获取
  const data = await fetcher()
  
  // 存入缓存
  dictCache.set(key, data, ttl)
  
  return data
}
