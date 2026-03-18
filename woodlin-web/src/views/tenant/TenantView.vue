<script setup lang="ts">
import type { AxiosError } from 'axios'
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSpace,
  NSelect,
  NStatistic,
  NTag,
  NText,
  NThing,
  NTooltip,
  NAlert,
  NScrollbar,
  NDivider,
  NEmpty,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules
} from 'naive-ui'
import {
  AddOutline,
  CalendarOutline,
  CheckmarkCircleOutline,
  PeopleOutline,
  PencilOutline,
  RefreshOutline,
  SearchOutline,
  ShieldCheckmarkOutline,
  TrashOutline
} from '@vicons/ionicons5'

import {
  addTenant,
  changeTenantStatus,
  deleteTenant,
  getTenantById,
  getTenantList,
  updateTenant,
  type PageResult,
  type SysTenant,
  type TenantListParams
} from '@/api/tenant'
import { PERMISSIONS } from '@/constants/permission-keys'
import { useUserStore } from '@/stores'

type TenantFormModel = {
  tenantId: string
  tenantName: string
  tenantCode: string
  contactName: string
  contactPhone: string
  contactEmail: string
  status: string
  expireTime: string | null
  userLimit: number | null
  remark: string
}

type RequestErrorBody = {
  message?: string
}

type StatusMeta = {
  label: string
  type: 'default' | 'success' | 'warning' | 'error' | 'info'
}

const DEFAULT_PAGE_SIZE = 12
const EXPIRING_SOON_DAYS = 30

const message = useMessage()
const userStore = useUserStore()

const canViewTenants = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.TENANT_LIST))
const canCreateTenant = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.TENANT_ADD))
const canEditTenant = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.TENANT_EDIT))
const canDeleteTenant = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.TENANT_REMOVE))

const listLoading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)

const searchForm = reactive({
  tenantName: '',
  tenantCode: '',
  status: ''
})

const tenantList = ref<SysTenant[]>([])
const pagination = reactive({
  page: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  pageCount: 0,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [12, 24, 48]
})

const modalVisible = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = reactive<TenantFormModel>(createEmptyForm())
const isEditMode = computed(() => formModel.tenantId.length > 0)

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const formStatusOptions = statusOptions.filter((item) => item.value !== '')

const formRules: FormRules = {
  tenantName: { required: true, message: '请输入租户名称', trigger: 'blur' },
  tenantCode: { required: true, message: '请输入租户编码', trigger: 'blur' }
}

const currentPageTenantCount = computed(() => tenantList.value.length)
const enabledTenantCount = computed(() =>
  tenantList.value.filter((tenant) => tenant.status === '1' && !isTenantExpired(tenant.expireTime)).length
)
const disabledTenantCount = computed(() => tenantList.value.filter((tenant) => tenant.status === '0').length)
const expiringSoonTenantCount = computed(() => tenantList.value.filter((tenant) => isTenantExpiringSoon(tenant.expireTime)).length)

const tenantColumns: DataTableColumns<SysTenant> = [
  {
    title: '租户',
    key: 'tenantName',
    minWidth: 240,
    render: (row) =>
      h(NThing, { title: row.tenantName }, {
        description: () =>
          h(NSpace, { size: 8, wrap: true }, {
            default: () => [
              h(NTag, { size: 'small', type: 'info', bordered: false }, { default: () => row.tenantCode }),
              h(NText, { depth: 3 }, { default: () => row.contactName || '未设置联系人' })
            ]
          })
      })
  },
  {
    title: '联系人',
    key: 'contactName',
    width: 220,
    render: (row) =>
      h(NSpace, { vertical: true, size: 2 }, {
        default: () => [
          h(NText, null, { default: () => row.contactPhone || '-' }),
          h(NText, { depth: 3 }, { default: () => row.contactEmail || '未设置邮箱' })
        ]
      })
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    align: 'center',
    render: (row) => renderStatusTag(row)
  },
  {
    title: '配额',
    key: 'userLimit',
    width: 120,
    align: 'center',
    render: (row) => `${row.userLimit ?? 0}`
  },
  {
    title: '到期时间',
    key: 'expireTime',
    width: 190,
    render: (row) => formatDateTime(row.expireTime)
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 190,
    render: (row) => formatDateTime(row.updateTime || row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    align: 'center',
    render: (row) => renderActionGroup(row)
  }
]

onMounted(() => {
  void loadTenantList()
})

function createEmptyForm(): TenantFormModel {
  return {
    tenantId: '',
    tenantName: '',
    tenantCode: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    status: '1',
    expireTime: null,
    userLimit: 100,
    remark: ''
  }
}

function applyTenantToForm(tenant: SysTenant): void {
  Object.assign(formModel, {
    tenantId: tenant.tenantId ?? '',
    tenantName: tenant.tenantName,
    tenantCode: tenant.tenantCode,
    contactName: tenant.contactName ?? '',
    contactPhone: tenant.contactPhone ?? '',
    contactEmail: tenant.contactEmail ?? '',
    status: tenant.status ?? '1',
    expireTime: tenant.expireTime ?? null,
    userLimit: tenant.userLimit ?? 100,
    remark: tenant.remark ?? ''
  })
}

function resetForm(): void {
  Object.assign(formModel, createEmptyForm())
  formRef.value?.restoreValidation()
}

function buildQueryParams(): TenantListParams {
  return {
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    tenantName: searchForm.tenantName.trim() || undefined,
    tenantCode: searchForm.tenantCode.trim() || undefined,
    status: searchForm.status || undefined
  }
}

function buildTenantPayload(): SysTenant {
  return {
    tenantId: formModel.tenantId || undefined,
    tenantName: formModel.tenantName.trim(),
    tenantCode: formModel.tenantCode.trim(),
    contactName: formModel.contactName.trim() || undefined,
    contactPhone: formModel.contactPhone.trim() || undefined,
    contactEmail: formModel.contactEmail.trim() || undefined,
    status: formModel.status,
    expireTime: formModel.expireTime || undefined,
    userLimit: formModel.userLimit ?? undefined,
    remark: formModel.remark.trim() || undefined
  }
}

function isTenantExpired(expireTime?: string): boolean {
  if (!expireTime) {
    return false
  }
  const timestamp = new Date(expireTime).getTime()
  return Number.isFinite(timestamp) && timestamp < Date.now()
}

function isTenantExpiringSoon(expireTime?: string): boolean {
  if (!expireTime) {
    return false
  }
  const timestamp = new Date(expireTime).getTime()
  if (!Number.isFinite(timestamp) || timestamp < Date.now()) {
    return false
  }
  return timestamp - Date.now() <= EXPIRING_SOON_DAYS * 24 * 60 * 60 * 1000
}

function getTenantStatusMeta(tenant: SysTenant): StatusMeta {
  if (tenant.status === '0') {
    return { label: '禁用', type: 'warning' }
  }
  if (isTenantExpired(tenant.expireTime)) {
    return { label: '已过期', type: 'error' }
  }
  if (isTenantExpiringSoon(tenant.expireTime)) {
    return { label: '即将到期', type: 'info' }
  }
  return { label: '启用', type: 'success' }
}

function formatDateTime(value?: string): string {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

function resolveRequestError(error: unknown, fallback: string): string {
  const axiosError = error as AxiosError<RequestErrorBody>
  return axiosError.response?.data?.message || axiosError.message || fallback
}

async function loadTenantList(): Promise<void> {
  if (!canViewTenants.value) {
    tenantList.value = []
    pagination.itemCount = 0
    pagination.pageCount = 0
    return
  }

  listLoading.value = true
  try {
    const pageResult = await getTenantList(buildQueryParams())
    applyPageResult(pageResult)
  } catch (error) {
    message.error(resolveRequestError(error, '加载租户列表失败'))
  } finally {
    listLoading.value = false
  }
}

function applyPageResult(pageResult: PageResult<SysTenant>): void {
  tenantList.value = pageResult.data ?? []
  pagination.page = Number(pageResult.current ?? pagination.page)
  pagination.pageSize = Number(pageResult.size ?? pagination.pageSize)
  pagination.itemCount = Number(pageResult.total ?? 0)
  pagination.pageCount = Number(pageResult.pages ?? 0)
}

function handleSearch(): void {
  pagination.page = 1
  void loadTenantList()
}

function handleReset(): void {
  Object.assign(searchForm, {
    tenantName: '',
    tenantCode: '',
    status: ''
  })
  pagination.page = 1
  void loadTenantList()
}

function handlePageChange(page: number): void {
  pagination.page = page
  void loadTenantList()
}

function handlePageSizeChange(pageSize: number): void {
  pagination.pageSize = pageSize
  pagination.page = 1
  void loadTenantList()
}

function openCreateModal(): void {
  if (!canCreateTenant.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.TENANT_ADD} 权限`)
    return
  }
  resetForm()
  modalVisible.value = true
}

async function openEditModal(row: SysTenant): Promise<void> {
  if (!canEditTenant.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.TENANT_EDIT} 权限`)
    return
  }
  if (!row.tenantId) {
    message.error('租户ID缺失，无法编辑')
    return
  }

  detailLoading.value = true
  try {
    const tenant = await getTenantById(row.tenantId)
    applyTenantToForm(tenant)
    modalVisible.value = true
  } catch (error) {
    message.error(resolveRequestError(error, '加载租户详情失败'))
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }

  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = buildTenantPayload()
    if (isEditMode.value) {
      await updateTenant(payload)
      message.success('租户已更新')
    } else {
      await addTenant(payload)
      message.success('租户已创建')
    }
    modalVisible.value = false
    if (!isEditMode.value) {
      pagination.page = 1
    }
    await loadTenantList()
  } catch (error) {
    message.error(resolveRequestError(error, isEditMode.value ? '保存租户失败' : '创建租户失败'))
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysTenant): Promise<void> {
  if (!canDeleteTenant.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.TENANT_REMOVE} 权限`)
    return
  }
  if (!row.tenantId) {
    message.error('租户ID缺失，无法删除')
    return
  }

  try {
    await deleteTenant(row.tenantId)
    if (tenantList.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    message.success('租户已删除')
    await loadTenantList()
  } catch (error) {
    message.error(resolveRequestError(error, '删除租户失败'))
  }
}

async function handleStatusChange(row: SysTenant): Promise<void> {
  if (!canEditTenant.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.TENANT_EDIT} 权限`)
    return
  }
  if (!row.tenantId) {
    message.error('租户ID缺失，无法修改状态')
    return
  }

  const nextStatus = row.status === '1' ? '0' : '1'
  try {
    await changeTenantStatus(row.tenantId, nextStatus)
    message.success(nextStatus === '1' ? '租户已启用' : '租户已禁用')
    await loadTenantList()
  } catch (error) {
    message.error(resolveRequestError(error, '修改租户状态失败'))
  }
}

function renderStatusTag(row: SysTenant) {
  const meta = getTenantStatusMeta(row)
  return h(NTag, { size: 'small', bordered: false, type: meta.type }, { default: () => meta.label })
}

function renderEditAction(row: SysTenant) {
  return h(
    NButton,
    {
      size: 'small',
      tertiary: true,
      type: 'primary',
      onClick: () => { void openEditModal(row) }
    },
    {
      default: () => '编辑',
      icon: () => h(NIcon, null, { default: () => h(PencilOutline) })
    }
  )
}

function renderStatusAction(row: SysTenant) {
  return h(
    NButton,
    {
      size: 'small',
      tertiary: true,
      type: row.status === '1' ? 'warning' : 'success',
      onClick: () => { void handleStatusChange(row) }
    },
    {
      default: () => (row.status === '1' ? '停用' : '启用'),
      icon: () => h(NIcon, null, { default: () => h(CheckmarkCircleOutline) })
    }
  )
}

function renderDeleteAction(row: SysTenant) {
  return h(
    NPopconfirm,
    { onPositiveClick: () => handleDelete(row) },
    {
      default: () => '删除租户后不可恢复，确认继续？',
      trigger: () =>
        h(
          NButton,
          {
            size: 'small',
            tertiary: true,
            type: 'error'
          },
          {
            default: () => '删除',
            icon: () => h(NIcon, null, { default: () => h(TrashOutline) })
          }
        )
    }
  )
}

function renderActionGroup(row: SysTenant) {
  const actions: ReturnType<typeof h>[] = []

  if (canEditTenant.value) {
    actions.push(renderEditAction(row), renderStatusAction(row))
  }

  if (canDeleteTenant.value) {
    actions.push(renderDeleteAction(row))
  }

  if (actions.length === 0) {
    return h(NText, { depth: 3 }, { default: () => '只读' })
  }

  return h(NSpace, { size: 4, justify: 'center' }, { default: () => actions })
}
</script>

<template>
  <div class="tenant-page">
    <section class="tenant-hero">
      <div class="tenant-hero__copy">
        <span class="tenant-hero__eyebrow">Tenant Control</span>
        <h1>租户管理</h1>
        <p>真实接口版租户工作台。支持分页查询、状态切换、创建编辑和删除，权限与后端 RBAC 保持一致。</p>
      </div>
      <NSpace :size="12" wrap>
        <NButton secondary type="default" @click="loadTenantList">
          <template #icon>
            <NIcon><RefreshOutline /></NIcon>
          </template>
          刷新列表
        </NButton>
        <NButton type="primary" :disabled="!canCreateTenant" @click="openCreateModal">
          <template #icon>
            <NIcon><AddOutline /></NIcon>
          </template>
          新建租户
        </NButton>
      </NSpace>
    </section>

    <NGrid cols="1 s:2 m:4" responsive="screen" :x-gap="16" :y-gap="16" class="tenant-stats">
      <NGridItem>
        <NCard :bordered="false" class="stat-card stat-card--slate">
          <NStatistic label="租户总数" :value="pagination.itemCount">
            <template #prefix>
              <NIcon><PeopleOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem>
        <NCard :bordered="false" class="stat-card stat-card--green">
          <NStatistic label="当前页启用" :value="enabledTenantCount">
            <template #prefix>
              <NIcon><CheckmarkCircleOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem>
        <NCard :bordered="false" class="stat-card stat-card--amber">
          <NStatistic label="当前页禁用" :value="disabledTenantCount">
            <template #prefix>
              <NIcon><ShieldCheckmarkOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem>
        <NCard :bordered="false" class="stat-card stat-card--rose">
          <NStatistic label="30 天内到期" :value="expiringSoonTenantCount">
            <template #prefix>
              <NIcon><CalendarOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
    </NGrid>

    <NCard :bordered="false" class="search-card">
      <NForm label-placement="top">
        <NGrid cols="1 s:2 m:4 l:5" responsive="screen" :x-gap="12" :y-gap="12">
          <NGridItem>
            <NFormItem label="租户名称">
              <NInput v-model:value="searchForm.tenantName" placeholder="按名称搜索" clearable @keyup.enter="handleSearch" />
            </NFormItem>
          </NGridItem>
          <NGridItem>
            <NFormItem label="租户编码">
              <NInput v-model:value="searchForm.tenantCode" placeholder="按编码搜索" clearable @keyup.enter="handleSearch" />
            </NFormItem>
          </NGridItem>
          <NGridItem>
            <NFormItem label="状态">
              <NSelect v-model:value="searchForm.status" :options="statusOptions" />
            </NFormItem>
          </NGridItem>
          <NGridItem span="2">
            <NFormItem label="操作">
              <NSpace>
                <NButton type="primary" :loading="listLoading" @click="handleSearch">
                  <template #icon>
                    <NIcon><SearchOutline /></NIcon>
                  </template>
                  搜索
                </NButton>
                <NButton secondary @click="handleReset">
                  <template #icon>
                    <NIcon><RefreshOutline /></NIcon>
                  </template>
                  重置
                </NButton>
              </NSpace>
            </NFormItem>
          </NGridItem>
        </NGrid>
      </NForm>
    </NCard>

    <NCard :bordered="false" class="table-card">
      <template #header>
        <div class="table-card__header">
          <div>
            <h2>租户列表</h2>
            <p>当前页 {{ currentPageTenantCount }} 条，支持后端分页与权限控制。</p>
          </div>
          <NTooltip>
            <template #trigger>
              <NTag size="small" type="info" :bordered="false">
                需要授权
              </NTag>
            </template>
            查看页面需要 `tenant:list`，新增/编辑/删除分别需要 `tenant:add`、`tenant:edit`、`tenant:remove`。
          </NTooltip>
        </div>
      </template>

      <NAlert v-if="!canViewTenants" type="warning" :show-icon="false" class="permission-alert">
        当前账号缺少 `tenant:list` 权限，无法读取租户列表。
      </NAlert>

      <NDataTable
        v-else
        remote
        striped
        class="tenant-table"
        :columns="tenantColumns"
        :data="tenantList"
        :loading="listLoading"
        :row-key="(row: SysTenant) => row.tenantId || row.tenantCode"
        :pagination="{
          page: pagination.page,
          pageSize: pagination.pageSize,
          pageCount: pagination.pageCount,
          itemCount: pagination.itemCount,
          showSizePicker: pagination.showSizePicker,
          pageSizes: pagination.pageSizes,
          onUpdatePage: handlePageChange,
          onUpdatePageSize: handlePageSizeChange
        }"
      />

      <NEmpty
        v-if="canViewTenants && !listLoading && tenantList.length === 0"
        description="当前筛选条件下没有租户数据"
        class="empty-state"
      />
    </NCard>

    <NModal
      v-model:show="modalVisible"
      preset="card"
      :title="isEditMode ? '编辑租户' : '新建租户'"
      :bordered="false"
      class="tenant-modal"
      :mask-closable="false"
    >
      <NScrollbar style="max-height: 72vh">
        <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="top">
          <NGrid cols="1 s:2" responsive="screen" :x-gap="16">
            <NGridItem>
              <NFormItem label="租户名称" path="tenantName">
                <NInput v-model:value="formModel.tenantName" placeholder="例如：华东运营中心" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="租户编码" path="tenantCode">
                <NInput v-model:value="formModel.tenantCode" placeholder="例如：east-ops" :disabled="detailLoading" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="联系人">
                <NInput v-model:value="formModel.contactName" placeholder="联系人姓名" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="联系电话">
                <NInput v-model:value="formModel.contactPhone" placeholder="联系电话" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="联系邮箱">
                <NInput v-model:value="formModel.contactEmail" placeholder="邮箱地址" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="状态">
                <NSelect v-model:value="formModel.status" :options="formStatusOptions" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="用户配额">
                <NInputNumber v-model:value="formModel.userLimit" :min="1" :precision="0" style="width: 100%" />
              </NFormItem>
            </NGridItem>
            <NGridItem>
              <NFormItem label="到期时间">
                <NDatePicker
                  v-model:formatted-value="formModel.expireTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  clearable
                  style="width: 100%"
                />
              </NFormItem>
            </NGridItem>
            <NGridItem span="2">
              <NFormItem label="备注">
                <NInput
                  v-model:value="formModel.remark"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 5 }"
                  placeholder="补充租户说明、业务范围或交付备注"
                />
              </NFormItem>
            </NGridItem>
          </NGrid>
        </NForm>
      </NScrollbar>

      <NDivider />
      <div class="tenant-modal__actions">
        <NButton secondary @click="modalVisible = false">取消</NButton>
        <NButton type="primary" :loading="submitLoading || detailLoading" @click="handleSubmit">
          {{ isEditMode ? '保存修改' : '创建租户' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.tenant-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  padding: 4px 0 24px;
  --tenant-bg: linear-gradient(135deg, #f5efe4 0%, #fbfaf6 48%, #eef4ef 100%);
  --tenant-surface: rgba(255, 255, 255, 0.88);
  --tenant-border: rgba(20, 33, 23, 0.08);
  --tenant-title: #16251d;
  --tenant-muted: #5d6e64;
  --tenant-shadow: 0 18px 50px rgba(34, 53, 42, 0.08);
}

.tenant-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  border: 1px solid var(--tenant-border);
  border-radius: 24px;
  background: var(--tenant-bg);
  box-shadow: var(--tenant-shadow);
}

.tenant-hero__copy {
  max-width: 720px;
}

.tenant-hero__eyebrow {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(22, 37, 29, 0.08);
  color: #2d4738;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.tenant-hero h1 {
  margin: 14px 0 8px;
  color: var(--tenant-title);
  font-size: 34px;
  line-height: 1.1;
}

.tenant-hero p {
  margin: 0;
  color: var(--tenant-muted);
  font-size: 15px;
  line-height: 1.75;
}

.tenant-stats,
.search-card,
.table-card {
  position: relative;
  border: 1px solid var(--tenant-border);
  border-radius: 24px;
  background: var(--tenant-surface);
  box-shadow: var(--tenant-shadow);
  backdrop-filter: blur(12px);
}

.stat-card {
  border-radius: 20px;
  overflow: hidden;
}

.stat-card :deep(.n-statistic) {
  color: #fff;
}

.stat-card--slate {
  background: linear-gradient(135deg, #1f312a 0%, #31493f 100%);
}

.stat-card--green {
  background: linear-gradient(135deg, #2d6d49 0%, #3e9b66 100%);
}

.stat-card--amber {
  background: linear-gradient(135deg, #8d5f1b 0%, #c6871c 100%);
}

.stat-card--rose {
  background: linear-gradient(135deg, #7c3940 0%, #b34e58 100%);
}

.search-card :deep(.n-card__content),
.table-card :deep(.n-card__content),
.table-card :deep(.n-card__header) {
  padding: 22px 24px;
}

.table-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.table-card__header h2 {
  margin: 0;
  color: var(--tenant-title);
  font-size: 20px;
}

.table-card__header p {
  margin: 6px 0 0;
  color: var(--tenant-muted);
  font-size: 13px;
}

.permission-alert {
  margin-bottom: 16px;
}

.tenant-table {
  min-height: 320px;
}

.empty-state {
  padding: 20px 0 4px;
}

.tenant-modal {
  width: min(760px, calc(100vw - 32px));
  border-radius: 24px;
}

.tenant-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .tenant-hero {
    flex-direction: column;
    padding: 22px;
  }

  .tenant-hero h1 {
    font-size: 28px;
  }
}

@media (max-width: 640px) {
  .table-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-card :deep(.n-card__content),
  .table-card :deep(.n-card__content),
  .table-card :deep(.n-card__header) {
    padding: 18px;
  }
}
</style>
