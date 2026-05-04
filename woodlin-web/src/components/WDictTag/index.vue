<!--
  @file WDictTag/index.vue
  @description 根据字典 code + value 渲染带颜色的 n-tag
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { NTag } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'

const props = defineProps<{
  /** 字典类型 code */
  dictCode: string
  /** 字典值 */
  value: string | number | null | undefined
}>()

const store = useDictStore()

onMounted(() => {
  void store.loadDict(props.dictCode)
})

const matched = computed(() => {
  const list = store.cache[props.dictCode] ?? []
  return list.find((it) => String(it.value) === String(props.value))
})
</script>

<template>
  <NTag v-if="matched" :type="(matched.color as 'default' | 'success' | 'warning' | 'error' | 'info' | 'primary') ?? 'default'">
    {{ matched.label }}
  </NTag>
  <span v-else>{{ value ?? '-' }}</span>
</template>
