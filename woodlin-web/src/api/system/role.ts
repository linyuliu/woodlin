/**
 * @file api/system/role.ts
 * @description 角色管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 角色实体 */
export interface SysRole {
  id?: number
  roleName: string
  roleCode: string
  status?: string
  remark?: string
  sort?: number
  createTime?: string
}

/** 角色分页查询参数 */
export interface RoleQuery {
  page?: number
  size?: number
  roleName?: string
  roleCode?: string
  status?: string
}

/** 分页查询角色 */
export function pageRoles(params: RoleQuery): Promise<PageResult<SysRole>> {
  return get('/system/role/list', params as Record<string, unknown>)
}

/** 获取角色详情 */
export function getRole(id: number): Promise<SysRole> {
  return get(`/system/role/${id}`)
}

/** 新增角色 */
export function createRole(data: SysRole): Promise<void> {
  return post('/system/role', data)
}

/** 更新角色（后端从 body 读取主键） */
export function updateRole(_id: number, data: SysRole): Promise<void> {
  return put('/system/role', data)
}

/** 删除角色（支持单个或批量，逗号拼接） */
export function deleteRole(id: number | number[]): Promise<void> {
  return del(`/system/role/${Array.isArray(id) ? id.join(',') : id}`)
}

/** 获取角色已分配的菜单/权限 ID 列表 */
export function getRoleMenus(id: number): Promise<number[]> {
  return get(`/system/role/menu/${id}`)
}

/** 保存角色菜单/权限分配 */
export function assignRoleMenus(id: number, permissionIds: number[]): Promise<void> {
  return put(`/system/role/menu/${id}`, { permissionIds })
}
