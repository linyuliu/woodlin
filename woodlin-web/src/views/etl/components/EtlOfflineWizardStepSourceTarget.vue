<script setup lang="ts">
import {
  NCard,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NText
} from 'naive-ui'
import type { EtlDatasourceTypeGroup } from '@/api/etl'
import type { EtlSelectOption } from '@/views/etl/types'

interface Props {
  datasourceTypeGroups: EtlDatasourceTypeGroup[]
  jobName: string
  sourceDatasourceType: string
  sourceDatasource: string
  targetDatasourceType: string
  targetDatasource: string
  sourceDatasourceOptions: EtlSelectOption[]
  targetDatasourceOptions: EtlSelectOption[]
  isRelationalGroup: (groupCode?: string) => boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:jobName': [value: string]
  'update:sourceDatasourceType': [value: string]
  'update:sourceDatasource': [value: string]
  'update:targetDatasourceType': [value: string]
  'update:targetDatasource': [value: string]
}>()

/**
 * 判断类型卡片是否可选
 */
function isOptionDisabled(groupCode: string, available: boolean) {
  return !props.isRelationalGroup(groupCode) || !available
}
</script>

<template>
  <n-space :size="16" vertical>
    <n-card :bordered="false" size="small" title="基础信息">
      <n-form label-placement="left" label-width="90">
        <n-form-item label="任务名称">
          <n-input
            :value="jobName"
            placeholder="请输入离线同步任务名称"
            @update:value="emit('update:jobName', $event)"
          />
        </n-form-item>
      </n-form>
    </n-card>

    <n-grid :cols="2" :x-gap="16" item-responsive responsive="screen">
      <n-grid-item>
        <n-card :bordered="false" class="side-card" size="small" title="源端设置">
          <n-space :size="14" vertical>
            <div class="section-title">
              <span>数据源类型</span>
              <n-tag size="small" type="info">V1 仅开放数据库</n-tag>
            </div>
            <div v-for="group in datasourceTypeGroups" :key="group.groupCode" class="type-group">
              <div class="type-group-head">
                <n-text depth="3">{{ group.groupName }}</n-text>
                <n-tag v-if="!isRelationalGroup(group.groupCode)" size="small">暂未开放</n-tag>
              </div>
              <div class="type-grid">
                <button
                  v-for="option in group.options"
                  :key="option.datasourceType"
                  :class="{
                    active: sourceDatasourceType === option.datasourceType,
                    disabled: isOptionDisabled(group.groupCode, option.available)
                  }"
                  :disabled="isOptionDisabled(group.groupCode, option.available)"
                  class="type-item"
                  type="button"
                  @click="emit('update:sourceDatasourceType', option.datasourceType)"
                >
                  <span>{{ option.displayName }}</span>
                  <small>{{ option.enabledCount }}/{{ option.totalCount }}</small>
                </button>
              </div>
            </div>

            <n-form label-placement="left" label-width="90">
              <n-form-item label="源实例">
                <n-select
                  :options="sourceDatasourceOptions"
                  :value="sourceDatasource"
                  clearable
                  filterable
                  placeholder="请选择源数据源实例"
                  @update:value="emit('update:sourceDatasource', $event || '')"
                />
              </n-form-item>
            </n-form>
          </n-space>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card :bordered="false" class="side-card" size="small" title="目标端设置">
          <n-space :size="14" vertical>
            <div class="section-title">
              <span>数据源类型</span>
              <n-tag size="small" type="success">按目标能力过滤</n-tag>
            </div>
            <div v-for="group in datasourceTypeGroups" :key="`${group.groupCode}-target`" class="type-group">
              <div class="type-group-head">
                <n-text depth="3">{{ group.groupName }}</n-text>
                <n-tag v-if="!isRelationalGroup(group.groupCode)" size="small">暂未开放</n-tag>
              </div>
              <div class="type-grid">
                <button
                  v-for="option in group.options"
                  :key="option.datasourceType"
                  :class="{
                    active: targetDatasourceType === option.datasourceType,
                    disabled: isOptionDisabled(group.groupCode, option.available)
                  }"
                  :disabled="isOptionDisabled(group.groupCode, option.available)"
                  class="type-item"
                  type="button"
                  @click="emit('update:targetDatasourceType', option.datasourceType)"
                >
                  <span>{{ option.displayName }}</span>
                  <small>{{ option.enabledCount }}/{{ option.totalCount }}</small>
                </button>
              </div>
            </div>

            <n-form label-placement="left" label-width="90">
              <n-form-item label="目标实例">
                <n-select
                  :options="targetDatasourceOptions"
                  :value="targetDatasource"
                  clearable
                  filterable
                  placeholder="请选择目标数据源实例"
                  @update:value="emit('update:targetDatasource', $event || '')"
                />
              </n-form-item>
            </n-form>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>
  </n-space>
</template>

<style scoped>
.side-card {
  min-height: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: 600;
}

.type-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.type-grid {
  display: grid;
  gap: 8px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.type-item {
  display: flex;
  min-height: 60px;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--card-color);
  color: var(--text-color-primary);
  cursor: pointer;
  transition: all 0.18s ease;
}

.type-item small {
  color: var(--text-color-secondary);
}

.type-item:hover:not(.disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.type-item.active {
  border-color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color) 10%, #ffffff);
}

.type-item.disabled {
  cursor: not-allowed;
  color: var(--text-color-disabled);
  background: var(--action-color);
}

@media (max-width: 1200px) {
  .type-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
