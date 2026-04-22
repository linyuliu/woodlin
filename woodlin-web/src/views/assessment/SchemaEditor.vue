<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NDivider,
  NInput,
  NInputNumber,
  NSelect,
  NSpace,
  NSwitch,
  NTabPane,
  NTabs,
  NTag,
  NText,
  useMessage
} from 'naive-ui'
import {
  type AssessmentSchemaAggregateDTO,
  compileAssessmentSchema,
  exportAssessmentDsl,
  getAssessmentSchema,
  importAssessmentDsl,
  saveAssessmentSchema,
  type SchemaDimensionBindingDTO,
  type SchemaDimensionDTO,
  type SchemaItemDTO,
  type SchemaOptionDTO,
  type SchemaRuleDTO,
  type SchemaSectionDTO,
  validateAssessmentSchema,
  type ValidationReport
} from '@/api/assessment'
import {createEmptySchema, ensureSchemaDefaults} from './schema-editor.utils'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const versionId = computed(() => String(route.params.versionId ?? ''))
const loading = ref(false)
const saving = ref(false)
const actionLoading = ref(false)
const schema = ref<AssessmentSchemaAggregateDTO>(createEmptySchema())
const validationReport = ref<ValidationReport | null>(null)
const expertDsl = ref('')

const assessmentTypeOptions = [
  {label: '量表', value: 'scale'},
  {label: '试卷', value: 'exam'},
  {label: '问卷', value: 'survey'}
]

const randomStrategyOptions = [
  {label: '不乱序', value: 'none'},
  {label: '仅题目乱序', value: 'random_items'},
  {label: '仅选项乱序', value: 'random_options'},
  {label: '题目与选项都乱序', value: 'random_both'}
]

const scoreModeOptions = [
  {label: '求和', value: 'sum'},
  {label: '均值', value: 'mean'},
  {label: '最大值', value: 'max'},
  {label: '最小值', value: 'min'},
  {label: '加权求和', value: 'weighted_sum'},
  {label: '自定义 DSL', value: 'custom_dsl'}
]

const reverseModeOptions = [
  {label: '不反向', value: 'none'},
  {label: '公式反向', value: 'formula'},
  {label: '映射表反向', value: 'table'}
]

const itemTypeOptions = [
  {label: '单选', value: 'single_choice'},
  {label: '多选', value: 'multiple_choice'},
  {label: '评分', value: 'rating'},
  {label: '文本', value: 'text'},
  {label: '数字', value: 'number'},
  {label: '说明', value: 'statement'}
]

const demographicSection = computed(() => {
  ensureSchemaDefaults(schema.value)
  return schema.value.sections.find(section => section.sectionCode === 'DEMOGRAPHIC')!
})

const regularSections = computed(() => schema.value.sections.filter(section => section.sectionCode !== 'DEMOGRAPHIC'))

onMounted(() => {
  loadSchema()
})

async function loadSchema() {
  if (!versionId.value) {
    message.error('缺少版本ID')
    return
  }
  loading.value = true
  try {
    const data = await getAssessmentSchema(versionId.value)
    schema.value = ensureSchemaDefaults(data)
    expertDsl.value = data.dslSource ?? ''
  } catch {
    message.error('加载结构失败')
  } finally {
    loading.value = false
  }
}

function createSection(sectionCode?: string, demographic = false): SchemaSectionDTO {
  return {
    sectionCode: sectionCode ?? `SEC_${String(schema.value.sections.length + 1).padStart(3, '0')}`,
    sectionTitle: demographic ? '人口学前置' : '新章节',
    sectionDesc: '',
    displayMode: 'paged',
    randomStrategy: demographic ? 'none' : 'none',
    sortOrder: schema.value.sections.length + 1,
    isRequired: demographic,
    items: []
  }
}

function createItem(demographic = false): SchemaItemDTO {
  return {
    itemCode: `${demographic ? 'D' : 'Q'}${Math.floor(Math.random() * 900 + 100)}`,
    itemType: demographic ? 'single_choice' : 'single_choice',
    stem: '',
    helpText: '',
    isRequired: true,
    isScored: !demographic,
    isAnchor: false,
    isReverse: false,
    isDemographic: demographic,
    demographicField: demographic ? '' : undefined,
    options: [],
    dimensionBindings: []
  }
}

function createOption(): SchemaOptionDTO {
  return {
    optionCode: `OPT_${Math.floor(Math.random() * 900 + 100)}`,
    displayText: '',
    rawValue: '',
    isExclusive: false,
    isCorrect: false
  }
}

function createDimension(): SchemaDimensionDTO {
  return {
    dimensionCode: `DIM_${Math.floor(Math.random() * 900 + 100)}`,
    dimensionName: '新维度',
    scoreMode: 'sum'
  }
}

function createDimensionBinding(): SchemaDimensionBindingDTO {
  return {
    dimensionCode: schema.value.dimensions[0]?.dimensionCode ?? '',
    weight: 1,
    reverseMode: 'none'
  }
}

function createRule(): SchemaRuleDTO {
  return {
    ruleCode: `RULE_${String(schema.value.rules.length + 1).padStart(3, '0')}`,
    ruleName: '新规则',
    ruleType: 'validation',
    targetType: 'form',
    targetCode: 'FORM',
    isActive: true,
    priority: schema.value.rules.length + 1
  }
}

async function handleSave() {
  saving.value = true
  try {
    schema.value = ensureSchemaDefaults(schema.value)
    const saved = await saveAssessmentSchema(versionId.value, schema.value)
    schema.value = ensureSchemaDefaults(saved)
    expertDsl.value = saved.dslSource ?? expertDsl.value
    message.success('结构已保存')
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleCompile() {
  actionLoading.value = true
  try {
    await handleSave()
    const result = await compileAssessmentSchema(versionId.value)
    await loadSchema()
    if (result.compileError) {
      message.warning(`编译完成，但存在错误：${result.compileError}`)
      return
    }
    message.success('编译通过')
  } catch {
    message.error('编译失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleValidate() {
  actionLoading.value = true
  try {
    await handleSave()
    validationReport.value = await validateAssessmentSchema(versionId.value)
    if (validationReport.value.valid) {
      message.success('校验通过')
    } else {
      message.warning(`校验完成：${validationReport.value.errorCount} 个错误，${validationReport.value.warningCount} 个警告`)
    }
  } catch {
    message.error('校验失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleImportDsl() {
  actionLoading.value = true
  try {
    const data = await importAssessmentDsl(versionId.value, expertDsl.value)
    schema.value = ensureSchemaDefaults(data)
    message.success('专家模式内容已导入')
  } catch {
    message.error('导入失败，请确认格式为导出后的 JSON DSL 或 canonical schema')
  } finally {
    actionLoading.value = false
  }
}

async function handleExportDsl() {
  actionLoading.value = true
  try {
    expertDsl.value = await exportAssessmentDsl(versionId.value)
    message.success('专家模式内容已刷新')
  } catch {
    message.error('导出失败')
  } finally {
    actionLoading.value = false
  }
}

function addDemographicItem() {
  demographicSection.value.items.push(createItem(true))
}

function addSection() {
  schema.value.sections.push(createSection())
}

function removeSection(sectionCode: string) {
  if (sectionCode === 'DEMOGRAPHIC') {
    return
  }
  schema.value.sections = schema.value.sections.filter(section => section.sectionCode !== sectionCode)
}

function addItem(section: SchemaSectionDTO, demographic = false) {
  section.items.push(createItem(demographic))
}

function removeItem(section: SchemaSectionDTO, itemCode: string) {
  section.items = section.items.filter(item => item.itemCode !== itemCode)
}

function addOption(item: SchemaItemDTO) {
  item.options.push(createOption())
}

function addDimension() {
  schema.value.dimensions.push(createDimension())
}

function addBinding(item: SchemaItemDTO) {
  item.dimensionBindings.push(createDimensionBinding())
}

function addRule() {
  schema.value.rules.push(createRule())
}
</script>

<template>
  <NCard :bordered="false" :loading="loading" :title="`结构编辑 · 版本 ${versionId}`">
    <template #header-extra>
      <NSpace>
        <NButton @click="router.push({ name: 'AssessmentVersion' })">返回版本列表</NButton>
        <NButton :loading="saving" @click="handleSave">保存草稿</NButton>
        <NButton :loading="actionLoading" type="primary" @click="handleCompile">编译</NButton>
        <NButton :loading="actionLoading" type="warning" @click="handleValidate">校验</NButton>
      </NSpace>
    </template>

    <NAlert v-if="schema.compileError" :show-icon="false" class="mb-4" type="error">
      最近一次编译错误：{{ schema.compileError }}
    </NAlert>

    <NSpace class="mb-4">
      <NTag>状态：{{ schema.status ?? 'draft' }}</NTag>
      <NTag type="info">schema_hash：{{ schema.schemaHash || '-' }}</NTag>
      <NTag type="info">dsl_hash：{{ schema.dslHash || '-' }}</NTag>
    </NSpace>

    <NTabs animated type="line">
      <NTabPane name="basic" tab="基本信息">
        <NCard size="small">
          <NSpace vertical>
            <NText depth="3">这部分只维护结构主数据，真正发布仍然要先编译再校验。</NText>
            <NSelect v-model:value="schema.assessmentType" :options="assessmentTypeOptions"
                     placeholder="测评类型"/>
            <NSelect v-model:value="schema.randomStrategy" :options="randomStrategyOptions"
                     placeholder="全局随机策略"/>
            <NInput v-model:value="schema.description" :rows="4" placeholder="结构说明"
                    type="textarea"/>
          </NSpace>
        </NCard>
      </NTabPane>

      <NTabPane name="demographic" tab="人口学前置">
        <NSpace class="mb-4" justify="space-between">
          <NText depth="3">人口学题固定落在 `DEMOGRAPHIC` 章节，后端会阻止其参与乱序和计分。</NText>
          <NButton secondary type="primary" @click="addDemographicItem">新增人口学题</NButton>
        </NSpace>

        <NCard
          v-for="item in demographicSection.items"
          :key="item.itemCode"
          :title="item.itemCode"
          class="schema-block"
          size="small"
        >
          <NSpace vertical>
            <NSpace>
              <NInput v-model:value="item.itemCode" placeholder="题目编码"/>
              <NInput v-model:value="item.demographicField"
                      placeholder="人口学字段，如 gender/age/region"/>
              <NButton quaternary type="error"
                       @click="removeItem(demographicSection, item.itemCode)">删除
              </NButton>
            </NSpace>
            <NSelect v-model:value="item.itemType" :options="itemTypeOptions" placeholder="题型"/>
            <NInput v-model:value="item.stem" :rows="2" placeholder="题干" type="textarea"/>
            <NInput v-model:value="item.helpText" placeholder="帮助文案"/>
            <NSpace>
              <NButton secondary size="small" @click="addOption(item)">新增选项</NButton>
            </NSpace>
            <NSpace v-for="option in item.options" :key="option.optionCode" align="center">
              <NInput v-model:value="option.optionCode" placeholder="选项编码"/>
              <NInput v-model:value="option.displayText" placeholder="选项文本"/>
              <NInput v-model:value="option.rawValue" placeholder="原始值"/>
            </NSpace>
          </NSpace>
        </NCard>
      </NTabPane>

      <NTabPane name="sections" tab="正式题">
        <NSpace class="mb-4" justify="space-between">
          <NText depth="3">正式题章节可以配置分页、乱序、锚题和多维映射。</NText>
          <NButton secondary type="primary" @click="addSection">新增章节</NButton>
        </NSpace>

        <NCard
          v-for="section in regularSections"
          :key="section.sectionCode"
          :title="section.sectionTitle || section.sectionCode"
          class="schema-block"
        >
          <NSpace vertical>
            <NSpace>
              <NInput v-model:value="section.sectionCode" placeholder="章节编码"/>
              <NInput v-model:value="section.sectionTitle" placeholder="章节标题"/>
              <NButton quaternary type="error" @click="removeSection(section.sectionCode)">
                删除章节
              </NButton>
            </NSpace>
            <NInput v-model:value="section.sectionDesc" :rows="2" placeholder="章节说明"
                    type="textarea"/>
            <NSpace>
              <NSelect v-model:value="section.displayMode"
                       :options="[{ label: '分页', value: 'paged' }, { label: '连续', value: 'continuous' }]"/>
              <NSelect v-model:value="section.randomStrategy" :options="randomStrategyOptions"/>
              <NSpace align="center">
                <NText depth="3">必做章节</NText>
                <NSwitch v-model:value="section.isRequired"/>
              </NSpace>
            </NSpace>
            <NButton secondary size="small" @click="addItem(section)">新增题目</NButton>

            <NCard v-for="item in section.items" :key="item.itemCode" embedded size="small">
              <NSpace vertical>
                <NSpace>
                  <NInput v-model:value="item.itemCode" placeholder="题目编码"/>
                  <NSelect v-model:value="item.itemType" :options="itemTypeOptions"
                           placeholder="题型"/>
                  <NButton quaternary type="error" @click="removeItem(section, item.itemCode)">
                    删除题目
                  </NButton>
                </NSpace>
                <NInput v-model:value="item.stem" :rows="2" placeholder="题干" type="textarea"/>
                <NInput v-model:value="item.helpText" placeholder="帮助文案"/>
                <NSpace>
                  <NSpace align="center">
                    <NText depth="3">必答</NText>
                    <NSwitch v-model:value="item.isRequired"/>
                  </NSpace>
                  <NSpace align="center">
                    <NText depth="3">计分</NText>
                    <NSwitch v-model:value="item.isScored"/>
                  </NSpace>
                  <NSpace align="center">
                    <NText depth="3">锚题</NText>
                    <NSwitch v-model:value="item.isAnchor"/>
                  </NSpace>
                  <NSpace align="center">
                    <NText depth="3">反向题</NText>
                    <NSwitch v-model:value="item.isReverse"/>
                  </NSpace>
                </NSpace>

                <NSpace>
                  <NInputNumber v-model:value="item.minScore" placeholder="最小分"/>
                  <NInputNumber v-model:value="item.maxScore" placeholder="最大分"/>
                  <NInputNumber v-model:value="item.timeLimitSeconds" placeholder="单题限时（秒）"/>
                </NSpace>

                <NDivider title-placement="left">选项</NDivider>
                <NButton secondary size="small" @click="addOption(item)">新增选项</NButton>
                <NSpace v-for="option in item.options" :key="option.optionCode" align="center">
                  <NInput v-model:value="option.optionCode" placeholder="选项编码"/>
                  <NInput v-model:value="option.displayText" placeholder="选项文本"/>
                  <NInputNumber v-model:value="option.scoreValue" placeholder="正向分值"/>
                  <NInputNumber v-model:value="option.scoreReverseValue" placeholder="反向分值"/>
                </NSpace>

                <NDivider title-placement="left">维度映射</NDivider>
                <NButton secondary size="small" @click="addBinding(item)">新增映射</NButton>
                <NSpace v-for="(binding, index) in item.dimensionBindings"
                        :key="`${item.itemCode}-${index}`" align="center">
                  <NSelect v-model:value="binding.dimensionCode"
                           :options="schema.dimensions.map(d => ({ label: d.dimensionName, value: d.dimensionCode }))"
                           placeholder="维度"/>
                  <NInputNumber v-model:value="binding.weight" placeholder="权重"/>
                  <NSelect v-model:value="binding.scoreMode" :options="scoreModeOptions"
                           clearable placeholder="计分模式覆盖"/>
                  <NSelect v-model:value="binding.reverseMode" :options="reverseModeOptions"
                           clearable placeholder="反向模式覆盖"/>
                </NSpace>
              </NSpace>
            </NCard>
          </NSpace>
        </NCard>
      </NTabPane>

      <NTabPane name="dimensions" tab="维度与分量表">
        <NSpace class="mb-4" justify="space-between">
          <NText depth="3">一个题目可以映射到多个维度，计分模式和反向模式可在映射层覆盖。</NText>
          <NButton secondary type="primary" @click="addDimension">新增维度</NButton>
        </NSpace>

        <NCard v-for="dimension in schema.dimensions" :key="dimension.dimensionCode" class="schema-block"
               size="small">
          <NSpace vertical>
            <NSpace>
              <NInput v-model:value="dimension.dimensionCode" placeholder="维度编码"/>
              <NInput v-model:value="dimension.dimensionName" placeholder="维度名称"/>
            </NSpace>
            <NInput v-model:value="dimension.parentDimensionCode" placeholder="父维度编码（可选）"/>
            <NSelect v-model:value="dimension.scoreMode" :options="scoreModeOptions"
                     placeholder="计分模式"/>
            <NInput v-model:value="dimension.scoreDsl" :rows="2" placeholder="自定义 DSL（当计分模式为 custom_dsl 时生效）"
                    type="textarea"/>
            <NInput v-model:value="dimension.dimensionDesc" placeholder="维度说明"/>
          </NSpace>
        </NCard>
      </NTabPane>

      <NTabPane name="rules" tab="规则">
        <NSpace class="mb-4" justify="space-between">
          <NText depth="3">
            当前先用结构化规则行承载展示、校验、终止等逻辑，正式执行仍以后端编译结果为准。
          </NText>
          <NButton secondary type="primary" @click="addRule">新增规则</NButton>
        </NSpace>

        <NCard v-for="rule in schema.rules" :key="rule.ruleCode" class="schema-block" size="small">
          <NSpace vertical>
            <NSpace>
              <NInput v-model:value="rule.ruleCode" placeholder="规则编码"/>
              <NInput v-model:value="rule.ruleName" placeholder="规则名称"/>
              <NSpace align="center">
                <NText depth="3">启用</NText>
                <NSwitch v-model:value="rule.isActive"/>
              </NSpace>
            </NSpace>
            <NSpace>
              <NInput v-model:value="rule.ruleType" placeholder="规则类型"/>
              <NInput v-model:value="rule.targetType" placeholder="目标类型"/>
              <NInput v-model:value="rule.targetCode" placeholder="目标编码"/>
              <NInputNumber v-model:value="rule.priority" placeholder="优先级"/>
            </NSpace>
            <NInput v-model:value="rule.dslSource" :rows="3" placeholder="规则 DSL / 表达式"
                    type="textarea"/>
          </NSpace>
        </NCard>
      </NTabPane>

      <NTabPane name="validation" tab="校验结果">
        <NAlert v-if="!validationReport" :show-icon="false" type="info">
          还没有执行校验。点击右上角“校验”后，结果会显示在这里。
        </NAlert>
        <template v-else>
          <NSpace class="mb-4">
            <NTag :type="validationReport.valid ? 'success' : 'error'">
              {{ validationReport.valid ? '可发布' : '存在阻塞项' }}
            </NTag>
            <NTag type="error">错误 {{ validationReport.errorCount }}</NTag>
            <NTag type="warning">警告 {{ validationReport.warningCount }}</NTag>
          </NSpace>
          <NCard
            v-for="issue in validationReport.issues"
            :key="`${issue.code}-${issue.targetCode}`"
            class="schema-block"
            size="small"
          >
            <NSpace justify="space-between">
              <NSpace>
                <NTag :type="issue.severity === 'ERROR' ? 'error' : 'warning'">{{
                    issue.severity
                  }}
                </NTag>
                <NText strong>{{ issue.code }}</NText>
              </NSpace>
              <NText depth="3">{{ issue.targetType }} / {{ issue.targetCode }}</NText>
            </NSpace>
            <div class="issue-message">{{ issue.message }}</div>
          </NCard>
        </template>
      </NTabPane>

      <NTabPane name="expert" tab="专家模式">
        <NSpace class="mb-4" justify="space-between">
          <NText depth="3">这里直接编辑 DSL/JSON 导入导出内容。当前导入支持导出的 DSL JSON 和
            canonical schema JSON。
          </NText>
          <NSpace>
            <NButton :loading="actionLoading" @click="handleExportDsl">刷新导出</NButton>
            <NButton :loading="actionLoading" type="primary" @click="handleImportDsl">
              导入到结构编辑器
            </NButton>
          </NSpace>
        </NSpace>
        <NInput v-model:value="expertDsl" :rows="26" placeholder="在这里编辑专家模式 DSL / JSON"
                type="textarea"/>
      </NTabPane>
    </NTabs>
  </NCard>
</template>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.schema-block {
  margin-bottom: 16px;
}

.issue-message {
  margin-top: 10px;
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
