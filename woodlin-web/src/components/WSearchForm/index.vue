<!--
  @file WSearchForm/index.vue
  @description 可折叠的搜索条容器
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NButton, NCard, NSpace } from 'naive-ui'

const props = withDefaults(
  defineProps<{
    /** 是否默认展开 */
    defaultExpand?: boolean
    /** 外部控制展开状态 */
    show?: boolean
    /** 是否展示折叠按钮 */
    collapsible?: boolean
    /** 是否显示默认查询/重置按钮 */
    showActions?: boolean
  }>(),
  { defaultExpand: true, show: undefined, collapsible: false, showActions: true },
)

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'search'): void
  (e: 'reset'): void
}>()

const expanded = ref(props.defaultExpand)

watch(
  () => props.show,
  (value) => {
    if (typeof value === 'boolean') {
      expanded.value = value
    }
  },
  { immediate: true },
)

const visible = computed(() => expanded.value)

/** 切换展开 */
function toggle(): void {
  expanded.value = !expanded.value
  emit('update:show', expanded.value)
}
</script>

<template>
  <NCard size="small" class="w-search-form">
    <div v-show="visible" class="w-search-form__body">
      <div class="w-search-form__fields">
        <slot />
      </div>
      <NSpace v-if="showActions" class="w-search-form__actions">
        <NButton type="primary" @click="emit('search')">查询</NButton>
        <NButton @click="emit('reset')">重置</NButton>
        <NButton v-if="collapsible" text @click="toggle">
          {{ visible ? '收起' : '展开' }}
        </NButton>
      </NSpace>
      <slot name="actions" />
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
.w-search-form__fields {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 12px;
  min-width: 0;
}
.w-search-form__actions {
  margin-left: auto;
}

@media (max-width: 768px) {
  .w-search-form__body,
  .w-search-form__fields,
  .w-search-form__actions {
    width: 100%;
  }

  .w-search-form__actions {
    margin-left: 0;
  }
}
</style>
