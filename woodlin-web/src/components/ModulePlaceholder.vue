<script setup lang="ts">
import { NCard, NTag } from 'naive-ui'

defineProps<{
  title: string
  subtitle: string
  tag?: string
  features: string[]
}>()
</script>

<template>
  <div class="page-container module-placeholder">
    <n-card :bordered="false" class="placeholder-hero">
      <div class="hero-row">
        <div>
          <h2>{{ title }}</h2>
          <p>{{ subtitle }}</p>
        </div>
        <n-tag v-if="tag" type="info" size="small">{{ tag }}</n-tag>
      </div>
      <div class="progress-bar">
        <span></span>
      </div>
    </n-card>

    <n-card :bordered="false" title="规划功能">
      <div class="todo-feature-list">
        <div v-for="item in features" :key="item" class="todo-feature-item">
          {{ item }}
        </div>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.module-placeholder {
  padding: 8px 0;
}

.placeholder-hero {
  background: linear-gradient(118deg, #114e60 0%, #0f766e 52%, #06b6d4 100%);
}

.hero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-color-inverse);
}

.hero-row h2 {
  margin: 0 0 6px;
  color: var(--text-color-inverse);
}

.hero-row p {
  margin: 0;
  color: color-mix(in srgb, var(--text-color-inverse) 82%, transparent);
}

.progress-bar {
  margin-top: 12px;
  height: 6px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.22);
  overflow: hidden;
}

.progress-bar span {
  display: block;
  width: 35%;
  height: 100%;
  border-radius: inherit;
  background: #ffffff;
  animation: loading-slide 1.6s ease-in-out infinite;
}

@keyframes loading-slide {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(320%);
  }
}
</style>
