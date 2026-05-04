<!--
  @file views/etl/offline/index.vue
  @description ETL 离线作业：列表 + 字段映射动态行 + Cron 调度 + 立即执行 + 数据预览
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDivider,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createOfflineJob,
  deleteOfflineJob,
  pageOfflineJobs,
  previewOfflineJob,
  runOfflineJob,
  updateOfflineJob,
  type EtlOfflineJob,
  type EtlOfflineJobQuery,
  type FieldMapping,
} from '@/api/etl'
import { pageDataSources, type DataSource } from '@/api/datasource'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<EtlOfflineJob[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<EtlOfflineJobQuery>({
  page: 1,
  size: 10,
  jobName: '',
  sourceId: undefined,
  targetId: undefined,
  status: undefined,
})

const dsOptions = ref<SelectOption[]>([])

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultMapping(): FieldMapping {
  return { src: '', dst: '', transform: '' }
}

function defaultForm(): EtlOfflineJob {
  return {
    jobName: '',
    sourceId: 0,
    targetTable: '',
    targetId: 0,
    targetTableDest: '',
    fieldMappings: [defaultMapping()],
    filterExpr: '',
    schedule: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<EtlOfflineJob>(defaultForm())

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入作业名称', trigger: 'blur' }],
  sourceId: [{ required: true, type: 'number', message: '请选择来源数据源', trigger: 'change' }],
  targetId: [{ required: true, type: 'number', message: '请选择目标数据源', trigger: 'change' }],
  targetTableDest: [{ required: true, message: '请输入目标表', trigger: 'blur' }],
}

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewData = ref<Record<string, unknown>[]>([])
const previewColumns = ref<DataTableColumns<Record<string, unknown>>>([])

async function loadDataSources(): Promise<void> {
  const res = await pageDataSources({ page: 1, size: 200 })
  dsOptions.value = (res?.records ?? []).map((d: DataSource) => ({
    label: d.dsName,
    value: d.id as number,
  }))
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageOfflineJobs(query)
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
  query.jobName = ''
  query.sourceId = undefined
  query.targetId = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

function resetForm(): void {
  const d = defaultForm()
  Object.assign(formData, d)
  formData.fieldMappings = [defaultMapping()]
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增 ETL 作业'
  resetForm()
  drawerVisible.value = true
}

function openEdit(row: EtlOfflineJob): void {
  isEdit.value = true
  drawerTitle.value = '编辑 ETL 作业'
  resetForm()
  Object.assign(formData, row)
  formData.fieldMappings =
    row.fieldMappings && row.fieldMappings.length > 0
      ? row.fieldMappings.map((m) => ({ ...m }))
      : [defaultMapping()]
  drawerVisible.value = true
}

function addMapping(): void {
  formData.fieldMappings = [...formData.fieldMappings, defaultMapping()]
}

function removeMapping(idx: number): void {
  formData.fieldMappings = formData.fieldMappings.filter((_, i) => i !== idx)
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateOfflineJob(formData.id, formData)
      message.success('更新成功')
    } else {
      await createOfflineJob(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: EtlOfflineJob): void {
  if (!row.id) return
  dialog.warning({
    title: '提示',
    content: `确认删除作业 ${row.jobName}？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteOfflineJob(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

function handleRun(row: EtlOfflineJob): void {
  if (!row.id) return
  dialog.info({
    title: '立即执行',
    content: `确认立即执行作业 ${row.jobName}？`,
    positiveText: '执行',
    negativeText: '取消',
    onPositiveClick: async () => {
      await runOfflineJob(row.id as number)
      message.success('已触发')
      void refresh()
    },
  })
}

async function handlePreview(row: EtlOfflineJob): Promise<void> {
  if (!row.id) return
  previewVisible.value = true
  previewLoading.value = true
  previewData.value = []
  previewColumns.value = []
  try {
    const rows = await previewOfflineJob(row.id)
    previewData.value = rows ?? []
    if (rows && rows.length > 0) {
      previewColumns.value = Object.keys(rows[0]).map((k) => ({
        title: k,
        key: k,
        ellipsis: { tooltip: true },
      }))
    }
  } finally {
    previewLoading.value = false
  }
}

const columns: DataTableColumns<EtlOfflineJob> = [
  { title: '作业名称', key: 'jobName', width: 180 },
  { title: '来源', key: 'sourceName', width: 140 },
  { title: '目标', key: 'targetName', width: 140 },
  { title: '目标表', key: 'targetTableDest', width: 160 },
  { title: 'Cron', key: 'schedule', width: 160 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '0' ? 'success' : 'error' },
        { default: () => (row.status === '0' ? '启用' : '禁用') },
      ),
  },
  { title: '上次运行', key: 'lastRunTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 280,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => handleRun(row) },
          { default: () => '执行' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'warning', onClick: () => void handlePreview(row) },
          { default: () => '预览' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', text: true, type: 'error' },
                { default: () => '删除' },
              ),
          },
        ),
      ]),
  },
]

onMounted(() => {
  void loadDataSources()
  void refresh()
})
</script>

<template>
  <div class="page-offline">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="作业名称">
          <n-input v-model:value="query.jobName" placeholder="作业名称" clearable />
        </n-form-item>
        <n-form-item label="来源">
          <n-select
            v-model:value="query.sourceId"
            :options="dsOptions"
            placeholder="来源数据源"
            clearable
            filterable
            style="min-width: 180px"
          />
        </n-form-item>
        <n-form-item label="目标">
          <n-select
            v-model:value="query.targetId"
            :options="dsOptions"
            placeholder="目标数据源"
            clearable
            filterable
            style="min-width: 180px"
          />
        </n-form-item>
        <n-form-item label="状态">
          <n-select
            v-model:value="query.status"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="min-width: 120px"
          />
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
        <n-button type="primary" @click="openAdd">新增</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: EtlOfflineJob) => row.id as number"
        :scroll-x="1500"
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

    <n-drawer v-model:show="drawerVisible" :width="800">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="作业名称" path="jobName">
            <n-input v-model:value="formData.jobName" />
          </n-form-item>
          <n-form-item label="来源数据源" path="sourceId">
            <n-select v-model:value="formData.sourceId" :options="dsOptions" filterable />
          </n-form-item>
          <n-form-item label="来源表" path="targetTable">
            <n-input v-model:value="formData.targetTable" placeholder="如 source_table" />
          </n-form-item>
          <n-form-item label="目标数据源" path="targetId">
            <n-select v-model:value="formData.targetId" :options="dsOptions" filterable />
          </n-form-item>
          <n-form-item label="目标表" path="targetTableDest">
            <n-input v-model:value="formData.targetTableDest" placeholder="如 dwd_table" />
          </n-form-item>

          <n-divider>字段映射</n-divider>
          <div
            v-for="(m, idx) in formData.fieldMappings"
            :key="idx"
            class="mapping-row"
          >
            <n-input v-model:value="m.src" placeholder="源字段" style="flex: 1" />
            <span class="arrow">→</span>
            <n-input v-model:value="m.dst" placeholder="目标字段" style="flex: 1" />
            <n-input v-model:value="m.transform" placeholder="转换表达式（可选）" style="flex: 1" />
            <n-button type="error" text @click="removeMapping(idx)">删除</n-button>
          </div>
          <n-button dashed block @click="addMapping">+ 添加映射</n-button>

          <n-form-item label="过滤表达式" path="filterExpr" style="margin-top: 12px">
            <n-input
              v-model:value="formData.filterExpr"
              type="textarea"
              placeholder="如 status = '1' AND create_time > '2024-01-01'"
              :autosize="{ minRows: 2 }"
            />
          </n-form-item>
          <n-form-item label="调度 Cron" path="schedule">
            <n-input v-model:value="formData.schedule" placeholder="如 0 0 2 * * ?" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="备注" path="remark">
            <n-input v-model:value="formData.remark" type="textarea" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="drawerVisible = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
              确定
            </n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <n-modal
      v-model:show="previewVisible"
      preset="card"
      title="数据预览"
      style="width: 880px"
    >
      <n-data-table
        :columns="previewColumns"
        :data="previewData"
        :loading="previewLoading"
        :max-height="420"
        striped
      />
    </n-modal>
  </div>
</template>

<style scoped>
.page-offline {
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
.mapping-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.arrow {
  color: #888;
}
</style>
