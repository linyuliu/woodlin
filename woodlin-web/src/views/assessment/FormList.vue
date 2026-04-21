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
  NTag,
  NText,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import { AddOutline, CreateOutline, RefreshOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import {
  addAssessmentForm,
  deleteAssessmentForm,
  getAssessmentFormPage,
  updateAssessmentForm,
  updateAssessmentFormStatus,
  type AssessmentForm
} from '@/api/assessment'
import { useUserStore } from '@/stores'
import { PERMISSIONS } from '@/constants/permission-keys'

const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<AssessmentForm[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchName = ref('')
const searchType = ref<string | null>(null)
const searchStatus = ref<number | null>(null)

const canAdd = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_FORM_ADD))
const canEdit = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_FORM_EDIT))
const canDelete = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_FORM_REMOVE))

const modalVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = ref<AssessmentForm>({
  formCode: '',
  formName: '',
  assessmentType: 'scale',
  status: 1,
  sortOrder: 0,
  description: '',
  remark: ''
})

const typeOptions = [
  { label: '量表', value: 'scale' },
  { label: '试卷/考试', value: 'exam' },
  { label: '问卷调查', value: 'survey' }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const formRules = {
  formCode: { required: true, message: '请输入编码', trigger: 'blur' },
  formName: { required: true, message: '请输入名称', trigger: 'blur' },
  assessmentType: { required: true, message: '请选择类型', trigger: 'change' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAssessmentFormPage({
      formName: searchName.value || undefined,
      assessmentType: searchType.value || undefined,
      status: searchStatus.value !== null ? searchStatus.value : undefined,
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
  searchName.value = ''
  searchType.value = null
  searchStatus.value = null
  currentPage.value = 1
  loadData()
}

function handleOpenAdd() {
  isEditMode.value = false
  formModel.value = { formCode: '', formName: '', assessmentType: 'scale', status: 1, sortOrder: 0, description: '', remark: '' }
  modalVisible.value = true
}

function handleOpenEdit(row: AssessmentForm) {
  isEditMode.value = true
  formModel.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(formId: number | string) {
  try {
    await deleteAssessmentForm(formId)
    message.success('删除成功')
    loadData()
  } catch {
    message.error('删除失败')
  }
}

async function handleToggleStatus(row: AssessmentForm) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateAssessmentFormStatus(row.formId!, newStatus)
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
      await updateAssessmentForm(formModel.value)
      message.success('修改成功')
    } else {
      await addAssessmentForm(formModel.value)
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

const columns: DataTableColumns<AssessmentForm> = [
  {
    title: '名称 / 编码',
    key: 'formName',
    width: 200,
    render: row =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.formName }),
        h(NText, { depth: 3, style: 'font-size:12px' }, { default: () => row.formCode })
      ])
  },
  {
    title: '类型',
    key: 'assessmentType',
    width: 100,
    render: row => {
      const labelMap: Record<string, string> = { scale: '量表', exam: '试卷', survey: '问卷' }
      return h(NTag, { size: 'small', type: 'info' }, { default: () => labelMap[row.assessmentType] ?? row.assessmentType })
    }
  },
  {
    title: '分类',
    key: 'categoryCode',
    width: 120,
    render: row => row.categoryCode ?? '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row =>
      h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'warning' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  {
    title: '排序',
    key: 'sortOrder',
    width: 70
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 170,
    render: row => row.updateTime ?? row.createTime ?? '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 230,
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
        btns.push(
          h(
            NButton,
            { size: 'small', tertiary: true, type: row.status === 1 ? 'warning' : 'success', onClick: () => handleToggleStatus(row) },
            { default: () => (row.status === 1 ? '禁用' : '启用') }
          )
        )
      }
      if (canDelete.value) {
        btns.push(
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.formId!) }, {
            default: () => '确认删除此测评？',
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
  <NCard title="测评表单管理" :bordered="false">
    <template #header-extra>
      <NSpace>
        <NButton :loading="loading" circle @click="loadData">
          <template #icon><NIcon><RefreshOutline /></NIcon></template>
        </NButton>
        <NButton v-if="canAdd" type="primary" @click="handleOpenAdd">
          <template #icon><NIcon><AddOutline /></NIcon></template>
          新增测评
        </NButton>
      </NSpace>
    </template>

    <!-- 搜索栏 -->
    <NGrid :cols="4" :x-gap="12" :y-gap="8" class="mb-4">
      <NGridItem>
        <NInput v-model:value="searchName" placeholder="测评名称" clearable @keyup.enter="handleSearch" />
      </NGridItem>
      <NGridItem>
        <NSelect v-model:value="searchType" :options="typeOptions" placeholder="测评类型" clearable />
      </NGridItem>
      <NGridItem>
        <NSelect v-model:value="searchStatus" :options="statusOptions" placeholder="状态" clearable />
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

  <!-- 新增/编辑弹窗 -->
  <NModal
    v-model:show="modalVisible"
    :title="isEditMode ? '编辑测评' : '新增测评'"
    preset="card"
    style="width: 560px"
    :mask-closable="false"
  >
    <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="90">
      <NFormItem label="编码" path="formCode">
        <NInput v-model:value="formModel.formCode" placeholder="唯一编码（字母+数字）" :disabled="isEditMode" />
      </NFormItem>
      <NFormItem label="名称" path="formName">
        <NInput v-model:value="formModel.formName" placeholder="测评名称" />
      </NFormItem>
      <NFormItem label="类型" path="assessmentType">
        <NSelect v-model:value="formModel.assessmentType" :options="typeOptions" />
      </NFormItem>
      <NFormItem label="分类编码">
        <NInput v-model:value="formModel.categoryCode" placeholder="可选分类编码" />
      </NFormItem>
      <NFormItem label="排序">
        <NInputNumber v-model:value="formModel.sortOrder" placeholder="数字越小越靠前" :min="0" />
      </NFormItem>
      <NFormItem label="简介">
        <NInput v-model:value="formModel.description" type="textarea" :rows="3" placeholder="测评简介" />
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
