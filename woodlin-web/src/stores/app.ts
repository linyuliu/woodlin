/**
 * 应用全局状态管理 Store
 * 
 * @author mumu
 * @description 管理应用级别的全局状态，如加载状态、侧边栏折叠等
 * @since 2025-01-01
 */

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

type DeviceType = 'mobile' | 'tablet' | 'desktop'
type ThemeMode = 'light' | 'dark'

/**
 * 应用全局状态管理 Store
 */
export const useAppStore = defineStore('app', () => {
  const SIDEBAR_STORAGE_KEY = 'sidebarCollapsed'
  const THEME_STORAGE_KEY = 'themeMode'

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
  const device = ref<DeviceType>('desktop')
  
  /** 侧边栏是否固定（移动端） */
  const sidebarFixed = ref(false)

  /** 全局主题模式 */
  const themeMode = ref<ThemeMode>('light')

  // ===== 计算属性 =====
  
  /** 是否是移动设备 */
  const isMobile = computed(() => device.value === 'mobile')
  
  /** 是否是平板设备 */
  const isTablet = computed(() => device.value === 'tablet')
  
  /** 是否是桌面设备 */
  const isDesktop = computed(() => device.value === 'desktop')

  /** 是否为深色主题 */
  const isDarkMode = computed(() => themeMode.value === 'dark')

  // ===== 方法 =====
  
  /**
   * 切换侧边栏折叠状态
   */
  function toggleSidebar() {
    setSidebarCollapsed(!sidebarCollapsed.value)
  }
  
  /**
   * 设置侧边栏状态
   * @param collapsed 是否折叠
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed))
  }
  
  /**
   * 恢复侧边栏状态
   */
  function restoreSidebarState() {
    const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY)
    if (saved !== null) {
      sidebarCollapsed.value = saved === 'true'
    }
  }

  /**
   * 应用主题到根节点
   */
  function applyTheme() {
    const root = document.documentElement
    root.setAttribute('data-theme', themeMode.value)
    root.style.colorScheme = themeMode.value
  }

  /**
   * 设置主题模式
   */
  function setThemeMode(mode: ThemeMode) {
    themeMode.value = mode
    localStorage.setItem(THEME_STORAGE_KEY, mode)
    applyTheme()
  }

  /**
   * 切换浅色/深色主题
   */
  function toggleThemeMode() {
    setThemeMode(themeMode.value === 'light' ? 'dark' : 'light')
  }

  /**
   * 恢复主题模式
   */
  function restoreThemeMode() {
    const saved = localStorage.getItem(THEME_STORAGE_KEY)
    if (saved === 'light' || saved === 'dark') {
      themeMode.value = saved
    }
    applyTheme()
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
  function setDevice(type: DeviceType) {
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
  restoreThemeMode()
  detectDevice()

  return {
    // 状态
    sidebarCollapsed,
    loading,
    loadingText,
    showSettings,
    device,
    sidebarFixed,
    themeMode,
    
    // 计算属性
    isMobile,
    isTablet,
    isDesktop,
    isDarkMode,
    
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
    detectDevice,
    setThemeMode,
    toggleThemeMode,
    applyTheme,
    restoreThemeMode
  }
})
