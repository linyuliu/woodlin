/**
 * @file api/system/user.ts
 * @description 用户管理 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult, RouteItem } from '@/types/global'

export interface SysUser {
  id?: number
  username: string
  nickname: string
  email?: string
  phone?: string
  status?: string
  deptId?: number
  roleIds?: number[]
}

/** 分页查询用户 */
export function pageUsers(params: Record<string, unknown>): Promise<PageResult<SysUser>> {
  return get('/system/user/page', params)
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
export function updateUser(data: SysUser): Promise<void> {
  return put('/system/user', data)
}

/** 删除用户 */
export function deleteUser(id: number | number[]): Promise<void> {
  return del(`/system/user/${Array.isArray(id) ? id.join(',') : id}`)
}

/** 获取当前用户菜单路由 */
export function getUserRoute(): Promise<RouteItem[]> {
  return get('/system/user/route')
}
