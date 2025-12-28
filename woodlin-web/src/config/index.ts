/**
 * 全局配置文件
 * 
 * @author mumu
 * @description 应用程序的全局配置项，包括系统设置、布局配置、主题配置等
 *              参考 vue-vben-admin 的配置系统设计，提供灵活的配置选项
 * @since 2025-01-01
 */

/**
 * 项目配置接口定义
 */
export interface ProjectConfig {
  /** 系统配置 */
  system: SystemConfig
  /** 布局配置 */
  layout: LayoutConfig
  /** 主题配置 */
  theme: ThemeConfig
  /** 请求配置 */
  http: HttpConfig
  /** 路由配置 */
  router: RouterConfig
}

/**
 * 系统配置
 */
export interface SystemConfig {
  /** 系统标题 */
  title: string
  /** 系统副标题 */
  subtitle: string
  /** 系统Logo */
  logo: string
  /** 系统版本 */
  version: string
  /** 是否显示版本信息 */
  showVersion: boolean
  /** 默认语言 */
  locale: 'zh-CN' | 'en-US'
}

/**
 * 布局配置
 */
export interface LayoutConfig {
  /** 布局模式: 左侧菜单 | 顶部菜单 | 混合菜单 */
  mode: 'sidebar' | 'top' | 'mix'
  /** 是否固定Header */
  fixedHeader: boolean
  /** 是否固定Sider */
  fixedSider: boolean
  /** 是否显示面包屑 */
  showBreadcrumb: boolean
  /** 是否显示标签页 */
  showTabs: boolean
  /** 是否显示页脚 */
  showFooter: boolean
  /** 内容区域宽度模式 */
  contentMode: 'full' | 'fixed'
  /** 侧边栏宽度（像素） */
  siderWidth: number
  /** 侧边栏折叠宽度（像素） */
  siderCollapsedWidth: number
}

/**
 * 主题配置
 */
export interface ThemeConfig {
  /** 主题模式 */
  mode: 'light' | 'dark' | 'auto'
  /** 主题色 */
  primaryColor: string
  /** 成功色 */
  successColor: string
  /** 警告色 */
  warningColor: string
  /** 错误色 */
  errorColor: string
  /** 信息色 */
  infoColor: string
}

/**
 * HTTP请求配置
 */
export interface HttpConfig {
  /** API基础URL */
  baseURL: string
  /** 请求超时时间（毫秒） */
  timeout: number
  /** 是否携带Cookie */
  withCredentials: boolean
  /** 请求重试次数 */
  retryCount: number
  /** 请求重试延迟（毫秒） */
  retryDelay: number
  /** Token存储键名 */
  tokenKey: string
  /** Token请求头名称 */
  tokenHeaderName: string
}

/**
 * 路由配置
 */
export interface RouterConfig {
  /** 路由模式 */
  mode: 'history' | 'hash'
  /** 基础路径 */
  base: string
  /** 是否开启路由权限 */
  enablePermission: boolean
  /** 登录页路径 */
  loginPath: string
  /** 默认首页路径 */
  homePath: string
  /** 404页面路径 */
  notFoundPath: string
  /** 是否开启路由缓存 */
  enableCache: boolean
  /** 路由切换动画 */
  transitionName: string
  /** 路由缓存过期时间（毫秒），默认1小时 */
  routeCacheExpiration: number
}

/**
 * 默认项目配置
 * 
 * 该配置提供系统运行的默认值，可通过环境变量覆盖部分配置
 */
const defaultConfig: ProjectConfig = {
  // ===== 系统配置 =====
  system: {
    title: 'Woodlin',
    subtitle: '多租户管理系统',
    logo: '🌲',
    version: '1.0.0',
    showVersion: true,
    locale: 'zh-CN'
  },

  // ===== 布局配置 =====
  layout: {
    mode: 'sidebar',
    fixedHeader: true,
    fixedSider: true,
    showBreadcrumb: true,
    showTabs: false,
    showFooter: false,
    contentMode: 'full',
    siderWidth: 210,
    siderCollapsedWidth: 64
  },

  // ===== 主题配置 =====
  theme: {
    mode: 'light',
    primaryColor: '#18a058',
    successColor: '#52c41a',
    warningColor: '#faad14',
    errorColor: '#f5222d',
    infoColor: '#1890ff'
  },

  // ===== HTTP请求配置 =====
  http: {
    // 从环境变量读取API基础URL，默认为本地开发地址
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 60000, // 60秒超时
    withCredentials: false,
    retryCount: 3,
    retryDelay: 1000,
    tokenKey: 'token',
    tokenHeaderName: 'Authorization'
  },

  // ===== 路由配置 =====
  router: {
    mode: 'history',
    base: import.meta.env.BASE_URL || '/',
    enablePermission: true,
    loginPath: '/login',
    homePath: '/dashboard',
    notFoundPath: '/404',
    enableCache: true,
    transitionName: 'fade-slide',
    routeCacheExpiration: 60 * 60 * 1000 // 1小时（毫秒）
  }
}

/**
 * 深度合并配置对象
 * 
 * @param target 目标对象
 * @param source 源对象
 * @returns 合并后的对象
 */
function deepMerge<T extends Record<string, any>>(target: T, source: Partial<T>): T {
  const result = { ...target }
  
  for (const key in source) {
    const sourceValue = source[key]
    const targetValue = result[key]
    
    if (
      sourceValue &&
      typeof sourceValue === 'object' &&
      !Array.isArray(sourceValue) &&
      targetValue &&
      typeof targetValue === 'object' &&
      !Array.isArray(targetValue)
    ) {
      result[key] = deepMerge(targetValue, sourceValue) as any
    } else if (sourceValue !== undefined) {
      result[key] = sourceValue as any
    }
  }
  
  return result
}

/**
 * 当前项目配置
 * 
 * 可以通过 updateConfig 方法动态更新配置
 */
let projectConfig: ProjectConfig = defaultConfig

/**
 * 获取项目配置
 * 
 * @returns 当前项目配置对象
 * 
 * @example
 * ```typescript
 * import { getConfig } from '@/config'
 * 
 * const config = getConfig()
 * console.log(config.system.title) // 'Woodlin'
 * ```
 */
export function getConfig(): Readonly<ProjectConfig> {
  return projectConfig
}

/**
 * 更新项目配置
 * 
 * @param config 要更新的配置对象（支持部分更新）
 * 
 * @example
 * ```typescript
 * import { updateConfig } from '@/config'
 * 
 * // 更新系统标题
 * updateConfig({
 *   system: {
 *     title: '新系统名称'
 *   }
 * })
 * ```
 */
export function updateConfig(config: Partial<ProjectConfig>): void {
  projectConfig = deepMerge(projectConfig, config)
}

/**
 * 重置配置为默认值
 * 
 * @example
 * ```typescript
 * import { resetConfig } from '@/config'
 * 
 * resetConfig()
 * ```
 */
export function resetConfig(): void {
  projectConfig = defaultConfig
}

// 默认导出配置
export default projectConfig
