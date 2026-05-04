<!--
  @file views/user/message/index.vue
  @description 消息中心：分类标签 + 分页列表 + 标记已读
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import {
  NBadge,
  NButton,
  NCard,
  NEmpty,
  NList,
  NListItem,
  NPagination,
  NSpace,
  NTabPane,
  NTabs,
  NThing,
  useMessage,
} from 'naive-ui'
import { pageNotices, readNotice, type SysNotice } from '@/api/system/notice'

type TabKey = '' | '1' | '2' | '3'

const message = useMessage()
const activeTab = ref<TabKey>('')
const loading = ref(false)
const list = ref<SysNotice[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function loadList(): Promise<void> {
  loading.value = true
  try {
    const res = await pageNotices({
      page: page.value,
      size: size.value,
      noticeType: activeTab.value || undefined,
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function handleRead(item: SysNotice): Promise<void> {
  if (!item.id || item.status === '1') return
  await readNotice(item.id)
  item.status = '1'
  message.success('已标记为已读')
}

function truncate(text: string | undefined, len = 80): string {
  if (!text) return ''
  return text.length > len ? `${text.slice(0, len)}…` : text
}

watch(activeTab, () => {
  page.value = 1
  void loadList()
})

onMounted(loadList)
</script>

<template>
  <div class="message">
    <NCard>
      <NTabs v-model:value="activeTab" type="line" animated>
        <NTabPane name="" tab="全部" />
        <NTabPane name="1" tab="系统消息" />
        <NTabPane name="2" tab="告警消息" />
        <NTabPane name="3" tab="待办事项" />
      </NTabs>

      <NList v-if="list.length" bordered hoverable clickable>
        <NListItem v-for="item in list" :key="item.id">
          <NThing :title="item.noticeTitle" :description="truncate(item.noticeContent)">
            <template #header-extra>
              <NBadge
                :type="item.status === '1' ? 'default' : 'error'"
                :value="item.status === '1' ? '已读' : '未读'"
              />
            </template>
            <template #footer>
              <NSpace align="center" justify="space-between">
                <span class="message__time">{{ item.createTime }}</span>
                <NButton
                  v-if="item.status !== '1'"
                  size="tiny"
                  type="primary"
                  @click="handleRead(item)"
                >
                  标记已读
                </NButton>
              </NSpace>
            </template>
          </NThing>
        </NListItem>
      </NList>
      <NEmpty v-else-if="!loading" description="暂无消息" style="margin: 32px 0" />

      <div class="message__pager">
        <NPagination
          v-model:page="page"
          v-model:page-size="size"
          :item-count="total"
          show-size-picker
          :page-sizes="[10, 20, 50]"
          @update:page="loadList"
          @update:page-size="loadList"
        />
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.message {
  padding: 16px;
}
.message__time {
  color: #8c95a6;
  font-size: 12px;
}
.message__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
