<script lang="ts" setup>
import {computed, h, onMounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {
  type DataTableColumns,
  NButton,
  NCard,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NIcon,
  NInput,
  NInputNumber,
  NRadio,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NStep,
  NSteps,
  NSwitch,
  NTag,
  NText,
  useMessage,
} from 'naive-ui'
import {ArrowBackOutline, RefreshOutline} from '@vicons/ionicons5'
import {createEtlJob, type EtlJob, type SyncMode} from '@/api/etl'
import {
  type ColumnMetadata,
  type DatasourceConfig,
  getDatasourceList,
  getDatasourceSchemas,
  getDatasourceTables,
  getTableColumns,
  type SchemaMetadata,
  type TableMetadata,
  testDatasource,
} from '@/api/datasource'

type DatasourceTypeCard = {
  label: string
  value: string
}

const router = useRouter()
const message = useMessage()

const TOTAL_STEPS = 5
const currentStep = ref(1)
const loading = ref(false)
const submitting = ref(false)
const sourceConnTesting = ref(false)
const targetConnTesting = ref(false)

const datasourceList = ref<DatasourceConfig[]>([])
const sourceSchemas = ref<SchemaMetadata[]>([])
const targetSchemas = ref<SchemaMetadata[]>([])
const sourceTables = ref<TableMetadata[]>([])
const targetTables = ref<TableMetadata[]>([])
const sourceColumns = ref<ColumnMetadata[]>([])

const sourceTableKeyword = ref('')

const cloudOptions = [
  {label: '自建', value: 'SELF_HOSTED'},
  {label: '阿里云', value: 'ALIYUN'},
  {label: '亚马逊AWS', value: 'AWS'},
  {label: '微软Azure', value: 'AZURE'},
  {label: '华为云', value: 'HUAWEI'},
  {label: '腾讯云', value: 'TENCENT'},
]

const datasourceTypeGroups: Array<{ title: string; items: DatasourceTypeCard[] }> = [
  {
    title: '关系型数据库',
    items: [
      {label: 'MySQL', value: 'MYSQL'},
      {label: 'MariaDB', value: 'MARIADB'},
      {label: 'TiDB', value: 'TIDB'},
      {label: 'PostgreSQL', value: 'POSTGRESQL'},
      {label: 'PolarDB-X', value: 'POLARDB'},
      {label: 'OceanBase', value: 'OCEANBASE'},
      {label: 'OpenGauss', value: 'OPENGAUSS'},
      {label: 'Dameng', value: 'DM'},
      {label: 'DB2', value: 'DB2'},
      {label: 'Oracle', value: 'ORACLE'},
      {label: 'SQLServer', value: 'SQLSERVER'},
      {label: 'KingbaseES', value: 'KINGBASE'},
      {label: 'GaussDB', value: 'GAUSSDB'},
      {label: 'Vastbase', value: 'VASTBASE'},
    ],
  },
  {
    title: '消息与中间件',
    items: [
      {label: 'Kafka', value: 'KAFKA'},
      {label: 'RocketMQ', value: 'ROCKETMQ'},
      {label: 'RabbitMQ', value: 'RABBITMQ'},
      {label: 'AutoMQ', value: 'AUTOMQ'},
      {label: 'MongoDB', value: 'MONGODB'},
      {label: 'Redis', value: 'REDIS'},
      {label: 'ElasticSearch', value: 'ELASTICSEARCH'},
      {label: 'Pulsar', value: 'PULSAR'},
    ],
  },
  {
    title: '分析型与湖仓',
    items: [
      {label: 'Greenplum', value: 'GREENPLUM'},
      {label: 'ClickHouse', value: 'CLICKHOUSE'},
      {label: 'StarRocks', value: 'STARROCKS'},
      {label: 'Doris', value: 'DORIS'},
      {label: 'SelectDB', value: 'SELECTDB'},
      {label: 'Hive', value: 'HIVE'},
      {label: 'Kudu', value: 'KUDU'},
      {label: 'Iceberg', value: 'ICEBERG'},
      {label: 'Paimon', value: 'PAIMON'},
      {label: 'DeltaLake', value: 'DELTALAKE'},
    ],
  },
]

/* =================== Step 1: Source & Target =================== */
const step1 = reactive({
  jobName: '',
  sourceProvider: 'SELF_HOSTED',
  sourceType: 'MYSQL',
  sourceNetwork: 'INTERNAL',
  sourceDatasource: '',
  sourceCharset: 'utf8mb4',

  targetProvider: 'SELF_HOSTED',
  targetType: 'MYSQL',
  targetNetwork: 'INTERNAL',
  targetDatasource: '',
  targetCharset: 'utf8mb4',
})

/* =================== Step 2: Functional Config =================== */
const step2 = reactive({
  taskType: 'INCREMENTAL_SYNC',
  fullInit: true,
  profile: 'BALANCED',
  jobDescription: '',
  syncMode: 'FULL' as SyncMode,
  syncDdl: false,
  validationMode: 'NONE',
  truncateTarget: false,
  recreateTarget: false,
  autoStart: false,
  useParamTemplate: false,
  batchSize: 1000,
  retryCount: 3,
  retryInterval: 60,
  allowConcurrent: false,
  cronExpression: '',
})

/* =================== Step 3: Table & Action Filter =================== */
const step3 = reactive({
  sourceSchema: '',
  sourceTable: '',
  targetSchema: '',
  targetTable: '',
  filterCondition: '',
  incrementalColumn: '',
  remark: '',
})

const datasourceTypeSet = computed(
  () => new Set(datasourceList.value.map((item) => item.datasourceType)),
)

const sourceDatasourceOptions = computed(() => {
  return datasourceList.value
    .filter((item) => !step1.sourceType || item.datasourceType === step1.sourceType)
    .map((item) => ({
      label: `${item.datasourceName} (${item.datasourceCode})`,
      value: item.datasourceCode,
    }))
})

const targetDatasourceOptions = computed(() => {
  return datasourceList.value
    .filter((item) => !step1.targetType || item.datasourceType === step1.targetType)
    .map((item) => ({
      label: `${item.datasourceName} (${item.datasourceCode})`,
      value: item.datasourceCode,
    }))
})

const sourceSchemaOptions = computed(() =>
  sourceSchemas.value.map((item) => ({label: item.schemaName, value: item.schemaName})),
)
const targetSchemaOptions = computed(() =>
  targetSchemas.value.map((item) => ({label: item.schemaName, value: item.schemaName})),
)
const incrementalColumnOptions = computed(() =>
  sourceColumns.value.map((item) => ({
    label: `${item.columnName}${item.dataType ? ` (${item.dataType})` : ''}`,
    value: item.columnName,
  })),
)

const filteredSourceTables = computed(() => {
  const key = sourceTableKeyword.value.trim().toLowerCase()
  if (!key) {return sourceTables.value}
  return sourceTables.value.filter((item) => {
    const content = [item.tableName, item.comment].filter(Boolean).join(' ').toLowerCase()
    return content.includes(key)
  })
})

const sourceTableColumns: DataTableColumns<TableMetadata> = [
  {
    title: '',
    key: 'selected',
    width: 58,
    render: (row) =>
      h(NRadio, {
        checked: row.tableName === step3.sourceTable,
        onUpdateChecked: (checked: boolean) => {
          if (checked) {handleSourceTableSelect(row.tableName)}
        },
      }),
  },
  {title: '源表', key: 'tableName', minWidth: 180},
  {title: '注释', key: 'comment', minWidth: 180, render: (row) => row.comment || '-'},
]

const selectedSourceDatasource = computed(() =>
  datasourceList.value.find((item) => item.datasourceCode === step1.sourceDatasource),
)
const selectedTargetDatasource = computed(() =>
  datasourceList.value.find((item) => item.datasourceCode === step1.targetDatasource),
)

/* =================== Data loading =================== */
const loadDatasourceList = async () => {
  loading.value = true
  try {
    datasourceList.value = await getDatasourceList()
  } finally {
    loading.value = false
  }
}

const hasDatasourceType = (type: string) => datasourceTypeSet.value.has(type)

const handleTestConnection = async (side: 'source' | 'target') => {
  const ds = side === 'source' ? selectedSourceDatasource.value : selectedTargetDatasource.value
  if (!ds) {
    message.warning('请先选择数据源')
    return
  }
  const flagRef = side === 'source' ? sourceConnTesting : targetConnTesting
  flagRef.value = true
  try {
    await testDatasource(ds)
    message.success(`${side === 'source' ? '源' : '目标'}数据源连接成功`)
  } catch {
    message.error(`${side === 'source' ? '源' : '目标'}数据源连接失败`)
  } finally {
    flagRef.value = false
  }
}

const loadSchemasAndTables = async (side: 'source' | 'target') => {
  const datasourceCode = side === 'source' ? step1.sourceDatasource : step1.targetDatasource
  if (!datasourceCode) {return}
  const schemaList = await getDatasourceSchemas(datasourceCode)
  if (side === 'source') {
    sourceSchemas.value = schemaList
    if (sourceSchemaOptions.value.length > 0 && !step3.sourceSchema)
      {step3.sourceSchema = sourceSchemaOptions.value[0].value as string}
  } else {
    targetSchemas.value = schemaList
    if (targetSchemaOptions.value.length > 0 && !step3.targetSchema)
      {step3.targetSchema = targetSchemaOptions.value[0].value as string}
  }
  await loadTables(side)
}

const loadTables = async (side: 'source' | 'target') => {
  const datasourceCode = side === 'source' ? step1.sourceDatasource : step1.targetDatasource
  if (!datasourceCode) {return}
  const schemaName = side === 'source' ? step3.sourceSchema : step3.targetSchema
  const tables = await getDatasourceTables(datasourceCode, schemaName || undefined)
  if (side === 'source') {
    sourceTables.value = tables
    if (!tables.find((item) => item.tableName === step3.sourceTable)) {step3.sourceTable = ''}
  } else {
    targetTables.value = tables
    if (!tables.find((item) => item.tableName === step3.targetTable)) {step3.targetTable = ''}
  }
}

const loadSourceColumns = async () => {
  if (!step1.sourceDatasource || !step3.sourceTable) {
    sourceColumns.value = []
    step3.incrementalColumn = ''
    return
  }
  sourceColumns.value = await getTableColumns(
    step1.sourceDatasource,
    step3.sourceTable,
    step3.sourceSchema || undefined,
  )
  if (!sourceColumns.value.find((item) => item.columnName === step3.incrementalColumn))
    {step3.incrementalColumn = ''}
}

/* =================== Step 1 handlers =================== */
const handleSourceTypeSelect = (value: string) => {
  step1.sourceType = value
  step1.sourceDatasource = ''
}

const handleTargetTypeSelect = (value: string) => {
  step1.targetType = value
  step1.targetDatasource = ''
}

const handleSourceTableSelect = (tableName: string) => {
  step3.sourceTable = tableName
  if (!step3.targetTable) {step3.targetTable = tableName}
}

/* =================== Validation =================== */
const validateStep1 = () => {
  if (!step1.jobName.trim()) {
    message.warning('请先输入任务名称')
    return false
  }
  if (!step1.sourceDatasource) {
    message.warning('请选择源数据源实例')
    return false
  }
  if (!step1.targetDatasource) {
    message.warning('请选择目标数据源实例')
    return false
  }
  return true
}

const validateStep2 = () => {
  if (step2.autoStart && !step2.cronExpression.trim()) {
    message.warning('启用自动启动时请填写 Cron 表达式')
    return false
  }
  return true
}

const validateStep3 = () => {
  if (!step3.sourceTable) {
    message.warning('请选择源表')
    return false
  }
  if (!step3.targetTable) {
    message.warning('请选择目标表')
    return false
  }
  if (step2.syncMode === 'INCREMENTAL' && !step3.incrementalColumn) {
    message.warning('增量同步需选择增量字段')
    return false
  }
  return true
}

/* =================== Build & Submit =================== */
const buildTransformRules = () => {
  const payload = {
    taskType: step2.taskType,
    profile: step2.profile,
    fullInit: step2.fullInit,
    syncDdl: step2.syncDdl,
    validationMode: step2.validationMode,
    truncateTarget: step2.truncateTarget,
    recreateTarget: step2.recreateTarget,
    useParamTemplate: step2.useParamTemplate,
    sourceProvider: step1.sourceProvider,
    targetProvider: step1.targetProvider,
    sourceNetwork: step1.sourceNetwork,
    targetNetwork: step1.targetNetwork,
    charset: {source: step1.sourceCharset, target: step1.targetCharset},
  }
  return JSON.stringify(payload)
}

const handleNextStep = async () => {
  if (currentStep.value === 1 && !validateStep1()) {return}
  if (currentStep.value === 2 && !validateStep2()) {return}
  if (currentStep.value === 3 && !validateStep3()) {return}
  if (currentStep.value < TOTAL_STEPS) {
    currentStep.value += 1
    if (currentStep.value === 3) {
      await Promise.all([loadSchemasAndTables('source'), loadSchemasAndTables('target')])
    }
  }
}

const handlePrevStep = () => {
  if (currentStep.value > 1) {currentStep.value -= 1}
}

const canSubmit = () => validateStep1() && validateStep2() && validateStep3()

const normalizeOptionalText = (value?: string) => value?.trim() || undefined

const buildScheduleConfig = () => ({
  cronExpression: normalizeOptionalText(step2.cronExpression),
  status: step2.autoStart ? '1' : '0',
  concurrent: step2.allowConcurrent ? '1' : '0',
  retryCount: step2.retryCount,
  retryInterval: step2.retryInterval
})

const buildSourceTargetConfig = () => ({
  sourceDatasource: step1.sourceDatasource,
  sourceSchema: step3.sourceSchema || undefined,
  sourceTable: step3.sourceTable,
  targetDatasource: step1.targetDatasource,
  targetSchema: step3.targetSchema || undefined,
  targetTable: step3.targetTable
})

const buildSyncConfig = () => ({
  syncMode: step2.syncMode,
  incrementalColumn: step2.syncMode === 'INCREMENTAL' ? step3.incrementalColumn : undefined,
  filterCondition: normalizeOptionalText(step3.filterCondition),
  batchSize: step2.batchSize,
  transformRules: buildTransformRules()
})

const buildSubmitPayload = (): EtlJob => ({
  jobName: step1.jobName.trim(),
  jobGroup: 'OFFLINE_SYNC',
  jobDescription: normalizeOptionalText(step2.jobDescription),
  ...buildSourceTargetConfig(),
  ...buildSyncConfig(),
  ...buildScheduleConfig(),
  remark: normalizeOptionalText(step3.remark),
})

const handleSubmit = async () => {
  if (!canSubmit()) {return}
  submitting.value = true
  try {
    await createEtlJob(buildSubmitPayload())
    message.success('离线同步任务创建成功')
    router.push('/etl/offline')
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/etl/offline')
}

/* =================== Watchers =================== */
watch(() => step3.sourceSchema, async () => {
  if (step1.sourceDatasource) {await loadTables('source')}
})
watch(() => step3.targetSchema, async () => {
  if (step1.targetDatasource) {await loadTables('target')}
})
watch(() => step3.sourceTable, async (value) => {
  if (!value) {
    sourceColumns.value = []
    step3.incrementalColumn = ''
    return
  }
  await loadSourceColumns()
})

onMounted(async () => {
  await loadDatasourceList()
})
</script>

<template>
  <div class="page-container etl-create-page">
    <!-- ===== Header + Steps ===== -->
    <n-card :bordered="false" class="wizard-head">
      <div class="wizard-head-row">
        <div class="wizard-head-title">
          <h2>创建离线任务</h2>
          <p>按向导流程配置数据源、同步功能、表映射和数据处理规则。</p>
        </div>
        <n-space>
          <n-button @click="loadDatasourceList">
            <template #icon><n-icon><refresh-outline/></n-icon></template>
            刷新数据源
          </n-button>
          <n-button @click="handleBack">
            <template #icon><n-icon><arrow-back-outline/></n-icon></template>
            返回任务管理
          </n-button>
        </n-space>
      </div>
      <n-steps :current="currentStep" class="wizard-steps" size="small">
        <n-step title="源&目标设置"/>
        <n-step title="功能配置"/>
        <n-step title="表&action过滤"/>
        <n-step title="数据处理"/>
        <n-step title="创建确认"/>
      </n-steps>
    </n-card>

    <!-- ===== Step 1: Source & Target ===== -->
    <n-card v-if="currentStep === 1" :bordered="false" title="源库和目标库设置">
      <n-form :show-label="false" style="margin-bottom: 12px">
        <n-form-item>
          <n-input v-model:value="step1.jobName" placeholder="任务名称（必填）" style="max-width: 460px"/>
        </n-form-item>
      </n-form>

      <n-grid :cols="2" :x-gap="14" item-responsive responsive="screen">
        <!-- Source side -->
        <n-grid-item>
          <n-card class="side-card" size="small" title="源端设置">
            <n-space :size="12" vertical>
              <div class="field-title">部署类型</div>
              <n-radio-group v-model:value="step1.sourceProvider">
                <n-radio-button v-for="item in cloudOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </n-radio-button>
              </n-radio-group>
              <div class="field-title">源类型</div>
              <div class="type-group-list">
                <div v-for="group in datasourceTypeGroups" :key="group.title" class="type-group">
                  <div class="type-group-title">{{ group.title }}</div>
                  <div class="type-grid">
                    <button
                      v-for="item in group.items" :key="item.value"
                      :class="{ active: step1.sourceType === item.value, disabled: !hasDatasourceType(item.value) }"
                      :disabled="!hasDatasourceType(item.value)"
                      class="type-item" type="button"
                      @click="handleSourceTypeSelect(item.value)"
                    >{{ item.label }}</button>
                  </div>
                </div>
              </div>
              <n-form label-placement="left" label-width="90">
                <n-form-item label="网络类型">
                  <n-radio-group v-model:value="step1.sourceNetwork">
                    <n-radio value="INTERNAL">内网</n-radio>
                    <n-radio value="EXTERNAL">外网</n-radio>
                  </n-radio-group>
                </n-form-item>
                <n-form-item label="源实例">
                  <n-space>
                    <n-select v-model:value="step1.sourceDatasource" :options="sourceDatasourceOptions"
                      clearable filterable placeholder="请选择数据源" style="min-width: 220px"/>
                    <n-button :loading="sourceConnTesting" :disabled="!step1.sourceDatasource"
                      @click="handleTestConnection('source')">测试连接</n-button>
                  </n-space>
                </n-form-item>
                <n-form-item label="字符集">
                  <n-select v-model:value="step1.sourceCharset"
                    :options="[{ label: 'utf8mb4', value: 'utf8mb4' }, { label: 'utf8', value: 'utf8' }, { label: 'gbk', value: 'gbk' }]"/>
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>
        <!-- Target side -->
        <n-grid-item>
          <n-card class="side-card" size="small" title="目标端设置">
            <n-space :size="12" vertical>
              <div class="field-title">部署类型</div>
              <n-radio-group v-model:value="step1.targetProvider">
                <n-radio-button v-for="item in cloudOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </n-radio-button>
              </n-radio-group>
              <div class="field-title">目标类型</div>
              <div class="type-group-list">
                <div v-for="group in datasourceTypeGroups" :key="group.title" class="type-group">
                  <div class="type-group-title">{{ group.title }}</div>
                  <div class="type-grid">
                    <button
                      v-for="item in group.items" :key="item.value"
                      :class="{ active: step1.targetType === item.value, disabled: !hasDatasourceType(item.value) }"
                      :disabled="!hasDatasourceType(item.value)"
                      class="type-item" type="button"
                      @click="handleTargetTypeSelect(item.value)"
                    >{{ item.label }}</button>
                  </div>
                </div>
              </div>
              <n-form label-placement="left" label-width="90">
                <n-form-item label="网络类型">
                  <n-radio-group v-model:value="step1.targetNetwork">
                    <n-radio value="INTERNAL">内网</n-radio>
                    <n-radio value="EXTERNAL">外网</n-radio>
                  </n-radio-group>
                </n-form-item>
                <n-form-item label="目标实例">
                  <n-space>
                    <n-select v-model:value="step1.targetDatasource" :options="targetDatasourceOptions"
                      clearable filterable placeholder="请选择数据源" style="min-width: 220px"/>
                    <n-button :loading="targetConnTesting" :disabled="!step1.targetDatasource"
                      @click="handleTestConnection('target')">测试连接</n-button>
                  </n-space>
                </n-form-item>
                <n-form-item label="字符集">
                  <n-select v-model:value="step1.targetCharset"
                    :options="[{ label: 'utf8mb4', value: 'utf8mb4' }, { label: 'utf8', value: 'utf8' }, { label: 'gbk', value: 'gbk' }]"/>
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- ===== Step 2: Functional Config ===== -->
    <n-card v-else-if="currentStep === 2" :bordered="false" title="功能配置">
      <n-form label-placement="left" label-width="130">
        <n-form-item label="任务类型">
          <n-radio-group v-model:value="step2.taskType">
            <n-radio-button disabled value="FULL_SYNC">全量迁移</n-radio-button>
            <n-radio-button value="INCREMENTAL_SYNC">增量同步</n-radio-button>
            <n-radio-button disabled value="CHECK_SYNC">校验与订正</n-radio-button>
            <n-radio-button disabled value="STRUCTURE_SYNC">结构迁移</n-radio-button>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="同步模式">
          <n-radio-group v-model:value="step2.syncMode">
            <n-radio value="FULL">全量</n-radio>
            <n-radio value="INCREMENTAL">增量</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="任务规格">
          <n-radio-group v-model:value="step2.profile">
            <n-radio-button value="INCREMENTAL_PLUS">增量增强型</n-radio-button>
            <n-radio-button value="FULL_PLUS">全量增强型</n-radio-button>
            <n-radio-button value="BALANCED">平衡型</n-radio-button>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="全量初始化">
          <n-switch v-model:value="step2.fullInit"/>
        </n-form-item>
        <n-form-item label="任务描述">
          <n-input v-model:value="step2.jobDescription" :autosize="{ minRows: 2, maxRows: 5 }"
            placeholder="描述任务用途和范围" type="textarea"/>
        </n-form-item>
        <n-form-item label="批处理大小">
          <n-input-number v-model:value="step2.batchSize" :max="200000" :min="100" :step="100"/>
        </n-form-item>
        <n-form-item label="失败重试次数">
          <n-input-number v-model:value="step2.retryCount" :max="20" :min="0"/>
        </n-form-item>
        <n-form-item label="重试间隔(秒)">
          <n-input-number v-model:value="step2.retryInterval" :max="3600" :min="1"/>
        </n-form-item>
        <n-form-item label="允许并发执行">
          <n-switch v-model:value="step2.allowConcurrent"/>
        </n-form-item>
        <n-form-item label="自动启动任务">
          <n-switch v-model:value="step2.autoStart"/>
        </n-form-item>
        <n-form-item v-if="step2.autoStart" label="Cron表达式">
          <n-input v-model:value="step2.cronExpression" placeholder="例如：0 0/30 * * * ?"/>
        </n-form-item>
        <n-form-item label="同步DDL">
          <n-radio-group v-model:value="step2.syncDdl">
            <n-radio :value="true">是</n-radio>
            <n-radio :value="false">否</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="数据校验">
          <n-radio-group v-model:value="step2.validationMode">
            <n-radio value="NONE">不开启校验</n-radio>
            <n-radio value="ONCE">开启一次性校验</n-radio>
            <n-radio value="PERIODIC">开启周期性校验</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="全量前清空目标表">
          <n-switch v-model:value="step2.truncateTarget"/>
        </n-form-item>
        <n-form-item label="重建目标表">
          <n-switch v-model:value="step2.recreateTarget"/>
        </n-form-item>
      </n-form>
    </n-card>

    <!-- ===== Step 3: Table & Action Filter ===== -->
    <n-card v-else-if="currentStep === 3" :bordered="false" title="表&action过滤">
      <n-grid :cols="24" :x-gap="14">
        <n-grid-item :span="10">
          <n-card class="mapping-card" size="small" title="源表清单（单表映射）">
            <n-space :size="10" vertical>
              <n-form label-placement="left" label-width="90">
                <n-form-item v-if="sourceSchemaOptions.length > 0" label="Schema">
                  <n-select v-model:value="step3.sourceSchema" :options="sourceSchemaOptions"
                    clearable filterable placeholder="选择Schema"/>
                </n-form-item>
              </n-form>
              <n-input v-model:value="sourceTableKeyword" clearable placeholder="筛选源表"/>
              <n-data-table :bordered="false" :columns="sourceTableColumns"
                :data="filteredSourceTables" :max-height="380" size="small"/>
            </n-space>
          </n-card>
        </n-grid-item>
        <n-grid-item :span="14">
          <n-card class="mapping-card" size="small" title="映射配置">
            <n-space :size="14" vertical>
              <n-space align="center">
                <n-tag type="info">源</n-tag>
                <n-text>{{ step1.sourceDatasource || '-' }}{{ step3.sourceSchema ? `.${step3.sourceSchema}` : '' }}.{{ step3.sourceTable || '-' }}</n-text>
              </n-space>
              <n-space align="center">
                <n-tag type="success">目标</n-tag>
                <n-input v-model:value="step3.targetTable" placeholder="目标表名" style="width: 280px"/>
              </n-space>
              <n-form label-placement="left" label-width="110">
                <n-form-item v-if="targetSchemaOptions.length > 0" label="目标Schema">
                  <n-select v-model:value="step3.targetSchema" :options="targetSchemaOptions"
                    clearable filterable placeholder="选择Schema"/>
                </n-form-item>
                <n-form-item v-if="step2.syncMode === 'INCREMENTAL'" label="增量字段">
                  <n-select v-model:value="step3.incrementalColumn" :options="incrementalColumnOptions"
                    clearable filterable placeholder="请选择增量字段"/>
                </n-form-item>
                <n-form-item label="过滤条件">
                  <n-input v-model:value="step3.filterCondition"
                    :autosize="{ minRows: 3, maxRows: 5 }"
                    placeholder="可选，例如：update_time >= NOW() - INTERVAL 1 DAY" type="textarea"/>
                </n-form-item>
                <n-form-item label="备注">
                  <n-input v-model:value="step3.remark" :autosize="{ minRows: 2, maxRows: 4 }"
                    placeholder="任务备注" type="textarea"/>
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- ===== Step 4: Data Processing (Stub) ===== -->
    <n-card v-else-if="currentStep === 4" :bordered="false" title="数据处理">
      <n-empty description="数据处理规则配置（暂未开放）" style="padding: 80px 0">
        <template #extra>
          <n-text depth="3">后续将支持字段级别的转换算子、数据脱敏、正则替换等数据处理能力，当前版本可直接跳过。</n-text>
        </template>
      </n-empty>
    </n-card>

    <!-- ===== Step 5: Confirmation ===== -->
    <n-card v-else-if="currentStep === 5" :bordered="false" title="创建确认">
      <n-descriptions :column="2" bordered label-placement="left" size="small">
        <n-descriptions-item label="任务名称">{{ step1.jobName }}</n-descriptions-item>
        <n-descriptions-item label="任务类型">{{ step2.taskType === 'INCREMENTAL_SYNC' ? '增量同步' : step2.taskType }}</n-descriptions-item>
        <n-descriptions-item label="源数据源">{{ step1.sourceDatasource }} ({{ step1.sourceType }})</n-descriptions-item>
        <n-descriptions-item label="目标数据源">{{ step1.targetDatasource }} ({{ step1.targetType }})</n-descriptions-item>
        <n-descriptions-item label="源表">{{ step3.sourceSchema ? `${step3.sourceSchema}.` : '' }}{{ step3.sourceTable || '-' }}</n-descriptions-item>
        <n-descriptions-item label="目标表">{{ step3.targetSchema ? `${step3.targetSchema}.` : '' }}{{ step3.targetTable || '-' }}</n-descriptions-item>
        <n-descriptions-item label="同步模式">{{ step2.syncMode === 'INCREMENTAL' ? '增量' : '全量' }}</n-descriptions-item>
        <n-descriptions-item label="增量字段">{{ step3.incrementalColumn || '-' }}</n-descriptions-item>
        <n-descriptions-item label="批处理大小">{{ step2.batchSize }}</n-descriptions-item>
        <n-descriptions-item label="重试次数 / 间隔">{{ step2.retryCount }} 次 / {{ step2.retryInterval }} 秒</n-descriptions-item>
        <n-descriptions-item label="全量初始化">{{ step2.fullInit ? '是' : '否' }}</n-descriptions-item>
        <n-descriptions-item label="清空目标表">{{ step2.truncateTarget ? '是' : '否' }}</n-descriptions-item>
        <n-descriptions-item label="允许并发">{{ step2.allowConcurrent ? '是' : '否' }}</n-descriptions-item>
        <n-descriptions-item label="自动启动">{{ step2.autoStart ? '是' : '否' }}</n-descriptions-item>
        <n-descriptions-item v-if="step2.autoStart" label="Cron 表达式">{{ step2.cronExpression || '-' }}</n-descriptions-item>
        <n-descriptions-item label="数据校验">{{ step2.validationMode === 'NONE' ? '不校验' : step2.validationMode === 'ONCE' ? '一次性' : '周期性' }}</n-descriptions-item>
        <n-descriptions-item label="过滤条件" :span="2">{{ step3.filterCondition || '-' }}</n-descriptions-item>
        <n-descriptions-item label="备注" :span="2">{{ step3.remark || '-' }}</n-descriptions-item>
      </n-descriptions>
    </n-card>

    <!-- ===== Footer ===== -->
    <n-card :bordered="false" class="wizard-footer">
      <n-space justify="center">
        <n-button @click="handleBack">返回任务管理</n-button>
        <n-button v-if="currentStep > 1" @click="handlePrevStep">上一步</n-button>
        <n-button v-if="currentStep < TOTAL_STEPS" type="primary" @click="handleNextStep">下一步</n-button>
        <n-button v-if="currentStep === TOTAL_STEPS" :loading="submitting" type="primary" @click="handleSubmit">
          创建任务
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
  background: rgba(248, 250, 252, 0.1);
}

.side-card {
  background: color-mix(in srgb, var(--bg-color-tertiary) 28%, transparent);
}

.field-title {
  font-weight: 600;
  color: var(--text-color-primary);
}

.type-group-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-group-title {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.type-grid {
  display: grid;
  gap: 6px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.type-item {
  border: 1px solid color-mix(in srgb, var(--border-color-light) 78%, transparent);
  border-radius: 6px;
  background: var(--bg-color);
  color: var(--text-color-secondary);
  padding: 6px 8px;
  text-align: center;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.18s ease;
}

.type-item:hover:not(.disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.type-item.active {
  border-color: var(--primary-color);
  background: color-mix(in srgb, var(--primary-color-light) 40%, #fff);
  color: var(--primary-color);
  font-weight: 600;
}

.type-item.disabled {
  cursor: not-allowed;
  color: var(--text-color-disabled);
  background: color-mix(in srgb, var(--bg-color-tertiary) 40%, #fff);
}

.mapping-card {
  min-height: 520px;
}

.wizard-footer {
  position: sticky;
  bottom: 0;
  z-index: 10;
}

@media (max-width: 1200px) {
  .type-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
