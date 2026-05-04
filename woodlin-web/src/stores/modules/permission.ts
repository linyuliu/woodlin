/**
 * @file stores/modules/permission.ts
 * @description 权限点缓存（Set 形式便于 v-permission 高速判断）
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'

interface PermissionState {
  /** 权限标识集合 */
  perms: Set<string>
  roles: Set<string>
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    perms: new Set<string>(),
    roles: new Set<string>(),
  }),
  actions: {
    /** 设置权限点列表 */
    setPermissions(perms: string[]): void {
      this.perms = new Set(perms)
    },
    /** 设置角色列表 */
    setRoles(roles: string[]): void {
      this.roles = new Set(roles)
    },
    /** 是否拥有指定权限点 */
    has(perm: string): boolean {
      return this.perms.has('*:*:*') || this.perms.has(perm)
    },
    /** 重置 */
    reset(): void {
      this.perms = new Set<string>()
      this.roles = new Set<string>()
    },
  },
})
