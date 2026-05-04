<!--
  @file views/system/dict/index.vue
  @description 字典类型管理：列表 + 搜索 + 新增/编辑抽屉，行可跳转到字典项
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import { useRouter } from 'vue-router'
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
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  createDictType,
  deleteDictType,
  pageDictTypes,
  updateDictType,
  type DictTypeQuery,
  type SysDictType,
} from '@/api/system/dict'

const message = useMessage()
const dialog = useDialog()
const router = useRouter()

const tableData: Ref<SysDictType[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<DictTypeQuery>({
  page: 1,
  size: 10,
  dictName: '',
  dictType: '',
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): SysDictType {
  return { dictName: '', dictType: '', status: '1', remark: '' }
}

const formData = reactive<SysDictType>(defaultForm())

const rules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageDictTypes(query)
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
  query.dictName = ''
  query.dictType = ''
  query.page = 1
  void refresh()
}

/** 新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增字典类型'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

/** 编辑 */
function openEdit(row: SysDictType): void {
  isEdit.value = true
  drawerTitle.value = '编辑字典类型'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateDictType(formData.id, formData)
      message.success('更新成功')
    } else {
      await createDictType(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除 */
function handleDelete(row: SysDictType): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除字典 ${row.dictName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDictType(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

/** 查看字典项 */
function viewItems(row: SysDictType): void {
  void router.push({ path: '/system/dict/data', query: { dictType: row.dictType } })
}

const columns: DataTableColumns<SysDictType> = [
  { title: '字典名称', key: 'dictName' },
  { title: '字典类型', key: 'dictType' },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => (row.status === '1' ? '启用' : '禁用'),
  },
  { title: '备注', key: 'remark' },
  {
    title: '操作',
    key: 'action',
    width: 220,
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => viewItems(row) },
          { default: () => '字典项' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => openEdit(row) },
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
  <div class="page-dict">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="字典名称">
          <n-input v-model:value="query.dictName" placeholder="字典名称" clearable />
        </n-form-item>
        <n-form-item label="字典类型">
          <n-input v-model:value="query.dictType" placeholder="字典类型" clearable />
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
        <n-button v-permission="'system:dict:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysDictType) => row.id as number"
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

    <n-drawer v-model:show="drawerVisible" :width="520">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="字典名称" path="dictName">
            <n-input v-model:value="formData.dictName" />
          </n-form-item>
          <n-form-item label="字典类型" path="dictType">
            <n-input v-model:value="formData.dictType" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="备注" path="remark">
            <n-input v-model:value="formData.remark" type="textarea" />
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
.page-dict {
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
