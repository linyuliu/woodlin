<script setup lang="ts">
/**
 * 角色管理视图（前端 mock，RBAC1）
 * 
 * @author mumu
 * @since 2025-01-01 (rev 2026-01-31)
 */
import { ref, h, computed } from 'vue'
import { 
  NCard, NButton, NDataTable, NSpace, NInput, NForm, NFormItem,
  NTag, NIcon, NPopconfirm, NModal, NSelect, NInputNumber, NTree,
  useMessage, type DataTableColumns, type FormInst
} from 'naive-ui'
import { 
  AddOutline, 
  SearchOutline, 
  RefreshOutline,
  CreateOutline,
  TrashOutline,
  ShieldCheckmarkOutline
} from '@vicons/ionicons5'
import {
  fetchRoleTree,
  createRole,
  updateRole as updateRoleMock,
  deleteRole as deleteRoleMock,
  type RoleNode
} from '@/api/mock/rbac'

type Role = RoleNode

/**
 * 搜索表单数据
 */
const searchForm = ref({
  name: '',
  code: ''
})

const loading = ref(false)
const message = useMessage()

const roleTree = ref<RoleNode[]>([])
const flatRoles = computed<Role[]>(() => {
  const list: Role[] = []
  const walk = (nodes: RoleNode[], parentName = '') => {
    nodes.forEach(n => {
      list.push({ ...n, parentName } as Role)
      if (n.children) {
        walk(n.children, n.roleName)
      }
    })
  }
  walk(roleTree.value)
  return list
})

const roleModalShow = ref(false)
const roleFormRef = ref<FormInst | null>(null)
const roleForm = ref<Partial<Role>>({
  roleId: undefined,
  parentRoleId: null,
  roleName: '',
  roleCode: '',
  sortOrder: 1,
  status: '1'
})

const roleRules = {
  roleName: { required: true, message: '请输入角色名称', trigger: 'blur' },
  roleCode: { required: true, message: '请输入角色编码', trigger: 'blur' }
}

/**
 * 渲染状态标签
 */
const renderStatus = (status: string) => {
  const statusMap: Record<string, { type: 'success' | 'warning'; text: string }> = {
    '1': { type: 'success', text: '启用' },
    '0': { type: 'warning', text: '禁用' }
  }
  const config = statusMap[status] || { type: 'warning', text: '未知' }
  return h(NTag, { type: config.type, size: 'small' }, { default: () => config.text })
}

/**
 * 表格列配置
 */
const columns: DataTableColumns<Role> = [
  { 
    title: '角色名称', 
    key: 'roleName',
    width: 160
  },
  { 
    title: '角色编码', 
    key: 'roleCode',
    width: 140
  },
  { 
    title: '父角色', 
    key: 'parentRoleId',
    width: 140,
    render: row => row.parentRoleId ? (row as any).parentName || `#${row.parentRoleId}` : '—'
  },
  { 
    title: '状态', 
    key: 'status', 
    width: 90,
    align: 'center',
    render: (row) => renderStatus(row.status)
  },
  { 
    title: '排序', 
    key: 'sortOrder', 
    width: 90,
    align: 'center'
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    align: 'center',
    render: (row) => {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(
            NButton,
            {
              text: true,
              type: 'primary',
              size: 'small',
              onClick: () => handlePermission(row)
            },
            {
              default: () => '权限配置',
              icon: () => h(NIcon, null, { default: () => h(ShieldCheckmarkOutline) })
            }
          ),
          h(
            NButton,
            {
              text: true,
              type: 'primary',
              size: 'small',
              onClick: () => handleEdit(row)
            },
            {
              default: () => '编辑',
              icon: () => h(NIcon, null, { default: () => h(CreateOutline) })
            }
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.roleId!)
            },
            {
              default: () => '确定要删除该角色吗？',
              trigger: () => h(
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
            }
          )
        ]
      })
    }
  }
]

/**
 * 搜索角色
 */
const handleSearch = () => {
  // 纯前端过滤，见 filteredRoles
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.value = {
    name: '',
    code: ''
  }
}

/**
 * 添加角色
 */
const handleAdd = () => {
  roleForm.value = {
    roleId: undefined,
    parentRoleId: null,
    roleName: '',
    roleCode: '',
    sortOrder: 1,
    status: '1'
  }
  roleModalShow.value = true
}

/**
 * 编辑角色
 */
const handleEdit = (row: Role) => {
  roleForm.value = { ...row }
  roleModalShow.value = true
}

/**
 * 权限配置
 */
const handlePermission = (row: Role) => {
  message.info(`后端接入后为 ${row.roleName} 分配菜单/数据权限`)
}

/**
 * 删除角色
 */
const handleDelete = (id: number) => {
  deleteRoleMock(id).then(() => {
    message.success('删除成功')
    loadRoles()
  })
}

const submitRole = async () => {
  await roleFormRef.value?.validate()
  const payload = { ...roleForm.value }
  loading.value = true
  try {
    if (payload.roleId) {
      await updateRoleMock(payload as RoleNode)
      message.success('更新成功')
    } else {
      await createRole(payload as RoleNode)
      message.success('新增成功')
    }
    roleModalShow.value = false
    await loadRoles()
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  loading.value = true
  try {
    roleTree.value = await fetchRoleTree()
  } finally {
    loading.value = false
  }
}

const filteredRoles = computed(() =>
  flatRoles.value.filter(r => {
    const byName = searchForm.value.name ? r.roleName.includes(searchForm.value.name) : true
    const byCode = searchForm.value.code ? r.roleCode.includes(searchForm.value.code) : true
    return byName && byCode
  })
)

loadRoles()
</script>

<template>
  <div class="role-management-container">
    <NSpace vertical :size="16">
      <!-- 搜索区域 -->
      <NCard :bordered="false" class="search-card">
        <NForm inline :model="searchForm" label-placement="left">
          <NFormItem label="角色名称">
            <NInput 
              v-model:value="searchForm.name" 
              placeholder="请输入角色名称"
              clearable
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="角色编码">
            <NInput 
              v-model:value="searchForm.code" 
              placeholder="请输入角色编码"
              clearable
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem>
            <NSpace>
              <NButton type="primary" @click="handleSearch" :loading="loading">
                <template #icon>
                  <NIcon>
                    <SearchOutline />
                  </NIcon>
                </template>
                搜索
              </NButton>
              <NButton @click="handleReset">
                <template #icon>
                  <NIcon>
                    <RefreshOutline />
                  </NIcon>
                </template>
                重置
              </NButton>
            </NSpace>
          </NFormItem>
        </NForm>
      </NCard>

      <!-- 角色列表 -->
      <NCard 
        title="角色列表" 
        :bordered="false" 
        :segmented="{ content: true }"
        class="table-card"
      >
        <template #header-extra>
          <NSpace>
            <NButton type="primary" @click="handleAdd">
              <template #icon>
                <NIcon>
                  <AddOutline />
                </NIcon>
              </template>
              添加角色
            </NButton>
            <NButton @click="loadRoles" :loading="loading">
              <template #icon>
                <NIcon>
                  <RefreshOutline />
                </NIcon>
              </template>
              刷新
            </NButton>
          </NSpace>
        </template>
        
        <NDataTable 
          :columns="columns" 
          :data="filteredRoles" 
          :loading="loading"
          :pagination="{ 
            pageSize: 10,
            showSizePicker: true,
            pageSizes: [10, 20, 50]
          }"
          :bordered="false"
          :single-line="false"
          striped
          class="role-table"
        />
      </NCard>

      <NCard title="角色树" :bordered="false">
        <NTree
          :data="roleTree"
          :key-field="'roleId'"
          :label-field="'roleName'"
          block-line
          expand-on-click
        />
      </NCard>
    </NSpace>

    <NModal
      v-model:show="roleModalShow"
      preset="card"
      :title="roleForm.roleId ? '编辑角色' : '新增角色'"
      style="width: 520px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="roleFormRef" :model="roleForm" :rules="roleRules" label-placement="left" label-width="90">
        <NFormItem label="角色名称" path="roleName">
          <NInput v-model:value="roleForm.roleName" placeholder="请输入角色名称" />
        </NFormItem>
        <NFormItem label="角色编码" path="roleCode">
          <NInput v-model:value="roleForm.roleCode" placeholder="唯一标识，如 system_admin" />
        </NFormItem>
        <NFormItem label="父角色">
          <NSelect
            v-model:value="roleForm.parentRoleId"
            :options="flatRoles.map(r => ({ label: r.roleName, value: r.roleId }))"
            clearable
            placeholder="根角色留空"
          />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="roleForm.sortOrder" :min="0" />
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
          <NInput v-model:value="roleForm.remark" type="textarea" placeholder="可填写职责说明" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="roleModalShow = false">取消</NButton>
          <NButton type="primary" :loading="loading" @click="submitRole">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.role-management-container {
  width: 100%;
  height: 100%;
}

.search-card {
  background: #fff;
}

.table-card {
  background: #fff;
}

.role-table {
  margin-top: 8px;
}
</style>
