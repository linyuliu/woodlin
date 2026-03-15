/**
 * 部门管理API服务
 *
 * @author mumu
 * @description 部门管理相关API
 * @since 2026-03-15
 */

import request from '@/utils/request'

/**
 * 部门信息
 */
export interface SysDept {
  deptId?: number
  parentId?: number | null
  ancestors?: string
  deptName: string
  deptCode?: string
  sortOrder?: number
  leader?: string
  phone?: string
  email?: string
  status?: string
  remark?: string
  children?: SysDept[]
  hasChildren?: boolean
}

/**
 * 部门查询参数
 */
export interface DeptQueryParams {
  deptName?: string
  status?: string
}

/**
 * 查询部门列表（树）
 */
export function getDeptList(params?: DeptQueryParams) {
  return request.get<SysDept[], SysDept[]>('/system/dept/list', { params })
}

/**
 * 查询部门树
 */
export function getDeptTree(params?: DeptQueryParams) {
  return request.get<SysDept[], SysDept[]>('/system/dept/tree', { params })
}

/**
 * 查询部门详情
 */
export function getDeptById(deptId: number) {
  return request.get<SysDept, SysDept>(`/system/dept/${deptId}`)
}

/**
 * 新增部门
 */
export function addDept(data: SysDept) {
  return request.post<SysDept, void>('/system/dept', data)
}

/**
 * 修改部门
 */
export function updateDept(data: SysDept) {
  return request.put<SysDept, void>('/system/dept', data)
}

/**
 * 删除部门
 */
export function deleteDept(deptId: number) {
  return request.delete<void, void>(`/system/dept/${deptId}`)
}
