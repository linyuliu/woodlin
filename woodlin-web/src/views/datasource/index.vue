<!--
  @file views/datasource/index.vue
  @description 多数据源管理：CRUD + 测试连接 + 展开行查看表元数据
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
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NSpin,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createDataSource,
  deleteDataSource,
  listTables,
  pageDataSources,
  testDataSource,
  updateDataSource,
  type DataSource,
  type DataSourceQuery,
} from '@/api/datasource'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<DataSource[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<DataSourceQuery>({
  page: 1,
  size: 10,
  dsName: '',
  dsType: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const expandedRowKeys = ref<number[]>([])
const tablesCache = ref<Record<number, { loading: boolean; tables: string[] }>>({})

function defaultForm(): DataSource {
  return {
    dsName: '',
    dsType: 'MySQL',
    host: '127.0.0.1',
    port: 3306,
    dbName: '',
    username: '',
    password: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<DataSource>(defaultForm())

const rules: FormRules = {
  dsName: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  dsType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, type: 'number', message: '请输入端口', trigger: 'blur' }],
  dbName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
}

const dsTypeOptions: SelectOption[] = [
  { label: 'MySQL', value: 'MySQL' },
  { label: 'PostgreSQL', value: 'PostgreSQL' },
  { label: 'Oracle', value: 'Oracle' },
]

const dsTypeColor: Record<string, 'info' | 'success' | 'warning' | 'error' | 'default'> = {
  MySQL: 'info',
  PostgreSQL: 'success',
  Oracle: 'warning',
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageDataSources(query)
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
  query.dsName = ''
  query.dsType = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增数据源'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: DataSource): void {
  isEdit.value = true
  drawerTitle.value = '编辑数据源'
  Object.assign(formData, defaultForm(), row, { password: '' })
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateDataSource(formData.id, formData)
      message.success('更新成功')
    } else {
      await createDataSource(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: DataSource): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除数据源 ${row.dsName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDataSource(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

async function handleTest(row: DataSource): Promise<void> {
  if (!row.id) {return}
  const reactiveMsg = message.loading('正在测试连接...', { duration: 0 })
  try {
    const res = await testDataSource(row.id)
    reactiveMsg.destroy()
    if (res?.success) {
      message.success(res?.message || '连接成功')
    } else {
      message.error(res?.message || '连接失败')
    }
  } catch (e) {
    reactiveMsg.destroy()
    message.error((e as Error)?.message || '连接失败')
  }
}

async function handleExpand(keys: Array<string | number>): Promise<void> {
  expandedRowKeys.value = keys as number[]
  for (const k of keys) {
    const id = Number(k)
    if (!tablesCache.value[id]) {
      tablesCache.value[id] = { loading: true, tables: [] }
      try {
        const tables = await listTables(id)
        tablesCache.value[id] = { loading: false, tables: tables ?? [] }
      } catch {
        tablesCache.value[id] = { loading: false, tables: [] }
      }
    }
  }
}

function renderTablesPanel(row: DataSource) {
  const id = row.id as number
  const cached = tablesCache.value[id]
  if (!cached || cached.loading) {
    return h(NSpin, { size: 'small' })
  }
  if (cached.tables.length === 0) {
    return h(NEmpty, { description: '暂无表元数据', size: 'small' })
  }
  return h(
    NSpace,
    { wrap: true, size: 'small' },
    () =>
      cached.tables.map((t) => h(NTag, { size: 'small', type: 'default' }, { default: () => t })),
  )
}

const columns: DataTableColumns<DataSource> = [
  { type: 'expand', renderExpand: (row) => renderTablesPanel(row) },
  { title: '数据源名称', key: 'dsName', width: 180 },
  {
    title: '类型',
    key: 'dsType',
    width: 120,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: dsTypeColor[row.dsType] ?? 'default' },
        { default: () => row.dsType },
      ),
  },
  {
    title: '地址',
    key: 'host',
    width: 220,
    render: (row) => `${row.host}:${row.port}`,
  },
  { title: '数据库', key: 'dbName', width: 160 },
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
    width: 240,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => handleTest(row) },
          { default: () => '测试连接' },
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
  void refresh()
})
</script>

<template>
  <div class="page-datasource">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="名称">
          <n-input v-model:value="query.dsName" placeholder="数据源名称" clearable />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="query.dsType"
            :options="dsTypeOptions"
            placeholder="类型"
            clearable
            style="min-width: 140px"
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
        :row-key="(row: DataSource) => row.id as number"
        :expanded-row-keys="expandedRowKeys"
        :scroll-x="1200"
        striped
        @update:expanded-row-keys="handleExpand"
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

    <n-drawer v-model:show="drawerVisible" :width="560">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="数据源名称" path="dsName">
            <n-input v-model:value="formData.dsName" />
          </n-form-item>
          <n-form-item label="类型" path="dsType">
            <n-select v-model:value="formData.dsType" :options="dsTypeOptions" />
          </n-form-item>
          <n-form-item label="主机" path="host">
            <n-input v-model:value="formData.host" />
          </n-form-item>
          <n-form-item label="端口" path="port">
            <n-input-number
              v-model:value="formData.port"
              :min="1"
              :max="65535"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="数据库" path="dbName">
            <n-input v-model:value="formData.dbName" />
          </n-form-item>
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="formData.username" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              :placeholder="isEdit ? '留空表示不修改' : '请输入密码'"
            />
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
  </div>
</template>

<style scoped>
.page-datasource {
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
</style>
