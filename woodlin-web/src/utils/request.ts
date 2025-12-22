/**
 * HTTP请求工具模块
 * 
 * @author mumu
 * @description 基于axios封装的HTTP请求工具，提供统一的请求拦截、响应处理和错误处理
 * @since 2025-01-01
 */

import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'

/**
 * 后端统一响应格式
 */
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}

/**
 * 创建axios实例
 * 配置基础URL、超时时间等默认参数
 */
const request = axios.create({
  // API基础URL，从环境变量获取或使用默认值
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // 请求超时时间（60秒）
  timeout: 60000,
  // 默认请求头
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 在发送请求之前执行，可以添加认证token、修改请求头等
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    console.warn(`🚀 API请求: ${config.method?.toUpperCase()} ${config.url}`)
    
    return config
  },
  (error) => {
    console.error('❌ 请求配置错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在收到响应后执行，可以统一处理响应数据、错误码等
 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    
    console.warn(`✅ API响应: ${response.config.url}`, data)
    
    // 根据后端的响应格式进行统一处理
    // 假设后端返回格式为 { code: number, message: string, data: any }
    if (data.code && data.code !== 200) {
      console.error('❌ API业务错误:', data.message)
      return Promise.reject(new Error(data.message || 'Unknown error'))
    }
    
    // 返回数据部分，简化组件中的数据获取
    return data.data
  },
  (error) => {
    console.error('❌ HTTP请求错误:', error)
    
    // 处理不同的HTTP状态码
    if (error.response?.status === 401) {
      // 未授权，清除token并跳转到登录页
      console.warn('🔐 认证失效，跳转到登录页')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else if (error.response?.status === 403) {
      console.error('🚫 权限不足')
    } else if (error.response?.status === 500) {
      console.error('💥 服务器内部错误')
    } else if (error.code === 'ECONNABORTED') {
      console.error('⏰ 请求超时')
    } else if (!error.response) {
      console.error('🌐 网络连接错误')
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
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    request.get<ApiResponse<T>>(url, config).then(res => res as unknown as T),
  
  /**
   * POST请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    request.post<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
  
  /**
   * PUT请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    request.put<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
  
  /**
   * DELETE请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<T>
   */
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    request.delete<ApiResponse<T>>(url, config).then(res => res as unknown as T),
    
  /**
   * PATCH请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<T>
   */
  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    request.patch<ApiResponse<T>>(url, data, config).then(res => res as unknown as T),
}