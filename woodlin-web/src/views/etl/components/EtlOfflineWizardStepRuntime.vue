<script setup lang="ts">
import {
  NCard,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NInput,
  NInputNumber,
  NSelect,
  NSpace,
  NSwitch,
  NTag
} from 'naive-ui'
import type { EtlOfflineRuntimeConfig, EtlOfflineScheduleConfig } from '@/api/etl'
import type { EtlSelectOption } from '@/views/etl/types'

defineProps<{
  runtimeConfig: EtlOfflineRuntimeConfig
  scheduleConfig: EtlOfflineScheduleConfig
  jobDescription: string
  validationModeOptions: EtlSelectOption[]
  schemaSyncModeOptions: EtlSelectOption[]
}>()

const emit = defineEmits<{
  'update:jobDescription': [value: string]
}>()
</script>

<template>
  <n-card :bordered="false" title="运行与调度配置">
    <n-grid :cols="2" :x-gap="16" item-responsive responsive="screen">
      <n-grid-item>
        <n-card :bordered="false" class="config-card" size="small" title="运行配置">
          <n-form label-placement="left" label-width="120">
            <n-form-item label="同步模式">
              <n-select
                v-model:value="runtimeConfig.syncMode"
                :options="[
                  { label: '全量同步', value: 'FULL' },
                  { label: '增量同步', value: 'INCREMENTAL' }
                ]"
              />
            </n-form-item>
            <n-form-item label="批处理大小">
              <n-input-number v-model:value="runtimeConfig.batchSize" :max="200000" :min="100" :step="100" />
            </n-form-item>
            <n-form-item label="失败重试次数">
              <n-input-number v-model:value="runtimeConfig.retryCount" :max="20" :min="0" />
            </n-form-item>
            <n-form-item label="重试间隔(秒)">
              <n-input-number v-model:value="runtimeConfig.retryInterval" :max="3600" :min="1" />
            </n-form-item>
            <n-form-item label="校验模式">
              <n-select v-model:value="runtimeConfig.validationMode" :options="validationModeOptions" />
            </n-form-item>
            <n-form-item label="目标结构策略">
              <n-select v-model:value="runtimeConfig.schemaSyncMode" :options="schemaSyncModeOptions" />
            </n-form-item>
            <n-form-item label="全量前清空目标表">
              <n-switch v-model:value="runtimeConfig.truncateTarget" />
            </n-form-item>
            <n-form-item label="允许并发执行">
              <n-switch v-model:value="runtimeConfig.allowConcurrent" />
            </n-form-item>
          </n-form>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-space :size="16" vertical>
          <n-card :bordered="false" class="config-card" size="small" title="调度配置">
            <n-form label-placement="left" label-width="120">
              <n-form-item label="自动启动任务">
                <n-switch v-model:value="scheduleConfig.autoStart" />
              </n-form-item>
              <n-form-item label="Cron 表达式">
                <n-input
                  v-model:value="scheduleConfig.cronExpression"
                  :disabled="!scheduleConfig.autoStart"
                  placeholder="例如：0 0/30 * * * ?"
                />
              </n-form-item>
            </n-form>
          </n-card>

          <n-card :bordered="false" class="config-card" size="small" title="任务描述">
            <n-space :size="12" vertical>
              <n-tag size="small" type="info">仅保留对执行链路真正生效的配置项</n-tag>
              <n-input
                :autosize="{ minRows: 5, maxRows: 8 }"
                :value="jobDescription"
                placeholder="描述任务目标、数据范围和特殊注意事项"
                type="textarea"
                @update:value="emit('update:jobDescription', $event)"
              />
            </n-space>
          </n-card>
        </n-space>
      </n-grid-item>
    </n-grid>
  </n-card>
</template>

<style scoped>
.config-card {
  min-height: 100%;
}
</style>
