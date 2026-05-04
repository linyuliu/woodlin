/**
 * @file api/system/user.ts
 * @description 用户管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult, RouteItem } from '@/types/global'

/** 用户实体 */
export interface SysUser {
  id?: number
  username: string
  password?: string
  nickname?: string
  mobile?: string
  email?: string
  deptId?: number
  deptName?: string
  gender?: string
  status?: string
  roleIds?: number[]
  lastLoginTime?: string
  createTime?: string
  remark?: string
}

/** 用户分页查询参数 */
export interface UserQuery {
  page?: number
  size?: number
  username?: string
  nickname?: string
  mobile?: string
  deptId?: number
  status?: string
}

/** 分页查询用户 */
export function pageUsers(params: UserQuery): Promise<PageResult<SysUser>> {
  return get('/system/user', params as Record<string, unknown>)
}

/** 获取用户详情 */
export function getUser(id: number): Promise<SysUser> {
  return get(`/system/user/${id}`)
}

/** 新增用户 */
export function createUser(data: SysUser): Promise<void> {
  return post('/system/user', data)
}

/** 更新用户 */
export function updateUser(id: number, data: SysUser): Promise<void> {
  return put(`/system/user/${id}`, data)
}

/** 删除用户 */
export function deleteUser(id: number | number[]): Promise<void> {
  return del(`/system/user/${Array.isArray(id) ? id.join(',') : id}`)
}

/** 修改用户状态 */
export function changeUserStatus(id: number, status: string): Promise<void> {
  return put(`/system/user/${id}/status`, { status })
}

/** 重置用户密码 */
export function resetUserPassword(id: number, password: string): Promise<void> {
  return put(`/system/user/${id}/reset-password`, { password })
}

/** 获取当前用户菜单路由 */
export function getUserRoute(): Promise<RouteItem[]> {
  return get('/system/user/route')
}
