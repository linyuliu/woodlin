<script setup lang="ts">
import { computed, h, onMounted, ref, watch, type VNodeChild } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NCheckbox,
  NCheckboxGroup,
  NDataTable,
  NEmpty,
  NGrid,
  NGridItem,
  NIcon,
  NInput,
  NPopover,
  NSpace,
  NSpin,
  NStatistic,
  NTag,
  NText,
  NTree,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import { ArrowBackOutline, FilterOutline, OptionsOutline, SyncOutline } from '@vicons/ionicons5'
import {
  getDatasourceByCode,
  getDatasourceCacheInfo,
  getDatasourceMetadata,
  getDatasourceSchemas,
  getDatasourceTables,
  getTableColumns,
  refreshDatasourceCache,
  type ColumnMetadata,
  type DatabaseMetadata,
  type DatasourceConfig,
  type MetadataCacheInfo,
  type SchemaMetadata,
  type TableMetadata
} from '@/api/datasource'
import { logger } from '@/utils/logger'

type TreeNode = {
  key: string
  label: string
  children?: TreeNode[]
  isLeaf?: boolean
}

type ColumnFieldKey =
  | 'ordinalPosition'
  | 'columnName'
  | 'dataType'
  | 'columnSize'
  | 'nullable'
  | 'defaultValue'
  | 'javaType'
  | 'comment'

const COLUMN_PREF_STORAGE_KEY = 'datasource.workspace.column.visible'
const DEFAULT_COLUMN_KEYS: ColumnFieldKey[] = [
  'ordinalPosition',
  'columnName',
  'dataType',
  'columnSize',
  'nullable',
  'defaultValue',
  'javaType',
  'comment'
]

const route = useRoute()
const router = useRouter()
const message = useMessage()

const datasourceCode = computed(() => String(route.params.code || '').trim())
const datasource = ref<DatasourceConfig | null>(null)

const metaLoading = ref(false)
const columnsLoading = ref(false)
const cacheLoading = ref(false)
const keyword = ref('')
const tableOnlyWithComment = ref(false)

const metadata = ref<DatabaseMetadata | null>(null)
const schemas = ref<SchemaMetadata[]>([])
const tableMap = ref<Record<string, TableMetadata[]>>({})
const schemaLoadedMap = ref<Record<string, boolean>>({})
const schemaLoadingMap = ref<Record<string, boolean>>({})
const columns = ref<ColumnMetadata[]>([])
const selectedTable = ref<TableMetadata | null>(null)
const selectedTreeKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])
const visibleColumnKeys = ref<ColumnFieldKey[]>([...DEFAULT_COLUMN_KEYS])
const cacheInfoList = ref<MetadataCacheInfo[]>([])

const schemaCount = computed(() => {
  const schemaNames = schemas.value.length > 0 ? schemas.value.map(item => item.schemaName) : ['__default__']
  return schemaNames.length
})
const loadedSchemaCount = computed(() => Object.values(schemaLoadedMap.value).filter(Boolean).length)
const tableCount = computed(() => Object.values(tableMap.value).reduce((total, tables) => total + tables.length, 0))
const lastCacheUpdatedAt = computed(() => {
  if (cacheInfoList.value.length === 0) {
    return null
  }
  return cacheInfoList.value.reduce((max, item) => Math.max(max, item.updatedAt), 0)
})

const treeData = computed<TreeNode[]>(() => {
  const hasSchemas = schemas.value.length > 0
  const schemaNodes = hasSchemas ? schemas.value.map(schema => schema.schemaName) : ['__default__']
  return schemaNodes.map(schemaName => {
    const tableList = tableMap.value[schemaName] || []
    const loaded = schemaLoadedMap.value[schemaName]
    const schemaLabel = schemaName === '__default__' ? 'default' : schemaName
    return {
      key: `schema::${schemaName}`,
      label: loaded ? `${schemaLabel} (${tableList.length})` : `${schemaLabel} (未加载)`,
      isLeaf: false,
      children: loaded
        ? tableList.map(table => ({
            key: `table::${schemaName}::${table.tableName}`,
            label: table.tableName,
            isLeaf: true
          }))
        : undefined
    }
  })
})

const filteredTreeData = computed<TreeNode[]>(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key && !tableOnlyWithComment.value) {
    return treeData.value
  }

  const result: TreeNode[] = []
  treeData.value.forEach(schemaNode => {
    const schemaName = schemaNode.key.split('::')[1] || '__default__'
    if (!schemaLoadedMap.value[schemaName]) {
      result.push({
        key: schemaNode.key,
        label: schemaNode.label,
        isLeaf: false
      })
      return
    }
    const children = (schemaNode.children || []).filter(child => {
      const tableName = child.label.toLowerCase()
      const tableNameFromKey = child.key.split('::').slice(2).join('::')
      const tableInfo = (tableMap.value[schemaName] || []).find(item => item.tableName === tableNameFromKey)
      const comment = tableInfo?.comment?.toLowerCase() || ''
      const matchesKeyword = !key || tableName.includes(key) || comment.includes(key)
      const matchesComment = !tableOnlyWithComment.value || Boolean(tableInfo?.comment?.trim())
      return matchesKeyword && matchesComment
    })
    if (children.length > 0) {
      const schemaLabel = schemaName === '__default__' ? 'default' : schemaName
      result.push({
        key: schemaNode.key,
        label: `${schemaLabel} (${children.length})`,
        isLeaf: false,
        children
      })
    }
  })
  return result
})

const filteredTableCount = computed(() =>
  filteredTreeData.value.reduce((total, schemaNode) => total + (schemaNode.children?.length || 0), 0)
)

const columnColumnDefinitions: Array<{
  key: ColumnFieldKey
  label: string
  column: DataTableColumns<ColumnMetadata>[number]
}> = [
  { key: 'ordinalPosition', label: '序号', column: { title: '#', key: 'ordinalPosition', width: 70 } },
  {
    key: 'columnName',
    label: '字段名',
    column: {
      title: '字段名',
      key: 'columnName',
      minWidth: 180,
      render: row => {
        const tags: VNodeChild[] = [h(NText, null, { default: () => row.columnName })]
        if (row.primaryKey) {
          tags.push(h(NTag, { size: 'tiny', type: 'warning', bordered: false }, { default: () => 'PK' }))
        }
        if (row.autoIncrement) {
          tags.push(h(NTag, { size: 'tiny', type: 'info', bordered: false }, { default: () => 'AI' }))
        }
        return h(NSpace, { size: 6, align: 'center' }, () => tags)
      }
    }
  },
  { key: 'dataType', label: '类型', column: { title: '类型', key: 'dataType', minWidth: 130 } },
  {
    key: 'columnSize',
    label: '长度',
    column: { title: '长度', key: 'columnSize', width: 80, render: row => row.columnSize ?? '-' }
  },
  {
    key: 'nullable',
    label: '可空',
    column: {
      title: '可空',
      key: 'nullable',
      width: 80,
      render: row =>
        h(
          NTag,
          { size: 'small', type: row.nullable ? 'default' : 'error', bordered: false },
          { default: () => (row.nullable ? 'YES' : 'NO') }
        )
    }
  },
  {
    key: 'defaultValue',
    label: '默认值',
    column: {
      title: '默认值',
      key: 'defaultValue',
      minWidth: 150,
      ellipsis: { tooltip: true },
      render: row => row.defaultValue || '-'
    }
  },
  { key: 'javaType', label: 'Java 类型', column: { title: 'Java 类型', key: 'javaType', width: 120, render: row => row.javaType || '-' } },
  { key: 'comment', label: '注释', column: { title: '注释', key: 'comment', minWidth: 220, ellipsis: { tooltip: true }, render: row => row.comment || '-' } }
]

const columnOptions = computed(() =>
  columnColumnDefinitions.map(item => ({
    label: item.label,
    value: item.key
  }))
)
const columnColumns = computed<DataTableColumns<ColumnMetadata>>(() => {
  const selected = new Set(visibleColumnKeys.value)
  return columnColumnDefinitions.filter(item => selected.has(item.key)).map(item => item.column)
})

const restoreColumnPreference = () => {
  const raw = localStorage.getItem(COLUMN_PREF_STORAGE_KEY)
  if (!raw) {
    return
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return
    }
    const allKeys = new Set(columnColumnDefinitions.map(item => item.key))
    const validKeys = parsed.filter((item): item is ColumnFieldKey => typeof item === 'string' && allKeys.has(item as ColumnFieldKey))
    if (validKeys.length > 0) {
      visibleColumnKeys.value = Array.from(new Set(validKeys))
    }
  } catch {
    localStorage.removeItem(COLUMN_PREF_STORAGE_KEY)
  }
}

watch(
  () => visibleColumnKeys.value,
  value => {
    if (value.length === 0) {
      visibleColumnKeys.value = ['columnName']
      return
    }
    localStorage.setItem(COLUMN_PREF_STORAGE_KEY, JSON.stringify(value))
  },
  { deep: true }
)

const formatTime = (timestamp?: number | null) => {
  if (!timestamp) {
    return '-'
  }
  return new Date(timestamp).toLocaleString()
}

const loadDatasourceBasic = async (code: string) => {
  datasource.value = await getDatasourceByCode(code)
}

const refreshCacheInfo = async (code: string) => {
  cacheLoading.value = true
  try {
    cacheInfoList.value = await getDatasourceCacheInfo(code)
  } finally {
    cacheLoading.value = false
  }
}

const ensureSchemaTablesLoaded = async (code: string, schemaName: string, refresh = false) => {
  const requestLabel = `[WORKSPACE][TABLES][${code}][${schemaName}]`
  if (schemaLoadingMap.value[schemaName]) {
    logger.debug(`${requestLabel} skip: schema is already loading`)
    return
  }
  if (schemaLoadedMap.value[schemaName] && !refresh) {
    logger.debug(`${requestLabel} skip: schema already loaded`)
    return
  }
  logger.info(`${requestLabel} start`, { refresh })
  schemaLoadingMap.value = {
    ...schemaLoadingMap.value,
    [schemaName]: true
  }
  try {
    const targetSchema = schemaName === '__default__' ? undefined : schemaName
    const tables = await getDatasourceTables(code, targetSchema, refresh)
    logger.info(`${requestLabel} end`, { tableCount: tables?.length || 0 })
    tableMap.value = {
      ...tableMap.value,
      [schemaName]: tables || []
    }
    schemaLoadedMap.value = {
      ...schemaLoadedMap.value,
      [schemaName]: true
    }
  } catch (error) {
    logger.error(`${requestLabel} failed`, error)
    message.error(`加载 Schema [${schemaName}] 表列表失败`)
  } finally {
    schemaLoadingMap.value = {
      ...schemaLoadingMap.value,
      [schemaName]: false
    }
  }
}

const loadColumnsForTable = async (code: string, schemaName: string, tableName: string, refresh = false) => {
  const requestLabel = `[WORKSPACE][COLUMNS][${code}][${schemaName}][${tableName}]`
  logger.info(`${requestLabel} start`, { refresh })
  columnsLoading.value = true
  try {
    const targetSchema = schemaName === '__default__' ? undefined : schemaName
    columns.value = await getTableColumns(code, tableName, targetSchema, refresh)
    logger.info(`${requestLabel} end`, { columnCount: columns.value.length })
    selectedTable.value = (tableMap.value[schemaName] || []).find(item => item.tableName === tableName) || null
  } catch (error) {
    logger.error(`${requestLabel} failed`, error)
    columns.value = []
    message.error(`加载表 [${tableName}] 字段失败`)
  } finally {
    columnsLoading.value = false
  }
}

const autoSelectFirstTable = async (code: string) => {
  const firstSchemaWithTable = Object.keys(tableMap.value).find(
    schemaName => schemaLoadedMap.value[schemaName] && (tableMap.value[schemaName]?.length || 0) > 0
  )
  if (!firstSchemaWithTable) {
    return
  }
  const firstTable = tableMap.value[firstSchemaWithTable][0]
  const treeKey = `table::${firstSchemaWithTable}::${firstTable.tableName}`
  selectedTreeKeys.value = [treeKey]
  await loadColumnsForTable(code, firstSchemaWithTable, firstTable.tableName)
}

const loadWorkspace = async (code: string, refresh = false) => {
  const requestLabel = `[WORKSPACE][INIT][${code}]`
  if (!code) {
    return
  }
  logger.info(`${requestLabel} start`, { refresh })
  metaLoading.value = true
  columns.value = []
  selectedTable.value = null
  selectedTreeKeys.value = []
  expandedKeys.value = []
  tableMap.value = {}
  schemaLoadedMap.value = {}
  schemaLoadingMap.value = {}
  keyword.value = ''
  tableOnlyWithComment.value = false

  try {
    await loadDatasourceBasic(code)
    const [metaData, schemaData] = await Promise.all([
      getDatasourceMetadata(code, refresh),
      getDatasourceSchemas(code, refresh).catch(() => [])
    ])
    metadata.value = metaData
    schemas.value = schemaData || []

    const schemaNames = schemas.value.length > 0 ? schemas.value.map(item => item.schemaName) : ['__default__']
    tableMap.value = schemaNames.reduce<Record<string, TableMetadata[]>>((acc, schemaName) => {
      acc[schemaName] = []
      return acc
    }, {})
    schemaLoadedMap.value = schemaNames.reduce<Record<string, boolean>>((acc, schemaName) => {
      acc[schemaName] = false
      return acc
    }, {})

    const firstSchema = schemaNames[0]
    if (firstSchema) {
      expandedKeys.value = [`schema::${firstSchema}`]
      await ensureSchemaTablesLoaded(code, firstSchema, refresh)
      await autoSelectFirstTable(code)
    }
    await refreshCacheInfo(code)
    logger.info(`${requestLabel} end`, {
      schemaCount: schemaNames.length,
      loadedSchemaCount: Object.values(schemaLoadedMap.value).filter(Boolean).length,
      tableCount: Object.values(tableMap.value).reduce((total, tables) => total + tables.length, 0)
    })
  } catch (error) {
    logger.error(`${requestLabel} failed`, error)
    message.error('加载数据源工作台失败，请查看控制台日志')
  } finally {
    metaLoading.value = false
  }
}

const handleTreeSelect = async (keys: Array<string | number>) => {
  selectedTreeKeys.value = keys.map(item => String(item))
  if (!keys.length || !datasourceCode.value) {
    return
  }
  const key = String(keys[0])
  if (key.startsWith('schema::')) {
    const schemaName = key.split('::')[1]
    if (!schemaName) {
      return
    }
    if (!expandedKeys.value.includes(key)) {
      expandedKeys.value = [...expandedKeys.value, key]
    }
    await ensureSchemaTablesLoaded(datasourceCode.value, schemaName)
    return
  }
  if (!key.startsWith('table::')) {
    return
  }
  const [, schemaName, ...rest] = key.split('::')
  const tableName = rest.join('::')
  if (!schemaName || !tableName) {
    return
  }
  await loadColumnsForTable(datasourceCode.value, schemaName, tableName)
}

const handleExpandedKeysUpdate = (keys: Array<string | number>) => {
  expandedKeys.value = keys.map(item => String(item))
}

const handleTreeLoad = async (node: unknown) => {
  if (!datasourceCode.value) {
    return
  }
  const treeNode = node as TreeNode
  if (!treeNode.key.startsWith('schema::')) {
    return
  }
  const schemaName = treeNode.key.split('::')[1]
  if (!schemaName) {
    return
  }
  await ensureSchemaTablesLoaded(datasourceCode.value, schemaName)
}

const handleBack = () => {
  router.push('/datasource/list')
}

const handleRefreshWorkspace = async () => {
  if (!datasourceCode.value) {
    return
  }
  await refreshDatasourceCache(datasourceCode.value)
  await loadWorkspace(datasourceCode.value, true)
  message.success('元数据已手动刷新（缓存已清空）')
}

watch(
  () => datasourceCode.value,
  async code => {
    if (!code) {
      message.warning('缺少数据源编码')
      return
    }
    await loadWorkspace(code)
  },
  { immediate: true }
)

onMounted(() => {
  restoreColumnPreference()
})
</script>

<template>
  <div class="datasource-workspace-page">
    <n-card class="hero-card" :bordered="false">
      <div class="hero-content">
        <div>
          <n-space align="center" :size="8">
            <n-button quaternary circle @click="handleBack">
              <template #icon>
                <n-icon><arrow-back-outline /></n-icon>
              </template>
            </n-button>
            <h2>{{ datasource?.datasourceName || datasourceCode }}</h2>
            <n-tag size="small" type="info">{{ datasource?.datasourceType || '-' }}</n-tag>
          </n-space>
          <p>按层级加载元数据（Schema -> 表 -> 字段），缓存30分钟，可手动刷新。</p>
        </div>
        <n-space>
          <n-tag size="small" type="success">缓存TTL 30分钟</n-tag>
          <n-tag size="small" type="default">最近更新：{{ formatTime(lastCacheUpdatedAt) }}</n-tag>
          <n-button type="primary" @click="handleRefreshWorkspace">
            <template #icon>
              <n-icon><sync-outline /></n-icon>
            </template>
            手动刷新
          </n-button>
        </n-space>
      </div>
    </n-card>

    <n-grid :x-gap="12" :y-gap="12" cols="1 s:3" responsive="screen">
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="Schema 总数" :value="schemaCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="已加载 Schema" :value="loadedSchemaCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="已加载表数" :value="tableCount" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-grid :x-gap="12" :y-gap="12" cols="1 l:24" responsive="screen">
      <n-grid-item :span="9">
        <n-card title="Schema / 表" :bordered="false" class="panel-card">
          <template #header-extra>
            <n-space size="small">
              <n-tag size="small">展示 {{ filteredTableCount }} / {{ tableCount }}</n-tag>
              <n-tag size="small" type="default">缓存项 {{ cacheInfoList.length }}</n-tag>
            </n-space>
          </template>

          <n-spin :show="metaLoading || cacheLoading">
            <n-space vertical size="small" class="meta-summary">
              <n-text depth="3">数据库：{{ metadata?.databaseName || '-' }}</n-text>
              <n-text depth="3">版本：{{ metadata?.databaseProductVersion || '-' }}</n-text>
              <n-text depth="3">字符集：{{ metadata?.charset || '-' }}</n-text>
              <n-text depth="3">排序规则：{{ metadata?.collation || '-' }}</n-text>
            </n-space>

            <div class="tree-tools">
              <n-input v-model:value="keyword" clearable size="small" placeholder="筛选表名/注释">
                <template #prefix>
                  <n-icon><filter-outline /></n-icon>
                </template>
              </n-input>
              <n-checkbox v-model:checked="tableOnlyWithComment">仅看有注释</n-checkbox>
            </div>

            <n-alert
              v-if="(keyword.trim().length > 0 || tableOnlyWithComment) && loadedSchemaCount < schemaCount"
              :bordered="false"
              type="info"
              title="筛选只作用于已加载Schema，展开节点会按需继续加载。"
            />

            <n-empty v-if="filteredTreeData.length === 0" size="small" description="当前筛选条件下无匹配表" />
            <n-tree
              v-else
              block-line
              :data="filteredTreeData"
              :selected-keys="selectedTreeKeys"
              :expanded-keys="expandedKeys"
              :on-load="handleTreeLoad"
              @update:expanded-keys="handleExpandedKeysUpdate"
              @update:selected-keys="handleTreeSelect"
            />
          </n-spin>
        </n-card>
      </n-grid-item>

      <n-grid-item :span="15">
        <n-card title="字段详情" :bordered="false" class="panel-card">
          <template #header-extra>
            <n-space size="small">
              <n-popover trigger="click" placement="bottom-end">
                <template #trigger>
                  <n-button tertiary size="small">
                    <template #icon>
                      <n-icon><options-outline /></n-icon>
                    </template>
                    列设置
                  </n-button>
                </template>
                <div class="column-config">
                  <n-text depth="3">字段详情列配置（自动保存）</n-text>
                  <n-checkbox-group v-model:value="visibleColumnKeys">
                    <n-space vertical size="small">
                      <n-checkbox v-for="item in columnOptions" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </n-checkbox>
                    </n-space>
                  </n-checkbox-group>
                </div>
              </n-popover>
              <n-tag type="warning" size="small" v-if="selectedTable?.tableType">{{ selectedTable?.tableType }}</n-tag>
              <n-tag type="info" size="small" v-if="selectedTable?.primaryKey">PK: {{ selectedTable?.primaryKey }}</n-tag>
            </n-space>
          </template>

          <n-alert
            v-if="!selectedTable"
            type="info"
            :bordered="false"
            title="请选择左侧一张表来查看字段信息"
          />
          <template v-else>
            <div class="table-brief">
              <n-text strong>{{ selectedTable.tableName }}</n-text>
              <n-text depth="3">{{ selectedTable.comment || '暂无表注释' }}</n-text>
            </div>
            <n-data-table
              :columns="columnColumns"
              :data="columns"
              :loading="columnsLoading"
              :pagination="{ pageSize: 12 }"
              size="small"
              max-height="580"
            />
          </template>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<style scoped>
.datasource-workspace-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #0a4d68 0%, #088395 52%, #05bfdb 100%);
  color: var(--text-color-inverse);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hero-content h2 {
  margin: 0;
  color: var(--text-color-inverse);
  font-size: 22px;
}

.hero-content p {
  margin: 8px 0 0;
  color: color-mix(in srgb, var(--text-color-inverse) 82%, transparent);
}

.stat-card {
  background: radial-gradient(circle at top right, rgba(5, 191, 219, 0.24), transparent 48%), var(--bg-color);
}

.panel-card {
  min-height: 440px;
}

.meta-summary {
  margin-bottom: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--bg-color-hover) 42%, transparent);
}

.tree-tools {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.column-config {
  min-width: 180px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.table-brief {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--bg-color-hover) 36%, transparent);
}

@media (max-width: 960px) {
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
