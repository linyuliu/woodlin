<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { 
  NCard, NButton, NSpace, NTable, NDescriptions, 
  NDescriptionsItem, NTag, NPopconfirm, useMessage, NSpin 
} from 'naive-ui'
import axios from 'axios'

interface CacheConfig {
  redisEnabled: boolean
  dictionary: {
    enabled: boolean
    expireSeconds: number
    refreshIntervalSeconds: number
  }
}

const message = useMessage()
const loading = ref(false)
const config = ref<CacheConfig>({
  redisEnabled: true,
  dictionary: {
    enabled: true,
    expireSeconds: 3600,
    refreshIntervalSeconds: 1800
  }
})

// 常见的字典类型示例
const commonDictTypes = [
  { type: 'user_status', name: '用户状态' },
  { type: 'menu_type', name: '菜单类型' },
  { type: 'permission_type', name: '权限类型' },
  { type: 'data_status', name: '数据状态' }
]

const loadCacheConfig = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/cache/config')
    config.value = response.data
  } catch (error) {
    console.error('获取缓存配置失败:', error)
    message.error('获取缓存配置失败')
  } finally {
    loading.value = false
  }
}

const evictDictionaryCache = async (dictType: string) => {
  try {
    await axios.delete(`/api/cache/dictionary/${dictType}`)
    message.success(`字典缓存清除成功: ${dictType}`)
  } catch (error) {
    console.error('清除字典缓存失败:', error)
    message.error('清除字典缓存失败')
  }
}

const evictAllDictionaryCache = async () => {
  try {
    await axios.delete('/api/cache/dictionary/all')
    message.success('所有字典缓存清除成功')
  } catch (error) {
    console.error('清除所有字典缓存失败:', error)
    message.error('清除所有字典缓存失败')
  }
}

const warmupDictionaryCache = async (dictType: string) => {
  try {
    await axios.post(`/api/cache/dictionary/${dictType}/warmup`)
    message.success(`字典缓存预热成功: ${dictType}`)
  } catch (error) {
    console.error('预热字典缓存失败:', error)
    message.error('预热字典缓存失败')
  }
}

const formatTime = (seconds: number) => {
  if (seconds < 60) {return `${seconds}秒`}
  if (seconds < 3600) {return `${Math.floor(seconds / 60)}分钟`}
  return `${Math.floor(seconds / 3600)}小时`
}

const columns = [
  {
    title: '字典类型',
    key: 'type',
    width: 150
  },
  {
    title: '字典名称',
    key: 'name',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    render: (row: { type: string; name: string }) => [
      h(NButton, 
        { 
          text: true, 
          type: 'primary', 
          onClick: () => warmupDictionaryCache(row.type) 
        }, 
        { default: () => '预热' }
      ),
      h('span', ' | '),
      h(NPopconfirm,
        {
          onPositiveClick: () => evictDictionaryCache(row.type)
        },
        {
          trigger: () => h(NButton, 
            { text: true, type: 'error' }, 
            { default: () => '清除' }
          ),
          default: () => `确认清除 ${row.name} 的缓存吗？`
        }
      )
    ]
  }
]

onMounted(() => {
  loadCacheConfig()
})
</script>

<template>
  <div class="cache-management">
    <NSpace vertical size="large">
      <!-- 缓存配置信息 -->
      <NCard title="缓存配置信息">
        <template #header-extra>
          <NButton @click="loadCacheConfig" :loading="loading">
            刷新配置
          </NButton>
        </template>
        
        <NSpin :show="loading">
          <NDescriptions bordered :column="2">
            <NDescriptionsItem label="Redis缓存">
              <NTag :type="config.redisEnabled ? 'success' : 'error'">
                {{ config.redisEnabled ? '已启用' : '已禁用' }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="字典缓存">
              <NTag :type="config.dictionary.enabled ? 'success' : 'error'">
                {{ config.dictionary.enabled ? '已启用' : '已禁用' }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="缓存过期时间">
              {{ formatTime(config.dictionary.expireSeconds) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="刷新间隔">
              {{ formatTime(config.dictionary.refreshIntervalSeconds) }}
            </NDescriptionsItem>
          </NDescriptions>
        </NSpin>
      </NCard>

      <!-- 字典缓存管理 -->
      <NCard title="字典缓存管理" v-if="config.dictionary.enabled">
        <template #header-extra>
          <NSpace>
            <NPopconfirm @positive-click="evictAllDictionaryCache">
              <template #trigger>
                <NButton type="error" ghost>
                  清除所有缓存
                </NButton>
              </template>
              确认清除所有字典缓存吗？此操作不可恢复。
            </NPopconfirm>
          </NSpace>
        </template>

        <NTable 
          :columns="columns" 
          :data="commonDictTypes" 
          :pagination="false"
          :bordered="false"
          size="small"
        />
      </NCard>

      <!-- 缓存状态提示 -->
      <NCard title="缓存状态说明" size="small">
        <div class="cache-tips">
          <p><strong>📋 字典缓存：</strong>缓存系统字典数据，减少数据库查询，提升响应速度</p>
          <p><strong>⚡ 预热缓存：</strong>主动加载数据到缓存中，避免首次访问延迟</p>
          <p><strong>🗑️ 清除缓存：</strong>清理过时数据，确保数据一致性</p>
          <p><strong>⏰ 自动过期：</strong>缓存会在设定时间后自动过期，无需手动清理</p>
        </div>
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
.cache-management {
  max-width: 1000px;
}

.cache-tips {
  line-height: 1.6;
  color: var(--n-text-color-base);
}

.cache-tips p {
  margin: 8px 0;
}
</style>