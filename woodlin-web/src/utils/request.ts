import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 60000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 这里可以根据后端的响应格式进行调整
    if (data.code && data.code !== 200) {
      console.error('API Error:', data.message)
      return Promise.reject(new Error(data.message || 'Unknown error'))
    }
    
    return data
  },
  (error) => {
    console.error('HTTP Error:', error)
    
    if (error.response?.status === 401) {
      // 未授权，跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

export default request

// 封装常用的请求方法
export const api = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    request.get(url, config),
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    request.post(url, data, config),
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    request.put(url, data, config),
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    request.delete(url, config),
}