/**
 * 数据源管理 API
 *
 * @author mumu
 * @since 2025-01-01
 */

import request from '@/utils/request'

export interface DatasourceConfig {
  id?: number
  datasourceCode: string
  datasourceName: string
  datasourceType: string
  driverClass?: string
  jdbcUrl: string
  username: string
  password: string
  testSql?: string
  status?: number
  owner?: string
  bizTags?: string
  remark?: string
  extConfig?: string
  createTime?: string
  updateTime?: string
}

export interface DatabaseMetadata {
  databaseName?: string
  databaseProductName?: string
  databaseProductVersion?: string
  majorVersion?: number
  minorVersion?: number
  driverName?: string
  driverVersion?: string
  supportsSchemas?: boolean
  schemas?: SchemaMetadata[]
  charset?: string
  collation?: string
}

export interface SchemaMetadata {
  schemaName: string
  databaseName?: string
  comment?: string
  tables?: TableMetadata[]
}

export interface TableMetadata {
  tableName: string
  schemaName?: string
  databaseName?: string
  comment?: string
  tableType?: string
  primaryKey?: string
  columns?: ColumnMetadata[]
  createTime?: string
  updateTime?: string
  engine?: string
  charset?: string
  collation?: string
}

export interface ColumnMetadata {
  columnName: string
  tableName?: string
  schemaName?: string
  databaseName?: string
  comment?: string
  dataType?: string
  jdbcType?: number
  columnSize?: number
  decimalDigits?: number
  nullable?: boolean
  defaultValue?: string
  primaryKey?: boolean
  autoIncrement?: boolean
  ordinalPosition?: number
  javaType?: string
}

export interface MetadataCacheInfo {
  cacheKey: string
  scope: string
  updatedAt: number
  expiresAt: number
  expired: boolean
}

export function getDatasourceList(): Promise<DatasourceConfig[]> {
  return request({
    url: '/admin/infra/datasource',
    method: 'get'
  }) as Promise<DatasourceConfig[]>
}

export function getDatasourceByCode(code: string): Promise<DatasourceConfig> {
  return request({
    url: `/admin/infra/datasource/${code}`,
    method: 'get'
  }) as Promise<DatasourceConfig>
}

export function addDatasource(data: DatasourceConfig): Promise<void> {
  return request({
    url: '/admin/infra/datasource',
    method: 'post',
    data
  }) as Promise<void>
}

export function updateDatasource(data: DatasourceConfig): Promise<void> {
  return request({
    url: '/admin/infra/datasource',
    method: 'put',
    data
  }) as Promise<void>
}

export function deleteDatasource(code: string): Promise<void> {
  return request({
    url: `/admin/infra/datasource/${code}`,
    method: 'delete'
  }) as Promise<void>
}

export function testDatasource(data: DatasourceConfig): Promise<void> {
  return request({
    url: '/admin/infra/datasource/test',
    method: 'post',
    data
  }) as Promise<void>
}

export function getDatasourceMetadata(code: string, refresh = false): Promise<DatabaseMetadata> {
  return request({
    url: '/admin/infra/datasource/metadata',
    method: 'get',
    params: { code, refresh }
  })
}

export function getDatasourceSchemas(code: string, refresh = false): Promise<SchemaMetadata[]> {
  return request({
    url: '/admin/infra/datasource/schemas',
    method: 'get',
    params: { code, refresh }
  })
}

export function getDatasourceTables(code: string, schemaName?: string, refresh = false): Promise<TableMetadata[]> {
  return request({
    url: '/admin/infra/datasource/tables',
    method: 'get',
    params: { code, schemaName, refresh }
  })
}

export function getTableColumns(code: string, table: string, schemaName?: string, refresh = false): Promise<ColumnMetadata[]> {
  return request({
    url: '/admin/infra/datasource/columns',
    method: 'get',
    params: { code, table, schemaName, refresh }
  })
}

export function refreshDatasourceCache(code: string): Promise<void> {
  return request({
    url: '/admin/infra/datasource/cache/refresh',
    method: 'post',
    params: { code }
  }) as Promise<void>
}

export function getDatasourceCacheInfo(code: string): Promise<MetadataCacheInfo[]> {
  return request({
    url: '/admin/infra/datasource/cache/info',
    method: 'get',
    params: { code }
  }) as Promise<MetadataCacheInfo[]>
}
