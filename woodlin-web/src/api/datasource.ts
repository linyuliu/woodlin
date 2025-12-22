/**
 * 数据源管理API服务
 * 
 * @author mumu
 * @description 基础设施数据源管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 数据源配置
 */
export interface DatasourceConfig {
  id?: number
  code: string
  name: string
  dbType: string
  jdbcUrl: string
  username: string
  password: string
  driverClass?: string
  description?: string
  createTime?: string
  updateTime?: string
}

/**
 * 数据源测试结果
 */
export interface DatasourceTestResult {
  success: boolean
  message: string
  connectionTime?: number
}

/**
 * 数据库元数据
 */
export interface DatabaseMetadata {
  schemas?: string[]
  tables?: TableMetadata[]
  columns?: ColumnMetadata[]
}

/**
 * 表元数据
 */
export interface TableMetadata {
  tableName: string
  tableComment?: string
  tableType?: string
}

/**
 * 列元数据
 */
export interface ColumnMetadata {
  columnName: string
  columnType: string
  columnComment?: string
  nullable?: boolean
  primaryKey?: boolean
}

/**
 * 获取数据源列表
 */
export function getDatasourceList() {
  return request.get('/admin/infra/datasource')
}

/**
 * 根据code获取数据源配置
 * @param code 数据源编码
 */
export function getDatasourceByCode(code: string): Promise<DatasourceConfig> {
  return request.get(`/admin/infra/datasource/${code}`)
}

/**
 * 新增数据源
 * @param data 数据源配置
 */
export function addDatasource(data: DatasourceConfig): Promise<void> {
  return request.post('/admin/infra/datasource', data)
}

/**
 * 更新数据源
 * @param data 数据源配置
 */
export function updateDatasource(data: DatasourceConfig): Promise<void> {
  return request.put('/admin/infra/datasource', data)
}

/**
 * 删除数据源
 * @param code 数据源编码
 */
export function deleteDatasource(code: string): Promise<void> {
  return request.delete(`/admin/infra/datasource/${code}`)
}

/**
 * 测试数据源连接
 * @param data 数据源配置
 */
export function testDatasource(data: DatasourceConfig): Promise<DatasourceTestResult> {
  return request.post('/admin/infra/datasource/test', data)
}

/**
 * 获取数据源元数据
 * @param code 数据源编码
 */
export function getDatasourceMetadata(code: string): Promise<DatabaseMetadata> {
  return request.get('/admin/infra/datasource/metadata', {
    params: { code }
  })
}

/**
 * 获取数据源schemas
 * @param code 数据源编码
 */
export function getDatasourceSchemas(code: string): Promise<string[]> {
  return request.get('/admin/infra/datasource/schemas', {
    params: { code }
  })
}

/**
 * 获取数据源表列表
 * @param code 数据源编码
 * @param schema schema名称
 */
export function getDatasourceTables(code: string, schema?: string): Promise<TableMetadata[]> {
  return request.get('/admin/infra/datasource/tables', {
    params: { code, schema }
  })
}

/**
 * 获取表列信息
 * @param code 数据源编码
 * @param tableName 表名
 * @param schema schema名称
 */
export function getTableColumns(code: string, tableName: string, schema?: string): Promise<ColumnMetadata[]> {
  return request.get('/admin/infra/datasource/columns', {
    params: { code, tableName, schema }
  })
}
