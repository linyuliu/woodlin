/**
 * @file api/auth.ts
 * @description 认证相关 API
 * @author yulin
 * @since 2026-05-04
 */
import { get, post } from '@/utils/request'
import type { LoginResponse, RouteItem, UserInfo } from '@/types/global'

export interface LoginParams {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
}

export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/** 登录 */
export function login(data: LoginParams): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', data)
}

/** 登出 */
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

/** 获取当前用户信息 */
export function getAuthInfo(): Promise<{ user: UserInfo; roles: string[]; permissions: string[] }> {
  return get('/auth/info')
}

/** 获取当前用户菜单路由 */
export function getUserRoutes(): Promise<RouteItem[]> {
  return get<RouteItem[]>('/auth/routes')
}

/** 修改当前登录用户密码 */
export function changePassword(data: ChangePasswordParams): Promise<void> {
  return post<void>('/auth/change-password', data)
}
