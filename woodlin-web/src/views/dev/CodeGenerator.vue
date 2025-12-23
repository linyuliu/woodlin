<template>
  <div class="page-container">
    <n-card title="代码生成器">
      <n-alert type="info" title="TODO" :bordered="false">
        此页面待实现，需要对接后端代码生成API
      </n-alert>
      <div style="margin-top: 20px;">
        <n-text>功能包括：</n-text>
        <ul>
          <li>数据库表选择</li>
          <li>代码模板配置</li>
          <li>代码生成和下载</li>
          <li>代码预览</li>
          <li>自定义生成规则</li>
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
