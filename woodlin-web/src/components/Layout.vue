<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NMenu, NBreadcrumb, NBreadcrumbItem, NButton, NIcon } from 'naive-ui'
import { LogOutOutline, MenuOutline } from '@vicons/ionicons5'
import type { MenuOption } from 'naive-ui'
import { useRouter } from 'vue-router'

const router = useRouter()
const collapsed = ref(false)

const menuOptions: MenuOption[] = [
  {
    label: '系统管理',
    key: 'system',
    children: [
      {
        label: '用户管理',
        key: 'user',
        icon: () => '👤'
      },
      {
        label: '角色管理', 
        key: 'role',
        icon: () => '🔐'
      },
      {
        label: '部门管理',
        key: 'dept',
        icon: () => '🏢'
      }
    ]
  },
  {
    label: '租户管理',
    key: 'tenant',
    children: [
      {
        label: '租户列表',
        key: 'tenant-list',
        icon: () => '🏘️'
      }
    ]
  }
]

const handleMenuSelect = (key: string) => {
  router.push(`/${key}`)
}

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const logout = () => {
  // TODO: 实现登出逻辑
  console.log('Logout')
}

onMounted(() => {
  console.log('Woodlin Admin Layout mounted')
})
</script>

<template>
  <NLayout has-sider style="height: 100vh;">
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <div class="logo-text" v-if="!collapsed">
          <h2>🌲 Woodlin</h2>
          <p>多租户管理系统</p>
        </div>
        <div v-else class="logo-collapsed">
          🌲
        </div>
      </div>
      <NMenu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        @update:value="handleMenuSelect"
      />
    </NLayoutSider>
    
    <NLayout>
      <NLayoutHeader bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
        <div style="display: flex; align-items: center; gap: 16px;">
          <NButton text @click="toggleCollapse">
            <NIcon size="18">
              <MenuOutline />
            </NIcon>
          </NButton>
          <NBreadcrumb>
            <NBreadcrumbItem>首页</NBreadcrumbItem>
            <NBreadcrumbItem>系统管理</NBreadcrumbItem>
          </NBreadcrumb>
        </div>
        
        <div style="display: flex; align-items: center; gap: 16px;">
          <span>Admin</span>
          <NButton text @click="logout">
            <NIcon>
              <LogOutOutline />
            </NIcon>
          </NButton>
        </div>
      </NLayoutHeader>
      
      <NLayoutContent style="padding: 24px;">
        <router-view />
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-color);
}

.logo-text {
  text-align: center;
}

.logo-text h2 {
  margin: 0;
  font-size: 18px;
  color: #18a058;
}

.logo-text p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.logo-collapsed {
  font-size: 24px;
}
</style>