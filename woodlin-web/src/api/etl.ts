/**
 * @file api/etl.ts
 * @description ETL 数据集成：离线作业 CRUD + 立即运行 + 数据预览 + 执行日志
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

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

/** 分页查询离线作业 */
export function pageOfflineJobs(
  params: EtlOfflineJobQuery,
): Promise<PageResult<EtlOfflineJob>> {
  return get('/etl/offline', params as Record<string, unknown>)
}

/** 新增离线作业 */
export function createOfflineJob(data: EtlOfflineJob): Promise<void> {
  return post('/etl/offline', data)
}

/** 更新离线作业 */
export function updateOfflineJob(id: number, data: EtlOfflineJob): Promise<void> {
  return put(`/etl/offline/${id}`, data)
}

/** 删除离线作业 */
export function deleteOfflineJob(id: number): Promise<void> {
  return del(`/etl/offline/${id}`)
}

/** 立即执行作业 */
export function runOfflineJob(id: number): Promise<void> {
  return post(`/etl/offline/${id}/run`)
}

/** 预览样例数据 */
export function previewOfflineJob(id: number): Promise<Record<string, unknown>[]> {
  return get(`/etl/offline/${id}/preview`)
}

/** 分页查询执行日志 */
export function pageEtlLogs(params: EtlLogQuery): Promise<PageResult<EtlLog>> {
  return get('/etl/log', params as Record<string, unknown>)
}

/** 获取日志详情 */
export function getEtlLog(id: number): Promise<EtlLog> {
  return get(`/etl/log/${id}`)
}

/** 清空日志 */
export function cleanEtlLogs(): Promise<void> {
  return del('/etl/log/clean')
}
