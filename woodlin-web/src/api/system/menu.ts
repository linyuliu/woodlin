/**
 * @file api/system/menu.ts
 * @description 权限/菜单管理 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { RouteItem } from '@/types/global'

export function listMenus(params?: Record<string, unknown>): Promise<RouteItem[]> {
  return get('/system/permission', params)
}

export function getMenu(id: number): Promise<RouteItem> {
  return get(`/system/permission/${id}`)
}

export function createMenu(data: Partial<RouteItem>): Promise<void> {
  return post('/system/permission', data)
}

export function updateMenu(data: Partial<RouteItem>): Promise<void> {
  return put('/system/permission', data)
}

export function deleteMenu(id: number): Promise<void> {
  return del(`/system/permission/${id}`)
}
