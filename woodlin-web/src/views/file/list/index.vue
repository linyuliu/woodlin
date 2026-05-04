<!--
  @file views/file/list/index.vue
  @description 文件管理列表：支持表格/网格视图、多文件上传、预览、分享、下载、删除
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NImage,
  NImageGroup,
  NInput,
  NInputNumber,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTabPane,
  NTabs,
  NTag,
  NUpload,
  useDialog,
  useMessage,
  type DataTableColumns,
  type SelectOption,
  type UploadFileInfo,
} from 'naive-ui'
import {
  deleteFile,
  getDownloadUrl,
  getUploadUrl,
  pageFiles,
  shareFile,
  type FileInfo,
  type FileQuery,
} from '@/api/file'
import { formatDate, formatSize } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'
import { getToken } from '@/utils/auth'
import { STORAGE_KEYS } from '@/constants'

const message = useMessage()
const dialog = useDialog()

const tableData: Ref<FileInfo[]> = ref([])
const loading = ref(false)
const total = ref(0)
const viewMode = ref<'list' | 'grid'>('list')

const query = reactive<FileQuery>({
  page: 1,
  size: 20,
  fileName: '',
  fileType: undefined,
  storageId: undefined,
})

const fileTypeOptions: SelectOption[] = [
  { label: '图片', value: 'image' },
  { label: '视频', value: 'video' },
  { label: '文档', value: 'doc' },
  { label: '其他', value: 'other' },
]

const fileTypeColor: Record<string, 'info' | 'success' | 'warning' | 'error' | 'default'> = {
  image: 'success',
  video: 'info',
  doc: 'warning',
  other: 'default',
}

const fileTypeLabel: Record<string, string> = {
  image: '图片',
  video: '视频',
  doc: '文档',
  other: '其他',
}

const uploadHeaders = computed<Record<string, string>>(() => {
  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.TENANT)
    if (raw) {
      const parsed = JSON.parse(raw) as { tenantId?: string }
      if (parsed?.tenantId) headers['X-Tenant-Id'] = parsed.tenantId
    }
  } catch {
    /* ignore */
  }
  return headers
})

const uploadAction = getUploadUrl()
const MAX_SIZE = 100 * 1024 * 1024

async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await pageFiles(query)
    tableData.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.page = 1
  void refresh()
}

function handleReset(): void {
  query.fileName = ''
  query.fileType = undefined
  query.storageId = undefined
  query.page = 1
  void refresh()
}

function beforeUpload(data: { file: UploadFileInfo }): boolean {
  const f = data.file.file
  if (f && f.size > MAX_SIZE) {
    message.error(`${data.file.name} 超过 100MB 限制`)
    return false
  }
  return true
}

function handleUploadFinish({ file }: { file: UploadFileInfo }): UploadFileInfo {
  message.success(`${file.name} 上传成功`)
  void refresh()
  return file
}

function handleUploadError({ file }: { file: UploadFileInfo }): UploadFileInfo {
  message.error(`${file.name} 上传失败`)
  return file
}

function isImage(row: FileInfo): boolean {
  return row.fileType === 'image'
}

function rowKey(row: FileInfo): number | string {
  return (row.id ?? row.fileId) as number | string
}

function handlePreview(row: FileInfo): void {
  if (isImage(row) && row.fileUrl) {
    window.open(row.fileUrl, '_blank')
    return
  }
  handleDownload(row)
}

function handleDownload(row: FileInfo): void {
  const id = row.id ?? row.fileId
  if (id == null) return
  downloadByUrl(getDownloadUrl(id), row.fileName)
}

function handleDelete(row: FileInfo): void {
  const id = row.id ?? row.fileId
  if (id == null) return
  dialog.warning({
    title: '提示',
    content: `确认删除文件 ${row.fileName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteFile(id)
      message.success('删除成功')
      void refresh()
    },
  })
}

const shareVisible = ref(false)
const shareSubmitting = ref(false)
const shareTarget = ref<FileInfo | null>(null)
const shareExpireDays = ref<number>(7)
const shareUrl = ref('')

function openShare(row: FileInfo): void {
  shareTarget.value = row
  shareExpireDays.value = 7
  shareUrl.value = ''
  shareVisible.value = true
}

async function submitShare(): Promise<void> {
  const id = shareTarget.value?.id ?? shareTarget.value?.fileId
  if (id == null) return
  shareSubmitting.value = true
  try {
    const res = await shareFile(id, shareExpireDays.value)
    shareUrl.value = res?.shareUrl ?? ''
    message.success('分享链接已生成')
  } finally {
    shareSubmitting.value = false
  }
}

async function copyShareUrl(): Promise<void> {
  if (!shareUrl.value) return
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    message.success('已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

const columns: DataTableColumns<FileInfo> = [
  { title: '文件名', key: 'fileName', minWidth: 220, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'fileType',
    width: 100,
    render: (row) =>
      h(
        NTag,
        { size: 'small', type: fileTypeColor[row.fileType ?? 'other'] ?? 'default' },
        { default: () => fileTypeLabel[row.fileType ?? 'other'] ?? row.fileType ?? '-' },
      ),
  },
  {
    title: '大小',
    key: 'fileSize',
    width: 120,
    render: (row) => formatSize(row.fileSize ?? 0),
  },
  { title: '存储', key: 'storageName', width: 140, render: (row) => row.storageName ?? row.storageId ?? '-' },
  {
    title: '上传时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDate(row.createTime),
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => handlePreview(row) },
          { default: () => (isImage(row) ? '预览' : '下载') },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openShare(row) },
          { default: () => '分享' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(
                NButton,
                { size: 'small', text: true, type: 'error' },
                { default: () => '删除' },
              ),
          },
        ),
      ]),
  },
]

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-file-list">
    <n-card size="small">
      <n-form inline label-placement="left" :model="query">
        <n-form-item label="文件名">
          <n-input v-model:value="query.fileName" placeholder="文件名" clearable />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="query.fileType"
            :options="fileTypeOptions"
            placeholder="类型"
            clearable
            style="min-width: 140px"
          />
        </n-form-item>
        <n-form-item label="存储ID">
          <n-input-number
            v-model:value="query.storageId"
            placeholder="存储ID"
            clearable
            :show-button="false"
            style="width: 140px"
          />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handleSearch">查询</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card size="small">
      <div class="toolbar">
        <n-upload
          multiple
          :action="uploadAction"
          :headers="uploadHeaders"
          name="file"
          :max="20"
          :show-file-list="true"
          @before-upload="beforeUpload"
          @finish="handleUploadFinish"
          @error="handleUploadError"
        >
          <n-button type="primary">上传文件</n-button>
        </n-upload>
        <n-tabs v-model:value="viewMode" type="segment" size="small" class="view-toggle">
          <n-tab-pane name="list" tab="列表" />
          <n-tab-pane name="grid" tab="网格" />
        </n-tabs>
      </div>

      <template v-if="viewMode === 'list'">
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :row-key="rowKey"
          :scroll-x="1100"
          striped
        />
      </template>

      <template v-else>
        <div v-if="!tableData.length && !loading" class="grid-empty">
          <n-empty description="暂无文件" />
        </div>
        <n-image-group v-else>
          <div class="grid">
            <div v-for="row in tableData" :key="String(rowKey(row))" class="grid-item">
              <div class="grid-thumb">
                <n-image
                  v-if="isImage(row) && row.fileUrl"
                  :src="row.fileUrl"
                  object-fit="cover"
                  width="160"
                  height="120"
                />
                <div v-else class="grid-thumb-fallback">
                  <n-tag :type="fileTypeColor[row.fileType ?? 'other'] ?? 'default'">
                    {{ fileTypeLabel[row.fileType ?? 'other'] ?? row.fileType ?? '-' }}
                  </n-tag>
                </div>
              </div>
              <div class="grid-name" :title="row.fileName">{{ row.fileName }}</div>
              <div class="grid-meta">{{ formatSize(row.fileSize ?? 0) }}</div>
              <n-space size="small" class="grid-actions">
                <n-button size="tiny" text type="info" @click="handlePreview(row)">
                  {{ isImage(row) ? '预览' : '下载' }}
                </n-button>
                <n-button size="tiny" text type="primary" @click="openShare(row)">分享</n-button>
                <n-button size="tiny" text type="error" @click="handleDelete(row)">删除</n-button>
              </n-space>
            </div>
          </div>
        </n-image-group>
      </template>

      <div class="pagination">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.size"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="refresh"
          @update:page-size="refresh"
        />
      </div>
    </n-card>

    <n-modal
      v-model:show="shareVisible"
      preset="card"
      title="生成分享链接"
      style="width: 520px"
      :bordered="false"
    >
      <n-form label-placement="top">
        <n-form-item label="文件">
          <span>{{ shareTarget?.fileName }}</span>
        </n-form-item>
        <n-form-item label="有效天数">
          <n-input-number v-model:value="shareExpireDays" :min="1" :max="365" style="width: 100%" />
        </n-form-item>
        <n-form-item v-if="shareUrl" label="分享链接">
          <n-input :value="shareUrl" readonly>
            <template #suffix>
              <n-button size="tiny" text type="primary" @click="copyShareUrl">复制</n-button>
            </template>
          </n-input>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="shareVisible = false">关闭</n-button>
          <n-button type="primary" :loading="shareSubmitting" @click="submitShare">
            生成链接
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.page-file-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;
}
.view-toggle {
  width: 200px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}
.grid-item {
  border: 1px solid var(--n-border-color, #eee);
  border-radius: 6px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.grid-thumb {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
  overflow: hidden;
}
.grid-thumb-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
.grid-name {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.grid-meta {
  font-size: 12px;
  color: var(--n-text-color-3, #999);
}
.grid-actions {
  margin-top: auto;
}
.grid-empty {
  padding: 32px;
  display: flex;
  justify-content: center;
}
</style>
