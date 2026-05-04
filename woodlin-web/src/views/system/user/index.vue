<!--
  @file views/system/user/index.vue
  @description 用户管理：列表 + 搜索 + 新增/编辑抽屉 + 重置密码 + 状态切换 + 角色分配
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NDataTable,
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
  NSwitch,
  NTag,
  NTreeSelect,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeSelectOption,
} from 'naive-ui'
import {
  changeUserStatus,
  createUser,
  deleteUser,
  pageUsers,
  resetUserPassword,
  updateUser,
  type SysUser,
  type UserQuery,
} from '@/api/system/user'
import { pageRoles, type SysRole } from '@/api/system/role'
import { getDeptTree, type SysDept } from '@/api/system/dept'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<SysUser[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<UserQuery>({
  page: 1,
  size: 10,
  username: '',
  nickname: '',
  mobile: '',
  deptId: undefined,
  status: undefined,
})

const deptOptions: Ref<TreeSelectOption[]> = ref([])
const roleOptions: Ref<SelectOption[]> = ref([])

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInst | null>(null)
const formData = reactive<SysUser>({
  username: '',
  nickname: '',
  mobile: '',
  email: '',
  deptId: undefined,
  gender: '0',
  status: '1',
  password: '',
  roleIds: [],
})
const isEdit = ref(false)

const resetVisible = ref(false)
const resetForm = reactive({ id: 0, password: '' })

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const genderOptions: SelectOption[] = [
  { label: '男', value: '0' },
  { label: '女', value: '1' },
  { label: '未知', value: '2' },
]

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

/** 转换部门树为 NTreeSelect 选项 */
function mapDeptTree(list: SysDept[]): TreeSelectOption[] {
  return list.map((d) => ({
    key: d.deptId ?? d.id ?? 0,
    label: d.deptName,
    children: d.children && d.children.length ? mapDeptTree(d.children) : undefined,
  }))
}

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageUsers(query)
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
  query.username = ''
  query.nickname = ''
  query.mobile = ''
  query.deptId = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

/** 打开新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增用户'
  Object.assign(formData, {
    id: undefined,
    username: '',
    nickname: '',
    mobile: '',
    email: '',
    deptId: undefined,
    gender: '0',
    status: '1',
    password: '',
    roleIds: [],
  })
  drawerVisible.value = true
}

/** 打开编辑 */
function openEdit(row: SysUser): void {
  isEdit.value = true
  drawerTitle.value = '编辑用户'
  Object.assign(formData, { ...row, password: '', roleIds: row.roleIds ?? [] })
  drawerVisible.value = true
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      const { password: _omit, ...rest } = formData
      void _omit
      await updateUser(formData.id, rest as SysUser)
      message.success('更新成功')
    } else {
      await createUser(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除用户 */
function handleDelete(row: SysUser): void {
  if (!row.id) return
  dialog.warning({
    title: '提示',
    content: `确认删除用户 ${row.username} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteUser(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

/** 切换用户状态 */
async function handleStatus(row: SysUser, val: string): Promise<void> {
  if (!row.id) return
  await changeUserStatus(row.id, val)
  row.status = val
  message.success('状态已更新')
}

/** 打开重置密码 */
function openReset(row: SysUser): void {
  if (!row.id) return
  resetForm.id = row.id
  resetForm.password = ''
  resetVisible.value = true
}

/** 提交重置密码 */
async function submitReset(): Promise<void> {
  if (!resetForm.password) {
    message.warning('请输入新密码')
    return
  }
  await resetUserPassword(resetForm.id, resetForm.password)
  message.success('重置成功')
  resetVisible.value = false
}

const columns: DataTableColumns<SysUser> = [
  { type: 'selection' },
  { title: '用户名', key: 'username', width: 130 },
  { title: '昵称', key: 'nickname', width: 130 },
  { title: '手机号', key: 'mobile', width: 130 },
  { title: '邮箱', key: 'email', width: 180 },
  { title: '部门', key: 'deptName', width: 140 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(NSwitch, {
        value: row.status === '1',
        checkedValue: true,
        uncheckedValue: false,
        onUpdateValue: (v: boolean) => handleStatus(row, v ? '1' : '0'),
      }),
  },
  {
    title: '上次登录',
    key: 'lastLoginTime',
    width: 170,
    render: (row) => row.lastLoginTime ?? '-',
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', type: 'primary', text: true, onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          { size: 'small', type: 'warning', text: true, onClick: () => openReset(row) },
          { default: () => '重置密码' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(NButton, { size: 'small', type: 'error', text: true }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

onMounted(async () => {
  void refresh()
  const tree = await getDeptTree().catch(() => [] as SysDept[])
  deptOptions.value = mapDeptTree(tree)
  const roles = await pageRoles({ page: 1, size: 100 }).catch(
    () => ({ records: [], total: 0, current: 1, size: 100 }),
  )
  roleOptions.value = (roles.records as SysRole[]).map((r) => ({
    label: r.roleName,
    value: r.id as number,
  }))
})
</script>

<template>
  <div class="page-user">
    <n-card size="small" class="search-card">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="用户名">
          <n-input v-model:value="query.username" placeholder="用户名" clearable />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="query.nickname" placeholder="昵称" clearable />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="query.mobile" placeholder="手机号" clearable />
        </n-form-item>
        <n-form-item label="部门">
          <n-tree-select
            v-model:value="query.deptId"
            :options="deptOptions"
            placeholder="选择部门"
            clearable
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

    <n-card size="small" class="table-card">
      <div class="toolbar">
        <n-button v-permission="'system:user:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysUser) => row.id as number"
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
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="formData.username" :disabled="isEdit" />
          </n-form-item>
          <n-form-item v-if="!isEdit" label="密码" path="password">
            <n-input v-model:value="formData.password" type="password" show-password-on="click" />
          </n-form-item>
          <n-form-item label="昵称" path="nickname">
            <n-input v-model:value="formData.nickname" />
          </n-form-item>
          <n-form-item label="手机号" path="mobile">
            <n-input v-model:value="formData.mobile" />
          </n-form-item>
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="formData.email" />
          </n-form-item>
          <n-form-item label="部门" path="deptId">
            <n-tree-select
              v-model:value="formData.deptId"
              :options="deptOptions"
              clearable
              placeholder="选择部门"
            />
          </n-form-item>
          <n-form-item label="性别" path="gender">
            <n-select v-model:value="formData.gender" :options="genderOptions" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="角色" path="roleIds">
            <n-select
              v-model:value="formData.roleIds"
              :options="roleOptions"
              multiple
              placeholder="分配角色"
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

    <n-modal
      v-model:show="resetVisible"
      preset="dialog"
      title="重置密码"
      positive-text="确定"
      negative-text="取消"
      @positive-click="submitReset"
    >
      <n-form :model="resetForm" label-placement="top">
        <n-form-item label="新密码" required>
          <n-input
            v-model:value="resetForm.password"
            type="password"
            show-password-on="click"
            placeholder="请输入新密码"
          />
        </n-form-item>
      </n-form>
    </n-modal>

    <n-tag v-if="false">{{ '' }}</n-tag>
  </div>
</template>

<style scoped>
.page-user {
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
