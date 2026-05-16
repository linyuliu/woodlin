/**
 * @file api/system/menu.ts
 * @description 菜单 / 权限管理 API
 * @author yulin
 * @since 2026-05-05
 */
import { del, get, post, put } from '@/utils/request'
import {
  getNumber,
  getOptionalString,
  type RawRecord,
} from '@/api/_utils'

export interface SysMenuNode {
  id: number
  parentId: number
  /** 节点类型：1=目录 2=菜单 3=按钮 */
  type: 1 | 2 | 3
  name: string
  title: string
  path: string
  component: string
  redirect?: string
  icon?: string
  permission?: string
  status: string
  visible: string
  isHidden: boolean
  isCache: boolean
  isFrame: boolean
  showInTabs: boolean
  activeMenu?: string
  sort: number
  remark?: string
  children?: SysMenuNode[]
}

function mapPermissionType(rawType: string | undefined): 1 | 2 | 3 {
  switch (rawType) {
    case 'M':
      return 1
    case 'F':
      return 3
    default:
      return 2
  }
}

function toPermissionType(type: number | undefined): 'M' | 'C' | 'F' {
  if (type === 1) {
    return 'M'
  }
  if (type === 3) {
    return 'F'
  }
  return 'C'
}

function deriveRouteName(title: string, path: string, component: string): string {
  const candidate = component || path || title
  return candidate
    .split(/[/-]/)
    .filter(Boolean)
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join('') || 'MenuRoute'
}

function mapMenu(raw: RawRecord): SysMenuNode {
  const title = getOptionalString(raw, 'title', 'menuName', 'permissionName') ?? ''
  const path = getOptionalString(raw, 'path') ?? ''
  const component = getOptionalString(raw, 'component') ?? ''
  const visible = getOptionalString(raw, 'visible') ?? '1'
  const status = getOptionalString(raw, 'status') ?? '1'

  return {
    id: getNumber(raw, ['id', 'menuId', 'permissionId']),
    parentId: getNumber(raw, ['parentId']),
    type: mapPermissionType(getOptionalString(raw, 'permissionType')),
    name: getOptionalString(raw, 'name') ?? deriveRouteName(title, path, component),
    title,
    path,
    component,
    redirect: getOptionalString(raw, 'redirect'),
    icon: getOptionalString(raw, 'icon'),
    permission: getOptionalString(raw, 'permission', 'permissionCode'),
    status,
    visible,
    isHidden: visible === '0',
    isCache: getOptionalString(raw, 'isCache') === '1',
    isFrame: getOptionalString(raw, 'isFrame') === '1',
    showInTabs: getOptionalString(raw, 'showInTabs') !== '0',
    activeMenu: getOptionalString(raw, 'activeMenu'),
    sort: getNumber(raw, ['sort', 'sortOrder']),
    remark: getOptionalString(raw, 'remark'),
    children: Array.isArray(raw.children) ? (raw.children as RawRecord[]).map(mapMenu) : undefined,
  }
}

function toBackendMenu(data: Partial<SysMenuNode>): Record<string, unknown> {
  const payload: Record<string, unknown> = {
    parentId: data.parentId ?? 0,
    permissionName: data.title,
    permissionCode: data.permission,
    permissionType: toPermissionType(data.type),
    path: data.path,
    component: data.component,
    icon: data.icon,
    sortOrder: data.sort ?? 0,
    status: data.status ?? '1',
    isFrame: data.isFrame ? '1' : '0',
    isCache: data.isCache ? '1' : '0',
    visible: data.isHidden ? '0' : '1',
    showInTabs: data.showInTabs === false ? '0' : '1',
    activeMenu: data.activeMenu,
    redirect: data.redirect,
    remark: data.remark,
  }
  if (data.id !== undefined) {
    payload.permissionId = data.id
  }
  return payload
}

/** 获取权限/菜单树 */
export async function getMenuTree(): Promise<SysMenuNode[]> {
  const data = await get<RawRecord[]>('/system/menu/tree')
  return Array.isArray(data) ? data.map(mapMenu) : []
}

/** 获取菜单详情 */
export async function getMenuDetail(id: number): Promise<SysMenuNode> {
  const data = await get<RawRecord>(`/system/menu/${id}`)
  return mapMenu(data ?? {})
}

/** 新增权限/菜单 */
export function createMenu(data: Partial<SysMenuNode>): Promise<void> {
  return post('/system/menu', toBackendMenu(data))
}

/** 更新权限/菜单（后端从 body 读取主键） */
export function updateMenu(_id: number, data: Partial<SysMenuNode>): Promise<void> {
  return put('/system/menu', toBackendMenu(data))
}

/** 删除权限/菜单 */
export function deleteMenu(id: number): Promise<void> {
  return del(`/system/menu/${id}`)
}
