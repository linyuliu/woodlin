import request from '@/utils/request'
import type {
  ApiEncryptionAlgorithm,
  ApiSecurityMode,
  ApiSignatureAlgorithm
} from '@/utils/openapi-security'

export interface OpenApiOverview {
  appCount: number
  credentialCount: number
  policyCount: number
}

export interface OpenApiGlobalSettings {
  defaultMode: ApiSecurityMode
  defaultSignatureAlgorithm: ApiSignatureAlgorithm
  timestampWindowSeconds: number
  nonceEnabled: boolean
  nonceTtlSeconds: number
  defaultEncryptionAlgorithm: ApiEncryptionAlgorithm
  encryptionRequired: boolean
  gmEnabled: boolean
}

export interface SysOpenApp {
  appId?: number
  appCode: string
  appName: string
  status?: string
  tenantId?: string
  ownerName?: string
  ipWhitelist?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface OpenApiCredentialRequest {
  credentialName: string
  securityMode: ApiSecurityMode
  signatureAlgorithm: ApiSignatureAlgorithm
  encryptionAlgorithm?: ApiEncryptionAlgorithm
  activeFrom?: string | null
  activeTo?: string | null
  remark?: string
}

export interface OpenApiCredentialView {
  credentialId: number
  appId: number
  credentialName: string
  accessKey: string
  secretKeyFingerprint: string
  signatureAlgorithm: ApiSignatureAlgorithm
  encryptionAlgorithm: ApiEncryptionAlgorithm
  securityMode: ApiSecurityMode
  activeFrom?: string
  activeTo?: string
  lastRotatedTime?: string
  serverPublicKey?: string
  status: string
  remark?: string
}

export interface OpenApiCredentialIssueResponse {
  credential: OpenApiCredentialView
  secretKey?: string
  signaturePrivateKey?: string
  encryptionPrivateKey?: string
}

export interface SysOpenApiPolicy {
  policyId?: number
  policyName: string
  pathPattern: string
  httpMethod: string
  securityMode: ApiSecurityMode
  signatureAlgorithm?: ApiSignatureAlgorithm
  encryptionAlgorithm?: ApiEncryptionAlgorithm
  timestampWindowSeconds?: number
  nonceEnabled?: string
  nonceTtlSeconds?: number
  tenantRequired?: string
  enabled?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export function getOpenApiOverview(): Promise<OpenApiOverview> {
  return request.get('/system/open-api/overview')
}

export function getOpenApiSettings(): Promise<OpenApiGlobalSettings> {
  return request.get('/system/open-api/settings')
}

export function updateOpenApiSettings(data: OpenApiGlobalSettings): Promise<void> {
  return request.put('/system/open-api/settings', data)
}

export function listOpenApps(keyword?: string): Promise<SysOpenApp[]> {
  return request.get('/system/open-api/apps', {params: {keyword}})
}

export function createOpenApp(data: SysOpenApp): Promise<void> {
  return request.post('/system/open-api/apps', data)
}

export function updateOpenApp(data: SysOpenApp): Promise<void> {
  return request.put('/system/open-api/apps', data)
}

export function deleteOpenApps(appIds: string): Promise<void> {
  return request.delete(`/system/open-api/apps/${appIds}`)
}

export function listOpenAppCredentials(appId: number): Promise<OpenApiCredentialView[]> {
  return request.get(`/system/open-api/apps/${appId}/credentials`)
}

export function issueOpenAppCredential(appId: number, data: OpenApiCredentialRequest): Promise<OpenApiCredentialIssueResponse> {
  return request.post(`/system/open-api/apps/${appId}/credentials`, data)
}

export function rotateOpenAppCredential(
  credentialId: number,
  data: OpenApiCredentialRequest
): Promise<OpenApiCredentialIssueResponse> {
  return request.post(`/system/open-api/credentials/${credentialId}/rotate`, data)
}

export function revokeOpenAppCredential(credentialId: number): Promise<void> {
  return request.post(`/system/open-api/credentials/${credentialId}/revoke`)
}

export function listOpenApiPolicies(keyword?: string): Promise<SysOpenApiPolicy[]> {
  return request.get('/system/open-api/policies', {params: {keyword}})
}

export function createOpenApiPolicy(data: SysOpenApiPolicy): Promise<void> {
  return request.post('/system/open-api/policies', data)
}

export function updateOpenApiPolicy(data: SysOpenApiPolicy): Promise<void> {
  return request.put('/system/open-api/policies', data)
}

export function deleteOpenApiPolicies(policyIds: string): Promise<void> {
  return request.delete(`/system/open-api/policies/${policyIds}`)
}
