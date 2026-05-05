<!--
  @file layouts/components/AppTopMenu/index.vue
  @description 顶部菜单：用于 top/mix 布局承载一级导航，支持根据当前路由高亮
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu, type MenuOption } from 'naive-ui'
import type { RouteItem } from '@/types/global'

const props = defineProps<{
  items: RouteItem[]
}>()

const route = useRoute()
const router = useRouter()

function findFirstPath(item: RouteItem): string {
  if (item.path) {
    return item.path
  }
  if (item.children?.length) {
    return findFirstPath(item.children[0]!)
  }
  return '/'
}

function containsPath(item: RouteItem, fullPath: string): boolean {
  if (item.path && fullPath.startsWith(item.path)) {
    return true
  }
  return item.children?.some((child) => containsPath(child, fullPath)) ?? false
}

const menuOptions = computed<MenuOption[]>(() =>
  props.items.map((item) => ({
    key: item.name || item.path,
    label: item.title,
  })),
)

const activeKey = computed(() => {
  const matched = props.items.find((item) => containsPath(item, route.path))
  return matched?.name || matched?.path || null
})

function handleSelect(key: string): void {
  const target = props.items.find((item) => (item.name || item.path) === key)
  if (!target) {return}
  void router.push(findFirstPath(target))
}
</script>

<template>
  <NMenu
    mode="horizontal"
    responsive
    :value="activeKey ?? undefined"
    :options="menuOptions"
    @update:value="handleSelect"
  />
</template>
