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
  nickname?: string
  email?: string
  phone?: string
  gender?: string
  status?: string
  deptId?: number
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
 * 分页查询用户列表
 * @param params 查询参数
 */
export function getUserList(params: UserListParams) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

/**
 * 根据用户ID获取详细信息
 * @param userId 用户ID
 */
export function getUserById(userId: number) {
  return request({
    url: `/system/user/${userId}`,
    method: 'get'
  })
}

/**
 * 新增用户
 * @param data 用户数据
 */
export function addUser(data: SysUser) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

/**
 * 修改用户
 * @param data 用户数据
 */
export function updateUser(data: SysUser) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param userIds 用户ID，可以是字符串（多个用逗号分隔）或数组
 */
export function deleteUser(userIds: string | number[]) {
  const ids = Array.isArray(userIds) ? userIds.join(',') : userIds
  return request({
    url: `/system/user/${ids}`,
    method: 'delete'
  })
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
 */
export function resetUserPassword(userId: number) {
  return request({
    url: `/system/user/resetPwd/${userId}`,
    method: 'put'
  })
}
