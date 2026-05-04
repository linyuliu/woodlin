<!--
  @file views/openapi/credential/index.vue
  @description OpenAPI 凭证管理：颁发 / 撤销 / 删除，新建后一次性展示 secretKey
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
  NDatePicker,
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
  createCredential,
  deleteCredential,
  pageCredentials,
  pageApps,
  revokeCredential,
  type CredentialQuery,
  type OpenApiApp,
  type OpenApiCredential,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiCredential[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<CredentialQuery>({
  page: 1,
  size: 10,
  appId: undefined,
  status: undefined,
})

const appOptions = ref<SelectOption[]>([])

const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInst | null>(null)

interface CredentialForm {
  appId: number | null
  expireTime: number | null
  remark: string
}

const formData = reactive<CredentialForm>({
  appId: null,
  expireTime: null,
  remark: '',
})

const rules: FormRules = {
  appId: [{ required: true, type: 'number', message: '请选择应用', trigger: 'change' }],
}

const statusOptions: SelectOption[] = [
  { label: '有效', value: '0' },
  { label: '已撤销', value: '1' },
]

const issuedVisible = ref(false)
const issuedKey = ref<OpenApiCredential | null>(null)

function maskKey(key?: string): string {
  if (!key) {return '-'}
  return key.length <= 8 ? `${key}***` : `${key.slice(0, 8)}***`
}

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
    const res = await pageCredentials(query)
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
  query.appId = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  formData.appId = null
  formData.expireTime = null
  formData.remark = ''
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload: OpenApiCredential = {
      appId: formData.appId as number,
      expireTime: formData.expireTime
        ? new Date(formData.expireTime).toISOString()
        : undefined,
      remark: formData.remark,
    }
    const res = await createCredential(payload)
    issuedKey.value = res
    issuedVisible.value = true
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleRevoke(row: OpenApiCredential): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: '撤销后该凭证将立即失效，确定吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await revokeCredential(row.id as number)
      message.success('已撤销')
      void refresh()
    },
  })
}

function handleDelete(row: OpenApiCredential): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: '确认删除该凭证？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteCredential(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

async function copyText(text?: string): Promise<void> {
  if (!text) {return}
  try {
    await navigator.clipboard.writeText(text)
    message.success('已复制')
  } catch {
    message.error('复制失败')
  }
}

const columns: DataTableColumns<OpenApiCredential> = [
  { title: '应用名称', key: 'appName', width: 180 },
  {
    title: 'AccessKey',
    key: 'accessKey',
    width: 200,
    render: (row) => maskKey(row.accessKey),
  },
  { title: '过期时间', key: 'expireTime', width: 180 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '0' ? 'success' : 'error' },
        { default: () => (row.status === '0' ? '有效' : '已撤销') },
      ),
  },
  { title: '备注', key: 'remark' },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: 'warning',
            disabled: row.status !== '0',
            onClick: () => handleRevoke(row),
          },
          { default: () => '撤销' },
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
  <div class="page-openapi-credential">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
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
        <n-button type="primary" @click="openAdd">颁发凭证</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: OpenApiCredential) => row.id as number"
        :scroll-x="1100"
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

    <n-drawer v-model:show="drawerVisible" :width="520">
      <n-drawer-content title="颁发凭证" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="应用" path="appId">
            <n-select
              v-model:value="formData.appId"
              :options="appOptions"
              placeholder="选择应用"
              filterable
            />
          </n-form-item>
          <n-form-item label="过期时间" path="expireTime">
            <n-date-picker
              v-model:value="formData.expireTime"
              type="datetime"
              clearable
              style="width: 100%"
              placeholder="留空表示长期有效"
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

    <n-modal
      v-model:show="issuedVisible"
      preset="card"
      title="凭证已生成"
      style="width: 540px"
      :mask-closable="false"
    >
      <n-alert type="warning" style="margin-bottom: 12px">
        SecretKey 仅在此处明文展示一次，请立即妥善保存，关闭后将无法再次查看。
      </n-alert>
      <n-form label-placement="top">
        <n-form-item label="AccessKey">
          <n-input :value="issuedKey?.accessKey ?? ''" readonly />
        </n-form-item>
        <n-form-item label="SecretKey">
          <n-input
            :value="issuedKey?.secretKey ?? ''"
            readonly
            type="textarea"
            :autosize="{ minRows: 2 }"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="copyText(issuedKey?.accessKey)">复制 AccessKey</n-button>
          <n-button type="primary" @click="copyText(issuedKey?.secretKey)">
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
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
