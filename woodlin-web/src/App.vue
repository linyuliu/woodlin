<script setup lang="ts">
import { RouterView } from 'vue-router'
import { NConfigProvider, darkTheme } from 'naive-ui'
import { ref } from 'vue'

const theme = ref(null) // null for light theme, darkTheme for dark theme
</script>

<template>
  <NConfigProvider :theme="theme">
    <div class="app">
      <RouterView />
    </div>
  </NConfigProvider>
</template>

<style>
#app {
  height: 100vh;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
}

.app {
  height: 100%;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
