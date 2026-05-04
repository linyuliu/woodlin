<!--
  @file views/monitor/loginLog/index.vue
  @description 登录日志：分页/筛选 + 单条删除 + 一键清空
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
  cleanLoginLog,
  deleteLoginLog,
  pageLoginLog,
  type LoginLog,
  type LoginLogQuery,
} from '@/api/monitor'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<LoginLog[]> = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref<[number, number] | null>(null)

const query = reactive<LoginLogQuery>({
  page: 1,
  size: 10,
  username: '',
  ipaddr: '',
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
    const res = await pageLoginLog(query)
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
  query.username = ''
  query.ipaddr = ''
  query.status = undefined
  dateRange.value = null
  query.page = 1
  void refresh()
}

function handleDelete(row: LoginLog): void {
  dialog.warning({
    title: '提示',
    content: '确认删除该条登录日志？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteLoginLog(row.id)
      message.success('删除成功')
      void refresh()
    },
  })
}

function handleClean(): void {
  dialog.warning({
    title: '清空日志',
    content: '此操作将清空全部登录日志，确认继续？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await cleanLoginLog()
      message.success('已清空')
      void refresh()
    },
  })
}

const columns: DataTableColumns<LoginLog> = [
  { title: '用户名', key: 'username', width: 140 },
  { title: 'IP 地址', key: 'ipaddr', width: 140 },
  { title: '登录地点', key: 'loginLocation', width: 160 },
  { title: '浏览器', key: 'browser', width: 140 },
  { title: '操作系统', key: 'os', width: 140 },
  { title: '消息', key: 'msg', width: 200, ellipsis: { tooltip: true } },
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
  { title: '登录时间', key: 'loginTime', width: 170 },
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
  <div class="page-login-log">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="用户名">
          <n-input v-model:value="query.username" placeholder="用户名" clearable />
        </n-form-item>
        <n-form-item label="IP 地址">
          <n-input v-model:value="query.ipaddr" placeholder="IP 地址" clearable />
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
        :row-key="(row: LoginLog) => row.id"
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
.page-login-log {
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
