<script setup lang="ts">
/**
 * 用户管理视图
 * 
 * @author mumu
 * @description 用户管理页面，包含用户列表、搜索、添加、编辑、删除等功能
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
  TrashOutline
} from '@vicons/ionicons5'

/**
 * 用户数据接口
 */
interface User {
  id: number
  username: string
  nickname: string
  email: string
  status: string
  createTime: string
}

/**
 * 搜索表单数据
 */
const searchForm = ref({
  username: '',
  nickname: '',
  status: ''
})

/**
 * 加载状态
 */
const loading = ref(false)

/**
 * 用户列表数据
 */
const users = ref<User[]>([
  {
    id: 1,
    username: 'admin',
    nickname: '系统管理员',
    email: 'admin@woodlin.com',
    status: 'active',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user001',
    nickname: '普通用户',
    email: 'user001@woodlin.com',
    status: 'active',
    createTime: '2025-01-02 09:30:00'
  },
  {
    id: 3,
    username: 'user002',
    nickname: '测试用户',
    email: 'user002@woodlin.com',
    status: 'inactive',
    createTime: '2025-01-03 14:20:00'
  }
])

/**
 * 渲染状态标签
 */
const renderStatus = (status: string) => {
  const statusMap: Record<string, { type: 'success' | 'warning' | 'error', text: string }> = {
    active: { type: 'success', text: '正常' },
    inactive: { type: 'warning', text: '禁用' },
    locked: { type: 'error', text: '锁定' }
  }
  const config = statusMap[status] || { type: 'warning', text: '未知' }
  return h(NTag, { type: config.type, size: 'small' }, { default: () => config.text })
}

/**
 * 表格列配置
 */
const columns: DataTableColumns<User> = [
  { 
    title: 'ID', 
    key: 'id', 
    width: 80,
    align: 'center'
  },
  { 
    title: '用户名', 
    key: 'username',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  { 
    title: '昵称', 
    key: 'nickname',
    width: 150,
    ellipsis: {
      tooltip: true
    }
  },
  { 
    title: '邮箱', 
    key: 'email',
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
    width: 180,
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
              default: () => '确定要删除该用户吗？',
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
 * 搜索用户
 */
const handleSearch = () => {
  loading.value = true
  console.log('搜索用户:', searchForm.value)
  // 模拟搜索延迟
  setTimeout(() => {
    loading.value = false
  }, 500)
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.value = {
    username: '',
    nickname: '',
    status: ''
  }
  handleSearch()
}

/**
 * 添加用户
 */
const handleAdd = () => {
  console.log('添加用户')
}

/**
 * 编辑用户
 */
const handleEdit = (row: User) => {
  console.log('编辑用户:', row)
}

/**
 * 删除用户
 */
const handleDelete = (id: number) => {
  console.log('删除用户:', id)
  users.value = users.value.filter(u => u.id !== id)
}
</script>

<template>
  <div class="user-management-container">
    <NSpace vertical :size="16">
      <!-- 搜索区域 -->
      <NCard :bordered="false" class="search-card">
        <NForm inline :model="searchForm" label-placement="left">
          <NFormItem label="用户名">
            <NInput 
              v-model:value="searchForm.username" 
              placeholder="请输入用户名"
              clearable
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="昵称">
            <NInput 
              v-model:value="searchForm.nickname" 
              placeholder="请输入昵称"
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

      <!-- 用户列表 -->
      <NCard 
        title="用户列表" 
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
            添加用户
          </NButton>
        </template>
        
        <NDataTable 
          :columns="columns" 
          :data="users" 
          :loading="loading"
          :pagination="{ 
            pageSize: 10,
            showSizePicker: true,
            pageSizes: [10, 20, 50]
          }"
          :bordered="false"
          :single-line="false"
          striped
          class="user-table"
        />
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
.user-management-container {
  width: 100%;
  height: 100%;
}

.search-card {
  background: #fff;
}

.table-card {
  background: #fff;
}

.user-table {
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-table :deep(.n-data-table-th),
  .user-table :deep(.n-data-table-td) {
    padding: 8px 4px;
    font-size: 12px;
  }
}
</style>