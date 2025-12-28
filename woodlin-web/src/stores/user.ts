/**
 * 用户状态管理 Store
 * 
 * @author mumu
 * @description 管理用户信息、权限、角色等状态
 * @since 2025-01-01
 */

import { ref, computed, nextTick } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/auth'

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
  /** 用户状态 */
  status?: string
  /** 创建时间 */
  createTime?: string
  /** 最后登录时间 */
  lastLoginTime?: string
}

/**
 * 用户状态管理 Store
 */
export const useUserStore = defineStore('user', () => {
  // ===== 状态 =====
  
  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)
  
  /** 用户权限列表 */
  const permissions = ref<string[]>([])
  
  /** 用户角色列表 */
  const roles = ref<string[]>([])
  
  /** 是否已加载用户信息 */
  const isUserInfoLoaded = ref(false)

  // ===== 计算属性 =====
  
  /** 是否已登录（判断是否有用户信息） */
  const isLoggedIn = computed(() => userInfo.value !== null)
  
  /** 用户名称（优先显示昵称，其次真实姓名，最后用户名） */
  const displayName = computed(() => {
    if (!userInfo.value) {return ''}
    return userInfo.value.nickname || userInfo.value.realName || userInfo.value.username
  })
  
  /** 是否是管理员 */
  const isAdmin = computed(() => {
    return roles.value.includes('admin') || roles.value.includes('ROLE_ADMIN')
  })
  
  /** 是否是超级管理员 */
  const isSuperAdmin = computed(() => {
    return roles.value.includes('super_admin') || roles.value.includes('ROLE_SUPER_ADMIN')
  })

  // ===== 方法 =====
  
  /**
   * 设置用户信息
   * @param info 用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    permissions.value = info.permissions || []
    roles.value = info.roles || []
    isUserInfoLoaded.value = true
    
    // 持久化用户信息到localStorage
    try {
      localStorage.setItem('userInfo', JSON.stringify(info))
      localStorage.setItem('userPermissions', JSON.stringify(info.permissions || []))
      localStorage.setItem('userRoles', JSON.stringify(info.roles || []))
    } catch (error) {
      console.error('保存用户信息到localStorage失败:', error)
    }
  }
  
  /**
   * 获取用户信息（从服务器获取）
   */
  async function fetchUserInfo(): Promise<UserInfo> {
    try {
      const data = await getUserInfo()
      // 将返回的数据转换为UserInfo类型，添加必要的字段处理
      const info: UserInfo = {
        id: (data as any).userId || (data as any).id,
        username: (data as any).username || '',
        nickname: (data as any).nickname,
        realName: (data as any).realName,
        email: (data as any).email,
        mobile: (data as any).mobile,
        avatar: (data as any).avatar,
        gender: (data as any).gender,
        deptId: (data as any).deptId,
        deptName: (data as any).deptName,
        tenantId: (data as any).tenantId,
        tenantName: (data as any).tenantName,
        roles: (data as any).roleCodes || (data as any).roles || [],
        permissions: (data as any).permissions || [],
        status: (data as any).status,
        createTime: (data as any).createTime,
        lastLoginTime: (data as any).lastLoginTime || (data as any).loginTime
      }
      setUserInfo(info)
      return info
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }
  
  /**
   * 检查用户是否拥有指定权限
   * @param permission 权限标识（支持数组）
   * @returns 是否拥有权限
   */
  function hasPermission(permission: string | string[]): boolean {
    // 超级管理员拥有所有权限
    if (isSuperAdmin.value) {
      return true
    }
    
    if (Array.isArray(permission)) {
      // 数组：检查是否拥有其中任意一个权限
      return permission.some(p => permissions.value.includes(p))
    } else {
      // 字符串：检查是否拥有该权限
      return permissions.value.includes(permission)
    }
  }
  
  /**
   * 检查用户是否拥有所有指定权限
   * @param permissionList 权限列表
   * @returns 是否拥有所有权限
   */
  function hasAllPermissions(permissionList: string[]): boolean {
    // 超级管理员拥有所有权限
    if (isSuperAdmin.value) {
      return true
    }
    
    return permissionList.every(p => permissions.value.includes(p))
  }
  
  /**
   * 检查用户是否拥有指定角色
   * @param role 角色标识（支持数组）
   * @returns 是否拥有角色
   */
  function hasRole(role: string | string[]): boolean {
    // 超级管理员拥有所有角色
    if (isSuperAdmin.value) {
      return true
    }
    
    if (Array.isArray(role)) {
      // 数组：检查是否拥有其中任意一个角色
      return role.some(r => roles.value.includes(r))
    } else {
      // 字符串：检查是否拥有该角色
      return roles.value.includes(role)
    }
  }
  
  /**
   * 从localStorage恢复用户信息
   */
  function restoreUserInfo(): boolean {
    try {
      const savedUserInfo = localStorage.getItem('userInfo')
      const savedPermissions = localStorage.getItem('userPermissions')
      const savedRoles = localStorage.getItem('userRoles')
      
      if (savedUserInfo) {
        userInfo.value = JSON.parse(savedUserInfo)
        permissions.value = savedPermissions ? JSON.parse(savedPermissions) : []
        roles.value = savedRoles ? JSON.parse(savedRoles) : []
        isUserInfoLoaded.value = true
        return true
      }
    } catch (error) {
      console.error('从localStorage恢复用户信息失败:', error)
    }
    return false
  }
  
  /**
   * 清除用户信息（登出时调用）
   */
  function clearUserInfo() {
    userInfo.value = null
    permissions.value = []
    roles.value = []
    isUserInfoLoaded.value = false
    
    // 从localStorage清除用户信息
    try {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userPermissions')
      localStorage.removeItem('userRoles')
    } catch (error) {
      console.error('清除localStorage用户信息失败:', error)
    }
  }
  
  /**
   * 更新用户信息（部分更新）
   * @param info 要更新的用户信息
   */
  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }

  // 初始化：从localStorage恢复用户信息（延迟执行，避免阻塞主线程）
  nextTick(() => {
    restoreUserInfo()
  })

  return {
    // 状态
    userInfo,
    permissions,
    roles,
    isUserInfoLoaded,
    
    // 计算属性
    isLoggedIn,
    displayName,
    isAdmin,
    isSuperAdmin,
    
    // 方法
    setUserInfo,
    fetchUserInfo,
    restoreUserInfo,
    hasPermission,
    hasAllPermissions,
    hasRole,
    clearUserInfo,
    updateUserInfo
  }
})
