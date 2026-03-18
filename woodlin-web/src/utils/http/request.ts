import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { getConfig } from '@/config'
import { logger } from '@/utils/logger'
import type { ApiResponse, RequestOptions } from './types'

type RetryableRequestConfig = InternalAxiosRequestConfig & RequestOptions & {
  __retryCount?: number
}

type RetryPolicy = {
  retryCount: number
  retryDelay: number
}

const SUCCESS_CODE = 200

function isApiResponse<T>(data: unknown): data is ApiResponse<T> {
  return typeof data === 'object' && data !== null && 'code' in data
}

function sleep(delay: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, delay)
  })
}

class HttpRequest {
  private readonly axiosInstance: AxiosInstance
  private readonly appConfig = getConfig()
  private readonly pendingRequests = new Map<string, AbortController>()

  constructor() {
    const { http } = this.appConfig
    this.axiosInstance = axios.create({
      baseURL: http.baseURL,
      timeout: http.timeout,
      withCredentials: http.withCredentials,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    })

    this.setupInterceptors()
  }

  private setupInterceptors(): void {
    this.axiosInstance.interceptors.request.use(
      (config) => this.prepareRequest(config),
      (error) => {
        logger.error('请求配置错误:', error)
        return Promise.reject(error)
      }
    )

    this.axiosInstance.interceptors.response.use(
      (response) => {
        this.clearPendingRequest(response.config)
        return response
      },
      async (error: AxiosError) => {
        this.clearPendingRequest(error.config)
        return this.handleError(error)
      }
    )
  }

  private prepareRequest(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
    const token = this.getToken()
    if (token && config.headers && this.shouldAttachToken(config)) {
      config.headers[this.appConfig.http.tokenHeaderName] = `Bearer ${token}`
    }

    this.trackPendingRequest(config)
    return config
  }

  private shouldAttachToken(config: AxiosRequestConfig): boolean {
    return (config as RequestOptions).requiresAuth !== false
  }

  private shouldTrackRequest(config: AxiosRequestConfig | undefined): boolean {
    if (!config) {
      return false
    }

    return (config as RequestOptions).ignoreDuplicateRequest !== true
  }

  private trackPendingRequest(config: InternalAxiosRequestConfig): void {
    if (!this.shouldTrackRequest(config)) {
      return
    }

    const requestKey = this.getRequestKey(config)
    this.pendingRequests.get(requestKey)?.abort()

    const controller = new AbortController()
    config.signal = controller.signal
    this.pendingRequests.set(requestKey, controller)
  }

  private clearPendingRequest(config?: AxiosRequestConfig): void {
    if (!config || !this.shouldTrackRequest(config)) {
      return
    }

    const requestKey = this.getRequestKey(config)
    const currentController = this.pendingRequests.get(requestKey)
    if (!currentController) {
      return
    }

    if (config?.signal && currentController.signal !== config.signal) {
      return
    }

    this.pendingRequests.delete(requestKey)
  }

  private handleResponse<T>(response: AxiosResponse<unknown>): T {
    const { data } = response
    if (isApiResponse<T>(data)) {
      if (data.code === SUCCESS_CODE) {
        return data.data
      }
      throw new Error(data.message || '请求失败')
    }

    return data as T
  }

  private async tryRetryOnTimeout(
    error: AxiosError,
    retryConfig: RetryableRequestConfig | undefined
  ): Promise<AxiosResponse<unknown> | null> {
    if (!this.shouldRetryTimeout(error.code, retryConfig)) {
      return null
    }

    const retryPolicy = this.resolveRetryPolicy(retryConfig)
    if (!retryConfig || !this.hasRetryQuota(retryConfig, retryPolicy.retryCount)) {
      return null
    }

    retryConfig.__retryCount = (retryConfig.__retryCount ?? 0) + 1
    await sleep(retryPolicy.retryDelay)
    return this.axiosInstance.request(retryConfig)
  }

  private shouldRetryTimeout(errorCode: string | undefined, requestOptions: RequestOptions | undefined): boolean {
    return errorCode === 'ECONNABORTED' && requestOptions?.enableRetry !== false
  }

  private resolveRetryPolicy(requestOptions: RequestOptions | undefined): RetryPolicy {
    const { http } = this.appConfig
    return {
      retryCount: requestOptions?.retryCount ?? http.retryCount,
      retryDelay: requestOptions?.retryDelay ?? http.retryDelay
    }
  }

  private hasRetryQuota(retryConfig: RetryableRequestConfig, retryCount: number): boolean {
    return (retryConfig.__retryCount ?? 0) < retryCount
  }

  private logHttpStatusError(status?: number): void {
    if (!status) {
      return
    }

    switch (status) {
      case 401:
        this.handleUnauthorized()
        return
      case 403:
        logger.error('权限不足')
        return
      case 404:
        logger.error('资源未找到')
        return
      case 500:
        logger.error('服务器内部错误')
        return
      case 503:
        logger.error('服务暂时不可用')
        return
      default:
        return
    }
  }

  private buildErrorMessage(response: AxiosResponse<unknown> | undefined, error: AxiosError): string {
    const responseData = isApiResponse(response?.data) ? response.data : undefined
    return responseData?.message || error.message || '请求失败'
  }

  private async handleError(error: AxiosError): Promise<AxiosResponse<unknown>> {
    if (axios.isCancel(error)) {
      return Promise.reject(new Error('请求已取消'))
    }

    const retryConfig = error.config as RetryableRequestConfig | undefined
    const retryResponse = await this.tryRetryOnTimeout(error, retryConfig)
    if (retryResponse) {
      return retryResponse
    }

    this.logHttpStatusError(error.response?.status)
    return Promise.reject(new Error(this.buildErrorMessage(error.response, error)))
  }

  private handleUnauthorized(): void {
    logger.warn('认证失效，请重新登录')
    this.removeToken()
    if (window.location.pathname !== this.appConfig.router.loginPath) {
      window.location.href = this.appConfig.router.loginPath
    }
  }

  private getToken(): string | null {
    return localStorage.getItem(this.appConfig.http.tokenKey)
  }

  private removeToken(): void {
    localStorage.removeItem(this.appConfig.http.tokenKey)
  }

  private serializeRequestPart(value: unknown): string {
    try {
      return JSON.stringify(value) ?? ''
    } catch {
      return String(value)
    }
  }

  private getRequestKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return [
      method,
      url,
      this.serializeRequestPart(params),
      this.serializeRequestPart(data)
    ].join('&')
  }

  get<T = unknown>(url: string, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.get(url, config).then((response) => this.handleResponse<T>(response))
  }

  post<T = unknown>(url: string, data?: unknown, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.post(url, data, config).then((response) => this.handleResponse<T>(response))
  }

  put<T = unknown>(url: string, data?: unknown, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.put(url, data, config).then((response) => this.handleResponse<T>(response))
  }

  delete<T = unknown>(url: string, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.delete(url, config).then((response) => this.handleResponse<T>(response))
  }

  patch<T = unknown>(url: string, data?: unknown, config?: RequestOptions): Promise<T> {
    return this.axiosInstance.patch(url, data, config).then((response) => this.handleResponse<T>(response))
  }
}

export const http = new HttpRequest()

export default http
