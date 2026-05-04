/**
 * @file stores/modules/user.ts
 * @description 当前用户状态：Token、用户信息、角色、权限及登录登出动作
 * @author yulin
 * @since 2026-01-01
 */
import { defineStore } from 'pinia'
import {
  getAuthInfo,
  getUserRoutes,
  login as loginApi,
  logout as logoutApi,
  type LoginParams,
} from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { LoginResponse, UserInfo } from '@/types/global'
import { useRouteStore } from '@/stores/modules/route'
import { usePermissionStore } from '@/stores/modules/permission'

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
  getters: {
    /** 是否已登录 */
    isLoggedIn(state): boolean {
      return !!state.token
    },
  },
  actions: {
    /**
     * 登录：写入 Token、用户信息，并立即拉取 + 注入动态路由
     * @param data 登录参数
     */
    async login(data: LoginParams): Promise<LoginResponse> {
      const res = await loginApi(data)
      this.token = res.token
      this.userInfo = res.user
      this.roles = res.roles ?? []
      this.permissions = res.permissions ?? []
      setToken(res.token)
      const permStore = usePermissionStore()
      permStore.setRoles(this.roles)
      permStore.setPermissions(this.permissions)
      // 拉取并生成动态路由（强制重置后再生成，避免上次会话残留）
      const routeStore = useRouteStore()
      routeStore.resetRoutes()
      const routes = await getUserRoutes().catch(() => [] as never)
      routeStore.generateRoutes(routes)
      return res
    },
    /** 拉取当前用户信息（不重新生成路由） */
    async fetchInfo(): Promise<void> {
      const info = await getAuthInfo()
      this.userInfo = info.user
      this.roles = info.roles ?? []
      this.permissions = info.permissions ?? []
      const permStore = usePermissionStore()
      permStore.setRoles(this.roles)
      permStore.setPermissions(this.permissions)
    },
    /** 登出：清空 Token / 用户 / 路由 / 权限 */
    async logout(): Promise<void> {
      try {
        await logoutApi()
      } catch {
        /* ignore */
      }
      this.reset()
    },
    /** 仅清空本地状态（不调用接口） */
    reset(): void {
      this.token = null
      this.userInfo = null
      this.roles = []
      this.permissions = []
      removeToken()
      usePermissionStore().reset()
      useRouteStore().resetRoutes()
    },
    /**
     * 是否拥有指定权限点（支持超级权限 *:*:*）
     * @param perm 权限标识
     */
    hasPermission(perm: string): boolean {
      if (!perm) return true
      return this.permissions.includes('*:*:*') || this.permissions.includes(perm)
    },
    /**
     * 是否拥有指定角色（支持超级角色 admin）
     * @param role 角色标识
     */
    hasRole(role: string): boolean {
      if (!role) return true
      return this.roles.includes('admin') || this.roles.includes(role)
    },
  },
  persist: {
    pick: ['token'],
  },
})
