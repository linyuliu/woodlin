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
    
    console.log('📋 开始生成路由, 用户权限:', permissions)
    
    // 如果权限中包含'*'或'admin'或'super_admin'，则拥有所有权限
    if (permissions.includes('*') || 
        permissions.includes('admin') || 
        permissions.includes('super_admin')) {
      console.log('🔑 用户拥有全部权限，加载所有路由')
      accessedRoutes = asyncRoutes || []
    } else {
      // 根据权限过滤路由
      console.log('🔍 根据权限过滤路由...')
      accessedRoutes = filterAsyncRoutes(asyncRoutes || [], permissions)
    }
    
    // 合并静态路由和动态路由
    routes.value = constantRoutes.concat(accessedRoutes)
    addedRoutes.value = accessedRoutes
    menuRoutes.value = routes.value.filter(route => !route.meta?.hideInMenu)
    isRoutesGenerated.value = true
    
    console.log('✅ 路由已生成:', {
      total: routes.value.length,
      added: addedRoutes.value.length,
      menu: menuRoutes.value.length,
      accessedRoutes: accessedRoutes.map(r => r.path)
    })
    
    return accessedRoutes
  }
  
  /**
   * 清除动态路由
   */
  function clearRoutes() {
    routes.value = []
    addedRoutes.value = []
    menuRoutes.value = []
    isRoutesGenerated.value = false
    
    console.log('✅ 路由已清除')
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
