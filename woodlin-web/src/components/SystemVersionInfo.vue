<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NDescriptions, NDescriptionsItem, NButton, NSpin, NTag } from 'naive-ui'
import axios from 'axios'

interface SystemVersionInfo {
  systemName: string
  systemVersion: string
  buildTime: string
  gitCommitId: string
  buildProfile: string
  javaVersion: string
  springProfile: string
  currentTime: string
}

const loading = ref(false)
const versionInfo = ref<SystemVersionInfo | null>(null)
const frontendVersion = ref({
  version: '1.0.0',
  buildTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
  nodeVersion: 'Unknown',
  environment: 'development'
})

const loadVersionInfo = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/system/version')
    versionInfo.value = response.data
  } catch (error) {
    console.error('获取系统版本信息失败:', error)
  } finally {
    loading.value = false
  }
}

const getBuildStatus = (profile: string): { type: 'success' | 'warning' | 'info', text: string } => {
  switch (profile) {
    case 'production':
      return { type: 'success', text: '生产环境' }
    case 'test':
      return { type: 'warning', text: '测试环境' }
    case 'development':
    case 'dev':
    default:
      return { type: 'info', text: '开发环境' }
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
              <strong>{{ versionInfo.systemName }}</strong>
            </NDescriptionsItem>
            <NDescriptionsItem label="系统版本">
              <NTag type="primary">v{{ versionInfo.systemVersion }}</NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="构建时间">
              {{ versionInfo.buildTime }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Git提交">
              <code>{{ versionInfo.gitCommitId }}</code>
            </NDescriptionsItem>
            <NDescriptionsItem label="构建环境">
              <NTag v-bind="getBuildStatus(versionInfo.buildProfile)">
                {{ getBuildStatus(versionInfo.buildProfile).text }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="Java版本">
              {{ versionInfo.javaVersion }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Spring环境">
              {{ versionInfo.springProfile }}
            </NDescriptionsItem>
            <NDescriptionsItem label="服务器时间">
              {{ versionInfo.currentTime }}
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>
        
        <!-- 前端版本信息 -->
        <NCard title="前端系统" size="small">
          <NDescriptions bordered :column="2">
            <NDescriptionsItem label="系统名称">
              <strong>{{ versionInfo?.systemName || 'Woodlin' }} Web</strong>
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
              <NTag v-bind="getBuildStatus(frontendVersion.environment)">
                {{ getBuildStatus(frontendVersion.environment).text }}
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