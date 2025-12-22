/**
 * Vue Router路由配置
 * 
 * @author mumu
 * @description 定义应用的路由结构和导航配置，包括页面路由、权限路由等
 *              使用优雅的路由守卫系统，参考vue-vben-admin设计
 * @since 2025-01-01
 */

import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { setupRouterGuards } from './guards'

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
      component: AdminLayout,
      // 默认重定向到仪表板页面
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: {
            title: '仪表板',
            icon: 'dashboard-outline',
            // 路由权限配置（可选）
            // permissions: ['dashboard:view']
          }
        },
        {
          path: 'user',
          name: 'UserManagement',
          component: () => import('@/views/system/UserView.vue'),
          meta: {
            title: '用户管理',
            icon: 'people-outline',
            // permissions: ['system:user:view']
          }
        },
        {
          path: 'role',
          name: 'RoleManagement',
          component: () => import('@/views/system/RoleView.vue'),
          meta: {
            title: '角色管理',
            icon: 'shield-outline',
            // permissions: ['system:role:view']
          }
        },
        {
          path: 'dept',
          name: 'DeptManagement',
          component: () => import('@/views/system/DeptView.vue'),
          meta: {
            title: '部门管理',
            icon: 'business-outline',
            // permissions: ['system:dept:view']
          }
        },
        {
          path: 'system-settings',
          name: 'SystemSettings',
          component: () => import('@/views/system/SystemSettingsView.vue'),
          meta: {
            title: '系统设置',
            icon: 'settings-outline',
            // permissions: ['system:settings:view']
          }
        },
        {
          path: 'tenant-list',
          name: 'TenantList',
          component: () => import('@/views/tenant/TenantView.vue'),
          meta: {
            title: '租户管理',
            icon: 'home-outline',
            // permissions: ['system:tenant:view']
          }
        },
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        title: '登录',
        hideInMenu: true
      }
    },
  ],
})

/**
 * 配置路由守卫
 * 
 * 包括登录验证、权限检查、页面标题设置等
 */
setupRouterGuards(router)

export default router
