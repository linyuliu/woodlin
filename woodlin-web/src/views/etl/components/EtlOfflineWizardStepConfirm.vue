<script setup lang="ts">
import { computed, h } from 'vue'
import {
  NAlert,
  NCard,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NSpace,
  NTag,
  NText,
  type DataTableColumns
} from 'naive-ui'
import type { EtlOfflineTableMapping, EtlOfflineValidationResult } from '@/api/etl'

interface Props {
  jobName: string
  jobDescription: string
  sourceDatasource: string
  targetDatasource: string
  syncMode: string
  tableMappings: EtlOfflineTableMapping[]
  validationResult: EtlOfflineValidationResult | null
  validating: boolean
  isEditMode: boolean
}

const props = defineProps<Props>()

const columns = computed<DataTableColumns<EtlOfflineTableMapping>>(() => [
  {
    title: '源表',
    key: 'sourceTable',
    minWidth: 180,
    render: (row) => `${row.sourceSchema ? `${row.sourceSchema}.` : ''}${row.sourceTable}`
  },
  {
    title: '目标表',
    key: 'targetTable',
    minWidth: 180,
    render: (row) => `${row.targetSchema ? `${row.targetSchema}.` : ''}${row.targetTable}`
  },
  {
    title: '同步模式',
    key: 'syncMode',
    width: 110,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.syncMode === 'INCREMENTAL' ? 'info' : 'default' },
        { default: () => (row.syncMode === 'INCREMENTAL' ? '增量' : '全量') }
      )
  },
  {
    title: '启用字段',
    key: 'fieldRules',
    width: 120,
    render: (row) => row.fieldRules.filter((rule) => rule.enabled).length
  },
  {
    title: '增量字段',
    key: 'incrementalColumn',
    minWidth: 140,
    render: (row) => row.incrementalColumn || '—'
  }
])
</script>

<template>
  <n-space :size="16" vertical>
    <n-card :bordered="false" size="small" title="任务摘要">
      <n-descriptions :column="2" bordered label-placement="left" size="small">
        <n-descriptions-item label="任务名称">{{ jobName }}</n-descriptions-item>
        <n-descriptions-item label="操作模式">{{ isEditMode ? '编辑任务' : '批量创建任务' }}</n-descriptions-item>
        <n-descriptions-item label="源数据源">{{ sourceDatasource }}</n-descriptions-item>
        <n-descriptions-item label="目标数据源">{{ targetDatasource }}</n-descriptions-item>
        <n-descriptions-item label="默认同步模式">{{ syncMode === 'INCREMENTAL' ? '增量' : '全量' }}</n-descriptions-item>
        <n-descriptions-item label="将提交任务数">{{ tableMappings.length }}</n-descriptions-item>
        <n-descriptions-item label="任务描述" :span="2">{{ jobDescription || '—' }}</n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card :bordered="false" size="small" title="任务明细">
      <n-empty v-if="tableMappings.length === 0" description="暂无待提交的表映射" />
      <n-data-table
        v-else
        :bordered="false"
        :columns="columns"
        :data="tableMappings"
        :loading="validating"
        :single-line="false"
        size="small"
      />
    </n-card>

    <n-card :bordered="false" size="small" title="预校验结果">
      <n-empty v-if="!validationResult" description="进入本步骤时会自动执行一次预校验" />
      <n-space v-else :size="12" vertical>
        <n-alert :type="validationResult.valid ? 'success' : 'error'" :title="validationResult.valid ? '预校验通过' : '预校验未通过'">
          <template #default>
            <div>全局错误 {{ validationResult.errors.length }} 条，全局告警 {{ validationResult.warnings.length }} 条。</div>
          </template>
        </n-alert>

        <n-alert v-for="error in validationResult.errors" :key="`error-${error}`" title="阻断错误" type="error">
          {{ error }}
        </n-alert>

        <n-alert v-for="warning in validationResult.warnings" :key="`warning-${warning}`" title="提示信息" type="warning">
          {{ warning }}
        </n-alert>

        <n-card
          v-for="table in validationResult.tables"
          :key="`${table.sourceSchema || '__default__'}::${table.sourceTable}`"
          :bordered="false"
          class="validation-card"
          size="small"
        >
          <n-space :size="8" vertical>
            <n-space align="center">
              <n-tag :type="table.valid ? 'success' : 'error'">{{ table.valid ? '通过' : '失败' }}</n-tag>
              <n-text>{{ table.sourceSchema ? `${table.sourceSchema}.` : '' }}{{ table.sourceTable }} → {{ table.targetSchema ? `${table.targetSchema}.` : '' }}{{ table.targetTable }}</n-text>
            </n-space>
            <n-text depth="3">推荐增量字段：{{ table.suggestedIncrementalColumns.join('、') || '无' }}</n-text>
            <n-alert v-for="error in table.errors" :key="`${table.sourceTable}-error-${error}`" title="表级错误" type="error">
              {{ error }}
            </n-alert>
            <n-alert v-for="warning in table.warnings" :key="`${table.sourceTable}-warning-${warning}`" title="表级告警" type="warning">
              {{ warning }}
            </n-alert>
          </n-space>
        </n-card>
      </n-space>
    </n-card>
  </n-space>
</template>

<style scoped>
.validation-card {
  background: color-mix(in srgb, var(--primary-color) 4%, var(--card-color));
}
</style>
