/**
 * Vite 构建配置文件
 * 
 * @author mumu
 * @description Vite构建工具的配置文件，定义插件、路径别名、开发服务器代理等构建相关配置
 * @since 2025-01-01
 * @see https://vite.dev/config/
 */

import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

/**
 * Vite配置
 * 
 * @returns Vite配置对象
 */
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  return {
    plugins: [
      // Vue3支持插件
      vue(),
      // Vue开发者工具插件，用于调试Vue应用
      vueDevTools(),
      // 自动导入Vue API（ref, computed, watch等）
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          {
            'naive-ui': [
              'useDialog',
              'useMessage',
              'useNotification',
              'useLoadingBar'
            ]
          }
        ],
        dts: 'src/auto-imports.d.ts',
        dirs: [
          'src/composables',
          'src/stores',
          'src/utils'
        ],
        vueTemplate: true,
        eslintrc: {
          enabled: true,
          filepath: './.eslintrc-auto-import.json',
          globalsPropValue: true
        }
      }),
      // 自动导入组件
      Components({
        resolvers: [NaiveUiResolver()],
        dts: 'src/components.d.ts',
        dirs: ['src/components'],
        extensions: ['vue'],
        deep: true,
        directoryAsNamespace: false
      })
    ],
    resolve: {
      alias: {
        // 设置'@'为src目录的别名，便于模块导入
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    // 开发服务器配置
    server: {
      port: 5173,
      host: true,
      open: false,
      // 配置代理，解决开发环境跨域问题
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path
        }
      }
    },
    // 生产构建配置
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true
        }
      },
      rollupOptions: {
        output: {
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'ui-vendor': ['naive-ui']
          }
        }
      },
      chunkSizeWarningLimit: 1000
    }
  }
})
