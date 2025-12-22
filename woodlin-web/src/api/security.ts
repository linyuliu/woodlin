/**
 * 安全配置API服务
 * 
 * @author mumu
 * @description 系统安全配置管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 用户活动监控配置
 */
export interface ActivityMonitoringConfig {
  enabled: boolean
  timeoutSeconds: number
  checkIntervalSeconds: number
  monitorApiRequests: boolean
  monitorUserInteractions: boolean
  warningBeforeTimeoutSeconds: number
}

/**
 * 用户活动状态响应
 */
export interface ActivityStatusResponse {
  status: string
  message: string
  remainingSeconds?: number
  warningThreshold?: boolean
}

/**
 * 获取用户活动监控配置
 */
export function getActivityMonitoringConfig(): Promise<ActivityMonitoringConfig> {
  return request.get('/security/activity-monitoring/config')
}

/**
 * 记录用户交互活动
 * 前端调用此接口记录用户的键盘、鼠标等交互活动
 */
export function recordUserInteraction(): Promise<void> {
  return request.post('/security/activity-monitoring/record-interaction')
}

/**
 * 检查当前用户活动状态
 */
export function getActivityStatus(): Promise<ActivityStatusResponse> {
  return request.get('/security/activity-monitoring/status')
}
