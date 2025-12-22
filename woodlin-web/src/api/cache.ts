/**
 * 缓存管理API服务
 * 
 * @author mumu
 * @description Redis缓存管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 缓存配置信息
 */
export interface CacheConfig {
  redisEnabled: boolean
  dictionary?: {
    enabled: boolean
    expireSeconds: number
    refreshIntervalSeconds: number
  }
  config?: {
    enabled: boolean
    expireSeconds: number
    refreshIntervalSeconds: number
  }
}

/**
 * 缓存操作结果
 */
export interface CacheOperationResult {
  success: boolean
  message: string
  timestamp: number
}

/**
 * 获取缓存配置信息
 */
export function getCacheConfig(): Promise<CacheConfig> {
  return request.get('/cache/config')
}

/**
 * 清除指定字典缓存
 * @param dictType 字典类型
 */
export function evictDictionaryCache(dictType: string): Promise<CacheOperationResult> {
  return request.delete(`/cache/dictionary/${dictType}`)
}

/**
 * 清除所有字典缓存
 */
export function evictAllDictionaryCache(): Promise<CacheOperationResult> {
  return request.delete('/cache/dictionary/all')
}

/**
 * 预热字典缓存
 * @param dictType 字典类型
 */
export function warmupDictionaryCache(dictType: string): Promise<CacheOperationResult> {
  return request.post(`/cache/dictionary/${dictType}/warmup`)
}
