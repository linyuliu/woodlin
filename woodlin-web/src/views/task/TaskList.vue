<template>
  <div class="page-container">
    <n-card title="任务列表">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端任务调度API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>定时任务列表</li>
          <li>添加/编辑/删除任务</li>
          <li>启动/停止任务</li>
          <li>任务执行状态监控</li>
          <li>Cron表达式配置</li>
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
