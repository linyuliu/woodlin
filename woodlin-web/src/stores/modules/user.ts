/**
 * @file stores/modules/user.ts
 * @description 当前用户状态：Token、用户信息、角色、权限及登录登出动作
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'
import { getAuthInfo, login as loginApi, logout as logoutApi, type LoginParams } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { LoginResponse, UserInfo } from '@/types/global'

interface UserState {
  token: string | null
  userInfo: UserInfo | null
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    userInfo: null,
    roles: [],
    permissions: [],
  }),
  actions: {
    /** 登录并写入 Token */
    async login(data: LoginParams): Promise<LoginResponse> {
      const res = await loginApi(data)
      this.token = res.token
      this.userInfo = res.user
      this.roles = res.roles ?? []
      this.permissions = res.permissions ?? []
      setToken(res.token)
      return res
    },
    /** 拉取当前用户信息 */
    async fetchInfo(): Promise<void> {
      const info = await getAuthInfo()
      this.userInfo = info.user
      this.roles = info.roles ?? []
      this.permissions = info.permissions ?? []
    },
    /** 登出 */
    async logout(): Promise<void> {
      try {
        await logoutApi()
      } catch {
        /* ignore */
      }
      this.reset()
    },
    /** 清空本地状态 */
    reset(): void {
      this.token = null
      this.userInfo = null
      this.roles = []
      this.permissions = []
      removeToken()
    },
  },
  persist: {
    pick: ['token'],
  },
})
