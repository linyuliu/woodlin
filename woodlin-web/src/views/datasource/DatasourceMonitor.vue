<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NGrid,
  NGridItem,
  NIcon,
  NSpin,
  NStatistic,
  NSpace,
  NTag,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import {
  FlashOutline,
  PulseOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import {
  getDatasourceList,
  getDatasourceMetadata,
  testDatasource,
  type DatasourceConfig
} from '@/api/datasource'

type HealthState = 'unknown' | 'ok' | 'fail'

type HealthRecord = {
  state: HealthState
  message: string
  product?: string
  version?: string
  charset?: string
  collation?: string
  checkedAt?: string
}

const message = useMessage()

const loading = ref(false)
const checking = ref(false)
const datasourceList = ref<DatasourceConfig[]>([])
const healthMap = ref<Record<string, HealthRecord>>({})

const healthyCount = computed(() =>
  datasourceList.value.filter(item => healthMap.value[item.datasourceCode]?.state === 'ok').length
)
const failedCount = computed(() =>
  datasourceList.value.filter(item => healthMap.value[item.datasourceCode]?.state === 'fail').length
)
const unknownCount = computed(() =>
  datasourceList.value.filter(item => !healthMap.value[item.datasourceCode]).length
)

const columns: DataTableColumns<DatasourceConfig> = [
  { title: '数据源', key: 'datasourceName', width: 150 },
  { title: '编码', key: 'datasourceCode', width: 140 },
  {
    title: '类型',
    key: 'datasourceType',
    width: 120,
    render: row => h(NTag, { size: 'small', type: 'info' }, { default: () => row.datasourceType })
  },
  {
    title: '运行状态',
    key: 'runtimeState',
    width: 110,
    render: row => {
      const state = healthMap.value[row.datasourceCode]?.state || 'unknown'
      const mapping: Record<HealthState, { type: 'default' | 'success' | 'error'; text: string }> = {
        unknown: { type: 'default', text: '未检测' },
        ok: { type: 'success', text: '正常' },
        fail: { type: 'error', text: '异常' }
      }
      const item = mapping[state]
      return h(NTag, { size: 'small', type: item.type }, { default: () => item.text })
    }
  },
  {
    title: '数据库产品',
    key: 'product',
    minWidth: 180,
    render: row => healthMap.value[row.datasourceCode]?.product || '-'
  },
  {
    title: '版本',
    key: 'version',
    width: 150,
    render: row => healthMap.value[row.datasourceCode]?.version || '-'
  },
  {
    title: '字符集 / 排序',
    key: 'charset',
    minWidth: 200,
    render: row => {
      const record = healthMap.value[row.datasourceCode]
      if (!record) {
        return '-'
      }
      const charset = record.charset || '-'
      const collation = record.collation || '-'
      return `${charset} / ${collation}`
    }
  },
  {
    title: '最近检测',
    key: 'checkedAt',
    width: 170,
    render: row => healthMap.value[row.datasourceCode]?.checkedAt || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 110,
    render: row =>
      h(
        NButton,
        {
          size: 'small',
          tertiary: true,
          type: 'primary',
          onClick: () => runHealthCheck(row)
        },
        { default: () => '检测' }
      )
  }
]

const nowText = () => new Date().toLocaleString()

const loadDatasourceList = async () => {
  loading.value = true
  try {
    datasourceList.value = await getDatasourceList()
  } finally {
    loading.value = false
  }
}

const runHealthCheck = async (row: DatasourceConfig) => {
  try {
    await testDatasource(row)
    const metadata = await getDatasourceMetadata(row.datasourceCode)
    healthMap.value[row.datasourceCode] = {
      state: 'ok',
      message: '连接正常',
      product: metadata.databaseProductName,
      version: metadata.databaseProductVersion,
      charset: metadata.charset,
      collation: metadata.collation,
      checkedAt: nowText()
    }
    message.success(`检测通过：${row.datasourceName}`)
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '连接失败'
    healthMap.value[row.datasourceCode] = {
      state: 'fail',
      message: errorMessage,
      checkedAt: nowText()
    }
    message.error(`检测失败：${row.datasourceName}`)
  }
}

const runHealthCheckAll = async () => {
  if (datasourceList.value.length === 0) {
    return
  }
  checking.value = true
  try {
    for (const row of datasourceList.value) {
      await runHealthCheck(row)
    }
  } finally {
    checking.value = false
  }
}

onMounted(async () => {
  await loadDatasourceList()
})
</script>

<template>
  <div class="monitor-page">
    <n-card class="monitor-hero" :bordered="false">
      <div class="monitor-hero-content">
        <div>
          <h2>数据源健康监控</h2>
          <p>快速巡检连接可用性与核心元数据状态，为 ETL / CDC 任务编排提供稳定基础。</p>
        </div>
        <n-space>
          <n-button @click="loadDatasourceList">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新列表
          </n-button>
          <n-button type="primary" :loading="checking" @click="runHealthCheckAll">
            <template #icon>
              <n-icon><pulse-outline /></n-icon>
            </template>
            全量巡检
          </n-button>
        </n-space>
      </div>
    </n-card>

    <n-grid :x-gap="12" :y-gap="12" cols="1 s:2 m:4" responsive="screen">
      <n-grid-item>
        <n-card :bordered="false" class="metric-card">
          <n-statistic label="总数据源" :value="datasourceList.length" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="metric-card">
          <n-statistic label="健康" :value="healthyCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="metric-card">
          <n-statistic label="异常" :value="failedCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="metric-card">
          <n-statistic label="待检测" :value="unknownCount" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-spin :show="loading">
      <n-card title="巡检明细" :bordered="false">
        <template #header-extra>
          <n-space align="center">
            <n-icon size="18" color="#0e7490"><flash-outline /></n-icon>
            <span>连接与元数据联合检测</span>
          </n-space>
        </template>
        <n-data-table
          :columns="columns"
          :data="datasourceList"
          :pagination="{ pageSize: 10 }"
          :single-line="false"
        />
      </n-card>
    </n-spin>
  </div>
</template>

<style scoped>
.monitor-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-hero {
  background: linear-gradient(120deg, #164e63 0%, #0e7490 48%, #0891b2 100%);
  color: #f0f9ff;
}

.monitor-hero-content {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.monitor-hero h2 {
  margin: 0 0 6px;
  color: #ffffff;
}

.monitor-hero p {
  margin: 0;
  color: rgba(240, 249, 255, 0.88);
}

.metric-card {
  background:
    radial-gradient(circle at top right, rgba(8, 145, 178, 0.2), transparent 50%),
    #ffffff;
}

@media (max-width: 960px) {
  .monitor-hero-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
