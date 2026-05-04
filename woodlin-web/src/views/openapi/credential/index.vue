<!--
  @file views/openapi/credential/index.vue
  @description OpenAPI 凭证管理：选择应用 → 列表 / 签发 / 轮换 / 吊销
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NAlert,
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NModal,
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
  issueCredential,
  listApps,
  listCredentials,
  revokeCredential,
  rotateCredential,
  type OpenApiApp,
  type OpenApiCredentialIssueResponse,
  type OpenApiCredentialRequest,
  type OpenApiCredentialView,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiCredentialView[]> = ref([])
const loading = ref(false)
const selectedAppId = ref<number | null>(null)
const appOptions = ref<SelectOption[]>([])

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInst | null>(null)

/** 当前抽屉模式：签发 (issue) 或轮换 (rotate) */
const drawerMode = ref<'issue' | 'rotate'>('issue')
/** 轮换时所操作的凭证 ID */
const rotatingCredentialId = ref<number | null>(null)

const formData = reactive<OpenApiCredentialRequest>({
  credentialName: '',
  securityMode: 'SIGN',
  signatureAlgorithm: 'HMAC-SHA256',
  encryptionAlgorithm: '',
  activeFrom: undefined,
  activeTo: undefined,
  remark: '',
})

const rules: FormRules = {
  credentialName: [{ required: true, message: '请输入凭证名称', trigger: 'blur' }],
  securityMode: [{ required: true, message: '请选择安全模式', trigger: 'change' }],
  signatureAlgorithm: [{ required: true, message: '请输入签名算法', trigger: 'blur' }],
}

const securityModeOptions: SelectOption[] = [
  { label: '签名', value: 'SIGN' },
  { label: '签名+加密', value: 'SIGN_ENCRYPT' },
  { label: '不校验', value: 'NONE' },
]

const issuedVisible = ref(false)
const issuedResult = ref<OpenApiCredentialIssueResponse | null>(null)

function maskKey(key?: string): string {
  if (!key) {
    return '-'
  }
  return key.length <= 8 ? `${key}***` : `${key.slice(0, 8)}***`
}

async function loadApps(): Promise<void> {
  const apps = (await listApps()) ?? []
  appOptions.value = apps.map((a: OpenApiApp) => ({
    label: a.appName,
    value: a.appId as number,
  }))
  if (!selectedAppId.value && apps.length > 0) {
    selectedAppId.value = apps[0].appId as number
  }
}

async function refresh(): Promise<void> {
  if (!selectedAppId.value) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    tableData.value = (await listCredentials(selectedAppId.value)) ?? []
  } finally {
    loading.value = false
  }
}

function resetForm(): void {
  formData.credentialName = ''
  formData.securityMode = 'SIGN'
  formData.signatureAlgorithm = 'HMAC-SHA256'
  formData.encryptionAlgorithm = ''
  formData.activeFrom = undefined
  formData.activeTo = undefined
  formData.remark = ''
}

function openIssue(): void {
  if (!selectedAppId.value) {
    message.warning('请先选择应用')
    return
  }
  drawerMode.value = 'issue'
  drawerTitle.value = '签发凭证'
  rotatingCredentialId.value = null
  resetForm()
  drawerVisible.value = true
}

function openRotate(row: OpenApiCredentialView): void {
  if (!row.credentialId) {
    return
  }
  drawerMode.value = 'rotate'
  drawerTitle.value = '轮换凭证'
  rotatingCredentialId.value = row.credentialId
  resetForm()
  formData.credentialName = row.credentialName ?? ''
  formData.securityMode = row.securityMode ?? 'SIGN'
  formData.signatureAlgorithm = row.signatureAlgorithm ?? 'HMAC-SHA256'
  formData.encryptionAlgorithm = row.encryptionAlgorithm ?? ''
  formData.remark = row.remark ?? ''
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    let res: OpenApiCredentialIssueResponse
    if (drawerMode.value === 'rotate' && rotatingCredentialId.value) {
      res = await rotateCredential(rotatingCredentialId.value, formData)
    } else {
      res = await issueCredential(selectedAppId.value as number, formData)
    }
    issuedResult.value = res
    issuedVisible.value = true
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleRevoke(row: OpenApiCredentialView): void {
  if (!row.credentialId) {
    return
  }
  dialog.warning({
    title: '提示',
    content: '吊销后该凭证将立即失效，确定吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await revokeCredential(row.credentialId as number)
      message.success('已吊销')
      void refresh()
    },
  })
}

async function copyText(text?: string): Promise<void> {
  if (!text) {
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    message.success('已复制')
  } catch {
    message.error('复制失败')
  }
}

const columns: DataTableColumns<OpenApiCredentialView> = [
  { title: '凭证名称', key: 'credentialName', width: 160 },
  {
    title: 'AccessKey',
    key: 'accessKey',
    width: 200,
    render: (row) => maskKey(row.accessKey),
  },
  { title: '安全模式', key: 'securityMode', width: 130 },
  { title: '签名算法', key: 'signatureAlgorithm', width: 140 },
  { title: '生效时间', key: 'activeFrom', width: 170 },
  { title: '失效时间', key: 'activeTo', width: 170 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '0' ? 'success' : 'error' },
        { default: () => (row.status === '0' ? '有效' : '已吊销') },
      ),
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: 'primary',
            onClick: () => openRotate(row),
          },
          { default: () => '轮换' },
        ),
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: 'warning',
            disabled: row.status !== '0',
            onClick: () => handleRevoke(row),
          },
          { default: () => '吊销' },
        ),
      ]),
  },
]

function onAppChange(): void {
  void refresh()
}

onMounted(async () => {
  await loadApps()
  void refresh()
})
</script>

<template>
  <div class="page-openapi-credential">
    <n-card size="small">
      <n-form inline label-placement="left">
        <n-form-item label="应用">
          <n-select
            v-model:value="selectedAppId"
            :options="appOptions"
            placeholder="选择应用"
            clearable
            filterable
            style="min-width: 220px"
            @update:value="onAppChange"
          />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="refresh">刷新</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card size="small">
      <div class="toolbar">
        <n-button type="primary" :disabled="!selectedAppId" @click="openIssue">
          签发凭证
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: OpenApiCredentialView) => row.credentialId as number"
        :scroll-x="1300"
        striped
      />
    </n-card>

    <n-drawer v-model:show="drawerVisible" :width="520">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="凭证名称" path="credentialName">
            <n-input v-model:value="formData.credentialName" />
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
      v-model:show="issuedVisible"
      preset="card"
      title="凭证已生成"
      style="width: 560px"
      :mask-closable="false"
    >
      <n-alert type="warning" style="margin-bottom: 12px">
        SecretKey 与私钥仅在此处明文展示一次，请立即妥善保存，关闭后将无法再次查看。
      </n-alert>
      <n-form label-placement="top">
        <n-form-item label="AccessKey">
          <n-input :value="issuedResult?.credential?.accessKey ?? ''" readonly />
        </n-form-item>
        <n-form-item label="SecretKey">
          <n-input
            :value="issuedResult?.secretKey ?? ''"
            readonly
            type="textarea"
            :autosize="{ minRows: 2 }"
          />
        </n-form-item>
        <n-form-item v-if="issuedResult?.signaturePrivateKey" label="签名私钥">
          <n-input
            :value="issuedResult?.signaturePrivateKey ?? ''"
            readonly
            type="textarea"
            :autosize="{ minRows: 3 }"
          />
        </n-form-item>
        <n-form-item v-if="issuedResult?.encryptionPrivateKey" label="加密私钥">
          <n-input
            :value="issuedResult?.encryptionPrivateKey ?? ''"
            readonly
            type="textarea"
            :autosize="{ minRows: 3 }"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="copyText(issuedResult?.credential?.accessKey)">
            复制 AccessKey
          </n-button>
          <n-button type="primary" @click="copyText(issuedResult?.secretKey)">
            复制 SecretKey
          </n-button>
          <n-button @click="issuedVisible = false">我已保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-openapi-credential {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  margin-bottom: 12px;
}
</style>
