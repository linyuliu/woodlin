<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { 
  NCard, 
  NSpace, 
  NButton, 
  NDataTable, 
  NInput,
  NForm,
  NFormItem,
  NModal,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import { 
  getConfigList, 
  getConfigsByCategory,
  addConfig,
  updateConfig,
  deleteConfig,
  getBuildInfo,
  evictConfigCache,
  warmupConfigCache,
  type SysConfig,
  type BuildInfo
} from '@/api/config'

const message = useMessage()
const loading = ref(false)

// 配置列表
const configList = ref<SysConfig[]>([])

// 构建信息
const buildInfo = ref<BuildInfo | null>(null)

// 编辑对话框
const showEditModal = ref(false)
const editingConfig = ref<SysConfig>({
  configName: '',
  configKey: '',
  configValue: ''
})
const isEditing = ref(false)

// 表格列定义
const columns: DataTableColumns<SysConfig> = [
  { title: 'ID', key: 'configId', width: 80 },
  { title: '配置名称', key: 'configName', width: 150 },
  { title: '配置键', key: 'configKey', width: 200 },
  { 
    title: '配置值', 
    key: 'configValue',
    ellipsis: {
      tooltip: true
    }
  },
  { title: '配置类型', key: 'configType', width: 100 },
  { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          style: { marginRight: '8px' },
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row.configId!)
        }, { default: () => '删除' })
      ]
    }
  }
]

/**
 * 加载配置列表
 */
const loadConfigList = async () => {
  try {
    loading.value = true
    configList.value = await getConfigList()
    message.success('加载配置列表成功')
  } catch (error) {
    message.error('加载配置列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载构建信息
 */
const loadBuildInfo = async () => {
  try {
    buildInfo.value = await getBuildInfo()
  } catch (error) {
    console.error('加载构建信息失败:', error)
  }
}

/**
 * 添加配置
 */
const handleAdd = () => {
  editingConfig.value = {
    configName: '',
    configKey: '',
    configValue: ''
  }
  isEditing.value = false
  showEditModal.value = true
}

/**
 * 编辑配置
 */
const handleEdit = (config: SysConfig) => {
  editingConfig.value = { ...config }
  isEditing.value = true
  showEditModal.value = true
}

/**
 * 保存配置
 */
const handleSave = async () => {
  try {
    loading.value = true
    if (isEditing.value) {
      await updateConfig(editingConfig.value)
      message.success('更新配置成功')
    } else {
      await addConfig(editingConfig.value)
      message.success('添加配置成功')
    }
    showEditModal.value = false
    await loadConfigList()
  } catch (error) {
    message.error('保存配置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 删除配置
 */
const handleDelete = async (configId: number) => {
  try {
    loading.value = true
    await deleteConfig(String(configId))
    message.success('删除配置成功')
    await loadConfigList()
  } catch (error) {
    message.error('删除配置失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 清除缓存
 */
const handleEvictCache = async () => {
  try {
    loading.value = true
    await evictConfigCache()
    message.success('清除缓存成功')
  } catch (error) {
    message.error('清除缓存失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 预热缓存
 */
const handleWarmupCache = async () => {
  try {
    loading.value = true
    await warmupConfigCache()
    message.success('预热缓存成功')
  } catch (error) {
    message.error('预热缓存失败')
    console.error(error)
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
    <NSpace vertical :size="16">
      <!-- 构建信息 -->
      <NCard v-if="buildInfo" title="构建信息" :bordered="false" size="small">
        <div class="build-info">
          <div class="info-item">
            <span class="label">版本:</span>
            <span class="value">{{ buildInfo.version }}</span>
          </div>
          <div class="info-item">
            <span class="label">构建时间:</span>
            <span class="value">{{ buildInfo.buildTime }}</span>
          </div>
          <div v-if="buildInfo.gitCommit" class="info-item">
            <span class="label">Git Commit:</span>
            <span class="value">{{ buildInfo.gitCommit }}</span>
          </div>
          <div v-if="buildInfo.gitBranch" class="info-item">
            <span class="label">Git Branch:</span>
            <span class="value">{{ buildInfo.gitBranch }}</span>
          </div>
        </div>
      </NCard>

      <!-- 配置管理 -->
      <NCard title="系统配置管理" :bordered="false">
        <template #header-extra>
          <NSpace>
            <NButton size="small" @click="handleAdd">
              新增配置
            </NButton>
            <NButton size="small" @click="handleEvictCache" :loading="loading">
              清除缓存
            </NButton>
            <NButton size="small" @click="handleWarmupCache" :loading="loading">
              预热缓存
            </NButton>
            <NButton size="small" @click="loadConfigList" :loading="loading">
              刷新
            </NButton>
          </NSpace>
        </template>
        <NDataTable
          :columns="columns"
          :data="configList"
          :bordered="false"
          :single-line="false"
          :loading="loading"
          size="small"
        />
      </NCard>
    </NSpace>

    <!-- 编辑对话框 -->
    <NModal
      v-model:show="showEditModal"
      preset="dialog"
      :title="isEditing ? '编辑配置' : '新增配置'"
      positive-text="保存"
      negative-text="取消"
      @positive-click="handleSave"
    >
      <NForm :model="editingConfig" label-placement="left" label-width="100">
        <NFormItem label="配置名称" required>
          <NInput v-model:value="editingConfig.configName" placeholder="请输入配置名称" />
        </NFormItem>
        <NFormItem label="配置键" required>
          <NInput v-model:value="editingConfig.configKey" placeholder="请输入配置键" />
        </NFormItem>
        <NFormItem label="配置值" required>
          <NInput 
            v-model:value="editingConfig.configValue" 
            type="textarea"
            placeholder="请输入配置值"
            :rows="3"
          />
        </NFormItem>
        <NFormItem label="配置类型">
          <NInput v-model:value="editingConfig.configType" placeholder="请输入配置类型" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput 
            v-model:value="editingConfig.remark" 
            type="textarea"
            placeholder="请输入备注"
            :rows="2"
          />
        </NFormItem>
      </NForm>
    </NModal>
  </div>
</template>

<script lang="ts">
import { h } from 'vue'

export default {
  name: 'ConfigView'
}
</script>

<style scoped>
.config-view {
  padding: 16px;
}

.build-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  min-width: 100px;
}

.info-item .value {
  color: #666;
}
</style>
