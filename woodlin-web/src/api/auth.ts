/**
 * 认证管理API服务
 * 
 * @author mumu
 * @description 用户认证相关的API接口调用，包括登录、登出、密码管理等功能
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginRequest {
  /** 登录方式: password, captcha, mobile_sms, sso, passkey, totp */
  loginType?: string
  /** 用户名 */
  username?: string
  /** 密码 */
  password?: string
  /** 图形验证码 */
  captcha?: string
  /** 验证码UUID */
  uuid?: string
  /** 手机号 */
  mobile?: string
  /** 短信验证码 */
  smsCode?: string
  /** SSO令牌 */
  ssoToken?: string
  /** SSO提供商 */
  ssoProvider?: string
  /** Passkey凭证ID */
  passkeyCredentialId?: string
  /** Passkey认证响应 */
  passkeyAuthResponse?: string
  /** TOTP验证码 */
  totpCode?: string
  /** TOTP密钥 */
  totpSecret?: string
  /** 是否记住我 */
  rememberMe?: boolean
}

/**
 * 登录响应
 */
export interface LoginResponse {
  /** 访问令牌 */
  token: string
  /** 令牌类型 */
  tokenType?: string
  /** 令牌过期时间（秒） */
  expiresIn?: number
  /** 是否需要修改密码 */
  requirePasswordChange?: boolean
  /** 密码是否即将过期 */
  passwordExpiringSoon?: boolean
  /** 密码过期剩余天数 */
  daysUntilPasswordExpiration?: number
  /** 提示消息 */
  message?: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  /** 旧密码 */
  oldPassword: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
}

/**
 * 忘记密码重置请求
 */
export interface ForgotPasswordRequest {
  /** 用户名或手机号或邮箱 */
  username: string
  /** 验证码 */
  code: string
  /** 验证码类型: sms, email */
  codeType: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
}

/**
 * 图形验证码信息
 */
export interface CaptchaInfo {
  /** 验证码UUID */
  uuid: string
  /** 验证码图片（Base64） */
  image: string
}

/**
 * 用户登录
 * @param data 登录请求参数
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/auth/login', data)
}

/**
 * 用户登出
 */
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}

/**
 * 修改密码
 * @param data 修改密码请求
 */
export function changePassword(data: ChangePasswordRequest): Promise<void> {
  return request.post('/auth/change-password', data)
}

/**
 * 忘记密码重置（通过验证码）
 * @param data 忘记密码重置请求
 */
export function forgotPassword(data: ForgotPasswordRequest): Promise<void> {
  return request.post('/auth/forgot-password', data)
}

/**
 * 用户信息响应接口
 */
export interface UserInfoResponse {
  /** 用户ID */
  userId?: string | number
  /** 用户ID (备用字段) */
  id?: string | number
  /** 用户名 */
  username?: string
  /** 昵称 */
  nickname?: string
  /** 真实姓名 */
  realName?: string
  /** 邮箱 */
  email?: string
  /** 手机号 */
  mobile?: string
  /** 头像 */
  avatar?: string
  /** 性别 */
  gender?: string
  /** 部门ID */
  deptId?: string | number
  /** 部门名称 */
  deptName?: string
  /** 租户ID */
  tenantId?: string | number
  /** 租户名称 */
  tenantName?: string
  /** 角色代码列表 */
  roleCodes?: string[]
  /** 角色列表 (备用字段) */
  roles?: string[]
  /** 权限列表 */
  permissions?: string[]
  /** 用户状态 */
  status?: string
  /** 创建时间 */
  createTime?: string
  /** 最后登录时间 */
  lastLoginTime?: string
  /** 登录时间 (备用字段) */
  loginTime?: string
}

/**
 * 获取当前用户信息
 */
export function getUserInfo(): Promise<UserInfoResponse> {
  return request.get('/auth/userinfo')
}

/**
 * 获取图形验证码
 */
export function getCaptcha(): Promise<CaptchaInfo> {
  return request.get('/auth/captcha')
}

/**
 * 发送短信验证码
 * @param mobile 手机号
 */
export function sendSmsCode(mobile: string): Promise<void> {
  return request.post('/auth/sms/send', null, { params: { mobile } })
}

/**
 * 获取当前用户的路由菜单
 */
export function getUserRoutes(): Promise<Record<string, unknown>[]> {
  return request.get('/auth/routes')
}
