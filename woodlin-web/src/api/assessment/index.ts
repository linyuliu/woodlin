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

// ===== 结构化 Schema =====

export interface SchemaDimensionBindingDTO {
  dimensionCode: string
  weight?: number
  scoreMode?: string
  reverseMode?: string
}

export interface SchemaOptionDTO {
  optionCode: string
  displayText: string
  mediaUrl?: string
  rawValue?: string
  scoreValue?: number
  scoreReverseValue?: number
  isExclusive?: boolean
  isCorrect?: boolean
  sortOrder?: number
}

export interface SchemaItemDTO {
  itemCode: string
  itemType: string
  stem?: string
  stemMediaUrl?: string
  helpText?: string
  sortOrder?: number
  isRequired?: boolean
  isScored?: boolean
  isAnchor?: boolean
  isReverse?: boolean
  isDemographic?: boolean
  maxScore?: number
  minScore?: number
  timeLimitSeconds?: number
  demographicField?: string
  options: SchemaOptionDTO[]
  dimensionBindings: SchemaDimensionBindingDTO[]
}

export interface SchemaSectionDTO {
  sectionCode: string
  sectionTitle: string
  sectionDesc?: string
  displayMode?: string
  randomStrategy?: string
  sortOrder?: number
  isRequired?: boolean
  anchorCode?: string
  items: SchemaItemDTO[]
}

export interface SchemaDimensionDTO {
  dimensionCode: string
  dimensionName: string
  dimensionDesc?: string
  parentDimensionCode?: string
  scoreMode?: string
  scoreDsl?: string
  normSetId?: number | string
  sortOrder?: number
}

export interface SchemaRuleDTO {
  ruleCode: string
  ruleName?: string
  ruleType?: string
  targetType?: string
  targetCode?: string
  dslSource?: string
  compiledRule?: string
  priority?: number
  isActive?: boolean
}

export interface AssessmentSchemaAggregateDTO {
  schemaId?: number | string
  formId?: number | string
  versionId?: number | string
  status?: string
  assessmentType?: string
  randomStrategy?: string
  description?: string
  contextFields?: Record<string, unknown>
  sections: SchemaSectionDTO[]
  dimensions: SchemaDimensionDTO[]
  rules: SchemaRuleDTO[]
  dslSource?: string
  compiledSchema?: string
  compileError?: string
  schemaHash?: string
  dslHash?: string
}

export interface AssessmentSchemaCompileResultDTO {
  versionId: number | string
  schemaId?: number | string
  status?: string
  schemaHash?: string
  dslHash?: string
  compiledSchema?: string
  compileError?: string
}

export interface ValidationIssue {
  severity: string
  code: string
  message: string
  targetType: string
  targetCode: string
}

export interface ValidationReport {
  versionId?: number | string
  valid: boolean
  issues: ValidationIssue[]
  errorCount: number
  warningCount: number
}

export function getAssessmentSchema(versionId: number | string): Promise<AssessmentSchemaAggregateDTO> {
  return request({
    url: `/assessment/schema/version/${versionId}`,
    method: 'get'
  }) as Promise<AssessmentSchemaAggregateDTO>
}

export function saveAssessmentSchema(
  versionId: number | string,
  data: AssessmentSchemaAggregateDTO
): Promise<AssessmentSchemaAggregateDTO> {
  return request({
    url: `/assessment/schema/version/${versionId}`,
    method: 'put',
    data
  }) as Promise<AssessmentSchemaAggregateDTO>
}

export function compileAssessmentSchema(versionId: number | string): Promise<AssessmentSchemaCompileResultDTO> {
  return request({
    url: `/assessment/schema/version/${versionId}/compile`,
    method: 'post'
  }) as Promise<AssessmentSchemaCompileResultDTO>
}

export function validateAssessmentSchema(versionId: number | string): Promise<ValidationReport> {
  return request({
    url: `/assessment/schema/version/${versionId}/validate`,
    method: 'post'
  }) as Promise<ValidationReport>
}

export function importAssessmentDsl(
  versionId: number | string,
  dslSource: string
): Promise<AssessmentSchemaAggregateDTO> {
  return request({
    url: `/assessment/schema/version/${versionId}/import-dsl`,
    method: 'post',
    data: {dslSource}
  }) as Promise<AssessmentSchemaAggregateDTO>
}

export async function exportAssessmentDsl(versionId: number | string): Promise<string> {
  const res = (await request({
    url: `/assessment/schema/version/${versionId}/export-dsl`,
    method: 'get'
  })) as { dslSource?: string }
  return res.dslSource ?? ''
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

// ===== 作答运行时 =====

export interface RuntimePublishVO {
  publishId: string
  formId: string
  versionId: string
  publishCode: string
  publishName: string
  status: string
  startTime?: string
  endTime?: string
  timeLimitMinutes: number
  maxAttempts: number
  allowAnonymous: boolean
  allowResume: boolean
  randomStrategy: string
  showResultImmediately: boolean
}

export interface RuntimeSessionVO {
  sessionId: string
  publishId: string
  formId: string
  versionId: string
  status: string
  displaySeed: number
  startedAt: string
  elapsedSeconds: number
  attemptNumber: number
  currentSectionCode?: string
  currentItemCode?: string
}

export interface RuntimeOptionVO {
  optionCode: string
  displayText: string
  mediaUrl?: string
  rawValue?: string
  isExclusive: boolean
  sortOrder: number
}

export interface RuntimeItemVO {
  itemCode: string
  itemType: string
  stem: string
  stemMediaUrl?: string
  helpText?: string
  sortOrder: number
  isRequired: boolean
  isScored: boolean
  isAnchor: boolean
  isReverse: boolean
  isDemographic: boolean
  timeLimitSeconds: number
  demographicField?: string
  options: RuntimeOptionVO[]
}

export interface RuntimeSectionVO {
  sectionCode: string
  sectionTitle: string
  sectionDesc?: string
  displayMode: string
  sortOrder: number
  isRequired: boolean
  anchorCode?: string
  items: RuntimeItemVO[]
}

export interface RuntimePayloadVO {
  publish: RuntimePublishVO
  session: RuntimeSessionVO
  sections: RuntimeSectionVO[]
  totalItems: number
  answerSnapshot?: Record<string, AnswerItemDTO>
}

export interface StartSessionDTO {
  publishId: string
  anonymousToken?: string
  clientIp?: string
  userAgent?: string
  deviceType?: string
}

export interface SaveSnapshotDTO {
  sessionId: string
  currentSectionCode?: string
  currentItemCode?: string
  answeredCache?: string
  elapsedSeconds?: number
}

export interface AnswerItemDTO {
  itemCode: string
  rawAnswer?: string
  selectedOptionCodes?: string[]
  textAnswer?: string
  timeSpentSeconds?: number
  isSkipped?: boolean
  displayOrder?: number
}

export interface SubmitAnswersDTO {
  sessionId: string
  answers: AnswerItemDTO[]
  elapsedSeconds?: number
}

export function getRuntimePublishInfo(publishId: string): Promise<RuntimePublishVO> {
  return request({ url: `/assessment/runtime/publish/${publishId}`, method: 'get' }) as Promise<RuntimePublishVO>
}

export function startOrResumeSession(data: StartSessionDTO): Promise<RuntimePayloadVO> {
  return request({ url: '/assessment/runtime/session/start', method: 'post', data }) as Promise<RuntimePayloadVO>
}

export function loadSessionPayload(sessionId: string): Promise<RuntimePayloadVO> {
  return request({ url: `/assessment/runtime/session/${sessionId}`, method: 'get' }) as Promise<RuntimePayloadVO>
}

export function saveSnapshot(data: SaveSnapshotDTO): Promise<void> {
  return request({ url: '/assessment/runtime/snapshot', method: 'post', data }) as Promise<void>
}

export function submitSession(data: SubmitAnswersDTO): Promise<void> {
  return request({ url: '/assessment/runtime/submit', method: 'post', data }) as Promise<void>
}
