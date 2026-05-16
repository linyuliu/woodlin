/**
 * @file api/sql2api.ts
 * @description SQL2API：对接真实 sql2api_config 配置表与在线测试接口
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'
import { getDataSourceById, getDataSourceList, type DataSource } from '@/api/datasource'

/** SQL2API 配置 */
export interface Sql2Api {
  id?: number
  apiName: string
  apiPath: string
  /** GET / POST */
  method: string
  dsId: number
  dsName?: string
  sqlTemplate: string
  paramSchema?: string
  resultSchema?: string
  status?: string
  remark?: string
  createTime?: string
}

/** SQL2API 查询参数 */
export interface Sql2ApiQuery {
  page?: number
  size?: number
  apiName?: string
  dsId?: number
  status?: string
}

type RawRecord = Record<string, unknown>

function getString(raw: RawRecord, key: string, fallback = ''): string {
  const value = raw[key]
  return value === undefined || value === null ? fallback : String(value)
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

function mapPageResult<T>(
  page: PageResult<RawRecord> | undefined,
  mapper: (item: RawRecord) => T,
  fallbackPage = 1,
  fallbackSize = 10,
): PageResult<T> {
  const source: Partial<PageResult<RawRecord>> = page ?? {}
  return {
    records: Array.isArray(source.records) ? source.records.map(mapper) : [],
    total: typeof source.total === 'number' ? source.total : 0,
    current: typeof source.current === 'number' ? source.current : fallbackPage,
    size: typeof source.size === 'number' ? source.size : fallbackSize,
  }
}

function toFrontStatus(status?: number): string {
  return status === 1 ? '1' : '0'
}

function toBackendStatus(status?: string): number {
  return status === '1' ? 1 : 0
}

function inferSqlType(sqlTemplate: string): string {
  const normalized = sqlTemplate.trim().split(/\s+/)[0]?.toUpperCase()
  if (normalized === 'INSERT' || normalized === 'UPDATE' || normalized === 'DELETE') {
    return normalized
  }
  return 'SELECT'
}

function inferResultType(sqlTemplate: string): string {
  const normalized = sqlTemplate.toLowerCase()
  if (normalized.includes('limit 1')) {
    return 'single'
  }
  return 'list'
}

function mapSql2Api(raw: RawRecord, datasourcesByCode: Map<string, DataSource>): Sql2Api {
  const datasourceCode = getString(raw, 'datasourceName')
  const datasource = datasourcesByCode.get(datasourceCode)
  return {
    id: getOptionalNumber(raw, 'id', 'apiId'),
    apiName: getString(raw, 'apiName'),
    apiPath: getString(raw, 'apiPath'),
    method: getString(raw, 'httpMethod', 'GET'),
    dsId: datasource?.id ?? 0,
    dsName: datasource?.dsName ?? datasourceCode,
    sqlTemplate: getString(raw, 'sqlContent'),
    paramSchema: getString(raw, 'paramsConfig', ''),
    resultSchema: '',
    status: toFrontStatus(getOptionalNumber(raw, 'status')),
    remark: getString(raw, 'apiDesc', ''),
    createTime: getString(raw, 'createTime', ''),
  }
}

async function toBackendSql2Api(data: Sql2Api): Promise<Record<string, unknown>> {
  const datasource = await getDataSourceById(data.dsId)
  return {
    apiId: data.id,
    apiName: data.apiName,
    apiPath: data.apiPath,
    httpMethod: data.method,
    datasourceName: datasource?.datasourceCode ?? '',
    sqlType: inferSqlType(data.sqlTemplate),
    sqlContent: data.sqlTemplate,
    paramsConfig: data.paramSchema,
    resultType: inferResultType(data.sqlTemplate),
    apiDesc: data.remark,
    enabled: data.status !== '1',
    status: toBackendStatus(data.status),
  }
}

/** 分页查询 */
export async function pageSql2Apis(params: Sql2ApiQuery): Promise<PageResult<Sql2Api>> {
  const datasources = await getDataSourceList()
  const datasourceMap = new Map(datasources.map((item) => [item.datasourceCode ?? '', item]))
  const selectedDatasource = params.dsId ? datasources.find((item) => item.id === params.dsId) : undefined
  const page = await get<PageResult<RawRecord>>('/sql2api', {
    apiName: params.apiName,
    datasourceName: selectedDatasource?.datasourceCode,
    status: params.status === undefined ? undefined : toBackendStatus(params.status),
    pageNum: params.page,
    pageSize: params.size,
  })
  return mapPageResult(page, (item) => mapSql2Api(item, datasourceMap), params.page ?? 1, params.size ?? 10)
}

/** 新增 */
export async function createSql2Api(data: Sql2Api): Promise<void> {
  return post('/sql2api', await toBackendSql2Api(data))
}

/** 更新 */
export async function updateSql2Api(id: number, data: Sql2Api): Promise<void> {
  return put(`/sql2api/${id}`, await toBackendSql2Api(data))
}

/** 删除 */
export function deleteSql2Api(id: number): Promise<void> {
  return del(`/sql2api/${id}`)
}

/** 测试 API（提交样例参数） */
export function testSql2Api(id: number, params: Record<string, unknown>): Promise<unknown> {
  return post(`/sql2api/${id}/test`, params)
}
