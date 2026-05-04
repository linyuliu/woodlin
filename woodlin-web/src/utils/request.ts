/**
 * @file utils/request.ts
 * @description Axios 实例与拦截器：自动注入 Token、租户头，统一解包/异常处理
 * @author yulin
 * @since 2026-05-04
 */
import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { getToken, removeToken } from './auth'
import type { R } from '@/types/global'

/** 统一异常处理回调（由 setupRequestHandler 注入，避免循环依赖） */
type Handler = (msg: string) => void
let onError: Handler = (msg) => console.error(msg)
let on401: () => void = () => {
  removeToken()
  if (location.hash.indexOf('/login') === -1) {
    location.hash = '#/login'
  }
}

/** 注入消息提示回调 */
export function setRequestErrorHandler(handler: Handler): void {
  onError = handler
}

/** 注入 401 处理 */
export function setUnauthorizedHandler(handler: () => void): void {
  on401 = handler
}

/** 获取当前租户 ID（运行时按需读取，避免循环依赖） */
function getTenantId(): string | null {
  try {
    const raw = localStorage.getItem('woodlin_tenant')
    if (!raw) {return null}
    const parsed = JSON.parse(raw) as { tenantId?: string }
    return parsed?.tenantId ?? null
  } catch {
    return null
  }
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      // 主要使用 Authorization Bearer（Sa-Token 已配置读取此头）
      config.headers.set('Authorization', `Bearer ${token}`)
      // 兼容性回退：同时设置 satoken 头（防止配置未生效）
      config.headers.set('satoken', token)
    }
    const tenantId = getTenantId()
    if (tenantId) {
      config.headers.set('X-Tenant-Id', tenantId)
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse<R<unknown>>) => {
    const body = response.data
    // 文件下载等非 JSON 响应直接返回
    if (response.config.responseType === 'blob') {
      return response as unknown as AxiosResponse
    }
    if (!body || typeof body !== 'object') {return response}
    if ('code' in body) {
      if (body.code === 200) {
        return body.data as never
      }
      if (body.code === 401) {
        on401()
      }
      onError(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body as never
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      on401()
    }
    const msg = error?.response?.data?.message || error.message || '网络异常'
    onError(msg)
    return Promise.reject(error)
  },
)

/** GET */
export function get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, { params, ...config }) as unknown as Promise<T>
}

/** POST */
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config) as unknown as Promise<T>
}

/** PUT */
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config) as unknown as Promise<T>
}

/** DELETE */
export function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, config) as unknown as Promise<T>
}

export default service
