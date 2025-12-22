<script setup lang="ts">
import { computed } from 'vue'
import { NLayoutSider, NMenu, type MenuOption } from 'naive-ui'

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
    bordered
    collapse-mode="width"
    :collapsed="collapsed"
    :collapsed-width="64"
    :width="240"
    :native-scrollbar="false"
    class="app-sider"
  >
    <div class="logo">
      <div class="logo-icon">🌲</div>
      <transition name="fade" mode="out-in">
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
      @update:value="handleSelect"
    />
  </NLayoutSider>
</template>

<style scoped>
.app-sider {
  box-shadow: 2px 0 8px rgba(15, 23, 42, 0.08);
  position: relative;
  z-index: 10;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  min-height: 64px;
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text h1 {
  font-size: 18px;
  font-weight: 600;
  color: #18a058;
}

.logo-text p {
  font-size: 12px;
  color: #8c8c8c;
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
