/**
 * @file api/system/notice.ts
 * @description 通知公告 API
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
  toPageParams,
  type RawRecord,
} from '@/api/_utils'

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

function mapNotice(raw: RawRecord): SysNotice {
  return {
    id: getOptionalNumber(raw, 'id', 'noticeId'),
    noticeTitle: getString(raw, 'noticeTitle'),
    noticeType: getString(raw, 'noticeType', '1'),
    noticeContent: getOptionalString(raw, 'noticeContent'),
    status: getOptionalString(raw, 'status'),
    createBy: getOptionalString(raw, 'createBy'),
    createTime: getOptionalString(raw, 'createTime'),
  }
}

function toBackendNotice(data: SysNotice): Record<string, unknown> {
  const payload: Record<string, unknown> = {
    noticeTitle: data.noticeTitle,
    noticeType: data.noticeType,
    noticeContent: data.noticeContent,
    status: data.status,
  }
  if (data.id !== undefined) {
    payload.noticeId = data.id
  }
  return payload
}

/** 分页查询通知公告 */
export async function pageNotices(params: NoticeQuery): Promise<PageResult<SysNotice>> {
  const page = await get<PageResult<RawRecord>>('/system/notice', {
    noticeTitle: params.noticeTitle,
    noticeType: params.noticeType,
    status: params.status,
    ...toPageParams(params),
  })
  return normalizePageResult(page, mapNotice, params.page ?? 1, params.size ?? 10)
}

/** 新增通知公告 */
export function createNotice(data: SysNotice): Promise<void> {
  return post('/system/notice', toBackendNotice(data))
}

/** 更新通知公告 */
export function updateNotice(id: number, data: SysNotice): Promise<void> {
  return put(`/system/notice/${id}`, toBackendNotice(data))
}

/** 删除通知公告 */
export function deleteNotice(id: number): Promise<void> {
  return del(`/system/notice/${id}`)
}

/** 标记通知为已读 */
export function readNotice(id: number): Promise<void> {
  return put(`/system/notice/${id}/read`)
}
