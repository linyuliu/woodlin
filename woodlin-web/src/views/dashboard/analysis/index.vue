<!--
  @file views/dashboard/analysis/index.vue
  @description 分析页：展示核心运行指标、模块分布与运维关注项，补齐动态菜单入口
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  NCard,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NGrid,
  NGridItem,
  NProgress,
  NSpace,
  NStatistic,
  NTag,
  type DataTableColumns,
} from 'naive-ui'
import { pageOnline } from '@/api/monitor'
import { useUserStore } from '@/stores/modules/user'

interface ModuleHealthRow {
  module: string
  score: number
  trend: string
  owner: string
}

const userStore = useUserStore()
const onlineCount = ref(0)
const moduleRows = ref<ModuleHealthRow[]>([
  { module: '系统管理', score: 96, trend: '稳定', owner: 'platform' },
  { module: '租户管理', score: 91, trend: '上升', owner: 'tenant' },
  { module: 'OpenAPI', score: 88, trend: '稳定', owner: 'integration' },
  { module: '任务调度', score: 84, trend: '关注', owner: 'ops' },
])

const totalModules = computed(() => moduleRows.value.length)
const avgScore = computed(() =>
  Math.round(moduleRows.value.reduce((sum, item) => sum + item.score, 0) / totalModules.value),
)
const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')

const columns: DataTableColumns<ModuleHealthRow> = [
  { title: '模块', key: 'module' },
  {
    title: '健康度',
    key: 'score',
    render: (row) => `${row.score}%`,
  },
  {
    title: '趋势',
    key: 'trend',
    render: (row) =>
      row.trend === '关注'
        ? '需关注'
        : row.trend,
  },
  { title: '负责人', key: 'owner' },
]

onMounted(async () => {
  try {
    const res = await pageOnline({ page: 1, size: 1 })
    onlineCount.value = res?.total ?? 0
  } catch (error) {
    console.warn('[analysis] 获取在线用户数失败', error)
  }
})
</script>

<template>
  <div class="analysis-page">
    <NGrid :cols="24" :x-gap="16" :y-gap="16">
      <NGridItem :span="24">
        <NCard title="运营概览" size="small">
          <NDescriptions bordered :column="3">
            <NDescriptionsItem label="当前值守">
              <NSpace align="center">
                <span>{{ displayName }}</span>
                <NTag type="success" size="small">在线</NTag>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="在线会话">
              {{ onlineCount }}
            </NDescriptionsItem>
            <NDescriptionsItem label="模块平均健康度">
              {{ avgScore }}%
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>
      </NGridItem>

      <NGridItem :span="8">
        <NCard size="small">
          <NStatistic label="系统模块数" :value="totalModules" />
        </NCard>
      </NGridItem>
      <NGridItem :span="8">
        <NCard size="small">
          <NStatistic label="重点关注项" :value="1" />
        </NCard>
      </NGridItem>
      <NGridItem :span="8">
        <NCard size="small">
          <NStatistic label="平均健康度" :value="avgScore" suffix="%" />
        </NCard>
      </NGridItem>

      <NGridItem :span="14">
        <NCard title="模块健康度" size="small">
          <div class="progress-list">
            <div v-for="item in moduleRows" :key="item.module" class="progress-item">
              <div class="progress-item__header">
                <span>{{ item.module }}</span>
                <NTag :type="item.score >= 90 ? 'success' : item.score >= 85 ? 'warning' : 'error'" size="small">
                  {{ item.score }}%
                </NTag>
              </div>
              <NProgress
                type="line"
                :percentage="item.score"
                :color="item.score >= 90 ? '#18a058' : item.score >= 85 ? '#f0a020' : '#d03050'"
                :show-indicator="false"
              />
            </div>
          </div>
        </NCard>
      </NGridItem>

      <NGridItem :span="10">
        <NCard title="治理建议" size="small">
          <ul class="tips-list">
            <li>动态菜单已接入后端路由树，新增菜单后建议同步检查 component 路径。</li>
            <li>租户、数据源、SQL2API、代码生成模块已增加历史路径兼容层。</li>
            <li>任务调度模块建议继续补全接口级权限校验和实时操作反馈。</li>
          </ul>
        </NCard>
      </NGridItem>

      <NGridItem :span="24">
        <NCard title="模块矩阵" size="small">
          <NDataTable :columns="columns" :data="moduleRows" :pagination="false" />
        </NCard>
      </NGridItem>
    </NGrid>
  </div>
</template>

<style scoped>
.analysis-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
}
</style>
