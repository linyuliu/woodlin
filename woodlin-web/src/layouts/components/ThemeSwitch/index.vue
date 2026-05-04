<!--
  @file layouts/components/ThemeSwitch/index.vue
  @description 深色 / 浅色主题切换按钮
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed } from 'vue'
import { NButton } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { useAppStore } from '@/stores/modules/app'

const appStore = useAppStore()
const icon = computed(() =>
  appStore.isDark ? 'vicons:antd:SunOutlined' : 'vicons:antd:MoonOutlined',
)

/** 切换深色 / 浅色 */
function toggle(): void {
  appStore.toggleDark()
}
</script>

<template>
  <NButton quaternary circle @click="toggle">
    <template #icon>
      <WIcon :icon="icon" />
    </template>
  </NButton>
</template>
