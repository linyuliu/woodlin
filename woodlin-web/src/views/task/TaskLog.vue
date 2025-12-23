<template>
  <div class="page-container">
    <n-card title="任务日志">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端任务日志API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>任务执行日志列表</li>
          <li>日志详情查看</li>
          <li>日志搜索和过滤</li>
          <li>执行结果统计</li>
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
