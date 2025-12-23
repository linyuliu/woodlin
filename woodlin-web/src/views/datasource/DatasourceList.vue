<template>
  <div class="page-container">
    <n-card title="数据源列表">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端数据源管理API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>数据源列表展示</li>
          <li>添加/编辑/删除数据源</li>
          <li>测试数据源连接</li>
          <li>数据源配置管理</li>
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
