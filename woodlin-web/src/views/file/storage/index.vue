<!--
  @file views/file/storage/index.vue
  @description 存储后端管理：CRUD + 测试连接 + 设为默认，按存储类型动态显示字段
  @author yulin
  @since 2026-01-01
-->
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
  createStorage,
  deleteStorage,
  pageStorages,
  setDefaultStorage,
  testStorage,
  updateStorage,
  type StorageConfig,
  type StorageQuery,
} from '@/api/file'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<StorageConfig[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<StorageQuery>({
  page: 1,
  size: 10,
  storageName: '',
  storageType: undefined,
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const storageTypeOptions: SelectOption[] = [
  { label: 'LOCAL（本地）', value: 'LOCAL' },
  { label: 'MinIO', value: 'MINIO' },
  { label: '阿里云 OSS', value: 'OSS' },
  { label: '腾讯云 COS', value: 'COS' },
  { label: '华为云 OBS', value: 'OBS' },
]

const storageTypeColor: Record<string, 'info' | 'success' | 'warning' | 'error' | 'default'> = {
  LOCAL: 'default',
  MINIO: 'info',
  OSS: 'warning',
  COS: 'success',
  OBS: 'error',
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

function defaultForm(): StorageConfig {
  return {
    storageName: '',
    storageType: 'MINIO',
    endpoint: '',
    bucket: '',
    accessKey: '',
    secretKey: '',
    region: '',
    basePath: '',
    status: '1',
    remark: '',
  }
}

const formData = reactive<StorageConfig>(defaultForm())

const rules: FormRules = {
  storageName: [{ required: true, message: '请输入存储名称', trigger: 'blur' }],
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
}

const showCloudFields = computed(() => formData.storageType !== 'LOCAL')
const showRegion = computed(() => ['OSS', 'COS', 'OBS'].includes(formData.storageType))

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageStorages(query)
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
  query.storageName = ''
  query.storageType = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增存储'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: StorageConfig): void {
  isEdit.value = true
  drawerTitle.value = '编辑存储'
  Object.assign(formData, defaultForm(), row, { secretKey: '' })
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateStorage(formData.id, formData)
      message.success('更新成功')
    } else {
      await createStorage(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: StorageConfig): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除存储 ${row.storageName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteStorage(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

async function handleTest(row: StorageConfig): Promise<void> {
  if (!row.id) {return}
  const reactiveMsg = message.loading('正在测试连接...', { duration: 0 })
  try {
    const res = await testStorage(row.id)
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

async function handleSetDefault(row: StorageConfig): Promise<void> {
  if (!row.id) {return}
  await setDefaultStorage(row.id)
  message.success('已设为默认')
  void refresh()
}

const columns: DataTableColumns<StorageConfig> = [
  { title: '名称', key: 'storageName', width: 180 },
  {
    title: '类型',
    key: 'storageType',
    width: 140,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: storageTypeColor[row.storageType] ?? 'default' },
        { default: () => row.storageType },
      ),
  },
  { title: 'Endpoint', key: 'endpoint', minWidth: 220, ellipsis: { tooltip: true } },
  { title: 'Bucket', key: 'bucket', width: 160 },
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
  {
    title: '默认',
    key: 'isDefault',
    width: 90,
    render: (row) =>
      row.isDefault
        ? h(NTag, { size: 'small', type: 'success' }, { default: () => '默认' })
        : h('span', { style: { color: '#999' } }, '-'),
  },
  {
    title: '操作',
    key: 'action',
    width: 280,
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
          {
            size: 'small',
            text: true,
            type: 'warning',
            disabled: !!row.isDefault,
            onClick: () => handleSetDefault(row),
          },
          { default: () => '设为默认' },
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
  <div class="page-file-storage">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="名称">
          <n-input v-model:value="query.storageName" placeholder="存储名称" clearable />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="query.storageType"
            :options="storageTypeOptions"
            placeholder="类型"
            clearable
            style="min-width: 160px"
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
        :row-key="(row: StorageConfig) => row.id as number"
        :scroll-x="1300"
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
          <n-form-item label="存储名称" path="storageName">
            <n-input v-model:value="formData.storageName" />
          </n-form-item>
          <n-form-item label="存储类型" path="storageType">
            <n-select v-model:value="formData.storageType" :options="storageTypeOptions" />
          </n-form-item>
          <template v-if="showCloudFields">
            <n-form-item label="Endpoint" path="endpoint">
              <n-input v-model:value="formData.endpoint" placeholder="服务地址" />
            </n-form-item>
            <n-form-item label="Bucket" path="bucket">
              <n-input v-model:value="formData.bucket" />
            </n-form-item>
            <n-form-item label="AccessKey" path="accessKey">
              <n-input v-model:value="formData.accessKey" />
            </n-form-item>
            <n-form-item label="SecretKey" path="secretKey">
              <n-input
                v-model:value="formData.secretKey"
                type="password"
                show-password-on="click"
                :placeholder="isEdit ? '留空表示不修改' : '请输入 SecretKey'"
              />
            </n-form-item>
          </template>
          <n-form-item v-if="showRegion" label="Region" path="region">
            <n-input v-model:value="formData.region" placeholder="区域，如 cn-hangzhou" />
          </n-form-item>
          <n-form-item label="基础路径" path="basePath">
            <n-input v-model:value="formData.basePath" placeholder="可选，例如 /uploads" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-switch
              :value="formData.status === '1'"
              @update:value="(v: boolean) => (formData.status = v ? '1' : '0')"
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
.page-file-storage {
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
