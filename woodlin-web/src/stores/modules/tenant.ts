/**
 * @file stores/modules/tenant.ts
 * @description 当前租户上下文
 * @author yulin
 * @since 2026-05-04
 */
import { defineStore } from 'pinia'

interface TenantState {
  tenantId: string | null
  tenantName: string | null
}

export const useTenantStore = defineStore('tenant', {
  state: (): TenantState => ({
    tenantId: null,
    tenantName: null,
  }),
  actions: {
    /** 设置当前租户 */
    setTenant(id: string | null, name: string | null = null): void {
      this.tenantId = id
      this.tenantName = name
    },
    /** 清空租户信息 */
    clear(): void {
      this.tenantId = null
      this.tenantName = null
    },
  },
  persist: {
    key: 'woodlin_tenant',
  },
})
