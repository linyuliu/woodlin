/**
 * @file api/tenant.ts
 * @description 租户管理 + 租户套餐 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 租户实体 */
export interface SysTenant {
  id?: number
  tenantName: string
  tenantCode: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  expireTime?: string
  userCount?: number
  packageId?: number
  packageName?: string
  status?: string
  remark?: string
  createTime?: string
}

/** 租户分页查询参数 */
export interface TenantQuery {
  page?: number
  size?: number
  tenantName?: string
  tenantCode?: string
  status?: string
}

/** 租户套餐实体 */
export interface SysTenantPackage {
  id?: number
  packageName: string
  menuIds?: number[]
  menuCount?: number
  status?: string
  remark?: string
  createTime?: string
}

/** 租户套餐分页查询参数 */
export interface PackageQuery {
  page?: number
  size?: number
  packageName?: string
  status?: string
}

/** 分页查询租户 */
export function getTenantPage(params: TenantQuery): Promise<PageResult<SysTenant>> {
  return get('/tenant', params as Record<string, unknown>)
}

/** 新增租户 */
export function createTenant(data: SysTenant): Promise<void> {
  return post('/tenant', data)
}

/** 更新租户 */
export function updateTenant(id: number, data: SysTenant): Promise<void> {
  return put(`/tenant/${id}`, data)
}

/** 删除租户 */
export function deleteTenant(id: number): Promise<void> {
  return del(`/tenant/${id}`)
}

/** 修改租户状态 */
export function updateTenantStatus(id: number, status: string): Promise<void> {
  return put(`/tenant/${id}/status`, { status })
}

/** 分页查询租户套餐 */
export function getPackagePage(params: PackageQuery): Promise<PageResult<SysTenantPackage>> {
  return get('/tenant/package', params as Record<string, unknown>)
}

/** 获取所有启用套餐（下拉用） */
export function getAllPackages(): Promise<SysTenantPackage[]> {
  return get('/tenant/package/all')
}

/** 新增租户套餐 */
export function createPackage(data: SysTenantPackage): Promise<void> {
  return post('/tenant/package', data)
}

/** 更新租户套餐 */
export function updatePackage(id: number, data: SysTenantPackage): Promise<void> {
  return put(`/tenant/package/${id}`, data)
}

/** 删除租户套餐 */
export function deletePackage(id: number): Promise<void> {
  return del(`/tenant/package/${id}`)
}
