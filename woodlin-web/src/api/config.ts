/**
 * 系统配置API服务
 * 
 * @author mumu
 * @description 系统配置相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 系统配置数据类型
 */
export interface SysConfig {
  configId?: number
  configName: string
  configKey: string
  configValue: string
  configType?: string
  tenantId?: string
  remark?: string
}

/**
 * 配置更新DTO
 */
export interface ConfigUpdateDto {
  category: string
  configs: Record<string, string>
}

/**
 * 构建信息
 */
export interface BuildInfo {
  version: string
  buildTime: string
  gitCommit?: string
  gitBranch?: string
  remoteUrl?: string
}

/**
 * 获取所有配置列表
 */
export function getConfigList(): Promise<SysConfig[]> {
  return request.get('/system/config/list')
}

/**
 * 根据配置键名获取配置
 * @param configKey 配置键名
 */
export function getConfigByKey(configKey: string): Promise<SysConfig> {
  return request.get(`/system/config/key/${configKey}`)
}

/**
 * 根据配置键名获取配置值
 * @param configKey 配置键名
 */
export function getConfigValueByKey(configKey: string): Promise<string> {
  return request.get(`/system/config/value/${configKey}`)
}

/**
 * 根据配置分类获取配置列表
 * @param category 配置分类（如：api.encryption, password.policy）
 */
export function getConfigsByCategory(category: string): Promise<SysConfig[]> {
  return request.get(`/system/config/category/${category}`)
}

/**
 * 根据配置ID获取详细信息
 * @param configId 配置ID
 */
export function getConfigById(configId: number): Promise<SysConfig> {
  return request.get(`/system/config/${configId}`)
}

/**
 * 新增配置
 * @param data 配置数据
 */
export function addConfig(data: SysConfig): Promise<void> {
  return request.post('/system/config', data)
}

/**
 * 修改配置
 * @param data 配置数据
 */
export function updateConfig(data: SysConfig): Promise<void> {
  return request.put('/system/config', data)
}

/**
 * 根据配置键名更新配置值
 * @param configKey 配置键名
 * @param configValue 配置值
 */
export function updateConfigByKey(configKey: string, configValue: string): Promise<void> {
  return request.put(`/system/config/key/${configKey}`, null, {
    params: { configValue }
  })
}

/**
 * 批量更新配置
 * @param data 配置更新DTO
 */
export function batchUpdateConfig(data: ConfigUpdateDto): Promise<void> {
  return request.put('/system/config/batch', data)
}

/**
 * 删除配置
 * @param configIds 配置ID，多个用逗号分隔
 */
export function deleteConfig(configIds: string): Promise<void> {
  return request.delete(`/system/config/${configIds}`)
}

/**
 * 获取构建信息
 */
export function getBuildInfo(): Promise<BuildInfo> {
  return request.get('/system/config/build-info')
}

/**
 * 清除配置缓存
 */
export function evictConfigCache(): Promise<void> {
  return request.post('/system/config/cache/evict')
}

/**
 * 预热配置缓存
 */
export function warmupConfigCache(): Promise<void> {
  return request.post('/system/config/cache/warmup')
}

