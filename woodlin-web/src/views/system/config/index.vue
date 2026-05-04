<!--
  @file views/system/config/index.vue
  @description 系统参数管理：列表 + 搜索 + 新增/编辑抽屉
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
  createConfig,
  deleteConfig,
  pageConfigs,
  updateConfig,
  type ConfigQuery,
  type SysConfig,
} from '@/api/system/config'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<SysConfig[]> = ref([])
const loading = ref(false)
const total = ref(0)
const query = reactive<ConfigQuery>({
  page: 1,
  size: 10,
  configName: '',
  configKey: '',
  configType: undefined,
})

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): SysConfig {
  return {
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: '',
  }
}

const formData = reactive<SysConfig>(defaultForm())

const rules: FormRules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
}

const typeOptions: SelectOption[] = [
  { label: '系统内置', value: 'Y' },
  { label: '自定义', value: 'N' },
]

/** 拉取列表 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageConfigs(query)
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
  query.configName = ''
  query.configKey = ''
  query.configType = undefined
  query.page = 1
  void refresh()
}

/** 新增 */
function openAdd(): void {
  isEdit.value = false
  drawerTitle.value = '新增参数'
  Object.assign(formData, defaultForm())
  drawerVisible.value = true
}

/** 编辑 */
function openEdit(row: SysConfig): void {
  isEdit.value = true
  drawerTitle.value = '编辑参数'
  Object.assign(formData, defaultForm(), row)
  drawerVisible.value = true
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateConfig(formData.id, formData)
      message.success('更新成功')
    } else {
      await createConfig(formData)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除 */
function handleDelete(row: SysConfig): void {
  if (!row.id) {return}
  if (row.configType === 'Y') {
    message.warning('系统内置参数不可删除')
    return
  }
  dialog.warning({
    title: '提示',
    content: `确认删除参数 ${row.configName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteConfig(row.id as number)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<SysConfig> = [
  { title: '参数名称', key: 'configName', width: 180 },
  { title: '参数键名', key: 'configKey', width: 220 },
  { title: '参数值', key: 'configValue' },
  {
    title: '类型',
    key: 'configType',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { type: row.configType === 'Y' ? 'warning' : 'default', size: 'small' },
        { default: () => (row.configType === 'Y' ? '系统内置' : '自定义') },
      ),
  },
  { title: '备注', key: 'remark' },
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
          { size: 'small', text: true, type: 'primary', onClick: () => openEdit(row) },
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
                {
                  size: 'small',
                  text: true,
                  type: 'error',
                  disabled: row.configType === 'Y',
                },
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
  <div class="page-config">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="参数名称">
          <n-input v-model:value="query.configName" placeholder="参数名称" clearable />
        </n-form-item>
        <n-form-item label="参数键名">
          <n-input v-model:value="query.configKey" placeholder="参数键名" clearable />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="query.configType"
            :options="typeOptions"
            placeholder="类型"
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
        <n-button v-permission="'system:config:add'" type="primary" @click="openAdd">
          新增
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: SysConfig) => row.id as number"
        :scroll-x="1100"
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
          <n-form-item label="参数名称" path="configName">
            <n-input v-model:value="formData.configName" />
          </n-form-item>
          <n-form-item label="参数键名" path="configKey">
            <n-input v-model:value="formData.configKey" :disabled="formData.configType === 'Y' && isEdit" />
          </n-form-item>
          <n-form-item label="参数值" path="configValue">
            <n-input v-model:value="formData.configValue" type="textarea" :autosize="{ minRows: 2 }" />
          </n-form-item>
          <n-form-item label="类型" path="configType">
            <n-select
              v-model:value="formData.configType"
              :options="typeOptions"
              :disabled="isEdit"
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
.page-config {
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
