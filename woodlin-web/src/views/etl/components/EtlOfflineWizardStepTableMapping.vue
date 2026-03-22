<script setup lang="ts">
import { computed } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NIcon,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NText,
  type DataTableColumns
} from 'naive-ui'
import { RefreshOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import type { EtlOfflineTableMapping } from '@/api/etl'
import type { EtlSelectableTable, EtlSelectOption } from '@/views/etl/types'

interface Props {
  sourceDatasource: string
  targetDatasource: string
  sourceTables: EtlSelectableTable[]
  selectedTableKeys: string[]
  sourceTableKeyword: string
  loadingSourceTables: boolean
  tableMappings: EtlOfflineTableMapping[]
  columnOptionsMap: Record<string, EtlSelectOption[]>
  isEditMode: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:sourceTableKeyword': [value: string]
  'search-source-tables': []
  'selection-change': [keys: string[], rows: EtlSelectableTable[]]
  'remove-mapping': [mappingKey: string]
}>()

const columns = computed<DataTableColumns<EtlSelectableTable>>(() => [
  {
    type: 'selection',
    disabled: (row) =>
      props.isEditMode && props.selectedTableKeys.length > 0 && !props.selectedTableKeys.includes(row.mappingKey)
  },
  {
    title: '源表',
    key: 'tableName',
    minWidth: 180,
    render: (row) => `${row.schemaName ? `${row.schemaName}.` : ''}${row.tableName}`
  },
  {
    title: '注释',
    key: 'comment',
    minWidth: 200,
    render: (row) => row.comment || '—'
  }
])

/**
 * 构建映射键
 */
function buildMappingKey(mapping: EtlOfflineTableMapping) {
  return `${mapping.sourceSchema || '__default__'}::${mapping.sourceTable}`
}

/**
 * 转发表格勾选结果，统一由父级负责维护映射状态。
 */
function handleCheckedRowKeys(keys: Array<string | number>, rows: unknown[]) {
  emit('selection-change', keys.map((key) => String(key)), rows as EtlSelectableTable[])
}
</script>

<template>
  <n-grid :cols="24" :x-gap="16">
    <n-grid-item :span="9">
      <n-card :bordered="false" size="small" title="源表选择">
        <n-space :size="12" vertical>
          <n-text depth="3">
            {{ sourceDatasource ? `当前源实例：${sourceDatasource}` : '请先选择源数据源实例' }}
          </n-text>
          <n-space>
            <n-input
              :value="sourceTableKeyword"
              clearable
              placeholder="按表名或注释筛选"
              @keyup.enter="emit('search-source-tables')"
              @update:value="emit('update:sourceTableKeyword', $event)"
            >
              <template #prefix>
                <n-icon><search-outline /></n-icon>
              </template>
            </n-input>
            <n-button :disabled="!sourceDatasource" @click="emit('search-source-tables')">
              <template #icon>
                <n-icon><refresh-outline /></n-icon>
              </template>
              刷新
            </n-button>
          </n-space>
          <n-data-table
            :bordered="false"
            :checked-row-keys="selectedTableKeys"
            :columns="columns"
            :data="sourceTables"
            :loading="loadingSourceTables"
          :row-key="row => row.mappingKey"
          :single-line="false"
          max-height="520"
          size="small"
          @update:checked-row-keys="handleCheckedRowKeys"
        />
          <n-text v-if="isEditMode" depth="3">编辑模式仅允许维护一条单表任务。</n-text>
        </n-space>
      </n-card>
    </n-grid-item>

    <n-grid-item :span="15">
      <n-card :bordered="false" size="small" title="表映射配置">
        <n-empty v-if="tableMappings.length === 0" description="请先在左侧选择至少一张源表" />
        <n-space v-else :size="12" vertical>
          <n-card
            v-for="mapping in tableMappings"
            :key="buildMappingKey(mapping)"
            :bordered="false"
            class="mapping-card"
            size="small"
          >
            <n-space :size="12" justify="space-between">
              <n-space align="center">
                <n-tag type="info">源表</n-tag>
                <n-text>{{ mapping.sourceSchema ? `${mapping.sourceSchema}.` : '' }}{{ mapping.sourceTable }}</n-text>
                <n-tag type="success">{{ mapping.syncMode === 'INCREMENTAL' ? '增量同步' : '全量同步' }}</n-tag>
              </n-space>
              <n-button quaternary type="error" @click="emit('remove-mapping', buildMappingKey(mapping))">
                <template #icon>
                  <n-icon><trash-outline /></n-icon>
                </template>
                移除
              </n-button>
            </n-space>

            <n-form label-placement="left" label-width="110" style="margin-top: 12px">
              <n-form-item label="目标实例">
                <n-text>{{ targetDatasource || '请先选择目标数据源实例' }}</n-text>
              </n-form-item>
              <n-form-item label="目标 Schema">
                <n-input v-model:value="mapping.targetSchema" placeholder="可选，PostgreSQL 等场景可填写" />
              </n-form-item>
              <n-form-item label="目标表名">
                <n-input v-model:value="mapping.targetTable" placeholder="默认与源表同名，可按需改名" />
              </n-form-item>
              <n-form-item v-if="mapping.syncMode === 'INCREMENTAL'" label="增量字段">
                <n-select
                  v-model:value="mapping.incrementalColumn"
                  :options="columnOptionsMap[buildMappingKey(mapping)] || []"
                  clearable
                  filterable
                  placeholder="请选择增量字段"
                />
              </n-form-item>
              <n-form-item label="过滤条件">
                <n-input
                  v-model:value="mapping.filterCondition"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  placeholder="可选，例如：update_time >= NOW() - INTERVAL 1 DAY"
                  type="textarea"
                />
              </n-form-item>
              <n-form-item label="备注">
                <n-input
                  v-model:value="mapping.remark"
                  :autosize="{ minRows: 2, maxRows: 3 }"
                  placeholder="记录该表的特殊同步说明"
                  type="textarea"
                />
              </n-form-item>
            </n-form>
          </n-card>
        </n-space>
      </n-card>
    </n-grid-item>
  </n-grid>
</template>

<style scoped>
.mapping-card {
  background: color-mix(in srgb, var(--primary-color) 4%, var(--card-color));
}
</style>
