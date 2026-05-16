/**
 * @file api/_utils.ts
 * @description 前后端字段对齐的轻量 mapper，集中处理分页、ID、状态和基础类型转换
 * @author yulin
 * @since 2026-05-06
 */
import type { PageResult } from '@/types/global'

export type RawRecord = Record<string, unknown>

export function getOptionalString(raw: RawRecord | undefined, ...keys: string[]): string | undefined {
  if (!raw) {
    return undefined
  }
  for (const key of keys) {
    const value = raw[key]
    if (value !== undefined && value !== null && String(value) !== '') {
      return String(value)
    }
  }
  return undefined
}

export function getString(raw: RawRecord | undefined, key: string, fallback = ''): string {
  return getOptionalString(raw, key) ?? fallback
}

export function getOptionalNumber(raw: RawRecord | undefined, ...keys: string[]): number | undefined {
  if (!raw) {
    return undefined
  }
  for (const key of keys) {
    const value = raw[key]
    if (value !== undefined && value !== null && String(value) !== '') {
      return Number(value)
    }
  }
  return undefined
}

export function getNumber(raw: RawRecord | undefined, keys: string[], fallback = 0): number {
  return getOptionalNumber(raw, ...keys) ?? fallback
}

function getPageNumber(value: unknown, fallback: number): number {
  return typeof value === 'number' ? value : fallback
}

export function normalizePageResult<T>(
  page: Partial<PageResult<RawRecord>> | undefined,
  mapper: (item: RawRecord) => T,
  fallbackPage = 1,
  fallbackSize = 10,
): PageResult<T> {
  const records = Array.isArray(page?.records) ? page.records : []
  return {
    records: records.map(mapper),
    total: getPageNumber(page?.total, 0),
    current: getPageNumber(page?.current, fallbackPage),
    size: getPageNumber(page?.size, fallbackSize),
  }
}

export function toPageParams(params: { page?: number; size?: number }): { pageNum?: number; pageSize?: number } {
  return {
    pageNum: params.page,
    pageSize: params.size,
  }
}

export function toStringStatus(value: unknown, fallback = ''): string {
  return value === undefined || value === null ? fallback : String(value)
}
