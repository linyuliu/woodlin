/**
 * 租户管理API服务
 * 
 * @author mumu
 * @description 租户管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 租户数据类型
 */
export interface SysTenant {
  tenantId?: number
  tenantName: string
  tenantCode: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  status?: string
  expireTime?: string
  maxUsers?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 租户列表查询参数
 */
export interface TenantListParams {
  pageNum?: number
  pageSize?: number
  tenantName?: string
  tenantCode?: string
  status?: string
}

/**
 * 分页查询租户列表
 * @param params 查询参数
 */
export function getTenantList(params: TenantListParams) {
  return request({
    url: '/system/tenant/list',
    method: 'get',
    params
  })
}

/**
 * 根据租户ID获取详细信息
 * @param tenantId 租户ID
 */
export function getTenantById(tenantId: number) {
  return request({
    url: `/system/tenant/${tenantId}`,
    method: 'get'
  })
}

/**
 * 新增租户
 * @param data 租户数据
 */
export function addTenant(data: SysTenant) {
  return request({
    url: '/system/tenant',
    method: 'post',
    data
  })
}

/**
 * 修改租户
 * @param data 租户数据
 */
export function updateTenant(data: SysTenant) {
  return request({
    url: '/system/tenant',
    method: 'put',
    data
  })
}

/**
 * 删除租户
 * @param tenantIds 租户ID，多个用逗号分隔
 */
export function deleteTenant(tenantIds: string) {
  return request({
    url: `/system/tenant/${tenantIds}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用租户
 * @param tenantId 租户ID
 * @param status 状态 (0-停用, 1-启用)
 */
export function changeTenantStatus(tenantId: number, status: string) {
  return request({
    url: `/system/tenant/changeStatus`,
    method: 'put',
    data: { tenantId, status }
  })
}
