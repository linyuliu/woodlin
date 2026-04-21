<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  NText,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import { AddOutline, CreateOutline, RefreshOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import {
  addPublish,
  deletePublish,
  getPublishPage,
  updatePublish,
  updatePublishStatus,
  type AssessmentPublish
} from '@/api/assessment'
import { useUserStore } from '@/stores'
import { PERMISSIONS } from '@/constants/permission-keys'

const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<AssessmentPublish[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchFormId = ref<string>('')
const searchName = ref('')
const searchStatus = ref<string | null>(null)

const canAdd = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_PUBLISH_ADD))
const canEdit = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_PUBLISH_EDIT))
const canDelete = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_PUBLISH_REMOVE))

const modalVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = ref<AssessmentPublish>({
  formId: '',
  versionId: '',
  publishName: '',
  publishCode: '',
  status: 'draft',
  timeLimitMinutes: 0,
  maxAttempts: 0,
  allowAnonymous: false,
  allowResume: true,
  randomStrategy: 'none',
  showResultImmediately: false,
  resultVisibility: 'self',
  remark: ''
})

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '审核中', value: 'under_review' },
  { label: '已发布', value: 'published' },
  { label: '已暂停', value: 'paused' },
  { label: '已关闭', value: 'closed' },
  { label: '已归档', value: 'archived' }
]

const statusColorMap: Record<string, 'default' | 'info' | 'success' | 'warning' | 'error'> = {
  draft: 'default',
  under_review: 'info',
  published: 'success',
  paused: 'warning',
  closed: 'error',
  archived: 'default'
}

const statusLabelMap: Record<string, string> = {
  draft: '草稿',
  under_review: '审核中',
  published: '已发布',
  paused: '已暂停',
  closed: '已关闭',
  archived: '已归档'
}

const randomStrategyOptions = [
  { label: '不随机', value: 'none' },
  { label: '随机题目', value: 'random_items' },
  { label: '随机选项', value: 'random_options' },
  { label: '全部随机', value: 'random_both' }
]

const visibilityOptions = [
  { label: '仅本人', value: 'self' },
  { label: '管理员', value: 'admin' },
  { label: '所有人', value: 'all' }
]

const formRules = {
  formId: { required: true, message: '请输入测评ID', trigger: 'blur' },
  versionId: { required: true, message: '请输入版本ID', trigger: 'blur' },
  publishName: { required: true, message: '请输入发布名称', trigger: 'blur' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPublishPage({
      formId: searchFormId.value || undefined,
      publishName: searchName.value || undefined,
      status: searchStatus.value || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    list.value = res.data ?? []
    total.value = Number(res.total) ?? 0
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  searchFormId.value = ''
  searchName.value = ''
  searchStatus.value = null
  currentPage.value = 1
  loadData()
}

function handleOpenAdd() {
  isEditMode.value = false
  formModel.value = {
    formId: '', versionId: '', publishName: '', publishCode: '', status: 'draft',
    timeLimitMinutes: 0, maxAttempts: 0, allowAnonymous: false, allowResume: true,
    randomStrategy: 'none', showResultImmediately: false, resultVisibility: 'self', remark: ''
  }
  modalVisible.value = true
}

function handleOpenEdit(row: AssessmentPublish) {
  isEditMode.value = true
  formModel.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(publishId: number | string) {
  try {
    await deletePublish(publishId)
    message.success('删除成功')
    loadData()
  } catch {
    message.error('删除失败')
  }
}

async function handleUpdateStatus(publishId: number | string, status: string) {
  try {
    await updatePublishStatus(publishId, status)
    message.success('状态已更新')
    loadData()
  } catch {
    message.error('操作失败')
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEditMode.value) {
      await updatePublish(formModel.value)
      message.success('修改成功')
    } else {
      await addPublish(formModel.value)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch {
    message.error(isEditMode.value ? '修改失败' : '新增失败')
  } finally {
    submitLoading.value = false
  }
}

const columns: DataTableColumns<AssessmentPublish> = [
  {
    title: '发布名称 / 编码',
    key: 'publishName',
    width: 190,
    render: row =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.publishName }),
        h(NText, { depth: 3, style: 'font-size:12px' }, { default: () => row.publishCode ?? '' })
      ])
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row =>
      h(
        NTag,
        { size: 'small', type: statusColorMap[row.status ?? ''] ?? 'default' },
        { default: () => statusLabelMap[row.status ?? ''] ?? row.status }
      )
  },
  {
    title: '测评ID',
    key: 'formId',
    width: 110,
    render: row => String(row.formId)
  },
  {
    title: '版本ID',
    key: 'versionId',
    width: 110,
    render: row => String(row.versionId)
  },
  {
    title: '时限(分)',
    key: 'timeLimitMinutes',
    width: 90,
    render: row => (row.timeLimitMinutes === 0 ? '不限' : String(row.timeLimitMinutes))
  },
  {
    title: '允许匿名',
    key: 'allowAnonymous',
    width: 90,
    render: row => h(NTag, { size: 'small', type: row.allowAnonymous ? 'success' : 'default' }, { default: () => (row.allowAnonymous ? '是' : '否') })
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 160,
    render: row => row.updateTime ?? row.createTime ?? '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render: row => {
      const btns: ReturnType<typeof h>[] = []
      if (canEdit.value) {
        btns.push(
          h(
            NButton,
            { size: 'small', tertiary: true, onClick: () => handleOpenEdit(row) },
            { default: () => '编辑', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }
          )
        )
        if (row.status === 'draft') {
          btns.push(
            h(
              NButton,
              { size: 'small', tertiary: true, type: 'success', onClick: () => handleUpdateStatus(row.publishId!, 'published') },
              { default: () => '发布' }
            )
          )
        } else if (row.status === 'published') {
          btns.push(
            h(
              NButton,
              { size: 'small', tertiary: true, type: 'warning', onClick: () => handleUpdateStatus(row.publishId!, 'paused') },
              { default: () => '暂停' }
            )
          )
        }
      }
      if (canDelete.value) {
        btns.push(
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.publishId!) }, {
            default: () => '确认删除此发布？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', tertiary: true, type: 'error' },
                { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }
              )
          })
        )
      }
      return h(NSpace, null, { default: () => btns })
    }
  }
]

onMounted(() => loadData())
</script>

<template>
  <NCard title="发布管理" :bordered="false">
    <template #header-extra>
      <NSpace>
        <NButton :loading="loading" circle @click="loadData">
          <template #icon><NIcon><RefreshOutline /></NIcon></template>
        </NButton>
        <NButton v-if="canAdd" type="primary" @click="handleOpenAdd">
          <template #icon><NIcon><AddOutline /></NIcon></template>
          新增发布
        </NButton>
      </NSpace>
    </template>

    <NGrid :cols="4" :x-gap="12" :y-gap="8" class="mb-4">
      <NGridItem>
        <NInput v-model:value="searchFormId" placeholder="测评ID" clearable />
      </NGridItem>
      <NGridItem>
        <NInput v-model:value="searchName" placeholder="发布名称" clearable @keyup.enter="handleSearch" />
      </NGridItem>
      <NGridItem>
        <NSelect v-model:value="searchStatus" :options="statusOptions" placeholder="发布状态" clearable />
      </NGridItem>
      <NGridItem>
        <NSpace>
          <NButton type="primary" @click="handleSearch">
            <template #icon><NIcon><SearchOutline /></NIcon></template>
            搜索
          </NButton>
          <NButton @click="handleReset">重置</NButton>
        </NSpace>
      </NGridItem>
    </NGrid>

    <NDataTable
      :columns="columns"
      :data="list"
      :loading="loading"
      :pagination="{
        page: currentPage,
        pageSize,
        itemCount: total,
        showSizePicker: true,
        pageSizes: [10, 20, 50],
        onChange: (p: number) => { currentPage = p; loadData() },
        onUpdatePageSize: (s: number) => { pageSize = s; currentPage = 1; loadData() }
      }"
      striped
    />
  </NCard>

  <NModal
    v-model:show="modalVisible"
    :title="isEditMode ? '编辑发布' : '新增发布'"
    preset="card"
    style="width: 600px"
    :mask-closable="false"
  >
    <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="100">
      <NFormItem label="测评ID" path="formId">
        <NInput v-model:value="formModel.formId" placeholder="所属测评ID" :disabled="isEditMode" />
      </NFormItem>
      <NFormItem label="版本ID" path="versionId">
        <NInput v-model:value="formModel.versionId" placeholder="绑定的版本ID" />
      </NFormItem>
      <NFormItem label="发布名称" path="publishName">
        <NInput v-model:value="formModel.publishName" placeholder="如：2025年Q1批次" />
      </NFormItem>
      <NFormItem label="发布编码">
        <NInput v-model:value="formModel.publishCode" placeholder="可选，用于生成访问链接" />
      </NFormItem>
      <NFormItem label="状态">
        <NSelect v-model:value="formModel.status" :options="statusOptions" />
      </NFormItem>
      <NFormItem label="时限(分钟)">
        <NInputNumber v-model:value="formModel.timeLimitMinutes" placeholder="0=不限" :min="0" />
      </NFormItem>
      <NFormItem label="最多作答次数">
        <NInputNumber v-model:value="formModel.maxAttempts" placeholder="0=不限" :min="0" />
      </NFormItem>
      <NFormItem label="随机策略">
        <NSelect v-model:value="formModel.randomStrategy" :options="randomStrategyOptions" />
      </NFormItem>
      <NFormItem label="结果可见">
        <NSelect v-model:value="formModel.resultVisibility" :options="visibilityOptions" />
      </NFormItem>
      <NFormItem label="允许匿名">
        <NSwitch v-model:value="formModel.allowAnonymous" />
      </NFormItem>
      <NFormItem label="断点续答">
        <NSwitch v-model:value="formModel.allowResume" />
      </NFormItem>
      <NFormItem label="即时展示结果">
        <NSwitch v-model:value="formModel.showResultImmediately" />
      </NFormItem>
      <NFormItem label="备注">
        <NInput v-model:value="formModel.remark" placeholder="备注" />
      </NFormItem>
    </NForm>
    <template #footer>
      <NSpace justify="end">
        <NButton @click="modalVisible = false">取消</NButton>
        <NButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
