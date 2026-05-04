<!--
  @file views/openapi/app/index.vue
  @description OpenAPI 应用管理：列表 + 搜索 + 新增/编辑抽屉
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
  NInputNumber,
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
  createApp,
  deleteApp,
  pageApps,
  updateApp,
  type AppQuery,
  type OpenApiApp,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiApp[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<AppQuery>({
  page: 1,
  size: 10,
  appName: '',
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): OpenApiApp {
  return {
    appName: '',
    appCode: '',
    signType: 'HMAC-SHA256',
    rateLimit: 0,
    ipWhitelist: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<OpenApiApp>(defaultForm())

const rules: FormRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  signType: [{ required: true, message: '请选择签名类型', trigger: 'change' }],
}

const signTypeOptions: SelectOption[] = [
  { label: 'HMAC-SHA256', value: 'HMAC-SHA256' },
  { label: 'RSA', value: 'RSA' },
  { label: 'NONE', value: 'NONE' },
]

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageApps(query)
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
  query.appName = ''
  query.status = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增应用'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: OpenApiApp): void {
  isEdit.value = true
  drawerTitle.value = '编辑应用'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateApp(formData.id, formData)
      message.success('更新成功')
    } else {
      await createApp(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: OpenApiApp): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除应用 ${row.appName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteApp(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<OpenApiApp> = [
  { title: '应用名称', key: 'appName', width: 180 },
  { title: '应用编码', key: 'appCode', width: 200 },
  {
    title: '签名类型',
    key: 'signType',
    width: 140,
    render: (row) => h(NTag, { size: 'small', type: 'info' }, { default: () => row.signType }),
  },
  { title: '限流(/分钟)', key: 'rateLimit', width: 120 },
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
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
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
  <div class="page-openapi-app">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="应用名称">
          <n-input v-model:value="query.appName" placeholder="应用名称" clearable />
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
        :row-key="(row: OpenApiApp) => row.id as number"
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

    <n-drawer v-model:show="drawerVisible" :width="560">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="应用名称" path="appName">
            <n-input v-model:value="formData.appName" />
          </n-form-item>
          <n-form-item label="应用编码" path="appCode">
            <n-input v-model:value="formData.appCode" :disabled="isEdit" />
          </n-form-item>
          <n-form-item label="签名类型" path="signType">
            <n-select v-model:value="formData.signType" :options="signTypeOptions" />
          </n-form-item>
          <n-form-item label="限流(/分钟)" path="rateLimit">
            <n-input-number
              v-model:value="formData.rateLimit"
              :min="0"
              placeholder="0 表示不限制"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="IP 白名单" path="ipWhitelist">
            <n-input
              v-model:value="formData.ipWhitelist"
              type="textarea"
              placeholder="多个 IP 用英文逗号分隔，留空表示不限制"
              :autosize="{ minRows: 2 }"
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
  </div>
</template>

<style scoped>
.page-openapi-app {
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
