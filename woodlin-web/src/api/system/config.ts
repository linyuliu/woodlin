/**
 * @file api/system/config.ts
 * @description 系统参数配置 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

export interface SysConfig {
  id?: number
  name: string
  code: string
  value: string
  remark?: string
}

export function pageConfigs(params: Record<string, unknown>): Promise<PageResult<SysConfig>> {
  return get('/system/config/page', params)
}

export function createConfig(data: SysConfig): Promise<void> {
  return post('/system/config', data)
}

export function updateConfig(data: SysConfig): Promise<void> {
  return put('/system/config', data)
}

export function deleteConfig(id: number): Promise<void> {
  return del(`/system/config/${id}`)
}
