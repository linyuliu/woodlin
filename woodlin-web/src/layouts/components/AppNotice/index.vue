<!--
  @file layouts/components/AppNotice/index.vue
  @description 顶部通知中心：展示最近未读消息，支持快速进入消息中心与标记已读
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NBadge,
  NButton,
  NCard,
  NEmpty,
  NList,
  NListItem,
  NPopover,
  NSpace,
  NThing,
} from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { pageNotices, readNotice, type SysNotice } from '@/api/system/notice'

const router = useRouter()
const notices = ref<SysNotice[]>([])
const loading = ref(false)

const unreadCount = computed(() => notices.value.filter((item) => item.status !== '1').length)

async function loadNotices(): Promise<void> {
  loading.value = true
  try {
    const res = await pageNotices({ page: 1, size: 6, status: '0' })
    notices.value = res.records ?? []
  } finally {
    loading.value = false
  }
}

async function handleRead(item: SysNotice): Promise<void> {
  if (!item.id || item.status === '1') {return}
  await readNotice(item.id)
  item.status = '1'
}

function goMessageCenter(): void {
  void router.push('/user/message')
}

onMounted(() => {
  void loadNotices()
})
</script>

<template>
  <NPopover trigger="click" placement="bottom-end" :width="360">
    <template #trigger>
      <NButton quaternary circle>
        <template #icon>
          <NBadge :value="unreadCount || undefined" :max="99">
            <WIcon icon="vicons:antd:BellOutlined" />
          </NBadge>
        </template>
      </NButton>
    </template>

    <NCard title="通知中心" size="small" :bordered="false">
      <NList v-if="notices.length > 0" hoverable clickable>
        <NListItem v-for="item in notices" :key="item.id">
          <NThing :title="item.noticeTitle" :description="item.noticeContent || '暂无内容'">
            <template #footer>
              <NSpace justify="space-between">
                <span>{{ item.createTime }}</span>
                <NButton v-if="item.status !== '1'" text type="primary" size="tiny" @click="handleRead(item)">
                  标记已读
                </NButton>
              </NSpace>
            </template>
          </NThing>
        </NListItem>
      </NList>
      <NEmpty v-else-if="!loading" description="暂无未读通知" />
      <div class="notice-footer">
        <NButton block tertiary @click="goMessageCenter">进入消息中心</NButton>
      </div>
    </NCard>
  </NPopover>
</template>

<style scoped>
.notice-footer {
  margin-top: 12px;
}
</style>
