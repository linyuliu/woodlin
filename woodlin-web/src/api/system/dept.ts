/**
 * @file api/system/dept.ts
 * @description 部门管理 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'

/** 部门实体 */
export interface SysDept {
  deptId?: number
  id?: number
  parentId?: number
  deptName: string
  leader?: string
  phone?: string
  email?: string
  status?: string
  sort?: number
  children?: SysDept[]
}

type RawRecord = Record<string, unknown>

function getOptionalString(raw: RawRecord, key: string): string | undefined {
  const value = raw[key]
  return value === undefined || value === null ? undefined : String(value)
}

function getString(raw: RawRecord, key: string, fallback = ''): string {
  const value = raw[key]
  return value === undefined || value === null ? fallback : String(value)
}

function getOptionalNumber(raw: RawRecord, ...keys: string[]): number | undefined {
  for (const key of keys) {
    const value = raw[key]
    if (value !== undefined && value !== null) {
      return Number(value)
    }
  }
  return undefined
}

function mapDept(raw: RawRecord): SysDept {
  return {
    deptId: getOptionalNumber(raw, 'deptId'),
    id: getOptionalNumber(raw, 'id', 'deptId'),
    parentId: getOptionalNumber(raw, 'parentId'),
    deptName: getString(raw, 'deptName'),
    leader: getOptionalString(raw, 'leader'),
    phone: getOptionalString(raw, 'phone'),
    email: getOptionalString(raw, 'email'),
    status: getOptionalString(raw, 'status'),
    sort: getOptionalNumber(raw, 'sort'),
    children: Array.isArray(raw.children) ? raw.children.map((item) => mapDept(item as RawRecord)) : undefined,
  }
}

function toBackendDept(data: SysDept, id?: number): Record<string, unknown> {
  return {
    deptId: id ?? data.deptId ?? data.id,
    parentId: data.parentId,
    deptName: data.deptName,
    leader: data.leader,
    phone: data.phone,
    email: data.email,
    status: data.status,
    sort: data.sort,
  }
}

/** 获取部门树 */
export async function getDeptTree(): Promise<SysDept[]> {
  const data = await get<RawRecord[]>('/system/dept/tree')
  return Array.isArray(data) ? data.map(mapDept) : []
}

/** 获取部门详情 */
export async function getDept(id: number): Promise<SysDept> {
  const data = await get<RawRecord>(`/system/dept/${id}`)
  return mapDept(data ?? {})
}

/** 新增部门 */
export function createDept(data: SysDept): Promise<void> {
  return post('/system/dept', toBackendDept(data))
}

/** 更新部门 */
export function updateDept(id: number, data: SysDept): Promise<void> {
  return put('/system/dept', toBackendDept(data, id))
}

/** 删除部门 */
export function deleteDept(id: number): Promise<void> {
  return del(`/system/dept/${id}`)
}
