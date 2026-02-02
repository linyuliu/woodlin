<script lang="ts" setup>
/**
 * 页面容器组件
 *
 * @author mumu
 * @description 通用页面布局容器，包含搜索区域和内容区域
 * @since 2025-01-01
 */
import {NCard, NSpace} from 'naive-ui'

withDefaults(defineProps<{
  /** 页面标题 */
  title?: string
  /** 是否显示搜索区域 */
  showSearch?: boolean
}>(), {
  showSearch: true
})
</script>

<template>
  <div class="page-container">
    <NSpace :size="20" vertical>
      <!-- 搜索区域 -->
      <NCard
        v-if="showSearch"
        :bordered="false"
        class="search-card"
        content-style="padding: 20px;"
      >
        <slot name="search"/>
      </NCard>

      <!-- 主内容卡片 -->
      <NCard
        :bordered="false"
        :title="title"
        class="content-card"
        content-style="padding: 20px;"
      >
        <template v-if="$slots['header-extra']" #header-extra>
          <slot name="header-extra"/>
        </template>

        <slot/>
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
.page-container {
  width: 100%;
  height: 100%;
}

.search-card {
  background: var(--bg-color);
}

.search-card :deep(.n-form-item) {
  margin-bottom: 0;
}

.search-card :deep(.n-form-item .n-form-item-feedback-wrapper) {
  min-height: 0;
}

.content-card {
  background: var(--bg-color);
}

.content-card :deep(.n-card-header) {
  padding-bottom: 16px;
}

.content-card :deep(.n-card-header__main) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
}
</style>
