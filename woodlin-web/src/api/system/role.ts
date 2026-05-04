/**
 * @file api/system/role.ts
 * @description 角色管理 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

export interface SysRole {
  id?: number
  name: string
  code: string
  remark?: string
  status?: string
  menuIds?: number[]
}

export function pageRoles(params: Record<string, unknown>): Promise<PageResult<SysRole>> {
  return get('/system/role/page', params)
}

export function getRole(id: number): Promise<SysRole> {
  return get(`/system/role/${id}`)
}

export function createRole(data: SysRole): Promise<void> {
  return post('/system/role', data)
}

export function updateRole(data: SysRole): Promise<void> {
  return put('/system/role', data)
}

export function deleteRole(id: number | number[]): Promise<void> {
  return del(`/system/role/${Array.isArray(id) ? id.join(',') : id}`)
}
