/**
 * @file router/guard.ts
 * @description 全局路由守卫：NProgress、登录态、动态路由注入
 * @author yulin
 * @since 2026-05-04
 */
import type { Router } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/modules/user'
import { useRouteStore } from '@/stores/modules/route'
import { usePermissionStore } from '@/stores/modules/permission'
import { getUserRoutes } from '@/api/auth'

NProgress.configure({ showSpinner: false })

const WHITE_LIST = ['/login', '/403', '/404', '/500']

/** 注册导航守卫 */
export function setupGuard(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    NProgress.start()
    const userStore = useUserStore()
    const routeStore = useRouteStore()
    const permStore = usePermissionStore()

    if (!userStore.token) {
      if (WHITE_LIST.includes(to.path)) {
        next()
      } else {
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      }
      return
    }

    if (to.path === '/login') {
      next('/')
      return
    }

    if (!routeStore.loaded) {
      try {
        if (!userStore.userInfo) {
          await userStore.fetchInfo()
        }
        permStore.setPermissions(userStore.permissions)
        permStore.setRoles(userStore.roles)
        const items = await getUserRoutes().catch(() => [])
        const records = routeStore.generateRoutes(items)
        records.forEach((r) => router.addRoute(r))
        next({ ...to, replace: true })
      } catch (e) {
        console.error('[guard] init routes failed', e)
        userStore.reset()
        next('/login')
      }
      return
    }

    next()
  })

  router.afterEach(() => {
    NProgress.done()
  })

  router.onError(() => {
    NProgress.done()
  })
}
