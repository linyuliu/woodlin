/**
 * 菜单管理API服务
 *
 * @author mumu
 * @description 菜单/按钮权限管理相关API
 * @since 2026-03-15
 */

import request from '@/utils/request'

/**
 * 菜单信息
 */
export interface SysMenu {
  menuId?: number
  parentId?: number | null
  menuName: string
  permissionCode?: string
  permissionType: 'M' | 'C' | 'F'
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  status?: string
  visible?: string
  isFrame?: string
  isCache?: string
  remark?: string
  children?: SysMenu[]
}

/**
 * 菜单查询参数
 */
export interface MenuQueryParams {
  menuName?: string
  permissionCode?: string
  permissionType?: 'M' | 'C' | 'F'
  status?: string
}

/**
 * 查询菜单列表
 */
export function getMenuList(params?: MenuQueryParams) {
  return request.get<SysMenu[], SysMenu[]>('/system/menu/list', { params })
}

/**
 * 查询菜单树
 */
export function getMenuTree(params?: MenuQueryParams) {
  return request.get<SysMenu[], SysMenu[]>('/system/menu/tree', { params })
}

/**
 * 查询菜单详情
 */
export function getMenuById(menuId: number) {
  return request.get<SysMenu, SysMenu>(`/system/menu/${menuId}`)
}

/**
 * 新增菜单
 */
export function addMenu(data: SysMenu) {
  return request.post<SysMenu, void>('/system/menu', data)
}

/**
 * 修改菜单
 */
export function updateMenu(data: SysMenu) {
  return request.put<SysMenu, void>('/system/menu', data)
}

/**
 * 删除菜单
 */
export function deleteMenu(menuId: number) {
  return request.delete<void, void>(`/system/menu/${menuId}`)
}
