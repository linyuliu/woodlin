/**
 * @file constants/index.ts
 * @description 常用枚举与常量
 * @author yulin
 * @since 2026-05-04
 */

/** HTTP 状态码 */
export const enum HttpStatus {
  OK = 200,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  SERVER_ERROR = 500,
}

/** 业务响应码 */
export const enum BizCode {
  SUCCESS = 200,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
}

/** 菜单类型 */
export const enum MenuType {
  DIR = 1,
  MENU = 2,
  BUTTON = 3,
}

/** 通用启停用 */
export const enum CommonStatus {
  ENABLED = '1',
  DISABLED = '0',
}

/** 缓存键 */
export const STORAGE_KEYS = {
  TOKEN: 'woodlin_token',
  TENANT: 'woodlin_tenant',
  LOCALE: 'woodlin_locale',
} as const
