<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NCard, NDescriptions, NDescriptionsItem, NButton, NSpin, NTag } from 'naive-ui'
import { getBuildInfo, type BuildInfo } from '@/api/config'
import { logger } from '@/utils/logger'

const loading = ref(false)
const versionInfo = ref<BuildInfo | null>(null)
const frontendVersion = ref({
  version: '1.0.0',
  buildTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
  nodeVersion: 'Unknown',
  environment: import.meta.env.MODE || 'development'
})

const backendCommitId = computed(() => versionInfo.value?.gitCommitIdAbbrev || versionInfo.value?.gitCommitId || '-')
const backendEnvironment = computed(() => {
  const mode = frontendVersion.value.environment
  switch (mode) {
    case 'production':
      return { type: 'success' as const, text: '生产环境' }
    case 'test':
      return { type: 'warning' as const, text: '测试环境' }
    case 'development':
    case 'dev':
    default:
      return { type: 'info' as const, text: '开发环境' }
  }
})
const backendWorkspaceStatus = computed(() =>
  versionInfo.value?.gitDirty === 'true'
    ? { type: 'warning' as const, text: '存在未提交变更' }
    : { type: 'success' as const, text: '工作区干净' }
)

const loadVersionInfo = async () => {
  loading.value = true
  try {
    versionInfo.value = await getBuildInfo()
  } catch (error) {
    logger.error('获取系统版本信息失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVersionInfo()
})
</script>

<template>
  <NCard title="系统版本信息" class="version-info-card">
    <template #header-extra>
      <NButton @click="loadVersionInfo" :loading="loading">
        刷新
      </NButton>
    </template>
    
    <NSpin :show="loading">
      <div class="version-content">
        <!-- 后端版本信息 -->
        <NCard title="后端系统" size="small" style="margin-bottom: 16px;">
          <NDescriptions v-if="versionInfo" bordered :column="2">
            <NDescriptionsItem label="系统名称">
              <strong>Woodlin Server</strong>
            </NDescriptionsItem>
            <NDescriptionsItem label="系统版本">
              <NTag type="primary">v{{ versionInfo.buildVersion || '-' }}</NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="构建时间">
              {{ versionInfo.buildTime || '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Git提交">
              <code>{{ backendCommitId }}</code>
            </NDescriptionsItem>
            <NDescriptionsItem label="构建环境">
              <NTag :type="backendEnvironment.type">
                {{ backendEnvironment.text }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="Git分支">
              {{ versionInfo.gitBranch || '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="工作区状态">
              <NTag :type="backendWorkspaceStatus.type">
                {{ backendWorkspaceStatus.text }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="远程仓库">
              {{ versionInfo.gitRemoteOriginUrl || '-' }}
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>
        
        <!-- 前端版本信息 -->
        <NCard title="前端系统" size="small">
          <NDescriptions bordered :column="2">
            <NDescriptionsItem label="系统名称">
              <strong>Woodlin Web</strong>
            </NDescriptionsItem>
            <NDescriptionsItem label="系统版本">
              <NTag type="primary">v{{ frontendVersion.version }}</NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="构建时间">
              {{ frontendVersion.buildTime }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Node.js版本">
              {{ frontendVersion.nodeVersion }}
            </NDescriptionsItem>
            <NDescriptionsItem label="构建环境">
              <NTag :type="backendEnvironment.type">
                {{ backendEnvironment.text }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="客户端时间">
              {{ new Date().toLocaleString() }}
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>
      </div>
    </NSpin>
  </NCard>
</template>

<style scoped>
.version-info-card {
  max-width: 800px;
}

.version-content {
  min-height: 300px;
}

code {
  background: var(--n-color-target);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}
</style>
