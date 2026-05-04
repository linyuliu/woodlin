/**
 * @file api/system/notice.ts
 * @description 通知公告 API
 * @author yulin
 * @since 2026-05-04
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

export interface SysNotice {
  id?: number
  title: string
  content: string
  type?: string
  status?: string
}

export function pageNotices(params: Record<string, unknown>): Promise<PageResult<SysNotice>> {
  return get('/system/notice/page', params)
}

export function createNotice(data: SysNotice): Promise<void> {
  return post('/system/notice', data)
}

export function updateNotice(data: SysNotice): Promise<void> {
  return put('/system/notice', data)
}

export function deleteNotice(id: number): Promise<void> {
  return del(`/system/notice/${id}`)
}
