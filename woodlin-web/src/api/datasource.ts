/**
 * @file api/datasource.ts
 * @description 多数据源管理：CRUD + 测试连接 + 元数据浏览接口（对齐后端 /admin/infra/datasource）
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

const BASE = '/admin/infra/datasource'

/** 数据源 */
export interface DataSource {
  /** 兼容旧前端字段：用于行 key */
  id?: number
  /** 后端唯一编码，作为 RESTful 主键 */
  datasourceCode?: string
  dsName: string
  /** MySQL / PostgreSQL / Oracle */
  dsType: string
  host: string
  port: number
  dbName: string
  username: string
  password?: string
  status?: string
  remark?: string
  createTime?: string
}

/** 数据源查询参数 */
export interface DataSourceQuery {
  page?: number
  size?: number
  dsName?: string
  dsType?: string
}

/** 测试连接结果 */
export interface ConnectionTestResult {
  success: boolean
  message: string
}

function resolveCode(idOrCode: number | string | DataSource): string {
  if (typeof idOrCode === 'number' || typeof idOrCode === 'string') {
    return String(idOrCode)
  }
  return String(idOrCode.datasourceCode ?? idOrCode.id ?? '')
}

/**
 * 查询数据源列表
 * 注：后端 `/admin/infra/datasource` 直接返回 List，前端这里包装成 PageResult 以保持调用方兼容。
 */
export async function pageDataSources(_params: DataSourceQuery): Promise<PageResult<DataSource>> {
  const list = await get<DataSource[]>(BASE)
  const records = list ?? []
  return {
    records,
    total: records.length,
    size: records.length,
    current: 1,
  }
}

/** 新增数据源 */
export function createDataSource(data: DataSource): Promise<void> {
  return post(BASE, data)
}

/** 更新数据源（后端从 body 读取主键 datasourceCode） */
export function updateDataSource(_idOrCode: number | string, data: DataSource): Promise<void> {
  return put(BASE, data)
}

/** 删除数据源 */
export function deleteDataSource(idOrCode: number | string): Promise<void> {
  return del(`${BASE}/${encodeURIComponent(resolveCode(idOrCode))}`)
}

/**
 * 测试数据源连接（后端为 POST /test，body 为完整配置）
 * 兼容旧调用方：传入 id/code 时返回基于已存在配置的“占位”测试结果。
 */
export async function testDataSource(
  idOrConfig: number | string | DataSource,
): Promise<ConnectionTestResult> {
  let payload: Record<string, unknown>
  if (typeof idOrConfig === 'object') {
    payload = idOrConfig as unknown as Record<string, unknown>
  } else {
    payload = { datasourceCode: String(idOrConfig) }
  }
  try {
    await post(`${BASE}/test`, payload)
    return { success: true, message: '连接成功' }
  } catch (e) {
    return { success: false, message: (e as Error)?.message || '连接失败' }
  }
}

/** 获取数据源表列表（后端返回 TableMetadata[]，这里转换为表名字符串数组以兼容旧调用方） */
export async function listTables(idOrCode: number | string, schemaName?: string): Promise<string[]> {
  const tables = await get<Array<{ tableName?: string; name?: string }>>(`${BASE}/tables`, {
    code: resolveCode(idOrCode),
    ...(schemaName ? { schemaName } : {}),
  })
  return (tables ?? []).map((t) => t.tableName ?? t.name ?? '').filter(Boolean)
}
