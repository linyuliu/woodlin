<!--
  @file views/about/apiDoc/index.vue
  @description 接口文档页：为动态菜单提供独立入口，统一跳转到后端 SpringDoc 页面
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { NButton, NCard, NDescriptions, NDescriptionsItem, NTag } from 'naive-ui'

const apiDocUrl = '/api/doc.html'
const actuatorUrl = '/api/actuator/health'
</script>

<template>
  <div class="about-api-doc">
    <NCard title="接口文档" size="small">
      <NDescriptions bordered :column="1">
        <NDescriptionsItem label="文档入口">
          <NButton text tag="a" :href="apiDocUrl" target="_blank" type="primary">
            {{ apiDocUrl }}
          </NButton>
        </NDescriptionsItem>
        <NDescriptionsItem label="健康检查">
          <NButton text tag="a" :href="actuatorUrl" target="_blank" type="primary">
            {{ actuatorUrl }}
          </NButton>
        </NDescriptionsItem>
        <NDescriptionsItem label="说明">
          <NTag type="info">登录后可查看完整接口定义、调试参数与响应模型</NTag>
        </NDescriptionsItem>
      </NDescriptions>
    </NCard>
  </div>
</template>

<style scoped>
.about-api-doc {
  padding: 16px;
}
</style>
