/**
 * 权限路由状态管理 Store
 * 
 * @author mumu
 * @description 管理动态路由、菜单权限等
 * @since 2025-01-01
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import { asyncRoutes, constantRoutes } from '@/router/routes'
import { getUserRoutes } from '@/api/auth'
import { logger } from '@/utils/logger'
import AdminLayout from '@/layouts/AdminLayout.vue'

/**
 * 后端路由数据结构
 */
interface BackendRoute {
  id: number | string
  parentId: number | string
  name?: string
  path: string
  component?: string
  redirect?: string
  meta?: {
    title: string
    icon?: string
    hideInMenu?: boolean
    affix?: boolean
    keepAlive?: boolean
    permissions?: string[]
    order?: number
    isFrame?: boolean
  }
  children?: BackendRoute[]
}

/**
 * 权限路由状态管理 Store
 */
export const usePermissionStore = defineStore('permission', () => {
  // ===== 状态 =====
  
  /** 所有路由（包括静态路由和动态路由） */
  const routes = ref<RouteRecordRaw[]>([])
  
  /** 动态添加的路由 */
  const addedRoutes = ref<RouteRecordRaw[]>([])
  
  /** 是否已生成路由 */
  const isRoutesGenerated = ref(false)
  
  /** 菜单列表（用于侧边栏显示） */
  const menuRoutes = ref<RouteRecordRaw[]>([])

  // ===== 计算属性 =====
  
  /** 扁平化的所有路由 */
  const flatRoutes = computed(() => {
    const flat: RouteRecordRaw[] = []
    
    function flatten(routes: RouteRecordRaw[]) {
      routes.forEach(route => {
        flat.push(route)
        if (route.children) {
          flatten(route.children)
        }
      })
    }
    
    flatten(routes.value)
    return flat
  })

  // ===== 方法 =====
  
  /**
   * 过滤异步路由
   * 根据用户权限过滤路由
   * 
   * @param routes 路由配置
   * @param permissions 用户权限列表
   * @returns 过滤后的路由
   */
  function filterAsyncRoutes(
    routes: RouteRecordRaw[],
    permissions: string[]
  ): RouteRecordRaw[] {
    const result: RouteRecordRaw[] = []
    
    routes.forEach(route => {
      const temp = { ...route }
      
      // 检查路由权限
      if (hasRoutePermission(temp, permissions)) {
        // 递归过滤子路由
        if (temp.children) {
          temp.children = filterAsyncRoutes(temp.children, permissions)
        }
        result.push(temp)
      }
    })
    
    return result
  }
  
  /**
   * 检查是否有路由权限
   * 
   * @param route 路由配置
   * @param permissions 用户权限列表
   * @returns 是否有权限
   */
  function hasRoutePermission(route: RouteRecordRaw, permissions: string[]): boolean {
    // 如果路由没有设置权限要求，默认可访问
    const routePermissions = route.meta?.permissions as string[] | undefined
    if (!routePermissions || routePermissions.length === 0) {
      return true
    }
    
    // 检查用户是否拥有路由所需的任一权限
    return routePermissions.some(permission => permissions.includes(permission))
  }
  
  /**
   * 生成路由
   * 
   * @param permissions 用户权限列表
   * @returns 可访问的路由
   */
  async function generateRoutes(permissions: string[]): Promise<RouteRecordRaw[]> {
    let accessedRoutes: RouteRecordRaw[]
    
    logger.log('📋 开始生成路由, 用户权限:', permissions)
    
    try {
      // 从后端获取用户路由
      logger.log('🌐 从后端获取用户路由...')
      const backendRoutes = await getUserRoutes() as unknown as BackendRoute[]
      
      if (backendRoutes && backendRoutes.length > 0) {
        logger.log('✅ 成功获取后端路由:', backendRoutes.length, '个')
        
        // 将后端路由转换为Vue Router格式
        accessedRoutes = convertBackendRoutesToVueRouter(backendRoutes)
        logger.log('✅ 路由转换完成:', accessedRoutes.length, '个')
      } else {
        // 如果后端没有返回路由，使用静态路由作为降级方案
        logger.warn('⚠️ 后端未返回路由，使用静态路由')
        accessedRoutes = useFallbackRoutes(permissions)
      }
    } catch (error) {
      // 如果获取失败，使用静态路由作为降级方案
      logger.error('❌ 获取后端路由失败，使用静态路由:', error)
      accessedRoutes = useFallbackRoutes(permissions)
    }
    
    // 合并静态路由和动态路由
    routes.value = constantRoutes.concat(accessedRoutes)
    addedRoutes.value = accessedRoutes
    menuRoutes.value = routes.value.filter(route => !route.meta?.hideInMenu)
    isRoutesGenerated.value = true
    
    logger.log('✅ 路由已生成:', {
      total: routes.value.length,
      added: addedRoutes.value.length,
      menu: menuRoutes.value.length,
      accessedRoutes: accessedRoutes.map(r => r.path)
    })
    
    return accessedRoutes
  }
  
  /**
   * 使用降级路由（静态路由）
   * 
   * @param permissions 用户权限列表
   * @returns 过滤后的路由
   */
  function useFallbackRoutes(permissions: string[]): RouteRecordRaw[] {
    // 如果权限中包含'*'或'admin'或'super_admin'，则拥有所有权限
    if (permissions.includes('*') || 
        permissions.includes('admin') || 
        permissions.includes('super_admin')) {
      logger.log('🔑 用户拥有全部权限，加载所有路由')
      return asyncRoutes || []
    } else {
      // 根据权限过滤路由
      logger.log('🔍 根据权限过滤路由...')
      return filterAsyncRoutes(asyncRoutes || [], permissions)
    }
  }
  
  /**
   * 将后端路由转换为Vue Router格式
   * 
   * @param backendRoutes 后端路由数据
   * @returns Vue Router路由配置
   */
  function convertBackendRoutesToVueRouter(backendRoutes: BackendRoute[]): RouteRecordRaw[] {
    // 创建根路由，使用AdminLayout作为布局组件
    const rootRoute: RouteRecordRaw = {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: []
    }
    
    // 转换后端路由为子路由
    rootRoute.children = backendRoutes.map(backendRoute => convertSingleRoute(backendRoute))
    
    return [rootRoute]
  }
  
  /**
   * 转换单个后端路由为Vue Router路由
   * 
   * @param backendRoute 后端路由数据
   * @returns Vue Router路由配置
   */
  function convertSingleRoute(backendRoute: BackendRoute): RouteRecordRaw {
    // 构建路由对象 - 使用 any 类型避免 TypeScript 严格检查
    const route: any = {
      path: backendRoute.path,
      name: backendRoute.name,
      meta: {
        title: backendRoute.meta?.title || '',
        icon: backendRoute.meta?.icon,
        hideInMenu: backendRoute.meta?.hideInMenu || false,
        affix: backendRoute.meta?.affix || false,
        keepAlive: backendRoute.meta?.keepAlive || false,
        permissions: backendRoute.meta?.permissions || [],
        order: backendRoute.meta?.order
      }
    }
    
    // 设置重定向
    if (backendRoute.redirect) {
      route.redirect = backendRoute.redirect
    }
    
    // 动态导入组件
    if (backendRoute.component) {
      route.component = loadComponent(backendRoute.component)
    } else {
      // 没有组件的路由，使用默认组件
      route.component = () => import('@/views/error/404.vue')
    }
    
    // 递归处理子路由
    if (backendRoute.children && backendRoute.children.length > 0) {
      route.children = backendRoute.children.map(child => convertSingleRoute(child))
    }
    
    return route as RouteRecordRaw
  }
  
  /**
   * 动态加载组件
   * 
   * @param componentPath 组件路径
   * @returns 组件加载函数
   */
  function loadComponent(componentPath: string) {
    // 处理组件路径
    const path = componentPath.startsWith('@/') 
      ? componentPath.slice(2) 
      : componentPath
    
    // 动态导入组件
    const modules = import.meta.glob('@/views/**/*.vue')
    const componentKey = `/src/views/${path}${path.endsWith('.vue') ? '' : '.vue'}`
    
    if (modules[componentKey]) {
      return modules[componentKey]
    }
    
    // 如果找不到组件，记录警告并返回一个占位组件
    logger.warn(`⚠️ 找不到组件: ${componentPath}`)
    return () => import('@/views/error/404.vue')
  }
  
  /**
   * 清除动态路由
   */
  function clearRoutes() {
    routes.value = []
    addedRoutes.value = []
    menuRoutes.value = []
    isRoutesGenerated.value = false
    
    logger.log('✅ 路由已清除')
  }
  
  /**
   * 根据路径查找路由
   * 
   * @param path 路由路径
   * @returns 找到的路由配置
   */
  function findRouteByPath(path: string): RouteRecordRaw | undefined {
    return flatRoutes.value.find(route => route.path === path)
  }
  
  /**
   * 根据名称查找路由
   * 
   * @param name 路由名称
   * @returns 找到的路由配置
   */
  function findRouteByName(name: string): RouteRecordRaw | undefined {
    return flatRoutes.value.find(route => route.name === name)
  }

  return {
    // 状态
    routes,
    addedRoutes,
    isRoutesGenerated,
    menuRoutes,
    
    // 计算属性
    flatRoutes,
    
    // 方法
    generateRoutes,
    clearRoutes,
    filterAsyncRoutes,
    hasRoutePermission,
    findRouteByPath,
    findRouteByName
  }
})
