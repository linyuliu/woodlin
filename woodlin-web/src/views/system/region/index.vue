<!--
  @file views/system/region/index.vue
  @description 行政区划：只读树形浏览 + 节点详情
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue'
import {
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NInput,
  NSpace,
  NSplit,
  NTree,
  type TreeOption,
} from 'naive-ui'
import { getRegionTree, type SysRegion } from '@/api/system/region'

const treeData: Ref<TreeOption[]> = ref([])
const loading = ref(false)
const filter = ref('')
const selectedKeys = ref<Array<string | number>>([])
const current = ref<SysRegion | null>(null)

/** 等级文案 */
const levelText: Record<number, string> = {
  1: '省 / 直辖市',
  2: '地级市',
  3: '区 / 县',
  4: '乡 / 镇',
  5: '村',
}

/** 区划 -> TreeOption */
function mapTree(list: SysRegion[]): TreeOption[] {
  return list.map((r) => ({
    key: r.code,
    label: r.name,
    raw: r,
    isLeaf: !r.children || r.children.length === 0,
    children: r.children && r.children.length ? mapTree(r.children) : undefined,
  }))
}

/** 拉取树 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await getRegionTree()
    treeData.value = mapTree(res ?? [])
  } finally {
    loading.value = false
  }
}

/** 选中节点 */
function handleSelect(_keys: Array<string | number>, option: Array<TreeOption | null>): void {
  current.value = (option[0]?.raw as SysRegion) ?? null
}

onMounted(() => {
  void refresh()
})
</script>

<template>
  <n-card size="small" class="page-region">
    <n-split direction="horizontal" :default-size="0.4" :max="0.65" :min="0.25">
      <template #1>
        <div class="left">
          <n-input
            v-model:value="filter"
            placeholder="输入名称过滤"
            clearable
            style="margin-bottom: 12px"
          />
          <n-tree
            v-model:selected-keys="selectedKeys"
            :data="treeData"
            :pattern="filter"
            :show-irrelevant-nodes="false"
            :loading="loading"
            block-line
            virtual-scroll
            style="max-height: 600px"
            @update:selected-keys="handleSelect"
          />
        </div>
      </template>
      <template #2>
        <div class="right">
          <n-empty v-if="!current" description="请选择左侧节点查看详情" />
          <n-descriptions v-else :column="1" bordered label-placement="left">
            <n-descriptions-item label="区划编码">
              {{ current.code }}
            </n-descriptions-item>
            <n-descriptions-item label="区划名称">
              {{ current.name }}
            </n-descriptions-item>
            <n-descriptions-item label="简称">
              {{ current.shortName ?? '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="拼音">
              {{ current.pinyin ?? '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="级别">
              {{ current.level ? (levelText[current.level] ?? current.level) : '-' }}
            </n-descriptions-item>
          </n-descriptions>
          <n-space v-if="false">{{ '' }}</n-space>
        </div>
      </template>
    </n-split>
  </n-card>
</template>

<style scoped>
.page-region {
  min-height: 600px;
}
.left {
  padding-right: 12px;
}
.right {
  padding-left: 16px;
}
</style>
