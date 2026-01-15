<script setup lang="ts">
import {NLayoutContent} from 'naive-ui'
</script>

<template>
  <NLayoutContent class="app-content">
    <div class="content-wrapper">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
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
