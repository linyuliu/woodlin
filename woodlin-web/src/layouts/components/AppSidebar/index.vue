<!--
  @file layouts/components/AppSidebar/index.vue
  @description 左侧菜单栏：基于 route store 渲染 n-menu，支持折叠
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, h, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NMenu, type MenuOption } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import AppLogo from '../AppLogo/index.vue'
import { useRouteStore } from '@/stores/modules/route'
import type { RouteItem } from '@/types/global'

const props = withDefaults(
  defineProps<{
    /** 是否折叠 */
    collapsed?: boolean
    /** 可选菜单项（mix 布局下传入当前顶级菜单的 children） */
    items?: RouteItem[]
  }>(),
  { collapsed: false, items: undefined },
)

const router = useRouter()
const route = useRoute()
const routeStore = useRouteStore()

/** 将 RouteItem 转换为 n-menu options */
function toMenuOption(item: RouteItem): MenuOption {
  const option: MenuOption = {
    key: item.name || item.path,
    label: item.title,
  }
  if (item.icon) {
    option.icon = () => h(WIcon, { icon: item.icon as string })
  }
  if (item.children && item.children.length) {
    option.children = item.children.map(toMenuOption)
  }
  return option
}

const currentMenuItems = computed<RouteItem[]>(() => props.items ?? routeStore.menuItems)

const menuOptions = computed<MenuOption[]>(() =>
  currentMenuItems.value.map(toMenuOption),
)

const activeKey = ref<string>(String(route.name ?? ''))
watch(
  () => route.name,
  (n) => {
    activeKey.value = String(n ?? '')
  },
)

/** 在菜单树中查找 key 对应的路径 */
function findPath(items: RouteItem[], key: string): string | null {
  for (const it of items) {
    if ((it.name || it.path) === key) {return it.path}
    if (it.children) {
      const r = findPath(it.children, key)
      if (r) {return r}
    }
  }
  return null
}

/** 菜单点击 */
function handleSelect(key: string): void {
  const path = findPath(currentMenuItems.value, key)
  if (path) {void router.push(path)}
}
</script>

<template>
  <div class="app-sidebar">
    <AppLogo :collapsed="props.collapsed" />
    <NMenu
      :value="activeKey"
      :options="menuOptions"
      :collapsed="props.collapsed"
      :collapsed-width="64"
      :collapsed-icon-size="20"
      :indent="18"
      @update:value="handleSelect"
    />
  </div>
</template>

<style scoped>
.app-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
