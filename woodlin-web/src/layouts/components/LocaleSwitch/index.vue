<!--
  @file layouts/components/LocaleSwitch/index.vue
  @description 语言切换：zh-CN / en-US
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { NButton, NDropdown, type DropdownOption } from 'naive-ui'
import WIcon from '@/components/WIcon/index.vue'
import { useAppStore } from '@/stores/modules/app'
import type { AppLocale } from '@/stores/modules/app'

const appStore = useAppStore()
const i18n = useI18n()

const options: DropdownOption[] = [
  { label: '简体中文', key: 'zh-CN' },
  { label: 'English', key: 'en-US' },
]

/** 切换语言 */
function handleSelect(key: string): void {
  const locale = key as AppLocale
  appStore.setLocale(locale)
  i18n.locale.value = locale
}
</script>

<template>
  <NDropdown :options="options" trigger="click" @select="handleSelect">
    <NButton quaternary circle>
      <template #icon>
        <WIcon icon="vicons:antd:GlobalOutlined" />
      </template>
    </NButton>
  </NDropdown>
</template>
