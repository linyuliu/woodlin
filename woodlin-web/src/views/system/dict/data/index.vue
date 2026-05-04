<!--
  @file views/system/dict/data/index.vue
  @description 字典项管理：根据 query.dictType 加载，列表 + 新增/编辑
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, watch, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
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
  createDictData,
  deleteDictData,
  pageDictData,
  updateDictData,
  type DictDataQuery,
  type SysDictData,
} from '@/api/system/dict'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const dictType = ref(String(route.query.dictType ?? ''))
const tableData: Ref<SysDictData[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<DictDataQuery>({
  page: 1,
  size: 10,
  dictType: dictType.value,
  dictLabel: '',
})

watch(
  () => route.query.dictType,
  (val) => {
    dictType.value = String(val ?? '')
    query.dictType = dictType.value
    void refresh()
  },
)

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): SysDictData {
  return {
    dictType: dictType.value,
    dictLabel: '',
    dictValue: '',
    cssClass: '',
    listClass: 'default',
    isDefault: 'N',
    sort: 0,
    status: '1',
  }
}

const formData = reactive<SysDictData>(defaultForm())

const rules: FormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

const tagTypeOptions: SelectOption[] = [
  { label: 'default', value: 'default' },
  { label: 'primary', value: 'primary' },
  { label: 'success', value: 'success' },
  { label: 'info', value: 'info' },
  { label: 'warning', value: 'warning' },
  { label: 'error', value: 'error' },
]

const yesNoOptions: SelectOption[] = [
  { label: '是', value: 'Y' },
  { label: '否', value: 'N' },
]

/** 拉取列表 */
async function refresh(): Promise<void> {
  if (!query.dictType) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await pageDictData(query)
    tableData.value = (res?.records ?? []) as SysDictData[]
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
  query.dictLabel = ''
  query.page = 1
  void refresh()
}

/** 新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增字典项'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

/** 编辑 */
function openEdit(row: SysDictData): void {
  isEdit.value = true
  drawerTitle.value = '编辑字典项'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateDictData(formData.id, formData)
      message.success('更新成功')
    } else {
      await createDictData(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除 */
function handleDelete(row: SysDictData): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除字典项 ${row.dictLabel} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDictData(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

/** 返回 */
function back(): void {
  void router.push('/system/dict')
}

const columns: DataTableColumns<SysDictData> = [
  { title: '字典标签', key: 'dictLabel', width: 160 },
  { title: '字典值', key: 'dictValue', width: 160 },
  {
    title: '颜色',
    key: 'listClass',
    width: 120,
    render: (row) =>
      h(
        NTag,
        {
          type: (row.listClass as 'default' | 'primary' | 'success' | 'info' | 'warning' | 'error') ?? 'default',
        },
        { default: () => row.dictLabel },
      ),
  },
  { title: 'css 类', key: 'cssClass', width: 140 },
  {
    title: '默认',
    key: 'isDefault',
    width: 80,
    render: (row) => (row.isDefault === 'Y' ? '是' : '否'),
  },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '操作',
    key: 'action',
    width: 180,
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
  <div class="page-dict-data">
    <n-card size="small">
      <n-space justify="space-between" align="center">
        <span>当前字典类型：<b>{{ dictType || '-' }}</b></span>
        <n-button @click="back">返回字典列表</n-button>
      </n-space>
    </n-card>
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="字典标签">
          <n-input v-model:value="query.dictLabel" clearable placeholder="字典标签" />
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
        <n-button v-permission="'system:dict:add'" type="primary" :disabled="!dictType" @click="openAdd">
          新增字典项
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysDictData) => row.id as number"
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
          <n-form-item label="字典标签" path="dictLabel">
            <n-input v-model:value="formData.dictLabel" />
          </n-form-item>
          <n-form-item label="字典值" path="dictValue">
            <n-input v-model:value="formData.dictValue" />
          </n-form-item>
          <n-form-item label="标签颜色" path="listClass">
            <n-select v-model:value="formData.listClass" :options="tagTypeOptions" />
          </n-form-item>
          <n-form-item label="CSS 类" path="cssClass">
            <n-input v-model:value="formData.cssClass" />
          </n-form-item>
          <n-form-item label="是否默认" path="isDefault">
            <n-select v-model:value="formData.isDefault" :options="yesNoOptions" />
          </n-form-item>
          <n-form-item label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" />
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
.page-dict-data {
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
