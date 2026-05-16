/**
 * @file api/authorization.ts
 * @description 统一授权中心 API
 * @author yulin
 * @since 2026-06-02
 */
import { get, post, put } from '@/utils/request'

export type AuthorizationEffect = 'ALLOW' | 'DENY'

export interface AuthorizationSubject {
  type: string
  id: string
}

export interface AuthorizationResource {
  type: string
  id?: string
  attributes?: Record<string, unknown>
}

export interface AuthorizationContext {
  tenantId?: string
  ip?: string
  time?: string
  deptId?: number
  appId?: number
  headers?: Record<string, string>
  attributes?: Record<string, unknown>
}

export interface AuthorizationRequest {
  subject: AuthorizationSubject
  action: string
  resource: AuthorizationResource
  context?: AuthorizationContext
}

export interface AuthorizationDecision {
  effect: AuthorizationEffect
  reason?: string
  matchedPolicyCodes?: string[]
  obligations?: Record<string, unknown>
  allowed?: boolean
}

export interface AuthorizationConstraint {
  allowed: boolean
  tenantId?: string
  userId?: number
  deptId?: number
  deptIds?: number[]
  dataScope?: string
}

export interface AuthPolicy {
  policyId?: number
  policyCode: string
  policyName: string
  policyType: string
  priority?: number
  effect: AuthorizationEffect
  policyJson: string
  version?: number
  enabled?: string
  tenantId?: string
  remark?: string
}

export interface AuthCapability {
  capabilityId?: number
  capabilityCode: string
  capabilityName: string
  resourceType: string
  resourcePattern: string
  enabled?: string
  tenantId?: string
}

export interface AuthScope {
  scopeId?: number
  capabilityId: number
  scopeCode: string
  scopeName: string
  actions: string
  enabled?: string
  tenantId?: string
}

export interface AuthSubjectGrant {
  grantId?: number
  subjectType: string
  subjectId: string
  capabilityId: number
  scopeId: number
  status?: string
  tenantId?: string
}

export interface AuthQuotaPolicy {
  quotaId?: number
  subjectType: string
  subjectId: string
  capabilityId?: number
  scopeId?: number
  windowSeconds: number
  limitCount: number
  enabled?: string
  tenantId?: string
}

export function getAuthorizationDecision(request: AuthorizationRequest): Promise<AuthorizationDecision> {
  return post('/authorization/decision', request)
}

export function getAuthorizationConstraints(request: AuthorizationRequest): Promise<AuthorizationConstraint> {
  return post('/authorization/constraints', request)
}

export function listAuthPolicies(): Promise<AuthPolicy[]> {
  return fetchList('/authorization/policies')
}

export function createAuthPolicy(policy: AuthPolicy): Promise<void> {
  return post('/authorization/policies', policy)
}

export function updateAuthPolicy(policy: AuthPolicy): Promise<void> {
  return put('/authorization/policies', policy)
}

export function listAuthCapabilities(): Promise<AuthCapability[]> {
  return fetchList('/authorization/capabilities')
}

export function listAuthScopes(): Promise<AuthScope[]> {
  return fetchList('/authorization/scopes')
}

export function listAuthGrants(): Promise<AuthSubjectGrant[]> {
  return fetchList('/authorization/grants')
}

export function listAuthQuotas(): Promise<AuthQuotaPolicy[]> {
  return fetchList('/authorization/quotas')
}

function fetchList<T>(url: string): Promise<T[]> {
  return get<T[]>(url)
}
