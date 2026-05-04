<!--
  @file WSearchForm/index.vue
  @description 可折叠的搜索条容器
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NCard, NSpace } from 'naive-ui'

const props = withDefaults(
  defineProps<{
    /** 是否默认展开 */
    defaultExpand?: boolean
    /** 是否展示折叠按钮 */
    collapsible?: boolean
  }>(),
  { defaultExpand: true, collapsible: false },
)

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()

const expanded = ref(props.defaultExpand)

/** 切换展开 */
function toggle(): void {
  expanded.value = !expanded.value
}
</script>

<template>
  <NCard size="small" class="w-search-form">
    <div v-show="expanded" class="w-search-form__body">
      <slot />
      <NSpace class="w-search-form__actions">
        <NButton type="primary" @click="emit('search')">查询</NButton>
        <NButton @click="emit('reset')">重置</NButton>
        <NButton v-if="collapsible" text @click="toggle">
          {{ expanded ? '收起' : '展开' }}
        </NButton>
      </NSpace>
    </div>
  </NCard>
</template>

<style scoped>
.w-search-form__body {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}
.w-search-form__actions {
  margin-left: auto;
}
</style>
