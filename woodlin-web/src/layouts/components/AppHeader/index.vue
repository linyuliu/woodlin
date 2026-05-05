<!--
  @file layouts/components/AppHeader/index.vue
  @description 顶部导航栏：折叠按钮 + 面包屑 + 工具区
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed } from 'vue'
import { NButton } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import AppBreadcrumb from '../AppBreadcrumb/index.vue'
import AppNotice from '../AppNotice/index.vue'
import LayoutSwitch from '../LayoutSwitch/index.vue'
import ThemeSwitch from '../ThemeSwitch/index.vue'
import LocaleSwitch from '../LocaleSwitch/index.vue'
import TenantSwitch from '../TenantSwitch/index.vue'
import UserDropdown from '../UserDropdown/index.vue'
import { useAppStore } from '@/stores/modules/app'

const props = withDefaults(defineProps<{
  showSidebarToggle?: boolean
  showBreadcrumb?: boolean
}>(), {
  showSidebarToggle: true,
  showBreadcrumb: true,
})

const appStore = useAppStore()

const collapseIcon = computed(() =>
  appStore.sidebarCollapsed
    ? 'vicons:antd:MenuUnfoldOutlined'
    : 'vicons:antd:MenuFoldOutlined',
)

/** 切换侧边栏 */
function toggleSidebar(): void {
  appStore.toggleSidebar()
}
</script>

<template>
  <div class="app-header">
    <div class="app-header__left">
      <NButton v-if="props.showSidebarToggle" quaternary circle @click="toggleSidebar">
        <template #icon>
          <WIcon :icon="collapseIcon" />
        </template>
      </NButton>
      <AppBreadcrumb v-if="props.showBreadcrumb" />
      <slot name="center" />
    </div>
    <div class="app-header__right">
      <TenantSwitch />
      <AppNotice />
      <LayoutSwitch />
      <ThemeSwitch />
      <LocaleSwitch />
      <UserDropdown />
    </div>
  </div>
</template>

<style scoped>
.app-header {
  height: var(--w-header-height);
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--w-color-border);
  background: var(--n-color, #fff);
}
.app-header__left,
.app-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header__left {
  min-width: 0;
  flex: 1;
}

.app-header__right {
  flex-shrink: 0;
}
</style>
