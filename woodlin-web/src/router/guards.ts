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

import type { Router } from 'vue-router'
import { getConfig } from '@/config'
import { useAuthStore, useUserStore, usePermissionStore } from '@/stores'
import { logger } from '@/utils/logger'

/**
 * 白名单路由路径（允许匿名访问）
 */
const WHITE_LIST = ['/login', '/register', '/forgot-password', '/403', '/404', '/500']

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
    if (to.meta.anonymous || WHITE_LIST.includes(to.path)) {
      // 如果已登录且访问登录页，重定向到首页
      if (to.path === config.router.loginPath && authStore.isAuthenticated) {
        next({ path: config.router.homePath })
        return
      }
      next()
      return
    }

    // 检查用户是否已认证
    if (!authStore.isAuthenticated) {
      logger.warn('用户未登录，跳转到登录页')
      next({
        path: config.router.loginPath,
        query: { redirect: to.fullPath } // 保存目标路径，登录后可以跳转回来
      })
      return
    }
    
    // 检查Token是否即将过期
    authStore.checkTokenRefresh()
    
    // 如果用户信息未加载，先加载用户信息
    if (!userStore.isUserInfoLoaded) {
      try {
        logger.log('加载用户信息...')
        await userStore.fetchUserInfo()
        
        // 生成动态路由
        if (!permissionStore.isRoutesGenerated) {
          logger.log('生成动态路由...')
          await permissionStore.generateRoutes(userStore.permissions)
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        
        // 清除认证状态
        authStore.clearToken()
        userStore.clearUserInfo()
        
        // 跳转到登录页
        next({
          path: config.router.loginPath,
          query: { redirect: to.fullPath }
        })
        return
      }
    }
    
    // 如果路由已生成但未添加到路由器，则添加路由
    if (permissionStore.isRoutesGenerated && !permissionStore.isRoutesAdded) {
      logger.log('添加动态路由到路由器...')
      
      // 动态添加路由
      permissionStore.addedRoutes.forEach(route => {
        router.addRoute(route)
      })
      
      // 添加404 catch-all路由（必须在所有动态路由之后）
      const { notFoundRoute } = await import('./routes')
      router.addRoute(notFoundRoute)
      logger.log('404路由已添加')
      
      // 标记路由已添加
      permissionStore.markRoutesAdded()
      
      // 重新导航到目标路由（确保使用新添加的路由）
      next({ ...to, replace: true })
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

    // 检查用户是否有权限
    if (permissions && permissions.length > 0 && !userStore.hasPermission(permissions)) {
      logger.error('用户无权限访问该页面:', to.path)
      logger.error('  需要权限:', permissions)
      logger.error('  用户权限:', userStore.permissions)
      
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
  router.beforeEach((to, from, next) => {
    // TODO: 启动进度条
    // 可以使用nprogress或naive-ui的加载条
    // import NProgress from 'nprogress'
    // NProgress.start()
    next()
  })

  router.afterEach(() => {
    // TODO: 完成进度条
    // NProgress.done()
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
