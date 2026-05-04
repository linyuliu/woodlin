<!--
  @file views/sql2api/index.vue
  @description SQL2API：列表 + 设计器抽屉（SQL 模板 / 参数 / 结果 schema）+ 在线测试
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
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
  createSql2Api,
  deleteSql2Api,
  pageSql2Apis,
  testSql2Api,
  updateSql2Api,
  type Sql2Api,
  type Sql2ApiQuery,
} from '@/api/sql2api'
import { pageDataSources, type DataSource } from '@/api/datasource'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<Sql2Api[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<Sql2ApiQuery>({
  page: 1,
  size: 10,
  apiName: '',
  dsId: undefined,
  status: undefined,
})

const dsOptions = ref<SelectOption[]>([])

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): Sql2Api {
  return {
    apiName: '',
    apiPath: '',
    method: 'GET',
    dsId: 0,
    sqlTemplate: '',
    paramSchema: '',
    resultSchema: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<Sql2Api>(defaultForm())

const rules: FormRules = {
  apiName: [{ required: true, message: '请输入 API 名称', trigger: 'blur' }],
  apiPath: [{ required: true, message: '请输入 API 路径', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  dsId: [{ required: true, type: 'number', message: '请选择数据源', trigger: 'change' }],
  sqlTemplate: [{ required: true, message: '请输入 SQL 模板', trigger: 'blur' }],
}

const methodOptions: SelectOption[] = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
]

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

const testVisible = ref(false)
const testTarget = ref<Sql2Api | null>(null)
const testParamsText = ref('{}')
const testResultText = ref('')
const testLoading = ref(false)

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
    const res = await pageSql2Apis(query)
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
  query.apiName = ''
  query.dsId = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增 API'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: Sql2Api): void {
  isEdit.value = true
  drawerTitle.value = '编辑 API'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateSql2Api(formData.id, formData)
      message.success('更新成功')
    } else {
      await createSql2Api(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: Sql2Api): void {
  if (!row.id) return
  dialog.warning({
    title: '提示',
    content: `确认删除 API ${row.apiName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteSql2Api(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

function openTest(row: Sql2Api): void {
  testTarget.value = row
  testParamsText.value = row.paramSchema?.trim() ? row.paramSchema : '{}'
  testResultText.value = ''
  testVisible.value = true
}

async function runTest(): Promise<void> {
  if (!testTarget.value?.id) return
  let params: Record<string, unknown> = {}
  try {
    params = testParamsText.value.trim() ? JSON.parse(testParamsText.value) : {}
  } catch {
    message.error('参数 JSON 解析失败')
    return
  }
  testLoading.value = true
  try {
    const res = await testSql2Api(testTarget.value.id, params)
    testResultText.value = JSON.stringify(res, null, 2)
  } catch (e) {
    testResultText.value = `执行失败: ${(e as Error)?.message ?? ''}`
  } finally {
    testLoading.value = false
  }
}

const columns: DataTableColumns<Sql2Api> = [
  { title: 'API 名称', key: 'apiName', width: 180 },
  { title: 'API 路径', key: 'apiPath', width: 240 },
  {
    title: '方法',
    key: 'method',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.method === 'POST' ? 'warning' : 'info' },
        { default: () => row.method },
      ),
  },
  { title: '数据源', key: 'dsName', width: 160 },
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
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => openTest(row) },
          { default: () => '测试' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
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
  <div class="page-sql2api">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="API 名称">
          <n-input v-model:value="query.apiName" placeholder="API 名称" clearable />
        </n-form-item>
        <n-form-item label="数据源">
          <n-select
            v-model:value="query.dsId"
            :options="dsOptions"
            placeholder="数据源"
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
        :row-key="(row: Sql2Api) => row.id as number"
        :scroll-x="1200"
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

    <n-drawer v-model:show="drawerVisible" :width="640">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="API 名称" path="apiName">
            <n-input v-model:value="formData.apiName" />
          </n-form-item>
          <n-form-item label="API 路径" path="apiPath">
            <n-input v-model:value="formData.apiPath" placeholder="/api/example" />
          </n-form-item>
          <n-form-item label="请求方法" path="method">
            <n-select v-model:value="formData.method" :options="methodOptions" />
          </n-form-item>
          <n-form-item label="数据源" path="dsId">
            <n-select v-model:value="formData.dsId" :options="dsOptions" filterable />
          </n-form-item>
          <n-form-item label="SQL 模板" path="sqlTemplate">
            <n-input
              v-model:value="formData.sqlTemplate"
              type="textarea"
              class="code-area"
              placeholder="SELECT * FROM t WHERE id = #{id}"
              :autosize="{ minRows: 6 }"
            />
          </n-form-item>
          <n-form-item label="参数 Schema (JSON)" path="paramSchema">
            <n-input
              v-model:value="formData.paramSchema"
              type="textarea"
              class="code-area"
              placeholder='{ "id": "number" }'
              :autosize="{ minRows: 4 }"
            />
          </n-form-item>
          <n-form-item label="结果 Schema (JSON)" path="resultSchema">
            <n-input
              v-model:value="formData.resultSchema"
              type="textarea"
              class="code-area"
              placeholder='{ "name": "string" }'
              :autosize="{ minRows: 4 }"
            />
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
      v-model:show="testVisible"
      preset="card"
      title="API 在线测试"
      style="width: 640px"
    >
      <n-form label-placement="top">
        <n-form-item label="样例参数 (JSON)">
          <n-input
            v-model:value="testParamsText"
            type="textarea"
            class="code-area"
            :autosize="{ minRows: 5 }"
          />
        </n-form-item>
        <n-form-item label="返回结果">
          <n-input
            :value="testResultText"
            type="textarea"
            readonly
            class="code-area"
            :autosize="{ minRows: 6 }"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="testVisible = false">关闭</n-button>
          <n-button type="primary" :loading="testLoading" @click="runTest">执行</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-sql2api {
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
.code-area :deep(textarea) {
  font-family:
    ui-monospace,
    SFMono-Regular,
    Menlo,
    Consolas,
    monospace;
  font-size: 13px;
}
</style>
