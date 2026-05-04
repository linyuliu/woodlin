<!--
  @file views/assessment/schema/index.vue
  @description 评估方案：列表 + 维度/指标嵌套构建器（动态行）+ 分值类型单选
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDivider,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NPopconfirm,
  NRadio,
  NRadioGroup,
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
  createSchema,
  deleteSchema,
  getSchema,
  pageSchemas,
  updateSchema,
  type AssessmentDimension,
  type AssessmentIndicator,
  type AssessmentSchema,
  type AssessmentSchemaQuery,
} from '@/api/assessment'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<AssessmentSchema[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<AssessmentSchemaQuery>({
  page: 1,
  size: 10,
  schemaName: '',
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const statusOptions: SelectOption[] = [
  { label: '启用', value: '0' },
  { label: '禁用', value: '1' },
]

function defaultIndicator(): AssessmentIndicator {
  return { indName: '', indDesc: '', weight: 0, scoreType: '100' }
}

function defaultDimension(): AssessmentDimension {
  return { dimName: '', weight: 0, indicators: [defaultIndicator()] }
}

function defaultForm(): AssessmentSchema {
  return {
    schemaName: '',
    schemaDesc: '',
    status: '0',
    dimensions: [defaultDimension()],
  }
}

const formData = reactive<AssessmentSchema>(defaultForm())

const rules: FormRules = {
  schemaName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageSchemas(query)
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
  query.schemaName = ''
  query.status = undefined
  query.page = 1
  void refresh()
}

function resetForm(): void {
  const d = defaultForm()
  formData.id = undefined
  formData.schemaName = d.schemaName
  formData.schemaDesc = d.schemaDesc
  formData.status = d.status
  formData.dimensions = d.dimensions
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增评估方案'
  resetForm()
  drawerVisible.value = true
}

async function openEdit(row: AssessmentSchema): Promise<void> {
  if (!row.id) {return}
  isEdit.value = true
  drawerTitle.value = '编辑评估方案'
  const detail = await getSchema(row.id)
  formData.id = detail.id
  formData.schemaName = detail.schemaName
  formData.schemaDesc = detail.schemaDesc
  formData.status = detail.status ?? '0'
  formData.dimensions =
    detail.dimensions && detail.dimensions.length > 0
      ? detail.dimensions.map((d) => ({
          ...d,
          indicators: d.indicators?.length ? d.indicators : [defaultIndicator()],
        }))
      : [defaultDimension()]
  drawerVisible.value = true
}

function addDimension(): void {
  formData.dimensions = [...(formData.dimensions ?? []), defaultDimension()]
}

function removeDimension(idx: number): void {
  formData.dimensions = (formData.dimensions ?? []).filter((_, i) => i !== idx)
}

function addIndicator(dim: AssessmentDimension): void {
  dim.indicators = [...(dim.indicators ?? []), defaultIndicator()]
}

function removeIndicator(dim: AssessmentDimension, idx: number): void {
  dim.indicators = (dim.indicators ?? []).filter((_, i) => i !== idx)
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateSchema(formData.id, formData)
      message.success('更新成功')
    } else {
      await createSchema(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: AssessmentSchema): void {
  if (!row.id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除方案 ${row.schemaName}？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteSchema(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<AssessmentSchema> = [
  { title: '方案名称', key: 'schemaName', width: 200 },
  { title: '描述', key: 'schemaDesc', ellipsis: { tooltip: true } },
  { title: '维度数', key: 'dimensionCount', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '0' ? 'success' : 'error' },
        { default: () => (row.status === '0' ? '启用' : '禁用') },
      ),
  },
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
          { size: 'small', text: true, type: 'primary', onClick: () => void openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', text: true, type: 'error' },
                { default: () => '删除' },
              ),
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
  <div class="page-schema">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="方案名称">
          <n-input v-model:value="query.schemaName" placeholder="方案名称" clearable />
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
        <n-button type="primary" @click="openAdd">新增</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: AssessmentSchema) => row.id as number"
        :scroll-x="1200"
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

    <n-drawer v-model:show="drawerVisible" :width="800">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="方案名称" path="schemaName">
            <n-input v-model:value="formData.schemaName" />
          </n-form-item>
          <n-form-item label="方案描述" path="schemaDesc">
            <n-input v-model:value="formData.schemaDesc" type="textarea" :autosize="{ minRows: 2 }" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>

          <n-divider>评估维度</n-divider>
          <div
            v-for="(dim, dIdx) in formData.dimensions ?? []"
            :key="dIdx"
            class="dim-block"
          >
            <div class="row">
              <n-input v-model:value="dim.dimName" placeholder="维度名称" style="flex: 1" />
              <n-input-number
                v-model:value="dim.weight"
                placeholder="权重"
                :min="0"
                :max="100"
                style="width: 140px"
              />
              <n-button type="error" text @click="removeDimension(dIdx)">移除维度</n-button>
            </div>
            <div class="indicators">
              <div
                v-for="(ind, iIdx) in dim.indicators ?? []"
                :key="iIdx"
                class="indicator-row"
              >
                <n-input v-model:value="ind.indName" placeholder="指标名称" style="flex: 1" />
                <n-input v-model:value="ind.indDesc" placeholder="指标说明" style="flex: 1" />
                <n-input-number
                  v-model:value="ind.weight"
                  placeholder="权重"
                  :min="0"
                  :max="100"
                  style="width: 120px"
                />
                <n-radio-group v-model:value="ind.scoreType">
                  <n-radio value="100">100分制</n-radio>
                  <n-radio value="level">等级制</n-radio>
                  <n-radio value="custom">自定义</n-radio>
                </n-radio-group>
                <n-button type="error" text @click="removeIndicator(dim, iIdx)">删除</n-button>
              </div>
              <n-button dashed block @click="addIndicator(dim)">+ 添加指标</n-button>
            </div>
          </div>
          <n-button dashed block @click="addDimension">+ 添加维度</n-button>
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
.page-schema {
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
.dim-block {
  border: 1px solid var(--n-border-color, #eee);
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 12px;
}
.row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.indicators {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 12px;
}
.indicator-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
