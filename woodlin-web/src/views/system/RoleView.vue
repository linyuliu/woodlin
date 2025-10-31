<script setup lang="ts">
/**
 * 角色管理视图
 * 
 * @author mumu
 * @description 角色管理页面，包含角色列表、添加、编辑、权限分配等功能
 * @since 2025-01-01
 */
import { ref, h } from 'vue'
import { 
  NCard, NButton, NDataTable, NSpace, NInput, NForm, NFormItem,
  NTag, NIcon, NPopconfirm,
  type DataTableColumns
} from 'naive-ui'
import { 
  AddOutline, 
  SearchOutline, 
  RefreshOutline,
  CreateOutline,
  TrashOutline,
  ShieldCheckmarkOutline
} from '@vicons/ionicons5'

/**
 * 角色数据接口
 */
interface Role {
  id: number
  name: string
  code: string
  description: string
  status: string
  createTime: string
}

/**
 * 搜索表单数据
 */
const searchForm = ref({
  name: '',
  code: ''
})

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 角色列表数据
 */
const roles = ref<Role[]>([
  {
    id: 1,
    name: '超级管理员',
    code: 'SUPER_ADMIN',
    description: '拥有系统所有权限',
    status: 'active',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    name: '管理员',
    code: 'ADMIN',
    description: '拥有系统管理权限',
    status: 'active',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 3,
    name: '普通用户',
    code: 'USER',
    description: '基本使用权限',
    status: 'active',
    createTime: '2025-01-01 10:00:00'
  }
])

/**
 * 渲染状态标签
 */
const renderStatus = (status: string) => {
  const statusMap: Record<string, { type: 'success' | 'warning' | 'error', text: string }> = {
    active: { type: 'success', text: '启用' },
    inactive: { type: 'warning', text: '禁用' }
  }
  const config = statusMap[status] || { type: 'warning', text: '未知' }
  return h(NTag, { type: config.type, size: 'small' }, { default: () => config.text })
}

/**
 * 表格列配置
 */
const columns: DataTableColumns<Role> = [
  { 
    title: 'ID', 
    key: 'id', 
    width: 80,
    align: 'center'
  },
  { 
    title: '角色名称', 
    key: 'name',
    width: 150
  },
  { 
    title: '角色编码', 
    key: 'code',
    width: 150
  },
  { 
    title: '描述', 
    key: 'description',
    ellipsis: {
      tooltip: true
    }
  },
  { 
    title: '状态', 
    key: 'status', 
    width: 100,
    align: 'center',
    render: (row) => renderStatus(row.status)
  },
  { 
    title: '创建时间', 
    key: 'createTime', 
    width: 180 
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
              onPositiveClick: () => handleDelete(row.id)
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
  loading.value = true
  console.log('搜索角色:', searchForm.value)
  setTimeout(() => {
    loading.value = false
  }, 500)
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.value = {
    name: '',
    code: ''
  }
  handleSearch()
}

/**
 * 添加角色
 */
const handleAdd = () => {
  console.log('添加角色')
}

/**
 * 编辑角色
 */
const handleEdit = (row: Role) => {
  console.log('编辑角色:', row)
}

/**
 * 权限配置
 */
const handlePermission = (row: Role) => {
  console.log('配置权限:', row)
}

/**
 * 删除角色
 */
const handleDelete = (id: number) => {
  console.log('删除角色:', id)
  roles.value = roles.value.filter(r => r.id !== id)
}
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
          <NButton type="primary" @click="handleAdd">
            <template #icon>
              <NIcon>
                <AddOutline />
              </NIcon>
            </template>
            添加角色
          </NButton>
        </template>
        
        <NDataTable 
          :columns="columns" 
          :data="roles" 
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
    </NSpace>
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