<!--
  @file views/error/500.vue
  @description 500 页
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { NButton, NResult } from 'naive-ui'
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <NResult status="500" title="500" description="服务器异常，请稍后重试">
    <template #footer>
      <NButton @click="router.push('/')">返回首页</NButton>
    </template>
  </NResult>
</template>
