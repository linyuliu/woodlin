<!--
  @file views/error/403.vue
  @description 403 无权限页
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { NButton, NResult } from 'naive-ui'
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <NResult status="403" title="无权限" description="您没有权限访问此页面" style="margin-top: 10vh">
    <template #footer>
      <NButton @click="router.push('/')">返回首页</NButton>
    </template>
  </NResult>
</template>
