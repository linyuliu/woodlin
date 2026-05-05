<!--
  @file views/system/role/index.vue
  @module 角色管理
  @description 生产级角色管理：列表+搜索+新增编辑+菜单权限+数据权限+用户列表+RBAC1层级
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, computed, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NPopconfirm,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import dayjs from 'dayjs'
import {
  pageRoles,
  deleteRole,
  type SysRole,
  type RoleQuery,
} from '@/api/system/role'
import RoleFormDrawer from './components/RoleFormDrawer.vue'
import MenuPermissionDrawer from './components/MenuPermissionDrawer.vue'
import DataScopeDrawer from './components/DataScopeDrawer.vue'
import RoleUsersModal from './components/RoleUsersModal.vue'
import WIcon from '@/components/WIcon/index.vue'

const message = useMessage()

const tableData: Ref<SysRole[]> = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const query = reactive<RoleQuery>({
  page: 1,
  size: 10,
  roleName: '',
  roleCode: '',
  status: undefined,
})

const roleFormDrawerRef = ref<InstanceType<typeof RoleFormDrawer> | null>(null)
const menuPermissionDrawerRef = ref<InstanceType<typeof MenuPermissionDrawer> | null>(null)
const dataScopeDrawerRef = ref<InstanceType<typeof DataScopeDrawer> | null>(null)
const roleUsersModalRef = ref<InstanceType<typeof RoleUsersModal> | null>(null)

const statusOptions: SelectOption[] = [
  { label: '全部', value: '' },
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const dataScopeMap: Record<string, string> = {
  '1': '全部',
  '2': '本部门',
  '3': '本部门及以下',
  '4': '仅本人',
  '5': '自定义',
}

const columns = computed<DataTableColumns<SysRole>>(() => [
  { key: 'id', title: '角色ID', width: 80 },
  { key: 'roleName', title: '角色名称', width: 150 },
  { key: 'roleCode', title: '角色编码', width: 150 },
  {
    key: 'sort',
    title: '排序',
    width: 80,
  },
  {
    key: 'dataScope',
    title: '数据权限',
    width: 120,
    render: (row: SysRole) =>
      row.dataScope
        ? h(NTag, { size: 'small', type: 'info' }, { default: () => dataScopeMap[row.dataScope!] || row.dataScope })
        : '-',
  },
  {
    key: 'status',
    title: '状态',
    width: 100,
    render: (row: SysRole) => h(NTag, { size: 'small', type: row.status === '1' ? 'success' : 'default' }, { default: () => (row.status === '1' ? '启用' : '禁用') }),
  },
  {
    key: 'createTime',
    title: '创建时间',
    width: 160,
    render: (row: SysRole) => (row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm') : '-'),
  },
  {
    key: 'action',
    title: '操作',
    width: 320,
    fixed: 'right',
    render: (row: SysRole) =>
      h(
        NSpace,
        { size: 4 },
        {
          default: () => [
            h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
            h(NButton, { text: true, type: 'info', size: 'small', onClick: () => handleMenuPermission(row) }, { default: () => '菜单权限' }),
            h(NButton, { text: true, type: 'warning', size: 'small', onClick: () => handleDataScope(row) }, { default: () => '数据权限' }),
            h(NButton, { text: true, size: 'small', onClick: () => handleViewUsers(row) }, { default: () => '用户列表' }),
            h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row.id!) },
              {
                trigger: () => h(NButton, { text: true, type: 'error', size: 'small' }, { default: () => '删除' }),
                default: () => '确认删除该角色？',
              }
            ),
          ],
        }
      ),
  },
])

/** 搜索 */
function handleSearch(): void {
  page.value = 1
  query.page = 1
  loadData()
}

/** 重置搜索 */
function handleReset(): void {
  query.roleName = ''
  query.roleCode = ''
  query.status = undefined
  handleSearch()
}

/** 加载数据 */
async function loadData(): Promise<void> {
  loading.value = true
  query.page = page.value
  query.size = pageSize.value
  try {
    const res = await pageRoles(query)
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

/** 新增 */
function handleAdd(): void {
  roleFormDrawerRef.value?.open()
}

/** 编辑 */
function handleEdit(row: SysRole): void {
  roleFormDrawerRef.value?.open(row)
}

/** 菜单权限 */
function handleMenuPermission(row: SysRole): void {
  menuPermissionDrawerRef.value?.open(row)
}

/** 数据权限 */
function handleDataScope(row: SysRole): void {
  dataScopeDrawerRef.value?.open(row)
}

/** 查看用户列表 */
function handleViewUsers(row: SysRole): void {
  roleUsersModalRef.value?.open(row)
}

/** 删除 */
async function handleDelete(id: number): Promise<void> {
  try {
    await deleteRole(id)
    message.success('删除成功')
    loadData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

/** 表单成功回调 */
function handleFormSuccess(): void {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <NCard>
    <NSpace vertical :size="16">
      <!-- 搜索栏 -->
      <NSpace :size="12" :wrap="false">
        <NInput
          v-model:value="query.roleName"
          placeholder="角色名称"
          clearable
          style="width: 160px"
        />
        <NInput
          v-model:value="query.roleCode"
          placeholder="角色编码"
          clearable
          style="width: 160px"
        />
        <NSelect
          v-model:value="query.status"
          :options="statusOptions"
          placeholder="状态"
          clearable
          style="width: 120px"
        />
        <NButton type="primary" @click="handleSearch">查询</NButton>
        <NButton @click="handleReset">重置</NButton>

        <div style="margin-left: auto; display: flex; gap: 8px">
          <NButton type="primary" @click="handleAdd">
            <template #icon>
              <WIcon icon="vicons:antd:PlusOutlined" />
            </template>
            新增
          </NButton>
          <NButton @click="loadData">
            <template #icon>
              <WIcon icon="vicons:antd:ReloadOutlined" />
            </template>
          </NButton>
        </div>
      </NSpace>

      <!-- 表格 -->
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysRole) => row.id!"
        :pagination="{
          page: page,
          pageSize: pageSize,
          itemCount: total,
          showSizePicker: true,
          pageSizes: [10, 20, 30, 50],
          onUpdatePage: handlePageChange,
          onUpdatePageSize: handlePageSizeChange,
        }"
        :scroll-x="1300"
        size="small"
      />
    </NSpace>
  </NCard>

  <RoleFormDrawer ref="roleFormDrawerRef" @success="handleFormSuccess" />
  <MenuPermissionDrawer ref="menuPermissionDrawerRef" />
  <DataScopeDrawer ref="dataScopeDrawerRef" />
  <RoleUsersModal ref="roleUsersModalRef" />
</template>
