/**
 * 优雅的HTTP请求工具
 * 
 * @author mumu
 * @description 基于axios封装的HTTP请求工具，参考vue-vben-admin设计
 *              提供请求拦截、响应处理、错误处理、请求重试等功能
 * @since 2025-01-01
 * 
 * @example
 * ```typescript
 * import { http } from '@/utils/http'
 * 
 * // GET请求
 * const users = await http.get<User[]>('/api/users')
 * 
 * // POST请求with自定义配置
 * const result = await http.post('/api/login', { username, password }, {
 *   showLoading: true,
 *   showSuccessMsg: true,
 *   successMsg: '登录成功'
 * })
 * ```
 */

import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { getConfig } from '@/config'
import type { ApiResponse, RequestOptions } from './types'

/**
 * HTTP请求类
 * 
 * 封装axios实例，提供统一的请求方法和拦截器配置
 */
class HttpRequest {
  /** Axios实例 */
  private axiosInstance: AxiosInstance
  
  /** 请求队列（用于取消重复请求） */
  private pendingRequests: Map<string, AbortController> = new Map()

  constructor() {
    const config = getConfig()
    
    // 创建axios实例
    this.axiosInstance = axios.create({
      baseURL: config.http.baseURL,
      timeout: config.http.timeout,
      withCredentials: config.http.withCredentials,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    })

    // 设置拦截器
    this.setupInterceptors()
  }

  /**
   * 设置请求和响应拦截器
   * 
   * 请求拦截器：添加Token、处理请求配置、取消重复请求
   * 响应拦截器：处理响应数据、错误处理、请求重试
   */
  private setupInterceptors(): void {
    // ===== 请求拦截器 =====
    this.axiosInstance.interceptors.request.use(
      (config) => {
        // 添加Token认证
        const token = this.getToken()
        if (token && config.headers) {
          const httpConfig = getConfig().http
          config.headers[httpConfig.tokenHeaderName] = `Bearer ${token}`
        }

        // 取消重复请求
        const requestKey = this.getRequestKey(config)
        if (this.pendingRequests.has(requestKey)) {
          const controller = this.pendingRequests.get(requestKey)
          controller?.abort()
        }
        
        const controller = new AbortController()
        config.signal = controller.signal
        this.pendingRequests.set(requestKey, controller)

        return config
      },
      (error) => {
        console.error('❌ 请求配置错误:', error)
        return Promise.reject(error)
      }
    )

    // ===== 响应拦截器 =====
    this.axiosInstance.interceptors.response.use(
      (response) => {
        // 移除pending请求
        const requestKey = this.getRequestKey(response.config)
        this.pendingRequests.delete(requestKey)

        return this.handleResponse(response)
      },
      async (error: AxiosError) => {
        // 移除pending请求
        if (error.config) {
          const requestKey = this.getRequestKey(error.config)
          this.pendingRequests.delete(requestKey)
        }

        return this.handleError(error)
      }
    )
  }

  /**
   * 处理响应数据
   * 
   * @param response Axios响应对象
   * @returns 处理后的数据
   */
  private handleResponse(response: AxiosResponse): any {
    const { data } = response
    
    // 如果响应数据符合ApiResponse格式
    if (data && typeof data === 'object' && 'code' in data) {
      const apiResponse = data as ApiResponse
      
      // 业务成功
      if (apiResponse.code === 200) {
        return apiResponse.data
      }
      
      // 业务失败
      return Promise.reject(new Error(apiResponse.message || '请求失败'))
    }
    
    // 直接返回数据
    return data
  }

  /**
   * 处理错误响应
   * 
   * @param error Axios错误对象
   * @returns Promise.reject
   */
  private async handleError(error: AxiosError): Promise<any> {
    // 请求被取消
    if (axios.isCancel(error)) {
      return Promise.reject(new Error('请求已取消'))
    }

    const { response, config } = error
    const requestOptions = config as RequestOptions

    // 请求超时，尝试重试
    if (error.code === 'ECONNABORTED' && requestOptions?.enableRetry !== false) {
      const httpConfig = getConfig().http
      const retryCount = requestOptions?.retryCount ?? httpConfig.retryCount
      const retryDelay = requestOptions?.retryDelay ?? httpConfig.retryDelay
      
      // @ts-ignore
      const currentRetry = config.__retryCount || 0
      
      if (currentRetry < retryCount) {
        // @ts-ignore
        config.__retryCount = currentRetry + 1
        
        // 延迟后重试
        await new Promise(resolve => setTimeout(resolve, retryDelay))
        return this.axiosInstance.request(config!)
      }
    }

    // 处理HTTP错误状态码
    if (response) {
      switch (response.status) {
        case 401:
          // 未授权，清除token并跳转登录
          this.handleUnauthorized()
          break
        case 403:
          console.error('🚫 权限不足')
          break
        case 404:
          console.error('📭 资源未找到')
          break
        case 500:
          console.error('💥 服务器内部错误')
          break
        case 503:
          console.error('🔧 服务暂时不可用')
          break
      }
    }

    const errorMsg = response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(errorMsg))
  }

  /**
   * 处理未授权情况
   * 
   * 清除token并跳转到登录页
   */
  private handleUnauthorized(): void {
    console.warn('🔐 认证失效，请重新登录')
    this.removeToken()
    const config = getConfig()
    window.location.href = config.router.loginPath
  }

  /**
   * 获取Token
   * 
   * @returns Token字符串或null
   */
  private getToken(): string | null {
    const config = getConfig()
    return localStorage.getItem(config.http.tokenKey)
  }

  /**
   * 移除Token
   */
  private removeToken(): void {
    const config = getConfig()
    localStorage.removeItem(config.http.tokenKey)
  }

  /**
   * 生成请求唯一键
   * 
   * 用于识别和取消重复请求
   * 
   * @param config 请求配置
   * @returns 请求唯一键
   */
  private getRequestKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
  }

  /**
   * GET请求
   * 
   * @template T 响应数据类型
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  get<T = any>(url: string, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.get(url, config)
  }

  /**
   * POST请求
   * 
   * @template T 响应数据类型
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  post<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.post(url, data, config)
  }

  /**
   * PUT请求
   * 
   * @template T 响应数据类型
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  put<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.put(url, data, config)
  }

  /**
   * DELETE请求
   * 
   * @template T 响应数据类型
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  delete<T = any>(url: string, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.delete(url, config)
  }

  /**
   * PATCH请求
   * 
   * @template T 响应数据类型
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  patch<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.patch(url, data, config)
  }
}

// 创建并导出HTTP实例
export const http = new HttpRequest()

// 默认导出
export default http
