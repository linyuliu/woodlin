/**
 * @file api/datasource.ts
 * @description 多数据源管理：CRUD + 测试连接 + 元数据浏览接口
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 数据源 */
export interface DataSource {
  id?: number
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

/** 分页查询数据源 */
export function pageDataSources(params: DataSourceQuery): Promise<PageResult<DataSource>> {
  return get('/datasource', params as Record<string, unknown>)
}

/** 新增数据源 */
export function createDataSource(data: DataSource): Promise<void> {
  return post('/datasource', data)
}

/** 更新数据源 */
export function updateDataSource(id: number, data: DataSource): Promise<void> {
  return put(`/datasource/${id}`, data)
}

/** 删除数据源 */
export function deleteDataSource(id: number): Promise<void> {
  return del(`/datasource/${id}`)
}

/** 测试数据源连接 */
export function testDataSource(id: number): Promise<ConnectionTestResult> {
  return post(`/datasource/${id}/test`)
}

/** 获取数据源表列表 */
export function listTables(id: number): Promise<string[]> {
  return get(`/datasource/${id}/tables`)
}
