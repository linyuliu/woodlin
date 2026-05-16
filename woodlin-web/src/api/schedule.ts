/**
 * @file api/schedule.ts
 * @description 任务调度：定时任务 CRUD + 暂停/恢复/立即执行 + 调度日志查询/清空
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'
import {
  getOptionalNumber,
  getOptionalString,
  getString,
  normalizePageResult,
  type RawRecord,
} from '@/api/_utils'

/** 定时任务 */
export interface ScheduleJob {
  id?: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  cronExpression: string
  /** 1=立即触发 2=触发一次 3=不触发 */
  misfirePolicy: string
  /** 1=允许 0=禁止 */
  concurrent: string
  /** 0=暂停 1=正常 */
  status: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 任务查询参数 */
export interface ScheduleJobQuery {
  page?: number
  size?: number
  jobName?: string
  jobGroup?: string
  status?: string
}

/** 调度日志 */
export interface ScheduleLog {
  id?: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  /** 0=成功 1=失败 */
  status: string
  message?: string
  startTime?: string
  stopTime?: string
  /** 耗时(ms) */
  elapsedTime?: number
}

/** 日志查询参数 */
export interface ScheduleLogQuery {
  page?: number
  size?: number
  jobName?: string
  jobGroup?: string
  status?: string
  startTime?: string
  endTime?: string
}

function mapJob(raw: RawRecord): ScheduleJob {
  return {
    id: getOptionalNumber(raw, 'id', 'jobId'),
    jobName: getString(raw, 'jobName'),
    jobGroup: getString(raw, 'jobGroup'),
    invokeTarget: getString(raw, 'invokeTarget'),
    cronExpression: getString(raw, 'cronExpression'),
    misfirePolicy: getString(raw, 'misfirePolicy', '1'),
    concurrent: getString(raw, 'concurrent', '0'),
    status: getString(raw, 'status', '1'),
    remark: getOptionalString(raw, 'remark'),
    createTime: getOptionalString(raw, 'createTime'),
    updateTime: getOptionalString(raw, 'updateTime'),
  }
}

function toBackendJob(data: ScheduleJob): Record<string, unknown> {
  const payload: Record<string, unknown> = {
    jobName: data.jobName,
    jobGroup: data.jobGroup,
    invokeTarget: data.invokeTarget,
    cronExpression: data.cronExpression,
    misfirePolicy: data.misfirePolicy,
    concurrent: data.concurrent,
    status: data.status,
    remark: data.remark,
  }
  if (data.id !== undefined) {
    payload.jobId = data.id
  }
  return payload
}

function mapLog(raw: RawRecord): ScheduleLog {
  return {
    id: getOptionalNumber(raw, 'id', 'logId'),
    jobName: getString(raw, 'jobName'),
    jobGroup: getString(raw, 'jobGroup'),
    invokeTarget: getString(raw, 'invokeTarget'),
    status: getString(raw, 'status'),
    message: getOptionalString(raw, 'message'),
    startTime: getOptionalString(raw, 'startTime'),
    stopTime: getOptionalString(raw, 'stopTime'),
    elapsedTime: getOptionalNumber(raw, 'elapsedTime'),
  }
}

/** 分页查询任务 */
export async function pageJobs(params: ScheduleJobQuery): Promise<PageResult<ScheduleJob>> {
  const page = await get<PageResult<RawRecord>>('/schedule/job', params as Record<string, unknown>)
  return normalizePageResult(page, mapJob, params.page ?? 1, params.size ?? 10)
}

/** 新增任务 */
export function createJob(data: ScheduleJob): Promise<void> {
  return post('/schedule/job', toBackendJob(data))
}

/** 更新任务 */
export function updateJob(data: ScheduleJob): Promise<void> {
  return put('/schedule/job', toBackendJob(data))
}

/** 删除任务 */
export function deleteJob(id: number): Promise<void> {
  return del(`/schedule/job/${id}`)
}

/** 修改任务状态（0=暂停 1=恢复） */
export function changeJobStatus(id: number, status: string): Promise<void> {
  return put(`/schedule/job/${id}/status`, { status })
}

/** 立即执行一次 */
export function runJobOnce(id: number): Promise<void> {
  return post(`/schedule/job/${id}/run`)
}

/** 分页查询日志 */
export async function pageLogs(params: ScheduleLogQuery): Promise<PageResult<ScheduleLog>> {
  const page = await get<PageResult<RawRecord>>('/schedule/log', params as Record<string, unknown>)
  return normalizePageResult(page, mapLog, params.page ?? 1, params.size ?? 10)
}

/** 删除单条日志 */
export function deleteLog(id: number): Promise<void> {
  return del(`/schedule/log/${id}`)
}

/** 清空全部日志 */
export function cleanLogs(): Promise<void> {
  return del('/schedule/log/clean')
}
