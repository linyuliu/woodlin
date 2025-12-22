<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NSpace, NButton, NDataTable, useMessage, NTag } from 'naive-ui'
import { getUserStatusDict, getGenderDict, getDemoUser, type DictItem, type DemoUser } from '@/api/dict'

const message = useMessage()
const loading = ref(false)

// 用户状态字典
const userStatusDict = ref<DictItem[]>([])
const userStatusColumns = [
  { title: '标签', key: 'label' },
  { title: '值', key: 'value' }
]

// 性别字典
const genderDict = ref<DictItem[]>([])
const genderColumns = [
  { title: '标签', key: 'label' },
  { title: '值', key: 'value' }
]

// 演示用户
const demoUser = ref<DemoUser | null>(null)

/**
 * 加载用户状态字典
 */
const loadUserStatusDict = async () => {
  try {
    loading.value = true
    userStatusDict.value = await getUserStatusDict()
    message.success('加载用户状态字典成功')
  } catch (error) {
    message.error('加载用户状态字典失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载性别字典
 */
const loadGenderDict = async () => {
  try {
    loading.value = true
    genderDict.value = await getGenderDict()
    message.success('加载性别字典成功')
  } catch (error) {
    message.error('加载性别字典失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载演示用户
 */
const loadDemoUser = async () => {
  try {
    loading.value = true
    demoUser.value = await getDemoUser()
    message.success('加载演示用户成功')
  } catch (error) {
    message.error('加载演示用户失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserStatusDict()
  loadGenderDict()
  loadDemoUser()
})
</script>

<template>
  <div class="dict-view">
    <NSpace vertical :size="16">
      <!-- 用户状态字典 -->
      <NCard title="用户状态字典" :bordered="false">
        <template #header-extra>
          <NButton size="small" @click="loadUserStatusDict" :loading="loading">
            刷新
          </NButton>
        </template>
        <NDataTable
          :columns="userStatusColumns"
          :data="userStatusDict"
          :bordered="false"
          :single-line="false"
          size="small"
        />
      </NCard>

      <!-- 性别字典 -->
      <NCard title="性别字典" :bordered="false">
        <template #header-extra>
          <NButton size="small" @click="loadGenderDict" :loading="loading">
            刷新
          </NButton>
        </template>
        <NDataTable
          :columns="genderColumns"
          :data="genderDict"
          :bordered="false"
          :single-line="false"
          size="small"
        />
      </NCard>

      <!-- 演示用户对象 -->
      <NCard title="演示用户对象（字典枚举序列化）" :bordered="false">
        <template #header-extra>
          <NButton size="small" @click="loadDemoUser" :loading="loading">
            刷新
          </NButton>
        </template>
        <div v-if="demoUser" class="demo-user">
          <div class="user-item">
            <span class="label">ID:</span>
            <span class="value">{{ demoUser.id }}</span>
          </div>
          <div class="user-item">
            <span class="label">姓名:</span>
            <span class="value">{{ demoUser.name }}</span>
          </div>
          <div class="user-item">
            <span class="label">性别:</span>
            <NTag type="info">
              {{ demoUser.gender.label }} ({{ demoUser.gender.value }})
            </NTag>
          </div>
          <div class="user-item">
            <span class="label">状态:</span>
            <NTag :type="demoUser.status.value === 'ENABLE' ? 'success' : 'error'">
              {{ demoUser.status.label }} ({{ demoUser.status.value }})
            </NTag>
          </div>
        </div>
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
.dict-view {
  padding: 16px;
}

.demo-user {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-item .label {
  font-weight: 500;
  min-width: 60px;
}

.user-item .value {
  color: #666;
}
</style>
