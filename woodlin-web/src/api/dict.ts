/**
 * 字典管理API服务
 * 
 * @author mumu
 * @description 字典数据管理相关的API接口调用
 * @since 2025-01-01
 */

import request from '@/utils/request'

/**
 * 字典项
 */
export interface DictItem {
  label: string
  value: string | number
  [key: string]: unknown
}

/**
 * 演示用户对象
 */
export interface DemoUser {
  id: number
  name: string
  gender: {
    label: string
    value: string
  }
  status: {
    label: string
    value: string
  }
}

/**
 * 获取用户状态字典
 */
export function getUserStatusDict(): Promise<DictItem[]> {
  return request.get('/dict/user-status')
}

/**
 * 获取性别字典
 */
export function getGenderDict(): Promise<DictItem[]> {
  return request.get('/dict/gender')
}

/**
 * 获取演示用户对象
 * 展示字典枚举在对象中的序列化效果
 */
export function getDemoUser(): Promise<DemoUser> {
  return request.get('/dict/demo-user')
}
