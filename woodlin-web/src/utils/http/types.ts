/**
 * HTTP请求类型定义
 * 
 * @author mumu
 * @description 定义HTTP请求相关的TypeScript类型和接口
 * @since 2025-01-01
 */

import type { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

/**
 * HTTP请求方法枚举
 */
export enum RequestMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH',
  HEAD = 'HEAD',
  OPTIONS = 'OPTIONS'
}

/**
 * HTTP响应码枚举
 */
export enum HttpStatusCode {
  /** 请求成功 */
  OK = 200,
  /** 已创建 */
  CREATED = 201,
  /** 已接受 */
  ACCEPTED = 202,
  /** 无内容 */
  NO_CONTENT = 204,
  /** 错误的请求 */
  BAD_REQUEST = 400,
  /** 未授权 */
  UNAUTHORIZED = 401,
  /** 禁止访问 */
  FORBIDDEN = 403,
  /** 未找到 */
  NOT_FOUND = 404,
  /** 请求超时 */
  REQUEST_TIMEOUT = 408,
  /** 服务器错误 */
  INTERNAL_SERVER_ERROR = 500,
  /** 网关错误 */
  BAD_GATEWAY = 502,
  /** 服务不可用 */
  SERVICE_UNAVAILABLE = 503,
  /** 网关超时 */
  GATEWAY_TIMEOUT = 504
}

/**
 * 业务响应码枚举
 */
export enum BusinessCode {
  /** 成功 */
  SUCCESS = 200,
  /** 失败 */
  FAIL = 500,
  /** 未授权 */
  UNAUTHORIZED = 401,
  /** 无权限 */
  FORBIDDEN = 403,
  /** 参数错误 */
  BAD_REQUEST = 400
}

/**
 * 统一响应结构
 * 
 * @template T 响应数据类型
 */
export interface ApiResponse<T = any> {
  /** 业务状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
  /** 时间戳 */
  timestamp?: number
  /** 追踪ID */
  traceId?: string
}

/**
 * 分页请求参数
 */
export interface PageParams {
  /** 当前页码（从1开始） */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
  /** 排序字段 */
  orderBy?: string
  /** 排序方向 */
  sortOrder?: 'asc' | 'desc'
}

/**
 * 分页响应数据
 * 
 * @template T 列表数据类型
 */
export interface PageResult<T = any> {
  /** 数据列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  current: number
  /** 每页数量 */
  size: number
  /** 总页数 */
  pages?: number
}

/**
 * 请求配置扩展
 */
export interface RequestOptions extends AxiosRequestConfig {
  /** 是否显示加载提示 */
  showLoading?: boolean
  /** 是否显示成功提示 */
  showSuccessMsg?: boolean
  /** 是否显示错误提示 */
  showErrorMsg?: boolean
  /** 自定义成功提示消息 */
  successMsg?: string
  /** 自定义错误提示消息 */
  errorMsg?: string
  /** 是否启用请求重试 */
  enableRetry?: boolean
  /** 重试次数 */
  retryCount?: number
  /** 重试延迟（毫秒） */
  retryDelay?: number
  /** 是否需要token认证 */
  requiresAuth?: boolean
  /** 是否忽略重复请求 */
  ignoreDuplicateRequest?: boolean
}

/**
 * 请求拦截器配置
 */
export interface RequestInterceptorConfig {
  /** 请求成功拦截器 */
  onFulfilled?: (config: AxiosRequestConfig) => AxiosRequestConfig | Promise<AxiosRequestConfig>
  /** 请求失败拦截器 */
  onRejected?: (error: any) => any
}

/**
 * 响应拦截器配置
 */
export interface ResponseInterceptorConfig {
  /** 响应成功拦截器 */
  onFulfilled?: (response: AxiosResponse) => AxiosResponse | Promise<AxiosResponse>
  /** 响应失败拦截器 */
  onRejected?: (error: AxiosError) => any
}

/**
 * HTTP错误信息
 */
export interface HttpErrorInfo {
  /** 错误码 */
  code: number
  /** 错误消息 */
  message: string
  /** HTTP状态码 */
  status?: number
  /** 错误详情 */
  details?: any
}
