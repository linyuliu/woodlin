/**
 * @file router/guard.ts
 * @description 全局路由守卫：NProgress、Token 校验、首次进入懒加载并注入动态路由
 * @author yulin
 * @since 2026-01-01
 */
import type { Router } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/modules/user'
import { useRouteStore } from '@/stores/modules/route'
import { getUserRoutes } from '@/api/auth'
import { settings } from '@/config/settings'

NProgress.configure({ showSpinner: false })

/** 不需要登录即可访问的路由白名单 */
const WHITE_LIST = ['/login', '/403', '/404', '/500']

/** 注册导航守卫 */
export function setupGuard(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    NProgress.start()
    const userStore = useUserStore()
    const routeStore = useRouteStore()

    // 1. 未登录
    if (!userStore.token) {
      if (WHITE_LIST.includes(to.path)) {
        next()
      } else {
        next({ path: '/login', query: { redirect: to.fullPath } })
      }
      return
    }

    // 2. 已登录访问 /login → 跳首页
    if (to.path === '/login') {
      next(settings.homePath)
      return
    }

    // 3. 已登录但路由未加载 → 拉取用户信息 + 动态路由
    if (!routeStore.isRoutesLoaded) {
      try {
        if (!userStore.userInfo) {
          await userStore.fetchInfo()
        }
        const items = await getUserRoutes().catch(() => [])
        routeStore.generateRoutes(items)
        // replace: true 让此次跳转命中新注入的路由
        next({ ...to, replace: true })
      } catch (e) {
        console.error('[guard] init routes failed', e)
        userStore.reset()
        next({ path: '/login', query: { redirect: to.fullPath } })
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
