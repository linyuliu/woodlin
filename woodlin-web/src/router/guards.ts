/**
 * 路由守卫配置
 * 
 * @author mumu
 * @description 优雅的路由守卫实现，参考vue-vben-admin设计
 *              提供登录验证、权限检查、页面标题设置等功能
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

/**
 * 白名单路由路径
 * 
 * 这些路由不需要登录验证即可访问
 */
const WHITE_LIST = ['/login', '/register', '/forgot-password']

/**
 * 检查用户是否已登录
 * 
 * @returns 如果用户已登录返回true，否则返回false
 */
function isLoggedIn(): boolean {
  const config = getConfig()
  const token = localStorage.getItem(config.http.tokenKey)
  return !!token
}

/**
 * 检查用户是否有权限访问路由
 * 
 * @param permissions 路由需要的权限列表
 * @returns 如果用户有权限返回true，否则返回false
 */
function hasPermission(permissions?: string[]): boolean {
  // 如果路由不需要权限，直接返回true
  if (!permissions || permissions.length === 0) {
    return true
  }

  // TODO: 从状态管理中获取用户权限并进行比对
  // 这里简化处理，实际项目中应该从Pinia store获取用户权限
  const userPermissions = getUserPermissions()
  
  // 检查用户是否拥有所需权限中的任意一个
  return permissions.some(permission => userPermissions.includes(permission))
}

/**
 * 获取用户权限列表
 * 
 * @returns 用户权限数组
 */
function getUserPermissions(): string[] {
  // TODO: 实际项目中应该从Pinia store或localStorage获取
  // 这里返回空数组作为示例
  return []
}

/**
 * 登录验证守卫
 * 
 * 检查用户登录状态，未登录则跳转到登录页
 * 
 * @param router Vue Router实例
 */
function createAuthGuard(router: Router): void {
  router.beforeEach((to, from, next) => {
    const config = getConfig()
    
    // 如果路由在白名单中，直接放行
    if (WHITE_LIST.includes(to.path)) {
      next()
      return
    }

    // 检查用户是否已登录
    if (!isLoggedIn()) {
      console.warn('🔐 用户未登录，跳转到登录页')
      next({
        path: config.router.loginPath,
        query: { redirect: to.fullPath } // 保存目标路径，登录后可以跳转回来
      })
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
    
    // 如果未启用权限验证，直接放行
    if (!config.router.enablePermission) {
      next()
      return
    }

    // 获取路由需要的权限
    const permissions = to.meta.permissions as string[] | undefined

    // 检查用户是否有权限
    if (!hasPermission(permissions)) {
      console.error('🚫 用户无权限访问该页面')
      // TODO: 跳转到403页面或显示无权限提示
      next({ path: '/403' })
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
    next()
  })

  router.afterEach(() => {
    // TODO: 完成进度条
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
    next()
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
  
  console.log('✅ 路由守卫配置完成')
}

/**
 * 导出白名单，供其他模块使用
 */
export { WHITE_LIST }
