<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NGrid,
  NGridItem,
  NDataTable,
  NDivider,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NSpin,
  NTabPane,
  NTabs,
  NTag,
  NTree,
  NStatistic,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import {
  clearDictCache,
  createDictDataAdmin,
  createDictTypeAdmin,
  deleteDictDataAdmin,
  deleteDictTypeAdmin,
  getRegionTree,
  listDictDataAdmin,
  listDictTypesAdmin,
  updateDictDataAdmin,
  updateDictTypeAdmin,
  type DictDataRecord,
  type DictTypeRecord,
  type RegionNode
} from '@/api/dict'

const message = useMessage()
const dialog = useDialog()

const typeLoading = ref(false)
const dataLoading = ref(false)
const submitTypeLoading = ref(false)
const submitDataLoading = ref(false)
const regionLoading = ref(false)
const activeTab = ref('dict')
const regionLoaded = ref(false)

const dictTypes = ref<DictTypeRecord[]>([])
const dictData = ref<DictDataRecord[]>([])
const selectedType = ref<DictTypeRecord | null>(null)

const regionTree = ref<RegionNode[]>([])
const regionTreeData = ref<any[]>([])

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]
const categoryOptions = [
  { label: '系统', value: 'system' },
  { label: '业务', value: 'business' },
  { label: '自定义', value: 'custom' }
]

const typeModalVisible = ref(false)
const typeFormRef = ref<FormInst | null>(null)
const typeForm = reactive<DictTypeRecord>({
  dictName: '',
  dictType: '',
  dictCategory: 'system',
  status: '1',
  remark: ''
})
const typeRules = {
  dictName: { required: true, message: '请输入字典名称', trigger: 'blur' },
  dictType: { required: true, message: '请输入字典类型', trigger: 'blur' }
}
const isEditType = computed(() => !!typeForm.dictId)

const dataModalVisible = ref(false)
const dataFormRef = ref<FormInst | null>(null)
const dataForm = reactive<DictDataRecord>({
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: '1',
  isDefault: '0',
  cssClass: '',
  listClass: '',
  extraData: ''
})
const dataRules = {
  dictLabel: { required: true, message: '请输入字典标签', trigger: 'blur' },
  dictValue: { required: true, message: '请输入字典值', trigger: 'blur' }
}
const isEditData = computed(() => !!dataForm.dataId)
const dictTypeCount = computed(() => dictTypes.value.length)
const enabledTypeCount = computed(() => dictTypes.value.filter(item => item.status === '1').length)
const dictDataCount = computed(() => dictData.value.length)
const typePagination = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [20, 50, 100]
})
const dataPagination = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [20, 50, 100]
})

const dictTypeColumns: DataTableColumns<DictTypeRecord> = [
  { title: '名称', key: 'dictName', width: 180 },
  { title: '标识', key: 'dictType', width: 180 },
  { title: '分类', key: 'dictCategory', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: row.status === '1' ? 'success' : 'warning', size: 'small' }, { default: () => (row.status === '1' ? '启用' : '禁用') })
  },
  { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: row =>
      h(NSpace, { size: 6 }, () => [
        h(
          NButton,
          { size: 'small', text: true, onClick: () => openEditType(row) },
          { default: () => '编辑' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDeleteType(row)
          },
          {
            default: () => '确定删除该字典类型及其字典项？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', text: true, type: 'error' },
                { default: () => '删除' }
              )
          }
        )
      ])
  }
]

const dictDataColumns: DataTableColumns<DictDataRecord> = [
  { title: '标签', key: 'dictLabel', width: 160 },
  { title: '值', key: 'dictValue', width: 140 },
  { title: '描述', key: 'dictDesc', ellipsis: { tooltip: true } },
  { title: '排序', key: 'dictSort', width: 80 },
  {
    title: '默认',
    key: 'isDefault',
    width: 80,
    render: row => h(NTag, { type: row.isDefault === '1' ? 'success' : 'default', size: 'small' }, { default: () => (row.isDefault === '1' ? '是' : '否') })
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row => h(NTag, { type: row.status === '1' ? 'success' : 'warning', size: 'small' }, { default: () => (row.status === '1' ? '启用' : '禁用') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: row =>
      h(NSpace, { size: 6 }, () => [
        h(
          NButton,
          { size: 'small', text: true, onClick: () => openEditData(row) },
          { default: () => '编辑' }
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteData(row) },
          {
            default: () => '确定删除该字典项？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', text: true, type: 'error' },
                { default: () => '删除' }
              )
          }
        )
      ])
  }
]

const convertToTreeData = (nodes: RegionNode[]): any[] =>
  nodes.map(node => ({
    key: node.code,
    label: `${node.name} (${node.code})`,
    children: node.children ? convertToTreeData(node.children) : undefined
  }))

const loadDictTypes = async () => {
  typeLoading.value = true
  try {
    const list = await listDictTypesAdmin()
    dictTypes.value = list

    if (selectedType.value) {
      const fresh = list.find(item => item.dictType === selectedType.value?.dictType)
      selectedType.value = fresh || null
    }

    if (!selectedType.value && list.length > 0) {
      selectedType.value = list[0]
      dataPagination.page = 1
      await loadDictData(list[0].dictType)
    } else if (selectedType.value) {
      await loadDictData(selectedType.value.dictType)
    }
  } catch (error) {
    console.error(error)
    message.error('加载字典类型失败')
  } finally {
    typeLoading.value = false
  }
}

const loadDictData = async (dictType: string) => {
  if (!dictType) {
    return
  }
  dataLoading.value = true
  try {
    dictData.value = await listDictDataAdmin(dictType)
  } catch (error) {
    console.error(error)
    message.error('加载字典项失败')
  } finally {
    dataLoading.value = false
  }
}

const loadRegionTree = async (force = false) => {
  if (regionLoaded.value && !force) {
    return
  }
  regionLoading.value = true
  try {
    regionTree.value = await getRegionTree(true)
    regionTreeData.value = convertToTreeData(regionTree.value)
    regionLoaded.value = true
  } catch (error) {
    console.error(error)
    message.error('加载行政区划树失败')
    regionLoaded.value = false
  } finally {
    regionLoading.value = false
  }
}

const handleClearCache = () => {
  clearDictCache()
  message.success('已清空前端字典缓存')
}

const handleSelectType = (row: DictTypeRecord) => {
  selectedType.value = row
  dataPagination.page = 1
  loadDictData(row.dictType)
}

const handleTabChange = (value: string) => {
  if (value === 'region') {
    loadRegionTree()
  }
}

const openCreateType = () => {
  Object.assign(typeForm, {
    dictId: undefined,
    dictName: '',
    dictType: '',
    dictCategory: 'system',
    status: '1',
    remark: ''
  })
  typeModalVisible.value = true
}

const openEditType = (row: DictTypeRecord) => {
  Object.assign(typeForm, row)
  typeModalVisible.value = true
}

const submitType = async () => {
  await typeFormRef.value?.validate()
  submitTypeLoading.value = true
  try {
    if (isEditType.value) {
      await updateDictTypeAdmin({ ...typeForm })
      message.success('更新成功')
    } else {
      await createDictTypeAdmin({ ...typeForm })
      message.success('新增成功')
    }
    typeModalVisible.value = false
    await loadDictTypes()
  } catch (error) {
    console.error(error)
    message.error('保存字典类型失败')
  } finally {
    submitTypeLoading.value = false
  }
}

const handleDeleteType = (row: DictTypeRecord) => {
  dialog.warning({
    title: '删除字典类型',
    content: '删除后字典项也会被标记删除，确认继续？',
    positiveText: '删除',
    negativeText: '取消',
    async onPositiveClick() {
      try {
        await deleteDictTypeAdmin(row.dictId!)
        message.success('删除成功')
        if (selectedType.value?.dictType === row.dictType) {
          selectedType.value = null
          dictData.value = []
        }
        await loadDictTypes()
      } catch (error) {
        console.error(error)
        message.error('删除失败')
      }
    }
  })
}

const openCreateData = () => {
  if (!selectedType.value) {
    message.warning('请先选择字典类型')
    return
  }
  Object.assign(dataForm, {
    dataId: undefined,
    dictType: selectedType.value.dictType,
    dictLabel: '',
    dictValue: '',
    dictDesc: '',
    dictSort: 0,
    status: '1',
    isDefault: '0',
    cssClass: '',
    listClass: '',
    extraData: ''
  })
  dataModalVisible.value = true
}

const openEditData = (row: DictDataRecord) => {
  Object.assign(dataForm, row)
  dataModalVisible.value = true
}

const submitData = async () => {
  if (!selectedType.value) {
    message.warning('请先选择字典类型')
    return
  }
  await dataFormRef.value?.validate()
  submitDataLoading.value = true
  try {
    const payload = { ...dataForm, dictType: selectedType.value.dictType }
    if (isEditData.value) {
      await updateDictDataAdmin(payload)
      message.success('更新成功')
    } else {
      await createDictDataAdmin(payload)
      message.success('新增成功')
    }
    dataModalVisible.value = false
    await loadDictData(selectedType.value.dictType)
  } catch (error) {
    console.error(error)
    message.error('保存字典项失败')
  } finally {
    submitDataLoading.value = false
  }
}

const handleDeleteData = (row: DictDataRecord) => {
  dialog.warning({
    title: '删除字典项',
    content: `确定删除「${row.dictLabel}」吗？`,
    positiveText: '删除',
    negativeText: '取消',
    async onPositiveClick() {
      try {
        await deleteDictDataAdmin(row.dataId!)
        message.success('删除成功')
        if (selectedType.value) {
          await loadDictData(selectedType.value.dictType)
        }
      } catch (error) {
        console.error(error)
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  loadDictTypes()
})
</script>

<template>
  <div class="dict-view">
    <NSpace vertical :size="16">
      <NCard :bordered="false" class="hero-card">
        <div class="hero-content">
          <div>
            <h2>字典与区划管理</h2>
            <p>统一维护字典类型、字典项和行政区划，保障配置语义一致与前后端回显标准化。</p>
          </div>
          <NTag type="info" size="small">字典标准中心</NTag>
        </div>
      </NCard>

      <NGrid :x-gap="12" :y-gap="12" cols="1 s:3" responsive="screen">
        <NGridItem>
          <NCard :bordered="false" class="stat-card">
            <NStatistic label="字典类型" :value="dictTypeCount" />
          </NCard>
        </NGridItem>
        <NGridItem>
          <NCard :bordered="false" class="stat-card">
            <NStatistic label="启用类型" :value="enabledTypeCount" />
          </NCard>
        </NGridItem>
        <NGridItem>
          <NCard :bordered="false" class="stat-card">
            <NStatistic label="当前字典项" :value="dictDataCount" />
          </NCard>
        </NGridItem>
      </NGrid>

      <NCard :bordered="false">
        <NSpace wrap>
          <NButton type="primary" @click="openCreateType">
            新增字典类型
          </NButton>
          <NButton @click="openCreateData" :disabled="!selectedType">
            新增字典项
          </NButton>
          <NButton :loading="typeLoading" @click="loadDictTypes">
            刷新类型
          </NButton>
          <NButton type="warning" @click="handleClearCache">
            清空前端缓存
          </NButton>
          <NTag type="info">
            选择左侧类型后即可管理对应字典项
          </NTag>
        </NSpace>
      </NCard>

      <NTabs v-model:value="activeTab" type="line" animated @update:value="handleTabChange">
        <NTabPane name="dict" tab="字典数据">
          <NSpace vertical :size="16">
            <NCard title="字典类型" :bordered="false">
              <NDataTable
                :columns="dictTypeColumns"
                :data="dictTypes"
                :loading="typeLoading"
                :bordered="false"
                :single-line="false"
                :pagination="typePagination"
                :max-height="520"
                :virtual-scroll="true"
                size="small"
                :row-key="(row: DictTypeRecord) => row.dictId || row.dictType"
                :row-class-name="(row: DictTypeRecord) => (row.dictType === selectedType?.dictType ? 'active-row' : '')"
                :row-props="(row: DictTypeRecord) => ({
                  style: 'cursor: pointer;',
                  onClick: () => handleSelectType(row)
                })"
              />
            </NCard>

            <NCard v-if="selectedType" :title="`字典项 - ${selectedType.dictName}`" :bordered="false">
              <template #header-extra>
                <NSpace>
                  <NButton size="small" type="primary" @click="openCreateData">新增字典项</NButton>
                  <NButton size="small" :loading="dataLoading" @click="loadDictData(selectedType.dictType)">刷新</NButton>
                </NSpace>
              </template>
              <NDataTable
                :columns="dictDataColumns"
                :data="dictData"
                :loading="dataLoading"
                :bordered="false"
                :single-line="false"
                :pagination="dataPagination"
                :max-height="520"
                :virtual-scroll="true"
                size="small"
                :row-key="(row: DictDataRecord) => row.dataId || `${row.dictType}_${row.dictValue}`"
              />
            </NCard>
            <NCard v-else :bordered="false">
              <div class="empty-hint">请先选择字典类型</div>
            </NCard>
          </NSpace>
        </NTabPane>

        <NTabPane name="region" tab="行政区划">
          <NCard title="行政区划树" :bordered="false">
            <template #header-extra>
              <NButton size="small" :loading="regionLoading" @click="loadRegionTree(true)">
                重新加载
              </NButton>
            </template>
            <NSpin :show="regionLoading">
              <NTree
                v-if="regionTreeData.length > 0"
                :data="regionTreeData"
                block-line
                expand-on-click
                selectable
              />
              <div v-else class="empty-hint">
                暂无区划数据
              </div>
            </NSpin>
          </NCard>
        </NTabPane>
      </NTabs>
    </NSpace>

    <NModal
      v-model:show="typeModalVisible"
      preset="card"
      :title="isEditType ? '编辑字典类型' : '新增字典类型'"
      style="width: 520px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90" label-placement="left">
        <NFormItem label="字典名称" path="dictName">
          <NInput v-model:value="typeForm.dictName" placeholder="例如：性别" />
        </NFormItem>
        <NFormItem label="字典类型" path="dictType">
          <NInput v-model:value="typeForm.dictType" :disabled="isEditType" placeholder="例如：gender" />
        </NFormItem>
        <NFormItem label="分类" path="dictCategory">
          <NSelect v-model:value="typeForm.dictCategory" :options="categoryOptions" placeholder="请选择分类" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSelect v-model:value="typeForm.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="备注" path="remark">
          <NInput v-model:value="typeForm.remark" type="textarea" placeholder="可填写标准或用途说明" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="typeModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="submitTypeLoading" @click="submitType">保存</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal
      v-model:show="dataModalVisible"
      preset="card"
      :title="isEditData ? '编辑字典项' : '新增字典项'"
      style="width: 520px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90" label-placement="left">
        <NFormItem label="所属类型">
          <NInput v-model:value="dataForm.dictType" disabled />
        </NFormItem>
        <NFormItem label="标签" path="dictLabel">
          <NInput v-model:value="dataForm.dictLabel" placeholder="例如：男" />
        </NFormItem>
        <NFormItem label="值" path="dictValue">
          <NInput v-model:value="dataForm.dictValue" placeholder="例如：1" />
        </NFormItem>
        <NFormItem label="排序" path="dictSort">
          <NInputNumber v-model:value="dataForm.dictSort" :min="0" />
        </NFormItem>
        <NFormItem label="是否默认">
          <NSelect v-model:value="dataForm.isDefault" :options="[{ label: '是', value: '1' }, { label: '否', value: '0' }]" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="dataForm.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="dataForm.dictDesc" type="textarea" placeholder="补充说明" />
        </NFormItem>
        <NDivider title-placement="left">样式/扩展</NDivider>
        <NFormItem label="标签类">
          <NInput v-model:value="dataForm.cssClass" placeholder="自定义标签样式类名" />
        </NFormItem>
        <NFormItem label="列表类">
          <NInput v-model:value="dataForm.listClass" placeholder="表格回显样式类名" />
        </NFormItem>
        <NFormItem label="扩展">
          <NInput v-model:value="dataForm.extraData" type="textarea" placeholder="可写入JSON或额外描述" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="dataModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="submitDataLoading" @click="submitData">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.dict-view {
  padding: 8px 0;
}

.hero-card {
  background: linear-gradient(120deg, #0a4d68 0%, #088395 52%, #05bfdb 100%);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-color-inverse);
}

.hero-content h2 {
  margin: 0 0 6px;
  color: var(--text-color-inverse);
}

.hero-content p {
  margin: 0;
  color: color-mix(in srgb, var(--text-color-inverse) 82%, transparent);
}

.stat-card {
  background: radial-gradient(circle at top right, rgba(5, 191, 219, 0.24), transparent 48%), var(--bg-color);
}

.empty-hint {
  padding: 20px;
  color: var(--text-color-tertiary);
  text-align: center;
}

:deep(.active-row .n-data-table-td) {
  background: var(--primary-color-light) !important;
}
</style>
