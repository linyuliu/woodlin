/**
 * @file api/system/menu.ts
 * @description 菜单 / 权限管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { RouteItem } from '@/types/global'

/** 获取权限/菜单树 */
export function getMenuTree(): Promise<RouteItem[]> {
  return get('/system/permission/tree')
}

/** 新增权限/菜单 */
export function createMenu(data: Partial<RouteItem>): Promise<void> {
  return post('/system/permission', data)
}

/** 更新权限/菜单 */
export function updateMenu(id: number, data: Partial<RouteItem>): Promise<void> {
  return put(`/system/permission/${id}`, data)
}

/** 删除权限/菜单 */
export function deleteMenu(id: number): Promise<void> {
  return del(`/system/permission/${id}`)
}
