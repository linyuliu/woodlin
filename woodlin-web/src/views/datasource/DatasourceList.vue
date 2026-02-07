<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
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
  NSpace,
  NStatistic,
  NTag,
  NText,
  NSelect,
  useMessage,
  type DataTableColumns,
  type FormInst
} from 'naive-ui'
import {
  AddOutline,
  CheckmarkCircleOutline,
  CreateOutline,
  LinkOutline,
  OpenOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  addDatasource,
  deleteDatasource,
  getDatasourceList,
  testDatasource,
  updateDatasource,
  type DatasourceConfig
} from '@/api/datasource'

const router = useRouter()
const message = useMessage()

const DEFAULT_PASSWORD = '12345678'

const listLoading = ref(false)
const submitLoading = ref(false)
const keyword = ref('')
const datasourceList = ref<DatasourceConfig[]>([])

const modalVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = ref<DatasourceConfig>({
  datasourceCode: '',
  datasourceName: '',
  datasourceType: 'MYSQL',
  jdbcUrl: '',
  username: '',
  password: DEFAULT_PASSWORD,
  driverClass: '',
  testSql: '',
  status: 1,
  owner: '',
  bizTags: '',
  remark: '',
  extConfig: ''
})

const datasourceTypeOptions = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'PostgreSQL', value: 'POSTGRESQL' },
  { label: 'Oracle', value: 'ORACLE' },
  { label: 'SQL Server', value: 'SQLSERVER' },
  { label: 'TiDB', value: 'TIDB' },
  { label: 'OceanBase', value: 'OCEANBASE' },
  { label: 'DM (达梦)', value: 'DM' },
  { label: 'Kingbase (人大金仓)', value: 'KINGBASE' },
  { label: 'openGauss', value: 'OPENGAUSS' },
  { label: 'GaussDB', value: 'GAUSSDB' },
  { label: 'UXDB', value: 'UXDB' },
  { label: 'Vastbase', value: 'VASTBASE' },
  { label: 'Doris', value: 'DORIS' },
  { label: 'StarRocks', value: 'STARROCKS' },
  { label: 'ClickHouse', value: 'CLICKHOUSE' }
]

const formRules = {
  datasourceCode: { required: true, message: '请输入数据源编码', trigger: 'blur' },
  datasourceName: { required: true, message: '请输入数据源名称', trigger: 'blur' },
  datasourceType: { required: true, message: '请选择数据库类型', trigger: 'change' },
  jdbcUrl: { required: true, message: '请输入 JDBC URL', trigger: 'blur' },
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const filteredDatasourceList = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) {
    return datasourceList.value
  }

  return datasourceList.value.filter(item => {
    const content = [
      item.datasourceCode,
      item.datasourceName,
      item.datasourceType,
      item.jdbcUrl,
      item.owner,
      item.bizTags
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return content.includes(key)
  })
})

const enabledCount = computed(() => datasourceList.value.filter(item => item.status === 1).length)
const typeCount = computed(() => new Set(datasourceList.value.map(item => item.datasourceType)).size)

const formatTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

const datasourceColumns: DataTableColumns<DatasourceConfig> = [
  {
    title: '名称',
    key: 'datasourceName',
    width: 170,
    render: row =>
      h(NSpace, { vertical: true, size: 2 }, () => [
        h(NText, { strong: true }, { default: () => row.datasourceName }),
        h(NText, { depth: 3 }, { default: () => row.datasourceCode })
      ])
  },
  {
    title: '类型',
    key: 'datasourceType',
    width: 130,
    render: row => h(NTag, { size: 'small', type: 'info' }, { default: () => row.datasourceType })
  },
  {
    title: '连接地址',
    key: 'jdbcUrl',
    ellipsis: { tooltip: true }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'warning', size: 'small' },
        { default: () => (row.status === 1 ? '启用' : '禁用') }
      )
  },
  {
    title: '最近更新',
    key: 'updateTime',
    width: 170,
    render: row => formatTime(row.updateTime || row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 360,
    render: row =>
      h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          {
            size: 'small',
            tertiary: true,
            type: 'primary',
            onClick: () => handleOpenWorkspace(row)
          },
          { default: () => '进入工作台', icon: () => h(NIcon, null, { default: () => h(OpenOutline) }) }
        ),
        h(
          NButton,
          {
            size: 'small',
            tertiary: true,
            type: 'default',
            onClick: () => handleOpenEdit(row)
          },
          { default: () => '编辑', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }
        ),
        h(
          NButton,
          {
            size: 'small',
            tertiary: true,
            type: 'success',
            onClick: () => handleTestDatasource(row)
          },
          { default: () => '测试', icon: () => h(NIcon, null, { default: () => h(LinkOutline) }) }
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteDatasource(row) },
          {
            default: () => `确认删除数据源 ${row.datasourceName} 吗？`,
            trigger: () =>
              h(
                NButton,
                { size: 'small', tertiary: true, type: 'error' },
                { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }
              )
          }
        )
      ])
  }
]

const resetForm = () => {
  formModel.value = {
    datasourceCode: '',
    datasourceName: '',
    datasourceType: 'MYSQL',
    jdbcUrl: '',
    username: '',
    password: DEFAULT_PASSWORD,
    driverClass: '',
    testSql: '',
    status: 1,
    owner: '',
    bizTags: '',
    remark: '',
    extConfig: ''
  }
}

const loadDatasourceList = async () => {
  listLoading.value = true
  try {
    datasourceList.value = await getDatasourceList()
  } finally {
    listLoading.value = false
  }
}

const handleOpenWorkspace = (row: DatasourceConfig) => {
  const routeLocation = router.resolve({
    name: 'DatasourceWorkspace',
    params: { code: row.datasourceCode }
  })
  if (!routeLocation.name) {
    router.push(`/datasource/workspace/${row.datasourceCode}`)
    return
  }
  router.push(routeLocation)
}

const handleOpenCreate = () => {
  isEditMode.value = false
  resetForm()
  modalVisible.value = true
}

const handleOpenEdit = (row: DatasourceConfig) => {
  isEditMode.value = true
  formModel.value = { ...row }
  modalVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEditMode.value) {
      await updateDatasource(formModel.value)
      message.success('数据源更新成功')
    } else {
      await addDatasource(formModel.value)
      message.success('数据源创建成功')
    }
    modalVisible.value = false
    await loadDatasourceList()
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteDatasource = async (row: DatasourceConfig) => {
  await deleteDatasource(row.datasourceCode)
  message.success('数据源已删除')
  await loadDatasourceList()
}

const handleTestDatasource = async (row: DatasourceConfig) => {
  await testDatasource(row)
  message.success(`连接测试通过：${row.datasourceName}`)
}

onMounted(async () => {
  await loadDatasourceList()
})
</script>

<template>
  <div class="datasource-list-page">
    <n-card class="hero-card" :bordered="false">
      <div class="hero-content">
        <div>
          <h2>数据源管理</h2>
          <p>列表页只做管理和检索。Schema、表、字段在独立工作台按层级懒加载，避免一次性全量读取。</p>
        </div>
        <n-space>
          <n-button type="primary" @click="handleOpenCreate">
            <template #icon>
              <n-icon><add-outline /></n-icon>
            </template>
            新增数据源
          </n-button>
          <n-button secondary @click="loadDatasourceList">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新列表
          </n-button>
        </n-space>
      </div>
    </n-card>

    <n-grid :x-gap="12" :y-gap="12" cols="1 s:2 m:3" responsive="screen">
      <n-grid-item>
        <n-card class="stat-card" :bordered="false">
          <n-statistic label="数据源总数" :value="datasourceList.length" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card" :bordered="false">
          <n-statistic label="启用中" :value="enabledCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card" :bordered="false">
          <n-statistic label="数据库类型" :value="typeCount" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="数据源列表" :bordered="false" class="panel-card">
      <template #header-extra>
        <n-space align="center" size="small">
          <n-input v-model:value="keyword" clearable placeholder="搜索名称/编码/类型/标签" size="small" style="width: 280px">
            <template #prefix>
              <n-icon><search-outline /></n-icon>
            </template>
          </n-input>
          <n-text depth="3">双击行可快速进入工作台</n-text>
        </n-space>
      </template>

      <n-data-table
        :columns="datasourceColumns"
        :data="filteredDatasourceList"
        :loading="listLoading"
        :pagination="{ pageSize: 8 }"
        size="small"
        max-height="640"
        :row-key="(row: DatasourceConfig) => row.datasourceCode"
        :row-props="(row: DatasourceConfig) => ({ onDblclick: () => handleOpenWorkspace(row) })"
      />
    </n-card>

    <n-modal
      v-model:show="modalVisible"
      preset="card"
      :title="isEditMode ? '编辑数据源' : '新增数据源'"
      style="width: 720px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <n-form ref="formRef" :model="formModel" :rules="formRules" label-placement="left" label-width="100">
        <n-grid cols="1 m:2" responsive="screen" :x-gap="12">
          <n-grid-item>
            <n-form-item label="数据源编码" path="datasourceCode">
              <n-input v-model:value="formModel.datasourceCode" :disabled="isEditMode" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="数据源名称" path="datasourceName">
              <n-input v-model:value="formModel.datasourceName" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="数据库类型" path="datasourceType">
              <n-select v-model:value="formModel.datasourceType" :options="datasourceTypeOptions" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="状态">
              <n-select
                v-model:value="formModel.status"
                :options="[
                  { label: '启用', value: 1 },
                  { label: '禁用', value: 0 }
                ]"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <n-form-item label="JDBC URL" path="jdbcUrl">
          <n-input v-model:value="formModel.jdbcUrl" />
        </n-form-item>
        <n-form-item label="驱动类">
          <n-input v-model:value="formModel.driverClass" placeholder="留空可按 URL 自动推断" />
        </n-form-item>

        <n-grid cols="1 m:2" responsive="screen" :x-gap="12">
          <n-grid-item>
            <n-form-item label="用户名" path="username">
              <n-input v-model:value="formModel.username" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="密码" path="password">
              <n-input
                v-model:value="formModel.password"
                type="password"
                show-password-on="click"
                placeholder="默认 12345678"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <n-grid cols="1 m:2" responsive="screen" :x-gap="12">
          <n-grid-item>
            <n-form-item label="负责人">
              <n-input v-model:value="formModel.owner" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="业务标签">
              <n-input v-model:value="formModel.bizTags" placeholder="例如：etl,cdc,report" />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <n-form-item label="测试 SQL">
          <n-input v-model:value="formModel.testSql" placeholder="留空则按数据库类型自动设置" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="formModel.remark" type="textarea" />
        </n-form-item>
        <n-form-item label="扩展配置">
          <n-input
            v-model:value="formModel.extConfig"
            type="textarea"
            placeholder='JSON，例如 {"metadata":{"preferNative":true}}'
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
            <template #icon>
              <n-icon><checkmark-circle-outline /></n-icon>
            </template>
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.datasource-list-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #0a4d68 0%, #088395 52%, #05bfdb 100%);
  color: var(--text-color-inverse);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hero-content h2 {
  margin: 0 0 6px;
  color: var(--text-color-inverse);
  font-size: 22px;
}

.hero-content p {
  margin: 0;
  color: color-mix(in srgb, var(--text-color-inverse) 82%, transparent);
}

.stat-card {
  background: radial-gradient(circle at top right, rgba(5, 191, 219, 0.24), transparent 48%), var(--bg-color);
}

.panel-card {
  min-height: 360px;
}

@media (max-width: 960px) {
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
