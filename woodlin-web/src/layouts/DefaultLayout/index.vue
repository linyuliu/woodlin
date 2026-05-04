<!--
  @file layouts/DefaultLayout/index.vue
  @description 默认布局：左侧可折叠 Sider + 顶部 Header + 多标签 + 内容区
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { NLayout, NLayoutContent, NLayoutHeader, NLayoutSider } from 'naive-ui'
import AppSidebar from '../components/AppSidebar/index.vue'
import AppHeader from '../components/AppHeader/index.vue'
import AppTabs from '../components/AppTabs/index.vue'
import { useAppStore } from '@/stores/modules/app'
import { settings } from '@/config/settings'

const appStore = useAppStore()
const collapsed = computed<boolean>({
  get: () => appStore.sidebarCollapsed,
  set: (v) => {
    if (v !== appStore.sidebarCollapsed) {appStore.toggleSidebar()}
  },
})
</script>

<template>
  <NLayout has-sider class="default-layout">
    <NLayoutSider
      v-model:collapsed="collapsed"
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      show-trigger="bar"
      :native-scrollbar="false"
    >
      <AppSidebar :collapsed="collapsed" />
    </NLayoutSider>
    <NLayout>
      <NLayoutHeader bordered>
        <AppHeader />
      </NLayoutHeader>
      <AppTabs v-if="settings.enableTabs" />
      <NLayoutContent class="default-layout__content" :native-scrollbar="false">
        <RouterView v-slot="{ Component }">
          <component :is="Component" />
        </RouterView>
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.default-layout {
  height: 100vh;
}
.default-layout__content {
  padding: 16px;
  background: var(--w-color-bg);
  min-height: calc(100vh - var(--w-header-height));
}
</style>
