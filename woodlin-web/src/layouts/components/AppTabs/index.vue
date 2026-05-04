<!--
  @file layouts/components/AppTabs/index.vue
  @description 多标签页栏：基于 tabs store，支持关闭、关闭其他、关闭全部
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, nextTick, ref, watch, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane, NDropdown, type DropdownOption } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { useTabsStore, type TabItem } from '@/stores/modules/tabs'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()

const tabs = computed<TabItem[]>(() => tabsStore.tabs)
const active = computed<string>({
  get: () => tabsStore.activeTab,
  set: (v) => tabsStore.setActive(v),
})

watch(
  () => route.fullPath,
  () => {
    if ((route.meta as Record<string, unknown>)?.showInTabs === false) return
    if (!route.name) return
    tabsStore.addTab(route)
  },
  { immediate: true },
)

/** 切换标签 */
function handleChange(key: string | number): void {
  const target = tabs.value.find((t) => t.fullPath === key)
  if (target) void router.push(target.fullPath)
}

/** 关闭某个标签 */
function handleClose(key: string | number): void {
  const fullPath = String(key)
  const target = tabs.value.find((t) => t.fullPath === fullPath)
  if (!target || target.affix) return
  tabsStore.removeTab(fullPath)
  if (active.value === fullPath) {
    const next = tabs.value[tabs.value.length - 1]
    if (next) {
      tabsStore.setActive(next.fullPath)
      void router.push(next.fullPath)
    }
  }
}

// ----- 右键菜单 -----
const ctxVisible = ref(false)
const ctxX = ref(0)
const ctxY = ref(0)
const ctxKey = ref('')

const ctxOptions = computed<DropdownOption[]>(() => [
  {
    label: '关闭',
    key: 'close',
    icon: () => h(WIcon, { icon: 'vicons:antd:CloseOutlined' }),
    disabled: tabs.value.find((t) => t.fullPath === ctxKey.value)?.affix,
  },
  {
    label: '关闭其他',
    key: 'closeOthers',
    icon: () => h(WIcon, { icon: 'vicons:antd:ColumnWidthOutlined' }),
  },
  {
    label: '关闭全部',
    key: 'closeAll',
    icon: () => h(WIcon, { icon: 'vicons:antd:MinusOutlined' }),
  },
])

/** 右键单个 tab */
function handleContextMenu(e: MouseEvent, fullPath: string): void {
  e.preventDefault()
  ctxVisible.value = false
  ctxX.value = e.clientX
  ctxY.value = e.clientY
  ctxKey.value = fullPath
  void nextTick(() => {
    ctxVisible.value = true
  })
}

/** 右键菜单选择 */
function handleCtxSelect(key: string): void {
  ctxVisible.value = false
  if (key === 'close') {
    handleClose(ctxKey.value)
  } else if (key === 'closeOthers') {
    tabsStore.removeOtherTabs(ctxKey.value)
    if (active.value !== ctxKey.value) {
      tabsStore.setActive(ctxKey.value)
      void router.push(ctxKey.value)
    }
  } else if (key === 'closeAll') {
    tabsStore.removeAllTabs()
    const remain = tabs.value[0]
    if (remain) {
      tabsStore.setActive(remain.fullPath)
      void router.push(remain.fullPath)
    }
  }
}

function handleClickOutside(): void {
  ctxVisible.value = false
}
</script>

<template>
  <div class="app-tabs" @click="handleClickOutside">
    <NTabs
      v-model:value="active"
      type="card"
      size="small"
      closable
      :tab-style="{ minWidth: '80px' }"
      @update:value="handleChange"
      @close="handleClose"
    >
      <NTabPane
        v-for="t in tabs"
        :key="t.fullPath"
        :name="t.fullPath"
        :tab="t.title"
        :closable="!t.affix"
        display-directive="show"
      >
        <template #tab>
          <span @contextmenu="(e: MouseEvent) => handleContextMenu(e, t.fullPath)">
            {{ t.title }}
          </span>
        </template>
      </NTabPane>
    </NTabs>
    <NDropdown
      :show="ctxVisible"
      :options="ctxOptions"
      :x="ctxX"
      :y="ctxY"
      placement="bottom-start"
      trigger="manual"
      @select="handleCtxSelect"
      @clickoutside="handleClickOutside"
    />
  </div>
</template>

<style scoped>
.app-tabs {
  padding: 6px 12px 0;
  background: var(--n-color, #fff);
  border-bottom: 1px solid var(--w-color-border);
}
</style>
