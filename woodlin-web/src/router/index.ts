/**
 * @file router/index.ts
 * @description 路由实例与静态路由
 * @author yulin
 * @since 2026-05-04
 */
import type { App } from 'vue'
import { createRouter, createWebHashHistory, type Router, type RouteRecordRaw } from 'vue-router'
import { settings } from '@/config/settings'

/** 静态路由（始终存在） */
export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/DefaultLayout/index.vue'),
    redirect: settings.homePath,
    children: [
      {
        path: 'dashboard/workplace',
        name: 'Workplace',
        component: () => import('@/views/dashboard/workplace/index.vue'),
        meta: { title: '工作台', icon: 'vicons:antd:DashboardOutlined', affix: true },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', hidden: true },
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500.vue'),
    meta: { title: '服务异常', hidden: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true },
  },
]

/** 根布局路由的 name，guard 注入动态路由时作为父节点 */
export const LAYOUT_ROUTE_NAME = 'Layout'

let router: Router | null = null

/** 安装路由 */
export function setupRouter(app: App): Router {
  router = createRouter({
    history: createWebHashHistory(),
    routes: staticRoutes,
    scrollBehavior: () => ({ top: 0 }),
  })
  app.use(router)
  return router
}

/** 获取已创建的路由实例 */
export function getRouter(): Router {
  if (!router) {throw new Error('Router not initialized')}
  return router
}
