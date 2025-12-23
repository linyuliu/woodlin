/**
 * 简单的日志工具类
 * 
 * @author mumu
 * @description 提供条件日志输出，仅在开发环境输出
 * @since 2025-12-23
 */

const isDev = import.meta.env.DEV

export const logger = {
  /**
   * 输出普通日志
   */
  log(...args: any[]) {
    if (isDev) {
      console.log(...args)
    }
  },

  /**
   * 输出信息日志
   */
  info(...args: any[]) {
    if (isDev) {
      console.info(...args)
    }
  },

  /**
   * 输出警告日志
   */
  warn(...args: any[]) {
    if (isDev) {
      console.warn(...args)
    }
  },

  /**
   * 输出错误日志（始终输出）
   */
  error(...args: any[]) {
    console.error(...args)
  },

  /**
   * 输出调试日志
   */
  debug(...args: any[]) {
    if (isDev) {
      console.debug(...args)
    }
  }
}

export default logger
