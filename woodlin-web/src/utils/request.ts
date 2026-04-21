/**
 * HTTP请求工具模块 - 简化稳定版
 *
 * @author mumu
 * @description 基于axios封装的HTTP请求工具，提供统一的请求拦截、响应处理、错误处理、加密解密等功能
 *              当前暂时停用复杂请求状态管理（去重、全局loading、自动重试），先保证请求链路稳定可观测
 * @since 2025-01-01
 */

import axios, {type AxiosRequestConfig, type InternalAxiosRequestConfig} from 'axios'
import {getConfig} from '@/config'
import {logger} from './logger'

/**
 * 后端统一响应格式
 */
interface ApiResponse<T = unknown> {
  code: number | string  // 支持 number 或 string 类型，以兼容不同的序列化配置
  message: string
  data: T
  timestamp?: string
}

/**
 * 扩展的Axios请求配置
 */
interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  /** 已废弃：管理后台请求链不再接入前端自定义加密 */
  encrypt?: boolean
  /** 已废弃：管理后台请求链不再接入前端自定义解密 */
  decrypt?: boolean
  /** 是否显示加载提示（当前简化模式暂不处理） */
  showLoading?: boolean
  /** 是否显示错误提示 */
  showError?: boolean
  /** 是否重试请求（当前简化模式暂不处理） */
  retry?: boolean
  /** 重试次数（当前简化模式暂不处理） */
  retryCount?: number
  /** 重试延迟（毫秒，当前简化模式暂不处理） */
  retryDelay?: number
  /** 是否忽略token */
  ignoreToken?: boolean
  /** 是否启用重复请求去重（当前简化模式暂不处理） */
  dedupe?: boolean
}

/**
 * 请求追踪配置
 */
type TraceableRequestConfig = InternalAxiosRequestConfig & {
  __traceId?: string
  __requestStartAt?: number
}

const getRequestMethod = (config?: { method?: string }) => (config?.method || 'GET').toUpperCase()
const getRequestUrl = (config?: { url?: string }) => config?.url || ''
const getDuration = (startAt?: number) => (typeof startAt === 'number' ? Date.now() - startAt : -1)

type WrappedResponseData = {
  code?: number | string
  message?: string
  data?: unknown
}

/**
 * 解析业务状态码
 */
function parseBusinessCode(code: unknown): number | null {
  if (typeof code === 'number') {
    return code
  }
  if (typeof code === 'string') {
    const parsed = parseInt(code, 10)
    return Number.isNaN(parsed) ? null : parsed
  }
  return null
}

/**
 * 记录响应结束日志
 */
function logResponseEnd(traceConfig: TraceableRequestConfig, httpStatus: number, businessCode: number | null) {
  logger.info('[HTTP][END]', {
    traceId: traceConfig.__traceId,
    method: getRequestMethod(traceConfig),
    url: getRequestUrl(traceConfig),
    httpStatus,
    businessCode,
    durationMs: getDuration(traceConfig.__requestStartAt)
  })
}

/**
 * 记录业务码异常
 */
function warnNon200BusinessCode(traceConfig: TraceableRequestConfig, data: WrappedResponseData, businessCode: number | null) {
  if (businessCode === null || businessCode === 200) {
    return
  }
  logger.warn('[HTTP][BUSINESS_CODE_NOT_200]', {
    traceId: traceConfig.__traceId,
    method: getRequestMethod(traceConfig),
    url: getRequestUrl(traceConfig),
    businessCode,
    message: data.message
  })
}

/**
 * 兼容两类返回体并提取业务数据
 */
function unwrapResponseData(data: unknown): unknown {
  if (data && typeof data === 'object' && 'data' in data) {
    return (data as WrappedResponseData).data
  }
  return data
}

/**
 * 清理认证本地缓存
 */
function clearAuthLocalStorage() {
  const config = getConfig()
  localStorage.removeItem(config.http.tokenKey)
  localStorage.removeItem(`${config.http.tokenKey}_expire`)
  localStorage.removeItem('tenantId')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userPermissions')
  localStorage.removeItem('userRoles')
  localStorage.removeItem('routesGenerated')
  localStorage.removeItem('routesGeneratedTime')
}

/**
 * 处理 401 未授权
 */
function handleUnauthorizedError() {
  clearAuthLocalStorage()
  const loginPath = getConfig().router.loginPath
  if (window.location.pathname !== loginPath) {
    window.location.href = loginPath
  }
}

/**
 * 按状态码输出错误日志
 */
function logHttpErrorStatus(status?: number, code?: string): void {
  if (status === 401) {
    handleUnauthorizedError()
    return
  }
  if (status === 403) {
    logger.error('权限不足')
    return
  }
  if (status === 404) {
    logger.error('资源不存在')
    return
  }
  if (status === 500) {
    logger.error('服务器内部错误')
    return
  }
  if (code === 'ECONNABORTED') {
    logger.error('请求超时')
    return
  }
  if (code === 'ERR_CANCELED') {
    logger.warn('请求已取消')
    return
  }
  if (!status) {
    logger.error('网络连接错误')
  }
}

/**
 * 统一响应成功处理
 */
function handleResponseSuccess(response: { config: InternalAxiosRequestConfig; status: number; data: unknown }): unknown {
  const traceConfig = response.config as TraceableRequestConfig
  const data = response.data as WrappedResponseData

  const businessCode = parseBusinessCode(data.code)
  logResponseEnd(traceConfig, response.status, businessCode)
  warnNon200BusinessCode(traceConfig, data, businessCode)
  return unwrapResponseData(data)
}

/**
 * 统一响应错误处理
 */
function handleResponseError(error: {
  config?: InternalAxiosRequestConfig
  response?: { status?: number }
  code?: string
  message?: string
}) {
  const traceConfig = (error.config || {}) as TraceableRequestConfig

  logger.error('[HTTP][ERROR]', {
    traceId: traceConfig.__traceId,
    method: getRequestMethod(traceConfig),
    url: getRequestUrl(traceConfig),
    httpStatus: error.response?.status,
    errorCode: error.code,
    message: error.message,
    durationMs: getDuration(traceConfig.__requestStartAt)
  })

  logHttpErrorStatus(error.response?.status, error.code)
  return Promise.reject(error)
}

/**
 * 创建axios实例
 * 配置基础URL、超时时间等默认参数
 */
const config = getConfig()

const request = axios.create({
  // API基础URL，从配置获取
  baseURL: config.http.baseURL,
  // 请求超时时间：排查阶段暂时关闭超时限制（0 = 不限制）
  timeout: 0,
  // 是否携带Cookie
  withCredentials: config.http.withCredentials,
  // 默认请求头
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 在发送请求之前执行，可以添加认证token、加密数据、修改请求头等
 */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const extConfig = config as ExtendedAxiosRequestConfig
    const traceConfig = config as TraceableRequestConfig

    // 添加认证Token
    if (!extConfig.ignoreToken) {
      const token = localStorage.getItem(getConfig().http.tokenKey)
      if (token) {
        config.headers[getConfig().http.tokenHeaderName] = `Bearer ${token}`
      }
    }

    // 添加租户ID（如果有）
    const tenantId = localStorage.getItem('tenantId')
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId
    }

    // 添加请求ID（用于追踪）
    const traceId = `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`
    traceConfig.__traceId = traceId
    traceConfig.__requestStartAt = Date.now()
    config.headers['X-Request-Id'] = traceId

    logger.info('[HTTP][START]', {
      traceId,
      method: getRequestMethod(config),
      url: getRequestUrl(config),
      params: config.params,
      data: config.data
    })

    return config
  },
  (error) => {
    logger.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在收到响应后执行，可以统一处理响应数据、解密数据、错误码等
 */
request.interceptors.response.use(
  (response) => handleResponseSuccess(response) as typeof response,
  (error) => handleResponseError(error)
)

// 默认导出axios实例
export default request

/**
 * 封装常用的HTTP请求方法
 * 提供类型安全的API调用方法
 */
export const api = {
  /**
   * GET请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  get: <T = unknown>(url: string, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.get<ApiResponse<T>>(url, config).then(res => res as unknown as T),

  /**
   * POST请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  post: <T = unknown>(url: string, data?: unknown, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.post<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),

  /**
   * PUT请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  put: <T = unknown>(url: string, data?: unknown, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.put<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),

  /**
   * DELETE请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  delete: <T = unknown>(url: string, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.delete<ApiResponse<T>>(url, config).then(res => res as unknown as T),

  /**
   * PATCH请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  patch: <T = unknown>(url: string, data?: unknown, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.patch<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
}

/**
 * 导出扩展的请求配置类型
 */
export type { ExtendedAxiosRequestConfig }
