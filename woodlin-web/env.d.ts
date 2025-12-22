/// <reference types="vite/client" />

/**
 * 环境变量类型定义
 */
interface ImportMetaEnv {
  /** API基础URL */
  readonly VITE_API_BASE_URL: string
  /** 后端服务地址（仅开发环境使用） */
  readonly VITE_BACKEND_URL?: string
  /** 应用标题 */
  readonly VITE_APP_TITLE: string
  /** 应用环境 */
  readonly VITE_APP_ENV: 'development' | 'production'
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
