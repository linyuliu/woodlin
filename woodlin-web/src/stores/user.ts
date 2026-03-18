/**
 * 用户状态管理 Store
 *
 * @author mumu
 * @description 管理用户信息、权限、角色等状态
 * @since 2025-01-01
 */

import {ref, computed, nextTick, type ComputedRef, type Ref} from 'vue'
import {defineStore} from 'pinia'
import {getUserInfo, type UserInfoResponse} from '@/api/auth'
import {logger} from '@/utils/logger'

/**
 * 用户信息接口
 */
export interface UserInfo {
  /** 用户ID */
  id: string | number
  /** 用户名 */
  username: string
  /** 昵称 */
  nickname?: string
  /** 真实姓名 */
  realName?: string
  /** 邮箱 */
  email?: string
  /** 手机号 */
  mobile?: string
  /** 头像 */
  avatar?: string
  /** 性别 */
  gender?: string
  /** 部门ID */
  deptId?: string | number
  /** 部门名称 */
  deptName?: string
  /** 租户ID */
  tenantId?: string | number
  /** 租户名称 */
  tenantName?: string
  /** 角色列表 */
  roles?: string[]
  /** 权限列表 */
  permissions?: string[]
  /** 是否超级管理员（后端返回） */
  superAdmin?: boolean
  /** 用户状态 */
  status?: string
  /** 创建时间 */
  createTime?: string
  /** 最后登录时间 */
  lastLoginTime?: string
}

type UserStateRefs = {
  userInfo: Ref<UserInfo | null>
  permissions: Ref<string[]>
  roles: Ref<string[]>
  superAdmin: Ref<boolean>
  isUserInfoLoaded: Ref<boolean>
}

type UserComputed = {
  isLoggedIn: ComputedRef<boolean>
  displayName: ComputedRef<string>
  isAdmin: ComputedRef<boolean>
  isSuperAdmin: ComputedRef<boolean>
}

const USER_INFO_KEY = 'userInfo'
const USER_PERMISSION_KEY = 'userPermissions'
const USER_ROLE_KEY = 'userRoles'
const USER_SUPER_ADMIN_KEY = 'userIsSuperAdmin'

/**
 * 创建用户状态
 *
 * @returns 状态对象
 */
function createUserState(): UserStateRefs {
  return {
    userInfo: ref<UserInfo | null>(null),
    permissions: ref<string[]>([]),
    roles: ref<string[]>([]),
    superAdmin: ref(false),
    isUserInfoLoaded: ref(false)
  }
}

/**
 * 创建计算属性
 *
 * @param state 用户状态
 * @returns 计算属性
 */
function createUserComputed(state: UserStateRefs): UserComputed {
  const isLoggedIn = computed(() => state.userInfo.value !== null)
  const displayName = computed(() => {
    const info = state.userInfo.value
    if (!info) {
      return ''
    }
    return info.nickname || info.realName || info.username
  })
  const isAdmin = computed(() => {
    return state.roles.value.includes('admin') || state.roles.value.includes('ROLE_ADMIN')
  })
  const isSuperAdmin = computed(() => {
    return state.superAdmin.value || state.permissions.value.includes('*')
  })

  return {isLoggedIn, displayName, isAdmin, isSuperAdmin}
}

/**
 * 持久化用户状态到 localStorage
 *
 * @param info 用户信息
 */
function persistUserState(info: UserInfo) {
  try {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
    localStorage.setItem(USER_PERMISSION_KEY, JSON.stringify(info.permissions || []))
    localStorage.setItem(USER_ROLE_KEY, JSON.stringify(info.roles || []))
    localStorage.setItem(USER_SUPER_ADMIN_KEY, String(Boolean(info.superAdmin)))
  } catch (error) {
    logger.error('保存用户信息到localStorage失败', error)
  }
}

/**
 * 从 localStorage 恢复用户状态
 *
 * @param state 用户状态
 * @returns 是否恢复成功
 */
function restorePersistedUserState(state: UserStateRefs): boolean {
  try {
    const savedUserInfo = localStorage.getItem(USER_INFO_KEY)
    const savedPermissions = localStorage.getItem(USER_PERMISSION_KEY)
    const savedRoles = localStorage.getItem(USER_ROLE_KEY)
    const savedIsSuperAdmin = localStorage.getItem(USER_SUPER_ADMIN_KEY)

    if (!savedUserInfo) {
      return false
    }

    state.userInfo.value = JSON.parse(savedUserInfo)
    state.permissions.value = savedPermissions ? JSON.parse(savedPermissions) : []
    state.roles.value = savedRoles ? JSON.parse(savedRoles) : []
    state.superAdmin.value = savedIsSuperAdmin === 'true'
    state.isUserInfoLoaded.value = true
    return true
  } catch (error) {
    logger.error('从localStorage恢复用户信息失败', error)
  }
  return false
}

/**
 * 清理 localStorage 用户状态
 */
function clearPersistedUserState() {
  try {
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(USER_PERMISSION_KEY)
    localStorage.removeItem(USER_ROLE_KEY)
    localStorage.removeItem(USER_SUPER_ADMIN_KEY)
  } catch (error) {
    logger.error('清除localStorage用户信息失败', error)
  }
}

/**
 * 将后端用户信息转换为前端模型
 *
 * @param data 后端响应
 * @returns 前端用户信息
 */
function mapToUserInfo(data: UserInfoResponse): UserInfo {
  return {
    id: data.userId || data.id || '',
    username: data.username || '',
    nickname: data.nickname,
    realName: data.realName,
    email: data.email,
    mobile: data.mobile,
    avatar: data.avatar,
    gender: data.gender,
    deptId: data.deptId,
    deptName: data.deptName,
    tenantId: data.tenantId,
    tenantName: data.tenantName,
    roles: data.roleCodes || data.roles || [],
    permissions: data.permissions || [],
    superAdmin: Boolean(data.superAdmin ?? data.isSuperAdmin),
    status: data.status,
    createTime: data.createTime,
    lastLoginTime: data.lastLoginTime || data.loginTime
  }
}

/**
 * 创建 setUserInfo action
 *
 * @param state 用户状态
 * @returns action
 */
function createSetUserInfoAction(state: UserStateRefs) {
  return (info: UserInfo) => {
    state.userInfo.value = info
    state.permissions.value = info.permissions || []
    state.roles.value = info.roles || []
    state.superAdmin.value = Boolean(info.superAdmin)
    state.isUserInfoLoaded.value = true
    persistUserState(info)
  }
}

/**
 * 创建 fetchUserInfo action
 *
 * @param setUserInfo 设置用户信息 action
 * @returns action
 */
function createFetchUserInfoAction(setUserInfo: (info: UserInfo) => void) {
  return async (): Promise<UserInfo> => {
    try {
      const data = await getUserInfo()
      const info = mapToUserInfo(data)
      setUserInfo(info)
      return info
    } catch (error) {
      logger.error('获取用户信息失败', error)
      throw error
    }
  }
}

/**
 * 创建 hasPermission action
 *
 * @param state 用户状态
 * @param isSuperAdmin 超管计算属性
 * @returns action
 */
function createHasPermissionAction(state: UserStateRefs, isSuperAdmin: ComputedRef<boolean>) {
  return (permission: string | string[]): boolean => {
    if (isSuperAdmin.value || state.permissions.value.includes('*')) {
      return true
    }
    if (Array.isArray(permission)) {
      if (permission.length === 0) {
        return true
      }
      return permission.some((item) => state.permissions.value.includes(item))
    }
    return state.permissions.value.includes(permission)
  }
}

/**
 * 创建 hasAllPermissions action
 *
 * @param state 用户状态
 * @param isSuperAdmin 超管计算属性
 * @returns action
 */
function createHasAllPermissionsAction(state: UserStateRefs, isSuperAdmin: ComputedRef<boolean>) {
  return (permissionList: string[]): boolean => {
    if (isSuperAdmin.value) {
      return true
    }
    return permissionList.every((permission) => state.permissions.value.includes(permission))
  }
}

/**
 * 创建 hasRole action
 *
 * @param state 用户状态
 * @param isSuperAdmin 超管计算属性
 * @returns action
 */
function createHasRoleAction(state: UserStateRefs, isSuperAdmin: ComputedRef<boolean>) {
  return (role: string | string[]): boolean => {
    if (isSuperAdmin.value) {
      return true
    }
    if (Array.isArray(role)) {
      return role.some((item) => state.roles.value.includes(item))
    }
    return state.roles.value.includes(role)
  }
}

/**
 * 创建 clearUserInfo action
 *
 * @param state 用户状态
 * @returns action
 */
function createClearUserInfoAction(state: UserStateRefs) {
  return () => {
    state.userInfo.value = null
    state.permissions.value = []
    state.roles.value = []
    state.superAdmin.value = false
    state.isUserInfoLoaded.value = false
    clearPersistedUserState()
  }
}

/**
 * 创建 updateUserInfo action
 *
 * @param state 用户状态
 * @returns action
 */
function createUpdateUserInfoAction(state: UserStateRefs) {
  return (info: Partial<UserInfo>) => {
    if (state.userInfo.value) {
      state.userInfo.value = {...state.userInfo.value, ...info}
    }
  }
}

/**
 * 创建用户动作集合
 *
 * @param state 用户状态
 * @param computedState 用户计算属性
 * @returns 动作集合
 */
function createUserActions(state: UserStateRefs, computedState: UserComputed) {
  const setUserInfo = createSetUserInfoAction(state)
  const fetchUserInfo = createFetchUserInfoAction(setUserInfo)
  const hasPermission = createHasPermissionAction(state, computedState.isSuperAdmin)
  const hasAllPermissions = createHasAllPermissionsAction(state, computedState.isSuperAdmin)
  const hasRole = createHasRoleAction(state, computedState.isSuperAdmin)
  const restoreUserInfo = () => restorePersistedUserState(state)
  const clearUserInfo = createClearUserInfoAction(state)
  const updateUserInfo = createUpdateUserInfoAction(state)

  return {
    setUserInfo,
    fetchUserInfo,
    hasPermission,
    hasAllPermissions,
    hasRole,
    restoreUserInfo,
    clearUserInfo,
    updateUserInfo
  }
}

/**
 * 组装用户 store
 *
 * @returns store 内容
 */
function createUserStore() {
  const state = createUserState()
  const computedState = createUserComputed(state)
  const actions = createUserActions(state, computedState)

  nextTick(() => {
    actions.restoreUserInfo()
  })

  return {
    ...state,
    ...computedState,
    ...actions
  }
}

/**
 * 用户状态管理 Store
 */
export const useUserStore = defineStore('user', createUserStore)
