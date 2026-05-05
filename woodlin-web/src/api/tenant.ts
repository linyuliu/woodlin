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
  /** 租户ID（后端为字符串） */
  tenantId?: string
  /** 兼容旧字段 */
  id?: number | string
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
  pageNum?: number
  pageSize?: number
  /** 兼容旧字段 */
  page?: number
  size?: number
  tenantName?: string
  tenantCode?: string
  status?: string
}

/** 租户套餐实体 */
export interface SysTenantPackage {
  packageId?: number
  /** 兼容旧字段 */
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
  pageNum?: number
  pageSize?: number
  /** 兼容旧字段 */
  page?: number
  size?: number
  packageName?: string
  status?: string
}

interface BackendTenant {
  tenantId?: string
  tenantName: string
  tenantCode: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  expireTime?: string
  userLimit?: number
  packageId?: number
  packageName?: string
  status?: string
  remark?: string
  createTime?: string
}

interface BackendTenantPackage {
  packageId?: number
  packageName: string
  menuIds?: string
  status?: string
  remark?: string
  createTime?: string
}

function mapTenant(record: BackendTenant): SysTenant {
  return {
    tenantId: record.tenantId,
    id: record.tenantId,
    tenantName: record.tenantName,
    tenantCode: record.tenantCode,
    contactName: record.contactName,
    contactPhone: record.contactPhone,
    contactEmail: record.contactEmail,
    expireTime: record.expireTime,
    userCount: record.userLimit,
    packageId: record.packageId,
    packageName: record.packageName,
    status: record.status,
    remark: record.remark,
    createTime: record.createTime,
  }
}

function toBackendTenant(data: SysTenant): BackendTenant {
  return {
    tenantId: data.tenantId ?? (data.id ? String(data.id) : undefined),
    tenantName: data.tenantName,
    tenantCode: data.tenantCode,
    contactName: data.contactName,
    contactPhone: data.contactPhone,
    contactEmail: data.contactEmail,
    expireTime: data.expireTime,
    userLimit: data.userCount,
    packageId: data.packageId,
    status: data.status,
    remark: data.remark,
  }
}

function mapPackage(record: BackendTenantPackage): SysTenantPackage {
  const menuIds = record.menuIds
    ? record.menuIds.split(',').map((item) => Number(item.trim())).filter((item) => !Number.isNaN(item))
    : []
  return {
    packageId: record.packageId,
    id: record.packageId,
    packageName: record.packageName,
    menuIds,
    menuCount: menuIds.length,
    status: record.status,
    remark: record.remark,
    createTime: record.createTime,
  }
}

function toBackendPackage(data: SysTenantPackage): BackendTenantPackage {
  return {
    packageId: data.packageId ?? data.id,
    packageName: data.packageName,
    menuIds: (data.menuIds ?? []).join(','),
    status: data.status,
    remark: data.remark,
  }
}

/** 分页查询租户 */
export function getTenantPage(params: TenantQuery): Promise<PageResult<SysTenant>> {
  return get<PageResult<BackendTenant>>(
    '/system/tenant/list',
    {
      pageNum: params.pageNum ?? params.page,
      pageSize: params.pageSize ?? params.size,
      tenantName: params.tenantName,
      tenantCode: params.tenantCode,
      status: params.status,
    },
  ).then((res) => ({
    ...res,
    records: (res?.records ?? []).map(mapTenant),
  }))
}

/** 新增租户 */
export function createTenant(data: SysTenant): Promise<void> {
  return post('/system/tenant', toBackendTenant(data))
}

/** 更新租户（后端从 body 读取主键） */
export function updateTenant(_id: number | string, data: SysTenant): Promise<void> {
  return put('/system/tenant', toBackendTenant(data))
}

/** 删除租户（支持单个或批量，逗号拼接） */
export function deleteTenant(id: number | string | Array<number | string>): Promise<void> {
  const ids = Array.isArray(id) ? id.join(',') : id
  return del(`/system/tenant/${ids}`)
}

/** 修改租户状态 */
export function updateTenantStatus(id: number | string, status: string): Promise<void> {
  return put('/system/tenant/changeStatus', { tenantId: String(id), status })
}

/** 分页查询租户套餐 */
export function getPackagePage(params: PackageQuery): Promise<PageResult<SysTenantPackage>> {
  return get<PageResult<BackendTenantPackage>>(
    '/system/tenant/package',
    {
      pageNum: params.pageNum ?? params.page,
      pageSize: params.pageSize ?? params.size,
      packageName: params.packageName,
      status: params.status,
    },
  ).then((res) => ({
    ...res,
    records: (res?.records ?? []).map(mapPackage),
  }))
}

/** 获取所有启用套餐（下拉用） */
export function getAllPackages(): Promise<SysTenantPackage[]> {
  return get<BackendTenantPackage[]>('/system/tenant/package/all').then((res) => res.map(mapPackage))
}

/** 新增租户套餐 */
export function createPackage(data: SysTenantPackage): Promise<void> {
  return post('/system/tenant/package', toBackendPackage(data))
}

/** 更新租户套餐 */
export function updatePackage(id: number, data: SysTenantPackage): Promise<void> {
  return put(`/system/tenant/package/${id}`, toBackendPackage(data))
}

/** 删除租户套餐（支持单个或批量） */
export function deletePackage(id: number | number[]): Promise<void> {
  const ids = Array.isArray(id) ? id.join(',') : id
  return del(`/system/tenant/package/${ids}`)
}
