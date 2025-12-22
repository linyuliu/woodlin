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
  roleSort?: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 分页查询角色列表
 * @param params 查询参数
 */
export function getRoleList(params: any) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

/**
 * 根据角色ID获取详细信息
 * @param roleId 角色ID
 */
export function getRoleById(roleId: number) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'get'
  })
}

/**
 * 新增角色
 * @param data 角色数据
 */
export function addRole(data: SysRole) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

/**
 * 修改角色
 * @param data 角色数据
 */
export function updateRole(data: SysRole) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

/**
 * 删除角色
 * @param roleIds 角色ID，多个用逗号分隔
 */
export function deleteRole(roleIds: string) {
  return request({
    url: `/system/role/${roleIds}`,
    method: 'delete'
  })
}

/**
 * 获取角色权限菜单
 * @param roleId 角色ID
 */
export function getRoleMenus(roleId: number) {
  return request({
    url: `/system/role/menu/${roleId}`,
    method: 'get'
  })
}

/**
 * 分配角色权限
 * @param roleId 角色ID
 * @param menuIds 菜单ID数组
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request({
    url: `/system/role/menu/${roleId}`,
    method: 'put',
    data: menuIds
  })
}
