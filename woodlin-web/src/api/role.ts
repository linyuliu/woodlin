/**
 * 角色管理API服务
 * 
 * @author mumu
 * @description 角色管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 角色数据类型
 */
export interface SysRole {
  roleId?: number
  roleName: string
  roleCode: string
  roleSort?: number // 兼容旧字段
  sortOrder?: number
  parentRoleId?: number | null
  roleLevel?: number
  rolePath?: string
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 角色列表查询参数
 */
export interface RoleListParams {
  pageNum?: number
  pageSize?: number
  roleName?: string
  roleCode?: string
  status?: string
}

/**
 * 分页查询角色列表
 * @param params 查询参数
 */
export function getRoleList(params: RoleListParams) {
  return request.get('/system/role/list', { params })
}

/**
 * 根据角色ID获取详细信息
 * @param roleId 角色ID
 */
export function getRoleById(roleId: number) {
  return request.get<SysRole, SysRole>(`/system/role/${roleId}`)
}

/**
 * 新增角色
 * @param data 角色数据
 */
export function addRole(data: SysRole) {
  return request.post<SysRole, void>('/system/role', data)
}

/**
 * 修改角色
 * @param data 角色数据
 */
export function updateRole(data: SysRole) {
  return request.put<SysRole, void>('/system/role', data)
}

/**
 * 删除角色
 * @param roleIds 角色ID，多个用逗号分隔
 */
export function deleteRole(roleIds: string) {
  return request.delete<void, void>(`/system/role/${roleIds}`)
}

/**
 * 获取角色权限菜单
 * @param roleId 角色ID
 */
export function getRoleMenus(roleId: number) {
  return request.get(`/system/role/menu/${roleId}`)
}

/**
 * 分配角色权限
 * @param roleId 角色ID
 * @param menuIds 菜单ID数组
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.put(`/system/role/menu/${roleId}`, menuIds)
}

/**
 * 角色树节点
 */
export interface RoleTreeNode {
  roleId: number
  parentRoleId?: number | null
  roleName: string
  roleCode: string
  roleLevel?: number
  rolePath?: string
  isInheritable?: string
  status?: string
  sortOrder?: number
  children?: RoleTreeNode[]
  hasChildren?: boolean
}

/**
 * 获取角色树（RBAC1）
 */
export function getRoleTree(params?: { tenantId?: string }) {
  return request.get<RoleTreeNode[], RoleTreeNode[]>('/system/role/tree', { params })
}

/**
 * 获取角色所有权限（包含继承）
 */
export function getRoleAllPermissions(roleId: number) {
  return request.get<string[], string[]>(`/system/role/${roleId}/all-permissions`)
}
