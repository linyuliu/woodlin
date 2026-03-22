<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { RefreshOutline } from '@vicons/ionicons5'
import {
  NButton,
  NCard,
  NDataTable,
  NIcon,
  NSpace,
  NStatistic,
  NTag,
  NText,
  type DataTableColumns
} from 'naive-ui'
import { getEtlExecutionLogList, type EtlExecutionLog } from '@/api/etl'
import { PERMISSIONS } from '@/constants/permission-keys'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const loading = ref(false)
const logs = ref<EtlExecutionLog[]>([])

const canViewLogs = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ETL_OFFLINE_LOG_LIST))
const successCount = computed(() => logs.value.filter((item) => item.executionStatus === 'SUCCESS').length)
const runningCount = computed(() => logs.value.filter((item) => item.executionStatus === 'RUNNING').length)

/**
 * 格式化时间，避免列表直接展示原始字符串。
 */
function formatTime(value?: string) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

/**
 * 根据执行状态返回标签类型。
 */
function getStatusType(status?: string) {
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
 * 加载执行日志。
 */
async function loadLogs() {
  if (!canViewLogs.value) {
    logs.value = []
    return
  }

  loading.value = true
  try {
    logs.value = await getEtlExecutionLogList()
  } finally {
    loading.value = false
  }
}

const columns: DataTableColumns<EtlExecutionLog> = [
  {
    title: '任务',
    key: 'jobName',
    minWidth: 220,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.jobName || `任务 #${row.jobId}` }),
        h(NText, { depth: 3 }, { default: () => `日志 ID：${row.logId}` })
      ])
  },
  {
    title: '执行状态',
    key: 'executionStatus',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: getStatusType(row.executionStatus) },
        { default: () => row.executionStatus || 'UNKNOWN' }
      )
  },
  {
    title: '时间窗口',
    key: 'timeRange',
    minWidth: 220,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, null, { default: () => `开始：${formatTime(row.startTime)}` }),
        h(NText, { depth: 3 }, { default: () => `结束：${formatTime(row.endTime)}` })
      ])
  },
  {
    title: '数据量',
    key: 'metrics',
    minWidth: 180,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, null, { default: () => `抽取：${row.extractedRows ?? 0}` }),
        h(NText, { depth: 3 }, { default: () => `加载：${row.loadedRows ?? 0}` })
      ])
  },
  {
    title: '错误信息',
    key: 'errorMessage',
    minWidth: 260,
    render: (row) => row.errorMessage || '—'
  }
]

onMounted(() => {
  void loadLogs()
})
</script>

<template>
  <div class="page-container etl-log-page">
    <n-card :bordered="false" class="hero-card">
      <div class="hero-head">
        <div>
          <h2>离线同步执行历史</h2>
          <p>保留 `/etl/logs` 兼容入口，同时在 ETL 模块内集中查看最近执行情况。</p>
        </div>
        <n-button @click="loadLogs">
          <template #icon>
            <n-icon><refresh-outline /></n-icon>
          </template>
          刷新
        </n-button>
      </div>
    </n-card>

    <n-card :bordered="false">
      <n-space>
        <n-statistic :value="logs.length" label="总日志数" />
        <n-statistic :value="successCount" label="成功" />
        <n-statistic :value="runningCount" label="执行中" />
      </n-space>
    </n-card>

    <n-card :bordered="false" title="执行记录">
      <n-data-table
        :bordered="false"
        :columns="columns"
        :data="logs"
        :loading="loading"
        :single-line="false"
        size="small"
      />
    </n-card>
  </div>
</template>

<style scoped>
.etl-log-page {
  gap: 12px;
}

.hero-card {
  background: linear-gradient(105deg, #155e75 0%, #0f766e 55%, #0ea5e9 100%);
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
