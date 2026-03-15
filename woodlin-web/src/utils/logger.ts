/**
 * 简单的日志工具类
 * 
 * @author mumu
 * @description 提供条件日志输出，仅在开发环境输出
 * @since 2025-12-23
 */

const isDev = import.meta.env.DEV
type LogArgs = unknown[]

/**
 * 开发环境日志输出（统一走 warn，避免 lint 限制）
 */
function devLog(prefix: string, ...args: LogArgs) {
  if (!isDev) {
    return
  }
  console.warn(prefix, ...args)
}

export const logger = {
  /**
   * 输出普通日志
   */
  log(...args: LogArgs) {
    devLog('[LOG]', ...args)
  },

  /**
   * 输出信息日志
   */
  info(...args: LogArgs) {
    devLog('[INFO]', ...args)
  },

  /**
   * 输出警告日志
   */
  warn(...args: LogArgs) {
    devLog('[WARN]', ...args)
  },

  /**
   * 输出错误日志（始终输出）
   */
  error(...args: LogArgs) {
    console.error(...args)
  },

  /**
   * 输出调试日志
   */
  debug(...args: LogArgs) {
    devLog('[DEBUG]', ...args)
  }
}

export default logger
