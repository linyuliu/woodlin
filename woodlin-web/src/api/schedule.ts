/**
 * @file api/schedule.ts
 * @description 任务调度：定时任务 CRUD + 暂停/恢复/立即执行 + 调度日志查询/清空
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

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

/** 分页查询任务 */
export function pageJobs(params: ScheduleJobQuery): Promise<PageResult<ScheduleJob>> {
  return get('/schedule/job', params as Record<string, unknown>)
}

/** 新增任务 */
export function createJob(data: ScheduleJob): Promise<void> {
  return post('/schedule/job', data)
}

/** 更新任务 */
export function updateJob(data: ScheduleJob): Promise<void> {
  return put('/schedule/job', data)
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
export function pageLogs(params: ScheduleLogQuery): Promise<PageResult<ScheduleLog>> {
  return get('/schedule/log', params as Record<string, unknown>)
}

/** 删除单条日志 */
export function deleteLog(id: number): Promise<void> {
  return del(`/schedule/log/${id}`)
}

/** 清空全部日志 */
export function cleanLogs(): Promise<void> {
  return del('/schedule/log/clean')
}
