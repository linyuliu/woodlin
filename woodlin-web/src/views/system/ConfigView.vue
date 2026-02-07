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
  NSpace,
  NStatistic,
  NTag,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import { AddOutline, RefreshOutline, SearchOutline } from '@vicons/ionicons5'
import {
  addConfig,
  deleteConfig,
  evictConfigCache,
  getBuildInfo,
  getConfigList,
  updateConfig,
  warmupConfigCache,
  type BuildInfo,
  type SysConfig
} from '@/api/config'

const message = useMessage()
const loading = ref(false)
const keyword = ref('')

const configList = ref<SysConfig[]>([])
const buildInfo = ref<BuildInfo | null>(null)

const showEditModal = ref(false)
const isEditing = ref(false)
const editingConfig = ref<SysConfig>({
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'system'
})

const filteredConfigList = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) {
    return configList.value
  }
  return configList.value.filter(item => {
    const target = [item.configName, item.configKey, item.configValue, item.configType, item.remark]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return target.includes(key)
  })
})

const categoryCount = computed(() => new Set(configList.value.map(item => item.configType || 'default')).size)

const columns: DataTableColumns<SysConfig> = [
  { title: '名称', key: 'configName', width: 150 },
  { title: '配置键', key: 'configKey', width: 220 },
  { title: '配置值', key: 'configValue', ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'configType',
    width: 110,
    render: row => h(NTag, { size: 'small', type: 'info' }, { default: () => row.configType || 'default' })
  },
  { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: row =>
      h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          {
            size: 'small',
            text: true,
            type: 'primary',
            onClick: () => handleEdit(row)
          },
          { default: () => '编辑' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => {
              if (typeof row.configId === 'number') {
                handleDelete(row.configId)
              }
            }
          },
          {
            default: () => `确认删除配置 ${row.configKey} 吗？`,
            trigger: () =>
              h(
                NButton,
                {
                  size: 'small',
                  text: true,
                  type: 'error'
                },
                { default: () => '删除' }
              )
          }
        )
      ])
  }
]

const loadConfigList = async () => {
  loading.value = true
  try {
    configList.value = await getConfigList()
  } catch (error) {
    console.error(error)
    message.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const loadBuildInfo = async () => {
  try {
    buildInfo.value = await getBuildInfo()
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  editingConfig.value = {
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'system'
  }
  isEditing.value = false
  showEditModal.value = true
}

const handleEdit = (config: SysConfig) => {
  editingConfig.value = { ...config }
  isEditing.value = true
  showEditModal.value = true
}

const handleSave = async () => {
  loading.value = true
  try {
    if (isEditing.value) {
      await updateConfig(editingConfig.value)
      message.success('更新配置成功')
    } else {
      await addConfig(editingConfig.value)
      message.success('新增配置成功')
    }
    showEditModal.value = false
    await loadConfigList()
  } catch (error) {
    console.error(error)
    message.error('保存配置失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (configId: number) => {
  loading.value = true
  try {
    await deleteConfig(String(configId))
    message.success('删除配置成功')
    await loadConfigList()
  } catch (error) {
    console.error(error)
    message.error('删除配置失败')
  } finally {
    loading.value = false
  }
}

const handleEvictCache = async () => {
  loading.value = true
  try {
    await evictConfigCache()
    message.success('配置缓存已清除')
  } catch (error) {
    console.error(error)
    message.error('清除配置缓存失败')
  } finally {
    loading.value = false
  }
}

const handleWarmupCache = async () => {
  loading.value = true
  try {
    await warmupConfigCache()
    message.success('配置缓存预热完成')
  } catch (error) {
    console.error(error)
    message.error('预热配置缓存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfigList()
  loadBuildInfo()
})
</script>

<template>
  <div class="config-view">
    <n-card :bordered="false" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>系统配置中心</h2>
          <p>集中管理平台运行参数、缓存与构建版本信息，支持在线编辑与即时生效。</p>
        </div>
        <n-tag type="info" size="small">运行配置面板</n-tag>
      </div>
    </n-card>

    <n-grid :x-gap="12" :y-gap="12" cols="1 s:3" responsive="screen">
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="配置总数" :value="configList.length" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="配置类型数" :value="categoryCount" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="当前版本" :value="buildInfo?.version || '-'" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card v-if="buildInfo" :bordered="false" title="构建信息">
      <div class="build-info">
        <div class="info-item"><span class="label">构建时间</span><span class="value">{{ buildInfo.buildTime || '-' }}</span></div>
        <div class="info-item"><span class="label">Git Commit</span><span class="value">{{ buildInfo.gitCommit || '-' }}</span></div>
        <div class="info-item"><span class="label">Git Branch</span><span class="value">{{ buildInfo.gitBranch || '-' }}</span></div>
      </div>
    </n-card>

    <n-card :bordered="false" title="配置列表">
      <template #header-extra>
        <n-space>
          <n-input v-model:value="keyword" clearable size="small" placeholder="搜索配置名称/键/值">
            <template #prefix>
              <n-icon><search-outline /></n-icon>
            </template>
          </n-input>
          <n-button size="small" type="primary" @click="handleAdd">
            <template #icon>
              <n-icon><add-outline /></n-icon>
            </template>
            新增配置
          </n-button>
          <n-button size="small" @click="handleEvictCache" :loading="loading">清除缓存</n-button>
          <n-button size="small" @click="handleWarmupCache" :loading="loading">预热缓存</n-button>
          <n-button size="small" @click="loadConfigList" :loading="loading">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新
          </n-button>
        </n-space>
      </template>

      <n-data-table
        :columns="columns"
        :data="filteredConfigList"
        :loading="loading"
        :pagination="{ pageSize: 10, showSizePicker: true, pageSizes: [10, 20, 50] }"
        :bordered="false"
        size="small"
        :single-line="false"
      />
    </n-card>

    <n-modal
      v-model:show="showEditModal"
      preset="card"
      :title="isEditing ? '编辑配置' : '新增配置'"
      style="width: 560px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <n-form :model="editingConfig" label-placement="left" label-width="100">
        <n-form-item label="配置名称" required>
          <n-input v-model:value="editingConfig.configName" placeholder="请输入配置名称" />
        </n-form-item>
        <n-form-item label="配置键" required>
          <n-input v-model:value="editingConfig.configKey" placeholder="请输入配置键" />
        </n-form-item>
        <n-form-item label="配置值" required>
          <n-input v-model:value="editingConfig.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </n-form-item>
        <n-form-item label="配置类型">
          <n-input v-model:value="editingConfig.configType" placeholder="system / security / api" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="editingConfig.remark" type="textarea" :rows="2" placeholder="可选说明" />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.config-view {
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

.build-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.info-item {
  border: 1px solid color-mix(in srgb, var(--border-color-light) 68%, transparent);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  background: color-mix(in srgb, var(--bg-color-tertiary) 54%, transparent);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.value {
  color: var(--text-color-primary);
  font-weight: 500;
}
</style>
