import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/DashboardView.vue'),
        },
        {
          path: 'user',
          name: 'UserManagement',
          component: () => import('../views/system/UserView.vue'),
        },
        {
          path: 'role',
          name: 'RoleManagement',
          component: () => import('../views/system/RoleView.vue'),
        },
        {
          path: 'dept',
          name: 'DeptManagement',
          component: () => import('../views/system/DeptView.vue'),
        },
        {
          path: 'tenant-list',
          name: 'TenantList',
          component: () => import('../views/tenant/TenantView.vue'),
        },
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue'),
    },
  ],
})

export default router
