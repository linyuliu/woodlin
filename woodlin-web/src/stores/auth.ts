/**
 * 认证状态管理 Store
 * 
 * @author mumu
 * @description 管理用户认证状态、Token等
 * @since 2025-01-01
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import { login, logout, type LoginRequest, type LoginResponse } from '@/api/auth'
import { useUserStore } from './user'
import { usePermissionStore } from './permission'
import { getConfig } from '@/config'

/**
 * 认证状态管理 Store
 */
export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const config = getConfig()
  
  // ===== 状态 =====
  
  /** 访问令牌 */
  const token = ref<string | null>(null)
  
  /** 令牌类型 */
  const tokenType = ref<string>('Bearer')
  
  /** 令牌过期时间（秒） */
  const expiresIn = ref<number | null>(null)
  
  /** 令牌过期时间戳（毫秒） */
  const tokenExpireTime = ref<number | null>(null)
  
  /** 是否记住我 */
  const rememberMe = ref<boolean>(false)
  
  /** 登录时间 */
  const loginTime = ref<number | null>(null)

  // ===== 计算属性 =====
  
  /** 是否已认证（有token且未过期） */
  const isAuthenticated = computed(() => {
    if (!token.value) return false
    
    // 检查token是否过期
    if (tokenExpireTime.value && Date.now() >= tokenExpireTime.value) {
      return false
    }
    
    return true
  })
  
  /** Token是否即将过期（小于5分钟） */
  const isTokenExpiringSoon = computed(() => {
    if (!tokenExpireTime.value) return false
    const remainingTime = tokenExpireTime.value - Date.now()
    return remainingTime > 0 && remainingTime < 5 * 60 * 1000 // 5分钟
  })
  
  /** 完整的授权头 */
  const authorizationHeader = computed(() => {
    if (!token.value) return null
    return `${tokenType.value} ${token.value}`
  })

  // ===== 方法 =====
  
  /**
   * 设置Token
   * @param tokenValue 令牌值
   * @param expire 过期时间（秒）
   * @param type 令牌类型
   */
  function setToken(tokenValue: string, expire?: number, type: string = 'Bearer') {
    token.value = tokenValue
    tokenType.value = type
    expiresIn.value = expire || null
    loginTime.value = Date.now()
    
    // 计算过期时间戳
    if (expire) {
      tokenExpireTime.value = Date.now() + expire * 1000
    }
    
    // 持久化到localStorage
    localStorage.setItem(config.http.tokenKey, tokenValue)
    if (expire) {
      localStorage.setItem(`${config.http.tokenKey}_expire`, String(tokenExpireTime.value))
    }
    
    console.log('✅ Token已设置:', { token: tokenValue, expire, type })
  }
  
  /**
   * 从localStorage恢复Token
   */
  function restoreToken() {
    const savedToken = localStorage.getItem(config.http.tokenKey)
    const savedExpire = localStorage.getItem(`${config.http.tokenKey}_expire`)
    
    if (savedToken) {
      token.value = savedToken
      
      if (savedExpire) {
        tokenExpireTime.value = Number(savedExpire)
        
        // 检查是否过期
        if (Date.now() >= tokenExpireTime.value) {
          console.warn('⚠️ Token已过期，清除Token')
          clearToken()
        } else {
          console.log('✅ Token已恢复:', savedToken)
        }
      } else {
        console.log('✅ Token已恢复（无过期时间）:', savedToken)
      }
    }
  }
  
  /**
   * 清除Token
   */
  function clearToken() {
    token.value = null
    tokenType.value = 'Bearer'
    expiresIn.value = null
    tokenExpireTime.value = null
    loginTime.value = null
    
    // 从localStorage清除
    localStorage.removeItem(config.http.tokenKey)
    localStorage.removeItem(`${config.http.tokenKey}_expire`)
    
    console.log('✅ Token已清除')
  }
  
  /**
   * 用户登录
   * @param loginData 登录数据
   * @returns 登录响应
   */
  async function doLogin(loginData: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await login(loginData)
      
      // 设置Token
      setToken(response.token, response.expiresIn, response.tokenType)
      
      // 设置记住我
      if (loginData.rememberMe) {
        rememberMe.value = true
        localStorage.setItem('rememberMe', 'true')
      }
      
      // 获取用户信息
      await userStore.fetchUserInfo()
      
      // 生成动态路由
      await permissionStore.generateRoutes(userStore.permissions)
      
      console.log('✅ 登录成功')
      
      return response
    } catch (error) {
      console.error('❌ 登录失败:', error)
      throw error
    }
  }
  
  /**
   * 用户登出
   */
  async function doLogout() {
    try {
      // 调用后端登出接口
      await logout()
    } catch (error) {
      console.error('⚠️ 登出接口调用失败（继续清除本地状态）:', error)
    } finally {
      // 清除本地状态
      clearToken()
      userStore.clearUserInfo()
      permissionStore.clearRoutes()
      
      // 跳转到登录页
      router.push(config.router.loginPath)
      
      console.log('✅ 已登出')
    }
  }
  
  /**
   * Token刷新
   * TODO: 如果后端支持token刷新，实现此方法
   */
  async function refreshToken() {
    console.warn('TODO: 实现Token刷新逻辑')
    // 实现逻辑：
    // 1. 调用后端refresh token接口
    // 2. 更新token
    // 3. 更新过期时间
  }
  
  /**
   * 检查Token是否需要刷新
   */
  function checkTokenRefresh() {
    if (isTokenExpiringSoon.value) {
      console.warn('⚠️ Token即将过期，尝试刷新')
      refreshToken()
    }
  }

  // 初始化：从localStorage恢复Token
  restoreToken()

  return {
    // 状态
    token,
    tokenType,
    expiresIn,
    tokenExpireTime,
    rememberMe,
    loginTime,
    
    // 计算属性
    isAuthenticated,
    isTokenExpiringSoon,
    authorizationHeader,
    
    // 方法
    setToken,
    restoreToken,
    clearToken,
    doLogin,
    doLogout,
    refreshToken,
    checkTokenRefresh
  }
})
