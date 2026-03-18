import { onMounted, onUnmounted, ref, type Ref } from 'vue'
import { getConfig } from '@/config'
import {
  getActivityMonitoringConfig,
  getActivityStatus,
  recordUserInteraction,
  type ActivityMonitoringConfig
} from '@/api/security'
import { logger } from '@/utils/logger'

type ActivityMonitoringState = {
  statusCheckTimer: number | null
  lastActivityTime: number
}

const MONITORED_EVENTS = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart'] as const
const THROTTLE_DELAY = 5000
const DEFAULT_CONFIG: ActivityMonitoringConfig = {
  enabled: true,
  timeoutSeconds: 1800,
  checkIntervalSeconds: 60,
  monitorApiRequests: true,
  monitorUserInteractions: true,
  warningBeforeTimeoutSeconds: 300
}

function createState(): ActivityMonitoringState {
  return {
    statusCheckTimer: null,
    lastActivityTime: Date.now()
  }
}

function createThrottledAction(action: () => void, delay: number): () => void {
  let lastTime = 0

  return () => {
    const now = Date.now()
    if (now - lastTime < delay) {
      return
    }

    lastTime = now
    action()
  }
}

function toggleEventListeners(handler: () => void, action: 'add' | 'remove'): void {
  MONITORED_EVENTS.forEach((eventName) => {
    const method = action === 'add' ? 'addEventListener' : 'removeEventListener'
    document[method](eventName, handler, true)
  })
}

async function loadActivityConfig(
  config: Ref<ActivityMonitoringConfig>,
  isEnabled: Ref<boolean>
): Promise<void> {
  try {
    const response = await getActivityMonitoringConfig()
    config.value = response
    isEnabled.value = response.enabled
  } catch (error) {
    logger.error('获取活动监控配置失败:', error)
    isEnabled.value = false
  }
}

function createRecordInteraction(config: Ref<ActivityMonitoringConfig>, state: ActivityMonitoringState): () => Promise<void> {
  return async () => {
    if (!config.value.enabled || !config.value.monitorUserInteractions) {
      return
    }

    try {
      state.lastActivityTime = Date.now()
      await recordUserInteraction()
    } catch (error) {
      logger.error('记录用户活动失败:', error)
    }
  }
}

function createStatusChecker(config: Ref<ActivityMonitoringConfig>): () => Promise<void> {
  return async () => {
    if (!config.value.enabled) {
      return
    }

    try {
      const response = await getActivityStatus()
      if (response.status === 'timeout') {
        window.location.href = `${getConfig().router.loginPath}?reason=timeout`
      }
    } catch (error) {
      logger.error('检查用户活动状态失败:', error)
    }
  }
}

function createMonitoringController(
  config: Ref<ActivityMonitoringConfig>,
  isEnabled: Ref<boolean>,
  state: ActivityMonitoringState,
  recordInteraction: () => void,
  checkActivityStatus: () => Promise<void>
) {
  const startMonitoring = (): void => {
    if (!isEnabled.value) {
      return
    }

    toggleEventListeners(recordInteraction, 'add')
    if (config.value.checkIntervalSeconds > 0) {
      state.statusCheckTimer = window.setInterval(checkActivityStatus, config.value.checkIntervalSeconds * 1000)
    }
  }

  const stopMonitoring = (): void => {
    toggleEventListeners(recordInteraction, 'remove')
    if (state.statusCheckTimer !== null) {
      clearInterval(state.statusCheckTimer)
      state.statusCheckTimer = null
    }
  }

  return { startMonitoring, stopMonitoring }
}

function createRemainingTimeGetter(config: Ref<ActivityMonitoringConfig>, state: ActivityMonitoringState): () => number {
  return () => {
    if (!config.value.enabled || config.value.timeoutSeconds <= 0) {
      return -1
    }

    const elapsed = Math.floor((Date.now() - state.lastActivityTime) / 1000)
    return Math.max(0, config.value.timeoutSeconds - elapsed)
  }
}

export function useActivityMonitoring() {
  const isEnabled = ref(false)
  const config = ref<ActivityMonitoringConfig>({ ...DEFAULT_CONFIG })
  const state = createState()
  const recordInteraction = createRecordInteraction(config, state)
  const throttledRecordInteraction = createThrottledAction(() => void recordInteraction(), THROTTLE_DELAY)
  const checkActivityStatus = createStatusChecker(config)
  const { startMonitoring, stopMonitoring } = createMonitoringController(
    config,
    isEnabled,
    state,
    throttledRecordInteraction,
    checkActivityStatus
  )
  const getRemainingTime = createRemainingTimeGetter(config, state)
  const shouldShowWarning = () => {
    const remaining = getRemainingTime()
    return remaining > 0 && remaining <= config.value.warningBeforeTimeoutSeconds
  }

  onMounted(async () => {
    await loadActivityConfig(config, isEnabled)
    if (isEnabled.value) {
      startMonitoring()
    }
  })
  onUnmounted(() => stopMonitoring())

  return {
    isEnabled,
    config,
    getRemainingTime,
    shouldShowWarning,
    recordInteraction: throttledRecordInteraction,
    startMonitoring,
    stopMonitoring
  }
}
