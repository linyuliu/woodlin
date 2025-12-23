/**
 * 应用全局状态管理 Store
 * 
 * @author mumu
 * @description 管理应用级别的全局状态，如加载状态、侧边栏折叠等
 * @since 2025-01-01
 */

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 应用全局状态管理 Store
 */
export const useAppStore = defineStore('app', () => {
  // ===== 状态 =====
  
  /** 侧边栏是否折叠 */
  const sidebarCollapsed = ref(false)
  
  /** 全局加载状态 */
  const loading = ref(false)
  
  /** 加载文本 */
  const loadingText = ref('加载中...')
  
  /** 是否显示设置抽屉 */
  const showSettings = ref(false)
  
  /** 设备类型 */
  const device = ref<'mobile' | 'tablet' | 'desktop'>('desktop')
  
  /** 侧边栏是否固定（移动端） */
  const sidebarFixed = ref(false)

  // ===== 计算属性 =====
  
  /** 是否是移动设备 */
  const isMobile = computed(() => device.value === 'mobile')
  
  /** 是否是平板设备 */
  const isTablet = computed(() => device.value === 'tablet')
  
  /** 是否是桌面设备 */
  const isDesktop = computed(() => device.value === 'desktop')

  // ===== 方法 =====
  
  /**
   * 切换侧边栏折叠状态
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    
    // 持久化到localStorage
    localStorage.setItem('sidebarCollapsed', String(sidebarCollapsed.value))
    
    console.log('侧边栏状态:', sidebarCollapsed.value ? '折叠' : '展开')
  }
  
  /**
   * 设置侧边栏状态
   * @param collapsed 是否折叠
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebarCollapsed', String(collapsed))
  }
  
  /**
   * 恢复侧边栏状态
   */
  function restoreSidebarState() {
    const saved = localStorage.getItem('sidebarCollapsed')
    if (saved !== null) {
      sidebarCollapsed.value = saved === 'true'
    }
  }
  
  /**
   * 显示全局加载
   * @param text 加载文本
   */
  function showLoading(text: string = '加载中...') {
    loading.value = true
    loadingText.value = text
  }
  
  /**
   * 隐藏全局加载
   */
  function hideLoading() {
    loading.value = false
    loadingText.value = '加载中...'
  }
  
  /**
   * 切换设置抽屉
   */
  function toggleSettings() {
    showSettings.value = !showSettings.value
  }
  
  /**
   * 打开设置抽屉
   */
  function openSettings() {
    showSettings.value = true
  }
  
  /**
   * 关闭设置抽屉
   */
  function closeSettings() {
    showSettings.value = false
  }
  
  /**
   * 设置设备类型
   * @param type 设备类型
   */
  function setDevice(type: 'mobile' | 'tablet' | 'desktop') {
    device.value = type
    
    // 移动设备自动折叠侧边栏
    if (type === 'mobile') {
      sidebarCollapsed.value = true
      sidebarFixed.value = false
    }
  }
  
  /**
   * 检测设备类型（基于窗口宽度）
   */
  function detectDevice() {
    const width = window.innerWidth
    
    if (width < 768) {
      setDevice('mobile')
    } else if (width < 1024) {
      setDevice('tablet')
    } else {
      setDevice('desktop')
    }
  }

  // 初始化：恢复侧边栏状态和检测设备类型
  restoreSidebarState()
  detectDevice()

  return {
    // 状态
    sidebarCollapsed,
    loading,
    loadingText,
    showSettings,
    device,
    sidebarFixed,
    
    // 计算属性
    isMobile,
    isTablet,
    isDesktop,
    
    // 方法
    toggleSidebar,
    setSidebarCollapsed,
    restoreSidebarState,
    showLoading,
    hideLoading,
    toggleSettings,
    openSettings,
    closeSettings,
    setDevice,
    detectDevice
  }
})
