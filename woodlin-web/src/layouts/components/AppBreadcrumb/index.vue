<!--
  @file layouts/components/AppBreadcrumb/index.vue
  @description 面包屑导航：基于 route.matched 渲染
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { NBreadcrumb, NBreadcrumbItem } from 'naive-ui'

const route = useRoute()

interface Crumb {
  title: string
  path: string
}

/** 从 matched 构建面包屑 */
const crumbs = computed<Crumb[]>(() => {
  const list: Crumb[] = []
  for (const m of route.matched) {
    const meta = (m.meta ?? {}) as Record<string, unknown>
    if (meta.hidden) continue
    const title = (meta.title as string) || (m.name as string) || ''
    if (!title) continue
    if (list.length && list[list.length - 1]!.title === title) continue
    list.push({ title, path: m.path })
  }
  return list
})
</script>

<template>
  <NBreadcrumb class="app-breadcrumb">
    <NBreadcrumbItem v-for="c in crumbs" :key="c.path">
      {{ c.title }}
    </NBreadcrumbItem>
  </NBreadcrumb>
</template>

<style scoped>
.app-breadcrumb {
  display: inline-flex;
  align-items: center;
}
</style>
