<!--
  @file layouts/components/UserDropdown/index.vue
  @description 顶部右侧用户头像下拉菜单
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { NAvatar, NDropdown, useDialog, type DropdownOption } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { useUserStore } from '@/stores/modules/user'
import { useRouteStore } from '@/stores/modules/route'
import { usePermissionStore } from '@/stores/modules/permission'
import { useTabsStore } from '@/stores/modules/tabs'

const router = useRouter()
const dialog = useDialog()
const userStore = useUserStore()
const routeStore = useRouteStore()
const permStore = usePermissionStore()
const tabsStore = useTabsStore()

/** 头像首字符 */
const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const options = computed<DropdownOption[]>(() => [
  {
    key: 'profile',
    label: '个人中心',
    icon: () => h(WIcon, { icon: 'vicons:antd:UserOutlined' }),
  },
  { type: 'divider', key: 'd1' },
  {
    key: 'logout',
    label: '退出登录',
    icon: () => h(WIcon, { icon: 'vicons:antd:LogoutOutlined' }),
  },
])

/** 执行登出 */
async function doLogout(): Promise<void> {
  await userStore.logout()
  routeStore.resetRoutes()
  permStore.reset()
  tabsStore.removeAllTabs()
  await router.push('/login')
}

/** 下拉项选择 */
function handleSelect(key: string): void {
  if (key === 'profile') {
    void router.push('/user/profile')
    return
  }
  if (key === 'logout') {
    dialog.warning({
      title: '提示',
      content: '确定退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: () => {
        void doLogout()
      },
    })
  }
}
</script>

<template>
  <NDropdown :options="options" trigger="click" @select="handleSelect">
    <div class="user-dropdown">
      <NAvatar
        v-if="userStore.userInfo?.avatar"
        round
        :size="32"
        :src="userStore.userInfo.avatar"
      />
      <NAvatar v-else round :size="32" color="#1677ff">
        {{ avatarText }}
      </NAvatar>
      <span class="user-dropdown__name">
        {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
      </span>
    </div>
  </NDropdown>
</template>

<style scoped>
.user-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
}
.user-dropdown__name {
  font-size: 14px;
  color: var(--w-color-text);
}
</style>
