<!--
  @file views/openapi/policy/index.vue
  @description OpenAPI 策略管理：列表 + 搜索 + 新增/编辑抽屉
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
  NSwitch,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createPolicy,
  deletePolicy,
  pagePolicies,
  pageApps,
  updatePolicy,
  type OpenApiApp,
  type OpenApiPolicy,
  type PolicyQuery,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiPolicy[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<PolicyQuery>({
  page: 1,
  size: 10,
  policyName: '',
  appId: undefined,
})

const appOptions = ref<SelectOption[]>([])

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): OpenApiPolicy {
  return {
    policyName: '',
    appId: 0,
    signRequired: true,
    encryptEnabled: false,
    rateLimitPerMin: 0,
    ipWhitelist: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<OpenApiPolicy>(defaultForm())

const rules: FormRules = {
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  appId: [{ required: true, type: 'number', message: '请选择应用', trigger: 'change' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

async function loadApps(): Promise<void> {
  const res = await pageApps({ page: 1, size: 200 })
  appOptions.value = (res?.records ?? []).map((a: OpenApiApp) => ({
    label: a.appName,
    value: a.id as number,
  }))
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pagePolicies(query)
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
  query.policyName = ''
  query.appId = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增策略'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: OpenApiPolicy): void {
  isEdit.value = true
  drawerTitle.value = '编辑策略'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updatePolicy(formData.id, formData)
      message.success('更新成功')
    } else {
      await createPolicy(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: OpenApiPolicy): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除策略 ${row.policyName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deletePolicy(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<OpenApiPolicy> = [
  { title: '策略名称', key: 'policyName', width: 180 },
  { title: '关联应用', key: 'appName', width: 180 },
  {
    title: '签名必需',
    key: 'signRequired',
    width: 110,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.signRequired ? 'success' : 'default' },
        { default: () => (row.signRequired ? '是' : '否') },
      ),
  },
  {
    title: '加密',
    key: 'encryptEnabled',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.encryptEnabled ? 'success' : 'default' },
        { default: () => (row.encryptEnabled ? '已开启' : '关闭') },
      ),
  },
  { title: '限流(/分钟)', key: 'rateLimitPerMin', width: 120 },
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
  void loadApps()
  void refresh()
})
</script>

<template>
  <div class="page-openapi-policy">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="策略名称">
          <n-input v-model:value="query.policyName" placeholder="策略名称" clearable />
        </n-form-item>
        <n-form-item label="应用">
          <n-select
            v-model:value="query.appId"
            :options="appOptions"
            placeholder="选择应用"
            clearable
            filterable
            style="min-width: 180px"
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
        :row-key="(row: OpenApiPolicy) => row.id as number"
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
          <n-form-item label="策略名称" path="policyName">
            <n-input v-model:value="formData.policyName" />
          </n-form-item>
          <n-form-item label="关联应用" path="appId">
            <n-select v-model:value="formData.appId" :options="appOptions" filterable />
          </n-form-item>
          <n-form-item label="签名必需" path="signRequired">
            <n-switch v-model:value="formData.signRequired" />
          </n-form-item>
          <n-form-item label="启用加密" path="encryptEnabled">
            <n-switch v-model:value="formData.encryptEnabled" />
          </n-form-item>
          <n-form-item label="限流(/分钟)" path="rateLimitPerMin">
            <n-input-number
              v-model:value="formData.rateLimitPerMin"
              :min="0"
              placeholder="0 表示不限制"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="IP 白名单" path="ipWhitelist">
            <n-input
              v-model:value="formData.ipWhitelist"
              type="textarea"
              placeholder="多个 IP 用英文逗号分隔"
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
.page-openapi-policy {
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
