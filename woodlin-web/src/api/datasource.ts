/**
 * @file api/datasource.ts
 * @description 多数据源管理：真实对接基础设施数据源接口，并兼容旧前端表单字段
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

interface RawDataSource {
  id?: number
  datasourceCode?: string
  datasourceName?: string
  datasourceType?: string
  driverClass?: string
  jdbcUrl?: string
  username?: string
  password?: string
  status?: number
  remark?: string
  createTime?: string
  testSql?: string
}

interface DataSourceRequest {
  id?: number
  datasourceCode: string
  datasourceName: string
  datasourceType: string
  jdbcUrl: string
  username: string
  password: string
  driverClass?: string
  testSql?: string
  status: number
  remark?: string
}

const DEFAULT_PORTS: Record<string, number> = {
  MySQL: 3306,
  PostgreSQL: 5432,
  Oracle: 1521,
}

const DRIVER_CLASS: Record<string, string> = {
  MySQL: 'com.mysql.cj.jdbc.Driver',
  PostgreSQL: 'org.postgresql.Driver',
  Oracle: 'oracle.jdbc.OracleDriver',
}

function normalizeType(value?: string): string {
  const upper = String(value ?? '').toUpperCase()
  if (upper === 'POSTGRESQL' || upper === 'PG') {
    return 'PostgreSQL'
  }
  if (upper === 'ORACLE') {
    return 'Oracle'
  }
  return 'MySQL'
}

function toBackendType(type?: string): string {
  const normalized = normalizeType(type)
  if (normalized === 'PostgreSQL') {
    return 'POSTGRESQL'
  }
  if (normalized === 'Oracle') {
    return 'ORACLE'
  }
  return 'MYSQL'
}

function toFrontStatus(status?: number): string {
  return status === 1 ? '0' : '1'
}

function toBackendStatus(status?: string): number {
  return status === '1' ? 0 : 1
}

function getDefaultPort(type?: string): number {
  return DEFAULT_PORTS[normalizeType(type)] ?? 3306
}

function getDriverClass(type?: string): string {
  return DRIVER_CLASS[normalizeType(type)] ?? DRIVER_CLASS.MySQL
}

function parseJdbcUrl(jdbcUrl?: string, datasourceType?: string): Pick<DataSource, 'host' | 'port' | 'dbName'> {
  const fallback = {
    host: '127.0.0.1',
    port: getDefaultPort(datasourceType),
    dbName: '',
  }
  const url = jdbcUrl?.trim()
  if (!url) {
    return fallback
  }
  const type = normalizeType(datasourceType)
  if (type === 'Oracle') {
    const oracleMatch = url.match(/^jdbc:oracle:[^:]+:@([^:/?]+):(\d+)(?::|\/)([^?]+)$/i)
    if (!oracleMatch) {
      return fallback
    }
    return {
      host: oracleMatch[1],
      port: Number(oracleMatch[2]),
      dbName: oracleMatch[3],
    }
  }
  const match = url.match(/^jdbc:[^:]+:\/\/([^:/?]+)(?::(\d+))?\/([^?;]+)/i)
  if (!match) {
    return fallback
  }
  return {
    host: match[1],
    port: match[2] ? Number(match[2]) : getDefaultPort(type),
    dbName: match[3],
  }
}

function buildJdbcUrl(data: DataSource): string {
  const type = normalizeType(data.dsType)
  const host = data.host?.trim() || '127.0.0.1'
  const port = data.port || getDefaultPort(type)
  const dbName = data.dbName?.trim() || ''
  if (type === 'PostgreSQL') {
    return `jdbc:postgresql://${host}:${port}/${dbName}`
  }
  if (type === 'Oracle') {
    return `jdbc:oracle:thin:@${host}:${port}:${dbName}`
  }
  return `jdbc:mysql://${host}:${port}/${dbName}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8`
}

function mapDataSource(raw: RawDataSource): DataSource {
  const parsed = parseJdbcUrl(raw.jdbcUrl, raw.datasourceType)
  return {
    id: raw.id,
    datasourceCode: raw.datasourceCode,
    dsName: raw.datasourceName ?? '',
    dsType: normalizeType(raw.datasourceType),
    host: parsed.host,
    port: parsed.port,
    dbName: parsed.dbName,
    username: raw.username ?? '',
    password: raw.password,
    status: toFrontStatus(raw.status),
    remark: raw.remark,
    createTime: raw.createTime,
  }
}

function buildDataSourceCode(data: DataSource): string {
  if (data.datasourceCode?.trim()) {
    return data.datasourceCode.trim()
  }
  const normalized = `${data.dsName}_${data.dbName}`.trim().replace(/[^A-Za-z0-9_]+/g, '_')
  return normalized || `DS_${Date.now()}`
}

function buildRequest(data: DataSource, existing?: RawDataSource): DataSourceRequest {
  const password = data.password?.trim() || existing?.password || ''
  return {
    id: data.id ?? existing?.id,
    datasourceCode: buildDataSourceCode(data),
    datasourceName: data.dsName,
    datasourceType: toBackendType(data.dsType),
    jdbcUrl: buildJdbcUrl(data),
    username: data.username,
    password,
    driverClass: getDriverClass(data.dsType),
    testSql: existing?.testSql,
    status: toBackendStatus(data.status),
    remark: data.remark,
  }
}

async function listRawDataSources(): Promise<RawDataSource[]> {
  const list = await get<RawDataSource[]>(BASE)
  return Array.isArray(list) ? list : []
}

async function getRawDataSourceByCode(code: string): Promise<RawDataSource | undefined> {
  if (!code) {
    return undefined
  }
  return get<RawDataSource>(`${BASE}/${encodeURIComponent(code)}`)
}

function resolveCode(input: number | string | DataSource): string {
  if (typeof input === 'string') {
    return input
  }
  if (typeof input === 'object') {
    return input.datasourceCode ?? String(input.id ?? '')
  }
  return String(input)
}

export async function getDataSourceList(): Promise<DataSource[]> {
  const list = await listRawDataSources()
  return list.map(mapDataSource)
}

export async function getDataSourceById(id: number): Promise<DataSource | undefined> {
  const list = await getDataSourceList()
  return list.find((item) => item.id === id)
}

export async function getDataSourceByCode(code: string): Promise<DataSource | undefined> {
  const raw = await getRawDataSourceByCode(code)
  return raw ? mapDataSource(raw) : undefined
}

/**
 * 查询数据源列表
 * 注：后端无分页，这里做前端过滤与分页兼容。
 */
export async function pageDataSources(params: DataSourceQuery): Promise<PageResult<DataSource>> {
  const pageNum = params.page ?? 1
  const pageSize = params.size ?? 10
  const keyword = params.dsName?.trim().toLowerCase()
  const type = params.dsType?.trim()
  let records = await getDataSourceList()
  if (keyword) {
    records = records.filter((item) => item.dsName.toLowerCase().includes(keyword))
  }
  if (type) {
    records = records.filter((item) => item.dsType === type)
  }
  const total = records.length
  const start = Math.max(pageNum - 1, 0) * pageSize
  return {
    records: records.slice(start, start + pageSize),
    total,
    size: pageSize,
    current: pageNum,
  }
}

/** 新增数据源 */
export function createDataSource(data: DataSource): Promise<void> {
  return post(BASE, buildRequest(data))
}

/** 更新数据源（后端从 body 读取主键 datasourceCode） */
export async function updateDataSource(idOrCode: number | string, data: DataSource): Promise<void> {
  const code = typeof idOrCode === 'string' ? idOrCode : (await getDataSourceById(idOrCode))?.datasourceCode
  const existing = code ? await getRawDataSourceByCode(code) : undefined
  return put(BASE, buildRequest({ ...data, datasourceCode: code ?? data.datasourceCode }, existing))
}

/** 删除数据源 */
export async function deleteDataSource(idOrCode: number | string | DataSource): Promise<void> {
  const code = await resolveDatasourceCode(idOrCode)
  return del(`${BASE}/${encodeURIComponent(code)}`)
}

async function resolveDatasourceCode(idOrCode: number | string | DataSource): Promise<string> {
  if (typeof idOrCode === 'string' && idOrCode) {
    return idOrCode
  }
  if (typeof idOrCode === 'object' && idOrCode.datasourceCode) {
    return idOrCode.datasourceCode
  }
  const id = typeof idOrCode === 'number' ? idOrCode : Number((idOrCode as DataSource).id)
  const datasource = await getDataSourceById(id)
  return datasource?.datasourceCode ?? resolveCode(idOrCode)
}

/**
 * 测试数据源连接
 */
export async function testDataSource(idOrConfig: number | string | DataSource): Promise<ConnectionTestResult> {
  try {
    if (typeof idOrConfig === 'object') {
      await post(`${BASE}/test`, buildRequest(idOrConfig))
    } else {
      const code = await resolveDatasourceCode(idOrConfig)
      const existing = await getRawDataSourceByCode(code)
      if (!existing) {
        throw new Error('数据源不存在')
      }
      await post(`${BASE}/test`, {
        id: existing.id,
        datasourceCode: existing.datasourceCode,
        datasourceName: existing.datasourceName,
        datasourceType: existing.datasourceType,
        jdbcUrl: existing.jdbcUrl,
        username: existing.username,
        password: existing.password,
        driverClass: existing.driverClass,
        testSql: existing.testSql,
        status: existing.status,
        remark: existing.remark,
      })
    }
    return { success: true, message: '连接成功' }
  } catch (error) {
    return {
      success: false,
      message: error instanceof Error ? error.message : '连接失败',
    }
  }
}

/** 获取数据源表列表 */
export async function listTables(idOrCode: number | string | DataSource, schemaName?: string): Promise<string[]> {
  const code = await resolveDatasourceCode(idOrCode)
  const tables = await get<Array<{ tableName?: string; name?: string }>>(`${BASE}/tables`, {
    code,
    ...(schemaName ? { schemaName } : {}),
  })
  return (tables ?? []).map((item) => item.tableName ?? item.name ?? '').filter(Boolean)
}
