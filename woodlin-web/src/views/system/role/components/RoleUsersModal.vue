<!--
  @file RoleUsersModal.vue
  @description 角色成员管理弹窗：查看、分配、移除角色成员
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, h, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NDataTable,
  NInput,
  NModal,
  NPopconfirm,
  NSpace,
  NSpin,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { getRoleUsers, type RoleUser, type RoleUserQuery, type SysRole } from '@/api/system/role'
import { assignUserRoles, getUser, pageUsers, type SysUser, type UserQuery } from '@/api/system/user'

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const syncing = ref(false)
const selectorVisible = ref(false)
const selectorLoading = ref(false)
const selectorSubmitting = ref(false)

const currentRole: Ref<SysRole | null> = ref(null)
const tableData: Ref<RoleUser[]> = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const checkedUserIds = ref<number[]>([])

const candidateQuery = reactive<UserQuery>({
  page: 1,
  size: 10,
  username: '',
  nickname: '',
  status: '1',
})
const candidateData: Ref<SysUser[]> = ref([])
const candidateTotal = ref(0)
const checkedCandidateIds = ref<number[]>([])

const filteredTableData = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) {
    return tableData.value
  }
  return tableData.value.filter((item) =>
    [item.username, item.nickname, item.deptName]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(search)),
  )
})

const userColumns = computed<DataTableColumns<RoleUser>>(() => [
  { type: 'selection' },
  { key: 'userId', title: '用户ID', width: 90 },
  { key: 'username', title: '用户名', width: 150 },
  { key: 'nickname', title: '昵称', width: 150 },
  { key: 'deptName', title: '部门', minWidth: 180 },
  {
    key: 'action',
    title: '操作',
    width: 100,
    render: (row: RoleUser) =>
      h(
        NPopconfirm,
        { onPositiveClick: () => void handleRemoveUsers([row.userId]) },
        {
          trigger: () => h(NButton, { text: true, type: 'error', size: 'small' }, { default: () => '移除' }),
          default: () => '确认移除该用户的当前角色？',
        },
      ),
  },
])

const candidateColumns = computed<DataTableColumns<SysUser>>(() => [
  { type: 'selection' },
  { key: 'id', title: '用户ID', width: 90 },
  { key: 'username', title: '用户名', width: 150 },
  { key: 'nickname', title: '昵称', width: 150 },
  { key: 'deptName', title: '部门', minWidth: 180 },
  {
    key: 'status',
    title: '状态',
    width: 90,
    render: (row: SysUser) =>
      h(NTag, { size: 'small', type: row.status === '1' ? 'success' : 'default' }, {
        default: () => (row.status === '1' ? '启用' : '禁用'),
      }),
  },
])

function getCurrentRoleId(): number | null {
  return currentRole.value?.id ?? null
}

/**
 * 打开模态框
 */
function open(role: SysRole): void {
  visible.value = true
  currentRole.value = role
  page.value = 1
  keyword.value = ''
  checkedUserIds.value = []
  void loadData()
}

/** 加载当前角色用户 */
async function loadData(): Promise<void> {
  const roleId = getCurrentRoleId()
  if (!roleId) {
    return
  }
  loading.value = true
  try {
    const query: RoleUserQuery = {
      roleId,
      page: page.value,
      size: pageSize.value,
    }
    const res = await getRoleUsers(query)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '加载数据失败'
    message.error(messageText)
  } finally {
    loading.value = false
  }
}

/** 打开分配用户弹窗 */
function openSelector(): void {
  selectorVisible.value = true
  checkedCandidateIds.value = []
  candidateQuery.page = 1
  candidateQuery.size = 10
  candidateQuery.username = ''
  candidateQuery.nickname = ''
  candidateQuery.status = '1'
  void loadCandidates()
}

/** 拉取候选用户 */
async function loadCandidates(): Promise<void> {
  selectorLoading.value = true
  try {
    const res = await pageUsers(candidateQuery)
    candidateData.value = res.records
    candidateTotal.value = res.total
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '加载用户失败'
    message.error(messageText)
  } finally {
    selectorLoading.value = false
  }
}

async function syncUserRoles(userIds: number[], mode: 'append' | 'remove'): Promise<void> {
  const roleId = getCurrentRoleId()
  if (!roleId || userIds.length === 0) {
    return
  }
  const tasks = userIds.map(async (userId) => {
    const detail = await getUser(userId)
    const currentRoleIds = detail.roleIds ?? []
    const nextRoleIds = mode === 'append'
      ? Array.from(new Set([...currentRoleIds, roleId]))
      : currentRoleIds.filter((item) => item !== roleId)
    await assignUserRoles(userId, nextRoleIds)
  })
  await Promise.all(tasks)
}

/** 批量移除用户 */
async function handleRemoveUsers(userIds: number[]): Promise<void> {
  if (!userIds.length) {
    return
  }
  syncing.value = true
  try {
    await syncUserRoles(userIds, 'remove')
    message.success(`已移除 ${userIds.length} 个用户`)
    checkedUserIds.value = []
    await loadData()
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '移除失败'
    message.error(messageText)
  } finally {
    syncing.value = false
  }
}

/** 保存新增成员 */
async function handleAssignUsers(): Promise<void> {
  if (!checkedCandidateIds.value.length) {
    message.warning('请选择要分配的用户')
    return
  }
  selectorSubmitting.value = true
  try {
    await syncUserRoles(checkedCandidateIds.value, 'append')
    message.success(`已分配 ${checkedCandidateIds.value.length} 个用户`)
    selectorVisible.value = false
    checkedCandidateIds.value = []
    await loadData()
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '分配失败'
    message.error(messageText)
  } finally {
    selectorSubmitting.value = false
  }
}

function handlePageChange(newPage: number): void {
  page.value = newPage
  void loadData()
}

function handlePageSizeChange(newSize: number): void {
  pageSize.value = newSize
  page.value = 1
  void loadData()
}

function handleCandidatePageChange(newPage: number): void {
  candidateQuery.page = newPage
  void loadCandidates()
}

function handleCandidatePageSizeChange(newSize: number): void {
  candidateQuery.size = newSize
  candidateQuery.page = 1
  void loadCandidates()
}

function handleCheckedUserIds(keys: Array<string | number>): void {
  checkedUserIds.value = keys.map((item) => Number(item))
}

function handleCheckedCandidateIds(keys: Array<string | number>): void {
  checkedCandidateIds.value = keys.map((item) => Number(item))
}

defineExpose({ open })
</script>

<template>
  <NModal
    v-model:show="visible"
    preset="card"
    :title="`角色成员 - ${currentRole?.roleName ?? ''}`"
    :style="{ width: '960px' }"
    :bordered="false"
    :segmented="{ content: true }"
  >
    <NSpace vertical :size="16">
      <NSpace justify="space-between" align="center">
        <NInput
          v-model:value="keyword"
          placeholder="筛选当前页成员"
          clearable
          style="width: 260px"
        />
        <NSpace>
          <NButton type="primary" @click="openSelector">分配用户</NButton>
          <NPopconfirm
            v-if="checkedUserIds.length > 0"
            @positive-click="() => void handleRemoveUsers(checkedUserIds)"
          >
            <template #trigger>
              <NButton type="error" ghost :loading="syncing">批量移除</NButton>
            </template>
            确认移除所选用户的当前角色？
          </NPopconfirm>
          <NButton @click="() => void loadData()">刷新</NButton>
        </NSpace>
      </NSpace>

      <NSpin :show="loading || syncing">
        <NDataTable
          :columns="userColumns"
          :data="filteredTableData"
          :row-key="(row: RoleUser) => row.userId"
          :checked-row-keys="checkedUserIds"
          :pagination="{
            page,
            pageSize,
            itemCount: total,
            showSizePicker: true,
            pageSizes: [10, 20, 30],
            onUpdatePage: handlePageChange,
            onUpdatePageSize: handlePageSizeChange,
          }"
          size="small"
          @update:checked-row-keys="handleCheckedUserIds"
        />
      </NSpin>
    </NSpace>

    <NModal
      v-model:show="selectorVisible"
      preset="card"
      title="分配角色用户"
      :style="{ width: '900px' }"
      :bordered="false"
      :segmented="{ content: true }"
    >
      <NSpace vertical :size="16">
        <NSpace>
          <NInput
            v-model:value="candidateQuery.username"
            placeholder="用户名"
            clearable
            style="width: 180px"
          />
          <NInput
            v-model:value="candidateQuery.nickname"
            placeholder="昵称"
            clearable
            style="width: 180px"
          />
          <NButton type="primary" @click="() => { candidateQuery.page = 1; void loadCandidates() }">
            查询
          </NButton>
          <NButton
            @click="() => { candidateQuery.username = ''; candidateQuery.nickname = ''; candidateQuery.page = 1; void loadCandidates() }"
          >
            重置
          </NButton>
        </NSpace>

        <NSpin :show="selectorLoading || selectorSubmitting">
          <NDataTable
            :columns="candidateColumns"
            :data="candidateData"
            :row-key="(row: SysUser) => row.id ?? 0"
            :checked-row-keys="checkedCandidateIds"
            :pagination="{
              page: candidateQuery.page,
              pageSize: candidateQuery.size,
              itemCount: candidateTotal,
              showSizePicker: true,
              pageSizes: [10, 20, 30],
              onUpdatePage: handleCandidatePageChange,
              onUpdatePageSize: handleCandidatePageSizeChange,
            }"
            size="small"
            @update:checked-row-keys="handleCheckedCandidateIds"
          />
        </NSpin>
      </NSpace>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="selectorVisible = false">取消</NButton>
          <NButton type="primary" :loading="selectorSubmitting" @click="handleAssignUsers">
            确定分配
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </NModal>
</template>
