/**
 * 用户管理API服务
 * 
 * @author mumu
 * @description 用户管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 用户数据类型
 */
export interface SysUser {
  userId?: number
  username: string
  password?: string
  nickname?: string
  email?: string
  mobile?: string
  phone?: string
  gender?: string
  status?: string
  deptId?: number
  roleIds?: number[]
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 用户列表查询参数
 */
export interface UserListParams {
  pageNum?: number
  pageSize?: number
  username?: string
  nickname?: string
  email?: string
  status?: string
  deptId?: number
}

/**
 * 分页响应结构
 */
export interface UserPageData<T> {
  data: T[]
  current: number
  size: number
  total: number
  pages: number
}

/**
 * 分页查询用户列表
 * @param params 查询参数
 */
export function getUserList(params: UserListParams) {
  return request.get<UserPageData<SysUser>, UserPageData<SysUser>>('/system/user/list', { params })
}

/**
 * 根据用户ID获取详细信息
 * @param userId 用户ID
 */
export function getUserById(userId: number) {
  return request.get<SysUser, SysUser>(`/system/user/${userId}`)
}

/**
 * 新增用户
 * @param data 用户数据
 */
export function addUser(data: SysUser) {
  return request.post<SysUser, void>('/system/user', data)
}

/**
 * 修改用户
 * @param data 用户数据
 */
export function updateUser(data: SysUser) {
  return request.put<SysUser, void>('/system/user', data)
}

/**
 * 删除用户
 * @param userIds 用户ID，可以是字符串（多个用逗号分隔）或数组
 */
export function deleteUser(userIds: string | number[]) {
  const ids = Array.isArray(userIds) ? userIds.join(',') : userIds
  return request.delete<void, void>(`/system/user/${ids}`)
}

/**
 * 导出用户列表
 * @param params 查询参数
 */
export function exportUser(params: UserListParams) {
  return request({
    url: '/system/user/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 重置用户密码
 * @param userId 用户ID
 * @param password 新密码
 */
export function resetUserPassword(userId: number, password: string) {
  return request.put<void, void>('/system/user/resetPwd', null, {
    params: { userId, password }
  })
}
