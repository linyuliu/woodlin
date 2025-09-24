<!--
  Woodlin应用根组件
  
  @author mumu
  @description 应用的根组件，提供全局的主题配置和路由视图渲染
  @since 2025-01-01
-->

<script setup lang="ts">
import { RouterView } from 'vue-router'
import { NConfigProvider, darkTheme } from 'naive-ui'
import { ref } from 'vue'

/**
 * 主题配置
 * null表示浅色主题，darkTheme表示深色主题
 * 可以根据用户偏好或系统设置进行切换
 */
const theme = ref(null) // null for light theme, darkTheme for dark theme
</script>

<template>
  <!-- Naive UI配置提供者，为整个应用提供主题配置 -->
  <NConfigProvider :theme="theme">
    <div class="app">
      <!-- 路由视图容器，渲染匹配的路由组件 -->
      <RouterView />
    </div>
  </NConfigProvider>
</template>

<style>
/* 全局应用样式 */
#app {
  height: 100vh;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
}

/* 应用容器样式 */
.app {
  height: 100%;
}

/* 全局重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
