<!--
  @file views/system/role/index.vue
  @description 角色管理：列表 + 搜索 + 新增/编辑抽屉 + 菜单/权限分配模态框
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
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
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
  assignRoleMenus,
  createRole,
  deleteRole,
  getRoleMenus,
  pageRoles,
  updateRole,
  type RoleQuery,
  type SysRole,
} from '@/api/system/role'
import { getMenuTree } from '@/api/system/menu'
import type { RouteItem } from '@/types/global'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<SysRole[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<RoleQuery>({
  page: 1,
  size: 10,
  roleName: '',
  roleCode: '',
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const formData = reactive<SysRole>({
  roleName: '',
  roleCode: '',
  status: '1',
  remark: '',
  sort: 0,
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const menuModalVisible = ref(false)
const menuTreeData: Ref<TreeOption[]> = ref([])
const checkedMenuKeys: Ref<Array<string | number>> = ref([])
const currentRoleId = ref<number>(0)
const menuLoading = ref(false)

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageRoles(query)
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
  query.roleName = ''
  query.roleCode = ''
  query.status = undefined
  query.page = 1
  void refresh()
}

/** 打开新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增角色'
  Object.assign(formData, {
    id: undefined,
    roleName: '',
    roleCode: '',
    status: '1',
    remark: '',
    sort: 0,
  })
  drawerVisible.value = true
}

/** 打开编辑 */
function openEdit(row: SysRole): void {
  isEdit.value = true
  drawerTitle.value = '编辑角色'
  Object.assign(formData, row)
  drawerVisible.value = true
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateRole(formData.id, formData)
      message.success('更新成功')
    } else {
      await createRole(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除 */
function handleDelete(row: SysRole): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除角色 ${row.roleName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteRole(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

/** 菜单树转 TreeOption */
function mapMenuTree(list: RouteItem[]): TreeOption[] {
  return list.map((m) => ({
    key: m.id,
    label: m.title,
    children: m.children && m.children.length ? mapMenuTree(m.children) : undefined,
  }))
}

/** 打开菜单分配 */
async function openMenuAssign(row: SysRole): Promise<void> {
  if (!row.id) {return}
  currentRoleId.value = row.id
  menuModalVisible.value = true
  menuLoading.value = true
  try {
    const [tree, assigned] = await Promise.all([
      getMenuTree(),
      getRoleMenus(row.id),
    ])
    menuTreeData.value = mapMenuTree(tree)
    checkedMenuKeys.value = assigned ?? []
  } finally {
    menuLoading.value = false
  }
}

/** 提交菜单分配 */
async function submitMenuAssign(): Promise<void> {
  await assignRoleMenus(currentRoleId.value, checkedMenuKeys.value as number[])
  message.success('保存成功')
  menuModalVisible.value = false
}

const columns: DataTableColumns<SysRole> = [
  { title: '角色名称', key: 'roleName', width: 160 },
  { title: '角色编码', key: 'roleCode', width: 160 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => (row.status === '1' ? '启用' : '禁用'),
  },
  { title: '备注', key: 'remark' },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 240,
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
          { size: 'small', text: true, type: 'info', onClick: () => openMenuAssign(row) },
          { default: () => '分配菜单' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(NButton, { size: 'small', text: true, type: 'error' }, { default: () => '删除' }),
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
  <div class="page-role">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="角色名称">
          <n-input v-model:value="query.roleName" placeholder="角色名称" clearable />
        </n-form-item>
        <n-form-item label="角色编码">
          <n-input v-model:value="query.roleCode" placeholder="角色编码" clearable />
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
        <n-button v-permission="'system:role:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysRole) => row.id as number"
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
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="角色名称" path="roleName">
            <n-input v-model:value="formData.roleName" />
          </n-form-item>
          <n-form-item label="角色编码" path="roleCode">
            <n-input v-model:value="formData.roleCode" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" />
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
      v-model:show="menuModalVisible"
      preset="card"
      title="分配菜单"
      style="width: 480px"
    >
      <n-tree
        v-model:checked-keys="checkedMenuKeys"
        :data="menuTreeData"
        checkable
        cascade
        :selectable="false"
        block-line
        :loading="menuLoading"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="menuModalVisible = false">取消</n-button>
          <n-button type="primary" @click="submitMenuAssign">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-role {
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
