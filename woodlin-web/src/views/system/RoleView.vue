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
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  NTree,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type SelectOption,
  type TreeOption
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  RefreshOutline,
  SearchOutline,
  ShieldCheckmarkOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  addRole,
  assignRoleMenus,
  deleteRole,
  getRoleById,
  getRoleList,
  getRoleMenus,
  getRoleTree,
  updateRole,
  type RoleTreeNode,
  type SysRole
} from '@/api/role'
import { getMenuTree, type SysMenu } from '@/api/menu'
import { logger } from '@/utils/logger'
import { useUserStore } from '@/stores'
import {PERMISSIONS} from '@/constants/permission-keys'

interface RoleSearchForm {
  roleName: string
  roleCode: string
}

interface RoleFormData {
  roleId?: number
  roleName: string
  roleCode: string
  parentRoleId: number | null
  sortOrder: number
  status: string
  remark: string
}

const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)

const searchForm = ref<RoleSearchForm>({
  roleName: '',
  roleCode: ''
})

const roleList = ref<SysRole[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const roleTree = ref<RoleTreeNode[]>([])
const parentOptions = computed<SelectOption[]>(() => {
  const options: SelectOption[] = []
  flattenRoleTree(roleTree.value, options)
  return options
})

const roleMap = computed(() => {
  const map = new Map<number, string>()
  const walk = (nodes: RoleTreeNode[]) => {
    for (const node of nodes) {
      map.set(node.roleId, node.roleName)
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(roleTree.value)
  return map
})

const roleModalVisible = ref(false)
const roleFormRef = ref<FormInst | null>(null)
const roleForm = ref<RoleFormData>({
  roleName: '',
  roleCode: '',
  parentRoleId: null,
  sortOrder: 0,
  status: '1',
  remark: ''
})

const roleRules = {
  roleName: { required: true, message: '请输入角色名称', trigger: 'blur' },
  roleCode: { required: true, message: '请输入角色编码', trigger: 'blur' }
}

const permissionModalVisible = ref(false)
const currentPermissionRole = ref<SysRole | null>(null)
const permissionTreeData = ref<TreeOption[]>([])
const checkedPermissionKeys = ref<Array<string | number>>([])
const expandedPermissionKeys = ref<Array<string | number>>([])

const totalRoles = computed(() => total.value)
const SUPER_ADMIN_ROLE_ID = 1
const canViewRoles = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_ROLE_LIST))
const canCreateRole = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_ROLE_ADD))
const canUpdateRole = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT))
const canDeleteRole = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_ROLE_REMOVE))
const canAssignRolePermissions = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT))

const isSuperAdminRole = (role: SysRole): boolean => {
  return Number(role.roleId) === SUPER_ADMIN_ROLE_ID
}

const canEditPermission = computed(() => {
  if (!currentPermissionRole.value) {
    return false
  }
  return !isSuperAdminRole(currentPermissionRole.value) && canAssignRolePermissions.value
})

// eslint-disable-next-line max-lines-per-function
const renderRoleActionButtons = (row: SysRole) => {
  const superAdmin = isSuperAdminRole(row)
  const actionButtons: ReturnType<typeof h>[] = []

  if (canAssignRolePermissions.value) {
    actionButtons.push(
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => openPermission(row)
        },
        {
          default: () => '权限配置',
          icon: () => h(NIcon, null, { default: () => h(ShieldCheckmarkOutline) })
        }
      )
    )
  }

  if (canUpdateRole.value) {
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

  const renderDeleteTrigger = () =>
    h(
      NButton,
      {
        text: true,
        type: 'error',
        size: 'small',
        disabled: superAdmin
      },
      {
        default: () => '删除',
        icon: () => h(NIcon, null, { default: () => h(TrashOutline) })
      }
    )

  if (canDeleteRole.value) {
    actionButtons.push(
      h(
        NPopconfirm,
        {
          onPositiveClick: () => handleDelete(row),
          disabled: superAdmin
        },
        {
          default: () => '确定删除该角色吗？',
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

const pagination = computed(() => ({
  page: pageNum.value,
  pageSize: pageSize.value,
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pageNum.value = page
    loadRoleList()
  },
  onUpdatePageSize: (size: number) => {
    pageSize.value = size
    pageNum.value = 1
    loadRoleList()
  }
}))

const columns: DataTableColumns<SysRole> = [
  { title: '角色名称', key: 'roleName', width: 160 },
  { title: '角色编码', key: 'roleCode', width: 150 },
  {
    title: '父角色',
    key: 'parentRoleId',
    width: 140,
    render: row => {
      const parentId = row.parentRoleId
      if (!parentId) {
        return '—'
      }
      return roleMap.value.get(parentId) || `#${parentId}`
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
  { title: '排序', key: 'sortOrder', width: 90, align: 'center' },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    align: 'center',
    render: renderRoleActionButtons
  }
]

const loadRoleList = async () => {
  if (!canViewRoles.value) {
    roleList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const result = await getRoleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      roleName: searchForm.value.roleName.trim() || undefined,
      roleCode: searchForm.value.roleCode.trim() || undefined
    })
    roleList.value = result.data || []
    total.value = result.total || 0
  } catch (error) {
    logger.error('加载角色列表失败', error)
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoleTree = async () => {
  if (!canViewRoles.value) {
    roleTree.value = []
    return
  }
  try {
    roleTree.value = await getRoleTree()
  } catch (error) {
    logger.error('加载角色树失败', error)
    message.error('加载角色树失败')
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadRoleList()
}

const handleReset = () => {
  searchForm.value = {
    roleName: '',
    roleCode: ''
  }
  pageNum.value = 1
  loadRoleList()
}

const openAdd = () => {
  if (!canCreateRole.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_ADD} 权限`)
    return
  }
  roleForm.value = {
    roleName: '',
    roleCode: '',
    parentRoleId: null,
    sortOrder: 0,
    status: '1',
    remark: ''
  }
  roleModalVisible.value = true
}

const openEdit = async (role: SysRole) => {
  if (!canUpdateRole.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT} 权限`)
    return
  }
  if (!role.roleId) {
    return
  }
  loading.value = true
  try {
    const detail = await getRoleById(role.roleId)
    roleForm.value = {
      roleId: detail.roleId,
      roleName: detail.roleName,
      roleCode: detail.roleCode,
      parentRoleId: detail.parentRoleId ?? null,
      sortOrder: detail.sortOrder ?? detail.roleSort ?? 0,
      status: String(detail.status || '1'),
      remark: detail.remark || ''
    }
    roleModalVisible.value = true
  } catch (error) {
    logger.error('加载角色详情失败', error)
    message.error('加载角色详情失败')
  } finally {
    loading.value = false
  }
}

const submitRole = async () => {
  await roleFormRef.value?.validate()

  const payload: SysRole = {
    roleId: roleForm.value.roleId,
    roleName: roleForm.value.roleName.trim(),
    roleCode: roleForm.value.roleCode.trim(),
    parentRoleId: roleForm.value.parentRoleId,
    sortOrder: roleForm.value.sortOrder,
    status: roleForm.value.status,
    remark: roleForm.value.remark.trim()
  }

  loading.value = true
  try {
    if (payload.roleId) {
      if (!canUpdateRole.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT} 权限`)
        return
      }
      await updateRole(payload)
      message.success('修改成功')
    } else {
      if (!canCreateRole.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_ADD} 权限`)
        return
      }
      await addRole(payload)
      message.success('新增成功')
    }
    roleModalVisible.value = false
    await Promise.all([loadRoleList(), loadRoleTree()])
  } catch (error) {
    logger.error('保存角色失败', error)
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (role: SysRole) => {
  if (!canDeleteRole.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_REMOVE} 权限`)
    return
  }
  if (!role.roleId) {
    return
  }
  if (isSuperAdminRole(role)) {
    message.warning('超级管理员角色不可删除')
    return
  }

  loading.value = true
  try {
    await deleteRole(String(role.roleId))
    message.success('删除成功')
    await Promise.all([loadRoleList(), loadRoleTree()])
  } catch (error) {
    logger.error('删除角色失败', error)
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

const openPermission = async (role: SysRole) => {
  if (!canAssignRolePermissions.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT} 权限`)
    return
  }
  if (!role.roleId) {
    return
  }

  loading.value = true
  try {
    const [menus, menuIds] = await Promise.all([getMenuTree(), getRoleMenus(role.roleId)])
    permissionTreeData.value = buildPermissionTree(menus)
    checkedPermissionKeys.value = menuIds
    expandedPermissionKeys.value = collectPermissionKeys(permissionTreeData.value)
    currentPermissionRole.value = role
    permissionModalVisible.value = true
  } catch (error) {
    logger.error('加载角色权限数据失败', error)
    message.error('加载权限数据失败')
  } finally {
    loading.value = false
  }
}

const submitPermissions = async () => {
  if (!canAssignRolePermissions.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_EDIT} 权限`)
    return
  }
  if (!currentPermissionRole.value?.roleId) {
    return
  }
  if (!canEditPermission.value) {
    message.warning('超级管理员角色默认拥有全部权限，且不可删减')
    return
  }

  const menuIds = checkedPermissionKeys.value.map(key => Number(key))

  loading.value = true
  try {
    await assignRoleMenus(currentPermissionRole.value.roleId, menuIds)
    message.success('权限分配成功')
    permissionModalVisible.value = false
  } catch (error) {
    logger.error('分配角色权限失败', error)
    message.error('权限分配失败')
  } finally {
    loading.value = false
  }
}

const buildPermissionTree = (menus: SysMenu[]): TreeOption[] =>
  menus.map(menu => ({
    key: menu.menuId || 0,
    label: `${menu.menuName} (${menu.permissionCode || '-'})`,
    children: menu.children ? buildPermissionTree(menu.children) : undefined
  }))

const collectPermissionKeys = (nodes: TreeOption[], keys: Array<string | number> = []): Array<string | number> => {
  for (const node of nodes) {
    if (node.key !== undefined) {
      keys.push(node.key)
    }
    const children = node.children as TreeOption[] | undefined
    if (children && children.length > 0) {
      collectPermissionKeys(children, keys)
    }
  }
  return keys
}

const flattenRoleTree = (nodes: RoleTreeNode[], options: SelectOption[], depth = 0): void => {
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
}

onMounted(() => {
  if (!canViewRoles.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_ROLE_LIST} 权限`)
    return
  }
  Promise.all([loadRoleList(), loadRoleTree()])
})
</script>

<template>
  <div class="role-page">
    <NCard :bordered="false" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>角色与权限矩阵</h2>
          <p>角色负责聚合菜单、按钮与接口权限，用户通过角色继承访问能力。</p>
        </div>
        <NTag type="info" size="small">共 {{ totalRoles }} 个角色</NTag>
      </div>
    </NCard>

    <NCard :bordered="false" class="toolbar-card">
      <NForm inline :model="searchForm" label-placement="left">
        <NFormItem label="角色名称">
          <NInput v-model:value="searchForm.roleName" clearable placeholder="请输入角色名称" style="width: 180px" />
        </NFormItem>
        <NFormItem label="角色编码">
          <NInput v-model:value="searchForm.roleCode" clearable placeholder="例如 admin" style="width: 200px" />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" :loading="loading" :disabled="!canViewRoles" @click="handleSearch">
              <template #icon>
                <NIcon><SearchOutline /></NIcon>
              </template>
              搜索
            </NButton>
            <NButton :disabled="!canViewRoles" @click="handleReset">
              <template #icon>
                <NIcon><RefreshOutline /></NIcon>
              </template>
              重置
            </NButton>
            <NButton v-if="canCreateRole" type="primary" secondary @click="openAdd">
              <template #icon>
                <NIcon><AddOutline /></NIcon>
              </template>
              新增角色
            </NButton>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" title="角色列表" class="table-card">
      <NDataTable
        :columns="columns"
        :data="roleList"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        striped
      />
    </NCard>

    <NModal
      v-model:show="roleModalVisible"
      preset="card"
      :title="roleForm.roleId ? '编辑角色' : '新增角色'"
      style="width: 560px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="roleFormRef" :model="roleForm" :rules="roleRules" label-placement="left" label-width="100">
        <NFormItem label="角色名称" path="roleName">
          <NInput v-model:value="roleForm.roleName" placeholder="请输入角色名称" />
        </NFormItem>
        <NFormItem label="角色编码" path="roleCode">
          <NInput v-model:value="roleForm.roleCode" placeholder="唯一编码，如 data_operator" />
        </NFormItem>
        <NFormItem label="父角色">
          <NSelect v-model:value="roleForm.parentRoleId" :options="parentOptions" clearable placeholder="根角色可留空" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="roleForm.sortOrder" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect
            v-model:value="roleForm.status"
            :options="[
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
            ]"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="roleForm.remark" type="textarea" placeholder="可选" />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="roleModalVisible = false">取消</NButton>
          <NButton
            type="primary"
            :loading="loading"
            :disabled="(roleForm.roleId && !canUpdateRole) || (!roleForm.roleId && !canCreateRole)"
            @click="submitRole"
          >
            保存
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal
      v-model:show="permissionModalVisible"
      preset="card"
      :title="currentPermissionRole ? `权限配置 - ${currentPermissionRole.roleName}` : '权限配置'"
      style="width: 760px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NTag v-if="!canEditPermission" type="warning" style="margin-bottom: 10px">
        超级管理员默认拥有所有权限，且不可删减
      </NTag>
      <NTree
        checkable
        selectable
        block-line
        cascade
        :data="permissionTreeData"
        :expanded-keys="expandedPermissionKeys"
        :checked-keys="checkedPermissionKeys"
        @update:checked-keys="(keys) => checkedPermissionKeys = keys"
      />

      <template #footer>
        <NSpace justify="end">
          <NButton @click="permissionModalVisible = false">取消</NButton>
          <NButton type="primary" :disabled="!canEditPermission" :loading="loading" @click="submitPermissions">
            保存权限
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #0f766e 0%, #0ea5e9 52%, #22d3ee 100%);
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
  background: radial-gradient(circle at top right, rgba(34, 211, 238, 0.14), transparent 44%), var(--bg-color);
}
</style>
