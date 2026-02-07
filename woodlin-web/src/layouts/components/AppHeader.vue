<script setup lang="ts">
import {computed, h} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {
  NAvatar,
  NBreadcrumb,
  NBreadcrumbItem,
  NButton,
  NDropdown,
  NIcon,
  NLayoutHeader,
  NTag,
  NTooltip,
  type DropdownOption
} from 'naive-ui'
import {
  MenuOutline,
  MoonOutline,
  NotificationsOutline,
  PersonCircleOutline,
  SettingsOutline,
  SunnyOutline
} from '@vicons/ionicons5'
import {useAppStore, useUserStore} from '@/stores'

const props = defineProps<{
  envLabel: string
  breadcrumbs: Array<{ title: string; path?: string }>
}>()

const emit = defineEmits<{ (e: 'toggle-collapse'): void; (e: 'logout'): void }>()

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const {isDarkMode} = storeToRefs(appStore)

const userInfo = computed(() => userStore.userInfo)
const userInitial = computed(() => {
  const name = userInfo.value?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const dropdownOptions: DropdownOption[] = [
  {label: '个人中心', key: 'profile', icon: () => h(NIcon, null, { default: () => h(PersonCircleOutline) })},
  {label: '修改密码', key: 'change-password', icon: () => h(NIcon, null, { default: () => h(SettingsOutline) })},
  { type: 'divider', key: 'divider' },
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, null, { default: () => h(MenuOutline) }) }
]

const handleDropdownSelect = (key: string) => {
  if (key === 'logout') {
    emit('logout')
    return
  }

  if (key === 'profile') {
    router.push('/profile').catch(() => {})
  }
}

const breadcrumbItems = computed(() => props.breadcrumbs)

const handleToggleTheme = () => {
  appStore.toggleThemeMode()
}
</script>

<template>
  <NLayoutHeader class="app-header">
    <div class="header-left">
      <NTooltip placement="bottom" trigger="hover">
        <template #trigger>
          <NButton class="collapse-btn" text @click="emit('toggle-collapse')">
            <template #icon>
              <NIcon color="var(--text-color-secondary)" size="20">
                <MenuOutline/>
              </NIcon>
            </template>
          </NButton>
        </template>
        切换侧边栏
      </NTooltip>

      <NBreadcrumb class="breadcrumb">
        <NBreadcrumbItem v-for="(item, index) in breadcrumbItems" :key="item.title">
          <span :class="{ 'is-last': index === breadcrumbItems.length - 1 }"
                class="breadcrumb-text">
            {{ item.title }}
          </span>
        </NBreadcrumbItem>
      </NBreadcrumb>
    </div>

    <div class="header-right">
      <NTooltip placement="bottom" trigger="hover">
        <template #trigger>
          <NButton class="icon-btn" text @click="handleToggleTheme">
            <template #icon>
              <NIcon color="var(--text-color-secondary)" size="20">
                <SunnyOutline v-if="isDarkMode"/>
                <MoonOutline v-else/>
              </NIcon>
            </template>
          </NButton>
        </template>
        {{ isDarkMode ? '切换浅色模式' : '切换深色模式' }}
      </NTooltip>

      <!-- 环境标签 -->
      <NTag
        :bordered="false"
        :type="envLabel === '生产环境' ? 'error' : 'success'"
        class="env-tag"
        size="small"
      >
        {{ envLabel }}
      </NTag>

      <!-- 通知图标 -->
      <NTooltip placement="bottom" trigger="hover">
        <template #trigger>
          <NButton class="icon-btn" text>
            <template #icon>
              <NIcon color="var(--text-color-secondary)" size="20">
                <NotificationsOutline/>
              </NIcon>
            </template>
          </NButton>
        </template>
        通知
      </NTooltip>

      <!-- 用户下拉菜单 -->
      <NDropdown
        :options="dropdownOptions"
        placement="bottom-end"
        trigger="hover"
        @select="handleDropdownSelect"
      >
        <div class="user-info">
          <NAvatar
            :style="{
              backgroundColor: 'var(--primary-color)',
              fontSize: '14px',
              fontWeight: '600'
            }"
            round
            size="small"
          >
            {{ userInitial }}
          </NAvatar>
          <div class="user-detail">
            <span class="user-name">{{ userInfo?.username || '管理员' }}</span>
            <span class="user-role">{{ userInfo?.email || '' }}</span>
          </div>
          <NIcon class="arrow-icon" color="var(--text-color-tertiary)" size="14">
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
            </svg>
          </NIcon>
        </div>
      </NDropdown>
    </div>
  </NLayoutHeader>
</template>

<style scoped>
.app-header {
  height: var(--header-height);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color-light);
  position: sticky;
  top: 0;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--bg-color-tertiary);
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.breadcrumb-text {
  font-size: var(--font-size-base);
  color: var(--text-color-tertiary);
  transition: color var(--transition-fast);
}

.breadcrumb-text.is-last {
  color: var(--text-color-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.env-tag {
  font-size: 11px;
  padding: 0 8px;
  height: 22px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.icon-btn:hover {
  background: var(--bg-color-tertiary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background var(--transition-fast);
  background: var(--bg-color-tertiary);
}

.user-info:hover {
  background: var(--primary-color-light);
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: var(--text-color-tertiary);
  line-height: 1.2;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  transition: transform var(--transition-fast);
}

.user-info:hover .arrow-icon {
  transform: rotate(180deg);
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .user-detail {
    display: none;
  }

  .user-info {
    padding: 4px;
  }

  .arrow-icon {
    display: none;
  }

  .env-tag {
    display: none;
  }
}
</style>
