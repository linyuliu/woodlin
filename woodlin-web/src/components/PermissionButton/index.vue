<!--
  @file PermissionButton/index.vue
  @description 带权限校验的按钮（缺权限不渲染）
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { computed } from 'vue'
import { NButton } from 'naive-ui'
import { hasPermission } from '@/utils/permission'

const props = defineProps<{
  /** 权限标识，未提供时直接渲染 */
  permission?: string | string[]
  /** Naive Button 类型 */
  type?: 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'
  /** 按钮尺寸 */
  size?: 'tiny' | 'small' | 'medium' | 'large'
}>()

const emit = defineEmits<{ (e: 'click', evt: MouseEvent): void }>()

const allow = computed(() => (props.permission ? hasPermission(props.permission) : true))

/** 点击转发 */
function onClick(evt: MouseEvent): void {
  emit('click', evt)
}
</script>

<template>
  <NButton v-if="allow" :type="props.type ?? 'default'" :size="props.size" @click="onClick">
    <slot />
  </NButton>
</template>
