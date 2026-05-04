<!--
  @file views/default/redirect/index.vue
  @description Tab 刷新中转页：立即将当前路由 replace 到目标路径
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const raw = route.params.path
  const path = Array.isArray(raw) ? raw.join('/') : (raw as string | undefined) ?? ''
  const query = route.query
  void router.replace({ path: '/' + path, query })
})
</script>

<template>
  <div />
</template>
