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
  addNormSet,
  deleteNormSet,
  getNormSetPage,
  updateNormSet,
  type AssessmentNormSet
} from '@/api/assessment'
import { useUserStore } from '@/stores'
import { PERMISSIONS } from '@/constants/permission-keys'

const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const list = ref<AssessmentNormSet[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const searchFormId = ref<string>('')
const searchName = ref('')
const searchStatus = ref<number | null>(null)

const canAdd = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_NORM_ADD))
const canEdit = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_NORM_EDIT))
const canDelete = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.ASSESSMENT_NORM_REMOVE))

const modalVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = ref<AssessmentNormSet>({
  formId: '',
  normSetName: '',
  normSetCode: '',
  sampleSize: undefined,
  sourceDesc: '',
  applicabilityDesc: '',
  isDefault: false,
  status: 1
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const formRules = {
  formId: { required: true, message: '请输入所属测评ID', trigger: 'blur' },
  normSetName: { required: true, message: '请输入常模集名称', trigger: 'blur' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getNormSetPage({
      formId: searchFormId.value || undefined,
      normSetName: searchName.value || undefined,
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
  searchFormId.value = ''
  searchName.value = ''
  searchStatus.value = null
  currentPage.value = 1
  loadData()
}

function handleOpenAdd() {
  isEditMode.value = false
  formModel.value = { formId: '', normSetName: '', normSetCode: '', sampleSize: undefined, sourceDesc: '', applicabilityDesc: '', isDefault: false, status: 1 }
  modalVisible.value = true
}

function handleOpenEdit(row: AssessmentNormSet) {
  isEditMode.value = true
  formModel.value = { ...row }
  modalVisible.value = true
}

async function handleDelete(normSetId: number | string) {
  try {
    await deleteNormSet(normSetId)
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
      await updateNormSet(formModel.value)
      message.success('修改成功')
    } else {
      await addNormSet(formModel.value)
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

const columns: DataTableColumns<AssessmentNormSet> = [
  {
    title: '常模集名称 / 编码',
    key: 'normSetName',
    width: 200,
    render: row =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.normSetName }),
        h(NText, { depth: 3, style: 'font-size:12px' }, { default: () => row.normSetCode ?? '' })
      ])
  },
  {
    title: '测评ID',
    key: 'formId',
    width: 110,
    render: row => String(row.formId)
  },
  {
    title: '样本量',
    key: 'sampleSize',
    width: 90,
    render: row => (row.sampleSize !== null && row.sampleSize !== undefined ? String(row.sampleSize) : '-')
  },
  {
    title: '默认',
    key: 'isDefault',
    width: 80,
    render: row =>
      h(NTag, { size: 'small', type: row.isDefault ? 'info' : 'default' }, { default: () => (row.isDefault ? '是' : '否') })
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row =>
      h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'warning' }, { default: () => (row.status === 1 ? '启用' : '停用') })
  },
  {
    title: '采集区间',
    key: 'collectionRange',
    width: 190,
    render: row => {
      if (!row.collectionStart && !row.collectionEnd) {return '-'}
      return `${row.collectionStart ?? '?'} ~ ${row.collectionEnd ?? '?'}`
    }
  },
  {
    title: '适用范围',
    key: 'applicabilityDesc',
    ellipsis: { tooltip: true },
    render: row => row.applicabilityDesc ?? '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
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
          h(NPopconfirm, { onPositiveClick: () => handleDelete(row.normSetId!) }, {
            default: () => '确认删除此常模集？',
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
  <NCard title="常模集管理" :bordered="false">
    <template #header-extra>
      <NSpace>
        <NButton :loading="loading" circle @click="loadData">
          <template #icon><NIcon><RefreshOutline /></NIcon></template>
        </NButton>
        <NButton v-if="canAdd" type="primary" @click="handleOpenAdd">
          <template #icon><NIcon><AddOutline /></NIcon></template>
          新增常模集
        </NButton>
      </NSpace>
    </template>

    <NGrid :cols="4" :x-gap="12" :y-gap="8" class="mb-4">
      <NGridItem>
        <NInput v-model:value="searchFormId" placeholder="测评ID" clearable />
      </NGridItem>
      <NGridItem>
        <NInput v-model:value="searchName" placeholder="常模集名称" clearable @keyup.enter="handleSearch" />
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

  <NModal
    v-model:show="modalVisible"
    :title="isEditMode ? '编辑常模集' : '新增常模集'"
    preset="card"
    style="width: 560px"
    :mask-closable="false"
  >
    <NForm ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="100">
      <NFormItem label="测评ID" path="formId">
        <NInput v-model:value="formModel.formId" placeholder="所属测评ID" :disabled="isEditMode" />
      </NFormItem>
      <NFormItem label="名称" path="normSetName">
        <NInput v-model:value="formModel.normSetName" placeholder="常模集名称" />
      </NFormItem>
      <NFormItem label="编码">
        <NInput v-model:value="formModel.normSetCode" placeholder="可选唯一编码" />
      </NFormItem>
      <NFormItem label="样本量">
        <NInputNumber v-model:value="formModel.sampleSize" placeholder="样本数量" :min="0" />
      </NFormItem>
      <NFormItem label="采集开始">
        <NInput v-model:value="formModel.collectionStart" placeholder="YYYY-MM-DD" />
      </NFormItem>
      <NFormItem label="采集结束">
        <NInput v-model:value="formModel.collectionEnd" placeholder="YYYY-MM-DD" />
      </NFormItem>
      <NFormItem label="数据来源">
        <NInput v-model:value="formModel.sourceDesc" type="textarea" :rows="2" placeholder="数据来源说明" />
      </NFormItem>
      <NFormItem label="适用范围">
        <NInput v-model:value="formModel.applicabilityDesc" type="textarea" :rows="2" placeholder="如：适用于 18-60 岁成人" />
      </NFormItem>
      <NFormItem label="状态">
        <NSelect v-model:value="formModel.status" :options="statusOptions" />
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
