<script setup lang="ts">
import { computed } from 'vue'
import { NLayoutContent, NSpin } from 'naive-ui'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const loading = computed(() => appStore.loading)
const loadingText = computed(() => appStore.loadingText)
</script>

<template>
  <NLayoutContent class="app-content">
    <div class="content-wrapper">
      <NSpin :show="loading" size="large" :description="loadingText">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </NSpin>
    </div>
  </NLayoutContent>
</template>

<style scoped>
.app-content {
  height: calc(100vh - var(--header-height));
  overflow: auto;
  background-color: var(--bg-color-secondary);
}

.content-wrapper {
  padding: var(--spacing-xl);
  min-height: 100%;
}

.content-wrapper :deep(.n-spin-container) {
  min-height: calc(100vh - var(--header-height) - var(--spacing-xl) * 2);
}

.fade-slide-enter-active {
  transition: all var(--transition-normal);
}

.fade-slide-leave-active {
  transition: all 0.2s ease-in;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: var(--spacing-lg);
  }
}
</style>
