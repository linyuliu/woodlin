<script setup lang="ts">
/**
 * 用户管理（前端 mock）
 */
import { h, onMounted, ref, computed } from 'vue'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NCard,
  NPopconfirm,
  NSpace,
  NTag,
  NModal,
  NSelect,
  NInputNumber,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  fetchUserList,
  createUser,
  updateUser,
  deleteUsers,
  fetchDeptTree,
  fetchRoleTree,
  type UserItem,
  type DeptNode,
  type RoleNode
} from '@/api/mock/rbac'

type User = UserItem

const message = useMessage()
const loading = ref(false)

const searchForm = ref({
  username: '',
  nickname: ''
})

const users = ref<User[]>([])
const deptOptions = ref<{ label: string; value: number }[]>([])
const roleOptions = ref<{ label: string; value: number }[]>([])

const userModalShow = ref(false)
const userFormRef = ref<FormInst | null>(null)
const userForm = ref<Partial<User>>({
  username: '',
  nickname: '',
  status: '1',
  deptId: undefined,
  roleIds: [],
  email: '',
  mobile: ''
})

const userRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' }
}

const filteredUsers = computed(() =>
  users.value.filter(u => {
    const nameOk = searchForm.value.username ? u.username.includes(searchForm.value.username) : true
    const nickOk = searchForm.value.nickname ? (u.nickname || '').includes(searchForm.value.nickname) : true
    return nameOk && nickOk
  })
)

const statusTag = (status: string) => {
  const map: Record<string, { type: 'success' | 'warning'; text: string }> = {
    '1': { type: 'success', text: '启用' },
    '0': { type: 'warning', text: '禁用' }
  }
  const cfg = map[status] || { type: 'warning', text: '未知' }
  return h(NTag, { type: cfg.type, size: 'small' }, { default: () => cfg.text })
}

const columns: DataTableColumns<User> = [
  { title: '用户名', key: 'username', width: 140 },
  { title: '昵称', key: 'nickname', width: 140 },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  {
    title: '部门',
    key: 'deptId',
    width: 140,
    render: row => row.deptId ? deptOptions.value.find(d => d.value === row.deptId)?.label || row.deptId : '—'
  },
  {
    title: '角色',
    key: 'roleIds',
    width: 180,
    render: row => row.roleIds.map(rid => roleOptions.value.find(r => r.value === rid)?.label || rid).join('、') || '—'
  },
  { title: '状态', key: 'status', width: 90, align: 'center', render: row => statusTag(row.status) },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 210,
    align: 'center',
    render: row =>
      h(NSpace, { size: 6 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => handleEdit(row) },
          { default: () => '编辑', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.userId!) },
          {
            default: () => '确定删除该用户吗？',
            trigger: () =>
              h(
                NButton,
                { text: true, type: 'error', size: 'small' },
                { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }
              )
          }
        )
      ])
  }
]

const handleSearch = () => {
  // 本地过滤，computed 已处理
}

const handleReset = () => {
  searchForm.value = { username: '', nickname: '' }
}

const handleAdd = () => {
  userForm.value = {
    userId: undefined,
    username: '',
    nickname: '',
    status: '1',
    deptId: undefined,
    roleIds: [],
    email: '',
    mobile: ''
  }
  userModalShow.value = true
}

const handleEdit = (row: User) => {
  userForm.value = { ...row }
  userModalShow.value = true
}

const handleDelete = (id: number) => {
  deleteUsers([id]).then(() => {
    message.success('删除成功')
    loadUsers()
  })
}

const submitUser = async () => {
  await userFormRef.value?.validate()
  loading.value = true
  try {
    if (userForm.value.userId) {
      await updateUser(userForm.value as UserItem)
      message.success('更新成功')
    } else {
      await createUser(userForm.value)
      message.success('新增成功')
    }
    userModalShow.value = false
    await loadUsers()
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await fetchUserList({
      username: searchForm.value.username,
      nickname: searchForm.value.nickname
    })
  } finally {
    loading.value = false
  }
}

const loadDepsAndRoles = async () => {
  const depts = await fetchDeptTree()
  const roles = await fetchRoleTree()
  const buildDeptOptions = (nodes: DeptNode[], list: any[] = []) => {
    nodes.forEach(n => {
      list.push({ label: n.deptName, value: n.deptId })
      if (n.children) buildDeptOptions(n.children, list)
    })
    return list
  }
  const buildRoleOptions = (nodes: RoleNode[], list: any[] = []) => {
    nodes.forEach(n => {
      list.push({ label: n.roleName, value: n.roleId })
      if (n.children) buildRoleOptions(n.children, list)
    })
    return list
  }
  deptOptions.value = buildDeptOptions(depts)
  roleOptions.value = buildRoleOptions(roles)
}

onMounted(async () => {
  await Promise.all([loadDepsAndRoles(), loadUsers()])
})
</script>

<template>
  <div class="user-page">
    <div class="toolbar">
      <NForm :model="searchForm" inline label-placement="left">
        <NFormItem label="用户名">
          <NInput
            v-model:value="searchForm.username"
            clearable
            placeholder="请输入用户名"
            style="width: 180px"
          />
        </NFormItem>
        <NFormItem label="昵称">
          <NInput
            v-model:value="searchForm.nickname"
            clearable
            placeholder="请输入昵称"
            style="width: 180px"
          />
        </NFormItem>
        <NFormItem>
          <NSpace :size="12">
            <NButton :loading="loading" type="primary" @click="handleSearch">
              <template #icon>
                <NIcon>
                  <SearchOutline/>
                </NIcon>
              </template>
              搜索
            </NButton>
            <NButton @click="handleReset">
              <template #icon>
                <NIcon>
                  <RefreshOutline/>
                </NIcon>
              </template>
              重置
            </NButton>
            <NButton type="primary" secondary @click="handleAdd">
              <template #icon>
                <NIcon><AddOutline/></NIcon>
              </template>
              新增
            </NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </div>

    <NDataTable
      :bordered="false"
      :columns="columns"
      :data="filteredUsers"
      :loading="loading"
      :pagination="{
        pageSize: 10,
        showSizePicker: true,
        pageSizes: [10, 20, 50],
        showQuickJumper: true
      }"
      :single-line="false"
      class="data-table"
      striped
    />

    <NModal
      v-model:show="userModalShow"
      preset="card"
      :title="userForm.userId ? '编辑用户' : '新增用户'"
      style="width: 560px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="userFormRef" :model="userForm" :rules="userRules" label-placement="left" label-width="100">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="userForm.username" placeholder="唯一用户名" />
        </NFormItem>
        <NFormItem label="昵称">
          <NInput v-model:value="userForm.nickname" placeholder="可选" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="userForm.email" placeholder="user@example.com" />
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
          <NInput v-model:value="userForm.remark" type="textarea" placeholder="可填写业务说明" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="userModalShow = false">取消</NButton>
          <NButton type="primary" :loading="loading" @click="submitUser">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.user-page {
  padding: 12px 0;
}
.data-table {
  margin-top: 8px;
}
.data-table :deep(.n-data-table-th) {
  font-weight: 600;
}
.data-table :deep(.n-data-table-td) {
  padding: 12px 16px;
}
</style>
