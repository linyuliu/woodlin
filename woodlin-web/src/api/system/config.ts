/**
 * @file api/system/config.ts
 * @description 系统参数配置 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 参数配置 */
export interface SysConfig {
  id?: number
  configName: string
  configKey: string
  configValue: string
  /** Y=系统内置 N=自定义 */
  configType?: string
  remark?: string
  createTime?: string
}

/** 配置查询参数 */
export interface ConfigQuery {
  page?: number
  size?: number
  configName?: string
  configKey?: string
  configType?: string
}

/** 分页查询配置 */
export function pageConfigs(params: ConfigQuery): Promise<PageResult<SysConfig>> {
  return get('/system/config/list', params as Record<string, unknown>)
}

/** 新增配置 */
export function createConfig(data: SysConfig): Promise<void> {
  return post('/system/config', data)
}

/** 更新配置（后端从 body 读取主键） */
export function updateConfig(_id: number, data: SysConfig): Promise<void> {
  return put('/system/config', data)
}

/** 删除配置（支持单个或批量，逗号拼接） */
export function deleteConfig(id: number | number[]): Promise<void> {
  return del(`/system/config/${Array.isArray(id) ? id.join(',') : id}`)
}
