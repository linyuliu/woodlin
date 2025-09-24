/**
 * Vite 构建配置文件
 * 
 * @author mumu
 * @description Vite构建工具的配置文件，定义插件、路径别名等构建相关配置
 * @since 2025-01-01
 * @see https://vite.dev/config/
 */

import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

/**
 * Vite配置
 * 
 * @returns Vite配置对象
 */
export default defineConfig({
  plugins: [
    // Vue3支持插件
    vue(),
    // Vue开发者工具插件，用于调试Vue应用
    vueDevTools(),
  ],
  resolve: {
    alias: {
      // 设置'@'为src目录的别名，便于模块导入
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
