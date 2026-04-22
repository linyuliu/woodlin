<script setup lang="ts">
import { NCard, NSpace, NTag, NText } from 'naive-ui'
import type { AnswerItemDTO, RuntimeItemVO } from '@/api/assessment'
import RuntimeOptionGroup from './RuntimeOptionGroup.vue'

const props = defineProps<{
  item: RuntimeItemVO
  index: number
  answer?: AnswerItemDTO
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'answer', itemCode: string, value: string[] | string | null, rawAnswer: string): void
}>()

function getAnswerValue(): string[] | string | null {
  if (!props.answer) {
    return null
  }
  if (props.answer.selectedOptionCodes?.length) {
    return props.answer.selectedOptionCodes
  }
  if (props.answer.textAnswer) {
    return props.answer.textAnswer
  }
  return null
}

function handleChange(itemCode: string, value: string[] | string | null) {
  let rawAnswer = 'null'

  if (Array.isArray(value) || typeof value === 'string') {
    rawAnswer = JSON.stringify(value)
  }

  emit('answer', itemCode, value, rawAnswer)
}
</script>

<template>
  <NCard class="item-card" :class="{ answered: !!answer && !answer.isSkipped }">
    <template #header>
      <NSpace align="center">
        <NText strong>{{ index + 1 }}.</NText>
        <NTag v-if="item.isRequired" type="error" size="small">必答</NTag>
        <NTag v-if="item.isAnchor" type="info" size="small">锚题</NTag>
      </NSpace>
    </template>
    <div class="item-stem" v-html="item.stem" />
    <div v-if="item.helpText" class="item-help">
      <NText depth="3" style="font-size: 13px;">{{ item.helpText }}</NText>
    </div>
    <RuntimeOptionGroup
      :item-code="item.itemCode"
      :item-type="item.itemType"
      :options="item.options"
      :model-value="getAnswerValue()"
      :disabled="disabled"
      @change="handleChange"
    />
  </NCard>
</template>

<style scoped>
.item-card {
  margin-bottom: 16px;
  border-left: 3px solid transparent;
  transition: border-color 0.2s;
}

.item-card.answered {
  border-left-color: #18a058;
}

.item-stem {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.item-help {
  margin-bottom: 8px;
}
</style>
