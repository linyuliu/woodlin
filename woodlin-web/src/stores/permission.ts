/**
 * 权限路由状态管理 Store
 *
 * @author mumu
 * @description 管理动态路由、菜单权限等
 * @since 2025-01-01
 */

import {computed, nextTick, ref, shallowRef, type ComputedRef, type Ref, type ShallowRef} from 'vue'
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

type PermissionStateRefs = {
  routes: ShallowRef<RouteRecordRaw[]>
  addedRoutes: ShallowRef<RouteRecordRaw[]>
  menuRoutes: ShallowRef<RouteRecordRaw[]>
  isRoutesGenerated: Ref<boolean>
  isRoutesAdded: Ref<boolean>
}

/**
 * 展平路由树
 *
 * @param routeTree 路由树
 * @returns 扁平路由
 */
function flattenRoutes(routeTree: RouteRecordRaw[]): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []

  const walk = (nodes: RouteRecordRaw[]) => {
    nodes.forEach((node) => {
      result.push(node)
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }

  walk(routeTree)
  return result
}

/**
 * 检查是否有路由权限
 *
 * @param route 路由配置
 * @param permissionSet 用户权限Set
 * @returns 是否有权限
 */
function hasRoutePermission(route: RouteRecordRaw, permissionSet: Set<string>): boolean {
  if (permissionSet.has('*')) {
    return true
  }
  const routePermissions = route.meta?.permissions as string[] | undefined
  if (!routePermissions || routePermissions.length === 0) {
    return true
  }
  return routePermissions.some((permission) => permissionSet.has(permission))
}

/**
 * 根据权限过滤路由
 *
 * @param routes 路由配置
 * @param permissions 用户权限
 * @returns 过滤结果
 */
function filterAsyncRoutes(routes: RouteRecordRaw[], permissions: string[]): RouteRecordRaw[] {
  const permissionSet = new Set(permissions)

  const filterTree = (source: RouteRecordRaw[]): RouteRecordRaw[] => {
    const result: RouteRecordRaw[] = []
    source.forEach((route) => {
      const copied = {...route}
      if (!hasRoutePermission(copied, permissionSet)) {
        return
      }
      if (copied.children?.length) {
        copied.children = filterTree(copied.children)
      }
      result.push(copied)
    })
    return result
  }

  return filterTree(routes)
}

/**
 * 获取无权限要求的路由
 *
 * @param routes 路由列表
 * @returns 无权限要求的路由
 */
function getPermissionFreeRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  return routes.filter((route) => {
    const permissions = route.meta?.permissions as string[] | undefined
    return !permissions || permissions.length === 0
  })
}

/**
 * 根据空权限场景决定降级路由
 *
 * @returns 降级路由
 */
function resolveEmptyPermissionRoutes(): RouteRecordRaw[] {
  logger.warn('用户权限为空，仅加载基础路由（请确认是否为开发环境）')
  const config = getConfig()

  if (import.meta.env.DEV || !config.router.enablePermission) {
    logger.warn('开发模式：加载所有路由')
    return asyncRoutes || []
  }

  logger.warn('生产模式：仅加载无权限要求的基础路由')
  return getPermissionFreeRoutes(asyncRoutes || [])
}

/**
 * 创建降级路由计算器
 *
 * @returns 计算函数
 */
function createFallbackResolver(): (permissions: string[]) => RouteRecordRaw[] {
  return (permissions: string[]) => {
    logger.log('使用降级路由（静态路由）, 用户权限:', permissions)

    if (permissions.includes('*')) {
      logger.log('用户拥有全部权限，加载所有路由')
      return asyncRoutes || []
    }

    if (permissions.length === 0) {
      return resolveEmptyPermissionRoutes()
    }

    logger.log('根据权限过滤路由...')
    const filtered = filterAsyncRoutes(asyncRoutes || [], permissions)
    if (filtered.length > 0) {
      return filtered
    }

    logger.warn('过滤后没有可用路由，返回无权限要求的基础路由作为降级方案')
    return getPermissionFreeRoutes(asyncRoutes || [])
  }
}

/**
 * 构建路由 meta
 *
 * @param meta 后端 meta
 * @returns 前端 meta
 */
function buildRouteMeta(meta?: BackendRoute['meta']): NonNullable<RouteRecordRaw['meta']> {
  const mergedMeta = {
    title: '',
    hideInMenu: false,
    affix: false,
    keepAlive: false,
    permissions: [] as string[],
    ...meta
  }

  return {
    title: mergedMeta.title,
    icon: mergedMeta.icon,
    hideInMenu: mergedMeta.hideInMenu,
    affix: mergedMeta.affix,
    keepAlive: mergedMeta.keepAlive,
    permissions: mergedMeta.permissions,
    order: mergedMeta.order
  }
}

/**
 * 解析组件并输出日志
 *
 * @param component 组件标识
 * @param path 路由路径
 * @returns 解析组件
 */
function resolveAndLogRouteComponent(component: string | undefined, path: string): RouteRecordRaw['component'] {
  const resolved = resolveRouteComponent(component)
  if (component) {
    logger.debug(`加载组件: ${component} for route: ${path}`)
  } else {
    logger.debug(`目录路由（使用RouterView）: ${path}`)
  }
  return resolved
}

/**
 * 转换单个后端路由
 *
 * @param backendRoute 后端路由
 * @returns 前端路由
 */
function convertSingleRoute(backendRoute: BackendRoute): RouteRecordRaw {
  const route: Partial<RouteRecordRaw> = {
    path: backendRoute.path,
    name: backendRoute.name,
    meta: buildRouteMeta(backendRoute.meta),
    component: resolveAndLogRouteComponent(backendRoute.component, backendRoute.path)
  }

  if (backendRoute.redirect) {
    route.redirect = backendRoute.redirect
  }

  const children = backendRoute.children || []
  if (children.length > 0) {
    route.children = children.map(convertSingleRoute)
    logger.debug(`路由 ${backendRoute.path} 有 ${children.length} 个子路由`)
  }

  return route as RouteRecordRaw
}

/**
 * 将后端路由树转换为 Vue Router 根路由
 *
 * @param backendRoutes 后端路由树
 * @returns Vue Router 路由
 */
function convertBackendRoutesToVueRouter(backendRoutes: BackendRoute[]): RouteRecordRaw[] {
  const rootRoute: RouteRecordRaw = {
    path: '/',
    component: LAYOUT,
    redirect: '/dashboard',
    children: backendRoutes.map(convertSingleRoute)
  }

  logger.log('后端路由已转换为Vue Router格式:', {
    routeCount: rootRoute.children?.length || 0,
    routes: rootRoute.children?.map((r) => ({path: r.path, name: r.name}))
  })
  return [rootRoute]
}

/**
 * 从后端或降级逻辑加载可访问路由
 *
 * @param permissions 用户权限
 * @param resolveFallbackRoutes 降级计算器
 * @returns 可访问路由
 */
async function loadAccessedRoutes(
  permissions: string[],
  resolveFallbackRoutes: (permissions: string[]) => RouteRecordRaw[]
): Promise<RouteRecordRaw[]> {
  try {
    logger.log('从后端获取用户路由...')
    const backendRoutes = (await getUserRoutes()) as unknown as BackendRoute[]
    if (backendRoutes && backendRoutes.length > 0) {
      logger.log('成功获取后端路由:', backendRoutes)
      const routes = convertBackendRoutesToVueRouter(backendRoutes)
      logger.log('路由转换完成:', routes.length, '个')
      return routes
    }
    logger.warn('后端未返回路由，使用静态路由')
    return resolveFallbackRoutes(permissions)
  } catch (error) {
    logger.error('获取后端路由失败，使用静态路由:', error)
    return resolveFallbackRoutes(permissions)
  }
}

/**
 * 从动态路由推导菜单路由
 *
 * @param accessedRoutes 动态路由
 * @returns 菜单路由
 */
function deriveMenuRoutes(accessedRoutes: RouteRecordRaw[]): RouteRecordRaw[] {
  const rootLayout = accessedRoutes.find((route) => route.path === '/' || route.children?.length)
  return (rootLayout?.children || accessedRoutes).filter((route) => !route.meta?.hideInMenu)
}

/**
 * 持久化路由状态
 */
function persistRoutesGeneratedState() {
  try {
    localStorage.setItem('routesGenerated', 'true')
    localStorage.setItem('routesGeneratedTime', String(Date.now()))
  } catch (error) {
    logger.error('保存路由状态到localStorage失败:', error)
  }
}

/**
 * 清理持久化路由状态
 */
function clearRoutesGeneratedState() {
  try {
    localStorage.removeItem('routesGenerated')
    localStorage.removeItem('routesGeneratedTime')
  } catch (error) {
    logger.error('清除localStorage路由状态失败:', error)
  }
}

/**
 * 恢复路由生成状态
 *
 * @param isRoutesGenerated 路由生成标记
 * @returns 是否恢复成功
 */
function restoreRoutesGeneratedState(isRoutesGenerated: Ref<boolean>): boolean {
  try {
    const routesGenerated = localStorage.getItem('routesGenerated')
    const routesGeneratedTime = localStorage.getItem('routesGeneratedTime')
    if (routesGenerated !== 'true' || !routesGeneratedTime) {
      return false
    }

    const generatedTime = Number(routesGeneratedTime)
    const now = Date.now()
    const expirationTime = getConfig().router.routeCacheExpiration

    if (now - generatedTime < expirationTime) {
      isRoutesGenerated.value = true
      logger.log('从localStorage恢复路由状态成功')
      return true
    }
    logger.log('路由状态已过期，需要重新生成')
  } catch (error) {
    logger.error('从localStorage恢复路由状态失败:', error)
  }
  return false
}

/**
 * 创建生成路由 action
 *
 * @param state store 状态
 * @param resolveFallbackRoutes 降级计算器
 * @returns action
 */
function createGenerateRoutesAction(
  state: PermissionStateRefs,
  resolveFallbackRoutes: (permissions: string[]) => RouteRecordRaw[]
) {
  return async (permissions: string[]): Promise<RouteRecordRaw[]> => {
    logger.log('开始生成路由, 用户权限:', permissions)
    const accessedRoutes = await loadAccessedRoutes(permissions, resolveFallbackRoutes)

    state.routes.value = constantRoutes.concat(accessedRoutes)
    state.addedRoutes.value = accessedRoutes
    state.menuRoutes.value = deriveMenuRoutes(accessedRoutes)
    state.isRoutesGenerated.value = true
    persistRoutesGeneratedState()

    logger.log('路由已生成:', {
      total: state.routes.value.length,
      added: state.addedRoutes.value.length,
      menu: state.menuRoutes.value.length,
      accessedRoutes: accessedRoutes.map((route) => route.path)
    })

    return accessedRoutes
  }
}

/**
 * 创建清空路由 action
 *
 * @param state store 状态
 * @returns action
 */
function createClearRoutesAction(state: PermissionStateRefs) {
  return () => {
    state.routes.value = []
    state.addedRoutes.value = []
    state.menuRoutes.value = []
    state.isRoutesGenerated.value = false
    state.isRoutesAdded.value = false
    clearRoutesGeneratedState()
    logger.log('路由已清除')
  }
}

/**
 * 创建标记路由已添加 action
 *
 * @param isRoutesAdded 路由添加标记
 * @returns action
 */
function createMarkRoutesAddedAction(isRoutesAdded: Ref<boolean>) {
  return () => {
    isRoutesAdded.value = true
    logger.log('路由已标记为已添加到路由器')
  }
}

/**
 * 根据路径查找路由
 *
 * @param flatRoutes 扁平路由
 * @param path 路径
 * @returns 路由
 */
function findRouteByPath(flatRoutes: ComputedRef<RouteRecordRaw[]>, path: string): RouteRecordRaw | undefined {
  return flatRoutes.value.find((route) => route.path === path)
}

/**
 * 根据名称查找路由
 *
 * @param flatRoutes 扁平路由
 * @param name 名称
 * @returns 路由
 */
function findRouteByName(flatRoutes: ComputedRef<RouteRecordRaw[]>, name: string): RouteRecordRaw | undefined {
  return flatRoutes.value.find((route) => route.name === name)
}

/**
 * 权限路由状态管理 Store
 */
export const usePermissionStore = defineStore('permission', () => {
  const routes = shallowRef<RouteRecordRaw[]>([])
  const addedRoutes = shallowRef<RouteRecordRaw[]>([])
  const isRoutesGenerated = ref(false)
  const isRoutesAdded = ref(false)
  const menuRoutes = shallowRef<RouteRecordRaw[]>([])
  const flatRoutes = computed(() => flattenRoutes(routes.value))

  const state: PermissionStateRefs = {
    routes,
    addedRoutes,
    menuRoutes,
    isRoutesGenerated,
    isRoutesAdded
  }

  const resolveFallbackRoutes = createFallbackResolver()
  const generateRoutes = createGenerateRoutesAction(state, resolveFallbackRoutes)
  const clearRoutes = createClearRoutesAction(state)
  const markRoutesAdded = createMarkRoutesAddedAction(isRoutesAdded)

  const restoreRoutesState = () => restoreRoutesGeneratedState(isRoutesGenerated)
  const findRouteByPathAction = (path: string) => findRouteByPath(flatRoutes, path)
  const findRouteByNameAction = (name: string) => findRouteByName(flatRoutes, name)

  nextTick(() => {
    restoreRoutesState()
  })

  return {
    routes,
    addedRoutes,
    isRoutesGenerated,
    isRoutesAdded,
    menuRoutes,
    flatRoutes,
    generateRoutes,
    clearRoutes,
    restoreRoutesState,
    markRoutesAdded,
    filterAsyncRoutes,
    hasRoutePermission,
    findRouteByPath: findRouteByPathAction,
    findRouteByName: findRouteByNameAction
  }
})
