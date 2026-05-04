<!--
  @file views/dashboard/workplace/index.vue
  @description 工作台 - 欢迎卡片、快捷入口、统计数据
  @author yulin
  @since 2026-06
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NGrid,
  NGridItem,
  NSpace,
  NStatistic,
  NTag,
  NText,
} from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'
import { pageOnline } from '@/api/monitor'

interface QuickLink {
  title: string
  icon: string
  path: string
  desc: string
}

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const onlineCount = ref(0)
const todoCount = ref(0)
const messageCount = ref(0)

const quickLinks: QuickLink[] = [
  { title: '用户管理', icon: '👥', path: '/system/user', desc: '管理系统用户' },
  { title: '角色管理', icon: '🛡', path: '/system/role', desc: '配置角色权限' },
  { title: '菜单管理', icon: '📋', path: '/system/menu', desc: '维护菜单结构' },
  { title: '在线用户', icon: '🟢', path: '/monitor/online', desc: '查看在线会话' },
  { title: '操作日志', icon: '📝', path: '/monitor/operLog', desc: '审计操作记录' },
  { title: '系统配置', icon: '⚙️', path: '/system/config', desc: '调整系统参数' },
]

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(
  () =>
    userStore.userInfo?.nickname
    ?? userStore.userInfo?.username
    ?? '访客',
)

const today = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const weeks = ['日', '一', '二', '三', '四', '五', '六']
  return `${y}-${m}-${day} 星期${weeks[d.getDay()]}`
})

async function loadStats(): Promise<void> {
  loading.value = true
  try {
    const res = await pageOnline({ page: 1, size: 1 })
    onlineCount.value = res?.total ?? 0
  } catch (e) {
    console.warn('[workplace] 获取在线用户数失败', e)
  } finally {
    loading.value = false
  }
}

function navigate(path: string): void {
  void router.push(path)
}

onMounted(() => {
  void loadStats()
})
</script>

<template>
  <div class="workplace">
    <NCard class="workplace__hero" :bordered="false">
      <div class="workplace__hero-inner">
        <div>
          <h2 class="workplace__hello">{{ greeting }}，{{ displayName }} 👋</h2>
          <NText depth="3">{{ today }} · 祝你今天工作顺利</NText>
        </div>
        <NSpace>
          <NTag :bordered="false" type="info" round>
            角色：{{ userStore.roles?.[0] ?? '普通用户' }}
          </NTag>
          <NTag :bordered="false" type="success" round>
            权限：{{ userStore.permissions?.length ?? 0 }} 项
          </NTag>
        </NSpace>
      </div>
    </NCard>

    <NCard title="快捷入口" class="workplace__section" :bordered="false">
      <NGrid :cols="6" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
        <NGridItem
          v-for="link in quickLinks"
          :key="link.path"
          span="6 s:3 m:2"
        >
          <div class="quick-link" @click="navigate(link.path)">
            <div class="quick-link__icon">{{ link.icon }}</div>
            <div class="quick-link__title">{{ link.title }}</div>
            <div class="quick-link__desc">{{ link.desc }}</div>
          </div>
        </NGridItem>
      </NGrid>
    </NCard>

    <NCard title="今日数据" class="workplace__section" :bordered="false">
      <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
        <NGridItem span="4 s:2 m:1">
          <NStatistic label="在线用户" :value="onlineCount">
            <template #suffix>
              <NText depth="3" style="font-size: 12px">人</NText>
            </template>
          </NStatistic>
        </NGridItem>
        <NGridItem span="4 s:2 m:1">
          <NStatistic label="待办任务" :value="todoCount" />
        </NGridItem>
        <NGridItem span="4 s:2 m:1">
          <NStatistic label="未读消息" :value="messageCount" />
        </NGridItem>
        <NGridItem span="4 s:2 m:1">
          <NStatistic label="我的角色" :value="userStore.roles?.length ?? 0" />
        </NGridItem>
      </NGrid>
    </NCard>
  </div>
</template>

<style scoped>
.workplace {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.workplace__hero {
  background: linear-gradient(135deg, #4f8bff 0%, #6f5cff 60%, #8a4fff 100%);
  color: #fff;
  border-radius: 12px;
}
.workplace__hero :deep(.n-card__content) {
  padding: 24px;
}
.workplace__hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.workplace__hello {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #fff;
}
.workplace__hero :deep(.n-text) {
  color: rgba(255, 255, 255, 0.85);
}
.workplace__section {
  border-radius: 12px;
}
.quick-link {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px;
  border-radius: 10px;
  background: var(--n-color-modal, #f7f9fc);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  border: 1px solid transparent;
}
.quick-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 139, 255, 0.18);
  border-color: rgba(79, 139, 255, 0.35);
}
.quick-link__icon {
  font-size: 28px;
  margin-bottom: 8px;
}
.quick-link__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--n-text-color, #1f2937);
}
.quick-link__desc {
  margin-top: 4px;
  font-size: 12px;
  color: #8c95a6;
}
</style>
