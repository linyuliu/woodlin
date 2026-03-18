/**
 * 认证状态管理 Store
 *
 * @author mumu
 * @description 管理用户认证状态、Token等
 * @since 2025-01-01
 */

import {ref, computed, type ComputedRef, type Ref} from 'vue'
import {defineStore} from 'pinia'
import {useRouter, type Router} from 'vue-router'
import {login, logout, type LoginRequest, type LoginResponse} from '@/api/auth'
import {useUserStore} from './user'
import {usePermissionStore} from './permission'
import {getConfig, type ProjectConfig} from '@/config'
import {logger} from '@/utils/logger'

type AuthStateRefs = {
  token: Ref<string | null>
  tokenType: Ref<string>
  expiresIn: Ref<number | null>
  tokenExpireTime: Ref<number | null>
  rememberMe: Ref<boolean>
  loginTime: Ref<number | null>
}

type AuthComputed = {
  isAuthenticated: ComputedRef<boolean>
  isTokenExpiringSoon: ComputedRef<boolean>
  authorizationHeader: ComputedRef<string | null>
}

type AuthDependencies = {
  router: Router
  config: ProjectConfig
  userStore: ReturnType<typeof useUserStore>
  permissionStore: ReturnType<typeof usePermissionStore>
}

/**
 * 创建认证状态
 *
 * @returns 认证状态
 */
function createAuthState(): AuthStateRefs {
  return {
    token: ref<string | null>(null),
    tokenType: ref('Bearer'),
    expiresIn: ref<number | null>(null),
    tokenExpireTime: ref<number | null>(null),
    rememberMe: ref(false),
    loginTime: ref<number | null>(null)
  }
}

/**
 * 创建认证计算属性
 *
 * @param state 认证状态
 * @returns 计算属性
 */
function createAuthComputed(state: AuthStateRefs): AuthComputed {
  const isAuthenticated = computed(() => {
    if (!state.token.value) {
      return false
    }
    if (state.tokenExpireTime.value && Date.now() >= state.tokenExpireTime.value) {
      return false
    }
    return true
  })

  const isTokenExpiringSoon = computed(() => {
    if (!state.tokenExpireTime.value) {
      return false
    }
    const remainingTime = state.tokenExpireTime.value - Date.now()
    return remainingTime > 0 && remainingTime < 5 * 60 * 1000
  })

  const authorizationHeader = computed(() => {
    if (!state.token.value) {
      return null
    }
    return `${state.tokenType.value} ${state.token.value}`
  })

  return {
    isAuthenticated,
    isTokenExpiringSoon,
    authorizationHeader
  }
}

/**
 * 创建设置 Token 动作
 *
 * @param state 认证状态
 * @param config 应用配置
 * @returns action
 */
function createSetTokenAction(state: AuthStateRefs, config: ProjectConfig) {
  return (tokenValue: string, expire?: number, type = 'Bearer') => {
    state.token.value = tokenValue
    state.tokenType.value = type
    state.expiresIn.value = expire || null
    state.loginTime.value = Date.now()

    if (expire) {
      state.tokenExpireTime.value = Date.now() + expire * 1000
    }

    localStorage.setItem(config.http.tokenKey, tokenValue)
    if (expire) {
      localStorage.setItem(`${config.http.tokenKey}_expire`, String(state.tokenExpireTime.value))
    }
  }
}

/**
 * 创建清理 Token 动作
 *
 * @param state 认证状态
 * @param config 应用配置
 * @returns action
 */
function createClearTokenAction(state: AuthStateRefs, config: ProjectConfig) {
  return () => {
    state.token.value = null
    state.tokenType.value = 'Bearer'
    state.expiresIn.value = null
    state.tokenExpireTime.value = null
    state.loginTime.value = null

    localStorage.removeItem(config.http.tokenKey)
    localStorage.removeItem(`${config.http.tokenKey}_expire`)
  }
}

/**
 * 创建恢复 Token 动作
 *
 * @param state 认证状态
 * @param config 应用配置
 * @param clearToken 清理 Token action
 * @returns action
 */
function createRestoreTokenAction(
  state: AuthStateRefs,
  config: ProjectConfig,
  clearToken: () => void
) {
  return () => {
    const savedToken = localStorage.getItem(config.http.tokenKey)
    const savedExpire = localStorage.getItem(`${config.http.tokenKey}_expire`)
    if (!savedToken) {
      return
    }

    state.token.value = savedToken
    if (!savedExpire) {
      return
    }

    state.tokenExpireTime.value = Number(savedExpire)
    if (Date.now() >= state.tokenExpireTime.value) {
      clearToken()
    }
  }
}

/**
 * 创建登录动作
 *
 * @param deps 依赖集合
 * @param setToken 设置 Token action
 * @returns action
 */
function createDoLoginAction(
  deps: AuthDependencies,
  setToken: (tokenValue: string, expire?: number, type?: string) => void,
  rememberMe: Ref<boolean>
) {
  return async (loginData: LoginRequest): Promise<LoginResponse> => {
    try {
      const response = await login(loginData)
      setToken(response.token, response.expiresIn, response.tokenType)

      if (loginData.rememberMe) {
        rememberMe.value = true
        localStorage.setItem('rememberMe', 'true')
      }

      await deps.userStore.fetchUserInfo()
      await deps.permissionStore.generateRoutes(deps.userStore.permissions)
      logger.log('登录成功')
      return response
    } catch (error) {
      logger.error('登录失败:', error)
      throw error
    }
  }
}

/**
 * 创建登出动作
 *
 * @param deps 依赖集合
 * @param clearToken 清理 Token action
 * @returns action
 */
function createDoLogoutAction(deps: AuthDependencies, clearToken: () => void) {
  return async () => {
    try {
      await logout()
    } catch (error) {
      logger.warn('登出接口调用失败，继续清除本地状态:', error)
    } finally {
      clearToken()
      deps.userStore.clearUserInfo()
      deps.permissionStore.clearRoutes()
      deps.router.push(deps.config.router.loginPath)
    }
  }
}

/**
 * 创建刷新 Token 动作
 *
 * @returns action
 */
function createRefreshTokenAction() {
  return async () => {
    logger.warn('Token刷新逻辑尚未接入，当前跳过自动刷新')
  }
}

/**
 * 创建检查 Token 刷新动作
 *
 * @param isTokenExpiringSoon Token 即将过期标记
 * @param refreshToken 刷新 Token action
 * @returns action
 */
function createCheckTokenRefreshAction(
  isTokenExpiringSoon: ComputedRef<boolean>,
  refreshToken: () => Promise<void>
) {
  return () => {
    if (isTokenExpiringSoon.value) {
      void refreshToken()
    }
  }
}

/**
 * 组装认证动作
 *
 * @param state 认证状态
 * @param computedState 计算属性
 * @param deps 依赖集合
 * @returns 动作集合
 */
function createAuthActions(state: AuthStateRefs, computedState: AuthComputed, deps: AuthDependencies) {
  const setToken = createSetTokenAction(state, deps.config)
  const clearToken = createClearTokenAction(state, deps.config)
  const restoreToken = createRestoreTokenAction(state, deps.config, clearToken)
  const doLogin = createDoLoginAction(deps, setToken, state.rememberMe)
  const doLogout = createDoLogoutAction(deps, clearToken)
  const refreshToken = createRefreshTokenAction()
  const checkTokenRefresh = createCheckTokenRefreshAction(computedState.isTokenExpiringSoon, refreshToken)

  return {
    setToken,
    clearToken,
    restoreToken,
    doLogin,
    doLogout,
    refreshToken,
    checkTokenRefresh
  }
}

/**
 * 组装认证 Store
 *
 * @returns store 内容
 */
function createAuthStore() {
  const deps: AuthDependencies = {
    router: useRouter(),
    config: getConfig(),
    userStore: useUserStore(),
    permissionStore: usePermissionStore()
  }

  const state = createAuthState()
  const computedState = createAuthComputed(state)
  const actions = createAuthActions(state, computedState, deps)

  actions.restoreToken()

  return {
    ...state,
    ...computedState,
    ...actions
  }
}

/**
 * 认证状态管理 Store
 */
export const useAuthStore = defineStore('auth', createAuthStore)
