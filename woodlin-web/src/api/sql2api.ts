/**
 * @file api/sql2api.ts
 * @description SQL2API：SQL 模板转 REST API 的 CRUD 与调试接口
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

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

/** 分页查询 */
export function pageSql2Apis(params: Sql2ApiQuery): Promise<PageResult<Sql2Api>> {
  return get('/sql2api', params as Record<string, unknown>)
}

/** 新增 */
export function createSql2Api(data: Sql2Api): Promise<void> {
  return post('/sql2api', data)
}

/** 更新 */
export function updateSql2Api(id: number, data: Sql2Api): Promise<void> {
  return put(`/sql2api/${id}`, data)
}

/** 删除 */
export function deleteSql2Api(id: number): Promise<void> {
  return del(`/sql2api/${id}`)
}

/** 测试 API（提交样例参数） */
export function testSql2Api(id: number, params: Record<string, unknown>): Promise<unknown> {
  return post(`/sql2api/${id}/test`, params)
}
