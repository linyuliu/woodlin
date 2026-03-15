/**
 * 应用全局状态管理 Store
 *
 * @author mumu
 * @description 管理应用级别的全局状态，如加载状态、侧边栏折叠等
 * @since 2025-01-01
 */

import {computed, ref, type ComputedRef, type Ref} from 'vue'
import {defineStore} from 'pinia'

type DeviceType = 'mobile' | 'tablet' | 'desktop'
type ThemeMode = 'light' | 'dark'

type AppStateRefs = {
  sidebarCollapsed: Ref<boolean>
  loading: Ref<boolean>
  loadingText: Ref<string>
  showSettings: Ref<boolean>
  device: Ref<DeviceType>
  sidebarFixed: Ref<boolean>
  themeMode: Ref<ThemeMode>
}

type AppComputed = {
  isMobile: ComputedRef<boolean>
  isTablet: ComputedRef<boolean>
  isDesktop: ComputedRef<boolean>
  isDarkMode: ComputedRef<boolean>
}

const SIDEBAR_STORAGE_KEY = 'sidebarCollapsed'
const THEME_STORAGE_KEY = 'themeMode'

/**
 * 创建应用状态
 *
 * @returns 状态对象
 */
function createAppState(): AppStateRefs {
  return {
    sidebarCollapsed: ref(false),
    loading: ref(false),
    loadingText: ref('加载中...'),
    showSettings: ref(false),
    device: ref<DeviceType>('desktop'),
    sidebarFixed: ref(false),
    themeMode: ref<ThemeMode>('light')
  }
}

/**
 * 创建计算属性
 *
 * @param state 状态对象
 * @returns 计算属性
 */
function createAppComputed(state: AppStateRefs): AppComputed {
  return {
    isMobile: computed(() => state.device.value === 'mobile'),
    isTablet: computed(() => state.device.value === 'tablet'),
    isDesktop: computed(() => state.device.value === 'desktop'),
    isDarkMode: computed(() => state.themeMode.value === 'dark')
  }
}

/**
 * 创建侧边栏动作
 *
 * @param state 状态对象
 * @returns 动作集合
 */
function createSidebarActions(state: AppStateRefs) {
  const setSidebarCollapsed = (collapsed: boolean) => {
    state.sidebarCollapsed.value = collapsed
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed))
  }

  const toggleSidebar = () => {
    setSidebarCollapsed(!state.sidebarCollapsed.value)
  }

  const restoreSidebarState = () => {
    const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY)
    if (saved !== null) {
      state.sidebarCollapsed.value = saved === 'true'
    }
  }

  return {
    toggleSidebar,
    setSidebarCollapsed,
    restoreSidebarState
  }
}

/**
 * 创建主题动作
 *
 * @param state 状态对象
 * @returns 动作集合
 */
function createThemeActions(state: AppStateRefs) {
  const applyTheme = () => {
    const root = document.documentElement
    root.setAttribute('data-theme', state.themeMode.value)
    root.style.colorScheme = state.themeMode.value
  }

  const setThemeMode = (mode: ThemeMode) => {
    state.themeMode.value = mode
    localStorage.setItem(THEME_STORAGE_KEY, mode)
    applyTheme()
  }

  const toggleThemeMode = () => {
    setThemeMode(state.themeMode.value === 'light' ? 'dark' : 'light')
  }

  const restoreThemeMode = () => {
    const saved = localStorage.getItem(THEME_STORAGE_KEY)
    if (saved === 'light' || saved === 'dark') {
      state.themeMode.value = saved
    }
    applyTheme()
  }

  return {
    applyTheme,
    setThemeMode,
    toggleThemeMode,
    restoreThemeMode
  }
}

/**
 * 创建加载动作
 *
 * @param state 状态对象
 * @returns 动作集合
 */
function createLoadingActions(state: AppStateRefs) {
  const showLoading = (text = '加载中...') => {
    state.loading.value = true
    state.loadingText.value = text
  }

  const hideLoading = () => {
    state.loading.value = false
    state.loadingText.value = '加载中...'
  }

  return {showLoading, hideLoading}
}

/**
 * 创建设置面板动作
 *
 * @param state 状态对象
 * @returns 动作集合
 */
function createSettingsActions(state: AppStateRefs) {
  const toggleSettings = () => {
    state.showSettings.value = !state.showSettings.value
  }

  const openSettings = () => {
    state.showSettings.value = true
  }

  const closeSettings = () => {
    state.showSettings.value = false
  }

  return {
    toggleSettings,
    openSettings,
    closeSettings
  }
}

/**
 * 创建设备动作
 *
 * @param state 状态对象
 * @returns 动作集合
 */
function createDeviceActions(state: AppStateRefs) {
  const setDevice = (type: DeviceType) => {
    state.device.value = type
    if (type === 'mobile') {
      state.sidebarCollapsed.value = true
      state.sidebarFixed.value = false
    }
  }

  const detectDevice = () => {
    const width = window.innerWidth
    if (width < 768) {
      setDevice('mobile')
      return
    }
    if (width < 1024) {
      setDevice('tablet')
      return
    }
    setDevice('desktop')
  }

  return {
    setDevice,
    detectDevice
  }
}

/**
 * 组装 App Store
 *
 * @returns store 内容
 */
function createAppStore() {
  const state = createAppState()
  const computedState = createAppComputed(state)
  const sidebarActions = createSidebarActions(state)
  const themeActions = createThemeActions(state)
  const loadingActions = createLoadingActions(state)
  const settingsActions = createSettingsActions(state)
  const deviceActions = createDeviceActions(state)

  sidebarActions.restoreSidebarState()
  themeActions.restoreThemeMode()
  deviceActions.detectDevice()

  return {
    ...state,
    ...computedState,
    ...sidebarActions,
    ...themeActions,
    ...loadingActions,
    ...settingsActions,
    ...deviceActions
  }
}

/**
 * 应用全局状态管理 Store
 */
export const useAppStore = defineStore('app', createAppStore)
