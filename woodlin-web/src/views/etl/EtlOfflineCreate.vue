<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ArrowBackOutline, RefreshOutline } from '@vicons/ionicons5'
import { NButton, NCard, NEmpty, NIcon, NSpace, NStep, NSteps, NSpin } from 'naive-ui'
import type { EtlSelectableTable } from '@/views/etl/types'
import EtlOfflineWizardStepConfirm from '@/views/etl/components/EtlOfflineWizardStepConfirm.vue'
import EtlOfflineWizardStepFieldRules from '@/views/etl/components/EtlOfflineWizardStepFieldRules.vue'
import EtlOfflineWizardStepRuntime from '@/views/etl/components/EtlOfflineWizardStepRuntime.vue'
import EtlOfflineWizardStepSourceTarget from '@/views/etl/components/EtlOfflineWizardStepSourceTarget.vue'
import EtlOfflineWizardStepTableMapping from '@/views/etl/components/EtlOfflineWizardStepTableMapping.vue'
import { useEtlOfflineWizard } from '@/views/etl/composables/useEtlOfflineWizard'

const {
  activeMappingKey,
  backToList,
  columnOptionsMap,
  currentStep,
  datasourceTypeGroups,
  form,
  goNextStep,
  goPrevStep,
  initialize,
  isEditMode,
  isRelationalGroup,
  loading,
  loadingFieldRules,
  loadingSourceTables,
  nullStrategyOptions,
  removeMapping,
  schemaSyncModeOptions,
  selectedTableKeys,
  sourceDatasourceOptions,
  sourceTableKeyword,
  sourceTables,
  submit,
  submitting,
  switchActiveMapping,
  syncSelectedTables,
  targetDatasourceOptions,
  transformTypeOptions,
  validationModeOptions,
  validationResult,
  validating,
  loadSourceTables
} = useEtlOfflineWizard()

const stepTitles = [
  '源目标设置',
  '运行配置',
  '批量表映射',
  '字段处理',
  '创建确认'
]

const pageTitle = computed(() => (isEditMode.value ? '编辑离线任务' : '创建离线任务'))
const pageSubtitle = computed(() =>
  isEditMode.value
    ? '统一使用离线向导契约维护单表任务，避免旧接口和新向导再次分叉。'
    : '按向导逐步配置源端、目标端、批量表映射和字段处理规则，最终批量创建单表任务。'
)

/**
 * 刷新当前步骤数据
 */
async function handleRefresh() {
  if (currentStep.value === 3) {
    await loadSourceTables()
    return
  }

  if (currentStep.value === 4 && activeMappingKey.value) {
    await switchActiveMapping(activeMappingKey.value)
    return
  }

  await initialize()
}

/**
 * 同步表勾选事件，避免模板里直接处理多参数事件导致类型丢失。
 */
function handleSelectionChange(keys: string[], rows: EtlSelectableTable[]) {
  syncSelectedTables(keys, rows)
}

/**
 * 更新源表关键字，保持向导状态与输入框同步。
 */
function handleSourceTableKeywordChange(value: string) {
  sourceTableKeyword.value = value
}

onMounted(() => {
  void initialize()
})
</script>

<template>
  <div class="page-container etl-create-page">
    <n-card :bordered="false" class="wizard-head">
      <div class="wizard-head-row">
        <div class="wizard-head-title">
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageSubtitle }}</p>
        </div>
        <n-space>
          <n-button @click="handleRefresh">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新当前数据
          </n-button>
          <n-button @click="backToList">
            <template #icon>
              <n-icon><arrow-back-outline /></n-icon>
            </template>
            返回任务列表
          </n-button>
        </n-space>
      </div>
      <n-steps :current="currentStep" class="wizard-steps" size="small">
        <n-step v-for="title in stepTitles" :key="title" :title="title" />
      </n-steps>
    </n-card>

    <n-card v-if="loading" :bordered="false">
      <n-spin size="large">
        <template #description>正在加载离线向导配置...</template>
      </n-spin>
    </n-card>

    <template v-else>
      <EtlOfflineWizardStepSourceTarget
        v-if="currentStep === 1"
        :datasource-type-groups="datasourceTypeGroups"
        :is-relational-group="isRelationalGroup"
        :job-name="form.jobName"
        :source-datasource="form.sourceDatasource"
        :source-datasource-options="sourceDatasourceOptions"
        :source-datasource-type="form.sourceDatasourceType"
        :target-datasource="form.targetDatasource"
        :target-datasource-options="targetDatasourceOptions"
        :target-datasource-type="form.targetDatasourceType"
        @update:job-name="form.jobName = $event"
        @update:source-datasource="form.sourceDatasource = $event"
        @update:source-datasource-type="form.sourceDatasourceType = $event"
        @update:target-datasource="form.targetDatasource = $event"
        @update:target-datasource-type="form.targetDatasourceType = $event"
      />

      <EtlOfflineWizardStepRuntime
        v-else-if="currentStep === 2"
        :job-description="form.jobDescription"
        :runtime-config="form.runtimeConfig"
        :schedule-config="form.scheduleConfig"
        :schema-sync-mode-options="schemaSyncModeOptions"
        :validation-mode-options="validationModeOptions"
        @update:job-description="form.jobDescription = $event"
      />

      <EtlOfflineWizardStepTableMapping
        v-else-if="currentStep === 3"
        :column-options-map="columnOptionsMap"
        :is-edit-mode="isEditMode"
        :loading-source-tables="loadingSourceTables"
        :selected-table-keys="selectedTableKeys"
        :source-datasource="form.sourceDatasource"
        :source-table-keyword="sourceTableKeyword"
        :source-tables="sourceTables"
        :table-mappings="form.tableMappings"
        :target-datasource="form.targetDatasource"
        @remove-mapping="removeMapping"
        @search-source-tables="loadSourceTables"
        @selection-change="handleSelectionChange"
        @update:source-table-keyword="handleSourceTableKeywordChange"
      />

      <EtlOfflineWizardStepFieldRules
        v-else-if="currentStep === 4"
        :active-mapping-key="activeMappingKey"
        :column-options-map="columnOptionsMap"
        :loading-field-rules="loadingFieldRules"
        :null-strategy-options="nullStrategyOptions"
        :table-mappings="form.tableMappings"
        :transform-type-options="transformTypeOptions"
        @switch-mapping="switchActiveMapping"
      />

      <EtlOfflineWizardStepConfirm
        v-else-if="currentStep === 5"
        :is-edit-mode="isEditMode"
        :job-description="form.jobDescription"
        :job-name="form.jobName"
        :source-datasource="form.sourceDatasource"
        :sync-mode="form.runtimeConfig.syncMode"
        :table-mappings="form.tableMappings"
        :target-datasource="form.targetDatasource"
        :validation-result="validationResult"
        :validating="validating"
      />

      <n-card v-else :bordered="false">
        <n-empty description="当前步骤不存在" />
      </n-card>
    </template>

    <n-card :bordered="false" class="wizard-footer">
      <n-space justify="center">
        <n-button @click="backToList">返回任务列表</n-button>
        <n-button v-if="currentStep > 1" @click="goPrevStep">上一步</n-button>
        <n-button v-if="currentStep < stepTitles.length" type="primary" @click="goNextStep">下一步</n-button>
        <n-button v-else :loading="submitting" type="primary" @click="submit">
          {{ isEditMode ? '保存任务' : '批量创建任务' }}
        </n-button>
      </n-space>
    </n-card>
  </div>
</template>

<style scoped>
.etl-create-page {
  gap: 12px;
}

.wizard-head {
  background: linear-gradient(105deg, #0c6ea8 0%, #0f766e 58%, #12b1d6 100%);
}

.wizard-head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #f8fafc;
}

.wizard-head-title h2 {
  margin: 0 0 6px;
  color: #f8fafc;
}

.wizard-head-title p {
  margin: 0;
  color: rgba(248, 250, 252, 0.86);
}

.wizard-steps {
  margin-top: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(248, 250, 252, 0.12);
}

.wizard-footer {
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>
