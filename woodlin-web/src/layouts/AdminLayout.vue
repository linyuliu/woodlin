<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {NLayout} from 'naive-ui'
import AppSidebar from './components/AppSidebar.vue'
import AppHeader from './components/AppHeader.vue'
import AppContent from './components/AppContent.vue'
import {generateMenuFromRoutes} from '@/utils/menu-generator'
import {useAppStore, useAuthStore, usePermissionStore} from '@/stores'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

// Use app store for collapsed state
const collapsed = computed({
  get: () => appStore.sidebarCollapsed,
  set: (value) => appStore.setSidebarCollapsed(value)
})

const menuOptions = computed(() => {
  // 优先使用 store 中的 menuRoutes（已过滤 hideInMenu），否则回退到 addedRoutes
  const menuSource =
    (permissionStore.menuRoutes?.length && permissionStore.menuRoutes) ||
    permissionStore.addedRoutes[0]?.children ||
    []

  return generateMenuFromRoutes(menuSource)
})

const activeKey = computed(() => (route.meta?.activeMenu as string) || route.fullPath || route.path)

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
  background-color: var(--bg-color-secondary);
  transition: background-color var(--transition-normal);
}
</style>
