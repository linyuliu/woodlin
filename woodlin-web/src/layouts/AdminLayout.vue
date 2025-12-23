<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout } from 'naive-ui'
import AppSidebar from './components/AppSidebar.vue'
import AppHeader from './components/AppHeader.vue'
import AppContent from './components/AppContent.vue'
import { appMenuItems, toNaiveMenuOptions } from './menu-options'
import { useAuthStore, useAppStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

// Use app store for collapsed state
const collapsed = computed({
  get: () => appStore.sidebarCollapsed,
  set: (value) => appStore.setSidebarCollapsed(value)
})

const menuOptions = toNaiveMenuOptions(appMenuItems)

const activeKey = computed(() => route.path)

const breadcrumbs = computed(() => {
  const items = route.matched
    .filter((item) => item.meta?.title)
    .map((item) => ({
      title: String(item.meta?.title),
      path: item.path
    }))

  if (!items.length && route.meta?.title) {
    items.push({ title: String(route.meta.title), path: route.path })
  }

  return items
})

const envLabel = import.meta.env.MODE === 'production' ? '生产环境' : '开发环境'

const handleMenuSelect = (key: string) => {
  router.push(key).catch(() => {})
}

const handleToggleCollapse = () => {
  appStore.toggleSidebar()
}

const handleLogout = async () => {
  await authStore.doLogout()
}
</script>

<template>
  <NLayout has-sider class="admin-layout">
    <AppSidebar :collapsed="collapsed" :menu-options="menuOptions" :value="activeKey" @select="handleMenuSelect" />

    <NLayout class="content-layout">
      <AppHeader :env-label="envLabel" :breadcrumbs="breadcrumbs" @toggle-collapse="handleToggleCollapse" @logout="handleLogout" />
      <AppContent />
    </NLayout>
  </NLayout>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.content-layout {
  background-color: #f7f9fc;
}
</style>
