/**
 * 文件管理API服务
 * 
 * @author mumu
 * @description 文件上传、管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 文件信息
 */
export interface FileInfo {
  fileId?: number
  fileName: string
  fileSize: number
  fileMd5?: string
  fileType?: string
  fileUrl?: string
  storageType?: string
  createTime?: string
  updateTime?: string
}

/**
 * 文件分页查询参数
 */
export interface FilePageParams {
  pageNum?: number
  pageSize?: number
  fileName?: string
  fileType?: string
  storageType?: string
}

/**
 * 上传令牌请求
 */
export interface UploadTokenRequest {
  fileName: string
  fileSize: number
  fileMd5: string
  chunkSize?: number
}

/**
 * 上传令牌响应
 */
export interface UploadTokenResponse {
  token: string
  uploadId: string
  expiresIn: number
}

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
  fileId: number
  fileName: string
  fileUrl: string
  fileMd5: string
}

/**
 * 获取文件上传令牌
 * @param data 上传令牌请求
 */
export function getUploadToken(data: UploadTokenRequest): Promise<UploadTokenResponse> {
  return request.post('/file/upload/token', data)
}

/**
 * 上传文件
 * @param file 文件对象
 */
export function uploadFile(file: File): Promise<FileUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 快速上传（秒传）
 * @param md5 文件MD5
 */
export function quickUpload(md5: string): Promise<FileUploadResponse> {
  return request.post('/file/upload/quick', null, {
    params: { md5 }
  })
}

/**
 * 检查文件是否已存在
 * @param md5 文件MD5
 */
export function checkFileExists(md5: string): Promise<boolean> {
  return request.get(`/file/upload/check/${md5}`)
}

/**
 * 分页查询文件列表
 * @param params 查询参数
 */
export function getFileList(params: FilePageParams) {
  return request.get('/file/manage/page', { params })
}

/**
 * 搜索文件
 * @param keyword 搜索关键词
 */
export function searchFiles(keyword: string) {
  return request.get('/file/manage/search', {
    params: { keyword }
  })
}

/**
 * 根据文件ID获取文件信息
 * @param fileId 文件ID
 */
export function getFileById(fileId: number): Promise<FileInfo> {
  return request.get(`/file/manage/${fileId}`)
}

/**
 * 获取文件下载URL
 * @param fileId 文件ID
 */
export function getFileDownloadUrl(fileId: number): Promise<string> {
  return request.get(`/file/manage/${fileId}/url`)
}

/**
 * 下载文件
 * @param fileId 文件ID
 */
export function downloadFile(fileId: number) {
  return request.get(`/file/manage/${fileId}/download`, {
    responseType: 'blob'
  })
}

/**
 * 批量删除文件
 * @param fileIds 文件ID数组
 */
export function batchDeleteFiles(fileIds: number[]): Promise<void> {
  return request.delete('/file/manage/batch', {
    data: fileIds
  })
}

/**
 * 删除文件
 * @param fileId 文件ID
 */
export function deleteFile(fileId: number): Promise<void> {
  return request.delete(`/file/manage/${fileId}`)
}
