import request from '@/utils/request'

export type SyncMode = 'FULL' | 'INCREMENTAL'

export interface EtlJob {
  jobId?: number
  jobName: string
  jobGroup?: string
  jobDescription?: string
  sourceDatasource: string
  sourceTable?: string
  sourceSchema?: string
  sourceQuery?: string
  targetDatasource: string
  targetTable: string
  targetSchema?: string
  syncMode: SyncMode
  incrementalColumn?: string
  columnMapping?: string
  transformRules?: string
  filterCondition?: string
  batchSize?: number
  cronExpression?: string
  status?: string
  concurrent?: string
  retryCount?: number
  retryInterval?: number
  nextExecuteTime?: string
  lastExecuteTime?: string
  lastExecuteStatus?: string
  tenantId?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

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

export function getEtlJobList(): Promise<EtlJob[]> {
  return request({
    url: '/etl/jobs',
    method: 'get',
  }) as Promise<EtlJob[]>
}

export function getEtlJobById(jobId: number | string): Promise<EtlJob> {
  return request({
    url: `/etl/jobs/${jobId}`,
    method: 'get',
  }) as Promise<EtlJob>
}

export function createEtlJob(data: EtlJob): Promise<boolean> {
  return request({
    url: '/etl/jobs',
    method: 'post',
    data,
  }) as Promise<boolean>
}

export function updateEtlJob(jobId: number | string, data: EtlJob): Promise<boolean> {
  return request({
    url: `/etl/jobs/${jobId}`,
    method: 'put',
    data,
  }) as Promise<boolean>
}

export function deleteEtlJob(jobId: number | string): Promise<boolean> {
  return request({
    url: `/etl/jobs/${jobId}`,
    method: 'delete',
  }) as Promise<boolean>
}

export function enableEtlJob(jobId: number | string): Promise<boolean> {
  return request({
    url: `/etl/jobs/${jobId}/enable`,
    method: 'post',
  }) as Promise<boolean>
}

export function disableEtlJob(jobId: number | string): Promise<boolean> {
  return request({
    url: `/etl/jobs/${jobId}/disable`,
    method: 'post',
  }) as Promise<boolean>
}

export function executeEtlJob(jobId: number | string): Promise<boolean> {
  return request({
    url: `/etl/jobs/${jobId}/execute`,
    method: 'post',
  }) as Promise<boolean>
}

export function getEtlExecutionLogList(): Promise<EtlExecutionLog[]> {
  return request({
    url: '/etl/logs',
    method: 'get',
  }) as Promise<EtlExecutionLog[]>
}
