<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type SelectOption
} from 'naive-ui'
import {
  AddOutline,
  CloudDownloadOutline,
  CloudUploadOutline,
  CreateOutline,
  DocumentTextOutline,
  KeyOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import { getDeptTree, type SysDept } from '@/api/dept'
import { getRoleTree, type RoleTreeNode } from '@/api/role'
import {
  addUser,
  deleteUser,
  downloadUserImportTemplate,
  exportUser,
  getUserById,
  getUserList,
  importUserData,
  resetUserPassword,
  updateUser,
  type SysUser,
  type UserListParams
} from '@/api/user'
import { logger } from '@/utils/logger'
import { useUserStore } from '@/stores'
import {PERMISSIONS} from '@/constants/permission-keys'

interface UserSearchForm {
  username: string
  nickname: string
}

interface UserFormData {
  userId?: number
  username: string
  password: string
  nickname: string
  email: string
  mobile: string
  deptId: number | null
  roleIds: number[]
  status: string
  remark: string
}

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const loading = ref(false)

const searchForm = ref<UserSearchForm>({
  username: '',
  nickname: ''
})

const userList = ref<SysUser[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const userImportInputRef = ref<HTMLInputElement | null>(null)

const deptOptions = ref<SelectOption[]>([])
const roleOptions = ref<SelectOption[]>([])

const userModalVisible = ref(false)
const userFormRef = ref<FormInst | null>(null)
const userForm = ref<UserFormData>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  mobile: '',
  deptId: null,
  roleIds: [],
  status: '1',
  remark: ''
})

const userRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' }
}

const totalUsers = computed(() => total.value)
const canViewUsers = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_LIST))
const canCreateUser = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_ADD))
const canUpdateUser = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_EDIT))
const canDeleteUser = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_REMOVE))
const canExportUsers = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_EXPORT))
const canImportUsers = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_IMPORT))
const canResetUserPassword = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_USER_RESET_PWD))
const roleNameMap = computed(() => {
  const map = new Map<number, string>()
  for (const option of roleOptions.value) {
    if (typeof option.value === 'number' && typeof option.label === 'string') {
      map.set(option.value, option.label)
    }
  }
  return map
})

const deptNameMap = computed(() => {
  const map = new Map<number, string>()
  for (const option of deptOptions.value) {
    if (typeof option.value === 'number' && typeof option.label === 'string') {
      map.set(option.value, option.label)
    }
  }
  return map
})

const pagination = computed(() => ({
  page: pageNum.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pageNum.value = page
    loadUserList()
  },
  onUpdatePageSize: (size: number) => {
    pageSize.value = size
    pageNum.value = 1
    loadUserList()
  }
}))

// eslint-disable-next-line max-lines-per-function
const renderUserActionButtons = (row: SysUser) => {
  const actionButtons: ReturnType<typeof h>[] = []

  if (canUpdateUser.value) {
    actionButtons.push(
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => openEdit(row)
        },
        {
          default: () => '编辑',
          icon: () => h(NIcon, null, { default: () => h(CreateOutline) })
        }
      )
    )
  }

  if (canResetUserPassword.value) {
    actionButtons.push(
      h(
        NButton,
        {
          text: true,
          type: 'warning',
          size: 'small',
          onClick: () => openResetPwd(row)
        },
        {
          default: () => '重置密码',
          icon: () => h(NIcon, null, { default: () => h(KeyOutline) })
        }
      )
    )
  }

  const renderDeleteTrigger = () =>
    h(
      NButton,
      {
        text: true,
        type: 'error',
        size: 'small'
      },
      {
        default: () => '删除',
        icon: () => h(NIcon, null, { default: () => h(TrashOutline) })
      }
    )

  if (canDeleteUser.value) {
    actionButtons.push(
      h(
        NPopconfirm,
        {
          onPositiveClick: () => handleDelete(row)
        },
        {
          default: () => '确定删除该用户吗？',
          trigger: renderDeleteTrigger
        }
      )
    )
  }

  if (actionButtons.length === 0) {
    return h(NTag, { size: 'small' }, { default: () => '只读' })
  }

  return h(NSpace, { size: 4 }, () => actionButtons)
}

const columns: DataTableColumns<SysUser> = [
  { title: '用户名', key: 'username', width: 140 },
  { title: '昵称', key: 'nickname', width: 140 },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  { title: '手机号', key: 'mobile', width: 140 },
  {
    title: '部门',
    key: 'deptId',
    width: 150,
    render: row => {
      if (!row.deptId) {
        return '—'
      }
      return deptNameMap.value.get(row.deptId) || `#${row.deptId}`
    }
  },
  {
    title: '角色',
    key: 'roleIds',
    width: 200,
    render: row => {
      const ids = row.roleIds || []
      if (!ids.length) {
        return '—'
      }
      return ids.map(id => roleNameMap.value.get(id) || `#${id}`).join('、')
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    align: 'center',
    render: row =>
      h(
        NTag,
        { size: 'small', type: row.status === '1' ? 'success' : 'warning' },
        { default: () => (row.status === '1' ? '启用' : '禁用') }
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    align: 'center',
    render: renderUserActionButtons
  }
]

const USER_EXPORT_FILENAME = '用户列表.xlsx'
const USER_IMPORT_TEMPLATE_FILENAME = '用户导入模板.xlsx'

const buildUserListParams = (): UserListParams => ({
  pageNum: pageNum.value,
  pageSize: pageSize.value,
  username: searchForm.value.username.trim() || undefined,
  nickname: searchForm.value.nickname.trim() || undefined
})

const downloadBlob = (blob: Blob, fileName: string) => {
  const downloadUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(downloadUrl)
}

const resolveDownloadBlob = async (blob: Blob): Promise<Blob> => {
  if (!blob.type.includes('application/json')) {
    return blob
  }

  const text = await blob.text()
  try {
    const payload = JSON.parse(text) as { message?: string }
    throw new Error(payload.message || '文件下载失败')
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('文件下载失败')
  }
}

const loadBaseOptions = async () => {
  try {
    const [roles, depts] = await Promise.all([getRoleTree(), getDeptTree()])
    roleOptions.value = flattenRoleTree(roles)
    deptOptions.value = flattenDeptTree(depts)
  } catch (error) {
    logger.error('加载角色/部门选项失败', error)
    message.error('加载角色/部门选项失败')
  }
}

const loadUserList = async () => {
  if (!canViewUsers.value) {
    userList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const result = await getUserList(buildUserListParams())

    const users = result.data || []
    const enriched = await enrichUserRoles(users)

    userList.value = enriched
    total.value = result.total || 0
  } catch (error) {
    logger.error('加载用户列表失败', error)
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleExportUsers = async () => {
  if (!canExportUsers.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_EXPORT} 权限`)
    return
  }

  loading.value = true
  try {
    const blob = await resolveDownloadBlob(await exportUser(buildUserListParams()))
    downloadBlob(blob, USER_EXPORT_FILENAME)
    message.success('导出成功')
  } catch (error) {
    logger.error('导出用户列表失败', error)
    message.error('导出失败')
  } finally {
    loading.value = false
  }
}

const handleDownloadImportTemplate = async () => {
  if (!canImportUsers.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_IMPORT} 权限`)
    return
  }

  loading.value = true
  try {
    const blob = await resolveDownloadBlob(await downloadUserImportTemplate())
    downloadBlob(blob, USER_IMPORT_TEMPLATE_FILENAME)
    message.success('模板下载成功')
  } catch (error) {
    logger.error('下载用户导入模板失败', error)
    message.error('模板下载失败')
  } finally {
    loading.value = false
  }
}

const triggerUserImport = () => {
  if (!canImportUsers.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_IMPORT} 权限`)
    return
  }
  userImportInputRef.value?.click()
}

const resetImportInput = (input: HTMLInputElement | null) => {
  if (input) {
    input.value = ''
  }
}

const handleImportFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]
  if (!file) {
    resetImportInput(input)
    return
  }

  dialog.warning({
    title: '导入用户',
    content: `确认导入文件 ${file.name}？当前按新增模式导入，已存在用户不会覆盖。`,
    positiveText: '开始导入',
    negativeText: '取消',
    async onPositiveClick() {
      loading.value = true
      try {
        const result = await importUserData(file, false)
        message.success(result || '导入成功')
        await Promise.all([loadBaseOptions(), loadUserList()])
      } catch (error) {
        logger.error('导入用户失败', error)
        message.error('导入失败')
      } finally {
        loading.value = false
        resetImportInput(input)
      }
    },
    onNegativeClick() {
      resetImportInput(input)
    }
  })
}

const enrichUserRoles = async (users: SysUser[]): Promise<SysUser[]> => {
  const roleByUserId = new Map<number, number[]>()

  const tasks = users
    .filter(item => typeof item.userId === 'number')
    .map(async item => {
      const detail = await getUserById(item.userId as number)
      roleByUserId.set(item.userId as number, detail.roleIds || [])
    })

  await Promise.allSettled(tasks)

  return users.map(item => {
    if (typeof item.userId !== 'number') {
      return item
    }
    return {
      ...item,
      roleIds: roleByUserId.get(item.userId) || item.roleIds || []
    }
  })
}

const handleSearch = () => {
  pageNum.value = 1
  loadUserList()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    nickname: ''
  }
  pageNum.value = 1
  loadUserList()
}

const openAdd = () => {
  if (!canCreateUser.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_ADD} 权限`)
    return
  }
  userForm.value = {
    username: '',
    password: '',
    nickname: '',
    email: '',
    mobile: '',
    deptId: null,
    roleIds: [],
    status: '1',
    remark: ''
  }
  userModalVisible.value = true
}

// eslint-disable-next-line complexity
const openEdit = async (row: SysUser) => {
  if (!canUpdateUser.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_EDIT} 权限`)
    return
  }
  if (!row.userId) {
    return
  }

  loading.value = true
  try {
    const detail = await getUserById(row.userId)
    userForm.value = {
      userId: detail.userId,
      username: detail.username,
      password: '',
      nickname: detail.nickname || '',
      email: detail.email || '',
      mobile: detail.mobile || '',
      deptId: detail.deptId ?? null,
      roleIds: detail.roleIds || [],
      status: String(detail.status || '1'),
      remark: detail.remark || ''
    }
    userModalVisible.value = true
  } catch (error) {
    logger.error('加载用户详情失败', error)
    message.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}

const submitUser = async () => {
  await userFormRef.value?.validate()

  const payload: SysUser = {
    userId: userForm.value.userId,
    username: userForm.value.username.trim(),
    password: userForm.value.password.trim() || undefined,
    nickname: userForm.value.nickname.trim(),
    email: userForm.value.email.trim(),
    mobile: userForm.value.mobile.trim(),
    deptId: userForm.value.deptId || undefined,
    roleIds: userForm.value.roleIds,
    status: userForm.value.status,
    remark: userForm.value.remark.trim()
  }

  loading.value = true
  try {
    if (payload.userId) {
      if (!canUpdateUser.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_EDIT} 权限`)
        return
      }
      await updateUser(payload)
      message.success('修改成功')
    } else {
      if (!canCreateUser.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_ADD} 权限`)
        return
      }
      await addUser(payload)
      message.success('新增成功')
    }
    userModalVisible.value = false
    await loadUserList()
  } catch (error) {
    logger.error('保存用户失败', error)
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

const openResetPwd = (row: SysUser) => {
  if (!canResetUserPassword.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_RESET_PWD} 权限`)
    return
  }
  if (!row.userId) {
    return
  }
  dialog.warning({
    title: '重置密码',
    content: `确认将用户 ${row.username} 的密码重置为默认密码 12345678？`,
    positiveText: '确认',
    negativeText: '取消',
    async onPositiveClick() {
      try {
        await resetUserPassword(row.userId as number, '12345678')
        message.success('密码重置成功')
      } catch (error) {
        logger.error('重置用户密码失败', error)
        message.error('密码重置失败')
      }
    }
  })
}

const handleDelete = async (row: SysUser) => {
  if (!canDeleteUser.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_REMOVE} 权限`)
    return
  }
  if (!row.userId) {
    return
  }

  loading.value = true
  try {
    await deleteUser([row.userId])
    message.success('删除成功')
    await loadUserList()
  } catch (error) {
    logger.error('删除用户失败', error)
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

const flattenRoleTree = (nodes: RoleTreeNode[], options: SelectOption[] = [], depth = 0): SelectOption[] => {
  const prefix = depth > 0 ? `${'  '.repeat(depth)}└ ` : ''

  for (const node of nodes) {
    options.push({
      label: `${prefix}${node.roleName}`,
      value: node.roleId
    })

    if (node.children && node.children.length > 0) {
      flattenRoleTree(node.children, options, depth + 1)
    }
  }

  return options
}

const flattenDeptTree = (nodes: SysDept[], options: SelectOption[] = [], depth = 0): SelectOption[] => {
  const prefix = depth > 0 ? `${'  '.repeat(depth)}└ ` : ''

  for (const node of nodes) {
    if (typeof node.deptId === 'number') {
      options.push({
        label: `${prefix}${node.deptName}`,
        value: node.deptId
      })
    }

    if (node.children && node.children.length > 0) {
      flattenDeptTree(node.children, options, depth + 1)
    }
  }

  return options
}

onMounted(async () => {
  if (!canViewUsers.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_USER_LIST} 权限`)
    return
  }
  await loadBaseOptions()
  await loadUserList()
})
</script>

<template>
  <div class="user-page">
    <NCard :bordered="false" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>用户账号管理</h2>
          <p>维护登录账号、角色归属和部门归属，账号权限由角色继承得到。</p>
        </div>
        <NTag type="info" size="small">共 {{ totalUsers }} 个账号</NTag>
      </div>
    </NCard>

    <NCard :bordered="false" class="toolbar-card">
      <NForm inline :model="searchForm" label-placement="left">
        <NFormItem label="用户名">
          <NInput v-model:value="searchForm.username" clearable placeholder="请输入用户名" style="width: 180px" />
        </NFormItem>
        <NFormItem label="昵称">
          <NInput v-model:value="searchForm.nickname" clearable placeholder="请输入昵称" style="width: 180px" />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" :loading="loading" :disabled="!canViewUsers" @click="handleSearch">
              <template #icon>
                <NIcon><SearchOutline /></NIcon>
              </template>
              搜索
            </NButton>
            <NButton :disabled="!canViewUsers" @click="handleReset">
              <template #icon>
                <NIcon><RefreshOutline /></NIcon>
              </template>
              重置
            </NButton>
            <NButton v-if="canCreateUser" type="primary" secondary @click="openAdd">
              <template #icon>
                <NIcon><AddOutline /></NIcon>
              </template>
              新增用户
            </NButton>
            <NButton v-if="canExportUsers" secondary :loading="loading" @click="handleExportUsers">
              <template #icon>
                <NIcon><CloudDownloadOutline /></NIcon>
              </template>
              导出用户
            </NButton>
            <NButton v-if="canImportUsers" secondary :loading="loading" @click="handleDownloadImportTemplate">
              <template #icon>
                <NIcon><DocumentTextOutline /></NIcon>
              </template>
              下载模板
            </NButton>
            <NButton v-if="canImportUsers" type="primary" quaternary :loading="loading" @click="triggerUserImport">
              <template #icon>
                <NIcon><CloudUploadOutline /></NIcon>
              </template>
              导入用户
            </NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" title="用户列表" class="table-card">
      <NDataTable
        :columns="columns"
        :data="userList"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        striped
      />
    </NCard>

    <input
      ref="userImportInputRef"
      accept=".xls,.xlsx"
      class="hidden-file-input"
      type="file"
      @change="handleImportFileChange"
    />

    <NModal
      v-model:show="userModalVisible"
      preset="card"
      :title="userForm.userId ? '编辑用户' : '新增用户'"
      style="width: 620px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="userFormRef" :model="userForm" :rules="userRules" label-placement="left" label-width="100">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="userForm.username" :disabled="!!userForm.userId" placeholder="登录用户名" />
        </NFormItem>
        <NFormItem label="密码" :required="!userForm.userId">
          <NInput
            v-model:value="userForm.password"
            type="password"
            show-password-on="click"
            :placeholder="userForm.userId ? '留空表示不修改密码' : '留空使用系统默认密码'"
          />
        </NFormItem>
        <NFormItem label="昵称">
          <NInput v-model:value="userForm.nickname" placeholder="可选" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="userForm.email" placeholder="可选" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="userForm.mobile" placeholder="可选" />
        </NFormItem>
        <NFormItem label="部门">
          <NSelect v-model:value="userForm.deptId" :options="deptOptions" clearable placeholder="请选择部门" />
        </NFormItem>
        <NFormItem label="角色">
          <NSelect
            v-model:value="userForm.roleIds"
            multiple
            :options="roleOptions"
            placeholder="请选择角色"
          />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect
            v-model:value="userForm.status"
            :options="[
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
            ]"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="userForm.remark" type="textarea" placeholder="可选" />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="userModalVisible = false">取消</NButton>
          <NButton
            type="primary"
            :loading="loading"
            :disabled="(userForm.userId && !canUpdateUser) || (!userForm.userId && !canCreateUser)"
            @click="submitUser"
          >
            保存
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #8b1e3f 0%, #c1292e 52%, #ea5f55 100%);
}

.hidden-file-input {
  display: none;
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-color-inverse);
}

.hero-content h2 {
  margin: 0 0 6px;
  color: var(--text-color-inverse);
}

.hero-content p {
  margin: 0;
  color: color-mix(in srgb, var(--text-color-inverse) 84%, transparent);
}

.toolbar-card,
.table-card {
  background: var(--bg-color);
}
</style>
