<script setup lang="ts">
import { NStep, NSteps } from 'naive-ui'
import type { RuntimeSectionVO } from '@/api/assessment'

const props = defineProps<{
  sections: RuntimeSectionVO[]
  currentIndex: number
  answeredItemCodes: string[]
}>()

const emit = defineEmits<{
  (e: 'navigate', index: number): void
}>()

function isCompleted(section: RuntimeSectionVO): boolean {
  return section.items.every(item => !item.isRequired || props.answeredItemCodes.includes(item.itemCode))
}

function getStatus(section: RuntimeSectionVO, index: number): 'wait' | 'process' | 'finish' {
  if (index === props.currentIndex) {
    return 'process'
  }
  return isCompleted(section) ? 'finish' : 'wait'
}

function handleUpdateCurrent(value: number) {
  emit('navigate', value - 1)
}
</script>

<template>
  <NSteps
    :current="currentIndex + 1"
    size="small"
    style="padding: 0 8px;"
    @update:current="handleUpdateCurrent"
  >
    <NStep
      v-for="(section, idx) in sections"
      :key="section.sectionCode"
      :title="section.sectionTitle || `第${idx + 1}节`"
      :status="getStatus(section, idx)"
    />
  </NSteps>
</template>
