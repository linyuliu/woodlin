<!--
  @file views/tenant/tenant/index.vue
  @description 租户管理：列表 + 搜索 + 新增/编辑抽屉 + 状态切换
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
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
  createTenant,
  deleteTenant,
  getAllPackages,
  getTenantPage,
  updateTenant,
  updateTenantStatus,
  type SysTenant,
  type SysTenantPackage,
  type TenantQuery,
} from '@/api/tenant'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const dialog = useDialog()
const { hasPermission } = usePermission()

const tableData: Ref<SysTenant[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<TenantQuery>({
  page: 1,
  size: 10,
  tenantName: '',
  tenantCode: '',
  status: undefined,
})

const packageOptions: Ref<SelectOption[]> = ref([])

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const expireTimestamp = ref<number | null>(null)

/** 表单初始值工厂 */
function emptyForm(): SysTenant {
  return {
    id: undefined,
    tenantName: '',
    tenantCode: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    expireTime: undefined,
    userCount: 0,
    packageId: undefined,
    status: '1',
    remark: '',
  }
}

const formData = reactive<SysTenant>(emptyForm())

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const rules: FormRules = {
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  packageId: [{ required: true, type: 'number', message: '请选择套餐', trigger: 'change' }],
}

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await getTenantPage(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

/** 加载套餐下拉 */
async function loadPackages(): Promise<void> {
  const list = await getAllPackages().catch(() => [] as SysTenantPackage[])
  packageOptions.value = list.map((p) => ({ label: p.packageName, value: p.id as number }))
}

/** 搜索 */
function handleSearch(): void {
  query.page = 1
  void refresh()
}

/** 重置搜索 */
function handleReset(): void {
  query.tenantName = ''
  query.tenantCode = ''
  query.status = undefined
  query.page = 1
  void refresh()
}

/** 打开新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增租户'
  Object.assign(formData, emptyForm())
  expireTimestamp.value = null
  drawerVisible.value = true
}

/** 打开编辑 */
function openEdit(row: SysTenant): void {
  isEdit.value = true
  drawerTitle.value = '编辑租户'
  Object.assign(formData, row)
  expireTimestamp.value = row.expireTime ? new Date(row.expireTime).getTime() : null
  drawerVisible.value = true
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  if (expireTimestamp.value) {
    formData.expireTime = new Date(expireTimestamp.value)
      .toISOString()
      .slice(0, 19)
      .replace('T', ' ')
  } else {
    formData.expireTime = undefined
  }
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateTenant(formData.id, formData)
      message.success('更新成功')
    } else {
      await createTenant(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除租户 */
function handleDelete(row: SysTenant): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除租户 ${row.tenantName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteTenant(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

/** 切换租户状态 */
async function handleStatus(row: SysTenant, val: string): Promise<void> {
  if (!row.id) {return}
  await updateTenantStatus(row.id, val)
  row.status = val
  message.success('状态已更新')
}

const columns: DataTableColumns<SysTenant> = [
  { title: '租户名称', key: 'tenantName', width: 160 },
  { title: '租户编码', key: 'tenantCode', width: 140 },
  { title: '联系人', key: 'contactName', width: 100 },
  { title: '联系电话', key: 'contactPhone', width: 130 },
  { title: '套餐', key: 'packageName', width: 140 },
  { title: '到期时间', key: 'expireTime', width: 170, render: (row) => row.expireTime ?? '-' },
  { title: '用户数上限', key: 'userCount', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 140,
    render: (row) =>
      h(NSpace, { size: 'small', align: 'center' }, () => [
        h(NSwitch, {
          value: row.status === '1',
          onUpdateValue: (v: boolean) => handleStatus(row, v ? '1' : '0'),
        }),
        h(
          NTag,
          { type: row.status === '1' ? 'success' : 'error', size: 'small' },
          { default: () => (row.status === '1' ? '启用' : '禁用') },
        ),
      ]),
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    render: (row) => {
      const items = [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
      ]
      if (hasPermission('tenant:list:remove')) {
        items.push(
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
        )
      }
      return h(NSpace, { size: 'small' }, () => items)
    },
  },
]

onMounted(() => {
  void refresh()
  void loadPackages()
})
</script>

<template>
  <div class="page-tenant">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="租户名称">
          <n-input v-model:value="query.tenantName" placeholder="租户名称" clearable />
        </n-form-item>
        <n-form-item label="租户编码">
          <n-input v-model:value="query.tenantCode" placeholder="租户编码" clearable />
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
        <n-button v-permission="'tenant:list:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysTenant) => row.id as number"
        :scroll-x="1400"
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
          <n-form-item label="租户名称" path="tenantName">
            <n-input v-model:value="formData.tenantName" />
          </n-form-item>
          <n-form-item label="租户编码" path="tenantCode">
            <n-input v-model:value="formData.tenantCode" :disabled="isEdit" />
          </n-form-item>
          <n-form-item label="套餐" path="packageId">
            <n-select
              v-model:value="formData.packageId"
              :options="packageOptions"
              placeholder="请选择套餐"
              clearable
            />
          </n-form-item>
          <n-form-item label="联系人" path="contactName">
            <n-input v-model:value="formData.contactName" />
          </n-form-item>
          <n-form-item label="联系电话" path="contactPhone">
            <n-input v-model:value="formData.contactPhone" />
          </n-form-item>
          <n-form-item label="联系邮箱" path="contactEmail">
            <n-input v-model:value="formData.contactEmail" />
          </n-form-item>
          <n-form-item label="到期时间" path="expireTime">
            <n-date-picker
              v-model:value="expireTimestamp"
              type="datetime"
              clearable
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="用户数上限" path="userCount">
            <n-input-number v-model:value="formData.userCount" :min="0" style="width: 100%" />
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
.page-tenant {
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
