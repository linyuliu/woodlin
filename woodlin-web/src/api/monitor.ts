/**
 * @file api/monitor.ts
 * @description 系统监控：在线用户 / 登录日志 / 操作日志 / 服务器信息 / 缓存监控
 * @author yulin
 * @since 2026-01-01
 */
import { del, get } from '@/utils/request'
import type { PageResult } from '@/types/global'

/** 在线用户 */
export interface OnlineUser {
  tokenId: string
  username: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  loginTime?: string
}

/** 在线用户查询参数 */
export interface OnlineUserQuery {
  page?: number
  size?: number
  username?: string
  ipaddr?: string
}

/** 登录日志 */
export interface LoginLog {
  id: number
  username: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  msg?: string
  /** 0=成功 1=失败 */
  status: string
  loginTime?: string
}

/** 登录日志查询参数 */
export interface LoginLogQuery {
  page?: number
  size?: number
  username?: string
  ipaddr?: string
  status?: string
  startTime?: string
  endTime?: string
}

/** 操作日志 */
export interface OperLog {
  id: number
  title?: string
  /** 业务类型 */
  businessType?: string
  method?: string
  requestMethod?: string
  operName?: string
  deptName?: string
  operUrl?: string
  operIp?: string
  operLocation?: string
  operParam?: string
  jsonResult?: string
  /** 0=成功 1=失败 */
  status: string
  errorMsg?: string
  operTime?: string
  costTime?: number
}

/** 操作日志查询参数 */
export interface OperLogQuery {
  page?: number
  size?: number
  title?: string
  operName?: string
  status?: string
  startTime?: string
  endTime?: string
}

/** CPU 信息 */
export interface CpuInfo {
  cpuNum?: number
  total?: number
  sys?: number
  used?: number
  wait?: number
  free?: number
}

/** 内存信息 */
export interface MemInfo {
  total?: number
  used?: number
  free?: number
  usage?: number
}

/** JVM 信息 */
export interface JvmInfo {
  total?: number
  max?: number
  free?: number
  used?: number
  usage?: number
  version?: string
  home?: string
  name?: string
  startTime?: string
  runTime?: string
  inputArgs?: string
}

/** 系统信息 */
export interface SysInfo {
  computerName?: string
  computerIp?: string
  userDir?: string
  osName?: string
  osArch?: string
}

/** 磁盘信息 */
export interface SysFile {
  dirName?: string
  sysTypeName?: string
  typeName?: string
  total?: string
  free?: string
  used?: string
  usage?: number
}

/** 服务器信息 */
export interface ServerInfo {
  cpu?: CpuInfo
  mem?: MemInfo
  jvm?: JvmInfo
  sys?: SysInfo
  disk?: SysFile[]
}

/** 缓存信息 */
export interface CacheInfo {
  cacheNames?: string[]
  cacheKeys?: string[]
  cacheValue?: string
}

/* ---------------- 在线用户 ---------------- */

/** 分页查询在线用户 */
export function pageOnline(params: OnlineUserQuery): Promise<PageResult<OnlineUser>> {
  return get('/monitor/online', params as Record<string, unknown>)
}

/** 强制下线 */
export function forceLogout(tokenId: string): Promise<void> {
  return del(`/monitor/online/${tokenId}`)
}

/** 批量强制下线 */
export function batchForceLogout(tokenIds: string[]): Promise<void> {
  return del('/monitor/online/batch', { data: { tokenIds } })
}

/* ---------------- 登录日志 ---------------- */

/** 分页查询登录日志 */
export function pageLoginLog(params: LoginLogQuery): Promise<PageResult<LoginLog>> {
  return get('/monitor/loginLog', params as Record<string, unknown>)
}

/** 删除单条登录日志 */
export function deleteLoginLog(id: number): Promise<void> {
  return del(`/monitor/loginLog/${id}`)
}

/** 清空登录日志 */
export function cleanLoginLog(): Promise<void> {
  return del('/monitor/loginLog/clean')
}

/* ---------------- 操作日志 ---------------- */

/** 分页查询操作日志 */
export function pageOperLog(params: OperLogQuery): Promise<PageResult<OperLog>> {
  return get('/monitor/operLog', params as Record<string, unknown>)
}

/** 获取操作日志详情 */
export function getOperLog(id: number): Promise<OperLog> {
  return get(`/monitor/operLog/${id}`)
}

/** 删除单条操作日志 */
export function deleteOperLog(id: number): Promise<void> {
  return del(`/monitor/operLog/${id}`)
}

/** 清空操作日志 */
export function cleanOperLog(): Promise<void> {
  return del('/monitor/operLog/clean')
}

/* ---------------- 服务器监控 ---------------- */

/** 获取服务器信息 */
export function getServerInfo(): Promise<ServerInfo> {
  return get('/monitor/server')
}

/* ---------------- 缓存监控 ---------------- */

/** 获取缓存名称列表 */
export function getCacheNames(): Promise<CacheInfo> {
  return get('/monitor/cache')
}

/** 获取缓存键列表 */
export function getCacheKeys(cacheName: string): Promise<string[]> {
  return get(`/monitor/cache/${encodeURIComponent(cacheName)}/keys`)
}

/** 获取缓存值 */
export function getCacheValue(cacheName: string, cacheKey: string): Promise<CacheInfo> {
  return get(`/monitor/cache/${encodeURIComponent(cacheName)}/${encodeURIComponent(cacheKey)}`)
}

/** 清空指定缓存 */
export function clearCacheName(cacheName: string): Promise<void> {
  return del(`/monitor/cache/${encodeURIComponent(cacheName)}`)
}

/** 删除指定缓存键 */
export function clearCacheKey(cacheName: string, cacheKey: string): Promise<void> {
  return del(`/monitor/cache/${encodeURIComponent(cacheName)}/${encodeURIComponent(cacheKey)}`)
}
