<!--
  @file layouts/MixLayout/index.vue
  @description 混合布局：顶部展示一级导航，左侧展示当前一级菜单的子菜单
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { NLayout, NLayoutContent, NLayoutHeader, NLayoutSider } from 'naive-ui'
import AppHeader from '../components/AppHeader/index.vue'
import AppSidebar from '../components/AppSidebar/index.vue'
import AppTabs from '../components/AppTabs/index.vue'
import AppTopMenu from '../components/AppTopMenu/index.vue'
import type { RouteItem } from '@/types/global'
import { useAppStore } from '@/stores/modules/app'
import { useRouteStore } from '@/stores/modules/route'
import { settings } from '@/config/settings'

const appStore = useAppStore()
const routeStore = useRouteStore()
const route = useRoute()

const collapsed = computed<boolean>({
  get: () => appStore.sidebarCollapsed,
  set: (value) => {
    if (value !== appStore.sidebarCollapsed) {
      appStore.toggleSidebar()
    }
  },
})

function containsPath(item: RouteItem, fullPath: string): boolean {
  if (item.path && fullPath.startsWith(item.path)) {
    return true
  }
  return item.children?.some((child) => containsPath(child, fullPath)) ?? false
}

const topMenus = computed<RouteItem[]>(() => routeStore.menuItems)

const sideMenus = computed<RouteItem[]>(() => {
  const currentTop = topMenus.value.find((item) => containsPath(item, route.path))
  if (!currentTop) {
    return routeStore.menuItems
  }
  return currentTop.children?.length ? currentTop.children : [currentTop]
})
</script>

<template>
  <NLayout has-sider class="mix-layout">
    <NLayoutSider
      v-model:collapsed="collapsed"
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :native-scrollbar="false"
    >
      <AppSidebar :collapsed="collapsed" :items="sideMenus" />
    </NLayoutSider>
    <NLayout>
      <NLayoutHeader bordered>
        <AppHeader>
          <template #center>
            <div class="mix-layout__top-menu">
              <AppTopMenu :items="topMenus" />
            </div>
          </template>
        </AppHeader>
      </NLayoutHeader>
      <AppTabs v-if="settings.enableTabs" />
      <NLayoutContent class="mix-layout__content" :native-scrollbar="false">
        <RouterView v-slot="{ Component }">
          <component :is="Component" />
        </RouterView>
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.mix-layout {
  height: 100vh;
}

.mix-layout__top-menu {
  flex: 1;
  min-width: 0;
}

.mix-layout__content {
  padding: 16px;
  background: var(--w-color-bg);
  min-height: calc(100vh - var(--w-header-height));
}
</style>
