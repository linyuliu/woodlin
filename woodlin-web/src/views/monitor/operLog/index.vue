<!--
  @file views/monitor/operLog/index.vue
  @description 操作日志：分页/筛选 + 可展开详情 + 单条删除 + 一键清空
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
  NDescriptions,
  NDescriptionsItem,
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
  cleanOperLog,
  deleteOperLog,
  pageOperLog,
  type OperLog,
  type OperLogQuery,
} from '@/api/monitor'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OperLog[]> = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref<[number, number] | null>(null)

const query = reactive<OperLogQuery>({
  page: 1,
  size: 10,
  title: '',
  operName: '',
  status: undefined,
  startTime: undefined,
  endTime: undefined,
})

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
    const res = await pageOperLog(query)
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
  query.title = ''
  query.operName = ''
  query.status = undefined
  dateRange.value = null
  query.page = 1
  void refresh()
}

function handleDelete(row: OperLog): void {
  dialog.warning({
    title: '提示',
    content: '确认删除该条操作日志？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteOperLog(row.id)
      message.success('删除成功')
      void refresh()
    },
  })
}

function handleClean(): void {
  dialog.warning({
    title: '清空日志',
    content: '此操作将清空全部操作日志，确认继续？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await cleanOperLog()
      message.success('已清空')
      void refresh()
    },
  })
}

function renderExpand(row: OperLog) {
  return h(
    NDescriptions,
    { labelPlacement: 'left', column: 1, size: 'small', bordered: true },
    {
      default: () => [
        h(NDescriptionsItem, { label: '请求参数' }, { default: () => row.operParam || '-' }),
        h(NDescriptionsItem, { label: '返回结果' }, { default: () => row.jsonResult || '-' }),
        h(NDescriptionsItem, { label: '错误信息' }, { default: () => row.errorMsg || '-' }),
      ],
    },
  )
}

const columns: DataTableColumns<OperLog> = [
  { type: 'expand', renderExpand },
  { title: '模块标题', key: 'title', width: 140 },
  { title: '操作人员', key: 'operName', width: 120 },
  { title: '操作 IP', key: 'operIp', width: 140 },
  { title: '请求 URL', key: 'operUrl', width: 220, ellipsis: { tooltip: true } },
  { title: '方法', key: 'method', width: 220, ellipsis: { tooltip: true } },
  { title: '请求方式', key: 'requestMethod', width: 100 },
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
  { title: '操作时间', key: 'operTime', width: 170 },
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
  <div class="page-oper-log">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="模块标题">
          <n-input v-model:value="query.title" placeholder="模块标题" clearable />
        </n-form-item>
        <n-form-item label="操作人员">
          <n-input v-model:value="query.operName" placeholder="操作人员" clearable />
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
        :row-key="(row: OperLog) => row.id"
        :scroll-x="1700"
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
.page-oper-log {
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
