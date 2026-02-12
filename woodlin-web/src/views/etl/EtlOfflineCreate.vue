<script lang="ts" setup>
import {computed, h, onMounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {
  type DataTableColumns,
  NButton,
  NCard,
  NDataTable,
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
} from '@/api/datasource'

type DatasourceTypeCard = {
  label: string
  value: string
}

const router = useRouter()
const message = useMessage()

const currentStep = ref(1)
const loading = ref(false)
const submitting = ref(false)

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

const step1 = reactive({
  jobName: '',
  sourceProvider: 'SELF_HOSTED',
  sourceType: 'MYSQL',
  sourceNetwork: 'INTERNAL',
  sourceDatasource: '',
  sourceSchema: '',
  sourceTable: '',
  sourceCharset: 'utf8mb4',

  targetProvider: 'SELF_HOSTED',
  targetType: 'MYSQL',
  targetNetwork: 'INTERNAL',
  targetDatasource: '',
  targetSchema: '',
  targetTable: '',
  targetCharset: 'utf8mb4',
})

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

const step3 = reactive({
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
  sourceSchemas.value.map((item) => ({
    label: item.schemaName,
    value: item.schemaName,
  })),
)

const targetSchemaOptions = computed(() =>
  targetSchemas.value.map((item) => ({
    label: item.schemaName,
    value: item.schemaName,
  })),
)

const sourceTableOptions = computed(() =>
  sourceTables.value.map((item) => ({
    label: item.tableName,
    value: item.tableName,
  })),
)

const targetTableOptions = computed(() =>
  targetTables.value.map((item) => ({
    label: item.tableName,
    value: item.tableName,
  })),
)

const incrementalColumnOptions = computed(() =>
  sourceColumns.value.map((item) => ({
    label: `${item.columnName}${item.dataType ? ` (${item.dataType})` : ''}`,
    value: item.columnName,
  })),
)

const filteredSourceTables = computed(() => {
  const key = sourceTableKeyword.value.trim().toLowerCase()
  if (!key) {
    return sourceTables.value
  }
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
        checked: row.tableName === step1.sourceTable,
        onUpdateChecked: (checked: boolean) => {
          if (checked) {
            handleSourceTableSelect(row.tableName)
          }
        },
      }),
  },
  {
    title: '源表',
    key: 'tableName',
    minWidth: 180,
  },
  {
    title: '注释',
    key: 'comment',
    minWidth: 180,
    render: (row) => row.comment || '-',
  },
]

const loadDatasourceList = async () => {
  loading.value = true
  try {
    datasourceList.value = await getDatasourceList()
  } finally {
    loading.value = false
  }
}

const hasDatasourceType = (type: string) => datasourceTypeSet.value.has(type)

const syncTypeFromDatasource = (datasourceCode: string, side: 'source' | 'target') => {
  const selected = datasourceList.value.find((item) => item.datasourceCode === datasourceCode)
  if (!selected) {
    return
  }
  if (side === 'source') {
    step1.sourceType = selected.datasourceType
  } else {
    step1.targetType = selected.datasourceType
  }
}

const loadSchemasAndTables = async (side: 'source' | 'target') => {
  const datasourceCode = side === 'source' ? step1.sourceDatasource : step1.targetDatasource
  if (!datasourceCode) {
    return
  }
  const schemaList = await getDatasourceSchemas(datasourceCode)
  if (side === 'source') {
    sourceSchemas.value = schemaList
    if (sourceSchemaOptions.value.length > 0 && !step1.sourceSchema) {
      step1.sourceSchema = sourceSchemaOptions.value[0].value as string
    }
  } else {
    targetSchemas.value = schemaList
    if (targetSchemaOptions.value.length > 0 && !step1.targetSchema) {
      step1.targetSchema = targetSchemaOptions.value[0].value as string
    }
  }
  await loadTables(side)
}

const loadTables = async (side: 'source' | 'target') => {
  const datasourceCode = side === 'source' ? step1.sourceDatasource : step1.targetDatasource
  if (!datasourceCode) {
    return
  }
  const schemaName = side === 'source' ? step1.sourceSchema : step1.targetSchema
  const tables = await getDatasourceTables(datasourceCode, schemaName || undefined)
  if (side === 'source') {
    sourceTables.value = tables
    if (!tables.find((item) => item.tableName === step1.sourceTable)) {
      step1.sourceTable = ''
    }
  } else {
    targetTables.value = tables
    if (!tables.find((item) => item.tableName === step1.targetTable)) {
      step1.targetTable = ''
    }
  }
}

const loadSourceColumns = async () => {
  if (!step1.sourceDatasource || !step1.sourceTable) {
    sourceColumns.value = []
    step3.incrementalColumn = ''
    return
  }
  sourceColumns.value = await getTableColumns(
    step1.sourceDatasource,
    step1.sourceTable,
    step1.sourceSchema || undefined,
  )
  if (!sourceColumns.value.find((item) => item.columnName === step3.incrementalColumn)) {
    step3.incrementalColumn = ''
  }
}

const handleSourceTypeSelect = (value: string) => {
  step1.sourceType = value
  step1.sourceDatasource = ''
  step1.sourceSchema = ''
  step1.sourceTable = ''
  sourceSchemas.value = []
  sourceTables.value = []
  sourceColumns.value = []
}

const handleTargetTypeSelect = (value: string) => {
  step1.targetType = value
  step1.targetDatasource = ''
  step1.targetSchema = ''
  step1.targetTable = ''
  targetSchemas.value = []
  targetTables.value = []
}

const handleSourceTableSelect = (tableName: string) => {
  step1.sourceTable = tableName
  if (!step1.targetTable) {
    step1.targetTable = tableName
  }
}

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
  if (!step1.sourceTable) {
    message.warning('请选择源表')
    return false
  }
  if (!step1.targetTable) {
    message.warning('请选择目标表')
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
  if (step2.syncMode === 'INCREMENTAL' && !step3.incrementalColumn) {
    message.warning('增量同步需选择增量字段')
    return false
  }
  return true
}

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
    charset: {
      source: step1.sourceCharset,
      target: step1.targetCharset,
    },
  }
  return JSON.stringify(payload)
}

const handleNextStep = async () => {
  if (currentStep.value === 1 && !validateStep1()) {
    return
  }
  if (currentStep.value === 2 && !validateStep2()) {
    return
  }
  if (currentStep.value < 3) {
    currentStep.value += 1
    if (currentStep.value === 3) {
      await loadSourceColumns()
    }
  }
}

const handlePrevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value -= 1
  }
}

const handleSubmit = async () => {
  if (!validateStep1() || !validateStep2() || !validateStep3()) {
    return
  }
  const payload: EtlJob = {
    jobName: step1.jobName.trim(),
    jobGroup: 'OFFLINE_SYNC',
    jobDescription: step2.jobDescription?.trim() || undefined,
    sourceDatasource: step1.sourceDatasource,
    sourceSchema: step1.sourceSchema || undefined,
    sourceTable: step1.sourceTable,
    targetDatasource: step1.targetDatasource,
    targetSchema: step1.targetSchema || undefined,
    targetTable: step1.targetTable,
    syncMode: step2.syncMode,
    incrementalColumn: step2.syncMode === 'INCREMENTAL' ? step3.incrementalColumn : undefined,
    filterCondition: step3.filterCondition?.trim() || undefined,
    batchSize: step2.batchSize,
    cronExpression: step2.cronExpression?.trim() || undefined,
    status: step2.autoStart ? '1' : '0',
    concurrent: step2.allowConcurrent ? '1' : '0',
    retryCount: step2.retryCount,
    retryInterval: step2.retryInterval,
    transformRules: buildTransformRules(),
    remark: step3.remark?.trim() || undefined,
  }

  submitting.value = true
  try {
    await createEtlJob(payload)
    message.success('离线同步任务创建成功')
    router.push('/etl/offline')
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/etl/offline')
}

watch(
  () => step1.sourceDatasource,
  async (value) => {
    step1.sourceSchema = ''
    step1.sourceTable = ''
    sourceSchemas.value = []
    sourceTables.value = []
    sourceColumns.value = []
    if (!value) {
      return
    }
    syncTypeFromDatasource(value, 'source')
    await loadSchemasAndTables('source')
  },
)

watch(
  () => step1.targetDatasource,
  async (value) => {
    step1.targetSchema = ''
    step1.targetTable = ''
    targetSchemas.value = []
    targetTables.value = []
    if (!value) {
      return
    }
    syncTypeFromDatasource(value, 'target')
    await loadSchemasAndTables('target')
  },
)

watch(
  () => step1.sourceSchema,
  async () => {
    if (step1.sourceDatasource) {
      await loadTables('source')
    }
  },
)

watch(
  () => step1.targetSchema,
  async () => {
    if (step1.targetDatasource) {
      await loadTables('target')
    }
  },
)

watch(
  () => step1.sourceTable,
  async (value) => {
    if (!value) {
      sourceColumns.value = []
      step3.incrementalColumn = ''
      return
    }
    await loadSourceColumns()
  },
)

onMounted(async () => {
  await loadDatasourceList()
})
</script>

<template>
  <div class="page-container etl-create-page">
    <n-card :bordered="false" class="wizard-head">
      <div class="wizard-head-row">
        <div class="wizard-head-title">
          <h2>创建离线任务</h2>
          <p>当前先支持离线简化流程，保留现有数据源提取逻辑。</p>
        </div>
        <n-space>
          <n-button @click="loadDatasourceList">
            <template #icon>
              <n-icon>
                <refresh-outline/>
              </n-icon>
            </template>
            刷新数据源
          </n-button>
          <n-button @click="handleBack">
            <template #icon>
              <n-icon>
                <arrow-back-outline/>
              </n-icon>
            </template>
            返回列表
          </n-button>
        </n-space>
      </div>
      <n-steps :current="currentStep" class="wizard-steps" size="small">
        <n-step title="源&目标设置"/>
        <n-step title="功能配置"/>
        <n-step title="表映射过滤"/>
      </n-steps>
    </n-card>

    <n-card v-if="currentStep === 1" :bordered="false" title="步骤1：源表和目标表设置">
      <n-form :show-label="false">
        <n-form-item>
          <n-input v-model:value="step1.jobName" placeholder="任务名称（必填）"/>
        </n-form-item>
      </n-form>

      <n-grid :cols="2" :x-gap="14" item-responsive responsive="screen">
        <n-grid-item>
          <n-card class="side-card" size="small" title="源端设置">
            <n-space :size="12" vertical>
              <div class="field-title">部署类型</div>
              <n-radio-group v-model:value="step1.sourceProvider">
                <n-radio-button v-for="item in cloudOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </n-radio-button>
              </n-radio-group>

              <div class="field-title">源类型选择</div>
              <div class="type-group-list">
                <div v-for="group in datasourceTypeGroups" :key="group.title" class="type-group">
                  <div class="type-group-title">{{ group.title }}</div>
                  <div class="type-grid">
                    <button
                      v-for="item in group.items"
                      :key="item.value"
                      :class="{
                        active: step1.sourceType === item.value,
                        disabled: !hasDatasourceType(item.value),
                      }"
                      :disabled="!hasDatasourceType(item.value)"
                      class="type-item"
                      type="button"
                      @click="handleSourceTypeSelect(item.value)"
                    >
                      {{ item.label }}
                    </button>
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
                  <n-select
                    v-model:value="step1.sourceDatasource"
                    :options="sourceDatasourceOptions"
                    clearable
                    filterable
                    placeholder="请选择源数据源"
                  />
                </n-form-item>
                <n-form-item v-if="sourceSchemaOptions.length > 0" label="源Schema">
                  <n-select
                    v-model:value="step1.sourceSchema"
                    :options="sourceSchemaOptions"
                    clearable
                    filterable
                    placeholder="请选择Schema"
                  />
                </n-form-item>
                <n-form-item label="源表">
                  <n-select
                    v-model:value="step1.sourceTable"
                    :options="sourceTableOptions"
                    clearable
                    filterable
                    placeholder="请选择源表"
                  />
                </n-form-item>
                <n-form-item label="字符集">
                  <n-select
                    v-model:value="step1.sourceCharset"
                    :options="[
                      { label: 'utf8mb4', value: 'utf8mb4' },
                      { label: 'utf8', value: 'utf8' },
                      { label: 'gbk', value: 'gbk' },
                    ]"
                  />
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card class="side-card" size="small" title="目标端设置">
            <n-space :size="12" vertical>
              <div class="field-title">部署类型</div>
              <n-radio-group v-model:value="step1.targetProvider">
                <n-radio-button v-for="item in cloudOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </n-radio-button>
              </n-radio-group>

              <div class="field-title">目标类型选择</div>
              <div class="type-group-list">
                <div v-for="group in datasourceTypeGroups" :key="group.title" class="type-group">
                  <div class="type-group-title">{{ group.title }}</div>
                  <div class="type-grid">
                    <button
                      v-for="item in group.items"
                      :key="item.value"
                      :class="{
                        active: step1.targetType === item.value,
                        disabled: !hasDatasourceType(item.value),
                      }"
                      :disabled="!hasDatasourceType(item.value)"
                      class="type-item"
                      type="button"
                      @click="handleTargetTypeSelect(item.value)"
                    >
                      {{ item.label }}
                    </button>
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
                  <n-select
                    v-model:value="step1.targetDatasource"
                    :options="targetDatasourceOptions"
                    clearable
                    filterable
                    placeholder="请选择目标数据源"
                  />
                </n-form-item>
                <n-form-item v-if="targetSchemaOptions.length > 0" label="目标Schema">
                  <n-select
                    v-model:value="step1.targetSchema"
                    :options="targetSchemaOptions"
                    clearable
                    filterable
                    placeholder="请选择Schema"
                  />
                </n-form-item>
                <n-form-item label="目标表">
                  <n-select
                    v-model:value="step1.targetTable"
                    :options="targetTableOptions"
                    clearable
                    filterable
                    placeholder="请选择目标表"
                  />
                </n-form-item>
                <n-form-item label="字符集">
                  <n-select
                    v-model:value="step1.targetCharset"
                    :options="[
                      { label: 'utf8mb4', value: 'utf8mb4' },
                      { label: 'utf8', value: 'utf8' },
                      { label: 'gbk', value: 'gbk' },
                    ]"
                  />
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <n-card v-else-if="currentStep === 2" :bordered="false" title="步骤2：功能配置">
      <n-space :size="14" vertical>
        <n-form label-placement="left" label-width="120">
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
            <n-input
              v-model:value="step2.jobDescription"
              :autosize="{ minRows: 3, maxRows: 6 }"
              placeholder="描述任务用途和范围"
              type="textarea"
            />
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
          <n-form-item label="Cron表达式">
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
          <n-form-item label="启用参数模板">
            <n-switch v-model:value="step2.useParamTemplate"/>
          </n-form-item>
        </n-form>
      </n-space>
    </n-card>

    <n-card v-else :bordered="false" title="步骤3：表映射与过滤">
      <n-grid :cols="24" :x-gap="14">
        <n-grid-item :span="10">
          <n-card class="mapping-card" size="small" title="源表清单（简化版仅支持单表映射）">
            <n-space :size="10" vertical>
              <n-input v-model:value="sourceTableKeyword" clearable placeholder="筛选源表"/>
              <n-data-table
                :bordered="false"
                :columns="sourceTableColumns"
                :data="filteredSourceTables"
                :max-height="420"
                size="small"
              />
            </n-space>
          </n-card>
        </n-grid-item>
        <n-grid-item :span="14">
          <n-card class="mapping-card" size="small" title="映射配置">
            <n-space :size="14" vertical>
              <n-space align="center">
                <n-tag type="info">源</n-tag>
                <n-text
                >{{
                    step1.sourceDatasource || '-'
                  }}{{ step1.sourceSchema ? `.${step1.sourceSchema}` : '' }}.{{
                    step1.sourceTable || '-'
                  }}
                </n-text
                >
              </n-space>
              <n-space align="center">
                <n-tag type="success">目标</n-tag>
                <n-input
                  v-model:value="step1.targetTable"
                  placeholder="目标表名"
                  style="width: 280px"
                />
              </n-space>

              <n-form label-placement="left" label-width="110">
                <n-form-item v-if="step2.syncMode === 'INCREMENTAL'" label="增量字段">
                  <n-select
                    v-model:value="step3.incrementalColumn"
                    :options="incrementalColumnOptions"
                    clearable
                    filterable
                    placeholder="请选择增量字段"
                  />
                </n-form-item>
                <n-form-item label="过滤条件">
                  <n-input
                    v-model:value="step3.filterCondition"
                    :autosize="{ minRows: 3, maxRows: 5 }"
                    placeholder="可选，例如：update_time >= NOW() - INTERVAL 1 DAY"
                    type="textarea"
                  />
                </n-form-item>
                <n-form-item label="备注">
                  <n-input
                    v-model:value="step3.remark"
                    :autosize="{ minRows: 2, maxRows: 4 }"
                    placeholder="任务备注"
                    type="textarea"
                  />
                </n-form-item>
              </n-form>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <n-card :bordered="false" class="wizard-footer">
      <n-space justify="end">
        <n-button v-if="currentStep > 1" @click="handlePrevStep">上一步</n-button>
        <n-button v-if="currentStep < 3" type="primary" @click="handleNextStep">下一步</n-button>
        <n-button v-else :loading="submitting" type="primary" @click="handleSubmit"
        >创建任务
        </n-button
        >
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
