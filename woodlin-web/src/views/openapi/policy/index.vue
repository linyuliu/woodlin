<!--
  @file views/openapi/policy/index.vue
  @description OpenAPI 策略管理：列表 + 关键字搜索 + 新增/编辑抽屉
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
  createPolicy,
  deletePolicies,
  listPolicies,
  updatePolicy,
  type OpenApiPolicy,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiPolicy[]> = ref([])
const loading = ref(false)
const keyword = ref('')

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): OpenApiPolicy {
  return {
    policyName: '',
    pathPattern: '',
    httpMethod: 'GET',
    securityMode: 'SIGN',
    signatureAlgorithm: 'HMAC-SHA256',
    encryptionAlgorithm: '',
    timestampWindowSeconds: 300,
    nonceEnabled: '1',
    nonceTtlSeconds: 600,
    tenantRequired: '0',
    enabled: '1',
    remark: '',
  }
}

const formData = reactive<OpenApiPolicy>(defaultForm())

const rules: FormRules = {
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  pathPattern: [{ required: true, message: '请输入路径模式', trigger: 'blur' }],
  httpMethod: [{ required: true, message: '请选择 HTTP 方法', trigger: 'change' }],
}

const httpMethodOptions: SelectOption[] = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'ALL', value: '*' },
]

const securityModeOptions: SelectOption[] = [
  { label: '签名', value: 'SIGN' },
  { label: '签名+加密', value: 'SIGN_ENCRYPT' },
  { label: '不校验', value: 'NONE' },
]

const enabledOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const boolFlagOptions: SelectOption[] = [
  { label: '是', value: '1' },
  { label: '否', value: '0' },
]

async function refresh(): Promise<void> {
  loading.value = true
  try {
    tableData.value = (await listPolicies(keyword.value || undefined)) ?? []
  } finally {
    loading.value = false
  }
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
    if (isEdit.value && formData.policyId) {
      await updatePolicy(formData)
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
  if (!row.policyId) {
    return
  }
  dialog.warning({
    title: '提示',
    content: `确认删除策略 ${row.policyName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deletePolicies(row.policyId as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<OpenApiPolicy> = [
  { title: '策略名称', key: 'policyName', width: 180 },
  { title: '路径模式', key: 'pathPattern', width: 220 },
  { title: 'HTTP方法', key: 'httpMethod', width: 110 },
  { title: '安全模式', key: 'securityMode', width: 140 },
  { title: '签名算法', key: 'signatureAlgorithm', width: 140 },
  {
    title: '启用',
    key: 'enabled',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.enabled === '1' ? 'success' : 'default' },
        { default: () => (row.enabled === '1' ? '启用' : '禁用') },
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
  void refresh()
})
</script>

<template>
  <div class="page-openapi-policy">
    <n-card size="small">
      <n-form inline label-placement="left">
        <n-form-item label="关键字">
          <n-input v-model:value="keyword" placeholder="策略名称/路径" clearable />
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
        :row-key="(row: OpenApiPolicy) => row.policyId as number"
        :scroll-x="1200"
        striped
      />
    </n-card>

    <n-drawer v-model:show="drawerVisible" :width="560">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="策略名称" path="policyName">
            <n-input v-model:value="formData.policyName" />
          </n-form-item>
          <n-form-item label="路径模式" path="pathPattern">
            <n-input v-model:value="formData.pathPattern" placeholder="例如 /open/**" />
          </n-form-item>
          <n-form-item label="HTTP 方法" path="httpMethod">
            <n-select v-model:value="formData.httpMethod" :options="httpMethodOptions" />
          </n-form-item>
          <n-form-item label="安全模式" path="securityMode">
            <n-select v-model:value="formData.securityMode" :options="securityModeOptions" />
          </n-form-item>
          <n-form-item label="签名算法" path="signatureAlgorithm">
            <n-input v-model:value="formData.signatureAlgorithm" />
          </n-form-item>
          <n-form-item label="加密算法" path="encryptionAlgorithm">
            <n-input v-model:value="formData.encryptionAlgorithm" />
          </n-form-item>
          <n-form-item label="时间窗(秒)" path="timestampWindowSeconds">
            <n-input-number
              v-model:value="formData.timestampWindowSeconds"
              :min="1"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="启用 nonce" path="nonceEnabled">
            <n-select v-model:value="formData.nonceEnabled" :options="boolFlagOptions" />
          </n-form-item>
          <n-form-item label="nonce TTL(秒)" path="nonceTtlSeconds">
            <n-input-number
              v-model:value="formData.nonceTtlSeconds"
              :min="1"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="要求租户" path="tenantRequired">
            <n-select v-model:value="formData.tenantRequired" :options="boolFlagOptions" />
          </n-form-item>
          <n-form-item label="状态" path="enabled">
            <n-select v-model:value="formData.enabled" :options="enabledOptions" />
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
</style>
