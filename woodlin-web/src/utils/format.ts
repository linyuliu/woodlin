/**
 * @file utils/format.ts
 * @description 时间、字节大小等格式化工具
 * @author yulin
 * @since 2026-05-04
 */
import dayjs from 'dayjs'

/** 格式化日期 */
export function formatDate(value: string | number | Date | null | undefined, pattern = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!value) return '-'
  return dayjs(value).format(pattern)
}

/** 格式化字节大小 */
export function formatSize(bytes: number): string {
  if (!bytes && bytes !== 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(2)} ${units[i]}`
}

/** 字符串脱敏（保留头尾若干位） */
export function maskString(value: string | null | undefined, head = 3, tail = 4): string {
  if (!value) return ''
  if (value.length <= head + tail) return value
  return `${value.slice(0, head)}****${value.slice(-tail)}`
}
