/**
 * @file global.d.ts
 * @description 全局共享类型定义：路由项、用户信息、字典、分页与统一响应结构
 * @author yulin
 * @since 2026-05-04
 */

/** 后端返回的菜单/路由节点 */
export interface RouteItem {
  id: number
  parentId: number
  /** 节点类型：1=目录 2=菜单 3=按钮 */
  type: 1 | 2 | 3
  name: string
  title: string
  path: string
  /** 组件标识：'Layout' | 'ParentView' | 'system/user/index' */
  component: string
  redirect?: string
  /** 图标，格式 'vicons:antd:UserOutlined' */
  icon?: string
  permission?: string
  isHidden: boolean
  isCache: boolean
  isFrame: boolean
  showInTabs?: boolean
  activeMenu?: string
  sort: number
  children?: RouteItem[]
}

/** 当前登录用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  deptId?: number
  deptName?: string
  tenantId?: string
}

/** 登录接口返回 */
export interface LoginResponse {
  token: string
  expire: number
  user: UserInfo
  roles: string[]
  permissions: string[]
  requirePasswordChange?: boolean
  passwordExpiringSoon?: boolean
  message?: string
}

/** 字典项 */
export interface DictItem {
  label: string
  value: string | number
  color?: string
  remark?: string
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 通用响应包装 */
export interface R<T = unknown> {
  code: number
  message: string
  data: T
}

declare global {
  interface Window {
    __APP_VERSION__?: string
  }
}

export {}
