/**
 * @file api/etl.ts
 * @description ETL 数据集成：对接真实离线任务插件接口，并兼容旧页面字段结构
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'
import { getDataSourceById, getDataSourceList, type DataSource } from '@/api/datasource'

/** 字段映射 */
export interface FieldMapping {
  src: string
  dst: string
  transform?: string
}

/** ETL 离线作业 */
export interface EtlOfflineJob {
  id?: number
  jobName: string
  sourceId: number
  sourceName?: string
  targetTable?: string
  targetId: number
  targetName?: string
  targetTableDest: string
  fieldMappings: FieldMapping[]
  filterExpr?: string
  schedule?: string
  status?: string
  lastRunTime?: string
  remark?: string
  createTime?: string
}

/** 离线作业查询参数 */
export interface EtlOfflineJobQuery {
  page?: number
  size?: number
  jobName?: string
  sourceId?: number
  targetId?: number
  status?: string
}

/** ETL 执行日志 */
export interface EtlLog {
  id?: number
  jobId: number
  jobName?: string
  /** 0=成功 1=失败 2=运行中 */
  status: string
  startTime?: string
  stopTime?: string
  rowsRead?: number
  rowsWritten?: number
  errorMsg?: string
  detail?: string
}

/** 日志查询参数 */
export interface EtlLogQuery {
  page?: number
  size?: number
  jobId?: number
  status?: string
  startTime?: string
  endTime?: string
}

type RawRecord = Record<string, unknown>

interface EtlOfflineValidationRequest {
  sourceDatasource: string
  targetDatasource: string
  runtimeConfig: Record<string, unknown>
  tableMappings: Array<Record<string, unknown>>
}

function getString(raw: RawRecord, key: string, fallback = ''): string {
  const value = raw[key]
  return value === undefined || value === null ? fallback : String(value)
}

function getOptionalNumber(raw: RawRecord, ...keys: string[]): number | undefined {
  for (const key of keys) {
    const value = raw[key]
    if (value !== undefined && value !== null) {
      return Number(value)
    }
  }
  return undefined
}

function toFrontJobStatus(status?: string): string {
  return status === '0' ? '1' : '0'
}

function toBackendJobStatus(status?: string): string | undefined {
  if (status === undefined) {
    return undefined
  }
  return status === '1' ? '0' : '1'
}

function toFrontLogStatus(status?: string): string {
  if (status === 'RUNNING') {
    return '2'
  }
  if (status === 'SUCCESS') {
    return '0'
  }
  return '1'
}

function toBackendLogStatus(status?: string): string | undefined {
  if (status === '2') {
    return 'RUNNING'
  }
  if (status === '0') {
    return 'SUCCESS'
  }
  if (status === '1') {
    return 'FAILED'
  }
  return undefined
}

function mapOfflineJob(raw: RawRecord, datasourcesByCode: Map<string, DataSource>): EtlOfflineJob {
  const sourceDatasource = getString(raw, 'sourceDatasource')
  const targetDatasource = getString(raw, 'targetDatasource')
  const source = datasourcesByCode.get(sourceDatasource)
  const target = datasourcesByCode.get(targetDatasource)
  return {
    id: getOptionalNumber(raw, 'id', 'jobId'),
    jobName: getString(raw, 'jobName'),
    sourceId: source?.id ?? 0,
    sourceName: source?.dsName ?? sourceDatasource,
    targetTable: getString(raw, 'sourceTable', ''),
    targetId: target?.id ?? 0,
    targetName: target?.dsName ?? targetDatasource,
    targetTableDest: getString(raw, 'targetTable'),
    fieldMappings: [],
    filterExpr: getString(raw, 'filterCondition', ''),
    schedule: getString(raw, 'cronExpression', ''),
    status: toFrontJobStatus(getString(raw, 'status', '1')),
    lastRunTime: getString(raw, 'lastExecuteTime', ''),
    remark: getString(raw, 'remark', ''),
    createTime: getString(raw, 'createTime', ''),
  }
}

function mapFieldRules(fieldRules: RawRecord[] | undefined): FieldMapping[] {
  return (fieldRules ?? []).map((item) => ({
    src: getString(item, 'sourceColumnName'),
    dst: getString(item, 'targetColumnName'),
    transform: getString(item, 'mappingAction', ''),
  }))
}

async function buildDatasourceMaps(): Promise<{
  byCode: Map<string, DataSource>
  byId: Map<number, DataSource>
}> {
  const datasources = await getDataSourceList()
  return {
    byCode: new Map(datasources.map((item) => [item.datasourceCode ?? '', item])),
    byId: new Map(datasources.map((item) => [item.id ?? 0, item])),
  }
}

async function toValidationRequest(job: EtlOfflineJob): Promise<EtlOfflineValidationRequest> {
  const source = await getDataSourceById(job.sourceId)
  const target = await getDataSourceById(job.targetId)
  return {
    sourceDatasource: source?.datasourceCode ?? '',
    targetDatasource: target?.datasourceCode ?? '',
    runtimeConfig: {
      syncMode: 'FULL',
      batchSize: 1000,
      retryCount: 3,
      retryInterval: 60,
      allowConcurrent: false,
      autoStart: job.status !== '1',
      cronExpression: job.schedule,
    },
    tableMappings: [
      {
        sourceTable: job.targetTable,
        targetTable: job.targetTableDest,
        filterCondition: job.filterExpr,
        fieldRules: job.fieldMappings.map((item, index) => ({
          sourceColumnName: item.src,
          targetColumnName: item.dst,
          mappingAction: item.transform || 'COPY',
          ordinalPosition: index + 1,
          enabled: true,
        })),
      },
    ],
  }
}

async function toCreateRequest(job: EtlOfflineJob): Promise<Record<string, unknown>> {
  const request = await toValidationRequest(job)
  return {
    jobName: job.jobName,
    jobGroup: 'OFFLINE_SYNC',
    jobDescription: job.remark,
    sourceDatasource: request.sourceDatasource,
    targetDatasource: request.targetDatasource,
    runtimeConfig: request.runtimeConfig,
    tableMappings: request.tableMappings,
    remark: job.remark,
  }
}

/** 分页查询离线作业 */
export async function pageOfflineJobs(
  params: EtlOfflineJobQuery,
): Promise<PageResult<EtlOfflineJob>> {
  const maps = await buildDatasourceMaps()
  const source = params.sourceId ? maps.byId.get(params.sourceId) : undefined
  const target = params.targetId ? maps.byId.get(params.targetId) : undefined
  const page = await get<PageResult<RawRecord>>('/etl/offline/jobs/page', {
    keyword: params.jobName,
    sourceDatasource: source?.datasourceCode,
    targetDatasource: target?.datasourceCode,
    status: toBackendJobStatus(params.status),
    pageNum: params.page,
    pageSize: params.size,
  })
  return {
    records: (page?.records ?? []).map((item) => mapOfflineJob(item, maps.byCode)),
    total: page?.total ?? 0,
    current: page?.current ?? params.page ?? 1,
    size: page?.size ?? params.size ?? 10,
  }
}

/** 查询离线作业详情 */
export async function getOfflineJob(id: number): Promise<EtlOfflineJob> {
  const maps = await buildDatasourceMaps()
  const detail = await get<RawRecord>(`/etl/offline/jobs/${id}`)
  const sourceDatasource = getString(detail ?? {}, 'sourceDatasource')
  const targetDatasource = getString(detail ?? {}, 'targetDatasource')
  const source = maps.byCode.get(sourceDatasource)
  const target = maps.byCode.get(targetDatasource)
  const runtimeConfig = (detail?.runtimeConfig ?? {}) as RawRecord
  const tableMapping = (detail?.tableMapping ?? {}) as RawRecord
  const fieldRules = Array.isArray(detail?.fieldRules) ? (detail.fieldRules as RawRecord[]) : []
  return {
    id: getOptionalNumber(detail ?? {}, 'jobId'),
    jobName: getString(detail ?? {}, 'jobName'),
    sourceId: source?.id ?? 0,
    sourceName: source?.dsName ?? sourceDatasource,
    targetTable: getString(tableMapping, 'sourceTable'),
    targetId: target?.id ?? 0,
    targetName: target?.dsName ?? targetDatasource,
    targetTableDest: getString(tableMapping, 'targetTable'),
    fieldMappings: mapFieldRules(fieldRules),
    filterExpr: getString(tableMapping, 'filterCondition', ''),
    schedule: getString(runtimeConfig, 'cronExpression', ''),
    status: toFrontJobStatus(getString(detail ?? {}, 'status', '1')),
    lastRunTime: getString(detail ?? {}, 'lastExecuteTime', ''),
    remark: getString(detail ?? {}, 'remark', ''),
    createTime: getString(detail ?? {}, 'createTime', ''),
  }
}

/** 新增离线作业 */
export async function createOfflineJob(data: EtlOfflineJob): Promise<void> {
  return post('/etl/offline/jobs', await toCreateRequest(data))
}

/** 更新离线作业 */
export async function updateOfflineJob(id: number, data: EtlOfflineJob): Promise<void> {
  return put(`/etl/offline/jobs/${id}`, await toCreateRequest(data))
}

/** 删除离线作业 */
export function deleteOfflineJob(id: number): Promise<void> {
  return del(`/etl/offline/jobs/${id}`)
}

/** 立即执行作业 */
export function runOfflineJob(id: number): Promise<void> {
  return post(`/etl/jobs/${id}/execute`)
}

/** 结构预览（基于预校验建议） */
export async function previewOfflineJob(id: number): Promise<Record<string, unknown>[]> {
  const job = await getOfflineJob(id)
  const validation = await post<RawRecord>('/etl/offline/validate', await toValidationRequest(job))
  const tableResults = Array.isArray(validation?.tableResults) ? (validation.tableResults as RawRecord[]) : []
  if (tableResults.length === 0) {
    return [
      {
        message: '暂无可预览数据',
        valid: validation?.valid,
      },
    ]
  }
  const first = tableResults[0]
  const suggested = Array.isArray(first.suggestedFieldRules) ? (first.suggestedFieldRules as RawRecord[]) : []
  if (suggested.length > 0) {
    return suggested.map((item) => ({
      sourceColumn: getString(item, 'sourceColumnName'),
      targetColumn: getString(item, 'targetColumnName'),
      action: getString(item, 'mappingAction'),
      enabled: item.enabled,
    }))
  }
  return [
    {
      sourceTable: getString(first, 'sourceTable'),
      targetTable: getString(first, 'targetTable'),
      sourceTableExists: first.sourceTableExists,
      targetTableExists: first.targetTableExists,
      valid: first.valid,
      errors: Array.isArray(first.errors) ? first.errors.join('; ') : '',
      warnings: Array.isArray(first.warnings) ? first.warnings.join('; ') : '',
    },
  ]
}

/** 分页查询执行日志 */
export async function pageEtlLogs(params: EtlLogQuery): Promise<PageResult<EtlLog>> {
  const page = await get<PageResult<RawRecord>>('/etl/logs/page', {
    pageNum: params.page,
    pageSize: params.size,
    jobId: params.jobId,
    status: toBackendLogStatus(params.status),
    startTime: params.startTime,
    endTime: params.endTime,
  })
  return {
    records: (page?.records ?? []).map((item) => ({
      id: getOptionalNumber(item, 'id', 'logId'),
      jobId: getOptionalNumber(item, 'jobId') ?? 0,
      jobName: getString(item, 'jobName', ''),
      status: toFrontLogStatus(getString(item, 'executionStatus')),
      startTime: getString(item, 'startTime', ''),
      stopTime: getString(item, 'endTime', ''),
      rowsRead: getOptionalNumber(item, 'extractedRows'),
      rowsWritten: getOptionalNumber(item, 'loadedRows'),
      errorMsg: getString(item, 'errorMessage', ''),
      detail: getString(item, 'executionDetail', ''),
    })),
    total: page?.total ?? 0,
    current: page?.current ?? params.page ?? 1,
    size: page?.size ?? params.size ?? 10,
  }
}

/** 获取日志详情 */
export async function getEtlLog(id: number): Promise<EtlLog> {
  const data = await get<RawRecord>(`/etl/logs/${id}`)
  return {
    id: getOptionalNumber(data ?? {}, 'id', 'logId'),
    jobId: getOptionalNumber(data ?? {}, 'jobId') ?? 0,
    jobName: getString(data ?? {}, 'jobName', ''),
    status: toFrontLogStatus(getString(data ?? {}, 'executionStatus')),
    startTime: getString(data ?? {}, 'startTime', ''),
    stopTime: getString(data ?? {}, 'endTime', ''),
    rowsRead: getOptionalNumber(data ?? {}, 'extractedRows'),
    rowsWritten: getOptionalNumber(data ?? {}, 'loadedRows'),
    errorMsg: getString(data ?? {}, 'errorMessage', ''),
    detail: getString(data ?? {}, 'executionDetail', ''),
  }
}

/** 清空日志 */
export function cleanEtlLogs(): Promise<void> {
  return del('/etl/logs/clean')
}
