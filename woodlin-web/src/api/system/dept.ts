/**
 * @file api/system/dept.ts
 * @description 部门管理 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'

export interface SysDept {
  id?: number
  parentId?: number
  name: string
  sort?: number
  status?: string
  children?: SysDept[]
}

export function listDepts(params?: Record<string, unknown>): Promise<SysDept[]> {
  return get('/system/dept', params)
}

export function getDept(id: number): Promise<SysDept> {
  return get(`/system/dept/${id}`)
}

export function createDept(data: SysDept): Promise<void> {
  return post('/system/dept', data)
}

export function updateDept(data: SysDept): Promise<void> {
  return put('/system/dept', data)
}

export function deleteDept(id: number): Promise<void> {
  return del(`/system/dept/${id}`)
}
