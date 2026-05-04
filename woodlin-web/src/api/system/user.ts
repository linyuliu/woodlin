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
  return get('/system/user/list', params as Record<string, unknown>)
}

/** 获取用户详情 */
export function getUser(id: number): Promise<SysUser> {
  return get(`/system/user/${id}`)
}

/** 新增用户 */
export function createUser(data: SysUser): Promise<void> {
  return post('/system/user', data)
}

/** 更新用户（后端从 body 读取主键） */
export function updateUser(_id: number, data: SysUser): Promise<void> {
  return put('/system/user', data)
}

/** 删除用户（支持单个或批量，逗号拼接） */
export function deleteUser(id: number | number[]): Promise<void> {
  return del(`/system/user/${Array.isArray(id) ? id.join(',') : id}`)
}

/** 修改用户状态 */
export function changeUserStatus(id: number, status: string): Promise<void> {
  return put('/system/user/changeStatus', { userId: id, status })
}

/** 重置用户密码 */
export function resetUserPassword(id: number, password: string): Promise<void> {
  return put('/system/user/resetPwd', { userId: id, password })
}

/** 获取当前用户菜单路由 */
export function getUserRoute(): Promise<RouteItem[]> {
  return get('/system/user/route')
}

/** 个人资料 */
export interface UserProfile {
  id?: number
  username?: string
  nickname?: string
  email?: string
  phone?: string
  sex?: string
  avatar?: string
  remark?: string
  lastLoginTime?: string
  lastLoginIp?: string
}

/** 获取个人资料 */
export function getProfile(): Promise<UserProfile> {
  return get('/system/user/profile')
}

/** 更新个人资料 */
export function updateProfile(data: UserProfile): Promise<void> {
  return post('/system/user/profile', data)
}

/** 修改个人密码 */
export function updatePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return post('/system/user/password', data)
}

/** 分配角色给用户 */
export function assignUserRoles(userId: number, roleIds: number[]): Promise<void> {
  return put(`/system/user/${userId}/roles`, { roleIds })
}
