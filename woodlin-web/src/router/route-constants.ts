import type { RouteComponent } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

/**
 * 全局布局占位符，供路由配置和动态路由转换复用
 */
export const LAYOUT: RouteComponent = AdminLayout
