<!--
  @file WUpload/index.vue
  @description 通用上传组件：统一上传按钮与 URL 值回填
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  NButton,
  NUpload,
  type UploadFileInfo,
  type UploadProps,
} from 'naive-ui'

type UploadResponsePayload = {
  data?: string | { url?: string; fullUrl?: string }
  url?: string
}

const props = withDefaults(defineProps<{
  /** 仅维护上传结果 URL 列表 */
  modelValue?: string[]
  /** 上传地址 */
  action: string
  /** 请求头 */
  headers?: Record<string, string>
  /** 是否允许多文件 */
  multiple?: boolean
  /** 最大文件数 */
  max?: number
  /** 接受的文件类型 */
  accept?: string
  /** 按钮文案 */
  buttonText?: string
}>(), {
  modelValue: () => [],
  headers: () => ({}),
  multiple: false,
  max: 1,
  accept: '*',
  buttonText: '上传文件',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const internalFileList = ref<UploadFileInfo[]>([])

const limitedFileList = computed(() => internalFileList.value.slice(0, props.max))

function toUploadFiles(urls: string[]): UploadFileInfo[] {
  return urls.map((url, index) => ({
    id: `${index}-${url}`,
    name: url.split('/').pop() ?? `file-${index + 1}`,
    status: 'finished',
    url,
  }))
}

function firstString(...values: Array<string | undefined>): string | null {
  return values.find((value) => typeof value === 'string' && value.length > 0) ?? null
}

function extractUrlFromPayload(payload: UploadResponsePayload | undefined): string | null {
  const dataPayload = typeof payload?.data === 'object' && payload?.data ? payload.data : undefined
  return firstString(
    typeof payload?.data === 'string' ? payload.data : undefined,
    dataPayload?.url,
    dataPayload?.fullUrl,
    payload?.url,
  )
}

function extractUrl(file: UploadFileInfo): string | null {
  if (typeof file.url === 'string' && file.url) {
    return file.url
  }
  const responseCarrier = file as UploadFileInfo & { response?: UploadResponsePayload }
  return extractUrlFromPayload(responseCarrier.response)
}

function syncValue(): void {
  emit(
    'update:modelValue',
    limitedFileList.value
      .map(extractUrl)
      .filter((url): url is string => !!url),
  )
}

const handleUpdate: UploadProps['onUpdate:fileList'] = (fileList) => {
  internalFileList.value = fileList
  syncValue()
}

watch(
  () => props.modelValue,
  (value) => {
    internalFileList.value = toUploadFiles(value ?? [])
  },
  { immediate: true },
)
</script>

<template>
  <NUpload
    :action="action"
    :headers="headers"
    :multiple="multiple"
    :max="max"
    :accept="accept"
    :default-upload="true"
    :file-list="limitedFileList"
    @update:file-list="handleUpdate"
  >
    <NButton>{{ buttonText }}</NButton>
  </NUpload>
</template>
