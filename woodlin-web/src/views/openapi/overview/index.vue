<!--
  @file views/openapi/overview/index.vue
  @description OpenAPI 概览：统计卡片 + Top Apps 柱状图
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import {
  NCard,
  NDataTable,
  NGi,
  NGrid,
  NSpin,
  NStatistic,
  type DataTableColumns,
} from 'naive-ui'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import {
  GridComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import { getOverview, type OpenApiOverview } from '@/api/openapi'

use([CanvasRenderer, BarChart, GridComponent, TitleComponent, TooltipComponent])

const loading = ref(false)
const overview = shallowRef<OpenApiOverview | null>(null)

const chartOption = computed(() => {
  const data = overview.value?.topApps ?? []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.appName),
      axisLabel: { interval: 0, rotate: data.length > 6 ? 30 : 0 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '调用次数',
        type: 'bar',
        data: data.map((d) => d.calls),
        itemStyle: { color: '#18a058' },
        barMaxWidth: 36,
      },
    ],
  }
})

const topColumns: DataTableColumns<{ appName: string; calls: number }> = [
  { title: '应用名称', key: 'appName' },
  { title: '调用次数', key: 'calls', width: 160 },
]

async function refresh(): Promise<void> {
  loading.value = true
  try {
    overview.value = await getOverview()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void refresh()
})
</script>

<template>
  <n-spin :show="loading">
    <div class="page-openapi-overview">
      <n-grid :cols="3" :x-gap="12">
        <n-gi>
          <n-card size="small">
            <n-statistic label="应用总数" :value="overview?.totalApps ?? 0" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small">
            <n-statistic label="累计调用" :value="overview?.totalCalls ?? 0" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small">
            <n-statistic
              label="成功率"
              :value="overview?.successRate ?? 0"
              :precision="2"
              suffix="%"
            />
          </n-card>
        </n-gi>
      </n-grid>

      <n-card size="small" title="Top Apps - 调用量">
        <div class="chart-wrap">
          <v-chart :option="chartOption" autoresize />
        </div>
      </n-card>

      <n-card size="small" title="Top Apps - 明细">
        <n-data-table
          :columns="topColumns"
          :data="overview?.topApps ?? []"
          :row-key="(row) => row.appName"
          striped
        />
      </n-card>
    </div>
  </n-spin>
</template>

<style scoped>
.page-openapi-overview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chart-wrap {
  height: 320px;
}
</style>
