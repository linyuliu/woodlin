<!--
  @file views/about/index.vue
  @description 关于页：项目信息、版本与技术栈
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed } from 'vue'
import {
  NButton,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NSpace,
  NTag,
} from 'naive-ui'

const version = computed<string>(
  () => (import.meta.env.VITE_APP_VERSION as string | undefined) ?? '1.0.0',
)

const apiDocUrl = '/api/doc.html'
const repoUrl = 'https://github.com/linyuliu/woodlin'
</script>

<template>
  <div class="about">
    <NCard title="关于 Woodlin">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="项目名称">Woodlin</NDescriptionsItem>
        <NDescriptionsItem label="项目描述">
          多租户企业级后台管理系统
        </NDescriptionsItem>
        <NDescriptionsItem label="当前版本">
          <NTag type="success">v{{ version }}</NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="后端技术">
          <NSpace>
            <NTag>Spring Boot 3.5.6</NTag>
            <NTag>Java 25</NTag>
            <NTag>MyBatis-Plus</NTag>
            <NTag>Sa-Token</NTag>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="前端技术">
          <NSpace>
            <NTag>Vue 3</NTag>
            <NTag>TypeScript</NTag>
            <NTag>Naive UI</NTag>
            <NTag>Vite</NTag>
            <NTag>Pinia</NTag>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="API 文档">
          <NButton text tag="a" :href="apiDocUrl" target="_blank" type="primary">
            {{ apiDocUrl }}
          </NButton>
        </NDescriptionsItem>
        <NDescriptionsItem label="代码仓库">
          <NButton text tag="a" :href="repoUrl" target="_blank" type="primary">
            {{ repoUrl }}
          </NButton>
        </NDescriptionsItem>
        <NDescriptionsItem label="开源许可">MIT License</NDescriptionsItem>
      </NDescriptions>
    </NCard>
  </div>
</template>

<style scoped>
.about {
  padding: 16px;
}
</style>
