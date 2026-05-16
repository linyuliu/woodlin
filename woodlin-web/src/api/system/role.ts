/**
 * @file api/system/role.ts
 * @description 角色管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'
import {
  getOptionalNumber,
  getOptionalString,
  getString,
  normalizePageResult,
  toPageParams,
  type RawRecord,
} from '@/api/_utils'

/** 角色实体 */
export interface SysRole {
  id?: number
  roleName: string
  roleCode: string
  status?: string
  remark?: string
  sort?: number
  createTime?: string
  dataScope?: string
  parentRoleId?: number
}

/** 角色分页查询参数 */
export interface RoleQuery {
  page?: number
  size?: number
  roleName?: string
  roleCode?: string
  status?: string
}

/** 角色树节点 */
export interface RoleTreeNode {
  id: number
  label: string
  parentId?: number
  children?: RoleTreeNode[]
}

/** 获取角色下的用户列表 */
export interface RoleUserQuery {
  page?: number
  size?: number
  roleId: number
}

export interface RoleUser {
  userId: number
  username: string
  nickname: string
  deptName?: string
  roleIds?: number[]
}

/** 数据权限范围 */
export interface DataScopeRequest {
  dataScope: string
  deptIds?: number[]
}

export interface RoleDataScope {
  dataScope: string
  deptIds: number[]
}

function mapRole(raw: RawRecord): SysRole {
  return {
    id: getOptionalNumber(raw, 'id', 'roleId'),
    roleName: getString(raw, 'roleName'),
    roleCode: getString(raw, 'roleCode'),
    status: getOptionalString(raw, 'status'),
    remark: getOptionalString(raw, 'remark'),
    sort: getOptionalNumber(raw, 'sort', 'sortOrder'),
    createTime: getOptionalString(raw, 'createTime'),
    dataScope: getOptionalString(raw, 'dataScope'),
    parentRoleId: getOptionalNumber(raw, 'parentRoleId'),
  }
}

function mapRoleUser(raw: RawRecord): RoleUser {
  return {
    userId: getOptionalNumber(raw, 'userId', 'id') ?? 0,
    username: getString(raw, 'username'),
    nickname: getString(raw, 'nickname'),
    deptName: getOptionalString(raw, 'deptName'),
    roleIds: Array.isArray(raw.roleIds) ? raw.roleIds.map((item) => Number(item)) : undefined,
  }
}

function toBackendRole(data: SysRole): Record<string, unknown> {
  const payload: Record<string, unknown> = {
    roleName: data.roleName,
    roleCode: data.roleCode,
    status: data.status,
    remark: data.remark,
    sortOrder: data.sort,
    dataScope: data.dataScope,
    parentRoleId: data.parentRoleId,
  }
  if (data.id !== undefined) {
    payload.roleId = data.id
  }
  return payload
}

/** 分页查询角色 */
export async function pageRoles(params: RoleQuery): Promise<PageResult<SysRole>> {
  const page = await get<PageResult<RawRecord>>('/system/role/list', {
    roleName: params.roleName,
    roleCode: params.roleCode,
    status: params.status,
    ...toPageParams(params),
  })
  return normalizePageResult(page, mapRole, params.page ?? 1, params.size ?? 10)
}

/** 获取角色详情 */
export async function getRole(id: number): Promise<SysRole> {
  const role = await get<RawRecord>(`/system/role/${id}`)
  return mapRole(role ?? {})
}

/** 新增角色 */
export function createRole(data: SysRole): Promise<void> {
  return post('/system/role', toBackendRole(data))
}

/** 更新角色（后端从 body 读取主键） */
export function updateRole(_id: number, data: SysRole): Promise<void> {
  return put('/system/role', toBackendRole(data))
}

/** 删除角色（支持单个或批量，逗号拼接） */
export function deleteRole(id: number | number[]): Promise<void> {
  return del(`/system/role/${Array.isArray(id) ? id.join(',') : id}`)
}

/** 获取角色已分配的菜单/权限 ID 列表 */
export function getRoleMenus(id: number): Promise<number[]> {
  return get(`/system/role/menu/${id}`)
}

/** 保存角色菜单/权限分配 */
export function assignRoleMenus(id: number, permissionIds: number[]): Promise<void> {
  return put(`/system/role/menu/${id}`, permissionIds)
}

/** 获取角色树（RBAC1） */
export function getRoleTree(tenantId?: string): Promise<RoleTreeNode[]> {
  return get('/system/role/tree', tenantId ? { tenantId } : {})
}

/** 获取角色下的用户列表 */
export async function getRoleUsers(params: RoleUserQuery): Promise<PageResult<RoleUser>> {
  const page = await get<PageResult<RawRecord>>(`/system/role/${params.roleId}/users`, {
    pageNum: params.page,
    pageSize: params.size,
  } as Record<string, unknown>)
  return normalizePageResult(page, mapRoleUser, params.page ?? 1, params.size ?? 10)
}

/** 保存角色数据权限 */
export function assignRoleDataScope(roleId: number, data: DataScopeRequest): Promise<void> {
  return post(`/system/role/${roleId}/data-scope`, data)
}

/** 查询角色数据权限 */
export async function getRoleDataScope(roleId: number): Promise<RoleDataScope> {
  const data = await get<{ dataScope?: string; deptIds?: number[] }>(`/system/role/${roleId}/data-scope`)
  return {
    dataScope: String(data?.dataScope ?? '1'),
    deptIds: Array.isArray(data?.deptIds) ? data.deptIds : [],
  }
}
