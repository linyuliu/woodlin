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
  <NResult status="500" title="服务器错误" description="服务器发生了错误，请稍后再试" style="margin-top: 10vh">
    <template #footer>
      <NButton @click="router.push('/')">返回首页</NButton>
    </template>
  </NResult>
</template>
