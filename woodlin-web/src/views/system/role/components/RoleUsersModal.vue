<!--
  @file RoleUsersModal.vue
  @description 查看角色下的用户列表模态框
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { ref, computed, type Ref } from 'vue'
import {
  NModal,
  NDataTable,
  NSpin,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { getRoleUsers, type RoleUser, type RoleUserQuery, type SysRole } from '@/api/system/role'

const message = useMessage()
const visible = ref(false)
const loading = ref(false)

const currentRole: Ref<SysRole | null> = ref(null)
const tableData: Ref<RoleUser[]> = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const columns = computed<DataTableColumns<RoleUser>>(() => [
  { key: 'userId', title: '用户ID', width: 80 },
  { key: 'username', title: '用户名', width: 150 },
  { key: 'nickname', title: '昵称', width: 150 },
  { key: 'deptName', title: '部门', width: 200 },
])

/**
 * 打开模态框
 */
function open(role: SysRole): void {
  visible.value = true
  currentRole.value = role
  page.value = 1
  loadData()
}

/** 加载数据 */
async function loadData(): Promise<void> {
  if (!currentRole.value) {return}
  loading.value = true
  try {
    const query: RoleUserQuery = {
      roleId: currentRole.value.id!,
      page: page.value,
      size: pageSize.value,
    }
    const res = await getRoleUsers(query)
    tableData.value = res.records
    total.value = res.total
  } catch (error: any) {
    message.error(error?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

/** 分页变化 */
function handlePageChange(newPage: number): void {
  page.value = newPage
  loadData()
}

function handlePageSizeChange(newSize: number): void {
  pageSize.value = newSize
  page.value = 1
  loadData()
}

defineExpose({ open })
</script>

<template>
  <NModal
    v-model:show="visible"
    preset="card"
    :title="`角色用户列表 - ${currentRole?.roleName}`"
    :style="{ width: '720px' }"
    :bordered="false"
    :segmented="{ content: true }"
  >
    <NSpin :show="loading">
      <NDataTable
        :columns="columns"
        :data="tableData"
        :pagination="{
          page: page,
          pageSize: pageSize,
          itemCount: total,
          showSizePicker: true,
          pageSizes: [10, 20, 30],
          onUpdatePage: handlePageChange,
          onUpdatePageSize: handlePageSizeChange,
        }"
        size="small"
      />
    </NSpin>
  </NModal>
</template>
