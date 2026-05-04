<!--
  @file App.vue
  @description 根组件，提供 Naive UI Provider 与全局消息处理
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { computed, defineComponent, h, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import {
  NConfigProvider,
  NDialogProvider,
  NLoadingBarProvider,
  NMessageProvider,
  NNotificationProvider,
  darkTheme,
  useMessage,
} from 'naive-ui'
import { useAppStore } from '@/stores/modules/app'
import { themeOverrides } from '@/config/theme'
import { setRequestErrorHandler } from '@/utils/request'

const appStore = useAppStore()
const theme = computed(() => (appStore.isDark ? darkTheme : null))

/** 内部 Shell：在 Provider 树内部调用 useMessage，注入到 axios 拦截器 */
const AppShell = defineComponent({
  name: 'AppShell',
  setup() {
    const msg = useMessage()
    onMounted(() => {
      setRequestErrorHandler((m: string) => msg.error(m))
    })
    return () => h(RouterView)
  },
})
</script>

<template>
  <NConfigProvider :theme="theme" :theme-overrides="themeOverrides">
    <NLoadingBarProvider>
      <NMessageProvider>
        <NDialogProvider>
          <NNotificationProvider>
            <AppShell />
          </NNotificationProvider>
        </NDialogProvider>
      </NMessageProvider>
    </NLoadingBarProvider>
  </NConfigProvider>
</template>
