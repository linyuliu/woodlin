<!--
  @file layouts/components/AppLogo/index.vue
  @description 应用 Logo 与标题，折叠状态下仅展示图标
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import logoUrl from '@/assets/logo.svg'
import { settings } from '@/config/settings'

withDefaults(
  defineProps<{
    /** 是否折叠（仅显示 Logo） */
    collapsed?: boolean
  }>(),
  { collapsed: false },
)
</script>

<template>
  <div class="app-logo" :class="{ 'is-collapsed': collapsed }">
    <img :src="logoUrl" class="app-logo__img" alt="logo" />
    <span v-if="!collapsed" class="app-logo__title">{{ settings.title }}</span>
  </div>
</template>

<style scoped>
.app-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  height: var(--w-header-height);
  padding: 0 16px;
  overflow: hidden;
  white-space: nowrap;
}
.app-logo__img {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}
.app-logo__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--w-color-text);
}
.app-logo.is-collapsed {
  justify-content: center;
  padding: 0;
}
</style>
