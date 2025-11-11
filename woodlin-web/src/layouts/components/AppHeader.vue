<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NLayoutHeader,
  NButton,
  NIcon,
  NBreadcrumb,
  NBreadcrumbItem,
  NSpace,
  NTag,
  NDropdown,
  NAvatar
} from 'naive-ui'
import { MenuOutline } from '@vicons/ionicons5'

const props = defineProps<{
  envLabel: string
  breadcrumbs: Array<{ title: string; path?: string }>
}>()

const emit = defineEmits<{ (e: 'toggle-collapse'): void; (e: 'logout'): void }>()

const router = useRouter()

const dropdownOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '修改密码', key: 'change-password' },
  { type: 'divider', key: 'divider' },
  { label: '退出登录', key: 'logout' }
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
</script>

<template>
  <NLayoutHeader bordered class="app-header">
    <div class="header-left">
      <NButton text class="collapse-btn" @click="emit('toggle-collapse')">
        <template #icon>
          <NIcon size="20">
            <MenuOutline />
          </NIcon>
        </template>
      </NButton>

      <NBreadcrumb>
        <NBreadcrumbItem v-for="item in breadcrumbItems" :key="item.title">
          <span class="breadcrumb-text">{{ item.title }}</span>
        </NBreadcrumbItem>
      </NBreadcrumb>
    </div>

    <NSpace :size="16" align="center">
      <NTag size="small" type="success" :bordered="false">{{ envLabel }}</NTag>

      <NDropdown :options="dropdownOptions" trigger="hover" @select="handleDropdownSelect">
        <div class="user-info">
          <NAvatar size="small" round :style="{ backgroundColor: '#18a058' }">A</NAvatar>
          <span class="user-name">管理员</span>
        </div>
      </NDropdown>
    </NSpace>
  </NLayoutHeader>
</template>

<style scoped>
.app-header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 6px rgba(15, 23, 42, 0.08);
  position: sticky;
  top: 0;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.collapse-btn {
  font-size: 20px;
}

.breadcrumb-text {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(24, 160, 88, 0.08);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .user-name {
    display: none;
  }
}
</style>
