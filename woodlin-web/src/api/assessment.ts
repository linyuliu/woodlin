/**
 * @file api/assessment.ts
 * @description 综合评估：方案 schema CRUD + 运行实例 + 提交评分
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 评估指标 */
export interface AssessmentIndicator {
  id?: number
  indName: string
  indDesc?: string
  weight: number
  /** 100 / level / custom */
  scoreType: string
  score?: number
}

/** 评估维度 */
export interface AssessmentDimension {
  id?: number
  dimName: string
  weight: number
  indicators: AssessmentIndicator[]
  score?: number
}

/** 评估方案 */
export interface AssessmentSchema {
  id?: number
  schemaName: string
  schemaDesc?: string
  status?: string
  dimensions?: AssessmentDimension[]
  dimensionCount?: number
  createTime?: string
}

/** 评估方案查询参数 */
export interface AssessmentSchemaQuery {
  page?: number
  size?: number
  schemaName?: string
  status?: string
}

/** 评估实例 */
export interface AssessmentRuntime {
  id?: number
  schemaId: number
  schemaName?: string
  targetId: string
  targetType: string
  status?: string
  startTime?: string
  endTime?: string
  totalScore?: number
  remark?: string
  dimensions?: AssessmentDimension[]
}

/** 评估实例查询参数 */
export interface AssessmentRuntimeQuery {
  page?: number
  size?: number
  schemaId?: number
  status?: string
  targetType?: string
}

/** 分页查询评估方案 */
export function pageSchemas(params: AssessmentSchemaQuery): Promise<PageResult<AssessmentSchema>> {
  return get('/assessment/schema', params as Record<string, unknown>)
}

/** 获取评估方案详情 */
export function getSchema(id: number): Promise<AssessmentSchema> {
  return get(`/assessment/schema/${id}`)
}

/** 新增评估方案 */
export function createSchema(data: AssessmentSchema): Promise<void> {
  return post('/assessment/schema', data)
}

/** 更新评估方案 */
export function updateSchema(id: number, data: AssessmentSchema): Promise<void> {
  return put(`/assessment/schema/${id}`, data)
}

/** 删除评估方案 */
export function deleteSchema(id: number): Promise<void> {
  return del(`/assessment/schema/${id}`)
}

/** 分页查询评估实例 */
export function pageRuntimes(
  params: AssessmentRuntimeQuery,
): Promise<PageResult<AssessmentRuntime>> {
  return get('/assessment/runtime', params as Record<string, unknown>)
}

/** 获取评估实例详情 */
export function getRuntime(id: number): Promise<AssessmentRuntime> {
  return get(`/assessment/runtime/${id}`)
}

/** 新增评估实例 */
export function createRuntime(data: AssessmentRuntime): Promise<void> {
  return post('/assessment/runtime', data)
}

/** 提交评估实例 */
export function submitRuntime(id: number): Promise<void> {
  return put(`/assessment/runtime/${id}/submit`)
}

/** 删除评估实例 */
export function deleteRuntime(id: number): Promise<void> {
  return del(`/assessment/runtime/${id}`)
}
