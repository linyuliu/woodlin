<!--
  @file views/about/changelog/index.vue
  @description 更新日志页：沉淀当前重构阶段的重要里程碑，补齐动态菜单页面
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { NCard, NTimeline, NTimelineItem } from 'naive-ui'

const milestones = [
  {
    title: '动态菜单链路打通',
    content: '统一后端 RouteVO，下发路由树并由前端 import.meta.glob 映射组件。',
    type: 'success' as const,
  },
  {
    title: '系统/RBAC 页面重写',
    content: '用户、角色、菜单、部门、字典、配置、公告等核心后台页面已切换到新结构。',
    type: 'info' as const,
  },
  {
    title: '默认密码与脚本修复',
    content: '默认账号统一为 Aa@12345，同时补齐 {bcrypt} 前缀和回滚脚本。',
    type: 'warning' as const,
  },
]
</script>

<template>
  <div class="about-changelog">
    <NCard title="更新日志" size="small">
      <NTimeline>
        <NTimelineItem
          v-for="item in milestones"
          :key="item.title"
          :type="item.type"
          :title="item.title"
          :content="item.content"
        />
      </NTimeline>
    </NCard>
  </div>
</template>

<style scoped>
.about-changelog {
  padding: 16px;
}
</style>
