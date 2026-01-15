<script setup lang="ts">
/**
 * 用户管理视图
 *
 * @author mumu
 * @description 用户管理页面，包含用户列表、搜索、添加、编辑、删除等功能
 * @since 2025-01-01
 */
import {h, ref} from 'vue'
import {
  type DataTableColumns,
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NPopconfirm,
  NSpace,
  NTag,
  NTooltip
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import PageContainer from '@/components/PageContainer.vue'

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
  return h(NTag, {
    type: config.type,
    size: 'small',
    round: true,
  }, {default: () => config.text})
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
    width: 160,
    align: 'center',
    render: (row) => {
      return h(NSpace, {size: 8, justify: 'center'}, {
        default: () => [
          h(
            NTooltip,
            {trigger: 'hover'},
            {
              trigger: () => h(
                NButton,
                {
                  text: true,
                  type: 'primary',
                  onClick: () => handleEdit(row)
                },
                {
                  icon: () => h(NIcon, {size: 18}, {default: () => h(CreateOutline)})
                }
              ),
              default: () => '编辑'
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
                NTooltip,
                {trigger: 'hover'},
                {
                  trigger: () => h(
                    NButton,
                    {
                      text: true,
                      type: 'error'
                    },
                    {
                      icon: () => h(NIcon, {size: 18}, {default: () => h(TrashOutline)})
                    }
                  ),
                  default: () => '删除'
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
  <PageContainer title="用户列表">
    <template #search>
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
          </NSpace>
        </NFormItem>
      </NForm>
    </template>

    <template #header-extra>
      <NButton type="primary" @click="handleAdd">
        <template #icon>
          <NIcon>
            <AddOutline/>
          </NIcon>
        </template>
        添加用户
      </NButton>
    </template>

    <NDataTable
      :bordered="false"
      :columns="columns"
      :data="users"
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
  </PageContainer>
</template>

<style scoped>
.data-table {
  margin-top: 8px;
}

.data-table :deep(.n-data-table-th) {
  font-weight: 600;
}

.data-table :deep(.n-data-table-td) {
  padding: 12px 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .data-table :deep(.n-data-table-th),
  .data-table :deep(.n-data-table-td) {
    padding: 8px 4px;
    font-size: 12px;
  }
}
</style>
