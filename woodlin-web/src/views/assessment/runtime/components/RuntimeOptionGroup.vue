<script setup lang="ts">
import { computed } from 'vue'
import { NCheckbox, NCheckboxGroup, NInput, NRadio, NRadioGroup, NRate, NSpace } from 'naive-ui'
import type { RuntimeOptionVO } from '@/api/assessment'

const props = defineProps<{
  itemCode: string
  itemType: string
  options: RuntimeOptionVO[]
  modelValue: string[] | string | null
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[] | string | null): void
  (e: 'change', itemCode: string, value: string[] | string | null): void
}>()

const isSingleChoice = computed(() => props.itemType === 'single_choice')
const isMultipleChoice = computed(() => props.itemType === 'multiple_choice')
const isRating = computed(() => props.itemType === 'rating')
const isText = computed(() => props.itemType === 'short_text' || props.itemType === 'text')

const singleValue = computed({
  get: () => (Array.isArray(props.modelValue) ? props.modelValue[0] : props.modelValue) as string,
  set: (value: string) => {
    emit('update:modelValue', value)
    emit('change', props.itemCode, value)
  }
})

const multiValue = computed({
  get: () => (Array.isArray(props.modelValue) ? props.modelValue : []) as string[],
  set: (value: string[]) => {
    emit('update:modelValue', value)
    emit('change', props.itemCode, value)
  }
})

const textValue = computed({
  get: () => (typeof props.modelValue === 'string' ? props.modelValue : '') as string,
  set: (value: string) => {
    emit('update:modelValue', value)
    emit('change', props.itemCode, value)
  }
})

const ratingValue = computed({
  get: () => {
    const value = Array.isArray(props.modelValue) ? props.modelValue[0] : props.modelValue
    return value ? Number(value) : 0
  },
  set: (value: number) => {
    const stringValue = String(value)
    emit('update:modelValue', stringValue)
    emit('change', props.itemCode, stringValue)
  }
})
</script>

<template>
  <div class="option-group">
    <NRadioGroup v-if="isSingleChoice" v-model:value="singleValue" :disabled="disabled">
      <NSpace vertical>
        <NRadio v-for="opt in options" :key="opt.optionCode" :value="opt.optionCode">
          {{ opt.displayText }}
        </NRadio>
      </NSpace>
    </NRadioGroup>

    <NCheckboxGroup v-else-if="isMultipleChoice" v-model:value="multiValue" :disabled="disabled">
      <NSpace vertical>
        <NCheckbox v-for="opt in options" :key="opt.optionCode" :value="opt.optionCode">
          {{ opt.displayText }}
        </NCheckbox>
      </NSpace>
    </NCheckboxGroup>

    <NRate v-else-if="isRating" v-model:value="ratingValue" :count="options.length || 5" :disabled="disabled" />

    <NInput
      v-else-if="isText"
      v-model:value="textValue"
      type="textarea"
      :rows="3"
      :disabled="disabled"
      placeholder="请输入您的回答..."
    />

    <NSpace v-else vertical>
      <NRadio
        v-for="opt in options"
        :key="opt.optionCode"
        :value="opt.optionCode"
        :checked="singleValue === opt.optionCode"
        :disabled="disabled"
        @change="singleValue = opt.optionCode"
      >
        {{ opt.displayText }}
      </NRadio>
    </NSpace>
  </div>
</template>

<style scoped>
.option-group {
  padding: 8px 0;
}
</style>
