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
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  NText,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import { AddOutline, CreateOutline, RefreshOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import {
  addFormVersion,
  deleteFormVersion,
  getFormVersionPage,
  updateFormVersion,
  type AssessmentFormVersion
} from '@/api/assessment'
import { useUserStore } from '@/stores'
import { PERMISSIONS } from '@/constants/permission-keys'

const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<AssessmentFormVersion[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchFormId = ref<string>('')
const searchVersionNo = ref('')
const searchStatus = ref<string | null>(null)

const canAdd = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_VERSION_ADD))
const canEdit = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_VERSION_EDIT))
const canDelete = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_VERSION_REMOVE))

const modalVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = ref<AssessmentFormVersion>({
  formId: '',
  versionNo: '',
  versionTag: '',
  status: 'draft',
  changeSummary: ''
})

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已编译', value: 'compiled' },
  { label: '已发布', value: 'published' },
  { label: '已废弃', value: 'deprecated' },
  { label: '已归档', value: 'archived' }
]

const statusColorMap: Record<string, 'default' | 'info' | 'success' | 'warning' | 'error'> = {
  draft: 'default',
  compiled: 'info',
  published: 'success',
  deprecated: 'warning',
  archived: 'default'
}

const statusLabelMap: Record<string, string> = {
  draft: '草稿',
  compiled: '已编译',
  published: '已发布',
  deprecated: '已废弃',
  archived: '已归档'
}

const formRules = {
  formId: { required: true, message: '请输入所属测评ID', trigger: 'blur' },
  versionNo: { required: true, message: '请输入版本号', trigger: 'blur' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFormVersionPage({
      formId: searchFormId.value || undefined,
      versionNo: searchVersionNo.value || undefined,
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
  searchVersionNo.value = ''
  searchStatus.value = null
  currentPage.value = 1
  loadData()
}

function handleOpenAdd() {
  isEditMode.value = false
  formModel.value = { formId: '', versionNo: '', versionTag: '', status: 'draft', changeSummary: '' }
  modalVisible.value = true
}

function handleOpenEdit(row: AssessmentFormVersion) {
  isEditMode.value = true
  formModel.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(versionId: number | string) {
  try {
    await deleteFormVersion(versionId)
    message.success('删除成功')
    loadData()
  } catch {
    message.error('删除失败')
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEditMode.value) {
      await updateFormVersion(formModel.value)
      message.success('修改成功')
    } else {
      await addFormVersion(formModel.value)
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

const columns: DataTableColumns<AssessmentFormVersion> = [
  {
    title: '测评ID',
    key: 'formId',
    width: 120,
    render: row => String(row.formId)
  },
  {
    title: '版本号',
    key: 'versionNo',
    width: 120,
    render: row =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.versionNo }),
        h(NText, { depth: 3, style: 'font-size:12px' }, { default: () => row.versionTag ?? '' })
      ])
  },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render: row =>
      h(
        NTag,
        { size: 'small', type: statusColorMap[row.status ?? ''] ?? 'default' },
        { default: () => statusLabelMap[row.status ?? ''] ?? row.status }
      )
  },
  {
    title: '变更说明',
    key: 'changeSummary',
    ellipsis: { tooltip: true },
    render: row => row.changeSummary ?? '-'
  },
  {
    title: '发布时间',
    key: 'publishedAt',
    width: 170,
    render: row => row.publishedAt ?? '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
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
      }
      if (canDelete.value) {
        btns.push(
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.versionId!) }, {
            default: () => '确认删除此版本？',
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
  <NCard title="测评版本管理" :bordered="false">
    <template #header-extra>
      <NSpace>
        <NButton :loading="loading" circle @click="loadData">
          <template #icon><NIcon><RefreshOutline /></NIcon></template>
        </NButton>
        <NButton v-if="canAdd" type="primary" @click="handleOpenAdd">
          <template #icon><NIcon><AddOutline /></NIcon></template>
          新增版本
        </NButton>
      </NSpace>
    </template>

    <NGrid :cols="4" :x-gap="12" :y-gap="8" class="mb-4">
      <NGridItem>
        <NInput v-model:value="searchFormId" placeholder="测评ID" clearable />
      </NGridItem>
      <NGridItem>
        <NInput v-model:value="searchVersionNo" placeholder="版本号" clearable />
      </NGridItem>
      <NGridItem>
        <NSelect v-model:value="searchStatus" :options="statusOptions" placeholder="版本状态" clearable />
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
    :title="isEditMode ? '编辑版本' : '新增版本'"
    preset="card"
    style="width: 520px"
    :mask-closable="false"
  >
    <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="90">
      <NFormItem label="测评ID" path="formId">
        <NInput v-model:value="formModel.formId" placeholder="所属测评ID" :disabled="isEditMode" />
      </NFormItem>
      <NFormItem label="版本号" path="versionNo">
        <NInput v-model:value="formModel.versionNo" placeholder="如 1.0.0" />
      </NFormItem>
      <NFormItem label="版本标签">
        <NInput v-model:value="formModel.versionTag" placeholder="如 初稿/正式版" />
      </NFormItem>
      <NFormItem label="状态">
        <NSelect v-model:value="formModel.status" :options="statusOptions" />
      </NFormItem>
      <NFormItem label="变更说明">
        <NInput v-model:value="formModel.changeSummary" type="textarea" :rows="3" placeholder="本版本变更说明" />
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
