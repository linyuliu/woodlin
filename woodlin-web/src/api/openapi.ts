/**
 * @file api/openapi.ts
 * @description OpenAPI 开放平台：应用 / 凭证 / 策略 / 概览 接口定义
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** OpenAPI 应用 */
export interface OpenApiApp {
  id?: number
  appName: string
  appCode: string
  /** 签名类型：HMAC-SHA256 / RSA / NONE */
  signType: string
  /** 每分钟限流，0 表示不限制 */
  rateLimit?: number
  ipWhitelist?: string
  status?: string
  remark?: string
  createTime?: string
}

/** 应用查询参数 */
export interface AppQuery {
  page?: number
  size?: number
  appName?: string
  status?: string
}

/** OpenAPI 凭证 */
export interface OpenApiCredential {
  id?: number
  appId: number
  appName?: string
  accessKey?: string
  secretKey?: string
  expireTime?: string
  status?: string
  remark?: string
  createTime?: string
}

/** 凭证查询参数 */
export interface CredentialQuery {
  page?: number
  size?: number
  appId?: number
  status?: string
}

/** OpenAPI 策略 */
export interface OpenApiPolicy {
  id?: number
  policyName: string
  appId: number
  appName?: string
  signRequired?: boolean
  encryptEnabled?: boolean
  rateLimitPerMin?: number
  ipWhitelist?: string
  status?: string
  remark?: string
  createTime?: string
}

/** 策略查询参数 */
export interface PolicyQuery {
  page?: number
  size?: number
  policyName?: string
  appId?: number
}

/** OpenAPI 概览数据 */
export interface OpenApiOverview {
  totalApps: number
  totalCalls: number
  successRate: number
  topApps: Array<{ appName: string; calls: number }>
}

/* ============== 应用 ============== */

/** 分页查询应用 */
export function pageApps(params: AppQuery): Promise<PageResult<OpenApiApp>> {
  return get('/openapi/app', params as Record<string, unknown>)
}

/** 新增应用 */
export function createApp(data: OpenApiApp): Promise<void> {
  return post('/openapi/app', data)
}

/** 更新应用 */
export function updateApp(id: number, data: OpenApiApp): Promise<void> {
  return put(`/openapi/app/${id}`, data)
}

/** 删除应用 */
export function deleteApp(id: number): Promise<void> {
  return del(`/openapi/app/${id}`)
}

/* ============== 凭证 ============== */

/** 分页查询凭证 */
export function pageCredentials(params: CredentialQuery): Promise<PageResult<OpenApiCredential>> {
  return get('/openapi/credential', params as Record<string, unknown>)
}

/** 新增凭证（返回包含一次性 secretKey 的明文） */
export function createCredential(data: OpenApiCredential): Promise<OpenApiCredential> {
  return post('/openapi/credential', data)
}

/** 撤销凭证 */
export function revokeCredential(id: number): Promise<void> {
  return put(`/openapi/credential/${id}/revoke`)
}

/** 删除凭证 */
export function deleteCredential(id: number): Promise<void> {
  return del(`/openapi/credential/${id}`)
}

/* ============== 策略 ============== */

/** 分页查询策略 */
export function pagePolicies(params: PolicyQuery): Promise<PageResult<OpenApiPolicy>> {
  return get('/openapi/policy', params as Record<string, unknown>)
}

/** 新增策略 */
export function createPolicy(data: OpenApiPolicy): Promise<void> {
  return post('/openapi/policy', data)
}

/** 更新策略 */
export function updatePolicy(id: number, data: OpenApiPolicy): Promise<void> {
  return put(`/openapi/policy/${id}`, data)
}

/** 删除策略 */
export function deletePolicy(id: number): Promise<void> {
  return del(`/openapi/policy/${id}`)
}

/* ============== 概览 ============== */

/** 概览统计 */
export function getOverview(): Promise<OpenApiOverview> {
  return get('/openapi/overview')
}
