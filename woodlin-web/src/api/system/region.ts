/**
 * @file api/system/region.ts
 * @description 区域 / 行政区划 API（只读）
 * @author yulin
 * @since 2026-01-01
 */
import { get } from '@/utils/request'

/** 行政区划节点 */
export interface SysRegion {
  id?: number
  parentId?: number
  /** 区划编码 */
  code: string
  /** 区划名称 */
  name: string
  /** 等级：1=省 2=市 3=区/县 4=乡/镇 5=村 */
  level?: number
  shortName?: string
  pinyin?: string
  children?: SysRegion[]
}

type RawRecord = Record<string, unknown>

function getString(raw: RawRecord, key: string, fallback = ''): string {
  const value = raw[key]
  return value === undefined || value === null ? fallback : String(value)
}

function getOptionalString(raw: RawRecord, key: string): string | undefined {
  const value = raw[key]
  return value === undefined || value === null ? undefined : String(value)
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

function mapRegion(raw: RawRecord): SysRegion {
  return {
    id: getOptionalNumber(raw, 'id', 'regionId'),
    parentId: getOptionalNumber(raw, 'parentId'),
    code: getOptionalString(raw, 'code') ?? getString(raw, 'regionCode'),
    name: getOptionalString(raw, 'name') ?? getString(raw, 'regionName'),
    level: getOptionalNumber(raw, 'level', 'regionLevel'),
    shortName: getOptionalString(raw, 'shortName'),
    pinyin: getOptionalString(raw, 'pinyin'),
    children: Array.isArray(raw.children) ? raw.children.map((item) => mapRegion(item as RawRecord)) : undefined,
  }
}

/** 获取区域树 */
export async function getRegionTree(): Promise<SysRegion[]> {
  const data = await get<RawRecord[]>('/system/region/tree')
  return Array.isArray(data) ? data.map(mapRegion) : []
}
