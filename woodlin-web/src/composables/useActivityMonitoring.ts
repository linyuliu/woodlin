import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

/**
 * 用户活动监控组合式函数
 * @description 监控用户的键盘、鼠标等交互活动，自动记录活动状态
 */
export function useActivityMonitoring() {
  const isEnabled = ref(false)
  const config = ref({
    enabled: true,
    timeoutSeconds: 1800,
    checkIntervalSeconds: 60,
    monitorApiRequests: true,
    monitorUserInteractions: true,
    warningBeforeTimeoutSeconds: 300
  })

  let statusCheckTimer: number | null = null
  let lastActivityTime = Date.now()

  /**
   * 记录用户交互活动
   */
  const recordInteraction = async () => {
    if (!config.value.enabled || !config.value.monitorUserInteractions) {
      return
    }

    try {
      lastActivityTime = Date.now()
      await axios.post('/api/security/activity-monitoring/record-interaction')
    } catch (error) {
      console.error('记录用户活动失败:', error)
    }
  }

  /**
   * 节流函数,避免频繁调用
   */
  const throttleRecordInteraction = (() => {
    let lastTime = 0
    const delay = 5000

    return () => {
      const now = Date.now()
      if (now - lastTime >= delay) {
        lastTime = now
        recordInteraction()
      }
    }
  })()

  /**
   * 检查活动状态
   */
  const checkActivityStatus = async () => {
    if (!config.value.enabled) {
      return
    }

    try {
      await axios.get('/api/security/activity-monitoring/status')
    } catch (error: unknown) {
      const err = error as { response?: { status?: number } }
      if (err.response?.status === 401) {
        window.location.href = '/login?reason=timeout'
      }
    }
  }

  /**
   * 获取配置信息
   */
  const loadConfig = async () => {
    try {
      const response = await axios.get('/api/security/activity-monitoring/config')
      config.value = response.data
      isEnabled.value = config.value.enabled
    } catch (error) {
      console.error('获取活动监控配置失败:', error)
      isEnabled.value = false
    }
  }

  // 开始监控
  const startMonitoring = () => {
    if (!isEnabled.value) {
      return
    }

    // 监听用户交互事件
    const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart']
    
    events.forEach(event => {
      document.addEventListener(event, throttleRecordInteraction, true)
    })

    // 定期检查状态
    if (config.value.checkIntervalSeconds > 0) {
      statusCheckTimer = window.setInterval(
        checkActivityStatus, 
        config.value.checkIntervalSeconds * 1000
      )
    }
  }

  // 停止监控
  const stopMonitoring = () => {
    const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart']
    
    events.forEach(event => {
      document.removeEventListener(event, throttleRecordInteraction, true)
    })

    if (statusCheckTimer) {
      clearInterval(statusCheckTimer)
      statusCheckTimer = null
    }
  }

  // 获取剩余时间（秒）
  const getRemainingTime = () => {
    if (!config.value.enabled || config.value.timeoutSeconds <= 0) {
      return -1
    }

    const elapsed = Math.floor((Date.now() - lastActivityTime) / 1000)
    const remaining = config.value.timeoutSeconds - elapsed
    return Math.max(0, remaining)
  }

  // 检查是否需要警告
  const shouldShowWarning = () => {
    const remaining = getRemainingTime()
    return remaining > 0 && remaining <= config.value.warningBeforeTimeoutSeconds
  }

  onMounted(async () => {
    await loadConfig()
    if (isEnabled.value) {
      startMonitoring()
    }
  })

  onUnmounted(() => {
    stopMonitoring()
  })

  return {
    isEnabled,
    config,
    getRemainingTime,
    shouldShowWarning,
    recordInteraction: throttleRecordInteraction,
    startMonitoring,
    stopMonitoring
  }
}