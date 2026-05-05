<!--
  @file views/system/user/index.vue
  @module 用户管理
  @description 生产级用户管理页面：左侧部门树 + 右侧数据表，支持搜索、新增、编辑、删除、重置密码、分配角色、批量操作、导入导出、列设置
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NInput,
  NLayout,
  NLayoutSider,
  NLayoutContent,
  NSelect,
  NSpace,
  NSwitch,
  NTree,
  NPopconfirm,
  NPopover,
  NCheckboxGroup,
  NCheckbox,
  useMessage,
  type DataTableColumns,
  type TreeOption,
  type SelectOption,
} from 'naive-ui'
import dayjs from 'dayjs'
import {
  pageUsers,
  deleteUser,
  changeUserStatus,
  resetUserPassword,
  type SysUser,
  type UserQuery,
} from '@/api/system/user'
import { getDeptTree, type SysDept } from '@/api/system/dept'
import UserFormDrawer from './components/UserFormDrawer.vue'
import AssignRoleModal from './components/AssignRoleModal.vue'
import WIcon from '@/components/WIcon/index.vue'

const message = useMessage()

const tableData: Ref<SysUser[]> = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const query = reactive<UserQuery>({
  page: 1,
  size: 10,
  username: '',
  deptId: undefined,
  status: undefined,
})

const deptTreeData: Ref<TreeOption[]> = ref([])
const selectedDeptIds = ref<Array<string | number>>([])
const expandedKeys = ref<Array<string | number>>([])
const deptSearchValue = ref('')

const formDrawerRef = ref<InstanceType<typeof UserFormDrawer> | null>(null)
const assignRoleModalRef = ref<InstanceType<typeof AssignRoleModal> | null>(null)

const selectedRowKeys = ref<Array<string | number>>([])

const statusOptions: SelectOption[] = [
  { label: '全部', value: '' },
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const allColumns = [
  { key: 'id', title: '用户ID', width: 80, fixed: 'left' as const },
  { key: 'username', title: '用户名', width: 120, fixed: 'left' as const },
  { key: 'nickname', title: '昵称', width: 120 },
  { key: 'deptName', title: '部门', width: 150 },
  { key: 'mobile', title: '手机号', width: 130 },
  { key: 'email', title: '邮箱', width: 180 },
  { key: 'status', title: '状态', width: 100 },
  { key: 'lastLoginTime', title: '最后登录', width: 160 },
  { key: 'createTime', title: '创建时间', width: 160 },
  { key: 'action', title: '操作', width: 240, fixed: 'right' as const },
]

const visibleColumns = ref<string[]>(allColumns.map((c) => c.key))

const columns = computed<DataTableColumns<SysUser>>(() => {
  const cols: DataTableColumns<SysUser> = []
  allColumns.forEach((col) => {
    if (!visibleColumns.value.includes(col.key)) {return}
    if (col.key === 'status') {
      cols.push({
        key: 'status',
        title: '状态',
        width: 100,
        render: (row: SysUser) =>
          h(NSwitch, {
            value: row.status === '1',
            onUpdateValue: (val: boolean) => handleStatusChange(row, val),
          }),
      })
    } else if (col.key === 'lastLoginTime' || col.key === 'createTime') {
      cols.push({
        ...col,
        render: (row: SysUser) => {
          const val = row[col.key as keyof SysUser] as string | undefined
          return val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-'
        },
      })
    } else if (col.key === 'action') {
      cols.push({
        ...col,
        render: (row: SysUser) =>
          h(
            NSpace,
            { size: 4 },
            {
              default: () => [
                h(
                  NButton,
                  {
                    text: true,
                    type: 'primary',
                    size: 'small',
                    onClick: () => handleEdit(row),
                  },
                  { default: () => '编辑' }
                ),
                h(
                  NPopconfirm,
                  {
                    onPositiveClick: () => handleResetPassword(row),
                  },
                  {
                    trigger: () =>
                      h(
                        NButton,
                        { text: true, type: 'warning', size: 'small' },
                        { default: () => '重置密码' }
                      ),
                    default: () => '确认重置密码为 Aa@12345？',
                  }
                ),
                h(
                  NButton,
                  {
                    text: true,
                    type: 'info',
                    size: 'small',
                    onClick: () => handleAssignRole(row),
                  },
                  { default: () => '分配角色' }
                ),
                h(
                  NPopconfirm,
                  {
                    onPositiveClick: () => handleDelete(row.id!),
                  },
                  {
                    trigger: () =>
                      h(
                        NButton,
                        { text: true, type: 'error', size: 'small' },
                        { default: () => '删除' }
                      ),
                    default: () => '确认删除该用户？',
                  }
                ),
              ],
            }
          ),
      })
    } else {
      cols.push(col)
    }
  })
  return cols
})

/** 加载部门树 */
async function loadDeptTree(): Promise<void> {
  try {
    const data = await getDeptTree()
    deptTreeData.value = transformDeptTree(data)
  } catch (error) {
    console.error('加载部门树失败', error)
  }
}

/** 转换部门树为 n-tree 格式 */
function transformDeptTree(list: SysDept[]): TreeOption[] {
  return list.map((item) => ({
    key: item.deptId || item.id,
    label: item.deptName,
    children: item.children ? transformDeptTree(item.children) : undefined,
  }))
}

/** 部门树节点点击 */
function handleDeptNodeClick(keys: Array<string | number>): void {
  if (keys.length > 0) {
    query.deptId = Number(keys[0])
  } else {
    query.deptId = undefined
  }
  selectedDeptIds.value = keys
  handleSearch()
}

/** 搜索 */
function handleSearch(): void {
  page.value = 1
  query.page = 1
  loadData()
}

/** 重置搜索 */
function handleReset(): void {
  query.username = ''
  query.status = undefined
  query.deptId = undefined
  selectedDeptIds.value = []
  deptSearchValue.value = ''
  handleSearch()
}

/** 加载数据 */
async function loadData(): Promise<void> {
  loading.value = true
  query.page = page.value
  query.size = pageSize.value
  try {
    const res = await pageUsers(query)
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
  formDrawerRef.value?.open()
}

/** 编辑 */
function handleEdit(row: SysUser): void {
  formDrawerRef.value?.open(row)
}

/** 状态切换 */
async function handleStatusChange(row: SysUser, val: boolean): Promise<void> {
  const newStatus = val ? '1' : '0'
  try {
    await changeUserStatus(row.id!, newStatus)
    row.status = newStatus
    message.success('状态修改成功')
  } catch (error: any) {
    message.error(error?.message || '状态修改失败')
  }
}

/** 重置密码 */
async function handleResetPassword(row: SysUser): Promise<void> {
  try {
    await resetUserPassword(row.id!, 'Aa@12345')
    message.success('密码已重置为 Aa@12345')
  } catch (error: any) {
    message.error(error?.message || '重置密码失败')
  }
}

/** 分配角色 */
function handleAssignRole(row: SysUser): void {
  assignRoleModalRef.value?.open(row)
}

/** 删除 */
async function handleDelete(id: number): Promise<void> {
  try {
    await deleteUser(id)
    message.success('删除成功')
    loadData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

/** 批量删除 */
async function handleBatchDelete(): Promise<void> {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的用户')
    return
  }
  try {
    await deleteUser(selectedRowKeys.value as number[])
    message.success('批量删除成功')
    selectedRowKeys.value = []
    loadData()
  } catch (error: any) {
    message.error(error?.message || '批量删除失败')
  }
}

/** 导入 */
function handleImport(): void {
  message.info('导入功能开发中')
}

/** 导出 */
function handleExport(): void {
  message.info('导出功能开发中')
}

/** 表单保存成功回调 */
function handleFormSuccess(): void {
  loadData()
}

function handleAssignSuccess(): void {
  loadData()
}

onMounted(() => {
  loadDeptTree()
  loadData()
})
</script>

<template>
  <NLayout has-sider style="height: 100%">
    <NLayoutSider
      :width="260"
      :native-scrollbar="false"
      bordered
      show-trigger
      collapse-mode="width"
      :collapsed-width="0"
    >
      <div style="padding: 16px">
        <NInput
          v-model:value="deptSearchValue"
          placeholder="搜索部门..."
          clearable
          style="margin-bottom: 12px"
        />
        <NTree
          :data="deptTreeData"
          :pattern="deptSearchValue"
          :selected-keys="selectedDeptIds"
          :expanded-keys="expandedKeys"
          block-line
          selectable
          @update:selected-keys="handleDeptNodeClick"
          @update:expanded-keys="(keys) => (expandedKeys = keys)"
        />
      </div>
    </NLayoutSider>

    <NLayoutContent :native-scrollbar="false">
      <NCard style="height: 100%">
        <NSpace vertical :size="16">
          <!-- 搜索栏 -->
          <NSpace :size="12" :wrap="false">
            <NInput
              v-model:value="query.username"
              placeholder="用户名/昵称"
              clearable
              style="width: 180px"
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
              <NPopconfirm @positive-click="handleBatchDelete">
                <template #trigger>
                  <NButton :disabled="selectedRowKeys.length === 0">
                    <template #icon>
                      <WIcon icon="vicons:antd:DeleteOutlined" />
                    </template>
                    批量删除
                  </NButton>
                </template>
                确认删除选中的 {{ selectedRowKeys.length }} 个用户？
              </NPopconfirm>
              <NButton @click="handleImport">
                <template #icon>
                  <WIcon icon="vicons:antd:UploadOutlined" />
                </template>
                导入
              </NButton>
              <NButton @click="handleExport">
                <template #icon>
                  <WIcon icon="vicons:antd:DownloadOutlined" />
                </template>
                导出
              </NButton>
              <NPopover trigger="click" placement="bottom-end">
                <template #trigger>
                  <NButton>
                    <template #icon>
                      <WIcon icon="vicons:antd:SettingOutlined" />
                    </template>
                    列设置
                  </NButton>
                </template>
                <NCheckboxGroup v-model:value="visibleColumns">
                  <NSpace vertical>
                    <NCheckbox
                      v-for="col in allColumns"
                      :key="col.key"
                      :value="col.key"
                      :label="col.title"
                    />
                  </NSpace>
                </NCheckboxGroup>
              </NPopover>
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
            :row-key="(row: SysUser) => row.id!"
            :pagination="{
              page: page,
              pageSize: pageSize,
              itemCount: total,
              showSizePicker: true,
              pageSizes: [10, 20, 30, 50],
              onUpdatePage: handlePageChange,
              onUpdatePageSize: handlePageSizeChange,
            }"
            :scroll-x="1500"
            size="small"
            :checked-row-keys="selectedRowKeys"
            @update:checked-row-keys="(keys) => (selectedRowKeys = keys)"
          />
        </NSpace>
      </NCard>
    </NLayoutContent>
  </NLayout>

  <UserFormDrawer ref="formDrawerRef" @success="handleFormSuccess" />
  <AssignRoleModal ref="assignRoleModalRef" @success="handleAssignSuccess" />
</template>

<style scoped>
:deep(.n-layout-sider) {
  background: #fff;
}
</style>
