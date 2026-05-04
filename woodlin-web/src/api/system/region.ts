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

/** 获取区域树 */
export function getRegionTree(): Promise<SysRegion[]> {
  return get('/system/region/tree')
}
