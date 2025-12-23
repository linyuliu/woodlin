<template>
  <div class="page-container">
    <n-card title="数据源监控">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端数据源监控API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>数据源连接池监控</li>
          <li>数据源性能指标展示</li>
          <li>慢SQL查询监控</li>
          <li>数据源健康状态检查</li>
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
