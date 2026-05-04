<!--
  @file views/openapi/overview/index.vue
  @description OpenAPI 概览：应用 / 凭证 / 策略 数量统计
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue'
import { NCard, NGi, NGrid, NSpin, NStatistic } from 'naive-ui'
import { getOverview, type OpenApiOverview } from '@/api/openapi'

const loading = ref(false)
const overview = shallowRef<OpenApiOverview | null>(null)

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
            <n-statistic label="应用总数" :value="overview?.appCount ?? 0" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small">
            <n-statistic label="凭证总数" :value="overview?.credentialCount ?? 0" />
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small">
            <n-statistic label="策略总数" :value="overview?.policyCount ?? 0" />
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </n-spin>
</template>

<style scoped>
.page-openapi-overview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
