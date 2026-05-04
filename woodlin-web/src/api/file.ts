/**
 * @file api/file.ts
 * @description 文件管理：上传/下载/分享/删除 + 存储后端 CRUD/测试连接/设为默认
 * @author yulin
 * @since 2026-01-01
 */
import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 文件信息 */
export interface FileInfo {
  id?: number
  fileId?: string
  fileName: string
  fileType?: string
  fileSize?: number
  fileUrl?: string
  storageId?: number
  storageName?: string
  contentType?: string
  createTime?: string
}

/** 文件查询参数 */
export interface FileQuery {
  page?: number
  size?: number
  fileName?: string
  fileType?: string
  storageId?: number
}

/** 文件上传响应 */
export interface FileUploadResult {
  fileId: string
  fileName: string
  fileUrl: string
  fileSize: number
  fileType: string
}

/** 分享响应 */
export interface FileShareResult {
  shareUrl: string
}

/** 存储后端配置 */
export interface StorageConfig {
  id?: number
  storageName: string
  /** LOCAL / MINIO / OSS / COS / OBS */
  storageType: string
  endpoint?: string
  bucket?: string
  accessKey?: string
  secretKey?: string
  region?: string
  basePath?: string
  status?: string
  isDefault?: boolean
  remark?: string
  createTime?: string
}

/** 存储查询参数 */
export interface StorageQuery {
  page?: number
  size?: number
  storageName?: string
  storageType?: string
  status?: string
}

/** 测试连接结果 */
export interface ConnectionTestResult {
  success: boolean
  message: string
}

/** 分页查询文件 */
export function pageFiles(params: FileQuery): Promise<PageResult<FileInfo>> {
  return get('/file', params as Record<string, unknown>)
}

/** 删除文件 */
export function deleteFile(id: number | string): Promise<void> {
  return del(`/file/${id}`)
}

/** 生成分享链接 */
export function shareFile(id: number | string, expireDays: number): Promise<FileShareResult> {
  return post(`/file/${id}/share`, { expireDays })
}

/** 文件下载地址 */
export function getDownloadUrl(id: number | string): string {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${base}/file/${id}/download`
}

/** 文件上传地址 */
export function getUploadUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${base}/file/upload`
}

/** 分页查询存储 */
export function pageStorages(params: StorageQuery): Promise<PageResult<StorageConfig>> {
  return get('/file/storage', params as Record<string, unknown>)
}

/** 新增存储 */
export function createStorage(data: StorageConfig): Promise<void> {
  return post('/file/storage', data)
}

/** 更新存储 */
export function updateStorage(id: number, data: StorageConfig): Promise<void> {
  return put(`/file/storage/${id}`, data)
}

/** 删除存储 */
export function deleteStorage(id: number): Promise<void> {
  return del(`/file/storage/${id}`)
}

/** 设为默认 */
export function setDefaultStorage(id: number): Promise<void> {
  return put(`/file/storage/${id}/default`)
}

/** 测试存储连接 */
export function testStorage(id: number): Promise<ConnectionTestResult> {
  return post(`/file/storage/${id}/test`)
}
