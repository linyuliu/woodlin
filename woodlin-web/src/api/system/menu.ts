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
  return get('/system/menu/tree')
}

/** 新增权限/菜单 */
export function createMenu(data: Partial<RouteItem>): Promise<void> {
  return post('/system/menu', data)
}

/** 更新权限/菜单（后端从 body 读取主键） */
export function updateMenu(_id: number, data: Partial<RouteItem>): Promise<void> {
  return put('/system/menu', data)
}

/** 删除权限/菜单 */
export function deleteMenu(id: number): Promise<void> {
  return del(`/system/menu/${id}`)
}
