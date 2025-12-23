<template>
  <div class="page-container">
    <n-card title="存储配置">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端文件存储配置API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>存储类型配置（本地/OSS/MinIO等）</li>
          <li>存储空间管理</li>
          <li>存储配额设置</li>
          <li>存储清理策略</li>
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
