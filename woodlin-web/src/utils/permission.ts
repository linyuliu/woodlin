/**
 * @file utils/permission.ts
 * @description 权限/角色判断辅助函数
 * @author yulin
 * @since 2026-05-04
 */
import { useUserStore } from '@/stores/modules/user'

/** 判断当前用户是否拥有指定权限 */
export function hasPermission(perm: string | string[]): boolean {
  const store = useUserStore()
  if (store.permissions.includes('*:*:*')) {return true}
  const perms = Array.isArray(perm) ? perm : [perm]
  return perms.some((p) => store.permissions.includes(p))
}

/** 判断当前用户是否拥有指定角色 */
export function hasRole(role: string | string[]): boolean {
  const store = useUserStore()
  if (store.roles.includes('admin')) {return true}
  const roles = Array.isArray(role) ? role : [role]
  return roles.some((r) => store.roles.includes(r))
}
