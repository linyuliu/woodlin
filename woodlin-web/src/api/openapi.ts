/**
 * @file api/openapi.ts
 * @description OpenAPI 开放平台：应用 / 凭证 / 策略 / 概览 / 全局设置 接口定义
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'

/* ============== 类型定义 ============== */

/** 开放应用（对应后端 SysOpenApp 实体） */
export interface OpenApiApp {
  appId?: number
  clientId?: number
  appCode: string
  appName: string
  status?: string
  tenantId?: string
  regionCode?: string
  regionName?: string
  ownerUserId?: number
  ownerDeptId?: number
  ownerName?: string
  ipWhitelist?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 开放客户 */
export interface OpenApiClient {
  clientId?: number
  clientCode: string
  clientName: string
  tenantId?: string
  ownerUserId?: number
  ownerDeptId?: number
  ownerName?: string
  status?: string
  remark?: string
}

/** 开放应用凭证视图（对应后端 OpenApiCredentialView） */
export interface OpenApiCredentialView {
  credentialId?: number
  appId?: number
  credentialName?: string
  credentialType?: string
  accessKey?: string
  appKeyMasked?: string
  secretKeyFingerprint?: string
  signatureAlgorithm?: string
  encryptionAlgorithm?: string
  securityMode?: string
  activeFrom?: string
  activeTo?: string
  lastRotatedTime?: string
  lastUsedTime?: string
  serverPublicKey?: string
  status?: string
  remark?: string
}

/** 凭证签发或轮换请求（对应后端 OpenApiCredentialRequest） */
export interface OpenApiCredentialRequest {
  credentialName: string
  credentialType?: string
  securityMode: string
  signatureAlgorithm?: string
  encryptionAlgorithm?: string
  activeFrom?: string
  activeTo?: string
  remark?: string
}

/** 凭证签发响应（对应后端 OpenApiCredentialIssueResponse） */
export interface OpenApiCredentialIssueResponse {
  credential: OpenApiCredentialView
  /** 明文 SecretKey，仅首次返回 */
  secretKey?: string
  /** 明文 AppKey，仅首次返回 */
  appKey?: string
  /** 签名私钥，仅首次返回 */
  signaturePrivateKey?: string
  /** 客户端解密私钥，仅首次返回 */
  encryptionPrivateKey?: string
}

/** 开放 API 策略（对应后端 SysOpenApiPolicy 实体） */
export interface OpenApiPolicy {
  policyId?: number
  policyName: string
  pathPattern: string
  httpMethod: string
  securityMode?: string
  signatureAlgorithm?: string
  encryptionAlgorithm?: string
  timestampWindowSeconds?: number
  nonceEnabled?: string
  nonceTtlSeconds?: number
  tenantRequired?: string
  enabled?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 开放 API 概览（对应后端 OpenApiOverviewDto） */
export interface OpenApiOverview {
  appCount: number
  credentialCount: number
  policyCount: number
}

/** 开放 API 全局设置（对应后端 OpenApiGlobalSettingsDto） */
export interface OpenApiGlobalSettings {
  defaultMode: string
  defaultSignatureAlgorithm: string
  timestampWindowSeconds: number
  nonceEnabled?: boolean
  nonceTtlSeconds: number
  defaultEncryptionAlgorithm: string
  encryptionRequired?: boolean
  gmEnabled?: boolean
}

export interface AuthScope {
  scopeId?: number
  capabilityId?: number
  scopeCode?: string
  scopeName?: string
  actions?: string
  enabled?: string
  tenantId?: string
}

export interface AuthSubjectGrant {
  grantId?: number
  subjectType?: string
  subjectId?: string
  capabilityId?: number
  scopeId?: number
  status?: string
  tenantId?: string
}

export interface AuthQuotaPolicy {
  quotaId?: number
  subjectType?: string
  subjectId?: string
  capabilityId?: number
  scopeId?: number
  windowSeconds?: number
  limitCount?: number
  enabled?: string
  tenantId?: string
}

export interface OpenApiResource {
  resourceId?: number
  resourceCode: string
  resourceName: string
  httpMethod: string
  pathPattern: string
  capabilityId?: number
  scopeId?: number
  authMode?: string
  status?: string
  tenantId?: string
  remark?: string
}

/* ============== 概览 / 全局设置 ============== */

/** 查询开放 API 概览 */
export function getOverview(): Promise<OpenApiOverview> {
  return get('/system/open-api/overview')
}

/** 查询开放 API 全局设置 */
export function getSettings(): Promise<OpenApiGlobalSettings> {
  return get('/system/open-api/settings')
}

/** 更新开放 API 全局设置 */
export function updateSettings(data: OpenApiGlobalSettings): Promise<void> {
  return put('/system/open-api/settings', data)
}

/* ============== 应用 ============== */

export function listClients(keyword?: string): Promise<OpenApiClient[]> {
  return get('/system/open-api/clients', keyword ? { keyword } : undefined)
}

export function createClient(data: OpenApiClient): Promise<void> {
  return post('/system/open-api/clients', data)
}

export function updateClient(data: OpenApiClient): Promise<void> {
  return put('/system/open-api/clients', data)
}

export function deleteClients(clientIds: number | number[] | string): Promise<void> {
  const ids = Array.isArray(clientIds) ? clientIds.join(',') : String(clientIds)
  return del(`/system/open-api/clients/${ids}`)
}

/** 查询开放应用列表（按关键字过滤） */
export function listApps(keyword?: string): Promise<OpenApiApp[]> {
  return get('/system/open-api/apps', keyword ? { keyword } : undefined)
}

/** 新增开放应用 */
export function createApp(data: OpenApiApp): Promise<void> {
  return post('/system/open-api/apps', data)
}

/** 更新开放应用 */
export function updateApp(data: OpenApiApp): Promise<void> {
  return put('/system/open-api/apps', data)
}

/** 删除开放应用（支持批量，多个 ID 用英文逗号分隔） */
export function deleteApps(appIds: number | number[] | string): Promise<void> {
  const ids = Array.isArray(appIds) ? appIds.join(',') : String(appIds)
  return del(`/system/open-api/apps/${ids}`)
}

export function listAppGrants(appId: number): Promise<AuthSubjectGrant[]> {
  return get(`/system/open-api/apps/${appId}/grants`)
}

export function saveAppGrants(appId: number, scopeIds: number[]): Promise<void> {
  return put(`/system/open-api/apps/${appId}/grants`, { scopeIds })
}

export function listAppQuotas(appId: number): Promise<AuthQuotaPolicy[]> {
  return get(`/system/open-api/apps/${appId}/quotas`)
}

export function saveAppQuotas(appId: number, quotas: AuthQuotaPolicy[]): Promise<void> {
  return put(`/system/open-api/apps/${appId}/quotas`, quotas)
}

/* ============== 凭证 ============== */

/** 查询某个应用下的凭证列表 */
export function listCredentials(appId: number): Promise<OpenApiCredentialView[]> {
  return get(`/system/open-api/apps/${appId}/credentials`)
}

/** 为指定应用签发新凭证 */
export function issueCredential(
  appId: number,
  request: OpenApiCredentialRequest,
): Promise<OpenApiCredentialIssueResponse> {
  return post(`/system/open-api/apps/${appId}/credentials`, request)
}

/** 轮换指定凭证 */
export function rotateCredential(
  credentialId: number,
  request: OpenApiCredentialRequest,
): Promise<OpenApiCredentialIssueResponse> {
  return post(`/system/open-api/credentials/${credentialId}/rotate`, request)
}

/** 吊销凭证 */
export function revokeCredential(credentialId: number): Promise<void> {
  return post(`/system/open-api/credentials/${credentialId}/revoke`)
}

/* ============== 策略 ============== */

/** 查询开放 API 策略列表（按关键字过滤） */
export function listPolicies(keyword?: string): Promise<OpenApiPolicy[]> {
  return get('/system/open-api/policies', keyword ? { keyword } : undefined)
}

/** 新增开放 API 策略 */
export function createPolicy(data: OpenApiPolicy): Promise<void> {
  return post('/system/open-api/policies', data)
}

/** 更新开放 API 策略 */
export function updatePolicy(data: OpenApiPolicy): Promise<void> {
  return put('/system/open-api/policies', data)
}

/** 删除开放 API 策略（支持批量，多个 ID 用英文逗号分隔） */
export function deletePolicies(policyIds: number | number[] | string): Promise<void> {
  const ids = Array.isArray(policyIds) ? policyIds.join(',') : String(policyIds)
  return del(`/system/open-api/policies/${ids}`)
}

/* ============== 授权目录 ============== */

export function listScopes(): Promise<AuthScope[]> {
  return get('/authorization/scopes')
}

export function listResources(keyword?: string): Promise<OpenApiResource[]> {
  return get('/system/open-api/resources', keyword ? { keyword } : undefined)
}

export function createResource(data: OpenApiResource): Promise<void> {
  return post('/system/open-api/resources', data)
}

export function updateResource(data: OpenApiResource): Promise<void> {
  return put('/system/open-api/resources', data)
}

export function deleteResources(resourceIds: number | number[] | string): Promise<void> {
  const ids = Array.isArray(resourceIds) ? resourceIds.join(',') : String(resourceIds)
  return del(`/system/open-api/resources/${ids}`)
}

export function listResourceApps(resourceId: number): Promise<OpenApiApp[]> {
  return get(`/system/open-api/resources/${resourceId}/apps`)
}
