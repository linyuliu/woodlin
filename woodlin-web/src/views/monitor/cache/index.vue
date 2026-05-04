<!--
  @file views/monitor/cache/index.vue
  @description 缓存监控：三栏布局（缓存名 → 键列表 → 值详情），支持按名/键删除
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NEmpty,
  NGi,
  NGrid,
  NInput,
  NList,
  NListItem,
  NPopconfirm,
  NScrollbar,
  NThing,
  useMessage,
} from 'naive-ui'
import {
  clearCacheKey,
  clearCacheName,
  getCacheKeys,
  getCacheNames,
  getCacheValue,
} from '@/api/monitor'

const message = useMessage()

const cacheNames: Ref<string[]> = ref([])
const cacheKeys: Ref<string[]> = ref([])
const cacheValue = ref<string>('')

const selectedName = ref<string>('')
const selectedKey = ref<string>('')

const loadingNames = ref(false)
const loadingKeys = ref(false)
const loadingValue = ref(false)

async function loadNames(): Promise<void> {
  loadingNames.value = true
  try {
    const res = await getCacheNames()
    cacheNames.value = res?.cacheNames ?? []
  } finally {
    loadingNames.value = false
  }
}

async function loadKeys(name: string): Promise<void> {
  selectedName.value = name
  selectedKey.value = ''
  cacheValue.value = ''
  loadingKeys.value = true
  try {
    cacheKeys.value = (await getCacheKeys(name)) ?? []
  } finally {
    loadingKeys.value = false
  }
}

async function loadValue(key: string): Promise<void> {
  selectedKey.value = key
  loadingValue.value = true
  try {
    const res = await getCacheValue(selectedName.value, key)
    cacheValue.value = res?.cacheValue ?? ''
  } finally {
    loadingValue.value = false
  }
}

async function handleDeleteName(name: string): Promise<void> {
  await clearCacheName(name)
  message.success('已清空缓存')
  if (selectedName.value === name) {
    selectedName.value = ''
    cacheKeys.value = []
    selectedKey.value = ''
    cacheValue.value = ''
  }
  void loadNames()
}

async function handleDeleteKey(key: string): Promise<void> {
  await clearCacheKey(selectedName.value, key)
  message.success('已删除键')
  if (selectedKey.value === key) {
    selectedKey.value = ''
    cacheValue.value = ''
  }
  void loadKeys(selectedName.value)
}

onMounted(() => {
  void loadNames()
})
</script>

<template>
  <div class="page-cache">
    <n-grid :cols="3" :x-gap="12" responsive="screen" item-responsive>
      <n-gi span="3 m:1">
        <n-card title="缓存名称列表" size="small">
          <template #header-extra>
            <n-button size="small" @click="loadNames">刷新</n-button>
          </template>
          <n-scrollbar style="max-height: 600px">
            <n-list v-if="cacheNames.length" hoverable clickable>
              <n-list-item
                v-for="name in cacheNames"
                :key="name"
                :class="{ active: name === selectedName }"
                @click="loadKeys(name)"
              >
                <n-thing>
                  <template #header>{{ name }}</template>
                </n-thing>
                <template #suffix>
                  <n-popconfirm @positive-click="handleDeleteName(name)">
                    <template #trigger>
                      <n-button size="tiny" text type="error" @click.stop>清空</n-button>
                    </template>
                    确认清空缓存 {{ name }}？
                  </n-popconfirm>
                </template>
              </n-list-item>
            </n-list>
            <n-empty v-else :description="loadingNames ? '加载中...' : '暂无缓存'" />
          </n-scrollbar>
        </n-card>
      </n-gi>

      <n-gi span="3 m:1">
        <n-card :title="`缓存键列表 ${selectedName ? '— ' + selectedName : ''}`" size="small">
          <template #header-extra>
            <n-button
              size="small"
              :disabled="!selectedName"
              @click="selectedName && loadKeys(selectedName)"
            >
              刷新
            </n-button>
          </template>
          <n-scrollbar style="max-height: 600px">
            <n-list v-if="cacheKeys.length" hoverable clickable>
              <n-list-item
                v-for="k in cacheKeys"
                :key="k"
                :class="{ active: k === selectedKey }"
                @click="loadValue(k)"
              >
                <n-thing>
                  <template #header>{{ k }}</template>
                </n-thing>
                <template #suffix>
                  <n-popconfirm @positive-click="handleDeleteKey(k)">
                    <template #trigger>
                      <n-button size="tiny" text type="error" @click.stop>删除</n-button>
                    </template>
                    确认删除键 {{ k }}？
                  </n-popconfirm>
                </template>
              </n-list-item>
            </n-list>
            <n-empty
              v-else
              :description="selectedName ? (loadingKeys ? '加载中...' : '暂无键') : '请先选择缓存名'"
            />
          </n-scrollbar>
        </n-card>
      </n-gi>

      <n-gi span="3 m:1">
        <n-card :title="`缓存值 ${selectedKey ? '— ' + selectedKey : ''}`" size="small">
          <n-scrollbar style="max-height: 600px">
            <n-input
              v-if="selectedKey"
              type="textarea"
              :value="cacheValue"
              :rows="20"
              readonly
              :placeholder="loadingValue ? '加载中...' : ''"
            />
            <n-empty v-else description="请选择键查看缓存值" />
          </n-scrollbar>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
.page-cache {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.active {
  background-color: var(--n-color-target, #f0f6ff);
}
</style>
