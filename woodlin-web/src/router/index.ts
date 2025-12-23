/**
 * Vue Router路由配置
 * 
 * @author mumu
 * @description 定义应用的路由结构和导航配置，包括页面路由、权限路由等
 *              使用优雅的路由守卫系统，参考vue-vben-admin设计
 * @since 2025-01-01
 */

import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'
import { constantRoutes } from './routes'

/**
 * 创建路由实例
 * 使用HTML5 History模式进行路由管理
 * 
 * 注意：动态路由会在用户登录后根据权限动态添加
 */
const router = createRouter({
  // 使用HTML5历史模式，需要服务器配置支持
  history: createWebHistory(import.meta.env.BASE_URL),
  // 初始只加载静态路由（登录页、错误页等）
  routes: constantRoutes,
  // 滚动行为：切换路由时滚动到顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

/**
 * 配置路由守卫
 * 
 * 包括登录验证、权限检查、页面标题设置、动态路由加载等
 */
setupRouterGuards(router)

export default router
