/**
 * 防抖工具函数
 * 
 * @author mumu
 * @description 实现函数防抖，在事件触发后等待一定时间才执行，如果在等待期间再次触发则重新计时
 *              适用场景：搜索输入框、窗口resize等需要等待用户停止操作后再执行的场景
 * @since 2025-01-04
 */

/**
 * 创建一个防抖函数
 * 
 * @param func 需要防抖的函数
 * @param wait 等待时间（毫秒）
 * @param immediate 是否立即执行（第一次触发时）
 * @returns 防抖后的函数
 * 
 * @example
 * ```typescript
 * // 基础用法
 * const debouncedSearch = debounce((keyword: string) => {
 *   console.log('搜索:', keyword)
 * }, 500)
 * 
 * // 立即执行模式
 * const debouncedSave = debounce(saveData, 1000, true)
 * ```
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    const context = this

    const later = () => {
      timeout = null
      if (!immediate) {
        func.apply(context, args)
      }
    }

    const callNow = immediate && !timeout

    if (timeout) {
      clearTimeout(timeout)
    }

    timeout = setTimeout(later, wait)

    if (callNow) {
      func.apply(context, args)
    }
  }
}

/**
 * 创建一个可取消的防抖函数
 * 
 * @param func 需要防抖的函数
 * @param wait 等待时间（毫秒）
 * @param immediate 是否立即执行（第一次触发时）
 * @returns 包含防抖函数和取消方法的对象
 * 
 * @example
 * ```typescript
 * const { run, cancel } = debounceCancelable(handleInput, 500)
 * 
 * // 使用
 * input.addEventListener('input', run)
 * 
 * // 取消
 * cancel()
 * ```
 */
export function debounceCancelable<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  immediate: boolean = false
): {
  run: (...args: Parameters<T>) => void
  cancel: () => void
} {
  let timeout: ReturnType<typeof setTimeout> | null = null

  const run = function (this: any, ...args: Parameters<T>) {
    const context = this

    const later = () => {
      timeout = null
      if (!immediate) {
        func.apply(context, args)
      }
    }

    const callNow = immediate && !timeout

    if (timeout) {
      clearTimeout(timeout)
    }

    timeout = setTimeout(later, wait)

    if (callNow) {
      func.apply(context, args)
    }
  }

  const cancel = () => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
  }

  return { run, cancel }
}

/**
 * Vue 3 Composition API 中使用的防抖 Hook
 * 
 * @param fn 需要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的函数
 * 
 * @example
 * ```typescript
 * import { ref } from 'vue'
 * import { useDebounceFn } from '@/utils/debounce'
 * 
 * const keyword = ref('')
 * const search = useDebounceFn((value: string) => {
 *   console.log('搜索:', value)
 * }, 500)
 * 
 * // 在模板中使用
 * // <input v-model="keyword" @input="search(keyword)" />
 * ```
 */
export function useDebounceFn<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  return debounce(fn, delay)
}
