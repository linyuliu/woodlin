/**
 * 节流工具函数 (Throttle Utility)
 * 
 * @author mumu
 * @description 实现函数节流，在指定时间内只执行一次，无论触发多少次
 *              适用场景：滚动事件、按钮点击、鼠标移动等高频触发的事件
 *              Throttle function execution - executes once per time interval, regardless of trigger count.
 *              Use cases: scroll events, button clicks, mouse movement, and other high-frequency events.
 * @since 2025-01-04
 */

/**
 * 创建一个节流函数（时间戳版本）
 * 
 * @param func 需要节流的函数
 * @param wait 时间间隔（毫秒）
 * @returns 节流后的函数
 * 
 * @example
 * ```typescript
 * // 基础用法
 * const throttledScroll = throttle(() => {
 *   console.log('滚动事件')
 * }, 200)
 * 
 * window.addEventListener('scroll', throttledScroll)
 * ```
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300
): (...args: Parameters<T>) => void {
  let lastTime = 0

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()

    if (now - lastTime >= wait) {
      lastTime = now
      func.apply(this, args)
    }
  }
}

/**
 * 创建一个节流函数（定时器版本，支持尾部调用）
 * 
 * @param func 需要节流的函数
 * @param wait 时间间隔（毫秒）
 * @param options 配置选项
 * @returns 节流后的函数
 * 
 * @example
 * ```typescript
 * // Disable first execution / 禁用首次立即执行
 * const throttledFunc = throttleAdvanced(handler, 1000, { leading: false })
 * 
 * // Disable trailing execution / 禁用尾部执行
 * const throttledFunc = throttleAdvanced(handler, 1000, { trailing: false })
 * ```
 */
export function throttleAdvanced<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  options: {
    leading?: boolean
    trailing?: boolean
  } = {}
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0

  const { leading = true, trailing = true } = options

  return function (this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()

    // 如果不需要首次立即执行，则重置previous
    if (!previous && !leading) {
      previous = now
    }

    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }

      previous = now
      func.apply(context, args)
    } else if (!timeout && trailing) {
      timeout = setTimeout(() => {
        previous = leading ? Date.now() : 0
        timeout = null
        func.apply(context, args)
      }, remaining)
    }
  }
}

/**
 * 创建一个可取消的节流函数
 * 
 * @param func 需要节流的函数
 * @param wait 时间间隔（毫秒）
 * @returns 包含节流函数和取消方法的对象
 * 
 * @example
 * ```typescript
 * const { run, cancel } = throttleCancelable(handleResize, 200)
 * 
 * // 使用
 * window.addEventListener('resize', run)
 * 
 * // 取消
 * cancel()
 * ```
 */
export function throttleCancelable<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300
): {
  run: (...args: Parameters<T>) => void
  cancel: () => void
} {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0

  const run = function (this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()
    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }

      previous = now
      func.apply(context, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(context, args)
      }, remaining)
    }
  }

  const cancel = () => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    previous = 0
  }

  return { run, cancel }
}

/**
 * Vue 3 Composition API 中使用的节流 Hook
 * 
 * @param fn 需要节流的函数
 * @param delay 延迟时间（毫秒）
 * @returns 节流后的函数
 * 
 * @example
 * ```typescript
 * import { useThrottleFn } from '@/utils/throttle'
 * 
 * const handleScroll = useThrottleFn(() => {
 *   console.log('处理滚动')
 * }, 200)
 * 
 * // 在模板中使用
 * // <div @scroll="handleScroll">...</div>
 * ```
 */
export function useThrottleFn<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  return throttle(fn, delay)
}
