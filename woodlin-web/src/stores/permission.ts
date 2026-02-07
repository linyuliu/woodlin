/**
 * 权限路由状态管理 Store
 *
 * @author mumu
 * @description 管理动态路由、菜单权限等
 * @since 2025-01-01
 */

import {computed, nextTick, ref, shallowRef} from 'vue'
import {defineStore} from 'pinia'
import type {RouteRecordRaw} from 'vue-router'
import {asyncRoutes, constantRoutes} from '@/router/routes'
import {LAYOUT} from '@/router/route-constants'
import {resolveRouteComponent} from '@/router/route-loader'
import {getUserRoutes} from '@/api/auth'
import {logger} from '@/utils/logger'
import {getConfig} from '@/config'

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
  const routes = shallowRef<RouteRecordRaw[]>([])

  /** 动态添加的路由 */
  const addedRoutes = shallowRef<RouteRecordRaw[]>([])

  /** 是否已生成路由 */
  const isRoutesGenerated = ref(false)

  /** 是否已将路由添加到路由器 */
  const isRoutesAdded = ref(false)

  /** 菜单列表（用于侧边栏显示） */
  const menuRoutes = shallowRef<RouteRecordRaw[]>([])

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
   * 优化：传入权限Set避免重复创建
   *
   * @param routes 路由配置
   * @param permissions 用户权限列表
   * @returns 过滤后的路由
   */
  function filterAsyncRoutes(
    routes: RouteRecordRaw[],
    permissions: string[]
  ): RouteRecordRaw[] {
    const permissionSet = new Set(permissions)

    const filterRoutes = (source: RouteRecordRaw[]): RouteRecordRaw[] => {
      const result: RouteRecordRaw[] = []

      source.forEach(route => {
        const temp = {...route}

        if (hasRoutePermission(temp, permissionSet)) {
          if (temp.children) {
            temp.children = filterRoutes(temp.children)
          }
          result.push(temp)
        }
      })

      return result
    }

    return filterRoutes(routes)
  }

  /**
   * 获取无权限要求的基础路由
   * 用于安全降级，返回所有不需要特定权限的路由
   *
   * @param routes 路由列表
   * @returns 无权限要求的路由
   */
  function getPermissionFreeRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
    return routes.filter(route => {
      return !route.meta?.permissions || (route.meta.permissions as string[]).length === 0
    })
  }

  /**
   * 检查是否有路由权限
   * 优化：使用Set进行权限查找，从O(n*m)降为O(n)
   *
   * @param route 路由配置
   * @param permissionSet 用户权限Set
   * @returns 是否有权限
   */
  function hasRoutePermission(route: RouteRecordRaw, permissionSet: Set<string>): boolean {
    // 如果用户拥有超级权限，允许访问所有路由
    if (permissionSet.has('*') ||
      permissionSet.has('admin') ||
        permissionSet.has('super_admin') ||
        permissionSet.has('ROLE_ADMIN') ||
        permissionSet.has('ROLE_SUPER_ADMIN')) {
      return true
    }

    // 如果路由没有设置权限要求，默认可访问
    const routePermissions = route.meta?.permissions as string[] | undefined
    if (!routePermissions || routePermissions.length === 0) {
      return true
    }

    // 检查用户是否拥有路由所需的任一权限
    return routePermissions.some(permission => permissionSet.has(permission))
  }

  /**
   * 生成路由
   *
   * @param permissions 用户权限列表
   * @returns 可访问的路由
   */
  async function generateRoutes(permissions: string[]): Promise<RouteRecordRaw[]> {
    let accessedRoutes: RouteRecordRaw[]

    logger.log('开始生成路由, 用户权限:', permissions)

    try {
      // 从后端获取用户路由
      logger.log('从后端获取用户路由...')
      const backendRoutes = await getUserRoutes() as unknown as BackendRoute[]

      if (backendRoutes && backendRoutes.length > 0) {
        logger.log('成功获取后端路由:', backendRoutes)

        // 将后端路由转换为Vue Router格式
        accessedRoutes = convertBackendRoutesToVueRouter(backendRoutes)
        logger.log('路由转换完成:', accessedRoutes.length, '个')
      } else {
        // 如果后端没有返回路由，使用静态路由作为降级方案
        logger.warn('后端未返回路由，使用静态路由')
        accessedRoutes = useFallbackRoutes(permissions)
      }
    } catch (error) {
      // 如果获取失败，使用静态路由作为降级方案
      logger.error('获取后端路由失败，使用静态路由:', error)
      accessedRoutes = useFallbackRoutes(permissions)
    }

    accessedRoutes = injectLocalExtensionRoutes(accessedRoutes)

    // 合并静态路由和动态路由
    routes.value = constantRoutes.concat(accessedRoutes)
    addedRoutes.value = accessedRoutes

    // 仅保留布局下的子路由用于菜单（避免把登录等基础路由展示在侧边栏）
    const rootLayout = accessedRoutes.find(r => r.path === '/' || r.children?.length)
    menuRoutes.value = (rootLayout?.children || accessedRoutes).filter(route => !route.meta?.hideInMenu)
    isRoutesGenerated.value = true

    // 持久化路由生成状态到localStorage
    try {
      localStorage.setItem('routesGenerated', 'true')
      localStorage.setItem('routesGeneratedTime', String(Date.now()))
    } catch (error) {
      logger.error('保存路由状态到localStorage失败:', error)
    }

    logger.log('路由已生成:', {
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
    logger.log('使用降级路由（静态路由）, 用户权限:', permissions)

    // 如果权限中包含'*'或admin相关角色，则拥有所有权限
    const hasAllPermissions = permissions.some(p =>
      p === '*' ||
      p === 'admin' ||
      p === 'super_admin' ||
      p === 'ROLE_ADMIN' ||
      p === 'ROLE_SUPER_ADMIN'
    )

    if (hasAllPermissions) {
      logger.log('用户拥有全部权限，加载所有路由')
      return asyncRoutes || []
    }

    // 如果用户权限列表为空，只加载基础路由（开发模式友好）
    // 生产环境应确保用户有适当的权限分配
    if (!permissions || permissions.length === 0) {
      logger.warn('用户权限为空，仅加载基础路由（请确认是否为开发环境）')
      const config = getConfig()

      // 在开发环境下，返回所有路由；生产环境下，仅返回无权限要求的基础路由
      if (import.meta.env.DEV || !config.router.enablePermission) {
        logger.warn('开发模式：加载所有路由')
        return asyncRoutes || []
      } else {
        // 生产环境：仅返回无权限要求的路由（如用户设置、个人中心等）
        logger.warn('生产模式：仅加载无权限要求的基础路由')
        return getPermissionFreeRoutes(asyncRoutes || [])
      }
    }

    // 根据权限过滤路由
    logger.log('根据权限过滤路由...')
    const filtered = filterAsyncRoutes(asyncRoutes || [], permissions)

    // 如果过滤后没有路由，返回无权限要求的基础路由（避免用户完全无法访问）
    if (!filtered || filtered.length === 0) {
      logger.warn('过滤后没有可用路由，返回无权限要求的基础路由作为降级方案')
      // 返回不需要权限的路由（如用户设置、个人中心等）
      return getPermissionFreeRoutes(asyncRoutes || [])
    }

    return filtered
  }

  /**
   * 注入本地扩展路由（后端未下发但前端必须可用的页面）
   */
  function injectLocalExtensionRoutes(accessedRoutes: RouteRecordRaw[]): RouteRecordRaw[] {
    const rootRoute = accessedRoutes.find(route => route.path === '/' || Array.isArray(route.children))
    if (!rootRoute || !Array.isArray(rootRoute.children) || rootRoute.children.length === 0) {
      return accessedRoutes
    }

    const datasourceRoute = rootRoute.children.find(route => {
      const normalized = normalizePath(route.path)
      return normalized === 'datasource' || normalized.endsWith('/datasource')
    })

    if (!datasourceRoute) {
      return accessedRoutes
    }

    if (!Array.isArray(datasourceRoute.children)) {
      datasourceRoute.children = []
    }

    const exists = datasourceRoute.children.some(route => {
      const normalized = normalizePath(route.path)
      return route.name === 'DatasourceWorkspace' ||
        normalized === 'workspace/:code' ||
        normalized.endsWith('/workspace/:code')
    })

    if (exists) {
      return accessedRoutes
    }

    datasourceRoute.children.push({
      path: 'workspace/:code',
      name: 'DatasourceWorkspace',
      component: () => import('@/views/datasource/DatasourceWorkspace.vue'),
      meta: {
        title: '元数据工作台',
        hideInMenu: true,
        activeMenu: '/datasource/list',
        permissions: ['datasource:list:view']
      }
    })

    logger.log('已注入本地扩展路由: DatasourceWorkspace')
    return accessedRoutes
  }

  function normalizePath(path?: string): string {
    if (!path) {
      return ''
    }
    return String(path).replace(/^\//, '')
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
      component: LAYOUT,
      redirect: '/dashboard',
      children: []
    }

    // 转换后端路由为子路由（后端已返回树形结构，直接转换即可）
    rootRoute.children = backendRoutes.map(backendRoute => convertSingleRoute(backendRoute))

    logger.log('后端路由已转换为Vue Router格式:', {
      routeCount: rootRoute.children?.length || 0,
      routes: rootRoute.children?.map(r => ({ path: r.path, name: r.name }))
    })

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

    // 动态导入组件 / 布局 / 占位容器
    route.component = resolveRouteComponent(backendRoute.component)
    if (backendRoute.component) {
      logger.debug(`加载组件: ${backendRoute.component} for route: ${backendRoute.path}`)
    } else {
      logger.debug(`目录路由（使用RouterView）: ${backendRoute.path}`)
    }

    // 递归处理子路由
    if (backendRoute.children && backendRoute.children.length > 0) {
      route.children = backendRoute.children.map(child => convertSingleRoute(child))
      logger.debug(`路由 ${backendRoute.path} 有 ${backendRoute.children.length} 个子路由`)
    }

    return route as RouteRecordRaw
  }

  /**
   * 从localStorage恢复路由生成状态
   */
  function restoreRoutesState(): boolean {
    try {
      const routesGenerated = localStorage.getItem('routesGenerated')
      const routesGeneratedTime = localStorage.getItem('routesGeneratedTime')

      if (routesGenerated === 'true' && routesGeneratedTime) {
        const generatedTime = Number(routesGeneratedTime)
        const now = Date.now()
        // 从配置获取路由缓存过期时间
        const config = getConfig()
        const expirationTime = config.router.routeCacheExpiration

        if (now - generatedTime < expirationTime) {
          isRoutesGenerated.value = true
          logger.log('从localStorage恢复路由状态成功')
          return true
        } else {
          logger.log('路由状态已过期，需要重新生成')
        }
      }
    } catch (error) {
      logger.error('从localStorage恢复路由状态失败:', error)
    }
    return false
  }

  /**
   * 清除动态路由
   */
  function clearRoutes() {
    routes.value = []
    addedRoutes.value = []
    menuRoutes.value = []
    isRoutesGenerated.value = false
    isRoutesAdded.value = false

    // 从localStorage清除路由状态
    try {
      localStorage.removeItem('routesGenerated')
      localStorage.removeItem('routesGeneratedTime')
    } catch (error) {
      logger.error('清除localStorage路由状态失败:', error)
    }

    logger.log('路由已清除')
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

  /**
   * 标记路由已添加到路由器
   */
  function markRoutesAdded() {
    isRoutesAdded.value = true
    logger.log('路由已标记为已添加到路由器')
  }

  // 初始化：从localStorage恢复路由状态（延迟执行，避免阻塞主线程）
  nextTick(() => {
    restoreRoutesState()
  })

  return {
    // 状态
    routes,
    addedRoutes,
    isRoutesGenerated,
    isRoutesAdded,
    menuRoutes,

    // 计算属性
    flatRoutes,

    // 方法
    generateRoutes,
    clearRoutes,
    restoreRoutesState,
    markRoutesAdded,
    filterAsyncRoutes,
    hasRoutePermission,
    findRouteByPath,
    findRouteByName
  }
})
