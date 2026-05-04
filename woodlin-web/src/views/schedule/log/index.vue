<!--
  @file views/schedule/log/index.vue
  @description 调度日志：分页/筛选/详情查看 + 单条删除 + 一键清空
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInput,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import {
  cleanLogs,
  deleteLog,
  pageLogs,
  type ScheduleLog,
  type ScheduleLogQuery,
} from '@/api/schedule'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<ScheduleLog[]> = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref<[number, number] | null>(null)

const query = reactive<ScheduleLogQuery>({
  page: 1,
  size: 10,
  jobName: '',
  jobGroup: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined,
})

const jobGroupOptions: SelectOption[] = [
  { label: 'DEFAULT', value: 'DEFAULT' },
  { label: 'SYSTEM', value: 'SYSTEM' },
]

const statusOptions: SelectOption[] = [
  { label: '成功', value: '0' },
  { label: '失败', value: '1' },
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
    const res = await pageLogs(query)
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
  query.jobName = ''
  query.jobGroup = undefined
  query.status = undefined
  dateRange.value = null
  query.page = 1
  void refresh()
}

function handleDelete(row: ScheduleLog): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: '确认删除该条日志？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteLog(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

function handleClean(): void {
  dialog.warning({
    title: '清空日志',
    content: '此操作将清空全部调度日志，确认继续？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await cleanLogs()
      message.success('已清空')
      void refresh()
    },
  })
}

const columns: DataTableColumns<ScheduleLog> = [
  { title: '任务名称', key: 'jobName', width: 160 },
  { title: '分组', key: 'jobGroup', width: 100 },
  { title: '调用目标', key: 'invokeTarget', width: 220, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '0' ? 'success' : 'error' },
        { default: () => (row.status === '0' ? '成功' : '失败') },
      ),
  },
  {
    title: '执行信息',
    key: 'message',
    width: 260,
    ellipsis: { tooltip: true },
  },
  { title: '开始时间', key: 'startTime', width: 170 },
  { title: '结束时间', key: 'stopTime', width: 170 },
  { title: '耗时(ms)', key: 'elapsedTime', width: 100 },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
    render: (row) =>
      h(
        NPopconfirm,
        { onPositiveClick: () => handleDelete(row) },
        {
          default: () => '确认删除？',
          trigger: () =>
            h(
              NButton,
              { size: 'small', text: true, type: 'error' },
              { default: () => '删除' },
            ),
        },
      ),
  },
]

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-log">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="任务名称">
          <n-input v-model:value="query.jobName" placeholder="任务名称" clearable />
        </n-form-item>
        <n-form-item label="分组">
          <n-select
            v-model:value="query.jobGroup"
            :options="jobGroupOptions"
            placeholder="分组"
            clearable
            style="min-width: 140px"
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
        :row-key="(row: ScheduleLog) => row.id as number"
        :scroll-x="1500"
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
.page-log {
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
