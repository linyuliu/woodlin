<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, type Ref } from 'vue'
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
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createResource,
  deleteResources,
  listResourceApps,
  listResources,
  listScopes,
  updateResource,
  type AuthScope,
  type OpenApiApp,
  type OpenApiResource,
} from '@/api/openapi'

const message = useMessage()
const tableData: Ref<OpenApiResource[]> = ref([])
const scopes: Ref<AuthScope[]> = ref([])
const loading = ref(false)
const keyword = ref('')
const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const appModalVisible = ref(false)
const resourceApps: Ref<OpenApiApp[]> = ref([])

function defaultForm(): OpenApiResource {
  return {
    resourceCode: '',
    resourceName: '',
    httpMethod: 'GET',
    pathPattern: '',
    capabilityId: undefined,
    scopeId: undefined,
    authMode: 'AKSK',
    status: '1',
    remark: '',
  }
}

const formData = reactive<OpenApiResource>(defaultForm())

const rules: FormRules = {
  resourceCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
  resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  httpMethod: [{ required: true, message: '请选择方法', trigger: 'change' }],
  pathPattern: [{ required: true, message: '请输入路径模式', trigger: 'blur' }],
  scopeId: [{ required: true, type: 'number', message: '请选择 Scope', trigger: 'change' }],
}

const methodOptions: SelectOption[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'ALL'].map((value) => ({
  label: value,
  value,
}))

const authModeOptions: SelectOption[] = [
  { label: 'AK/SK', value: 'AKSK' },
  { label: 'AppKey', value: 'APP_KEY' },
  { label: 'Token', value: 'TOKEN' },
  { label: '公开', value: 'NONE' },
]

const scopeOptions = computed<SelectOption[]>(() =>
  scopes.value.map((scope) => ({
    label: `${scope.scopeName ?? scope.scopeCode}（${scope.scopeCode}）`,
    value: scope.scopeId as number,
  })),
)

function scopeName(scopeId?: number): string {
  if (!scopeId) {
    return '-'
  }
  return scopes.value.find((scope) => scope.scopeId === scopeId)?.scopeCode ?? String(scopeId)
}

async function loadScopes(): Promise<void> {
  scopes.value = (await listScopes()) ?? []
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    tableData.value = (await listResources(keyword.value || undefined)) ?? []
  } finally {
    loading.value = false
  }
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增接口资源'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: OpenApiResource): void {
  isEdit.value = true
  drawerTitle.value = '编辑接口资源'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

function onScopeChange(scopeId: number | null): void {
  const scope = scopes.value.find((item) => item.scopeId === scopeId)
  formData.capabilityId = scope?.capabilityId
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.resourceId) {
      await updateResource(formData)
      message.success('更新成功')
    } else {
      await createResource(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

async function handleShowApps(row: OpenApiResource): Promise<void> {
  if (!row.resourceId) {
    return
  }
  resourceApps.value = (await listResourceApps(row.resourceId)) ?? []
  appModalVisible.value = true
}

async function handleDelete(row: OpenApiResource): Promise<void> {
  if (!row.resourceId) {
    return
  }
  await deleteResources(row.resourceId)
  message.success('删除成功')
  void refresh()
}

const columns: DataTableColumns<OpenApiResource> = [
  { title: '资源名称', key: 'resourceName', width: 180 },
  { title: '资源编码', key: 'resourceCode', width: 220 },
  { title: '方法', key: 'httpMethod', width: 90 },
  { title: '路径', key: 'pathPattern', ellipsis: { tooltip: true } },
  { title: 'Scope', key: 'scopeId', width: 220, render: (row) => scopeName(row.scopeId) },
  {
    title: '认证',
    key: 'authMode',
    width: 100,
    render: (row) => h(NTag, { size: 'small' }, { default: () => row.authMode ?? '-' }),
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '1' ? 'success' : 'error' },
        { default: () => (row.status === '1' ? '启用' : '禁用') },
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
          { size: 'small', text: true, type: 'info', onClick: () => void handleShowApps(row) },
          { default: () => '授权App' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => void handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(NButton, { size: 'small', text: true, type: 'error' }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

onMounted(async () => {
  await loadScopes()
  void refresh()
})
</script>

<template>
  <div class="page-openapi-resource">
    <n-card size="small">
      <n-form inline label-placement="left">
        <n-form-item label="关键字">
          <n-input v-model:value="keyword" placeholder="资源名称/编码/路径" clearable />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="refresh">查询</n-button>
            <n-button @click="keyword = ''; refresh()">重置</n-button>
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
        :row-key="(row: OpenApiResource) => row.resourceId as number"
        :scroll-x="1300"
        striped
      />
    </n-card>

    <n-drawer v-model:show="drawerVisible" :width="560">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="资源名称" path="resourceName">
            <n-input v-model:value="formData.resourceName" />
          </n-form-item>
          <n-form-item label="资源编码" path="resourceCode">
            <n-input v-model:value="formData.resourceCode" :disabled="isEdit" />
          </n-form-item>
          <n-form-item label="HTTP 方法" path="httpMethod">
            <n-select v-model:value="formData.httpMethod" :options="methodOptions" />
          </n-form-item>
          <n-form-item label="路径模式" path="pathPattern">
            <n-input v-model:value="formData.pathPattern" placeholder="/open/user/*" />
          </n-form-item>
          <n-form-item label="Scope" path="scopeId">
            <n-select
              v-model:value="formData.scopeId"
              :options="scopeOptions"
              filterable
              @update:value="onScopeChange"
            />
          </n-form-item>
          <n-form-item label="认证模式" path="authMode">
            <n-select v-model:value="formData.authMode" :options="authModeOptions" />
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

    <n-modal v-model:show="appModalVisible" preset="card" title="已授权 App" style="width: 640px">
      <n-data-table
        :columns="[
          { title: '应用名称', key: 'appName' },
          { title: '应用编码', key: 'appCode' },
          { title: '租户', key: 'tenantId' },
          { title: '地区', key: 'regionName' },
        ]"
        :data="resourceApps"
        :row-key="(row: OpenApiApp) => row.appId as number"
      />
    </n-modal>
  </div>
</template>

<style scoped>
.page-openapi-resource {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar {
  margin-bottom: 12px;
}
</style>
