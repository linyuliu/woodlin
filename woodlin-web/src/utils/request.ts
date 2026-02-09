/**
 * HTTP请求工具模块 - 简化稳定版
 *
 * @author mumu
 * @description 基于axios封装的HTTP请求工具，提供统一的请求拦截、响应处理、错误处理、加密解密等功能
 *              当前暂时停用复杂请求状态管理（去重、全局loading、自动重试），先保证请求链路稳定可观测
 * @since 2025-01-01
 */

import axios, { type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getConfig } from '@/config'
import { simpleEncrypt, simpleDecrypt } from './crypto'
import { logger } from './logger'

/**
 * 后端统一响应格式
 */
interface ApiResponse<T = any> {
  code: number | string  // 支持 number 或 string 类型，以兼容不同的序列化配置
  message: string
  data: T
  timestamp?: string
}

/**
 * 扩展的Axios请求配置
 */
interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  /** 是否加密请求数据 */
  encrypt?: boolean
  /** 是否解密响应数据 */
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

/**
 * 创建axios实例
 * 配置基础URL、超时时间等默认参数
 */
const config = getConfig()

const request = axios.create({
  // API基础URL，从配置获取
  baseURL: config.http.baseURL,
  // 请求超时时间
  timeout: config.http.timeout,
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
    
    // 加密请求数据
    if (extConfig.encrypt && config.data) {
      config.data = {
        encrypted: simpleEncrypt(config.data)
      }
    }

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
  (response: AxiosResponse<ApiResponse>) => {
    const extConfig = response.config as ExtendedAxiosRequestConfig
    const traceConfig = response.config as TraceableRequestConfig
    
    const { data } = response
    
    // 解密响应数据
    if (extConfig.decrypt && data.data?.encrypted) {
      data.data = simpleDecrypt(data.data.encrypted)
    }

    const statusCode = typeof data.code === 'string' ? parseInt(data.code, 10) : data.code
    logger.info('[HTTP][END]', {
      traceId: traceConfig.__traceId,
      method: getRequestMethod(traceConfig),
      url: getRequestUrl(traceConfig),
      httpStatus: response.status,
      businessCode: statusCode,
      durationMs: getDuration(traceConfig.__requestStartAt)
    })
    
    // 根据后端的响应格式进行统一处理
    // 假设后端返回格式为 { code: number, message: string, data: any }
    // 将 code 转换为数字进行比较，以处理可能为字符串或数字的情况
    if (statusCode != null && statusCode !== 200) {
      logger.error('API业务错误:', data.message)
      
      // 显示错误提示
      if (extConfig.showError !== false) {
        // TODO: 显示错误消息
        // window.$message?.error(data.message || 'Unknown error')
      }
      
      return Promise.reject(new Error(data.message || 'Unknown error'))
    }
    
    // 返回数据部分，简化组件中的数据获取
    return data.data
  },
  (error) => {
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
    
    // 处理不同的HTTP状态码
    if (error.response?.status === 401) {
      // 未授权，清除token并跳转到登录页
      localStorage.removeItem(getConfig().http.tokenKey)
      localStorage.removeItem(`${getConfig().http.tokenKey}_expire`)
      localStorage.removeItem('tenantId')
      // 清除用户信息和路由状态
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userPermissions')
      localStorage.removeItem('userRoles')
      localStorage.removeItem('routesGenerated')
      localStorage.removeItem('routesGeneratedTime')
      
      // 避免在登录页重复跳转
      if (window.location.pathname !== getConfig().router.loginPath) {
        window.location.href = getConfig().router.loginPath
      }
    } else if (error.response?.status === 403) {
      logger.error('权限不足')
      // TODO: 跳转到403页面
      // window.location.href = '/403'
    } else if (error.response?.status === 404) {
      logger.error('资源不存在')
    } else if (error.response?.status === 500) {
      logger.error('服务器内部错误')
    } else if (error.code === 'ECONNABORTED') {
      logger.error('请求超时')
    } else if (error.code === 'ERR_CANCELED') {
      logger.warn('请求已取消')
      return Promise.reject(error)
    } else if (!error.response) {
      logger.error('网络连接错误')
    }
    
    return Promise.reject(error)
  }
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
  get: <T = any>(url: string, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.get<ApiResponse<T>>(url, config).then(res => res as unknown as T),
  
  /**
   * POST请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  post: <T = any>(url: string, data?: any, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.post<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
  
  /**
   * PUT请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  put: <T = any>(url: string, data?: any, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.put<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
  
  /**
   * DELETE请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  delete: <T = any>(url: string, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.delete<ApiResponse<T>>(url, config).then(res => res as unknown as T),
    
  /**
   * PATCH请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  patch: <T = any>(url: string, data?: any, config?: ExtendedAxiosRequestConfig): Promise<T> =>
    request.patch<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
}

/**
 * 导出扩展的请求配置类型
 */
export type { ExtendedAxiosRequestConfig }
