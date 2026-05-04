/**
 * @file api/system/notice.ts
 * @description 通知公告 API
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 通知公告 */
export interface SysNotice {
  id?: number
  noticeTitle: string
  /** 1=通知 2=公告 */
  noticeType: string
  noticeContent?: string
  status?: string
  createBy?: string
  createTime?: string
}

/** 通知公告查询参数 */
export interface NoticeQuery {
  page?: number
  size?: number
  noticeTitle?: string
  noticeType?: string
  status?: string
}

/** 分页查询通知公告 */
export function pageNotices(params: NoticeQuery): Promise<PageResult<SysNotice>> {
  return get('/system/notice', params as Record<string, unknown>)
}

/** 新增通知公告 */
export function createNotice(data: SysNotice): Promise<void> {
  return post('/system/notice', data)
}

/** 更新通知公告 */
export function updateNotice(id: number, data: SysNotice): Promise<void> {
  return put(`/system/notice/${id}`, data)
}

/** 删除通知公告 */
export function deleteNotice(id: number): Promise<void> {
  return del(`/system/notice/${id}`)
}

/** 标记通知为已读 */
export function readNotice(id: number): Promise<void> {
  return put(`/system/notice/${id}/read`)
}
