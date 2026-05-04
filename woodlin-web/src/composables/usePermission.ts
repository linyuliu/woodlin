/**
 * @file composables/usePermission.ts
 * @description 权限/角色判断 composable
 * @author yulin
 * @since 2026-05-04
 */
import { useUserStore } from '@/stores/modules/user'

/** 权限/角色 hook */
export function usePermission() {
  const userStore = useUserStore()
  /** 是否拥有指定权限 */
  const hasPermission = (perm: string): boolean =>
    userStore.permissions.includes('*:*:*') || userStore.permissions.includes(perm)
  /** 是否拥有指定角色 */
  const hasRole = (role: string): boolean =>
    userStore.roles.includes('admin') || userStore.roles.includes(role)
  return { hasPermission, hasRole }
}
