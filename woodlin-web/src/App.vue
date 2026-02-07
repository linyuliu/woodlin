<script setup lang="ts">
import { RouterView } from 'vue-router'
import { computed } from 'vue'
import {
  darkTheme,
  dateZhCN,
  NConfigProvider,
  NDialogProvider,
  NLoadingBarProvider,
  NMessageProvider,
  zhCN
} from 'naive-ui'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const { themeMode } = storeToRefs(appStore)
const naiveTheme = computed(() => (themeMode.value === 'dark' ? darkTheme : null))
</script>

<template>
  <NConfigProvider :theme="naiveTheme" :locale="zhCN" :date-locale="dateZhCN">
    <NMessageProvider>
      <NDialogProvider>
        <NLoadingBarProvider>
          <div class="app">
            <RouterView />
          </div>
        </NLoadingBarProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>

<style>
#app {
  height: 100vh;
  font-family: var(--font-family);
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
