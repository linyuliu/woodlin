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
 * 获取所有配置列表
 */
export function getConfigList() {
  return request({
    url: '/system/config/list',
    method: 'get'
  })
}

/**
 * 根据配置键名获取配置
 * @param configKey 配置键名
 */
export function getConfigByKey(configKey: string) {
  return request({
    url: `/system/config/key/${configKey}`,
    method: 'get'
  })
}

/**
 * 根据配置键名获取配置值
 * @param configKey 配置键名
 */
export function getConfigValueByKey(configKey: string) {
  return request({
    url: `/system/config/value/${configKey}`,
    method: 'get'
  })
}

/**
 * 根据配置分类获取配置列表
 * @param category 配置分类（如：api.encryption, password.policy）
 */
export function getConfigsByCategory(category: string) {
  return request({
    url: `/system/config/category/${category}`,
    method: 'get'
  })
}

/**
 * 根据配置ID获取详细信息
 * @param configId 配置ID
 */
export function getConfigById(configId: number) {
  return request({
    url: `/system/config/${configId}`,
    method: 'get'
  })
}

/**
 * 新增配置
 * @param data 配置数据
 */
export function addConfig(data: SysConfig) {
  return request({
    url: '/system/config',
    method: 'post',
    data
  })
}

/**
 * 修改配置
 * @param data 配置数据
 */
export function updateConfig(data: SysConfig) {
  return request({
    url: '/system/config',
    method: 'put',
    data
  })
}

/**
 * 根据配置键名更新配置值
 * @param configKey 配置键名
 * @param configValue 配置值
 */
export function updateConfigByKey(configKey: string, configValue: string) {
  return request({
    url: `/system/config/key/${configKey}`,
    method: 'put',
    params: { configValue }
  })
}

/**
 * 批量更新配置
 * @param data 配置更新DTO
 */
export function batchUpdateConfig(data: ConfigUpdateDto) {
  return request({
    url: '/system/config/batch',
    method: 'put',
    data
  })
}

/**
 * 删除配置
 * @param configIds 配置ID，多个用逗号分隔
 */
export function deleteConfig(configIds: string) {
  return request({
    url: `/system/config/${configIds}`,
    method: 'delete'
  })
}
