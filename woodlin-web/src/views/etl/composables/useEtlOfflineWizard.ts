import { computed, reactive, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import type { ColumnMetadata } from '@/api/datasource'
import {
  createEtlOfflineJobs,
  getEtlOfflineColumns,
  getEtlOfflineDatasourceOptions,
  getEtlOfflineJobDetail,
  getEtlOfflineTables,
  getEtlOfflineWizardConfig,
  type EtlDatasourceTypeGroup,
  type EtlNullStrategy,
  type EtlOfflineDatasourceOption,
  type EtlOfflineFieldRule,
  type EtlOfflineJobDetail,
  type EtlOfflineJobPayload,
  type EtlOfflineRuntimeConfig,
  type EtlOfflineScheduleConfig,
  type EtlOfflineTableMapping,
  type EtlOfflineValidationResult,
  type EtlTransformType,
  type EtlOfflineWizardConfig,
  updateEtlOfflineJob,
  validateEtlOfflineJob
} from '@/api/etl'
import type { EtlSelectableTable, EtlSelectOption } from '@/views/etl/types'

const TOTAL_STEPS = 5
const RELATIONAL_GROUP_CODE = 'RELATIONAL'

/**
 * 构建表映射唯一键
 */
function buildMappingKey(schemaName?: string, tableName?: string) {
  return `${schemaName || '__default__'}::${tableName || ''}`
}

/**
 * 清理可选文本
 */
function normalizeText(value?: string) {
  const text = value?.trim()
  return text ? text : undefined
}

/**
 * 判断数据源是否为当前版本可用类型
 */
function isRelationalGroup(groupCode?: string) {
  return groupCode === RELATIONAL_GROUP_CODE
}

/**
 * 构建字段选项
 */
function buildColumnOptions(columns: ColumnMetadata[]): EtlSelectOption[] {
  return columns.map((column) => ({
    label: `${column.columnName}${column.dataType ? ` (${column.dataType})` : ''}`,
    value: column.columnName
  }))
}

/**
 * 构建默认字段规则
 */
function buildDefaultFieldRule(
  column: ColumnMetadata,
  targetSchemaName: string | undefined,
  targetTableName: string,
  targetColumnType?: string
): EtlOfflineFieldRule {
  return {
    enabled: true,
    ordinalPosition: column.ordinalPosition || 0,
    sourceSchemaName: column.schemaName,
    sourceTableName: column.tableName,
    sourceColumnName: column.columnName,
    sourceColumnType: column.dataType,
    targetSchemaName,
    targetTableName,
    targetColumnName: column.columnName,
    targetColumnType,
    valueSource: 'SOURCE_COLUMN',
    nullStrategy: 'KEEP_NULL',
    transformType: 'NONE',
    transformConfig: {}
  }
}

/**
 * 创建默认运行配置
 */
function createDefaultRuntimeConfig(config?: EtlOfflineWizardConfig): EtlOfflineRuntimeConfig {
  return {
    syncMode: config?.defaultSyncMode || 'FULL',
    batchSize: config?.defaultBatchSize || 1000,
    retryCount: config?.defaultRetryCount || 3,
    retryInterval: config?.defaultRetryInterval || 60,
    allowConcurrent: false,
    validationMode: 'NONE',
    truncateTarget: false,
    schemaSyncMode: 'AUTO_ADD_COLUMNS'
  }
}

/**
 * 创建默认调度配置
 */
function createDefaultScheduleConfig(): EtlOfflineScheduleConfig {
  return {
    autoStart: false,
    cronExpression: ''
  }
}

/**
 * 向导状态管理
 */
export function useEtlOfflineWizard() {
  const router = useRouter()
  const route = useRoute()
  const message = useMessage()

  const currentStep = ref(1)
  const submitting = ref(false)
  const loading = ref(false)
  const validating = ref(false)
  const loadingSourceTables = ref(false)
  const loadingFieldRules = ref<Record<string, boolean>>({})
  const sourceTableKeyword = ref('')
  const wizardConfig = ref<EtlOfflineWizardConfig | null>(null)
  const datasourceOptions = ref<EtlOfflineDatasourceOption[]>([])
  const sourceTables = ref<EtlSelectableTable[]>([])
  const validationResult = ref<EtlOfflineValidationResult | null>(null)
  const activeMappingKey = ref('')
  const columnOptionsMap = ref<Record<string, EtlSelectOption[]>>({})

  const form = reactive({
    jobName: '',
    jobGroup: 'OFFLINE_SYNC',
    jobDescription: '',
    sourceDatasourceType: '',
    sourceDatasource: '',
    targetDatasourceType: '',
    targetDatasource: '',
    runtimeConfig: createDefaultRuntimeConfig(),
    scheduleConfig: createDefaultScheduleConfig(),
    tableMappings: [] as EtlOfflineTableMapping[]
  })

  const isEditMode = computed(() => Boolean(route.params.jobId))
  const currentJobId = computed(() => {
    const raw = route.params.jobId
    const jobId = raw ? Number(raw) : 0
    return Number.isNaN(jobId) || jobId <= 0 ? null : jobId
  })

  const datasourceTypeGroups = computed<EtlDatasourceTypeGroup[]>(() => wizardConfig.value?.datasourceTypeGroups || [])

  const sourceDatasourceOptions = computed(() =>
    datasourceOptions.value
      .filter((item) => item.datasourceType === form.sourceDatasourceType)
      .map((item) => ({
        label: `${item.datasourceName} (${item.datasourceCode})`,
        value: item.datasourceCode
      }))
  )

  const targetDatasourceOptions = computed(() =>
    datasourceOptions.value
      .filter((item) => item.datasourceType === form.targetDatasourceType)
      .map((item) => ({
        label: `${item.datasourceName} (${item.datasourceCode})`,
        value: item.datasourceCode
      }))
  )

  const validationModeOptions = computed<EtlSelectOption[]>(() => {
    const options = wizardConfig.value?.validationModes
    if (options?.length) {
      return options
    }
    return [
      { label: '不开启校验', value: 'NONE' },
      { label: '执行后校验', value: 'ONCE' },
      { label: '严格校验', value: 'PERIODIC' }
    ]
  })

  const schemaSyncModeOptions = computed<EtlSelectOption[]>(() => {
    const options = wizardConfig.value?.schemaSyncModes
    if (options?.length) {
      return options
    }
    return [
      { label: '自动补齐目标字段', value: 'AUTO_ADD_COLUMNS' },
      { label: '仅校验不补齐', value: 'NONE' }
    ]
  })

  const transformTypeOptions = computed<EtlSelectOption[]>(() => {
    const options = wizardConfig.value?.transformTypes
    if (options?.length) {
      return options
    }
    return [
      { label: '不处理', value: 'NONE' },
      { label: '去除首尾空格', value: 'TRIM' },
      { label: '转大写', value: 'UPPER' },
      { label: '转小写', value: 'LOWER' },
      { label: '日期格式化', value: 'DATE_FORMAT' },
      { label: '数字转换', value: 'NUMBER_CAST' }
    ]
  })

  const nullStrategyOptions = computed<EtlSelectOption[]>(() => {
    const options = wizardConfig.value?.nullStrategies
    if (options?.length) {
      return options
    }
    return [
      { label: '保留空值', value: 'KEEP_NULL' },
      { label: '转空字符串', value: 'EMPTY_STRING' },
      { label: '转 0', value: 'ZERO' },
      { label: '使用当前时间', value: 'CURRENT_TIME' },
      { label: '跳过字段', value: 'SKIP_FIELD' }
    ]
  })

  const selectedTableKeys = computed(() =>
    form.tableMappings.map((mapping) => buildMappingKey(mapping.sourceSchema, mapping.sourceTable))
  )

  const validationResultMap = computed<Record<string, NonNullable<EtlOfflineValidationResult['tables']>[number]>>(() => {
    const map: Record<string, NonNullable<EtlOfflineValidationResult['tables']>[number]> = {}
    for (const table of validationResult.value?.tables || []) {
      map[buildMappingKey(table.sourceSchema, table.sourceTable)] = table
    }
    return map
  })

  /**
   * 初始化向导
   */
  async function initialize() {
    loading.value = true
    try {
      const [config, datasources] = await Promise.all([
        getEtlOfflineWizardConfig(),
        getEtlOfflineDatasourceOptions({ enabledOnly: false })
      ])
      wizardConfig.value = config
      datasourceOptions.value = datasources
      form.jobGroup = config.defaultJobGroup || 'OFFLINE_SYNC'
      form.runtimeConfig = reactive(createDefaultRuntimeConfig(config))
      form.scheduleConfig = reactive(createDefaultScheduleConfig())
      ensureDatasourceTypeDefaults()

      if (isEditMode.value && currentJobId.value) {
        await loadJobDetail(currentJobId.value)
      } else if (form.sourceDatasource) {
        await loadSourceTables()
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置默认数据源类型
   */
  function ensureDatasourceTypeDefaults() {
    const relationalOptions = datasourceTypeGroups.value
      .filter((group) => isRelationalGroup(group.groupCode))
      .flatMap((group) => group.options)

    if (!form.sourceDatasourceType) {
      form.sourceDatasourceType = relationalOptions.find((item) => item.available)?.datasourceType || relationalOptions[0]?.datasourceType || ''
    }
    if (!form.targetDatasourceType) {
      form.targetDatasourceType = relationalOptions.find((item) => item.available)?.datasourceType || relationalOptions[0]?.datasourceType || ''
    }
  }

  /**
   * 加载编辑详情
   */
  async function loadJobDetail(jobId: number) {
    const detail = await getEtlOfflineJobDetail(jobId)
    applyDetail(detail)
    await loadSourceTables()
    await ensureAllFieldRulesLoaded()
  }

  /**
   * 回填详情数据
   */
  function applyDetail(detail: EtlOfflineJobDetail) {
    form.jobName = detail.jobName
    form.jobGroup = detail.jobGroup || form.jobGroup
    form.jobDescription = detail.jobDescription || ''
    form.sourceDatasource = detail.sourceDatasource
    form.targetDatasource = detail.targetDatasource
    form.sourceDatasourceType = detail.sourceDatasourceType || resolveDatasourceType(detail.sourceDatasource)
    form.targetDatasourceType = detail.targetDatasourceType || resolveDatasourceType(detail.targetDatasource)
    form.runtimeConfig = reactive({
      ...createDefaultRuntimeConfig(wizardConfig.value || undefined),
      ...detail.runtimeConfig
    })
    form.scheduleConfig = reactive({
      ...createDefaultScheduleConfig(),
      ...detail.scheduleConfig
    })
    form.tableMappings = (detail.tableMappings || []).map((mapping) => ({
      ...mapping,
      syncMode: mapping.syncMode || detail.runtimeConfig?.syncMode || form.runtimeConfig.syncMode,
      fieldRules: (mapping.fieldRules || []).map((rule, index) => ({
        ...rule,
        enabled: rule.enabled ?? true,
        ordinalPosition: rule.ordinalPosition || index + 1,
        valueSource: rule.valueSource || 'SOURCE_COLUMN',
        nullStrategy: rule.nullStrategy || 'KEEP_NULL',
        transformType: rule.transformType || 'NONE',
        transformConfig: rule.transformConfig || {}
      }))
    }))

    if (form.tableMappings.length > 0) {
      activeMappingKey.value = buildMappingKey(form.tableMappings[0].sourceSchema, form.tableMappings[0].sourceTable)
    }
  }

  /**
   * 通过数据源编码反查类型
   */
  function resolveDatasourceType(datasourceCode?: string) {
    return datasourceOptions.value.find((item) => item.datasourceCode === datasourceCode)?.datasourceType || ''
  }

  /**
   * 刷新源表列表
   */
  async function loadSourceTables() {
    if (!form.sourceDatasource) {
      sourceTables.value = []
      form.tableMappings = []
      return
    }

    loadingSourceTables.value = true
    try {
      const tables = await getEtlOfflineTables({
        datasourceCode: form.sourceDatasource,
        keyword: normalizeText(sourceTableKeyword.value),
        limit: 1000
      })

      sourceTables.value = tables.map((table) => ({
        ...table,
        mappingKey: buildMappingKey(table.schemaName, table.tableName)
      }))
    } finally {
      loadingSourceTables.value = false
    }
  }

  /**
   * 根据表勾选结果同步映射配置
   */
  function syncSelectedTables(keys: string[], rows: EtlSelectableTable[]) {
    if (isEditMode.value && rows.length > 1) {
      message.warning('编辑模式一次仅允许维护一条单表任务')
      const lastRow = rows[rows.length - 1]
      form.tableMappings = [createMappingFromTable(lastRow)]
      activeMappingKey.value = lastRow.mappingKey
      return
    }

    const selectedKeySet = new Set(keys)
    const selectedRowMap = new Map(rows.map((row) => [row.mappingKey, row]))
    const nextMappings = form.tableMappings
      .filter((mapping) => selectedKeySet.has(buildMappingKey(mapping.sourceSchema, mapping.sourceTable)))
      .map((mapping) => ({
        ...mapping,
        syncMode: form.runtimeConfig.syncMode
      }))

    for (const row of rows) {
      const exists = nextMappings.some((mapping) => buildMappingKey(mapping.sourceSchema, mapping.sourceTable) === row.mappingKey)
      if (!exists) {
        nextMappings.push(createMappingFromTable(row))
      }
    }

    form.tableMappings = nextMappings

    if (!activeMappingKey.value || !selectedKeySet.has(activeMappingKey.value)) {
      activeMappingKey.value = nextMappings[0] ? buildMappingKey(nextMappings[0].sourceSchema, nextMappings[0].sourceTable) : ''
    }

    for (const key of Object.keys(columnOptionsMap.value)) {
      if (!selectedKeySet.has(key) && !selectedRowMap.has(key)) {
        delete columnOptionsMap.value[key]
      }
    }
  }

  /**
   * 创建默认表映射
   */
  function createMappingFromTable(row: EtlSelectableTable): EtlOfflineTableMapping {
    return {
      sourceSchema: row.schemaName,
      sourceTable: row.tableName,
      targetSchema: row.schemaName,
      targetTable: row.tableName,
      syncMode: form.runtimeConfig.syncMode,
      incrementalColumn: '',
      filterCondition: '',
      remark: '',
      fieldRules: []
    }
  }

  /**
   * 移除单条表映射
   */
  function removeMapping(mappingKey: string) {
    form.tableMappings = form.tableMappings.filter(
      (mapping) => buildMappingKey(mapping.sourceSchema, mapping.sourceTable) !== mappingKey
    )
    if (activeMappingKey.value === mappingKey) {
      activeMappingKey.value = form.tableMappings[0]
        ? buildMappingKey(form.tableMappings[0].sourceSchema, form.tableMappings[0].sourceTable)
        : ''
    }
    delete columnOptionsMap.value[mappingKey]
  }

  /**
   * 批量加载字段规则
   */
  async function ensureAllFieldRulesLoaded() {
    await Promise.all(form.tableMappings.map((mapping) => loadFieldRules(mapping)))
  }

  /**
   * 加载单表字段规则
   */
  async function loadFieldRules(mapping: EtlOfflineTableMapping) {
    const mappingKey = buildMappingKey(mapping.sourceSchema, mapping.sourceTable)
    loadingFieldRules.value = {
      ...loadingFieldRules.value,
      [mappingKey]: true
    }

    try {
      const [sourceColumns, targetColumns] = await Promise.all([
        getEtlOfflineColumns({
          datasourceCode: form.sourceDatasource,
          schemaName: normalizeText(mapping.sourceSchema),
          tableName: mapping.sourceTable,
          limit: 1000
        }),
        loadTargetColumnsSafely(mapping)
      ])

      const targetColumnMap = new Map(targetColumns.map((column) => [column.columnName.toLowerCase(), column]))
      const existingRuleMap = new Map(mapping.fieldRules.map((rule) => [rule.sourceColumnName.toLowerCase(), rule]))

      const mergedRules = sourceColumns.map((column, index) => {
        const targetColumn = targetColumnMap.get(column.columnName.toLowerCase())
        const baseRule = buildDefaultFieldRule(column, mapping.targetSchema, mapping.targetTable, targetColumn?.dataType)
        const existRule = existingRuleMap.get(column.columnName.toLowerCase())
        return {
          ...baseRule,
          ...existRule,
          ordinalPosition: existRule?.ordinalPosition || index + 1,
          targetSchemaName: mapping.targetSchema,
          targetTableName: mapping.targetTable
        }
      })

      const extraRules = mapping.fieldRules.filter((rule) => {
        const ruleKey = rule.sourceColumnName.toLowerCase()
        return !sourceColumns.some((column) => column.columnName.toLowerCase() === ruleKey)
      })

      mapping.fieldRules = [...mergedRules, ...extraRules]
      columnOptionsMap.value = {
        ...columnOptionsMap.value,
        [mappingKey]: buildColumnOptions(sourceColumns)
      }
    } finally {
      loadingFieldRules.value = {
        ...loadingFieldRules.value,
        [mappingKey]: false
      }
    }
  }

  /**
   * 安全加载目标字段，目标表不存在时返回空列表。
   */
  async function loadTargetColumnsSafely(mapping: EtlOfflineTableMapping) {
    if (!form.targetDatasource || !mapping.targetTable) {
      return [] as ColumnMetadata[]
    }

    try {
      return await getEtlOfflineColumns({
        datasourceCode: form.targetDatasource,
        schemaName: normalizeText(mapping.targetSchema),
        tableName: mapping.targetTable,
        limit: 1000
      })
    } catch (error) {
      return []
    }
  }

  /**
   * 切换当前编辑的表映射
   */
  async function switchActiveMapping(mappingKey: string) {
    activeMappingKey.value = mappingKey
    const mapping = form.tableMappings.find(
      (item) => buildMappingKey(item.sourceSchema, item.sourceTable) === mappingKey
    )
    if (mapping) {
      await loadFieldRules(mapping)
    }
  }

  /**
   * 构建请求载荷
   */
  function buildPayload(): EtlOfflineJobPayload {
    return {
      jobName: form.jobName.trim(),
      jobGroup: normalizeText(form.jobGroup),
      jobDescription: normalizeText(form.jobDescription),
      sourceDatasource: form.sourceDatasource,
      targetDatasource: form.targetDatasource,
      runtimeConfig: {
        ...form.runtimeConfig
      },
      scheduleConfig: {
        ...form.scheduleConfig,
        cronExpression: normalizeText(form.scheduleConfig.cronExpression)
      },
      tableMappings: form.tableMappings.map((mapping) => ({
        ...mapping,
        targetSchema: normalizeText(mapping.targetSchema),
        targetTable: mapping.targetTable.trim(),
        incrementalColumn: normalizeText(mapping.incrementalColumn),
        filterCondition: normalizeText(mapping.filterCondition),
        remark: normalizeText(mapping.remark),
        fieldRules: mapping.fieldRules.map((rule) => ({
          ...rule,
          targetSchemaName: normalizeText(mapping.targetSchema),
          targetTableName: mapping.targetTable.trim(),
          targetColumnName: rule.targetColumnName.trim(),
          constantValue: normalizeText(rule.constantValue),
          defaultValue: normalizeText(rule.defaultValue)
        }))
      }))
    }
  }

  /**
   * 本地校验步骤一
   */
  function validateStepOne() {
    if (!form.jobName.trim()) {
      message.warning('请输入任务名称')
      return false
    }
    if (!form.sourceDatasourceType || !form.sourceDatasource) {
      message.warning('请选择源数据源类型和实例')
      return false
    }
    if (!form.targetDatasourceType || !form.targetDatasource) {
      message.warning('请选择目标数据源类型和实例')
      return false
    }
    return true
  }

  /**
   * 本地校验步骤二
   */
  function validateStepTwo() {
    if (form.scheduleConfig.autoStart && !normalizeText(form.scheduleConfig.cronExpression)) {
      message.warning('自动启动任务时必须填写 Cron 表达式')
      return false
    }
    return true
  }

  /**
   * 本地校验步骤三
   */
  function validateStepThree() {
    if (form.tableMappings.length === 0) {
      message.warning('请至少选择一张源表')
      return false
    }

    for (const mapping of form.tableMappings) {
      if (!mapping.targetTable.trim()) {
        message.warning(`请填写目标表名：${mapping.sourceTable}`)
        return false
      }
      if (mapping.syncMode === 'INCREMENTAL' && !normalizeText(mapping.incrementalColumn)) {
        message.warning(`请为增量任务选择增量字段：${mapping.sourceTable}`)
        return false
      }
    }

    return true
  }

  /**
   * 本地校验步骤四
   */
  function validateStepFour() {
    for (const mapping of form.tableMappings) {
      const enabledRules = mapping.fieldRules.filter((rule) => rule.enabled)
      if (enabledRules.length === 0) {
        message.warning(`请至少保留一个启用字段：${mapping.sourceTable}`)
        return false
      }

      const targetNames = new Set<string>()
      for (const rule of enabledRules) {
        const targetName = rule.targetColumnName.trim().toLowerCase()
        if (!targetName) {
          message.warning(`字段映射目标列不能为空：${mapping.sourceTable}`)
          return false
        }
        if (targetNames.has(targetName)) {
          message.warning(`目标字段重复：${mapping.sourceTable} -> ${rule.targetColumnName}`)
          return false
        }
        targetNames.add(targetName)

        if (rule.valueSource === 'CONSTANT' && !normalizeText(rule.constantValue)) {
          message.warning(`常量值不能为空：${mapping.sourceTable} -> ${rule.sourceColumnName}`)
          return false
        }

        if (rule.valueSource === 'DEFAULT_VALUE' && !normalizeText(rule.defaultValue)) {
          message.warning(`默认值不能为空：${mapping.sourceTable} -> ${rule.sourceColumnName}`)
          return false
        }
      }
    }

    return true
  }

  /**
   * 执行远程预校验
   */
  async function runRemoteValidation() {
    validating.value = true
    try {
      validationResult.value = await validateEtlOfflineJob(buildPayload())
      return validationResult.value
    } finally {
      validating.value = false
    }
  }

  /**
   * 下一步
   */
  async function goNextStep() {
    if (currentStep.value === 1 && !validateStepOne()) {
      return
    }
    if (currentStep.value === 2 && !validateStepTwo()) {
      return
    }
    if (currentStep.value === 3) {
      if (!validateStepThree()) {
        return
      }
      await ensureAllFieldRulesLoaded()
    }
    if (currentStep.value === 4) {
      if (!validateStepFour()) {
        return
      }
      await runRemoteValidation()
    }

    if (currentStep.value < TOTAL_STEPS) {
      currentStep.value += 1
    }
  }

  /**
   * 上一步
   */
  function goPrevStep() {
    if (currentStep.value > 1) {
      currentStep.value -= 1
    }
  }

  /**
   * 提交任务
   */
  async function submit() {
    if (!validateStepOne() || !validateStepTwo() || !validateStepThree() || !validateStepFour()) {
      return
    }

    const remoteValidation = await runRemoteValidation()
    if (!remoteValidation.valid) {
      message.error('预校验未通过，请先处理页面中的错误项')
      currentStep.value = 5
      return
    }

    const payload = buildPayload()
    submitting.value = true
    try {
      if (isEditMode.value && currentJobId.value) {
        await updateEtlOfflineJob(currentJobId.value, {
          ...payload,
          tableMappings: payload.tableMappings.slice(0, 1)
        })
        message.success('离线任务更新成功')
      } else {
        const response = await createEtlOfflineJobs(payload)
        message.success(`离线任务创建成功，共创建 ${response.createdJobCount || response.createdJobIds.length} 条任务`)
      }

      await router.push('/etl/offline')
    } finally {
      submitting.value = false
    }
  }

  /**
   * 返回列表
   */
  async function backToList() {
    await router.push('/etl/offline')
  }

  watch(
    () => form.sourceDatasourceType,
    () => {
      const currentType = resolveDatasourceType(form.sourceDatasource)
      if (!form.sourceDatasource || currentType === form.sourceDatasourceType) {
        return
      }

      form.sourceDatasource = ''
      sourceTables.value = []
      form.tableMappings = []
      activeMappingKey.value = ''
      columnOptionsMap.value = {}
      validationResult.value = null
    }
  )

  watch(
    () => form.targetDatasourceType,
    () => {
      const currentType = resolveDatasourceType(form.targetDatasource)
      if (!form.targetDatasource || currentType === form.targetDatasourceType) {
        return
      }

      form.targetDatasource = ''
      validationResult.value = null
      form.tableMappings.forEach((mapping) => {
        mapping.fieldRules = []
      })
      columnOptionsMap.value = {}
    }
  )

  watch(
    () => form.runtimeConfig.syncMode,
    (syncMode) => {
      form.tableMappings.forEach((mapping) => {
        mapping.syncMode = syncMode
        if (syncMode !== 'INCREMENTAL') {
          mapping.incrementalColumn = ''
        }
      })
    }
  )

  watch(
    () => form.sourceDatasource,
    async () => {
      sourceTableKeyword.value = ''
      validationResult.value = null
      if (!form.sourceDatasource) {
        sourceTables.value = []
        form.tableMappings = []
        return
      }
      await loadSourceTables()
    }
  )

  watch(
    () => form.targetDatasource,
    () => {
      validationResult.value = null
      form.tableMappings.forEach((mapping) => {
        mapping.fieldRules = []
      })
      columnOptionsMap.value = {}
    }
  )

  return {
    activeMappingKey,
    backToList,
    columnOptionsMap,
    currentStep,
    datasourceOptions,
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
    validationResultMap,
    validating,
    loadSourceTables
  }
}
