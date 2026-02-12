<script lang="ts" setup>
import {computed, h, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {
  type DataTableColumns,
  NButton,
  NCard,
  NDataTable,
  NIcon,
  NInput,
  NPopconfirm,
  NSpace,
  NStatistic,
  NTag,
  NText,
  useMessage,
} from 'naive-ui'
import {
  AddOutline,
  FlashOutline,
  PauseCircleOutline,
  PlayCircleOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import {
  deleteEtlJob,
  disableEtlJob,
  enableEtlJob,
  type EtlJob,
  executeEtlJob,
  getEtlJobList,
} from '@/api/etl'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const keyword = ref('')
const tableData = ref<EtlJob[]>([])

const totalCount = computed(() => tableData.value.length)
const enabledCount = computed(() => tableData.value.filter((item) => item.status === '1').length)
const incrementalCount = computed(
  () => tableData.value.filter((item) => item.syncMode === 'INCREMENTAL').length,
)

const filteredTableData = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) {
    return tableData.value
  }
  return tableData.value.filter((item) => {
    const content = [
      item.jobName,
      item.jobGroup,
      item.sourceDatasource,
      item.sourceSchema,
      item.sourceTable,
      item.targetDatasource,
      item.targetSchema,
      item.targetTable,
      item.syncMode,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return content.includes(key)
  })
})

const formatTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

const getSyncModeText = (value?: string) => {
  if (value === 'INCREMENTAL') {
    return '增量'
  }
  return '全量'
}

const getJobStatusTagType = (value?: string) => {
  if (value === '1') {
    return 'success' as const
  }
  return 'default' as const
}

const getExecutionStatusTagType = (value?: string) => {
  if (value === 'SUCCESS') {
    return 'success' as const
  }
  if (value === 'RUNNING') {
    return 'info' as const
  }
  if (value === 'FAILED') {
    return 'error' as const
  }
  return 'default' as const
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getEtlJobList()
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  router.push('/etl/offline/create')
}

const handleExecute = async (row: EtlJob) => {
  if (!row.jobId) {
    return
  }
  await executeEtlJob(row.jobId)
  message.success(`已触发任务：${row.jobName}`)
  await loadData()
}

const handleEnableChange = async (row: EtlJob, enable: boolean) => {
  if (!row.jobId) {
    return
  }
  if (enable) {
    await enableEtlJob(row.jobId)
    message.success(`已启用任务：${row.jobName}`)
  } else {
    await disableEtlJob(row.jobId)
    message.success(`已禁用任务：${row.jobName}`)
  }
  await loadData()
}

const handleDelete = async (row: EtlJob) => {
  if (!row.jobId) {
    return
  }
  await deleteEtlJob(row.jobId)
  message.success(`已删除任务：${row.jobName}`)
  await loadData()
}

const columns: DataTableColumns<EtlJob> = [
  {
    title: '任务',
    key: 'jobName',
    width: 220,
    render: (row) =>
      h(NSpace, {vertical: true, size: 2}, () => [
        h(NText, {strong: true}, {default: () => row.jobName || '-'}),
        h(NText, {depth: 3}, {default: () => row.jobGroup || 'OFFLINE_SYNC'}),
      ]),
  },
  {
    title: '源 -> 目标',
    key: 'mapping',
    minWidth: 360,
    render: (row) => {
      const source = `${row.sourceDatasource || '-'}${row.sourceSchema ? `.${row.sourceSchema}` : ''}.${row.sourceTable || '-'}`
      const target = `${row.targetDatasource || '-'}${row.targetSchema ? `.${row.targetSchema}` : ''}.${row.targetTable || '-'}`
      return h(NSpace, {vertical: true, size: 2}, () => [
        h(NText, null, {default: () => `源: ${source}`}),
        h(NText, {depth: 3}, {default: () => `目标: ${target}`}),
      ])
    },
  },
  {
    title: '同步模式',
    key: 'syncMode',
    width: 110,
    render: (row) =>
      h(
        NTag,
        {type: row.syncMode === 'INCREMENTAL' ? 'info' : 'default', size: 'small'},
        {default: () => getSyncModeText(row.syncMode)},
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        {type: getJobStatusTagType(row.status), size: 'small'},
        {default: () => (row.status === '1' ? '启用' : '禁用')},
      ),
  },
  {
    title: '计划',
    key: 'cronExpression',
    width: 160,
    render: (row) => row.cronExpression || '手动触发',
  },
  {
    title: '最近执行',
    key: 'lastExecuteTime',
    width: 190,
    render: (row) =>
      h(NSpace, {vertical: true, size: 2}, () => [
        h(NText, null, {default: () => formatTime(row.lastExecuteTime)}),
        h(
          NTag,
          {size: 'tiny', type: getExecutionStatusTagType(row.lastExecuteStatus)},
          {default: () => row.lastExecuteStatus || '-'},
        ),
      ]),
  },
  {
    title: '操作',
    key: 'actions',
    width: 290,
    render: (row) =>
      h(NSpace, {size: 4}, () => [
        h(
          NButton,
          {
            size: 'small',
            tertiary: true,
            type: 'primary',
            onClick: () => handleExecute(row),
          },
          {
            default: () => '立即执行',
            icon: () => h(NIcon, null, {default: () => h(FlashOutline)}),
          },
        ),
        row.status === '1'
          ? h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'warning',
              onClick: () => handleEnableChange(row, false),
            },
            {
              default: () => '禁用',
              icon: () => h(NIcon, null, {default: () => h(PauseCircleOutline)}),
            },
          )
          : h(
            NButton,
            {
              size: 'small',
              tertiary: true,
              type: 'success',
              onClick: () => handleEnableChange(row, true),
            },
            {
              default: () => '启用',
              icon: () => h(NIcon, null, {default: () => h(PlayCircleOutline)}),
            },
          ),
        h(
          NPopconfirm,
          {onPositiveClick: () => handleDelete(row)},
          {
            default: () => `确认删除任务 ${row.jobName} 吗？`,
            trigger: () =>
              h(
                NButton,
                {size: 'small', tertiary: true, type: 'error'},
                {
                  default: () => '删除',
                  icon: () => h(NIcon, null, {default: () => h(TrashOutline)}),
                },
              ),
          },
        ),
      ]),
  },
]

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="page-container etl-offline-page">
    <n-card :bordered="false" class="hero-card">
      <div class="hero-head">
        <div>
          <h2>离线同步任务</h2>
          <p>先支持简化离线同步流程，保留现有数据源解析与提取链路。</p>
        </div>
        <n-space>
          <n-button @click="loadData">
            <template #icon>
              <n-icon>
                <refresh-outline/>
              </n-icon>
            </template>
            刷新
          </n-button>
          <n-button type="primary" @click="handleCreate">
            <template #icon>
              <n-icon>
                <add-outline/>
              </n-icon>
            </template>
            创建离线任务
          </n-button>
        </n-space>
      </div>
    </n-card>

    <n-card :bordered="false">
      <n-space align="center" class="toolbar-row" justify="space-between">
        <n-space>
          <n-statistic :value="totalCount" label="总任务"/>
          <n-statistic :value="enabledCount" label="启用中"/>
          <n-statistic :value="incrementalCount" label="增量任务"/>
        </n-space>
        <n-input
          v-model:value="keyword"
          clearable
          placeholder="搜索任务名 / 数据源 / 表名"
          style="width: 340px"
        >
          <template #prefix>
            <n-icon>
              <search-outline/>
            </n-icon>
          </template>
        </n-input>
      </n-space>
    </n-card>

    <n-card :bordered="false" title="任务列表">
      <n-data-table
        :bordered="false"
        :columns="columns"
        :data="filteredTableData"
        :loading="loading"
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

.toolbar-row {
  width: 100%;
}
</style>
