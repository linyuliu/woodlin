<!--
  @file views/system/notice/index.vue
  @description 通知公告管理：列表 + 搜索 + 新增/编辑抽屉（textarea 内容）
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createNotice,
  deleteNotice,
  pageNotices,
  updateNotice,
  type NoticeQuery,
  type SysNotice,
} from '@/api/system/notice'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<SysNotice[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<NoticeQuery>({
  page: 1,
  size: 10,
  noticeTitle: '',
  noticeType: undefined,
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): SysNotice {
  return {
    noticeTitle: '',
    noticeType: '1',
    noticeContent: '',
    status: '1',
  }
}

const formData = reactive<SysNotice>(defaultForm())

const rules: FormRules = {
  noticeTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型' }],
}

const typeOptions: SelectOption[] = [
  { label: '通知', value: '1' },
  { label: '公告', value: '2' },
]

const statusOptions: SelectOption[] = [
  { label: '正常', value: '1' },
  { label: '关闭', value: '0' },
]

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageNotices(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function handleSearch(): void {
  query.page = 1
  void refresh()
}

/** 重置 */
function handleReset(): void {
  query.noticeTitle = ''
  query.noticeType = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

/** 新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增通知'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

/** 编辑 */
function openEdit(row: SysNotice): void {
  isEdit.value = true
  drawerTitle.value = '编辑通知'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateNotice(formData.id, formData)
      message.success('更新成功')
    } else {
      await createNotice(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除 */
function handleDelete(row: SysNotice): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除通知 ${row.noticeTitle} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteNotice(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<SysNotice> = [
  { title: '标题', key: 'noticeTitle' },
  {
    title: '类型',
    key: 'noticeType',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.noticeType === '1' ? 'info' : 'warning', size: 'small' },
        { default: () => (row.noticeType === '1' ? '通知' : '公告') },
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.status === '1' ? 'success' : 'default', size: 'small' },
        { default: () => (row.status === '1' ? '正常' : '关闭') },
      ),
  },
  { title: '创建人', key: 'createBy', width: 140 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(NButton, { size: 'small', text: true, type: 'error' }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-notice">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="标题">
          <n-input v-model:value="query.noticeTitle" placeholder="标题" clearable />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="query.noticeType"
            :options="typeOptions"
            placeholder="类型"
            clearable
            style="min-width: 120px"
          />
        </n-form-item>
        <n-form-item label="状态">
          <n-select
            v-model:value="query.status"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="min-width: 120px"
          />
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
        <n-button v-permission="'system:notice:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysNotice) => row.id as number"
        :scroll-x="1000"
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

    <n-drawer v-model:show="drawerVisible" :width="600">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="标题" path="noticeTitle">
            <n-input v-model:value="formData.noticeTitle" />
          </n-form-item>
          <n-form-item label="类型" path="noticeType">
            <n-select v-model:value="formData.noticeType" :options="typeOptions" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="内容" path="noticeContent">
            <n-input
              v-model:value="formData.noticeContent"
              type="textarea"
              :autosize="{ minRows: 6 }"
              placeholder="请输入通知公告内容"
            />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="drawerVisible = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
              确定
            </n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.page-notice {
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
