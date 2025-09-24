/**
 * Pinia状态管理 - 计数器示例Store
 * 
 * @author mumu
 * @description 演示Pinia状态管理的基本用法，可以作为其他Store的模板
 * @since 2025-01-01
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 计数器状态管理Store
 * 
 * 使用Composition API风格定义Store
 * 提供计数器的状态、计算属性和操作方法
 */
export const useCounterStore = defineStore('counter', () => {
  // 响应式状态
  const count = ref(0)
  
  // 计算属性 - 双倍计数值
  const doubleCount = computed(() => count.value * 2)
  
  /**
   * 增加计数器的值
   * 每次调用都会将count加1
   */
  function increment() {
    count.value++
  }
  
  /**
   * 减少计数器的值
   * 每次调用都会将count减1
   */
  function decrement() {
    count.value--
  }
  
  /**
   * 重置计数器
   * 将count重置为0
   */
  function reset() {
    count.value = 0
  }
  
  /**
   * 设置特定值
   * @param value 要设置的数值
   */
  function setCount(value: number) {
    count.value = value
  }

  // 返回状态和方法
  return { 
    count, 
    doubleCount, 
    increment, 
    decrement, 
    reset, 
    setCount 
  }
})
