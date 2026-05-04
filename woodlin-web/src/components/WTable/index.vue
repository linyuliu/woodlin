<!--
  @file WTable/index.vue
  @description 包装 n-data-table 与 n-pagination 的常用组合组件
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts" generic="T extends Record<string, unknown>">
import { NDataTable, NPagination, type DataTableColumns } from 'naive-ui'

const props = defineProps<{
  columns: DataTableColumns<T>
  data: T[]
  loading?: boolean
  page?: number
  pageSize?: number
  total?: number
  rowKey?: (row: T) => string | number
}>()

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:pageSize', size: number): void
  (e: 'change'): void
}>()

/** 页码变更 */
function onPage(page: number): void {
  emit('update:page', page)
  emit('change')
}

/** 每页条数变更 */
function onPageSize(size: number): void {
  emit('update:pageSize', size)
  emit('change')
}
</script>

<template>
  <div class="w-table">
    <NDataTable
      :columns="props.columns"
      :data="props.data"
      :loading="props.loading"
      :row-key="props.rowKey"
      remote
      striped
    />
    <div v-if="props.total && props.total > 0" class="w-table__pagination">
      <NPagination
        :page="props.page ?? 1"
        :page-size="props.pageSize ?? 10"
        :item-count="props.total"
        show-size-picker
        :page-sizes="[10, 20, 50, 100]"
        @update:page="onPage"
        @update:page-size="onPageSize"
      />
    </div>
  </div>
</template>

<style scoped>
.w-table__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
