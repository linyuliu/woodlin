<!--
  @file WIcon/index.vue
  @description 通过字符串渲染图标的组件，支持 'vicons:antd:Xxx' / 'vicons:ionicons5:Xxx'
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { computed, type Component } from 'vue'
import { NIcon } from 'naive-ui'
import * as Antd from '@vicons/antd'
import * as Ionicons from '@vicons/ionicons5'

const props = withDefaults(
  defineProps<{
    /** 图标标识，如 'vicons:antd:UserOutlined' 或 'UserOutlined' */
    icon: string
    /** 图标尺寸 */
    size?: number | string
    /** 颜色 */
    color?: string
  }>(),
  { size: 18, color: undefined },
)

const AntdMap = Antd as unknown as Record<string, Component>
const IoniconsMap = Ionicons as unknown as Record<string, Component>

/** 解析为具体组件 */
const resolved = computed<Component | null>(() => {
  if (!props.icon) {return null}
  const parts = props.icon.split(':')
  let pack = 'antd'
  let name = props.icon
  if (parts.length === 3 && parts[0] === 'vicons') {
    pack = parts[1]
    name = parts[2]
  } else if (parts.length === 2) {
    pack = parts[0]
    name = parts[1]
  }
  const map = pack === 'ionicons5' ? IoniconsMap : AntdMap
  return map[name] ?? null
})
</script>

<template>
  <NIcon v-if="resolved" :size="size" :color="color">
    <component :is="resolved" />
  </NIcon>
  <span v-else />
</template>
