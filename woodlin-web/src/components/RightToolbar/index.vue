<!--
  @file RightToolbar/index.vue
  @description 列表页右侧工具栏：搜索开关、刷新，以及额外操作插槽
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { NButton, NButtonGroup, NSpace, NTooltip } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'

withDefaults(
  defineProps<{
    /** 搜索区域是否显示 */
    showSearch?: boolean
    /** 是否展示搜索切换按钮 */
    enableSearchToggle?: boolean
  }>(),
  {
    showSearch: true,
    enableSearchToggle: true,
  },
)

const emit = defineEmits<{
  (e: 'refresh'): void
  (e: 'toggle-search'): void
}>()
</script>

<template>
  <NSpace justify="end" align="center" class="right-toolbar">
    <slot />
    <NButtonGroup>
      <NTooltip v-if="enableSearchToggle" trigger="hover">
        <template #trigger>
          <NButton quaternary circle @click="emit('toggle-search')">
            <template #icon>
              <WIcon icon="vicons:antd:SearchOutlined" />
            </template>
          </NButton>
        </template>
        {{ showSearch ? '隐藏搜索' : '显示搜索' }}
      </NTooltip>
      <NTooltip trigger="hover">
        <template #trigger>
          <NButton quaternary circle @click="emit('refresh')">
            <template #icon>
              <WIcon icon="vicons:antd:ReloadOutlined" />
            </template>
          </NButton>
        </template>
        刷新
      </NTooltip>
    </NButtonGroup>
  </NSpace>
</template>

<style scoped>
.right-toolbar {
  width: 100%;
}
</style>
