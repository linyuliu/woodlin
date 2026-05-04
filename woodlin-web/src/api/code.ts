/**
 * @file api/code.ts
 * @description 代码生成：表/字段元数据 + 模板预览 + 下载 zip + 直接导入项目
 * @author yulin
 * @since 2026-01-01
 */
import service, { get, post } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 业务表 */
export interface GenTable {
  tableName: string
  tableComment?: string
  createTime?: string
  updateTime?: string
}

/** 表查询参数 */
export interface GenTableQuery {
  page?: number
  size?: number
  tableName?: string
  tableComment?: string
  dataSourceId?: number
}

/** 表字段 */
export interface GenColumn {
  columnName: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField?: string
  isPk?: string
  isRequired?: string
  isInsert?: string
  isEdit?: string
  isList?: string
  isQuery?: string
  queryType?: string
  htmlType?: string
}

/** 模板文件 */
export interface TemplateFile {
  name: string
  content: string
}

/** 预览返回 */
export interface PreviewResult {
  templateFiles: TemplateFile[]
}

/** 生成配置（提交给 preview/download/import 的统一 body） */
export interface GenConfig {
  tableName: string
  dataSourceId?: number
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  author?: string
  [key: string]: unknown
}

/** 分页查询业务表 */
export function pageTables(params: GenTableQuery): Promise<PageResult<GenTable>> {
  return get('/gen/table', params as Record<string, unknown>)
}

/** 获取表字段 */
export function getColumns(tableName: string, dataSourceId?: number): Promise<GenColumn[]> {
  return get(`/gen/column/${tableName}`, { dataSourceId })
}

/** 预览模板 */
export function previewCode(data: GenConfig): Promise<PreviewResult> {
  return post('/gen/preview', data)
}

/** 下载代码 zip */
export async function downloadCode(data: GenConfig): Promise<Blob> {
  const res = await service.post('/gen/download', data, { responseType: 'blob' })
  const body = res.data
  return body instanceof Blob ? body : new Blob([body])
}

/** 直接导入到当前项目 */
export function importCode(data: GenConfig): Promise<void> {
  return post('/gen/import', data)
}
