<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NSpace, NButton, NDataTable, useMessage, NTag, NTabs, NTabPane, NTree, NSpin } from 'naive-ui'
import { 
  getDictTypes, 
  getDictData, 
  getRegionTree, 
  clearDictCache,
  type DictType,
  type DictItem,
  type RegionNode
} from '@/api/dict'

const message = useMessage()
const loading = ref(false)

// 字典类型列表
const dictTypes = ref<DictType[]>([])
const dictTypesColumns = [
  { title: '字典类型', key: 'dictType' },
  { title: '字典名称', key: 'dictName' },
  { title: '字典分类', key: 'dictCategory' }
]

// 选中的字典类型
const selectedDictType = ref<string>('')
const dictData = ref<DictItem[]>([])
const dictDataColumns = [
  { title: '标签', key: 'label' },
  { title: '值', key: 'value' },
  { title: '描述', key: 'desc' },
  { title: '排序', key: 'sort' }
]

// 行政区划树
const regionTree = ref<RegionNode[]>([])
const regionTreeData = ref<any[]>([])

/**
 * 加载字典类型列表
 */
const loadDictTypes = async (useCache: boolean = true) => {
  try {
    loading.value = true
    dictTypes.value = await getDictTypes(useCache)
    message.success(`加载字典类型成功（${useCache ? '使用缓存' : '强制刷新'}）`)
  } catch (error) {
    message.error('加载字典类型失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载指定类型的字典数据
 */
const loadDictData = async (dictType: string, useCache: boolean = true) => {
  if (!dictType) {
    message.warning('请先选择字典类型')
    return
  }
  
  try {
    loading.value = true
    selectedDictType.value = dictType
    dictData.value = await getDictData(dictType, useCache)
    message.success(`加载字典数据成功（${useCache ? '使用缓存' : '强制刷新'}）`)
  } catch (error) {
    message.error('加载字典数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载行政区划树
 */
const loadRegionTree = async (useCache: boolean = true) => {
  try {
    loading.value = true
    regionTree.value = await getRegionTree(useCache)
    
    // 转换为NTree组件格式
    regionTreeData.value = convertToTreeData(regionTree.value)
    
    message.success(`加载行政区划树成功（${useCache ? '使用缓存' : '强制刷新'}）`)
  } catch (error) {
    message.error('加载行政区划树失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 转换为NTree组件数据格式
 */
const convertToTreeData = (nodes: RegionNode[]): any[] => {
  return nodes.map(node => ({
    key: node.code,
    label: `${node.name} (${node.code})`,
    children: node.children ? convertToTreeData(node.children) : undefined
  }))
}

/**
 * 清空缓存
 */
const handleClearCache = () => {
  clearDictCache()
  message.success('缓存已清空')
}

/**
 * 处理字典类型选择
 */
const handleDictTypeSelect = (row: DictType) => {
  loadDictData(row.dictType, true)
}

/**
 * Handle keyboard navigation for row selection
 */
const handleRowKeydown = (event: KeyboardEvent, row: DictType) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    handleDictTypeSelect(row)
  }
}

onMounted(() => {
  loadDictTypes(true)
})
</script>

<template>
  <div class="dict-view">
    <NSpace vertical :size="16">
      <!-- 操作栏 -->
      <NCard :bordered="false">
        <NSpace>
          <NButton type="primary" @click="loadDictTypes(false)" :loading="loading">
            刷新字典类型
          </NButton>
          <NButton type="warning" @click="handleClearCache">
            清空缓存
          </NButton>
          <NTag type="info">
            缓存说明：字典数据会自动缓存5分钟，减少重复请求
          </NTag>
        </NSpace>
      </NCard>

      <NTabs type="line" animated>
        <!-- 字典类型和数据 -->
        <NTabPane name="dict" tab="动态字典">
          <NSpace vertical :size="16">
            <!-- 字典类型列表 -->
            <NCard title="字典类型列表" :bordered="false">
              <template #header-extra>
                <NButton size="small" @click="loadDictTypes(false)" :loading="loading">
                  刷新
                </NButton>
              </template>
              <NDataTable
                :columns="dictTypesColumns"
                :data="dictTypes"
                :bordered="false"
                :single-line="false"
                size="small"
                :row-props="(row: DictType) => ({
                  style: 'cursor: pointer;',
                  tabindex: 0,
                  onClick: () => handleDictTypeSelect(row),
                  onKeydown: (e: KeyboardEvent) => handleRowKeydown(e, row)
                })"
              />
            </NCard>

            <!-- 字典数据 -->
            <NCard 
              v-if="selectedDictType"
              :title="`字典数据 - ${selectedDictType}`" 
              :bordered="false"
            >
              <template #header-extra>
                <NSpace>
                  <NButton 
                    size="small" 
                    @click="loadDictData(selectedDictType, true)" 
                    :loading="loading"
                  >
                    使用缓存
                  </NButton>
                  <NButton 
                    size="small" 
                    @click="loadDictData(selectedDictType, false)" 
                    :loading="loading"
                  >
                    强制刷新
                  </NButton>
                </NSpace>
              </template>
              <NDataTable
                :columns="dictDataColumns"
                :data="dictData"
                :bordered="false"
                :single-line="false"
                size="small"
              />
            </NCard>
          </NSpace>
        </NTabPane>

        <!-- 行政区划树 -->
        <NTabPane name="region" tab="行政区划">
          <NCard title="行政区划树" :bordered="false">
            <template #header-extra>
              <NSpace>
                <NButton 
                  size="small" 
                  @click="loadRegionTree(true)" 
                  :loading="loading"
                >
                  加载（使用缓存）
                </NButton>
                <NButton 
                  size="small" 
                  @click="loadRegionTree(false)" 
                  :loading="loading"
                >
                  强制刷新
                </NButton>
              </NSpace>
            </template>
            <NSpin :show="loading">
              <NTree
                v-if="regionTreeData.length > 0"
                :data="regionTreeData"
                block-line
                expand-on-click
                selectable
              />
              <div v-else style="padding: 20px; text-align: center; color: #999;">
                请点击"加载"按钮加载行政区划树
              </div>
            </NSpin>
          </NCard>
        </NTabPane>
      </NTabs>
    </NSpace>
  </div>
</template>

<style scoped>
.dict-view {
  padding: 16px;
}
</style>
