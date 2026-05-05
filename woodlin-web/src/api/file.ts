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

interface BackendFileInfo {
  fileId?: number
  fileName: string
  fileUrl?: string
  fileSize?: number
  fileType?: string
  mimeType?: string
  detectedMimeType?: string
  storageConfigId?: number
  storageType?: string
  createTime?: string
}

interface BackendStorageConfig {
  configId?: number
  configName: string
  storageType: string
  endpoint?: string
  bucketName?: string
  accessKey?: string
  secretKey?: string
  region?: string
  basePath?: string
  status?: string
  isDefault?: string
  remark?: string
  createTime?: string
}

function normalizeFileType(fileType?: string): string {
  const value = fileType?.toLowerCase() ?? ''
  if (value.startsWith('image/')) {return 'image'}
  if (value.startsWith('video/')) {return 'video'}
  if (value.startsWith('application/')) {return 'doc'}
  return 'other'
}

function mapFile(record: BackendFileInfo): FileInfo {
  const detectedType = record.detectedMimeType || record.mimeType || record.fileType
  return {
    id: record.fileId,
    fileId: String(record.fileId ?? ''),
    fileName: record.fileName,
    fileType: normalizeFileType(detectedType),
    fileSize: record.fileSize,
    fileUrl: record.fileUrl,
    storageId: record.storageConfigId,
    storageName: record.storageType?.toUpperCase(),
    contentType: detectedType,
    createTime: record.createTime,
  }
}

function mapStorage(record: BackendStorageConfig): StorageConfig {
  return {
    id: record.configId,
    storageName: record.configName,
    storageType: record.storageType?.toUpperCase() || 'LOCAL',
    endpoint: record.endpoint,
    bucket: record.bucketName,
    accessKey: record.accessKey,
    secretKey: record.secretKey,
    region: record.region,
    basePath: record.basePath,
    status: record.status,
    isDefault: record.isDefault === '1',
    remark: record.remark,
    createTime: record.createTime,
  }
}

function toBackendStorage(data: StorageConfig): BackendStorageConfig {
  return {
    configId: data.id,
    configName: data.storageName,
    storageType: data.storageType.toLowerCase(),
    endpoint: data.endpoint,
    bucketName: data.bucket,
    accessKey: data.accessKey,
    secretKey: data.secretKey,
    region: data.region,
    basePath: data.basePath,
    status: data.status,
    isDefault: data.isDefault ? '1' : '0',
    remark: data.remark,
  }
}

/** 分页查询文件 */
export function pageFiles(params: FileQuery): Promise<PageResult<FileInfo>> {
  return get<PageResult<BackendFileInfo>>(
    '/file/manage/page',
    {
      current: params.page,
      size: params.size,
      fileName: params.fileName,
      fileType: params.fileType,
      storageId: params.storageId,
    },
  ).then((res) => ({
    ...res,
    records: (res?.records ?? []).map(mapFile),
  }))
}

/** 删除文件 */
export function deleteFile(id: number | string): Promise<void> {
  return del(`/file/manage/${id}`)
}

/** 生成分享链接 */
export function shareFile(id: number | string, expireDays: number): Promise<FileShareResult> {
  return get<string>(`/file/manage/${id}/url`, { expirationTime: expireDays * 24 * 60 * 60 })
    .then((shareUrl) => ({ shareUrl }))
}

/** 文件下载地址 */
export function getDownloadUrl(id: number | string): string {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${base}/file/manage/${id}/download`
}

/** 文件上传地址 */
export function getUploadUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${base}/file/upload/quick?policyCode=default`
}

/** 分页查询存储 */
export function pageStorages(params: StorageQuery): Promise<PageResult<StorageConfig>> {
  return get<PageResult<BackendStorageConfig>>('/file/storage', params as Record<string, unknown>)
    .then((res) => ({
      ...res,
      records: (res?.records ?? []).map(mapStorage),
    }))
}

/** 新增存储 */
export function createStorage(data: StorageConfig): Promise<void> {
  return post('/file/storage', toBackendStorage(data))
}

/** 更新存储 */
export function updateStorage(id: number, data: StorageConfig): Promise<void> {
  return put(`/file/storage/${id}`, toBackendStorage(data))
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
