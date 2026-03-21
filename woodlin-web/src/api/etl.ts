/**
 * ETL 离线向导 API
 *
 * 统一封装 ETL 离线任务的向导配置、分页列表、详情回填、预校验和批量创建接口。
 */

import request from '@/utils/request'
import type { ColumnMetadata, TableMetadata } from '@/api/datasource'

/**
 * 分页响应结构
 */
export interface PageData<T> {
  data: T[]
  current: number
  size: number
  total: number
  pages: number
  hasPrevious?: boolean
  hasNext?: boolean
}

/**
 * 同步模式
 */
export type EtlSyncMode = 'FULL' | 'INCREMENTAL'

/**
 * 字段取值来源
 */
export type EtlFieldValueSource = 'SOURCE_COLUMN' | 'CONSTANT' | 'DEFAULT_VALUE'

/**
 * 字段转换类型
 */
export type EtlTransformType =
  | 'NONE'
  | 'TRIM'
  | 'UPPER'
  | 'LOWER'
  | 'DATE_FORMAT'
  | 'NUMBER_CAST'

/**
 * 空值处理策略
 */
export type EtlNullStrategy =
  | 'KEEP_NULL'
  | 'EMPTY_STRING'
  | 'ZERO'
  | 'CURRENT_TIME'
  | 'SKIP_FIELD'

/**
 * 字段处理规则
 */
export interface EtlOfflineFieldRule {
  mappingId?: number
  enabled: boolean
  ordinalPosition: number
  sourceSchemaName?: string
  sourceTableName?: string
  sourceColumnName: string
  sourceColumnType?: string
  targetSchemaName?: string
  targetTableName?: string
  targetColumnName: string
  targetColumnType?: string
  valueSource: EtlFieldValueSource
  constantValue?: string
  defaultValue?: string
  nullStrategy: EtlNullStrategy
  transformType: EtlTransformType
  transformConfig?: Record<string, string | number | boolean | null | undefined>
  remark?: string
}

/**
 * 单表映射配置
 */
export interface EtlOfflineTableMapping {
  sourceSchema?: string
  sourceTable: string
  targetSchema?: string
  targetTable: string
  syncMode: EtlSyncMode
  incrementalColumn?: string
  filterCondition?: string
  remark?: string
  fieldRules: EtlOfflineFieldRule[]
}

/**
 * 运行时配置
 */
export interface EtlOfflineRuntimeConfig {
  syncMode: EtlSyncMode
  batchSize: number
  retryCount: number
  retryInterval: number
  allowConcurrent: boolean
  validationMode: string
  truncateTarget: boolean
  schemaSyncMode: string
}

/**
 * 调度配置
 */
export interface EtlOfflineScheduleConfig {
  autoStart: boolean
  cronExpression?: string
}

/**
 * 离线任务提交载荷
 */
export interface EtlOfflineJobPayload {
  jobName: string
  jobGroup?: string
  jobDescription?: string
  sourceDatasource: string
  targetDatasource: string
  runtimeConfig: EtlOfflineRuntimeConfig
  scheduleConfig: EtlOfflineScheduleConfig
  tableMappings: EtlOfflineTableMapping[]
  remark?: string
}

/**
 * 离线任务分页查询参数
 */
export interface EtlOfflineJobPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
  syncMode?: EtlSyncMode
  sourceDatasource?: string
  targetDatasource?: string
}

/**
 * 离线任务列表项
 */
export interface EtlOfflineJobSummary {
  jobId: number
  jobName: string
  jobGroup?: string
  jobDescription?: string
  sourceDatasource: string
  sourceSchema?: string
  sourceTable?: string
  targetDatasource: string
  targetSchema?: string
  targetTable: string
  syncMode: EtlSyncMode
  status?: string
  batchSize?: number
  retryCount?: number
  retryInterval?: number
  cronExpression?: string
  lastExecuteTime?: string
  lastExecuteStatus?: string
  createTime?: string
  updateTime?: string
}

/**
 * 离线任务详情
 */
export interface EtlOfflineJobDetail extends EtlOfflineJobPayload {
  jobId: number
  status?: string
  sourceDatasourceType?: string
  targetDatasourceType?: string
  lastExecuteTime?: string
  lastExecuteStatus?: string
  createTime?: string
  updateTime?: string
}

/**
 * 创建任务返回
 */
export interface EtlOfflineCreateJobResponse {
  createdJobIds: number[]
  createdJobCount: number
  items?: Array<{
    jobId: number
    jobName: string
    sourceTable?: string
    targetTable?: string
    status?: string
    cronExpression?: string
  }>
}

/**
 * 数据源类型配置
 */
export interface EtlDatasourceTypeOption {
  datasourceType: string
  displayName: string
  available: boolean
  enabledCount: number
  totalCount: number
}

/**
 * 数据源类型分组
 */
export interface EtlDatasourceTypeGroup {
  groupCode: string
  groupName: string
  options: EtlDatasourceTypeOption[]
}

/**
 * 离线向导初始化配置
 */
export interface EtlOfflineWizardConfig {
  datasourceTypeGroups: EtlDatasourceTypeGroup[]
  defaultSyncMode: EtlSyncMode
  defaultBatchSize: number
  defaultRetryCount: number
  defaultRetryInterval: number
  defaultJobGroup: string
  validationModes?: Array<{ label: string; value: string }>
  schemaSyncModes?: Array<{ label: string; value: string }>
  transformTypes?: Array<{ label: string; value: EtlTransformType }>
  nullStrategies?: Array<{ label: string; value: EtlNullStrategy }>
}

/**
 * 离线可选数据源
 */
export interface EtlOfflineDatasourceOption {
  datasourceCode: string
  datasourceName: string
  datasourceType: string
  status?: number
  owner?: string
  bizTags?: string
  remark?: string
}

/**
 * 字段规则建议
 */
export interface EtlOfflineFieldRuleSuggestion extends EtlOfflineFieldRule {
  matched?: boolean
}

/**
 * 单表校验结果
 */
export interface EtlOfflineValidationTableResult {
  sourceSchema?: string
  sourceTable: string
  targetSchema?: string
  targetTable: string
  syncMode: EtlSyncMode
  valid: boolean
  sourceTableExists?: boolean
  targetTableExists?: boolean
  errors: string[]
  warnings: string[]
  sourceColumns: string[]
  targetColumns: string[]
  suggestedIncrementalColumns: string[]
  suggestedFieldRules?: EtlOfflineFieldRuleSuggestion[]
}

/**
 * 整体预校验结果
 */
export interface EtlOfflineValidationResult {
  valid: boolean
  errors: string[]
  warnings: string[]
  tables: EtlOfflineValidationTableResult[]
}

/**
 * 执行日志
 */
export interface EtlExecutionLog {
  logId: number
  jobId: number
  jobName: string
  executionStatus: string
  startTime?: string
  endTime?: string
  duration?: number
  extractedRows?: number
  transformedRows?: number
  loadedRows?: number
  failedRows?: number
  errorMessage?: string
  executionDetail?: string
  tenantId?: string
  createTime?: string
  updateTime?: string
}

/**
 * 离线数据源查询参数
 */
export interface EtlOfflineDatasourceQuery {
  datasourceType?: string
  keyword?: string
  enabledOnly?: boolean
}

/**
 * 离线表查询参数
 */
export interface EtlOfflineTableQuery {
  datasourceCode: string
  schemaName?: string
  keyword?: string
  limit?: number
}

/**
 * 离线字段查询参数
 */
export interface EtlOfflineColumnQuery {
  datasourceCode: string
  schemaName?: string
  tableName: string
  keyword?: string
  limit?: number
}

interface RawOptionItem {
  label?: string
  value?: string
}

interface RawEtlOfflineWizardConfig {
  datasourceTypeGroups?: EtlDatasourceTypeGroup[]
  defaultSyncMode?: string
  defaultBatchSize?: number
  defaultRetryCount?: number
  defaultRetryInterval?: number
  defaultJobGroup?: string
  validationModes?: RawOptionItem[]
  schemaSyncModes?: RawOptionItem[]
  supportedValidationModes?: string[]
  supportedSchemaSyncModes?: string[]
}

interface RawEtlOfflineFieldRule {
  mappingId?: number
  enabled?: boolean | string
  ordinalPosition?: number
  sourceSchemaName?: string
  sourceTableName?: string
  sourceColumnName?: string
  sourceColumnType?: string
  targetSchemaName?: string
  targetTableName?: string
  targetColumnName?: string
  targetColumnType?: string
  valueSource?: string
  constantValue?: string
  defaultValue?: string
  nullStrategy?: string
  transformType?: string
  transformConfig?: Record<string, string | number | boolean | null | undefined>
  mappingAction?: string
  emptyValuePolicy?: string
  transformParams?: Record<string, string | number | boolean | null | undefined>
  remark?: string
}

interface RawEtlOfflineTableMapping {
  sourceSchema?: string
  sourceTable?: string
  targetSchema?: string
  targetTable?: string
  syncMode?: string
  incrementalColumn?: string
  filterCondition?: string
  remark?: string
  fieldRules?: RawEtlOfflineFieldRule[]
}

interface RawEtlOfflineRuntimeConfig {
  syncMode?: string
  batchSize?: number
  retryCount?: number
  retryInterval?: number
  allowConcurrent?: boolean
  validationMode?: string
  truncateTarget?: boolean
  schemaSyncMode?: string
  autoStart?: boolean
  cronExpression?: string
}

interface RawEtlOfflineJobDetail {
  jobId: number
  jobName: string
  jobGroup?: string
  jobDescription?: string
  sourceDatasource: string
  sourceDatasourceType?: string
  targetDatasource: string
  targetDatasourceType?: string
  status?: string
  concurrent?: string
  cronExpression?: string
  lastExecuteTime?: string
  lastExecuteStatus?: string
  remark?: string
  runtimeConfig?: RawEtlOfflineRuntimeConfig
  scheduleConfig?: Partial<EtlOfflineScheduleConfig>
  tableMapping?: RawEtlOfflineTableMapping
  tableMappings?: RawEtlOfflineTableMapping[]
  fieldRules?: RawEtlOfflineFieldRule[]
}

interface RawEtlOfflineValidationTableResult {
  sourceSchema?: string
  sourceTable?: string
  targetSchema?: string
  targetTable?: string
  syncMode?: string
  valid?: boolean
  sourceTableExists?: boolean
  targetTableExists?: boolean
  errors?: string[]
  warnings?: string[]
  sourceColumns?: string[]
  targetColumns?: string[]
  suggestedIncrementalColumns?: string[]
  suggestedFieldRules?: RawEtlOfflineFieldRule[]
}

interface RawEtlOfflineValidationResult {
  valid?: boolean
  errors?: string[]
  warnings?: string[]
  tables?: RawEtlOfflineValidationTableResult[]
  tableResults?: RawEtlOfflineValidationTableResult[]
}

const VALIDATION_MODE_LABELS: Record<string, string> = {
  NONE: '不开启校验',
  ROW_COUNT: '行数校验',
  CHECKSUM: '校验和值'
}

const SCHEMA_SYNC_MODE_LABELS: Record<string, string> = {
  NONE: '仅校验不补齐',
  AUTO_ADD_COLUMNS: '自动补齐目标字段'
}

/**
 * 标准化同步模式。
 */
function normalizeSyncMode(value?: string): EtlSyncMode {
  return value === 'INCREMENTAL' ? 'INCREMENTAL' : 'FULL'
}

/**
 * 将字符串数组转换为下拉选项。
 */
function buildOptions(values?: string[], labelMap?: Record<string, string>) {
  return values?.map((value) => ({
    label: labelMap?.[value] || value,
    value
  }))
}

/**
 * 解析字段取值来源。
 */
function resolveValueSource(rule: RawEtlOfflineFieldRule): EtlFieldValueSource {
  if (rule.valueSource === 'CONSTANT' || rule.mappingAction === 'CONSTANT') {
    return 'CONSTANT'
  }
  if (rule.valueSource === 'DEFAULT_VALUE' || rule.mappingAction === 'DEFAULT') {
    return 'DEFAULT_VALUE'
  }
  return 'SOURCE_COLUMN'
}

/**
 * 解析字段转换类型。
 */
function resolveTransformType(rule: RawEtlOfflineFieldRule): EtlTransformType {
  const transformType = (rule.transformType || rule.mappingAction || '').toUpperCase()
  if (
    transformType === 'TRIM' ||
    transformType === 'UPPER' ||
    transformType === 'LOWER' ||
    transformType === 'DATE_FORMAT' ||
    transformType === 'NUMBER_CAST'
  ) {
    return transformType
  }
  return 'NONE'
}

/**
 * 解析空值策略。
 */
function resolveNullStrategy(rule: RawEtlOfflineFieldRule): EtlNullStrategy {
  const nullStrategy = (rule.nullStrategy || '').toUpperCase()
  if (
    nullStrategy === 'KEEP_NULL' ||
    nullStrategy === 'EMPTY_STRING' ||
    nullStrategy === 'ZERO' ||
    nullStrategy === 'CURRENT_TIME' ||
    nullStrategy === 'SKIP_FIELD'
  ) {
    return nullStrategy
  }

  const emptyValuePolicy = (rule.emptyValuePolicy || '').toUpperCase()
  if (emptyValuePolicy === 'SKIP_FIELD') {
    return 'SKIP_FIELD'
  }
  if (emptyValuePolicy === 'DEFAULT') {
    if (rule.defaultValue === '') {
      return 'EMPTY_STRING'
    }
    if (rule.defaultValue === '0') {
      return 'ZERO'
    }
    if (rule.defaultValue === '__CURRENT_TIME__') {
      return 'CURRENT_TIME'
    }
  }
  return 'KEEP_NULL'
}

/**
 * 标准化字段规则回填。
 */
function normalizeFieldRule(
  rule: RawEtlOfflineFieldRule,
  targetSchemaName?: string,
  targetTableName?: string
): EtlOfflineFieldRule {
  return {
    mappingId: rule.mappingId,
    enabled: rule.enabled !== false && rule.enabled !== '0',
    ordinalPosition: rule.ordinalPosition || 0,
    sourceSchemaName: rule.sourceSchemaName,
    sourceTableName: rule.sourceTableName,
    sourceColumnName: rule.sourceColumnName || '',
    sourceColumnType: rule.sourceColumnType,
    targetSchemaName: rule.targetSchemaName || targetSchemaName,
    targetTableName: rule.targetTableName || targetTableName,
    targetColumnName: rule.targetColumnName || rule.sourceColumnName || '',
    targetColumnType: rule.targetColumnType,
    valueSource: resolveValueSource(rule),
    constantValue: rule.constantValue,
    defaultValue: rule.defaultValue,
    nullStrategy: resolveNullStrategy(rule),
    transformType: resolveTransformType(rule),
    transformConfig: rule.transformConfig || rule.transformParams || {},
    remark: rule.remark
  }
}

/**
 * 标准化表级回填。
 */
function normalizeTableMapping(
  mapping: RawEtlOfflineTableMapping,
  sharedFieldRules: RawEtlOfflineFieldRule[],
  fallbackSyncMode: string
): EtlOfflineTableMapping {
  const normalizedFieldRules = (mapping.fieldRules?.length ? mapping.fieldRules : sharedFieldRules).map((rule) =>
    normalizeFieldRule(rule, mapping.targetSchema, mapping.targetTable)
  )

  return {
    sourceSchema: mapping.sourceSchema,
    sourceTable: mapping.sourceTable || '',
    targetSchema: mapping.targetSchema,
    targetTable: mapping.targetTable || mapping.sourceTable || '',
    syncMode: normalizeSyncMode(mapping.syncMode || fallbackSyncMode),
    incrementalColumn: mapping.incrementalColumn,
    filterCondition: mapping.filterCondition,
    remark: mapping.remark,
    fieldRules: normalizedFieldRules
  }
}

/**
 * 标准化向导配置。
 */
function normalizeWizardConfig(raw: RawEtlOfflineWizardConfig): EtlOfflineWizardConfig {
  return {
    datasourceTypeGroups: raw.datasourceTypeGroups || [],
    defaultSyncMode: normalizeSyncMode(raw.defaultSyncMode),
    defaultBatchSize: raw.defaultBatchSize || 1000,
    defaultRetryCount: raw.defaultRetryCount || 3,
    defaultRetryInterval: raw.defaultRetryInterval || 60,
    defaultJobGroup: raw.defaultJobGroup || 'OFFLINE_SYNC',
    validationModes: raw.validationModes?.length
      ? raw.validationModes
          .filter((item): item is Required<RawOptionItem> => Boolean(item.label && item.value))
          .map((item) => ({ label: item.label, value: item.value }))
      : buildOptions(raw.supportedValidationModes, VALIDATION_MODE_LABELS),
    schemaSyncModes: raw.schemaSyncModes?.length
      ? raw.schemaSyncModes
          .filter((item): item is Required<RawOptionItem> => Boolean(item.label && item.value))
          .map((item) => ({ label: item.label, value: item.value }))
      : buildOptions(raw.supportedSchemaSyncModes, SCHEMA_SYNC_MODE_LABELS)
  }
}

/**
 * 标准化详情回填。
 */
function normalizeJobDetail(raw: RawEtlOfflineJobDetail): EtlOfflineJobDetail {
  const runtimeConfig = raw.runtimeConfig || {}
  const tableMappings = (raw.tableMappings?.length ? raw.tableMappings : raw.tableMapping ? [raw.tableMapping] : []).map(
    (mapping) => normalizeTableMapping(mapping, raw.fieldRules || [], runtimeConfig.syncMode || 'FULL')
  )

  return {
    jobId: raw.jobId,
    jobName: raw.jobName,
    jobGroup: raw.jobGroup,
    jobDescription: raw.jobDescription,
    sourceDatasource: raw.sourceDatasource,
    sourceDatasourceType: raw.sourceDatasourceType,
    targetDatasource: raw.targetDatasource,
    targetDatasourceType: raw.targetDatasourceType,
    status: raw.status,
    runtimeConfig: {
      syncMode: normalizeSyncMode(runtimeConfig.syncMode),
      batchSize: runtimeConfig.batchSize || 1000,
      retryCount: runtimeConfig.retryCount || 3,
      retryInterval: runtimeConfig.retryInterval || 60,
      allowConcurrent: Boolean(runtimeConfig.allowConcurrent ?? raw.concurrent === '1'),
      validationMode: runtimeConfig.validationMode || 'NONE',
      truncateTarget: Boolean(runtimeConfig.truncateTarget),
      schemaSyncMode: runtimeConfig.schemaSyncMode || 'AUTO_ADD_COLUMNS'
    },
    scheduleConfig: {
      autoStart: Boolean(raw.scheduleConfig?.autoStart ?? runtimeConfig.autoStart ?? raw.status === '1'),
      cronExpression: raw.scheduleConfig?.cronExpression || runtimeConfig.cronExpression || raw.cronExpression || ''
    },
    tableMappings,
    remark: raw.remark,
    lastExecuteTime: raw.lastExecuteTime,
    lastExecuteStatus: raw.lastExecuteStatus
  }
}

/**
 * 标准化预校验结果。
 */
function normalizeValidationResult(raw: RawEtlOfflineValidationResult): EtlOfflineValidationResult {
  const tables = (raw.tables || raw.tableResults || []).map((table) => ({
    sourceSchema: table.sourceSchema,
    sourceTable: table.sourceTable || '',
    targetSchema: table.targetSchema,
    targetTable: table.targetTable || '',
    syncMode: normalizeSyncMode(table.syncMode),
    valid: Boolean(table.valid),
    sourceTableExists: table.sourceTableExists,
    targetTableExists: table.targetTableExists,
    errors: table.errors || [],
    warnings: table.warnings || [],
    sourceColumns: table.sourceColumns || [],
    targetColumns: table.targetColumns || [],
    suggestedIncrementalColumns: table.suggestedIncrementalColumns || [],
    suggestedFieldRules: (table.suggestedFieldRules || []).map((rule) =>
      normalizeFieldRule(rule, table.targetSchema, table.targetTable)
    )
  }))

  return {
    valid: Boolean(raw.valid),
    errors: raw.errors || [],
    warnings: raw.warnings || [],
    tables
  }
}

/**
 * 将前端字段规则转换为后端兼容结构。
 */
function serializeFieldRule(rule: EtlOfflineFieldRule) {
  let mappingAction = 'COPY'
  if (rule.valueSource === 'CONSTANT') {
    mappingAction = 'CONSTANT'
  } else if (rule.valueSource === 'DEFAULT_VALUE') {
    mappingAction = 'DEFAULT'
  } else if (rule.transformType !== 'NONE') {
    mappingAction = rule.transformType
  }

  let emptyValuePolicy: string | undefined
  let defaultValue = rule.defaultValue
  if (rule.nullStrategy === 'EMPTY_STRING') {
    emptyValuePolicy = 'DEFAULT'
    defaultValue = ''
  } else if (rule.nullStrategy === 'ZERO') {
    emptyValuePolicy = 'DEFAULT'
    defaultValue = '0'
  } else if (rule.nullStrategy === 'CURRENT_TIME') {
    emptyValuePolicy = 'DEFAULT'
    defaultValue = '__CURRENT_TIME__'
  } else if (rule.nullStrategy === 'SKIP_FIELD') {
    emptyValuePolicy = 'SKIP_FIELD'
  } else {
    emptyValuePolicy = 'KEEP'
  }

  return {
    mappingId: rule.mappingId,
    enabled: rule.enabled,
    ordinalPosition: rule.ordinalPosition,
    sourceSchemaName: rule.sourceSchemaName,
    sourceTableName: rule.sourceTableName,
    sourceColumnName: rule.sourceColumnName,
    sourceColumnType: rule.sourceColumnType,
    targetSchemaName: rule.targetSchemaName,
    targetTableName: rule.targetTableName,
    targetColumnName: rule.targetColumnName,
    targetColumnType: rule.targetColumnType,
    valueSource: rule.valueSource,
    constantValue: rule.constantValue,
    defaultValue,
    nullStrategy: rule.nullStrategy,
    transformType: rule.transformType,
    transformConfig: rule.transformConfig,
    remark: rule.remark,
    mappingAction,
    emptyValuePolicy,
    transformParams: rule.transformConfig
  }
}

/**
 * 将前端新契约转换为当前后端可消费的请求体。
 */
function serializeJobPayload(data: EtlOfflineJobPayload) {
  return {
    jobName: data.jobName,
    jobGroup: data.jobGroup,
    jobDescription: data.jobDescription,
    sourceDatasource: data.sourceDatasource,
    targetDatasource: data.targetDatasource,
    runtimeConfig: {
      ...data.runtimeConfig,
      autoStart: data.scheduleConfig.autoStart,
      cronExpression: data.scheduleConfig.cronExpression
    },
    scheduleConfig: data.scheduleConfig,
    tableMappings: data.tableMappings.map((mapping) => ({
      sourceSchema: mapping.sourceSchema,
      sourceTable: mapping.sourceTable,
      targetSchema: mapping.targetSchema,
      targetTable: mapping.targetTable,
      syncMode: mapping.syncMode,
      incrementalColumn: mapping.incrementalColumn,
      filterCondition: mapping.filterCondition,
      remark: mapping.remark,
      fieldRules: mapping.fieldRules.map((rule) =>
        serializeFieldRule({
          ...rule,
          sourceSchemaName: rule.sourceSchemaName || mapping.sourceSchema,
          sourceTableName: rule.sourceTableName || mapping.sourceTable,
          targetSchemaName: rule.targetSchemaName || mapping.targetSchema,
          targetTableName: rule.targetTableName || mapping.targetTable
        })
      )
    })),
    remark: data.remark
  }
}

/**
 * 获取离线向导配置
 */
export function getEtlOfflineWizardConfig() {
  return request
    .get<RawEtlOfflineWizardConfig, RawEtlOfflineWizardConfig>('/etl/offline/config')
    .then(normalizeWizardConfig)
}

/**
 * 获取离线数据源选项
 */
export function getEtlOfflineDatasourceOptions(params?: EtlOfflineDatasourceQuery) {
  return request.get<EtlOfflineDatasourceOption[], EtlOfflineDatasourceOption[]>('/etl/offline/datasources', { params })
}

/**
 * 分页查询离线任务
 */
export function getEtlOfflineJobPage(params: EtlOfflineJobPageParams) {
  return request.get<PageData<EtlOfflineJobSummary>, PageData<EtlOfflineJobSummary>>('/etl/offline/jobs/page', { params })
}

/**
 * 获取离线任务详情
 */
export function getEtlOfflineJobDetail(jobId: number | string) {
  return request
    .get<RawEtlOfflineJobDetail, RawEtlOfflineJobDetail>(`/etl/offline/jobs/${jobId}`)
    .then(normalizeJobDetail)
}

/**
 * 预校验离线任务
 */
export function validateEtlOfflineJob(data: EtlOfflineJobPayload) {
  return request
    .post<ReturnType<typeof serializeJobPayload>, RawEtlOfflineValidationResult>(
      '/etl/offline/validate',
      serializeJobPayload(data)
    )
    .then(normalizeValidationResult)
}

/**
 * 批量创建离线任务
 */
export function createEtlOfflineJobs(data: EtlOfflineJobPayload) {
  return request.post<ReturnType<typeof serializeJobPayload>, EtlOfflineCreateJobResponse>(
    '/etl/offline/jobs',
    serializeJobPayload(data)
  )
}

/**
 * 更新离线任务
 */
export function updateEtlOfflineJob(jobId: number | string, data: EtlOfflineJobPayload) {
  return request.put<ReturnType<typeof serializeJobPayload>, EtlOfflineCreateJobResponse>(
    `/etl/offline/jobs/${jobId}`,
    serializeJobPayload(data)
  )
}

/**
 * 删除离线任务
 */
export function deleteEtlOfflineJob(jobId: number | string) {
  return request.delete<void, void>(`/etl/offline/jobs/${jobId}`)
}

/**
 * 启用离线任务
 */
export function enableEtlOfflineJob(jobId: number | string) {
  return request.post<void, void>(`/etl/jobs/${jobId}/enable`)
}

/**
 * 禁用离线任务
 */
export function disableEtlOfflineJob(jobId: number | string) {
  return request.post<void, void>(`/etl/jobs/${jobId}/disable`)
}

/**
 * 立即执行离线任务
 */
export function executeEtlOfflineJob(jobId: number | string) {
  return request.post<void, void>(`/etl/jobs/${jobId}/execute`)
}

/**
 * 查询可选表列表
 */
export function getEtlOfflineTables(params: EtlOfflineTableQuery) {
  return request.get<TableMetadata[], TableMetadata[]>('/etl/offline/tables', { params })
}

/**
 * 查询字段列表
 */
export function getEtlOfflineColumns(params: EtlOfflineColumnQuery) {
  return request.get<ColumnMetadata[], ColumnMetadata[]>('/etl/offline/columns', { params })
}

/**
 * 查询执行日志
 */
export function getEtlExecutionLogList() {
  return request.get<EtlExecutionLog[], EtlExecutionLog[]>('/etl/logs')
}
