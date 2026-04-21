/**
 * 测评模块 API
 *
 * @author mumu
 * @since 2025-01-01
 */

import request from '@/utils/request'

// ===== 通用分页响应 =====

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

// ===== 测评主体 =====

export interface AssessmentForm {
  formId?: number | string
  formCode: string
  formName: string
  assessmentType: string
  categoryCode?: string
  description?: string
  coverUrl?: string
  tags?: string
  currentVersionId?: number | string
  status?: number
  sortOrder?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface AssessmentFormQuery {
  formName?: string
  formCode?: string
  assessmentType?: string
  categoryCode?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export function getAssessmentFormPage(params: AssessmentFormQuery): Promise<PageResult<AssessmentForm>> {
  return request({ url: '/assessment/form/list', method: 'get', params }) as Promise<PageResult<AssessmentForm>>
}

export function getAssessmentFormById(formId: number | string): Promise<AssessmentForm> {
  return request({ url: `/assessment/form/${formId}`, method: 'get' }) as Promise<AssessmentForm>
}

export function addAssessmentForm(data: AssessmentForm): Promise<void> {
  return request({ url: '/assessment/form', method: 'post', data }) as Promise<void>
}

export function updateAssessmentForm(data: AssessmentForm): Promise<void> {
  return request({ url: '/assessment/form', method: 'put', data }) as Promise<void>
}

export function deleteAssessmentForm(formId: number | string): Promise<void> {
  return request({ url: `/assessment/form/${formId}`, method: 'delete' }) as Promise<void>
}

export function updateAssessmentFormStatus(formId: number | string, status: number): Promise<void> {
  return request({ url: '/assessment/form/status', method: 'put', params: { formId, status } }) as Promise<void>
}

// ===== 测评版本 =====

export interface AssessmentFormVersion {
  versionId?: string
  formId: string
  versionNo: string
  versionTag?: string
  schemaId?: string
  schemaHash?: string
  dslHash?: string
  status?: string
  publishedAt?: string
  publishedBy?: string
  changeSummary?: string
  createTime?: string
  updateTime?: string
}

export interface AssessmentFormVersionQuery {
  formId?: number | string
  versionNo?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export function getFormVersionPage(params: AssessmentFormVersionQuery): Promise<PageResult<AssessmentFormVersion>> {
  return request({ url: '/assessment/form-version/list', method: 'get', params }) as Promise<PageResult<AssessmentFormVersion>>
}

export function getFormVersionById(versionId: number | string): Promise<AssessmentFormVersion> {
  return request({ url: `/assessment/form-version/${versionId}`, method: 'get' }) as Promise<AssessmentFormVersion>
}

export function addFormVersion(data: AssessmentFormVersion): Promise<void> {
  return request({ url: '/assessment/form-version', method: 'post', data }) as Promise<void>
}

export function updateFormVersion(data: AssessmentFormVersion): Promise<void> {
  return request({ url: '/assessment/form-version', method: 'put', data }) as Promise<void>
}

export function deleteFormVersion(versionId: number | string): Promise<void> {
  return request({ url: `/assessment/form-version/${versionId}`, method: 'delete' }) as Promise<void>
}

// ===== 发布实例 =====

export interface AssessmentPublish {
  publishId?: string
  formId: string
  versionId: string
  publishCode?: string
  publishName: string
  status?: string
  startTime?: string
  endTime?: string
  timeLimitMinutes?: number
  maxAttempts?: number
  allowAnonymous?: boolean
  allowResume?: boolean
  randomStrategy?: string
  showResultImmediately?: boolean
  resultVisibility?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface AssessmentPublishQuery {
  formId?: number | string
  publishName?: string
  publishCode?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export function getPublishPage(params: AssessmentPublishQuery): Promise<PageResult<AssessmentPublish>> {
  return request({ url: '/assessment/publish/list', method: 'get', params }) as Promise<PageResult<AssessmentPublish>>
}

export function getPublishById(publishId: number | string): Promise<AssessmentPublish> {
  return request({ url: `/assessment/publish/${publishId}`, method: 'get' }) as Promise<AssessmentPublish>
}

export function addPublish(data: AssessmentPublish): Promise<void> {
  return request({ url: '/assessment/publish', method: 'post', data }) as Promise<void>
}

export function updatePublish(data: AssessmentPublish): Promise<void> {
  return request({ url: '/assessment/publish', method: 'put', data }) as Promise<void>
}

export function deletePublish(publishId: number | string): Promise<void> {
  return request({ url: `/assessment/publish/${publishId}`, method: 'delete' }) as Promise<void>
}

export function updatePublishStatus(publishId: number | string, status: string): Promise<void> {
  return request({ url: '/assessment/publish/status', method: 'put', params: { publishId, status } }) as Promise<void>
}

// ===== 常模集 =====

export interface AssessmentNormSet {
  normSetId?: string
  formId: string
  normSetName: string
  normSetCode?: string
  sampleSize?: number
  collectionStart?: string
  collectionEnd?: string
  sourceDesc?: string
  applicabilityDesc?: string
  isDefault?: boolean
  status?: number
  createTime?: string
  updateTime?: string
}

export interface AssessmentNormSetQuery {
  formId?: number | string
  normSetName?: string
  normSetCode?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export function getNormSetPage(params: AssessmentNormSetQuery): Promise<PageResult<AssessmentNormSet>> {
  return request({ url: '/assessment/norm-set/list', method: 'get', params }) as Promise<PageResult<AssessmentNormSet>>
}

export function getNormSetById(normSetId: number | string): Promise<AssessmentNormSet> {
  return request({ url: `/assessment/norm-set/${normSetId}`, method: 'get' }) as Promise<AssessmentNormSet>
}

export function addNormSet(data: AssessmentNormSet): Promise<void> {
  return request({ url: '/assessment/norm-set', method: 'post', data }) as Promise<void>
}

export function updateNormSet(data: AssessmentNormSet): Promise<void> {
  return request({ url: '/assessment/norm-set', method: 'put', data }) as Promise<void>
}

export function deleteNormSet(normSetId: number | string): Promise<void> {
  return request({ url: `/assessment/norm-set/${normSetId}`, method: 'delete' }) as Promise<void>
}
