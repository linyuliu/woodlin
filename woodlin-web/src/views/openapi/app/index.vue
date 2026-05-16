<!--
  @file views/openapi/app/index.vue
  @description OpenAPI 应用管理：列表 + 关键字搜索 + 新增/编辑抽屉
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NCheckboxGroup,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
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
  deleteApps,
  listAppGrants,
  listApps,
  listClients,
  listScopes,
  saveAppGrants,
  updateApp,
  type AuthScope,
  type OpenApiApp,
  type OpenApiClient,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiApp[]> = ref([])
const clients: Ref<OpenApiClient[]> = ref([])
const scopes: Ref<AuthScope[]> = ref([])
const loading = ref(false)
const keyword = ref('')

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const grantDrawerVisible = ref(false)
const grantLoading = ref(false)
const selectedGrantApp = ref<OpenApiApp | null>(null)
const selectedScopeIds = ref<number[]>([])

function defaultForm(): OpenApiApp {
  return {
    appName: '',
    appCode: '',
    clientId: undefined,
    tenantId: '',
    regionCode: '',
    regionName: '',
    ownerUserId: undefined,
    ownerDeptId: undefined,
    ownerName: '',
    ipWhitelist: '',
    status: '1',
    remark: '',
  }
}

const formData = reactive<OpenApiApp>(defaultForm())

const rules: FormRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const clientOptions = computed<SelectOption[]>(() =>
  clients.value.map((item) => ({
    label: item.clientName,
    value: item.clientId as number,
  })),
)

const scopeOptions = computed<SelectOption[]>(() =>
  scopes.value
    .filter((item) => item.enabled !== '0' && item.scopeId)
    .map((item) => ({
      label: `${item.scopeName ?? item.scopeCode}（${item.scopeCode}）`,
      value: item.scopeId as number,
    })),
)

function clientName(clientId?: number): string {
  if (!clientId) {
    return '-'
  }
  return clients.value.find((item) => item.clientId === clientId)?.clientName ?? String(clientId)
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    tableData.value = (await listApps(keyword.value || undefined)) ?? []
  } finally {
    loading.value = false
  }
}

async function loadDictionaries(): Promise<void> {
  const [clientData, scopeData] = await Promise.all([listClients(), listScopes()])
  clients.value = clientData ?? []
  scopes.value = scopeData ?? []
}

function handleSearch(): void {
  void refresh()
}

function handleReset(): void {
  keyword.value = ''
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

async function openGrant(row: OpenApiApp): Promise<void> {
  if (!row.appId) {
    return
  }
  selectedGrantApp.value = row
  grantDrawerVisible.value = true
  grantLoading.value = true
  try {
    const grants = (await listAppGrants(row.appId)) ?? []
    selectedScopeIds.value = grants
      .map((item) => item.scopeId)
      .filter((item): item is number => typeof item === 'number')
  } finally {
    grantLoading.value = false
  }
}

async function handleSaveGrants(): Promise<void> {
  if (!selectedGrantApp.value?.appId) {
    return
  }
  grantLoading.value = true
  try {
    await saveAppGrants(selectedGrantApp.value.appId, selectedScopeIds.value)
    message.success('授权已保存')
    grantDrawerVisible.value = false
  } finally {
    grantLoading.value = false
  }
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.appId) {
      await updateApp(formData)
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
  if (!row.appId) {
    return
  }
  dialog.warning({
    title: '提示',
    content: `确认删除应用 ${row.appName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteApps(row.appId as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<OpenApiApp> = [
  { title: '应用名称', key: 'appName', width: 180 },
  { title: '应用编码', key: 'appCode', width: 200 },
  { title: '客户', key: 'clientId', width: 160, render: (row) => clientName(row.clientId) },
  { title: '地区', key: 'regionName', width: 120 },
  { title: '租户', key: 'tenantId', width: 120 },
  { title: '负责人', key: 'ownerName', width: 140 },
  { title: 'IP白名单', key: 'ipWhitelist', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '1' ? 'success' : 'error' },
        { default: () => (row.status === '1' ? '启用' : '禁用') },
      ),
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 220,
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
          { size: 'small', text: true, type: 'info', onClick: () => void openGrant(row) },
          { default: () => '授权' },
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

onMounted(async () => {
  await loadDictionaries()
  void refresh()
})
</script>

<template>
  <div class="page-openapi-app">
    <n-card size="small">
      <n-form inline label-placement="left">
        <n-form-item label="关键字">
          <n-input v-model:value="keyword" placeholder="应用名称/编码" clearable />
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
        :row-key="(row: OpenApiApp) => row.appId as number"
        :scroll-x="1200"
        striped
      />
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
          <n-form-item label="客户" path="clientId">
            <n-select
              v-model:value="formData.clientId"
              :options="clientOptions"
              clearable
              filterable
            />
          </n-form-item>
          <n-form-item label="租户" path="tenantId">
            <n-input v-model:value="formData.tenantId" />
          </n-form-item>
          <n-form-item label="地区编码" path="regionCode">
            <n-input v-model:value="formData.regionCode" />
          </n-form-item>
          <n-form-item label="地区名称" path="regionName">
            <n-input v-model:value="formData.regionName" />
          </n-form-item>
          <n-form-item label="负责人用户ID" path="ownerUserId">
            <n-input-number v-model:value="formData.ownerUserId" clearable style="width: 100%" />
          </n-form-item>
          <n-form-item label="负责部门ID" path="ownerDeptId">
            <n-input-number v-model:value="formData.ownerDeptId" clearable style="width: 100%" />
          </n-form-item>
          <n-form-item label="负责人" path="ownerName">
            <n-input v-model:value="formData.ownerName" />
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

    <n-drawer v-model:show="grantDrawerVisible" :width="640">
      <n-drawer-content :title="`接口授权 - ${selectedGrantApp?.appName ?? ''}`" closable>
        <n-card size="small" :bordered="false">
          <n-checkbox-group v-model:value="selectedScopeIds">
            <n-space vertical>
              <n-checkbox
                v-for="option in scopeOptions"
                :key="String(option.value)"
                :value="option.value as number"
              >
                {{ option.label }}
              </n-checkbox>
            </n-space>
          </n-checkbox-group>
        </n-card>
        <template #footer>
          <n-space justify="end">
            <n-button @click="grantDrawerVisible = false">取消</n-button>
            <n-button type="primary" :loading="grantLoading" @click="handleSaveGrants">
              保存
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
</style>
