<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NPopconfirm,
  NSelect,
  NSpace,
  NStatistic,
  NTag,
  NText,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  FlashOutline,
  PauseCircleOutline,
  PlayCircleOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  deleteEtlOfflineJob,
  disableEtlOfflineJob,
  enableEtlOfflineJob,
  executeEtlOfflineJob,
  getEtlOfflineDatasourceOptions,
  getEtlOfflineJobPage,
  type EtlOfflineDatasourceOption,
  type EtlOfflineJobPageParams,
  type EtlOfflineJobSummary
} from '@/api/etl'
import { PERMISSIONS } from '@/constants/permission-keys'
import { useUserStore } from '@/stores'

interface SearchFormState {
  keyword: string
  status: string | null
  syncMode: string | null
  sourceDatasource: string | null
  targetDatasource: string | null
}

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const datasourceOptions = ref<EtlOfflineDatasourceOption[]>([])
const searchForm = ref<SearchFormState>({
  keyword: '',
  status: null,
  syncMode: null,
  sourceDatasource: null,
  targetDatasource: null
})

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<EtlOfflineJobSummary[]>([])

const canList = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_LIST))
const canCreate = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_CREATE))
const canEdit = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_EDIT))
const canDelete = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_DELETE))
const canExecute = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_EXECUTE))
const canEnable = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_ENABLE))
const canDisable = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_DISABLE))
const canViewLogs = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_LOG_LIST))

const pagination = computed(() => ({
  page: pageNum.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pageNum.value = page
    void loadData()
  },
  onUpdatePageSize: (size: number) => {
    pageSize.value = size
    pageNum.value = 1
    void loadData()
  }
}))

const totalCount = computed(() => total.value)
const enabledCount = computed(() => tableData.value.filter((item) => item.status === '1').length)
const incrementalCount = computed(() => tableData.value.filter((item) => item.syncMode === 'INCREMENTAL').length)

const datasourceSelectOptions = computed(() =>
  datasourceOptions.value.map((item) => ({
    label: `${item.datasourceName} (${item.datasourceCode})`,
    value: item.datasourceCode
  }))
)

/**
 * 构建分页查询参数
 */
function buildPageParams(): EtlOfflineJobPageParams {
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchForm.value.keyword.trim() || undefined,
    status: searchForm.value.status || undefined,
    syncMode: (searchForm.value.syncMode || undefined) as EtlOfflineJobPageParams['syncMode'],
    sourceDatasource: searchForm.value.sourceDatasource || undefined,
    targetDatasource: searchForm.value.targetDatasource || undefined
  }
}

/**
 * 格式化时间
 */
function formatTime(value?: string) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

/**
 * 获取任务状态标签类型
 */
function getJobTagType(status?: string) {
  return status === '1' ? 'success' : 'default'
}

/**
 * 获取执行状态标签类型
 */
function getExecutionTagType(status?: string) {
  if (status === 'SUCCESS') {
    return 'success'
  }
  if (status === 'RUNNING') {
    return 'info'
  }
  if (status === 'FAILED') {
    return 'error'
  }
  return 'default'
}

/**
 * 加载分页数据
 */
async function loadData() {
  if (!canList.value) {
    tableData.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const page = await getEtlOfflineJobPage(buildPageParams())
    tableData.value = page.data || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载筛选数据源
 */
async function loadDatasourceOptions() {
  datasourceOptions.value = await getEtlOfflineDatasourceOptions({ enabledOnly: false })
}

/**
 * 重置查询
 */
async function handleReset() {
  searchForm.value = {
    keyword: '',
    status: null,
    syncMode: null,
    sourceDatasource: null,
    targetDatasource: null
  }
  pageNum.value = 1
  await loadData()
}

/**
 * 跳转到创建页
 */
function handleCreate() {
  router.push('/etl/offline/create')
}

/**
 * 跳转到执行历史页
 */
function handleViewLogs() {
  router.push('/etl/offline/logs')
}

/**
 * 跳转到编辑页
 */
function handleEdit(row: EtlOfflineJobSummary) {
  if (!row.jobId) {
    return
  }
  router.push(`/etl/offline/${row.jobId}/edit`)
}

/**
 * 执行任务
 */
async function handleExecute(row: EtlOfflineJobSummary) {
  await executeEtlOfflineJob(row.jobId)
  message.success(`已触发任务：${row.jobName}`)
  await loadData()
}

/**
 * 启停任务
 */
async function handleToggleStatus(row: EtlOfflineJobSummary) {
  if (row.status === '1') {
    await disableEtlOfflineJob(row.jobId)
    message.success(`已禁用任务：${row.jobName}`)
  } else {
    await enableEtlOfflineJob(row.jobId)
    message.success(`已启用任务：${row.jobName}`)
  }
  await loadData()
}

/**
 * 删除任务
 */
async function handleDelete(row: EtlOfflineJobSummary) {
  await deleteEtlOfflineJob(row.jobId)
  message.success(`已删除任务：${row.jobName}`)
  await loadData()
}

/**
 * 渲染操作区
 */
function renderActionCell(row: EtlOfflineJobSummary) {
  const actions: ReturnType<typeof h>[] = []

  if (canEdit.value) {
    actions.push(
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => handleEdit(row)
        },
        {
          default: () => '编辑',
          icon: () => h(NIcon, null, { default: () => h(CreateOutline) })
        }
      )
    )
  }

  if (canExecute.value) {
    actions.push(
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => handleExecute(row)
        },
        {
          default: () => '执行',
          icon: () => h(NIcon, null, { default: () => h(FlashOutline) })
        }
      )
    )
  }

  const canToggle = row.status === '1' ? canDisable.value : canEnable.value
  if (canToggle) {
    actions.push(
      h(
        NButton,
        {
          text: true,
          type: row.status === '1' ? 'warning' : 'success',
          size: 'small',
          onClick: () => handleToggleStatus(row)
        },
        {
          default: () => (row.status === '1' ? '禁用' : '启用'),
          icon: () =>
            h(NIcon, null, {
              default: () => h(row.status === '1' ? PauseCircleOutline : PlayCircleOutline)
            })
        }
      )
    )
  }

  if (canDelete.value) {
    actions.push(
      h(
        NPopconfirm,
        {
          onPositiveClick: () => handleDelete(row)
        },
        {
          default: () => `确认删除任务 ${row.jobName} 吗？`,
          trigger: () =>
            h(
              NButton,
              {
                text: true,
                type: 'error',
                size: 'small'
              },
              {
                default: () => '删除',
                icon: () => h(NIcon, null, { default: () => h(TrashOutline) })
              }
            )
        }
      )
    )
  }

  if (actions.length === 0) {
    return h(NTag, { size: 'small' }, { default: () => '只读' })
  }

  return h(NSpace, { size: 4 }, () => actions)
}

const columns: DataTableColumns<EtlOfflineJobSummary> = [
  {
    title: '任务',
    key: 'jobName',
    minWidth: 220,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.jobName }),
        h(NText, { depth: 3 }, { default: () => row.jobDescription || row.jobGroup || '离线同步任务' })
      ])
  },
  {
    title: '源 → 目标',
    key: 'mapping',
    minWidth: 340,
    render: (row) => {
      const source = `${row.sourceDatasource}${row.sourceSchema ? `.${row.sourceSchema}` : ''}.${row.sourceTable || '—'}`
      const target = `${row.targetDatasource}${row.targetSchema ? `.${row.targetSchema}` : ''}.${row.targetTable}`
      return h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, null, { default: () => `源：${source}` }),
        h(NText, { depth: 3 }, { default: () => `目标：${target}` })
      ])
    }
  },
  {
    title: '模式',
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
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: getJobTagType(row.status) },
        { default: () => (row.status === '1' ? '启用' : '禁用') }
      )
  },
  {
    title: 'Cron',
    key: 'cronExpression',
    width: 170,
    render: (row) => row.cronExpression || '手动触发'
  },
  {
    title: '最近执行',
    key: 'lastExecuteTime',
    width: 200,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, null, { default: () => formatTime(row.lastExecuteTime) }),
        h(
          NTag,
          { size: 'tiny', type: getExecutionTagType(row.lastExecuteStatus) },
          { default: () => row.lastExecuteStatus || '未执行' }
        )
      ])
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render: renderActionCell
  }
]

onMounted(async () => {
  await Promise.all([loadDatasourceOptions(), loadData()])
})
</script>

<template>
  <div class="page-container etl-offline-page">
    <n-card :bordered="false" class="hero-card">
      <div class="hero-head">
        <div>
          <h2>离线同步任务</h2>
          <p>统一使用 `/etl/offline/*` 契约管理分页、编辑回填和批量创建入口。</p>
        </div>
        <n-space>
          <n-button v-if="canViewLogs" @click="handleViewLogs">执行历史</n-button>
          <n-button @click="loadData">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新
          </n-button>
          <n-button v-if="canCreate" type="primary" @click="handleCreate">
            <template #icon>
              <n-icon><add-outline /></n-icon>
            </template>
            创建离线任务
          </n-button>
        </n-space>
      </div>
    </n-card>

    <n-card :bordered="false">
      <n-space justify="space-between">
        <n-space>
          <n-statistic :value="totalCount" label="总任务" />
          <n-statistic :value="enabledCount" label="启用中" />
          <n-statistic :value="incrementalCount" label="增量任务" />
        </n-space>
      </n-space>
    </n-card>

    <n-card :bordered="false" title="筛选条件">
      <n-form inline label-placement="left" label-width="70">
        <n-form-item label="关键字">
          <n-input v-model:value="searchForm.keyword" clearable placeholder="任务名 / 数据源 / 表名">
            <template #prefix>
              <n-icon><search-outline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item label="状态">
          <n-select
            v-model:value="searchForm.status"
            :options="[
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
            ]"
            clearable
            placeholder="全部"
            style="width: 140px"
          />
        </n-form-item>
        <n-form-item label="同步模式">
          <n-select
            v-model:value="searchForm.syncMode"
            :options="[
              { label: '全量', value: 'FULL' },
              { label: '增量', value: 'INCREMENTAL' }
            ]"
            clearable
            placeholder="全部"
            style="width: 160px"
          />
        </n-form-item>
        <n-form-item label="源实例">
          <n-select
            v-model:value="searchForm.sourceDatasource"
            :options="datasourceSelectOptions"
            clearable
            filterable
            placeholder="全部"
            style="width: 220px"
          />
        </n-form-item>
        <n-form-item label="目标实例">
          <n-select
            v-model:value="searchForm.targetDatasource"
            :options="datasourceSelectOptions"
            clearable
            filterable
            placeholder="全部"
            style="width: 220px"
          />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="pageNum = 1; loadData()">
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card :bordered="false" title="任务列表">
      <n-data-table
        :bordered="false"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :single-line="false"
        size="small"
      />
    </n-card>
  </div>
</template>

<style scoped>
.etl-offline-page {
  gap: 12px;
}

.hero-card {
  background: linear-gradient(105deg, #0c6ea8 0%, #0f766e 58%, #12b1d6 100%);
}

.hero-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  color: #f8fafc;
}

.hero-head h2 {
  margin: 0 0 6px;
  color: #f8fafc;
}

.hero-head p {
  margin: 0;
  color: rgba(248, 250, 252, 0.86);
}
</style>
