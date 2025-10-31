<script setup lang="ts">
/**
 * Woodlin后台管理系统 - 主布局组件
 * 
 * @author mumu
 * @description 基于Naive UI的管理系统布局，参考sz-admin设计模式
 * @since 2025-01-01
 */
import { ref, onMounted, computed, h } from 'vue'
import { 
  NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, 
  NMenu, NBreadcrumb, NBreadcrumbItem, NButton, NIcon,
  NDropdown, NAvatar, NSpace, NTag,
  type MenuOption
} from 'naive-ui'
import { 
  LogOutOutline, 
  MenuOutline,
  HomeOutline,
  PeopleOutline,
  ShieldCheckmarkOutline,
  BusinessOutline,
  SettingsOutline,
  AppsOutline
} from '@vicons/ionicons5'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

/**
 * 环境标签
 */
const envLabel = import.meta.env.MODE === 'production' ? '生产环境' : '开发环境'

/**
 * 渲染图标组件
 */
const renderIcon = (icon: any) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

/**
 * 菜单选项配置
 */
const menuOptions: MenuOption[] = [
  {
    label: '仪表板',
    key: 'dashboard',
    icon: renderIcon(HomeOutline)
  },
  {
    label: '系统管理',
    key: 'system',
    icon: renderIcon(AppsOutline),
    children: [
      {
        label: '用户管理',
        key: 'user',
        icon: renderIcon(PeopleOutline)
      },
      {
        label: '角色管理', 
        key: 'role',
        icon: renderIcon(ShieldCheckmarkOutline)
      },
      {
        label: '部门管理',
        key: 'dept',
        icon: renderIcon(BusinessOutline)
      },
      {
        label: '系统设置',
        key: 'system-settings',
        icon: renderIcon(SettingsOutline)
      }
    ]
  },
  {
    label: '租户管理',
    key: 'tenant',
    icon: renderIcon(BusinessOutline),
    children: [
      {
        label: '租户列表',
        key: 'tenant-list',
        icon: renderIcon(AppsOutline)
      }
    ]
  }
]

/**
 * 当前激活的菜单项
 */
const activeKey = computed(() => {
  const path = route.path.substring(1) // 移除开头的 '/'
  return path || 'dashboard'
})

/**
 * 菜单选择处理
 */
const handleMenuSelect = (key: string) => {
  router.push(`/${key}`)
}

/**
 * 切换侧边栏折叠状态
 */
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

/**
 * 用户下拉菜单选项
 */
const userDropdownOptions = [
  {
    label: '个人中心',
    key: 'profile'
  },
  {
    label: '修改密码',
    key: 'change-password'
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout'
  }
]

/**
 * 用户下拉菜单选择处理
 */
const handleUserDropdown = (key: string) => {
  if (key === 'logout') {
    logout()
  } else if (key === 'profile') {
    console.log('前往个人中心')
  } else if (key === 'change-password') {
    console.log('修改密码')
  }
}

/**
 * 用户登出
 */
const logout = () => {
  // 清除token
  localStorage.removeItem('token')
  // 跳转到登录页
  router.push('/login')
}

onMounted(() => {
  console.log('Woodlin Admin Layout 已加载')
})
</script>

<template>
  <NLayout has-sider class="admin-layout">
    <!-- 左侧边栏 -->
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      :native-scrollbar="false"
      class="admin-sider"
    >
      <!-- Logo区域 -->
      <div class="logo-container">
        <transition name="logo-fade" mode="out-in">
          <div v-if="!collapsed" class="logo-expanded">
            <div class="logo-icon">🌲</div>
            <div class="logo-text">
              <h2>Woodlin</h2>
              <p>多租户管理系统</p>
            </div>
          </div>
          <div v-else class="logo-collapsed">
            <div class="logo-icon">🌲</div>
          </div>
        </transition>
      </div>
      
      <!-- 菜单 -->
      <NMenu
        :value="activeKey"
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :indent="24"
        :options="menuOptions"
        @update:value="handleMenuSelect"
      />
    </NLayoutSider>
    
    <!-- 主内容区 -->
    <NLayout>
      <!-- 顶部导航栏 -->
      <NLayoutHeader bordered class="admin-header">
        <div class="header-left">
          <NButton text class="collapse-btn" @click="toggleCollapse">
            <template #icon>
              <NIcon size="20">
                <MenuOutline />
              </NIcon>
            </template>
          </NButton>
          <NBreadcrumb>
            <NBreadcrumbItem>{{ route.meta.title || '首页' }}</NBreadcrumbItem>
          </NBreadcrumb>
        </div>
        
        <div class="header-right">
          <NSpace :size="16">
            <!-- 环境标签 -->
            <NTag type="success" size="small" :bordered="false">
              {{ envLabel }}
            </NTag>
            
            <!-- 用户信息 -->
            <NDropdown 
              :options="userDropdownOptions" 
              @select="handleUserDropdown"
              placement="bottom-end"
            >
              <div class="user-info">
                <NAvatar 
                  round 
                  size="small" 
                  :style="{ background: '#18a058' }"
                >
                  Admin
                </NAvatar>
                <span class="user-name">管理员</span>
              </div>
            </NDropdown>
          </NSpace>
        </div>
      </NLayoutHeader>
      
      <!-- 内容区域 -->
      <NLayoutContent class="admin-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
/* ===== 布局容器样式 ===== */
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧边栏样式 ===== */
.admin-sider {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 999;
}

/* Logo容器 */
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 16px;
  background: #fff;
  position: relative;
  overflow: hidden;
}

/* Logo展开状态 */
.logo-expanded {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
  flex-shrink: 0;
}

.logo-text {
  flex: 1;
  min-width: 0;
}

.logo-text h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #18a058;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-text p {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Logo折叠状态 */
.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.logo-collapsed .logo-icon {
  font-size: 32px;
}

/* Logo切换动画 */
.logo-fade-enter-active,
.logo-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.logo-fade-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.logo-fade-leave-to {
  opacity: 0;
  transform: scale(1.1);
}

/* ===== 顶部导航栏样式 ===== */
.admin-header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 998;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.collapse-btn {
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* ===== 内容区域样式 ===== */
.admin-content {
  height: calc(100vh - 64px);
  overflow: auto;
  background: #f5f5f5;
}

.content-wrapper {
  padding: 24px;
  min-height: 100%;
}

/* ===== 页面切换动画 ===== */
.fade-slide-enter-active {
  transition: all 0.3s ease-out;
}

.fade-slide-leave-active {
  transition: all 0.2s ease-in;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 16px;
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .user-name {
    display: none;
  }
}
</style>