<!--
  @file views/schedule/job/index.vue
  @description 定时任务管理：CRUD + 暂停/恢复/立即执行 + Cron 表达式
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
  NRadio,
  NRadioGroup,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
} from 'naive-ui'
import {
  changeJobStatus,
  createJob,
  deleteJob,
  pageJobs,
  runJobOnce,
  updateJob,
  type ScheduleJob,
  type ScheduleJobQuery,
} from '@/api/schedule'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<ScheduleJob[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<ScheduleJobQuery>({
  page: 1,
  size: 10,
  jobName: '',
  jobGroup: undefined,
  status: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

const jobGroupOptions: SelectOption[] = [
  { label: 'DEFAULT', value: 'DEFAULT' },
  { label: 'SYSTEM', value: 'SYSTEM' },
]

const statusOptions: SelectOption[] = [
  { label: '正常', value: '1' },
  { label: '暂停', value: '0' },
]

function defaultForm(): ScheduleJob {
  return {
    jobName: '',
    jobGroup: 'DEFAULT',
    invokeTarget: '',
    cronExpression: '',
    misfirePolicy: '1',
    concurrent: '0',
    status: '1',
    remark: '',
  }
}

const formData = reactive<ScheduleJob>(defaultForm())

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请选择任务分组', trigger: 'change' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入 Cron 表达式', trigger: 'blur' }],
}

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageJobs(query)
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
  query.jobName = ''
  query.jobGroup = undefined
  query.status = undefined
  query.page = 1
  void refresh()
}

function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增任务'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

function openEdit(row: ScheduleJob): void {
  isEdit.value = true
  drawerTitle.value = '编辑任务'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateJob(formData)
      message.success('更新成功')
    } else {
      await createJob(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: ScheduleJob): void {
  if (!row.id) return
  dialog.warning({
    title: '提示',
    content: `确认删除任务 ${row.jobName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteJob(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

async function handleToggleStatus(row: ScheduleJob): Promise<void> {
  if (!row.id) return
  const next = row.status === '1' ? '0' : '1'
  await changeJobStatus(row.id, next)
  message.success(next === '1' ? '已恢复' : '已暂停')
  void refresh()
}

async function handleRun(row: ScheduleJob): Promise<void> {
  if (!row.id) return
  await runJobOnce(row.id)
  message.success('已触发执行')
}

const misfireText: Record<string, string> = {
  '1': '立即触发',
  '2': '触发一次',
  '3': '不触发',
}

const columns: DataTableColumns<ScheduleJob> = [
  { title: '任务名称', key: 'jobName', width: 160 },
  { title: '分组', key: 'jobGroup', width: 100 },
  { title: '调用目标', key: 'invokeTarget', width: 240, ellipsis: { tooltip: true } },
  { title: 'Cron 表达式', key: 'cronExpression', width: 160 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: row.status === '1' ? 'success' : 'warning' },
        { default: () => (row.status === '1' ? '正常' : '暂停') },
      ),
  },
  {
    title: '错过策略',
    key: 'misfirePolicy',
    width: 110,
    render: (row) => misfireText[row.misfirePolicy] ?? row.misfirePolicy,
  },
  { title: '备注', key: 'remark', width: 160, ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'action',
    width: 280,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: row.status === '1' ? 'warning' : 'success',
            onClick: () => handleToggleStatus(row),
          },
          { default: () => (row.status === '1' ? '暂停' : '恢复') },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => handleRun(row) },
          { default: () => '执行一次' },
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
  <div class="page-job">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="任务名称">
          <n-input v-model:value="query.jobName" placeholder="任务名称" clearable />
        </n-form-item>
        <n-form-item label="分组">
          <n-select
            v-model:value="query.jobGroup"
            :options="jobGroupOptions"
            placeholder="分组"
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
        :row-key="(row: ScheduleJob) => row.id as number"
        :scroll-x="1600"
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
          <n-form-item label="任务名称" path="jobName">
            <n-input v-model:value="formData.jobName" />
          </n-form-item>
          <n-form-item label="任务分组" path="jobGroup">
            <n-select v-model:value="formData.jobGroup" :options="jobGroupOptions" />
          </n-form-item>
          <n-form-item label="调用目标" path="invokeTarget">
            <n-input
              v-model:value="formData.invokeTarget"
              placeholder="如 demoTask.sayHello('woodlin')"
            />
          </n-form-item>
          <n-form-item label="Cron 表达式" path="cronExpression">
            <div style="width: 100%">
              <n-input v-model:value="formData.cronExpression" placeholder="0 0/5 * * * ?" />
              <a
                href="https://cron.qqe2.com/"
                target="_blank"
                rel="noopener noreferrer"
                style="font-size: 12px"
              >
                Cron 帮助
              </a>
            </div>
          </n-form-item>
          <n-form-item label="错过策略" path="misfirePolicy">
            <n-radio-group v-model:value="formData.misfirePolicy">
              <n-radio value="1">立即触发</n-radio>
              <n-radio value="2">触发一次</n-radio>
              <n-radio value="3">不触发</n-radio>
            </n-radio-group>
          </n-form-item>
          <n-form-item label="是否并发" path="concurrent">
            <n-radio-group v-model:value="formData.concurrent">
              <n-radio value="1">允许</n-radio>
              <n-radio value="0">禁止</n-radio>
            </n-radio-group>
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-switch
              :value="formData.status === '1'"
              @update:value="(v: boolean) => (formData.status = v ? '1' : '0')"
            />
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
.page-job {
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
