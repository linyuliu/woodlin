<template>
  <div class="page-container">
    <n-card title="文件列表">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端文件管理API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>文件上传</li>
          <li>文件列表展示</li>
          <li>文件预览/下载</li>
          <li>文件删除</li>
          <li>文件搜索和过滤</li>
        </ul>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NCard, NAlert, NText } from 'naive-ui'
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
