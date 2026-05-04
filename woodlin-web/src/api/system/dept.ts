/**
 * @file api/system/dept.ts
 * @description 部门管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'

/** 部门实体 */
export interface SysDept {
  deptId?: number
  id?: number
  parentId?: number
  deptName: string
  leader?: string
  phone?: string
  email?: string
  status?: string
  sort?: number
  children?: SysDept[]
}

/** 获取部门树 */
export function getDeptTree(): Promise<SysDept[]> {
  return get('/system/dept/tree')
}

/** 获取部门详情 */
export function getDept(id: number): Promise<SysDept> {
  return get(`/system/dept/${id}`)
}

/** 新增部门 */
export function createDept(data: SysDept): Promise<void> {
  return post('/system/dept', data)
}

/** 更新部门 */
export function updateDept(id: number, data: SysDept): Promise<void> {
  return put(`/system/dept/${id}`, data)
}

/** 删除部门 */
export function deleteDept(id: number): Promise<void> {
  return del(`/system/dept/${id}`)
}
