<!--
  @file views/tenant/package/index.vue
  @description 租户套餐管理：列表 + 搜索 + 新增/编辑抽屉 + 菜单分配（复选树）
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
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  NTree,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeOption,
} from 'naive-ui'
import {
  createPackage,
  deletePackage,
  getPackagePage,
  updatePackage,
  type PackageQuery,
  type SysTenantPackage,
} from '@/api/tenant'
import { getMenuTree } from '@/api/system/menu'
import type { RouteItem } from '@/types/global'
import { usePermission } from '@/composables/usePermission'

const message = useMessage()
const dialog = useDialog()
const { hasPermission } = usePermission()

const tableData: Ref<SysTenantPackage[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<PackageQuery>({
  page: 1,
  size: 10,
  packageName: '',
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

/** 表单初始值工厂 */
function emptyForm(): SysTenantPackage {
  return {
    id: undefined,
    packageName: '',
    menuIds: [],
    status: '1',
    remark: '',
  }
}

const formData = reactive<SysTenantPackage>(emptyForm())
const menuTreeData: Ref<TreeOption[]> = ref([])
const checkedMenuKeys: Ref<Array<string | number>> = ref([])
const menuLoading = ref(false)

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const rules: FormRules = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
}

/**
 * 过滤并转换菜单树为 TreeOption（仅保留目录/菜单：type 1 & 2，过滤按钮）
 * @param list 后端菜单/权限树
 */
function mapMenuTree(list: RouteItem[]): TreeOption[] {
  return list
    .filter((m) => m.type === 1 || m.type === 2)
    .map((m) => ({
      key: m.id,
      label: m.title,
      children: m.children && m.children.length ? mapMenuTree(m.children) : undefined,
    }))
}

/** 拉取菜单树 */
async function loadMenuTree(): Promise<void> {
  if (menuTreeData.value.length) {return}
  menuLoading.value = true
  try {
    const tree = await getMenuTree()
    menuTreeData.value = mapMenuTree(tree)
  } finally {
    menuLoading.value = false
  }
}

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await getPackagePage(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function handleSearch(): void {
  query.page = 1
  void refresh()
}

/** 重置搜索 */
function handleReset(): void {
  query.packageName = ''
  query.status = undefined
  query.page = 1
  void refresh()
}

/** 打开新增 */
async function openAdd(): Promise<void> {
  isEdit.value = false
  drawerTitle.value = '新增套餐'
  Object.assign(formData, emptyForm())
  checkedMenuKeys.value = []
  drawerVisible.value = true
  await loadMenuTree()
}

/** 打开编辑 */
async function openEdit(row: SysTenantPackage): Promise<void> {
  isEdit.value = true
  drawerTitle.value = '编辑套餐'
  Object.assign(formData, { ...row, menuIds: row.menuIds ?? [] })
  checkedMenuKeys.value = (row.menuIds ?? []) as number[]
  drawerVisible.value = true
  await loadMenuTree()
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  formData.menuIds = checkedMenuKeys.value as number[]
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updatePackage(formData.id, formData)
      message.success('更新成功')
    } else {
      await createPackage(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除套餐 */
function handleDelete(row: SysTenantPackage): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除套餐 ${row.packageName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deletePackage(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<SysTenantPackage> = [
  { title: '套餐名称', key: 'packageName', width: 200 },
  {
    title: '菜单数',
    key: 'menuCount',
    width: 100,
    render: (row) => row.menuCount ?? (row.menuIds?.length ?? 0),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.status === '1' ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === '1' ? '启用' : '禁用') },
      ),
  },
  { title: '备注', key: 'remark' },
  { title: '创建时间', key: 'createTime', width: 170 },
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
      if (hasPermission('tenant:package:delete')) {
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
})
</script>

<template>
  <div class="page-package">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="套餐名称">
          <n-input v-model:value="query.packageName" placeholder="套餐名称" clearable />
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
        <n-button v-permission="'tenant:package:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysTenantPackage) => row.id as number"
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

    <n-drawer v-model:show="drawerVisible" :width="560">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="套餐名称" path="packageName">
            <n-input v-model:value="formData.packageName" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="备注" path="remark">
            <n-input v-model:value="formData.remark" type="textarea" />
          </n-form-item>
          <n-form-item label="分配菜单">
            <n-tree
              v-model:checked-keys="checkedMenuKeys"
              :data="menuTreeData"
              checkable
              cascade
              :selectable="false"
              block-line
              :loading="menuLoading"
              style="width: 100%"
            />
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
.page-package {
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
