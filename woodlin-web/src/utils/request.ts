/**
 * HTTP请求工具模块 - 增强版
 * 
 * @author mumu
 * @description 基于axios封装的HTTP请求工具，提供统一的请求拦截、响应处理、错误处理、加密解密等功能
 *              参考vue-vben-admin的请求封装设计
 * @since 2025-01-01
 */

import axios, { type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getConfig } from '@/config'
import { simpleEncrypt, simpleDecrypt } from './crypto'

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
  /** 是否显示加载提示 */
  showLoading?: boolean
  /** 是否显示错误提示 */
  showError?: boolean
  /** 是否重试请求 */
  retry?: boolean
  /** 重试次数 */
  retryCount?: number
  /** 重试延迟（毫秒） */
  retryDelay?: number
  /** 是否忽略token */
  ignoreToken?: boolean
}

/**
 * 请求队列，用于管理并发请求
 */
const requestQueue = new Map<string, AbortController>()

/**
 * 生成请求唯一标识
 */
function generateRequestKey(config: InternalAxiosRequestConfig): string {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

/**
 * 取消重复请求
 */
function removePendingRequest(config: InternalAxiosRequestConfig) {
  const requestKey = generateRequestKey(config)
  
  if (requestQueue.has(requestKey)) {
    const controller = requestQueue.get(requestKey)
    controller?.abort()
    requestQueue.delete(requestKey)
  }
}

/**
 * 添加请求到队列
 */
function addPendingRequest(config: InternalAxiosRequestConfig) {
  const requestKey = generateRequestKey(config)
  const controller = new AbortController()
  config.signal = controller.signal
  requestQueue.set(requestKey, controller)
}

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
    
    // 取消重复请求
    removePendingRequest(config)
    addPendingRequest(config)
    
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
    config.headers['X-Request-Id'] = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    
    // 加密请求数据
    if (extConfig.encrypt && config.data) {
      config.data = {
        encrypted: simpleEncrypt(config.data)
      }
    }
    
    // 显示加载提示
    if (extConfig.showLoading !== false) {
      // TODO: 显示全局loading
      // useAppStore().showLoading()
    }
    
    return config
  },
  (error) => {
    console.error('请求配置错误:', error)
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
    
    // 从请求队列中移除
    removePendingRequest(response.config as InternalAxiosRequestConfig)
    
    // 隐藏加载提示
    if (extConfig.showLoading !== false) {
      // TODO: 隐藏全局loading
      // useAppStore().hideLoading()
    }
    
    const { data } = response
    
    // 解密响应数据
    if (extConfig.decrypt && data.data?.encrypted) {
      data.data = simpleDecrypt(data.data.encrypted)
    }
    
    // 根据后端的响应格式进行统一处理
    // 假设后端返回格式为 { code: number, message: string, data: any }
    // 将 code 转换为数字进行比较，以处理可能为字符串或数字的情况
    const statusCode = typeof data.code === 'string' ? parseInt(data.code, 10) : data.code
    if (statusCode != null && statusCode !== 200) {
      console.error('API业务错误:', data.message)
      
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
  async (error) => {
    const extConfig = error.config as ExtendedAxiosRequestConfig
    
    // 从请求队列中移除
    if (error.config) {
      removePendingRequest(error.config)
    }
    
    // 隐藏加载提示
    if (extConfig?.showLoading !== false) {
      // TODO: 隐藏全局loading
      // useAppStore().hideLoading()
    }
    
    console.error('HTTP请求错误:', error)
    
    // 处理不同的HTTP状态码
    if (error.response?.status === 401) {
      // 未授权，清除token并跳转到登录页
      localStorage.removeItem(getConfig().http.tokenKey)
      localStorage.removeItem('tenantId')
      
      // 避免在登录页重复跳转
      if (window.location.pathname !== getConfig().router.loginPath) {
        window.location.href = getConfig().router.loginPath
      }
    } else if (error.response?.status === 403) {
      console.error('权限不足')
      // TODO: 跳转到403页面
      // window.location.href = '/403'
    } else if (error.response?.status === 404) {
      console.error('资源不存在')
    } else if (error.response?.status === 500) {
      console.error('服务器内部错误')
    } else if (error.code === 'ECONNABORTED') {
      console.error('请求超时')
    } else if (error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    } else if (!error.response) {
      console.error('网络连接错误')
    }
    
    // 请求重试
    if (extConfig?.retry && extConfig.retryCount && extConfig.retryCount > 0) {
      extConfig.retryCount--
      
      // 延迟后重试
      await new Promise(resolve => setTimeout(resolve, extConfig.retryDelay || 1000))
      
      return request(extConfig)
    }
    
    // 显示错误提示
    if (extConfig?.showError !== false) {
      // TODO: 显示错误消息
      // const message = error.response?.data?.message || error.message || '请求失败'
      // window.$message?.error(message)
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