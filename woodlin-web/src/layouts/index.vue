<!--
  @file layouts/index.vue
  @description 布局壳组件：根据 app store 的 layoutMode 选择 Default/Mix/Top 三种布局实现
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed } from 'vue'
import DefaultLayout from './DefaultLayout/index.vue'
import MixLayout from './MixLayout/index.vue'
import TopLayout from './TopLayout/index.vue'
import { useAppStore } from '@/stores/modules/app'

const appStore = useAppStore()

const currentLayout = computed(() => {
  if (appStore.currentLayoutMode === 'mix') {
    return MixLayout
  }
  if (appStore.currentLayoutMode === 'top') {
    return TopLayout
  }
  return DefaultLayout
})
</script>

<template>
  <component :is="currentLayout" />
</template>
