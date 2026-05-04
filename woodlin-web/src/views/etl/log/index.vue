<!--
  @file views/etl/log/index.vue
  @description ETL 执行日志：作业/状态/日期范围筛选 + 可展开详情 + 一键清空
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref, type VNode } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInputNumber,
  NPagination,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import { cleanEtlLogs, pageEtlLogs, type EtlLog, type EtlLogQuery } from '@/api/etl'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<EtlLog[]> = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref<[number, number] | null>(null)

const query = reactive<EtlLogQuery>({
  page: 1,
  size: 10,
  jobId: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined,
})

const statusOptions: SelectOption[] = [
  { label: '成功', value: '0' },
  { label: '失败', value: '1' },
  { label: '运行中', value: '2' },
]

function formatTs(ts: number): string {
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function refresh(): Promise<void> {
  loading.value = true
  if (dateRange.value) {
    query.startTime = formatTs(dateRange.value[0])
    query.endTime = formatTs(dateRange.value[1])
  } else {
    query.startTime = undefined
    query.endTime = undefined
  }
  try {
    const res = await pageEtlLogs(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.page = 1
  void refresh()
}

function handleReset(): void {
  query.jobId = undefined
  query.status = undefined
  dateRange.value = null
  query.page = 1
  void refresh()
}

function handleClean(): void {
  dialog.warning({
    title: '清空日志',
    content: '此操作将清空全部 ETL 日志，确认继续？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await cleanEtlLogs()
      message.success('已清空')
      void refresh()
    },
  })
}

function statusTagType(s: string): 'default' | 'success' | 'error' | 'warning' {
  if (s === '0') return 'success'
  if (s === '1') return 'error'
  return 'warning'
}

function statusLabel(s: string): string {
  if (s === '0') return '成功'
  if (s === '1') return '失败'
  return '运行中'
}

const columns: DataTableColumns<EtlLog> = [
  { type: 'expand', renderExpand: (row) => renderDetail(row) },
  { title: '作业名称', key: 'jobName', width: 180 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: statusTagType(row.status) },
        { default: () => statusLabel(row.status) },
      ),
  },
  { title: '开始时间', key: 'startTime', width: 170 },
  { title: '结束时间', key: 'stopTime', width: 170 },
  { title: '读取行数', key: 'rowsRead', width: 110 },
  { title: '写入行数', key: 'rowsWritten', width: 110 },
  {
    title: '错误信息',
    key: 'errorMsg',
    ellipsis: { tooltip: true },
  },
]

function renderDetail(row: EtlLog): VNode {
  return h(
    'div',
    { style: 'padding: 8px 12px; white-space: pre-wrap; font-family: ui-monospace, Menlo, Consolas, monospace; font-size: 12px;' },
    row.detail || row.errorMsg || '无详情',
  )
}

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-etl-log">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="作业 ID">
          <n-input-number
            v-model:value="query.jobId"
            placeholder="作业 ID"
            clearable
            :show-button="false"
            style="width: 140px"
          />
        </n-form-item>
        <n-form-item label="状态">
          <n-select
            v-model:value="query.status"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="min-width: 120px"
          />
        </n-form-item>
        <n-form-item label="时间范围">
          <n-date-picker v-model:value="dateRange" type="datetimerange" clearable />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch">查询</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card size="small">
      <div class="toolbar">
        <n-button type="error" @click="handleClean">清空日志</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: EtlLog) => row.id as number"
        :scroll-x="1300"
        striped
      />
      <div class="pagination">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.size"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="refresh"
          @update:page-size="refresh"
        />
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.page-etl-log {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  margin-bottom: 12px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
