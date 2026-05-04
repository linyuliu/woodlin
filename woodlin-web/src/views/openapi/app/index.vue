<!--
  @file views/openapi/app/index.vue
  @description OpenAPI 应用管理：列表 + 关键字搜索 + 新增/编辑抽屉
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
  listApps,
  updateApp,
  type OpenApiApp,
} from '@/api/openapi'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OpenApiApp[]> = ref([])
const loading = ref(false)
const keyword = ref('')

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): OpenApiApp {
  return {
    appName: '',
    appCode: '',
    ownerName: '',
    ipWhitelist: '',
    status: '0',
    remark: '',
  }
}

const formData = reactive<OpenApiApp>(defaultForm())

const rules: FormRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

async function refresh(): Promise<void> {
  loading.value = true
  try {
    tableData.value = (await listApps(keyword.value || undefined)) ?? []
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
  { title: '负责人', key: 'ownerName', width: 140 },
  { title: 'IP白名单', key: 'ipWhitelist', ellipsis: { tooltip: true } },
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
