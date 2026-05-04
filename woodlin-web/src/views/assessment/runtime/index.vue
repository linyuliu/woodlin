<!--
  @file views/assessment/runtime/index.vue
  @description 评估实例：按方案/对象类型筛选 + 实例 CRUD + 提交评估 + 评分明细抽屉
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NCollapse,
  NCollapseItem,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
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
  createRuntime,
  deleteRuntime,
  getRuntime,
  pageRuntimes,
  pageSchemas,
  submitRuntime,
  type AssessmentRuntime,
  type AssessmentRuntimeQuery,
  type AssessmentSchema,
} from '@/api/assessment'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<AssessmentRuntime[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<AssessmentRuntimeQuery>({
  page: 1,
  size: 10,
  schemaId: undefined,
  status: undefined,
  targetType: undefined,
})

const schemaOptions = ref<SelectOption[]>([])
const schemaMap = ref<Record<number, string>>({})

const statusOptions: SelectOption[] = [
  { label: '草稿', value: '0' },
  { label: '已提交', value: '1' },
  { label: '已完成', value: '2' },
]

const targetTypeOptions: SelectOption[] = [
  { label: '员工', value: 'user' },
  { label: '部门', value: 'dept' },
  { label: '项目', value: 'project' },
]

const drawerVisible = ref(false)
const formRef = ref<FormInst | null>(null)
const submitLoading = ref(false)

function defaultForm(): AssessmentRuntime {
  return {
    schemaId: 0,
    targetId: '',
    targetType: 'user',
    startTime: undefined,
    endTime: undefined,
    remark: '',
  }
}

const formData = reactive<AssessmentRuntime>(defaultForm())

const rules: FormRules = {
  schemaId: [{ required: true, type: 'number', message: '请选择方案', trigger: 'change' }],
  targetId: [{ required: true, message: '请输入对象 ID', trigger: 'blur' }],
  targetType: [{ required: true, message: '请选择对象类型', trigger: 'change' }],
}

const detailVisible = ref(false)
const detailData = ref<AssessmentRuntime | null>(null)

async function loadSchemas(): Promise<void> {
  const res = await pageSchemas({ page: 1, size: 200 })
  const records = res?.records ?? []
  schemaOptions.value = records.map((s: AssessmentSchema) => ({
    label: s.schemaName,
    value: s.id as number,
  }))
  schemaMap.value = Object.fromEntries(records.map((s) => [s.id as number, s.schemaName]))
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageRuntimes(query)
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
  query.schemaId = undefined
  query.status = undefined
  query.targetType = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await createRuntime(formData)
    message.success('新增成功')
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

async function openDetail(row: AssessmentRuntime): Promise<void> {
  if (!row.id) return
  detailData.value = await getRuntime(row.id)
  detailVisible.value = true
}

function handleSubmitRuntime(row: AssessmentRuntime): void {
  if (!row.id) return
  dialog.warning({
    title: '提交评估',
    content: `提交后将无法修改，确认提交？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await submitRuntime(row.id as number)
      message.success('已提交')
      void refresh()
    },
  })
}

function handleDelete(row: AssessmentRuntime): void {
  if (!row.id) return
  dialog.warning({
    title: '提示',
    content: '确认删除该评估实例？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteRuntime(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

function statusTagType(s?: string): 'default' | 'success' | 'warning' | 'info' {
  if (s === '2') return 'success'
  if (s === '1') return 'info'
  return 'warning'
}

function statusLabel(s?: string): string {
  if (s === '2') return '已完成'
  if (s === '1') return '已提交'
  return '草稿'
}

const columns = computed<DataTableColumns<AssessmentRuntime>>(() => [
  {
    title: '评估方案',
    key: 'schemaName',
    width: 180,
    render: (row) => row.schemaName ?? schemaMap.value[row.schemaId] ?? '-',
  },
  { title: '对象类型', key: 'targetType', width: 100 },
  { title: '对象 ID', key: 'targetId', width: 140 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: statusTagType(row.status) },
        { default: () => statusLabel(row.status) },
      ),
  },
  { title: '开始时间', key: 'startTime', width: 170 },
  { title: '结束时间', key: 'endTime', width: 170 },
  { title: '总分', key: 'totalScore', width: 90 },
  {
    title: '操作',
    key: 'action',
    width: 240,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => void openDetail(row) },
          { default: () => '详情' },
        ),
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: 'primary',
            disabled: row.status !== '0',
            onClick: () => handleSubmitRuntime(row),
          },
          { default: () => '提交' },
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
])

onMounted(() => {
  void loadSchemas()
  void refresh()
})
</script>

<template>
  <div class="page-runtime">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="评估方案">
          <n-select
            v-model:value="query.schemaId"
            :options="schemaOptions"
            placeholder="评估方案"
            clearable
            filterable
            style="min-width: 180px"
          />
        </n-form-item>
        <n-form-item label="对象类型">
          <n-select
            v-model:value="query.targetType"
            :options="targetTypeOptions"
            placeholder="对象类型"
            clearable
            style="min-width: 140px"
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
        <n-button type="primary" @click="openAdd">新增</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: AssessmentRuntime) => row.id as number"
        :scroll-x="1300"
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

    <n-drawer v-model:show="drawerVisible" :width="540">
      <n-drawer-content title="新增评估实例" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="评估方案" path="schemaId">
            <n-select v-model:value="formData.schemaId" :options="schemaOptions" filterable />
          </n-form-item>
          <n-form-item label="对象类型" path="targetType">
            <n-select v-model:value="formData.targetType" :options="targetTypeOptions" />
          </n-form-item>
          <n-form-item label="对象 ID" path="targetId">
            <n-input v-model:value="formData.targetId" />
          </n-form-item>
          <n-form-item label="开始时间">
            <n-input v-model:value="formData.startTime" placeholder="YYYY-MM-DD HH:mm:ss" />
          </n-form-item>
          <n-form-item label="结束时间">
            <n-input v-model:value="formData.endTime" placeholder="YYYY-MM-DD HH:mm:ss" />
          </n-form-item>
          <n-form-item label="备注">
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

    <n-drawer v-model:show="detailVisible" :width="640">
      <n-drawer-content title="评估详情" closable>
        <template v-if="detailData">
          <n-descriptions label-placement="left" :column="2" bordered>
            <n-descriptions-item label="方案">
              {{ detailData.schemaName ?? schemaMap[detailData.schemaId] }}
            </n-descriptions-item>
            <n-descriptions-item label="对象">
              {{ detailData.targetType }} / {{ detailData.targetId }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="statusTagType(detailData.status)" size="small">
                {{ statusLabel(detailData.status) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="总分">{{ detailData.totalScore ?? '-' }}</n-descriptions-item>
            <n-descriptions-item label="开始时间">{{ detailData.startTime ?? '-' }}</n-descriptions-item>
            <n-descriptions-item label="结束时间">{{ detailData.endTime ?? '-' }}</n-descriptions-item>
          </n-descriptions>
          <n-collapse v-if="detailData.dimensions?.length" class="dim-collapse" arrow-placement="right">
            <n-collapse-item
              v-for="(dim, dIdx) in detailData.dimensions"
              :key="dIdx"
              :title="`${dim.dimName}（权重 ${dim.weight}，得分 ${dim.score ?? '-'}）`"
              :name="dIdx"
            >
              <n-descriptions label-placement="left" :column="1" bordered size="small">
                <n-descriptions-item
                  v-for="(ind, iIdx) in dim.indicators ?? []"
                  :key="iIdx"
                  :label="ind.indName"
                >
                  权重 {{ ind.weight }} ｜ 类型 {{ ind.scoreType }} ｜ 得分 {{ ind.score ?? '-' }}
                  <div v-if="ind.indDesc" class="ind-desc">{{ ind.indDesc }}</div>
                </n-descriptions-item>
              </n-descriptions>
            </n-collapse-item>
          </n-collapse>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.page-runtime {
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
.dim-collapse {
  margin-top: 16px;
}
.ind-desc {
  color: #888;
  font-size: 12px;
  margin-top: 4px;
}
</style>
