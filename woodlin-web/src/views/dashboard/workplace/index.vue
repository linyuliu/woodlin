<!--
  @file views/dashboard/workplace/index.vue
  @description 工作台占位页
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { NCard } from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()
</script>

<template>
  <NCard title="工作台">
    <p>欢迎回来，{{ userStore.userInfo?.nickname ?? userStore.userInfo?.username ?? '访客' }}！</p>
    <p>当前为骨架页面，业务模块将在后续 Commit 中接入。</p>
  </NCard>
</template>
