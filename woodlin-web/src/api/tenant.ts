import request from '@/utils/request'

export interface SysTenant {
  tenantId?: string
  tenantName: string
  tenantCode: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  status?: string
  expireTime?: string
  userLimit?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface TenantListParams {
  pageNum?: number
  pageSize?: number
  tenantName?: string
  tenantCode?: string
  status?: string
}

export interface PageResult<T> {
  code?: number
  message?: string
  data: T[]
  current: number
  size: number
  total: number
  pages: number
  hasPrevious?: boolean
  hasNext?: boolean
}

export function getTenantList(params: TenantListParams): Promise<PageResult<SysTenant>> {
  return request({
    url: '/system/tenant/list',
    method: 'get',
    params
  }) as Promise<PageResult<SysTenant>>
}

export function getTenantById(tenantId: string): Promise<SysTenant> {
  return request({
    url: `/system/tenant/${tenantId}`,
    method: 'get'
  }) as Promise<SysTenant>
}

export function addTenant(data: SysTenant): Promise<void> {
  return request({
    url: '/system/tenant',
    method: 'post',
    data
  }) as Promise<void>
}

export function updateTenant(data: SysTenant): Promise<void> {
  return request({
    url: '/system/tenant',
    method: 'put',
    data
  }) as Promise<void>
}

export function deleteTenant(tenantIds: string): Promise<void> {
  return request({
    url: `/system/tenant/${tenantIds}`,
    method: 'delete'
  }) as Promise<void>
}

export function changeTenantStatus(tenantId: string, status: string): Promise<void> {
  return request({
    url: `/system/tenant/changeStatus`,
    method: 'put',
    data: { tenantId, status }
  }) as Promise<void>
}
