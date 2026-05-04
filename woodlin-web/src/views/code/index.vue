<!--
  @file views/code/index.vue
  @description 代码生成：选择数据源 → 表列表 → 预览（多模板 Tabs）/下载 zip/直接导入
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NCode,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPagination,
  NSelect,
  NSpace,
  NSpin,
  NTabPane,
  NTabs,
  useMessage,
  type DataTableColumns,
  type DataTableRowKey,
  type SelectOption,
} from 'naive-ui'
import {
  downloadCode,
  importCode,
  pageTables,
  previewCode,
  type GenConfig,
  type GenTable,
  type GenTableQuery,
  type TemplateFile,
} from '@/api/code'
import { pageDataSources, type DataSource } from '@/api/datasource'
import { downloadBlob } from '@/utils/download'

const message = useMessage()

const dataSources: Ref<DataSource[]> = ref([])
const dataSourceOptions = ref<SelectOption[]>([])
const currentDataSourceId = ref<number | null>(null)

const tableData: Ref<GenTable[]> = ref([])
const loading = ref(false)
const total = ref(0)

const query = reactive<GenTableQuery>({
  page: 1,
  size: 10,
  tableName: '',
  tableComment: '',
  dataSourceId: undefined,
})

const checkedKeys = ref<DataTableRowKey[]>([])

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewFiles = ref<TemplateFile[]>([])
const previewActiveTab = ref<string>('')

async function loadDataSources(): Promise<void> {
  const res = await pageDataSources({ page: 1, size: 100 })
  dataSources.value = res?.records ?? []
  dataSourceOptions.value = dataSources.value
    .filter((d) => d.id !== null && d.id !== undefined)
    .map((d) => ({ label: d.dsName, value: d.id as number }))
  if (!currentDataSourceId.value && dataSourceOptions.value.length > 0) {
    currentDataSourceId.value = dataSourceOptions.value[0].value as number
    query.dataSourceId = currentDataSourceId.value
    void refresh()
  }
}

async function refresh(): Promise<void> {
  if (!currentDataSourceId.value) {
    tableData.value = []
    total.value = 0
    return
  }
  query.dataSourceId = currentDataSourceId.value
  loading.value = true
  try {
    const res = await pageTables(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.page = 1
  void refresh()
}

function handleReset(): void {
  query.tableName = ''
  query.tableComment = ''
  query.page = 1
  void refresh()
}

function handleDataSourceChange(v: number): void {
  currentDataSourceId.value = v
  checkedKeys.value = []
  query.page = 1
  void refresh()
}

function buildConfig(tableName: string): GenConfig {
  return {
    tableName,
    dataSourceId: currentDataSourceId.value ?? undefined,
  }
}

function getCheckedTable(): string | null {
  if (checkedKeys.value.length === 0) {
    message.warning('请先选择一张表')
    return null
  }
  return String(checkedKeys.value[0])
}

async function handlePreview(): Promise<void> {
  const tableName = getCheckedTable()
  if (!tableName) {return}
  previewVisible.value = true
  previewLoading.value = true
  previewFiles.value = []
  previewActiveTab.value = ''
  try {
    const res = await previewCode(buildConfig(tableName))
    previewFiles.value = res?.templateFiles ?? []
    if (previewFiles.value.length > 0) {
      previewActiveTab.value = previewFiles.value[0].name
    }
  } finally {
    previewLoading.value = false
  }
}

async function handleDownload(): Promise<void> {
  const tableName = getCheckedTable()
  if (!tableName) {return}
  const blob = await downloadCode(buildConfig(tableName))
  downloadBlob(blob, `${tableName}.zip`)
  message.success('下载已开始')
}

async function handleImport(): Promise<void> {
  const tableName = getCheckedTable()
  if (!tableName) {return}
  await importCode(buildConfig(tableName))
  message.success('导入成功')
}

function detectLang(name: string): string {
  if (name.endsWith('.java')) {return 'java'}
  if (name.endsWith('.xml')) {return 'xml'}
  if (name.endsWith('.vue')) {return 'html'}
  if (name.endsWith('.ts') || name.endsWith('.js')) {return 'javascript'}
  if (name.endsWith('.sql')) {return 'sql'}
  if (name.endsWith('.yml') || name.endsWith('.yaml')) {return 'yaml'}
  return 'text'
}

const columns: DataTableColumns<GenTable> = [
  { type: 'selection', multiple: false },
  { title: '表名', key: 'tableName', width: 220 },
  { title: '说明', key: 'tableComment', width: 240 },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '更新时间', key: 'updateTime', width: 180 },
]

onMounted(() => {
  void loadDataSources()
})
</script>

<template>
  <div class="page-code">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="数据源">
          <n-select
            :value="currentDataSourceId"
            :options="dataSourceOptions"
            placeholder="选择数据源"
            style="min-width: 200px"
            @update:value="handleDataSourceChange"
          />
        </n-form-item>
        <n-form-item label="表名">
          <n-input v-model:value="query.tableName" placeholder="表名" clearable />
        </n-form-item>
        <n-form-item label="说明">
          <n-input v-model:value="query.tableComment" placeholder="表说明" clearable />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch">查询</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card size="small">
      <div class="toolbar">
        <n-space>
          <n-button type="primary" @click="handlePreview">预览</n-button>
          <n-button type="info" @click="handleDownload">下载</n-button>
          <n-button type="success" @click="handleImport">导入</n-button>
        </n-space>
      </div>
      <n-data-table
        v-model:checked-row-keys="checkedKeys"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: GenTable) => row.tableName"
        :scroll-x="1000"
        striped
      />
      <div class="pagination">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.size"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="refresh"
          @update:page-size="refresh"
        />
      </div>
    </n-card>

    <n-modal
      v-model:show="previewVisible"
      preset="card"
      title="代码预览"
      style="width: 900px; max-width: 90vw"
      :bordered="false"
    >
      <n-spin :show="previewLoading">
        <n-empty v-if="previewFiles.length === 0 && !previewLoading" description="暂无内容" />
        <n-tabs
          v-else
          v-model:value="previewActiveTab"
          type="line"
          placement="left"
          style="min-height: 480px"
        >
          <n-tab-pane v-for="f in previewFiles" :key="f.name" :name="f.name" :tab="f.name">
            <div class="code-wrap">
              <n-code :code="f.content" :language="detectLang(f.name)" show-line-numbers />
            </div>
          </n-tab-pane>
        </n-tabs>
      </n-spin>
    </n-modal>
  </div>
</template>

<style scoped>
.page-code {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  margin-bottom: 12px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.code-wrap {
  max-height: 60vh;
  overflow: auto;
  background: var(--n-color-embedded, #fafafa);
  padding: 12px;
  border-radius: 4px;
}
</style>
