/**
 * 路由守卫配置 - 增强版
 *
 * @author mumu
 * @description 优雅的路由守卫实现，参考vue-vben-admin设计
 *              提供登录验证、权限检查、动态路由加载、页面标题设置等功能
 * @since 2025-01-01
 *
 * @example
 * ```typescript
 * import { setupRouterGuards } from './guards'
 * import router from './index'
 *
 * setupRouterGuards(router)
 * ```
 */

import type {NavigationGuardNext, RouteLocationNormalized, Router} from 'vue-router'
import {getConfig} from '@/config'
import {useAppStore, useAuthStore, usePermissionStore, useUserStore} from '@/stores'
import {logger} from '@/utils/logger'

/**
 * 白名单路由路径（允许匿名访问）
 */
const WHITE_LIST = ['/login', '/register', '/forgot-password', '/403', '/404', '/500']

/**
 * 路由是否允许匿名访问
 *
 * @param to 目标路由
 * @returns 是否匿名可访问
 */
function isAnonymousRoute(to: RouteLocationNormalized): boolean {
  return Boolean(to.meta.anonymous) || WHITE_LIST.includes(to.path)
}

/**
 * 跳转到登录页并携带回跳地址
 *
 * @param next 导航回调
 * @param loginPath 登录页路径
 * @param redirect 回跳地址
 */
function redirectToLogin(next: NavigationGuardNext, loginPath: string, redirect: string): void {
  next({
    path: loginPath,
    query: {redirect}
  })
}

/**
 * 补齐动态路由
 *
 * @param permissionStore 权限store
 * @param permissions 用户权限列表
 * @param startLog 开始日志
 * @param failLog 失败日志
 */
async function ensureDynamicRoutes(
  permissionStore: ReturnType<typeof usePermissionStore>,
  permissions: string[],
  startLog: string,
  failLog: string
): Promise<void> {
  if (permissionStore.isRoutesGenerated && permissionStore.addedRoutes.length > 0) {
    return
  }

  logger.log(startLog)
  try {
    await permissionStore.generateRoutes(permissions)
  } catch (error) {
    logger.error(failLog, error)
    logger.warn('动态路由生成失败，继续使用静态路由')
  }
}

/**
 * 确保用户信息和动态路由已就绪
 *
 * @param authStore 认证store
 * @param userStore 用户store
 * @param permissionStore 权限store
 * @returns 是否准备完成
 */
async function ensureUserSessionReady(
  authStore: ReturnType<typeof useAuthStore>,
  userStore: ReturnType<typeof useUserStore>,
  permissionStore: ReturnType<typeof usePermissionStore>
): Promise<boolean> {
  if (!userStore.isUserInfoLoaded) {
    try {
      logger.log('用户信息未加载，开始获取用户信息...')
      await userStore.fetchUserInfo()
      await ensureDynamicRoutes(
        permissionStore,
        userStore.permissions,
        '路由未生成，开始生成动态路由...',
        '生成路由失败，将使用降级方案:'
      )
    } catch (error) {
      logger.error('加载用户信息失败:', error)
      authStore.clearToken()
      userStore.clearUserInfo()
      return false
    }
    return true
  }

  await ensureDynamicRoutes(
    permissionStore,
    userStore.permissions,
    '用户信息已存在，但路由未生成或为空，开始生成动态路由...',
    '生成路由失败:'
  )
  return true
}

/**
 * 将动态路由注册到router
 *
 * @param router 路由实例
 * @param permissionStore 权限store
 */
async function addDynamicRoutes(
  router: Router,
  permissionStore: ReturnType<typeof usePermissionStore>
): Promise<void> {
  logger.log('添加动态路由到路由器...')

  permissionStore.addedRoutes.forEach(route => {
    router.addRoute(route)
  })

  const {notFoundRoute} = await import('./routes')
  router.addRoute(notFoundRoute)
  logger.log('404路由已添加')
  permissionStore.markRoutesAdded()
}

/**
 * 登录验证守卫
 *
 * 检查用户登录状态，未登录则跳转到登录页
 *
 * @param router Vue Router实例
 */
function createAuthGuard(router: Router): void {
  router.beforeEach(async (to, from, next) => {
    const config = getConfig()
    const authStore = useAuthStore()
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 如果路由允许匿名访问，直接放行
    if (isAnonymousRoute(to)) {
      // 如果已登录且访问登录页，重定向到首页
      if (to.path === config.router.loginPath && authStore.isAuthenticated) {
        next({path: config.router.homePath})
        return
      }
      next()
      return
    }

    // 检查用户是否已认证
    if (!authStore.isAuthenticated) {
      logger.warn('用户未登录，跳转到登录页')
      redirectToLogin(next, config.router.loginPath, to.fullPath)
      return
    }

    // 检查Token是否即将过期
    authStore.checkTokenRefresh()

    const isReady = await ensureUserSessionReady(authStore, userStore, permissionStore)
    if (!isReady) {
      redirectToLogin(next, config.router.loginPath, to.fullPath)
      return
    }

    // 如果路由已生成但未添加到路由器，则添加路由
    if (permissionStore.isRoutesGenerated && !permissionStore.isRoutesAdded) {
      await addDynamicRoutes(router, permissionStore)
      // 重新导航到目标路由（确保使用新添加的路由）
      next({...to, replace: true})
      return
    }

    next()
  })
}

/**
 * 权限验证守卫
 *
 * 检查用户是否有权限访问路由
 *
 * @param router Vue Router实例
 */
function createPermissionGuard(router: Router): void {
  router.beforeEach((to, from, next) => {
    const config = getConfig()
    const userStore = useUserStore()

    // 如果未启用权限验证或允许匿名访问，直接放行
    if (!config.router.enablePermission || to.meta.anonymous || WHITE_LIST.includes(to.path)) {
      next()
      return
    }

    // 获取路由需要的权限
    const permissions = to.meta.permissions as string[] | undefined

    // 如果路由没有权限要求，直接放行
    if (!permissions || permissions.length === 0) {
      next()
      return
    }

    // 检查用户是否有权限
    if (!userStore.hasPermission(permissions)) {
      logger.warn('用户无权限访问该页面:', to.path)
      logger.warn('  需要权限:', permissions)
      logger.warn('  用户权限:', userStore.permissions)
      logger.warn('  用户角色:', userStore.roles)

      // 跳转到403页面
      next({ path: '/403', replace: true })
      return
    }

    next()
  })
}

/**
 * 页面标题守卫
 *
 * 根据路由meta信息设置页面标题
 *
 * @param router Vue Router实例
 */
function createTitleGuard(router: Router): void {
  router.afterEach((to) => {
    const config = getConfig()
    const title = to.meta.title as string | undefined

    if (title) {
      document.title = `${title} - ${config.system.title}`
    } else {
      document.title = config.system.title
    }
  })
}

/**
 * 页面加载进度守卫
 *
 * 在路由切换时显示加载进度条
 *
 * @param router Vue Router实例
 */
function createProgressGuard(router: Router): void {
  let timer: number | null = null

  const stopLoading = () => {
    if (timer !== null) {
      window.clearTimeout(timer)
      timer = null
    }
    const appStore = useAppStore()
    appStore.hideLoading()
  }

  router.beforeEach((to, from, next) => {
    if (timer !== null) {
      window.clearTimeout(timer)
    }
    // 延迟展示避免短路由切换闪烁
    timer = window.setTimeout(() => {
      const appStore = useAppStore()
      appStore.showLoading('页面加载中...')
    }, 120)
    next()
  })

  router.afterEach(() => {
    stopLoading()
  })

  router.onError(() => {
    stopLoading()
  })
}

/**
 * 页面缓存守卫
 *
 * 根据路由配置决定是否缓存页面
 *
 * @param router Vue Router实例
 */
function createCacheGuard(router: Router): void {
  router.beforeEach((to, from, next) => {
    const config = getConfig()

    // 如果未启用路由缓存，直接放行
    if (!config.router.enableCache) {
      next()
      return
    }

    // TODO: 实现页面缓存逻辑
    // 可以使用keep-alive配合路由meta信息
    // 参考: https://github.com/vbenjs/vue-vben-admin
    next()
  })
}

/**
 * 路由访问日志守卫
 *
 * 记录用户访问的路由信息（用于审计和分析）
 *
 * @param router Vue Router实例
 */
function createLogGuard(router: Router): void {
  router.afterEach((to, from) => {
    // 记录路由访问日志
    logger.debug(`路由变化: ${from.path} -> ${to.path}`)

    // TODO: 可以将访问日志发送到服务器
    // if (to.meta.logAccess) {
    //   api.post('/system/log/access', {
    //     path: to.path,
    //     params: to.params,
    //     query: to.query,
    //     timestamp: Date.now()
    //   })
    // }
  })
}

/**
 * 设置所有路由守卫
 *
 * 统一配置所有路由守卫，保持代码简洁
 *
 * @param router Vue Router实例
 *
 * @example
 * ```typescript
 * import { createRouter } from 'vue-router'
 * import { setupRouterGuards } from './guards'
 *
 * const router = createRouter({ ... })
 * setupRouterGuards(router)
 * ```
 */
export function setupRouterGuards(router: Router): void {
  // 登录验证守卫（第一优先级）
  createAuthGuard(router)

  // 权限验证守卫（第二优先级）
  createPermissionGuard(router)

  // 页面标题守卫
  createTitleGuard(router)

  // 页面加载进度守卫
  createProgressGuard(router)

  // 页面缓存守卫
  createCacheGuard(router)

  // 路由访问日志守卫
  createLogGuard(router)

  logger.log('路由守卫配置完成')
}

/**
 * 导出白名单，供其他模块使用
 */
export { WHITE_LIST }
