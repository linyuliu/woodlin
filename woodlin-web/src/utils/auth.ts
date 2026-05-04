/**
 * @file utils/auth.ts
 * @description Token 本地存储读写工具
 * @author yulin
 * @since 2026-05-04
 */
import { STORAGE_KEYS } from '@/constants'

const TOKEN_KEY = STORAGE_KEYS.TOKEN

/** 读取 Token */
export const getToken = (): string | null => localStorage.getItem(TOKEN_KEY)

/** 写入 Token */
export const setToken = (token: string): void => localStorage.setItem(TOKEN_KEY, token)

/** 删除 Token */
export const removeToken = (): void => localStorage.removeItem(TOKEN_KEY)
