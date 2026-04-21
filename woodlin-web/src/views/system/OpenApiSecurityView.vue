<script lang="ts" setup>
import {computed, h, onMounted, reactive, ref} from 'vue'
import {
  type DataTableColumns,
  NAlert,
  NButton,
  NCard,
  NCode,
  NDataTable,
  NDivider,
  NEmpty,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NStatistic,
  NSwitch,
  NTabPane,
  NTabs,
  NTag,
  useMessage
} from 'naive-ui'
import {
  createOpenApiPolicy,
  createOpenApp,
  deleteOpenApiPolicies,
  deleteOpenApps,
  getOpenApiOverview,
  getOpenApiSettings,
  issueOpenAppCredential,
  listOpenApiPolicies,
  listOpenAppCredentials,
  listOpenApps,
  type OpenApiCredentialIssueResponse,
  type OpenApiCredentialRequest,
  type OpenApiCredentialView,
  type OpenApiGlobalSettings,
  type OpenApiOverview,
  revokeOpenAppCredential,
  rotateOpenAppCredential,
  type SysOpenApiPolicy,
  type SysOpenApp,
  updateOpenApiPolicy,
  updateOpenApiSettings,
  updateOpenApp
} from '@/api/openApiSecurity'
import {type DictItem, getDictData} from '@/api/dict'
import {logger} from '@/utils/logger'
import {
  normalizeOpenApiDictOptions,
  OPEN_API_DEFAULTS,
  OPEN_API_ENCRYPTION_ALGORITHM_OPTIONS,
  OPEN_API_HTTP_METHOD_OPTIONS,
  OPEN_API_SECURITY_MODE_OPTIONS,
  OPEN_API_SIGNATURE_ALGORITHM_OPTIONS
} from '@/utils/openapi-security'

const message = useMessage()
const loading = ref(false)
const activeTab = ref('settings')
const keyword = ref('')
const policyKeyword = ref('')

const overview = ref<OpenApiOverview>({
  appCount: 0,
  credentialCount: 0,
  policyCount: 0
})

const settingsForm = reactive<OpenApiGlobalSettings>({
  defaultMode: OPEN_API_DEFAULTS.securityMode,
  defaultSignatureAlgorithm: OPEN_API_DEFAULTS.signatureAlgorithm,
  timestampWindowSeconds: 300,
  nonceEnabled: true,
  nonceTtlSeconds: 300,
  defaultEncryptionAlgorithm: OPEN_API_DEFAULTS.encryptionAlgorithm,
  encryptionRequired: false,
  gmEnabled: true
})

const apps = ref<SysOpenApp[]>([])
const selectedAppId = ref<number | null>(null)
const credentials = ref<OpenApiCredentialView[]>([])
const policies = ref<SysOpenApiPolicy[]>([])

const securityModeOptions = ref<DictItem[]>([])
const signatureAlgorithmOptions = ref<DictItem[]>([])
const encryptionAlgorithmOptions = ref<DictItem[]>([])

const statusOptions = [
  {label: '启用', value: '1'},
  {label: '停用', value: '0'}
]
const booleanOptions = [
  {label: '是', value: '1'},
  {label: '否', value: '0'}
]
const httpMethodOptions = OPEN_API_HTTP_METHOD_OPTIONS

const showAppModal = ref(false)
const showCredentialModal = ref(false)
const showPolicyModal = ref(false)
const showIssuedSecretModal = ref(false)

const appForm = reactive<SysOpenApp>({
  appCode: '',
  appName: '',
  status: '1',
  tenantId: '',
  ownerName: '',
  ipWhitelist: '',
  remark: ''
})

const credentialForm = reactive<OpenApiCredentialRequest>({
  credentialName: '',
  securityMode: OPEN_API_DEFAULTS.securityMode,
  signatureAlgorithm: OPEN_API_DEFAULTS.signatureAlgorithm,
  encryptionAlgorithm: OPEN_API_DEFAULTS.encryptionAlgorithm,
  activeFrom: '',
  activeTo: '',
  remark: ''
})

const policyForm = reactive<SysOpenApiPolicy>({
  policyName: '',
  pathPattern: '/openapi/**',
  httpMethod: '*',
  securityMode: OPEN_API_DEFAULTS.securityMode,
  signatureAlgorithm: OPEN_API_DEFAULTS.signatureAlgorithm,
  encryptionAlgorithm: OPEN_API_DEFAULTS.encryptionAlgorithm,
  timestampWindowSeconds: 300,
  nonceEnabled: '1',
  nonceTtlSeconds: 300,
  tenantRequired: '0',
  enabled: '1',
  remark: ''
})

const issuedSecret = ref<OpenApiCredentialIssueResponse | null>(null)

const isEditingApp = computed(() => !!appForm.appId)
const isEditingPolicy = computed(() => !!policyForm.policyId)
const selectedApp = computed(() => apps.value.find(item => item.appId === selectedAppId.value) || null)
const filteredApps = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) {
    return apps.value
  }
  return apps.value.filter(item =>
    [item.appCode, item.appName, item.ownerName, item.tenantId, item.ipWhitelist, item.remark]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(value)
  )
})
const filteredPolicies = computed(() => {
  const value = policyKeyword.value.trim().toLowerCase()
  if (!value) {
    return policies.value
  }
  return policies.value.filter(item =>
    [item.policyName, item.pathPattern, item.httpMethod, item.securityMode, item.remark]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(value)
  )
})

const appColumns: DataTableColumns<SysOpenApp> = [
  {title: '应用编码', key: 'appCode', width: 180},
  {title: '应用名称', key: 'appName', width: 180},
  {title: '租户', key: 'tenantId', width: 120},
  {title: '负责人', key: 'ownerName', width: 120},
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row => h(NTag, {
      type: row.status === '1' ? 'success' : 'warning',
      size: 'small'
    }, {default: () => (row.status === '1' ? '启用' : '停用')})
  },
  {title: '白名单', key: 'ipWhitelist', ellipsis: {tooltip: true}},
  {
    title: '操作',
    key: 'actions',
    width: 190,
    render: row =>
      h(NSpace, {size: 4}, () => [
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: selectedAppId.value === row.appId ? 'primary' : 'default',
            onClick: () => selectApp(row.appId || 0)
          },
          {default: () => '凭证'}
        ),
        h(
          NButton,
          {size: 'small', text: true, onClick: () => openEditApp(row)},
          {default: () => '编辑'}
        ),
        h(
          NPopconfirm,
          {onPositiveClick: () => handleDeleteApp(row)},
          {
            default: () => `确认删除应用 ${row.appName} 吗？`,
            trigger: () =>
              h(
                NButton,
                {size: 'small', text: true, type: 'error'},
                {default: () => '删除'}
              )
          }
        )
      ])
  }
]

const credentialColumns: DataTableColumns<OpenApiCredentialView> = [
  {title: '凭证名称', key: 'credentialName', width: 180},
  {title: 'AK', key: 'accessKey', width: 220, ellipsis: {tooltip: true}},
  {title: '指纹', key: 'secretKeyFingerprint', width: 120},
  {title: '安全模式', key: 'securityMode', width: 130},
  {title: '签名算法', key: 'signatureAlgorithm', width: 140},
  {title: '加密算法', key: 'encryptionAlgorithm', width: 140},
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row => h(NTag, {
      type: row.status === '1' ? 'success' : 'warning',
      size: 'small'
    }, {default: () => (row.status === '1' ? '启用' : '吊销')})
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: row =>
      h(NSpace, {size: 4}, () => [
        h(
          NButton,
          {size: 'small', text: true, onClick: () => openRotateCredential(row)},
          {default: () => '轮换'}
        ),
        h(
          NPopconfirm,
          {onPositiveClick: () => handleRevokeCredential(row)},
          {
            default: () => `确认吊销凭证 ${row.credentialName} 吗？`,
            trigger: () =>
              h(
                NButton,
                {size: 'small', text: true, type: 'error'},
                {default: () => '吊销'}
              )
          }
        )
      ])
  }
]

const policyColumns: DataTableColumns<SysOpenApiPolicy> = [
  {title: '策略名称', key: 'policyName', width: 180},
  {title: '路径模式', key: 'pathPattern', width: 220},
  {title: '方法', key: 'httpMethod', width: 90},
  {title: '安全模式', key: 'securityMode', width: 130},
  {title: '签名算法', key: 'signatureAlgorithm', width: 140},
  {title: '加密算法', key: 'encryptionAlgorithm', width: 140},
  {title: '时间窗', key: 'timestampWindowSeconds', width: 100},
  {
    title: '启用',
    key: 'enabled',
    width: 90,
    render: row => h(NTag, {
      type: row.enabled === '1' ? 'success' : 'default',
      size: 'small'
    }, {default: () => (row.enabled === '1' ? '启用' : '停用')})
  },
  {
    title: '操作',
    key: 'actions',
    width: 170,
    render: row =>
      h(NSpace, {size: 4}, () => [
        h(
          NButton,
          {size: 'small', text: true, onClick: () => openEditPolicy(row)},
          {default: () => '编辑'}
        ),
        h(
          NPopconfirm,
          {onPositiveClick: () => handleDeletePolicy(row)},
          {
            default: () => `确认删除策略 ${row.policyName} 吗？`,
            trigger: () =>
              h(
                NButton,
                {size: 'small', text: true, type: 'error'},
                {default: () => '删除'}
              )
          }
        )
      ])
  }
]

const normalizeOptions = (items: DictItem[], fallbackOptions: { label: string; value: string }[]) =>
  normalizeOpenApiDictOptions(items, fallbackOptions)

const resetAppForm = () => {
  Object.assign(appForm, {
    appId: undefined,
    appCode: '',
    appName: '',
    status: '1',
    tenantId: '',
    ownerName: '',
    ipWhitelist: '',
    remark: ''
  })
}

const resetCredentialForm = () => {
  Object.assign(credentialForm, {
    credentialName: '',
    securityMode: settingsForm.defaultMode || OPEN_API_DEFAULTS.securityMode,
    signatureAlgorithm: settingsForm.defaultSignatureAlgorithm || OPEN_API_DEFAULTS.signatureAlgorithm,
    encryptionAlgorithm: settingsForm.defaultEncryptionAlgorithm || OPEN_API_DEFAULTS.encryptionAlgorithm,
    activeFrom: '',
    activeTo: '',
    remark: ''
  })
  ;(credentialForm as OpenApiCredentialRequest & { credentialId?: number }).credentialId = undefined
}

const resetPolicyForm = () => {
  Object.assign(policyForm, {
    policyId: undefined,
    policyName: '',
    pathPattern: '/openapi/**',
    httpMethod: '*',
    securityMode: settingsForm.defaultMode || OPEN_API_DEFAULTS.securityMode,
    signatureAlgorithm: settingsForm.defaultSignatureAlgorithm || OPEN_API_DEFAULTS.signatureAlgorithm,
    encryptionAlgorithm: settingsForm.defaultEncryptionAlgorithm || OPEN_API_DEFAULTS.encryptionAlgorithm,
    timestampWindowSeconds: settingsForm.timestampWindowSeconds || 300,
    nonceEnabled: settingsForm.nonceEnabled ? '1' : '0',
    nonceTtlSeconds: settingsForm.nonceTtlSeconds || 300,
    tenantRequired: '0',
    enabled: '1',
    remark: ''
  })
}

const loadDictionaries = async () => {
  const [securityModes, signatureAlgorithms, encryptionAlgorithms] = await Promise.all([
    getDictData('api_security_mode'),
    getDictData('api_signature_algorithm'),
    getDictData('api_encryption_algorithm')
  ])
  securityModeOptions.value = normalizeOptions(securityModes, OPEN_API_SECURITY_MODE_OPTIONS)
  signatureAlgorithmOptions.value = normalizeOptions(signatureAlgorithms, OPEN_API_SIGNATURE_ALGORITHM_OPTIONS)
  encryptionAlgorithmOptions.value = normalizeOptions(encryptionAlgorithms, OPEN_API_ENCRYPTION_ALGORITHM_OPTIONS)
}

const loadOverview = async () => {
  overview.value = await getOpenApiOverview()
}

const loadSettings = async () => {
  Object.assign(settingsForm, await getOpenApiSettings())
}

const loadApps = async () => {
  apps.value = await listOpenApps()
  if (!selectedAppId.value && apps.value.length > 0) {
    selectedAppId.value = apps.value[0].appId || null
  }
  if (selectedAppId.value) {
    await loadCredentials(selectedAppId.value)
  }
}

const loadCredentials = async (appId: number) => {
  credentials.value = await listOpenAppCredentials(appId)
}

const loadPolicies = async () => {
  policies.value = await listOpenApiPolicies()
}

const refreshAll = async () => {
  loading.value = true
  try {
    await Promise.all([loadDictionaries(), loadOverview(), loadSettings(), loadApps(), loadPolicies()])
  } catch (error) {
    logger.error('加载开放API安全中心失败', error)
    message.error('加载开放API安全中心失败')
  } finally {
    loading.value = false
  }
}

const handleSaveSettings = async () => {
  loading.value = true
  try {
    await updateOpenApiSettings({...settingsForm})
    message.success('全局配置已保存')
    await Promise.all([loadSettings(), loadOverview()])
  } catch (error) {
    logger.error('保存开放API设置失败', error)
    message.error('保存开放API设置失败')
  } finally {
    loading.value = false
  }
}

const openCreateApp = () => {
  resetAppForm()
  showAppModal.value = true
}

const openEditApp = (row: SysOpenApp) => {
  Object.assign(appForm, row)
  showAppModal.value = true
}

const handleSaveApp = async () => {
  loading.value = true
  try {
    if (isEditingApp.value) {
      await updateOpenApp({...appForm})
      message.success('应用已更新')
    } else {
      await createOpenApp({...appForm})
      message.success('应用已创建')
    }
    showAppModal.value = false
    await Promise.all([loadApps(), loadOverview()])
  } catch (error) {
    logger.error('保存开放应用失败', error)
    message.error('保存开放应用失败')
  } finally {
    loading.value = false
  }
}

const handleDeleteApp = async (row: SysOpenApp) => {
  if (!row.appId) {
    return
  }
  loading.value = true
  try {
    await deleteOpenApps(String(row.appId))
    message.success('应用已删除')
    if (selectedAppId.value === row.appId) {
      selectedAppId.value = null
      credentials.value = []
    }
    await Promise.all([loadApps(), loadOverview()])
  } catch (error) {
    logger.error('删除开放应用失败', error)
    message.error('删除开放应用失败')
  } finally {
    loading.value = false
  }
}

const selectApp = async (appId: number) => {
  selectedAppId.value = appId
  await loadCredentials(appId)
}

const openIssueCredential = () => {
  if (!selectedAppId.value) {
    message.warning('请先选择一个开放应用')
    return
  }
  resetCredentialForm()
  showCredentialModal.value = true
}

const openRotateCredential = (row: OpenApiCredentialView) => {
  Object.assign(credentialForm, {
    credentialName: row.credentialName,
    securityMode: row.securityMode,
    signatureAlgorithm: row.signatureAlgorithm,
    encryptionAlgorithm: row.encryptionAlgorithm,
    activeFrom: row.activeFrom || '',
    activeTo: row.activeTo || '',
    remark: row.remark || ''
  })
  ;(credentialForm as OpenApiCredentialRequest & {
    credentialId?: number
  }).credentialId = row.credentialId
  showCredentialModal.value = true
}

const handleSaveCredential = async () => {
  if (!selectedAppId.value) {
    message.warning('请先选择一个开放应用')
    return
  }
  const payload = {
    ...credentialForm,
    activeFrom: credentialForm.activeFrom || null,
    activeTo: credentialForm.activeTo || null
  }
  const credentialId = (credentialForm as OpenApiCredentialRequest & {
    credentialId?: number
  }).credentialId
  loading.value = true
  try {
    issuedSecret.value = credentialId
      ? await rotateOpenAppCredential(credentialId, payload)
      : await issueOpenAppCredential(selectedAppId.value, payload)
    showCredentialModal.value = false
    showIssuedSecretModal.value = true
    message.success(credentialId ? '凭证已轮换' : '凭证已签发')
    await Promise.all([loadCredentials(selectedAppId.value), loadOverview()])
  } catch (error) {
    logger.error('签发凭证失败', error)
    message.error('凭证操作失败')
  } finally {
    ;(credentialForm as OpenApiCredentialRequest & {
      credentialId?: number
    }).credentialId = undefined
    loading.value = false
  }
}

const handleRevokeCredential = async (row: OpenApiCredentialView) => {
  loading.value = true
  try {
    await revokeOpenAppCredential(row.credentialId)
    message.success('凭证已吊销')
    if (selectedAppId.value) {
      await Promise.all([loadCredentials(selectedAppId.value), loadOverview()])
    }
  } catch (error) {
    logger.error('吊销凭证失败', error)
    message.error('吊销凭证失败')
  } finally {
    loading.value = false
  }
}

const openCreatePolicy = () => {
  resetPolicyForm()
  showPolicyModal.value = true
}

const openEditPolicy = (row: SysOpenApiPolicy) => {
  Object.assign(policyForm, row)
  showPolicyModal.value = true
}

const handleSavePolicy = async () => {
  loading.value = true
  try {
    if (isEditingPolicy.value) {
      await updateOpenApiPolicy({...policyForm})
      message.success('策略已更新')
    } else {
      await createOpenApiPolicy({...policyForm})
      message.success('策略已创建')
    }
    showPolicyModal.value = false
    await Promise.all([loadPolicies(), loadOverview()])
  } catch (error) {
    logger.error('保存开放API策略失败', error)
    message.error('保存开放API策略失败')
  } finally {
    loading.value = false
  }
}

const handleDeletePolicy = async (row: SysOpenApiPolicy) => {
  if (!row.policyId) {
    return
  }
  loading.value = true
  try {
    await deleteOpenApiPolicies(String(row.policyId))
    message.success('策略已删除')
    await Promise.all([loadPolicies(), loadOverview()])
  } catch (error) {
    logger.error('删除开放API策略失败', error)
    message.error('删除开放API策略失败')
  } finally {
    loading.value = false
  }
}

const copyHint = computed(() => issuedSecret.value ? '下列密钥与私钥只展示一次，创建后请立即保存到安全存储。' : '')

onMounted(() => {
  refreshAll()
})
</script>

<template>
  <div class="open-api-security-view">
    <n-space :size="16" vertical>
      <n-card :bordered="false" class="hero-card">
        <div class="hero-content">
          <div>
            <h2>开放 API 安全中心</h2>
            <p>统一管理 Token / AKSK /
              双因子鉴权、签名算法、国际与国密加密策略，以及开放应用凭证与接口策略。</p>
          </div>
          <n-space>
            <n-button :loading="loading" tertiary @click="refreshAll">刷新</n-button>
            <n-button :loading="loading" type="primary" @click="handleSaveSettings">保存全局配置
            </n-button>
          </n-space>
        </div>
      </n-card>

      <n-grid :x-gap="12" :y-gap="12" cols="1 s:3" responsive="screen">
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic :value="overview.appCount" label="开放应用"/>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic :value="overview.credentialCount" label="活动凭证"/>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card :bordered="false" class="stat-card">
            <n-statistic :value="overview.policyCount" label="接口策略"/>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-card :bordered="false">
        <n-tabs v-model:value="activeTab" animated type="line">
          <n-tab-pane name="settings" tab="全局策略">
            <n-grid :x-gap="16" :y-gap="12" cols="1 s:2" responsive="screen">
              <n-grid-item>
                <n-form :model="settingsForm" label-placement="left" label-width="140">
                  <n-form-item label="默认安全模式">
                    <n-select v-model:value="settingsForm.defaultMode"
                              :options="securityModeOptions"/>
                  </n-form-item>
                  <n-form-item label="默认签名算法">
                    <n-select v-model:value="settingsForm.defaultSignatureAlgorithm"
                              :options="signatureAlgorithmOptions"/>
                  </n-form-item>
                  <n-form-item label="默认加密算法">
                    <n-select v-model:value="settingsForm.defaultEncryptionAlgorithm"
                              :options="encryptionAlgorithmOptions"/>
                  </n-form-item>
                  <n-form-item label="签名时间窗（秒）">
                    <n-input-number v-model:value="settingsForm.timestampWindowSeconds" :min="1"/>
                  </n-form-item>
                </n-form>
              </n-grid-item>
              <n-grid-item>
                <n-form :model="settingsForm" label-placement="left" label-width="140">
                  <n-form-item label="启用 Nonce 防重放">
                    <n-switch v-model:value="settingsForm.nonceEnabled"/>
                  </n-form-item>
                  <n-form-item label="Nonce 过期时间">
                    <n-input-number v-model:value="settingsForm.nonceTtlSeconds" :min="1"/>
                  </n-form-item>
                  <n-form-item label="强制报文加密">
                    <n-switch v-model:value="settingsForm.encryptionRequired"/>
                  </n-form-item>
                  <n-form-item label="启用国密算法">
                    <n-switch v-model:value="settingsForm.gmEnabled"/>
                  </n-form-item>
                </n-form>
              </n-grid-item>
            </n-grid>

            <n-alert :bordered="false" title="运行时说明" type="info">
              `/openapi/**` 路径会走统一开放 API 过滤器。后台管理 SPA 继续使用 HTTPS +
              Bearer，不再接入前端自定义加解密链路。
            </n-alert>
          </n-tab-pane>

          <n-tab-pane name="apps" tab="应用与凭证">
            <n-grid :x-gap="16" :y-gap="16" cols="1 xl:2" responsive="screen">
              <n-grid-item>
                <n-card :bordered="false" title="开放应用">
                  <template #header-extra>
                    <n-space>
                      <n-input v-model:value="keyword" clearable placeholder="搜索应用/租户/负责人"
                               style="width: 220px"/>
                      <n-button type="primary" @click="openCreateApp">新增应用</n-button>
                    </n-space>
                  </template>
                  <n-data-table
                    :bordered="false"
                    :columns="appColumns"
                    :data="filteredApps"
                    :single-line="false"
                    size="small"
                  />
                </n-card>
              </n-grid-item>

              <n-grid-item>
                <n-card :bordered="false">
                  <template #header>
                    <div class="section-title">
                      <span>凭证管理</span>
                      <n-tag v-if="selectedApp" size="small" type="info">{{
                          selectedApp.appName
                        }}
                      </n-tag>
                    </div>
                  </template>
                  <template #header-extra>
                    <n-button :disabled="!selectedAppId" type="primary"
                              @click="openIssueCredential">签发凭证
                    </n-button>
                  </template>

                  <n-empty v-if="!selectedAppId" description="请选择左侧应用后管理凭证"/>
                  <n-data-table
                    v-else
                    :bordered="false"
                    :columns="credentialColumns"
                    :data="credentials"
                    :single-line="false"
                    size="small"
                  />

                  <n-divider title-placement="left">凭证策略说明</n-divider>
                  <n-alert :bordered="false" type="warning">
                    `SK`、服务端私钥只在签发或轮换时展示一次，数据库仅保存加密后的密文和指纹。
                  </n-alert>
                </n-card>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="policies" tab="接口策略">
            <n-card :bordered="false">
              <template #header-extra>
                <n-space>
                  <n-input v-model:value="policyKeyword" clearable placeholder="搜索策略/路径/模式"
                           style="width: 220px"/>
                  <n-button type="primary" @click="openCreatePolicy">新增策略</n-button>
                </n-space>
              </template>
              <n-data-table
                :bordered="false"
                :columns="policyColumns"
                :data="filteredPolicies"
                :single-line="false"
                size="small"
              />
            </n-card>
          </n-tab-pane>
        </n-tabs>
      </n-card>
    </n-space>

    <n-modal v-model:show="showAppModal" preset="card" style="width: 640px" title="开放应用">
      <n-form :model="appForm" label-placement="left" label-width="120">
        <n-grid :x-gap="12" cols="2">
          <n-grid-item>
            <n-form-item label="应用编码">
              <n-input v-model:value="appForm.appCode" placeholder="partner_center"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="应用名称">
              <n-input v-model:value="appForm.appName" placeholder="合作伙伴中心"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="租户ID">
              <n-input v-model:value="appForm.tenantId" placeholder="tenant001"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="负责人">
              <n-input v-model:value="appForm.ownerName" placeholder="张三"/>
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="状态">
          <n-select v-model:value="appForm.status" :options="statusOptions"/>
        </n-form-item>
        <n-form-item label="IP白名单">
          <n-input v-model:value="appForm.ipWhitelist" placeholder="支持逗号分隔、通配符与CIDR"
                   type="textarea"/>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="appForm.remark" placeholder="应用说明、责任边界、对接信息"
                   type="textarea"/>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAppModal = false">取消</n-button>
          <n-button :loading="loading" type="primary" @click="handleSaveApp">
            {{ isEditingApp ? '保存修改' : '创建应用' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showCredentialModal" preset="card" style="width: 720px" title="凭证管理">
      <n-form :model="credentialForm" label-placement="left" label-width="140">
        <n-grid :x-gap="12" cols="2">
          <n-grid-item>
            <n-form-item label="凭证名称">
              <n-input v-model:value="credentialForm.credentialName" placeholder="默认生产凭证"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="安全模式">
              <n-select v-model:value="credentialForm.securityMode" :options="securityModeOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="签名算法">
              <n-select v-model:value="credentialForm.signatureAlgorithm"
                        :options="signatureAlgorithmOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="加密算法">
              <n-select v-model:value="credentialForm.encryptionAlgorithm"
                        :options="encryptionAlgorithmOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="生效时间">
              <n-input v-model:value="credentialForm.activeFrom" placeholder="2026-04-13T14:00:00"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="失效时间">
              <n-input v-model:value="credentialForm.activeTo" placeholder="2027-04-13T14:00:00"/>
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="备注">
          <n-input v-model:value="credentialForm.remark" placeholder="轮换策略、对接环境、风险备注"
                   type="textarea"/>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCredentialModal = false">取消</n-button>
          <n-button :loading="loading" type="primary" @click="handleSaveCredential">确认</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showPolicyModal" preset="card" style="width: 760px" title="接口策略">
      <n-form :model="policyForm" label-placement="left" label-width="140">
        <n-grid :x-gap="12" cols="2">
          <n-grid-item>
            <n-form-item label="策略名称">
              <n-input v-model:value="policyForm.policyName" placeholder="默认开放接口策略"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="HTTP方法">
              <n-select v-model:value="policyForm.httpMethod" :options="httpMethodOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item :span="2">
            <n-form-item label="路径模式">
              <n-input v-model:value="policyForm.pathPattern" placeholder="/openapi/**"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="安全模式">
              <n-select v-model:value="policyForm.securityMode" :options="securityModeOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="签名算法">
              <n-select v-model:value="policyForm.signatureAlgorithm"
                        :options="signatureAlgorithmOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="加密算法">
              <n-select v-model:value="policyForm.encryptionAlgorithm"
                        :options="encryptionAlgorithmOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="时间窗（秒）">
              <n-input-number v-model:value="policyForm.timestampWindowSeconds" :min="1"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="Nonce校验">
              <n-select v-model:value="policyForm.nonceEnabled" :options="booleanOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="Nonce TTL">
              <n-input-number v-model:value="policyForm.nonceTtlSeconds" :min="1"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="要求租户">
              <n-select v-model:value="policyForm.tenantRequired" :options="booleanOptions"/>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="启用状态">
              <n-select v-model:value="policyForm.enabled" :options="statusOptions"/>
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="备注">
          <n-input v-model:value="policyForm.remark" placeholder="说明命中范围、租户要求、敏感等级"
                   type="textarea"/>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPolicyModal = false">取消</n-button>
          <n-button :loading="loading" type="primary" @click="handleSavePolicy">
            {{ isEditingPolicy ? '保存修改' : '创建策略' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showIssuedSecretModal" preset="card" style="width: 760px"
             title="凭证已生成">
      <n-space :size="12" vertical>
        <n-alert v-if="copyHint" :bordered="false" :title="copyHint" type="warning"/>
        <n-card size="small" title="AK / SK">
          <n-code :code="JSON.stringify({
            accessKey: issuedSecret?.credential?.accessKey,
            secretKey: issuedSecret?.secretKey,
            secretKeyFingerprint: issuedSecret?.credential?.secretKeyFingerprint,
            serverPublicKey: issuedSecret?.credential?.serverPublicKey
          }, null, 2)" language="json"/>
        </n-card>
        <n-card v-if="issuedSecret?.signaturePrivateKey" size="small" title="签名私钥">
          <n-code :code="issuedSecret.signaturePrivateKey" language="text"/>
        </n-card>
        <n-card v-if="issuedSecret?.encryptionPrivateKey" size="small" title="客户端解密私钥">
          <n-code :code="issuedSecret.encryptionPrivateKey" language="text"/>
        </n-card>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button type="primary" @click="showIssuedSecretModal = false">我已保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.open-api-security-view {
  --page-accent: #165d4b;
  --page-accent-soft: rgba(22, 93, 75, 0.1);
}

.hero-card {
  background: radial-gradient(circle at top left, rgba(22, 93, 75, 0.18), transparent 40%),
  linear-gradient(135deg, #f6fbf8 0%, #eef5ff 100%);
  overflow: hidden;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
}

.hero-content h2 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.hero-content p {
  margin: 0;
  max-width: 760px;
  line-height: 1.7;
  color: rgba(0, 0, 0, 0.68);
}

.stat-card {
  background: linear-gradient(135deg, rgba(22, 93, 75, 0.08), rgba(32, 96, 160, 0.08));
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
  }
}
</style>
