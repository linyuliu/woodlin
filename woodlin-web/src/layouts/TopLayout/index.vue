<!--
  @file layouts/TopLayout/index.vue
  @description 顶部布局：一级导航全部放在头部，内容区全宽展示
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { RouterView } from 'vue-router'
import { NLayout, NLayoutContent, NLayoutHeader } from 'naive-ui'
import AppHeader from '../components/AppHeader/index.vue'
import AppTabs from '../components/AppTabs/index.vue'
import AppTopMenu from '../components/AppTopMenu/index.vue'
import { useRouteStore } from '@/stores/modules/route'
import { settings } from '@/config/settings'

const routeStore = useRouteStore()
</script>

<template>
  <NLayout class="top-layout">
    <NLayoutHeader bordered>
      <AppHeader :show-sidebar-toggle="false">
        <template #center>
          <div class="top-layout__menu">
            <AppTopMenu :items="routeStore.menuItems" />
          </div>
        </template>
      </AppHeader>
    </NLayoutHeader>
    <AppTabs v-if="settings.enableTabs" />
    <NLayoutContent class="top-layout__content" :native-scrollbar="false">
      <RouterView v-slot="{ Component }">
        <component :is="Component" />
      </RouterView>
    </NLayoutContent>
  </NLayout>
</template>

<style scoped>
.top-layout {
  height: 100vh;
}

.top-layout__menu {
  flex: 1;
  min-width: 0;
}

.top-layout__content {
  padding: 16px;
  background: var(--w-color-bg);
  min-height: calc(100vh - var(--w-header-height));
}
</style>
