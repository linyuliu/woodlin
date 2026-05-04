<!--
  @file views/error/404.vue
  @description 404 页
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { NButton, NResult } from 'naive-ui'
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <NResult status="404" title="页面不存在" description="您访问的页面不存在" style="margin-top: 10vh">
    <template #footer>
      <NButton @click="router.push('/')">返回首页</NButton>
    </template>
  </NResult>
</template>
