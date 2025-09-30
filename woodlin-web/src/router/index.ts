/**
 * Vue Router路由配置
 * 
 * @author mumu
 * @description 定义应用的路由结构和导航配置，包括页面路由、权限路由等
 * @since 2025-01-01
 */

import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'

/**
 * 创建路由实例
 * 使用HTML5 History模式进行路由管理
 */
const router = createRouter({
  // 使用HTML5历史模式，需要服务器配置支持
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      // 默认重定向到仪表板页面
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: {
            title: '仪表板',
            icon: 'dashboard-outline'
          }
        },
        {
          path: 'user',
          name: 'UserManagement',
          component: () => import('../views/system/UserView.vue'),
          meta: {
            title: '用户管理',
            icon: 'people-outline'
          }
        },
        {
          path: 'role',
          name: 'RoleManagement',
          component: () => import('../views/system/RoleView.vue'),
          meta: {
            title: '角色管理',
            icon: 'shield-outline'
          }
        },
        {
          path: 'dept',
          name: 'DeptManagement',
          component: () => import('../views/system/DeptView.vue'),
          meta: {
            title: '部门管理',
            icon: 'business-outline'
          }
        },
        {
          path: 'system-settings',
          name: 'SystemSettings',
          component: () => import('../views/system/SystemSettingsView.vue'),
          meta: {
            title: '系统设置',
            icon: 'settings-outline'
          }
        },
        {
          path: 'tenant-list',
          name: 'TenantList',
          component: () => import('../views/tenant/TenantView.vue'),
          meta: {
            title: '租户管理',
            icon: 'home-outline'
          }
        },
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        title: '登录',
        hideInMenu: true
      }
    },
  ],
})

/**
 * 路由守卫 - 全局前置守卫
 * 在每次路由跳转前执行，用于权限验证、登录状态检查等
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Woodlin管理系统`
  }
  
  // 这里可以添加权限验证逻辑
  // 例如：检查用户是否已登录，是否有访问权限等
  
  next()
})

export default router
