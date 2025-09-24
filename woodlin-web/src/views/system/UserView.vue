<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NButton, NDataTable, NSpace, NInput, NForm, NFormItem } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface User {
  id: number
  username: string
  nickname: string
  email: string
  status: string
  createTime: string
}

const searchForm = ref({
  username: '',
  status: ''
})

const users = ref<User[]>([
  {
    id: 1,
    username: 'admin',
    nickname: '系统管理员',
    email: 'admin@woodlin.com',
    status: '正常',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user001',
    nickname: '普通用户',
    email: 'user001@woodlin.com',
    status: '正常',
    createTime: '2025-01-02 09:30:00'
  }
])

const columns: DataTableColumns<User> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '状态', key: 'status', width: 100 },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: () => {
      return [
        NButton.create({ text: true, type: 'primary' }, '编辑'),
        ' | ',
        NButton.create({ text: true, type: 'error' }, '删除')
      ]
    }
  }
]

const handleSearch = () => {
  console.log('搜索用户:', searchForm.value)
}

const handleAdd = () => {
  console.log('添加用户')
}
</script>

<template>
  <div>
    <NSpace vertical size="large">
      <NCard title="用户管理">
        <template #header-extra>
          <NButton type="primary" @click="handleAdd">添加用户</NButton>
        </template>
        
        <NSpace vertical size="medium">
          <NForm inline :model="searchForm">
            <NFormItem label="用户名">
              <NInput v-model:value="searchForm.username" placeholder="请输入用户名" />
            </NFormItem>
            <NFormItem label="状态">
              <NInput v-model:value="searchForm.status" placeholder="请选择状态" />
            </NFormItem>
            <NFormItem>
              <NButton type="primary" @click="handleSearch">搜索</NButton>
            </NFormItem>
          </NForm>
          
          <NDataTable 
            :columns="columns" 
            :data="users" 
            :pagination="{ pageSize: 10 }"
            :bordered="false"
          />
        </NSpace>
      </NCard>
    </NSpace>
  </div>
</template>