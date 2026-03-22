<script setup lang="ts">
import { computed } from 'vue'
import {
  NButton,
  NCard,
  NEmpty,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NInput,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  NText
} from 'naive-ui'
import type { EtlOfflineFieldRule, EtlOfflineTableMapping } from '@/api/etl'
import type { EtlSelectOption } from '@/views/etl/types'

const VALUE_SOURCE_OPTIONS: EtlSelectOption[] = [
  { label: '源字段取值', value: 'SOURCE_COLUMN' },
  { label: '常量值', value: 'CONSTANT' },
  { label: '默认值', value: 'DEFAULT_VALUE' }
]

interface Props {
  tableMappings: EtlOfflineTableMapping[]
  activeMappingKey: string
  columnOptionsMap: Record<string, EtlSelectOption[]>
  transformTypeOptions: EtlSelectOption[]
  nullStrategyOptions: EtlSelectOption[]
  loadingFieldRules: Record<string, boolean>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'switch-mapping': [mappingKey: string]
}>()

/**
 * 构建表映射键
 */
function buildMappingKey(mapping: EtlOfflineTableMapping) {
  return `${mapping.sourceSchema || '__default__'}::${mapping.sourceTable}`
}

const activeMapping = computed(() =>
  props.tableMappings.find((mapping) => buildMappingKey(mapping) === props.activeMappingKey)
)

/**
 * 确保转换配置对象存在
 */
function ensureTransformConfig(rule: EtlOfflineFieldRule) {
  if (!rule.transformConfig) {
    rule.transformConfig = {}
  }
  return rule.transformConfig
}
</script>

<template>
  <n-space :size="16" vertical>
    <n-card :bordered="false" size="small" title="字段处理范围">
      <n-space wrap>
        <n-button
          v-for="mapping in tableMappings"
          :key="buildMappingKey(mapping)"
          :loading="loadingFieldRules[buildMappingKey(mapping)]"
          :type="activeMappingKey === buildMappingKey(mapping) ? 'primary' : 'default'"
          @click="emit('switch-mapping', buildMappingKey(mapping))"
        >
          {{ mapping.sourceSchema ? `${mapping.sourceSchema}.` : '' }}{{ mapping.sourceTable }}
        </n-button>
      </n-space>
    </n-card>

    <n-card :bordered="false" size="small" title="字段级处理规则">
      <n-empty v-if="!activeMapping" description="请先选择需要维护的表映射" />
      <n-space v-else :size="12" vertical>
        <n-space align="center" justify="space-between">
          <n-space align="center">
            <n-tag type="info">源表</n-tag>
            <n-text>{{ activeMapping.sourceSchema ? `${activeMapping.sourceSchema}.` : '' }}{{ activeMapping.sourceTable }}</n-text>
            <n-tag type="success">目标表</n-tag>
            <n-text>{{ activeMapping.targetSchema ? `${activeMapping.targetSchema}.` : '' }}{{ activeMapping.targetTable }}</n-text>
          </n-space>
          <n-space :size="8">
            <n-tag size="small">源字段 {{ columnOptionsMap[activeMappingKey]?.length || 0 }}</n-tag>
            <n-tag size="small">启用字段 {{ activeMapping.fieldRules.filter(rule => rule.enabled).length }}</n-tag>
          </n-space>
        </n-space>

        <n-card
          v-for="rule in activeMapping.fieldRules"
          :key="`${rule.sourceColumnName}-${rule.ordinalPosition}`"
          :bordered="false"
          class="rule-card"
          size="small"
        >
          <n-grid :cols="24" :x-gap="12">
            <n-grid-item :span="24">
              <n-space align="center" justify="space-between">
                <n-space align="center">
                  <n-tag>{{ rule.sourceColumnName }}</n-tag>
                  <n-text depth="3">{{ rule.sourceColumnType || '未知类型' }}</n-text>
                </n-space>
                <n-space align="center">
                  <n-text depth="3">启用</n-text>
                  <n-switch v-model:value="rule.enabled" />
                </n-space>
              </n-space>
            </n-grid-item>

            <n-grid-item :span="8">
              <n-form-item label="目标字段" label-placement="top">
                <n-input v-model:value="rule.targetColumnName" placeholder="请输入目标字段名" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item :span="5">
              <n-form-item label="取值来源" label-placement="top">
                <n-select v-model:value="rule.valueSource" :options="VALUE_SOURCE_OPTIONS" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item :span="5">
              <n-form-item label="空值策略" label-placement="top">
                <n-select v-model:value="rule.nullStrategy" :options="nullStrategyOptions" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item :span="6">
              <n-form-item label="转换类型" label-placement="top">
                <n-select v-model:value="rule.transformType" :options="transformTypeOptions" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item v-if="rule.valueSource === 'CONSTANT'" :span="12">
              <n-form-item label="常量值" label-placement="top">
                <n-input v-model:value="rule.constantValue" placeholder="请输入常量值" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item v-if="rule.valueSource === 'DEFAULT_VALUE'" :span="12">
              <n-form-item label="默认值" label-placement="top">
                <n-input v-model:value="rule.defaultValue" placeholder="请输入默认值" />
              </n-form-item>
            </n-grid-item>

            <n-grid-item v-if="rule.transformType === 'DATE_FORMAT'" :span="12">
              <n-form-item label="日期格式" label-placement="top">
                <n-input
                  v-model:value="ensureTransformConfig(rule).pattern as string"
                  placeholder="例如：yyyy-MM-dd HH:mm:ss"
                />
              </n-form-item>
            </n-grid-item>

            <n-grid-item v-if="rule.transformType === 'NUMBER_CAST'" :span="12">
              <n-form-item label="数字目标类型" label-placement="top">
                <n-input
                  v-model:value="ensureTransformConfig(rule).targetType as string"
                  placeholder="例如：INTEGER / DECIMAL(18,2)"
                />
              </n-form-item>
            </n-grid-item>

            <n-grid-item :span="24">
              <n-form label-placement="top">
                <n-form-item label="备注">
                  <n-input
                    v-model:value="rule.remark"
                    :autosize="{ minRows: 1, maxRows: 2 }"
                    placeholder="可记录字段转换原因或特殊说明"
                    type="textarea"
                  />
                </n-form-item>
              </n-form>
            </n-grid-item>
          </n-grid>
        </n-card>
      </n-space>
    </n-card>
  </n-space>
</template>

<style scoped>
.rule-card {
  background: color-mix(in srgb, var(--primary-color) 4%, var(--card-color));
}
</style>
