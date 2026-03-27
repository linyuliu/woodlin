import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

/**
 * 根据依赖来源拆分 Rollup chunk。
 *
 * 这里优先把体积较大的 UI 依赖、图标依赖和基础框架拆开，
 * 避免入口 chunk 因为共享依赖过多而持续膨胀。
 *
 * @param id 模块绝对路径
 * @returns chunk 名称
 */
function resolveManualChunk(id: string): string | undefined {
  if (!id.includes('node_modules')) {
    return undefined
  }

  if (id.includes('node_modules/naive-ui')) {
    return 'naive-ui'
  }

  if (id.includes('node_modules/@vicons/ionicons5')) {
    return 'icon-ionicons'
  }

  if (id.includes('node_modules/@vicons/antd')) {
    return 'icon-antd'
  }

  if (id.includes('node_modules/vue/') || id.includes('node_modules/vue-router/') || id.includes('node_modules/pinia/')) {
    return 'vue-vendor'
  }

  if (id.includes('node_modules/axios/')) {
    return 'http-vendor'
  }

  return 'app-vendor'
}

function createPlugins() {
  return [
    vue(),
    vueDevTools(),
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
      dirs: ['src/composables', 'src/stores', 'src/utils'],
      vueTemplate: true,
      eslintrc: {
        enabled: true,
        filepath: './.eslintrc-auto-import.json',
        globalsPropValue: true
      }
    }),
    Components({
      resolvers: [NaiveUiResolver()],
      dts: 'src/components.d.ts',
      dirs: ['src/components'],
      extensions: ['vue'],
      deep: true,
      directoryAsNamespace: false
    })
  ]
}

function createServerConfig(env: Record<string, string>) {
  return {
    port: 5173,
    host: true,
    open: false,
    proxy: {
      '/api': {
        target: env.VITE_BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}

function createBuildConfig() {
  return {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser' as const,
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: resolveManualChunk
      }
    },
    chunkSizeWarningLimit: 1000
  }
}

function createViteConfig(env: Record<string, string>) {
  return {
    plugins: createPlugins(),
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: createServerConfig(env),
    build: createBuildConfig()
  }
}

export default defineConfig(({ mode }) => createViteConfig(loadEnv(mode, process.cwd())))
