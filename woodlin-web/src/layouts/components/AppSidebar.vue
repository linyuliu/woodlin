<script setup lang="ts">
import {computed} from 'vue'
import {type MenuOption, NLayoutSider, NMenu} from 'naive-ui'
import logoSvg from '@/assets/logo-tree.svg'

const props = defineProps<{
  collapsed: boolean
  menuOptions: MenuOption[]
  value: string
}>()

const emit = defineEmits<{ (e: 'select', key: string): void }>()

const menuValue = computed(() => props.value)

const handleSelect = (key: string) => {
  emit('select', key)
}
</script>

<template>
  <NLayoutSider
    collapse-mode="width"
    :collapsed="collapsed"
    :collapsed-width="64"
    :width="240"
    :native-scrollbar="false"
    class="app-sider"
  >
    <div :class="{ collapsed }" class="logo">
      <div class="logo-icon">
        <img :src="logoSvg" alt="Woodlin Logo"/>
      </div>
      <transition mode="out-in" name="logo-text">
        <div v-if="!collapsed" key="expanded" class="logo-text">
          <h1>Woodlin</h1>
          <p>多租户管理系统</p>
        </div>
      </transition>
    </div>

    <NMenu
      :value="menuValue"
      :options="menuOptions"
      :collapsed="collapsed"
      :collapsed-width="64"
      :indent="24"
      :root-indent="18"
      :collapsed-icon-size="20"
      class="app-menu"
      @update:value="handleSelect"
    />

    <!-- 底部版本信息 -->
    <div class="sider-footer">
      <transition name="fade">
        <span v-if="!collapsed" class="version">v1.0.0</span>
      </transition>
    </div>
  </NLayoutSider>
</template>

<style scoped>
.app-sider {
  background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
  box-shadow: var(--shadow-md);
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-color-light);
}

/* Logo 区域 */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  min-height: var(--header-height);
  border-bottom: 1px solid var(--border-color-light);
  background: var(--bg-color);
  transition: all var(--transition-normal);
}

.logo.collapsed {
  justify-content: center;
  padding: 16px 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  transition: transform var(--transition-normal);
}

.logo-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo:hover .logo-icon {
  transform: scale(1.05);
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.logo-text h1 {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, #36ad6a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.logo-text p {
  font-size: 11px;
  color: var(--text-color-tertiary);
  white-space: nowrap;
}

/* 菜单样式 */
.app-menu {
  flex: 1;
  padding: 8px 0;
}

:deep(.n-menu-item) {
  margin: 4px 8px;
  border-radius: var(--radius-md) !important;
}

:deep(.n-menu-item-content) {
  padding: 0 12px !important;
  border-radius: var(--radius-md) !important;
}

:deep(.n-menu-item-content::before) {
  border-radius: var(--radius-md) !important;
}

:deep(.n-menu-item-content--selected) {
  background: var(--primary-color-light) !important;
}

:deep(.n-menu-item-content--selected::before) {
  background: transparent !important;
}

:deep(.n-menu-item-content:hover:not(.n-menu-item-content--selected)) {
  background: var(--bg-color-tertiary) !important;
}

:deep(.n-submenu-children) {
  padding-left: 8px;
}

/* 底部版本信息 */
.sider-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--border-color-light);
  text-align: center;
}

.version {
  font-size: 11px;
  color: var(--text-color-disabled);
}

/* 动画 */
.logo-text-enter-active,
.logo-text-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
