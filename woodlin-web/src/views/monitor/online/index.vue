<!--
  @file views/monitor/online/index.vue
  @description 在线用户：分页查询 + 单个/批量强制下线
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NPagination,
  NPopconfirm,
  NSpace,
  useDialog,
  useMessage,
  type DataTableColumns,
  type DataTableRowKey,
} from 'naive-ui'
import {
  batchForceLogout,
  forceLogout,
  pageOnline,
  type OnlineUser,
  type OnlineUserQuery,
} from '@/api/monitor'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<OnlineUser[]> = ref([])
const loading = ref(false)
const total = ref(0)
const checkedKeys = ref<DataTableRowKey[]>([])

const query = reactive<OnlineUserQuery>({
  page: 1,
  size: 10,
  username: '',
  ipaddr: '',
})

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageOnline(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.page = 1
  void refresh()
}

function handleReset(): void {
  query.username = ''
  query.ipaddr = ''
  query.page = 1
  void refresh()
}

function handleKick(row: OnlineUser): void {
  dialog.warning({
    title: '强制下线',
    content: `确认将用户 ${row.username} 强制下线？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await forceLogout(row.tokenId)
      message.success('已下线')
      void refresh()
    },
  })
}

function handleBatchKick(): void {
  if (checkedKeys.value.length === 0) {
    message.warning('请至少选择一项')
    return
  }
  dialog.warning({
    title: '批量踢出',
    content: `确认将选中的 ${checkedKeys.value.length} 个用户强制下线？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchForceLogout(checkedKeys.value as string[])
      message.success('已下线')
      checkedKeys.value = []
      void refresh()
    },
  })
}

const columns: DataTableColumns<OnlineUser> = [
  { type: 'selection' },
  { title: '用户名', key: 'username', width: 140 },
  { title: 'IP 地址', key: 'ipaddr', width: 140 },
  { title: '登录地点', key: 'loginLocation', width: 160 },
  { title: '浏览器', key: 'browser', width: 140 },
  { title: '操作系统', key: 'os', width: 140 },
  { title: '登录时间', key: 'loginTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
    render: (row) =>
      h(
        NPopconfirm,
        { onPositiveClick: () => handleKick(row) },
        {
          default: () => `确认踢出 ${row.username}？`,
          trigger: () =>
            h(
              NButton,
              { size: 'small', text: true, type: 'error' },
              { default: () => '强制下线' },
            ),
        },
      ),
  },
]

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-online">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="用户名">
          <n-input v-model:value="query.username" placeholder="用户名" clearable />
        </n-form-item>
        <n-form-item label="IP 地址">
          <n-input v-model:value="query.ipaddr" placeholder="IP 地址" clearable />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch">查询</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card size="small">
      <div class="toolbar">
        <n-button type="error" :disabled="checkedKeys.length === 0" @click="handleBatchKick">
          批量踢出
        </n-button>
      </div>
      <n-data-table
        v-model:checked-row-keys="checkedKeys"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: OnlineUser) => row.tokenId"
        :scroll-x="1100"
        striped
      />
      <div class="pagination">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.size"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="refresh"
          @update:page-size="refresh"
        />
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.page-online {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  margin-bottom: 12px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
