<!--
  @file layouts/components/LayoutSwitch/index.vue
  @description 布局模式切换器：支持 default / mix / top 三种页面骨架快速切换
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, h } from 'vue'
import { NButton, NDropdown, type DropdownOption } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { useAppStore, type LayoutMode } from '@/stores/modules/app'

const appStore = useAppStore()

const options = computed<DropdownOption[]>(() => [
  { key: 'default', label: '侧栏布局' },
  { key: 'mix', label: '混合布局' },
  { key: 'top', label: '顶部布局' },
])

function handleSelect(key: string): void {
  appStore.setLayoutMode(key as LayoutMode)
}
</script>

<template>
  <NDropdown :options="options" trigger="click" @select="handleSelect">
    <NButton quaternary circle>
      <template #icon>
        <WIcon icon="vicons:antd:LayoutOutlined" />
      </template>
    </NButton>
  </NDropdown>
</template>
