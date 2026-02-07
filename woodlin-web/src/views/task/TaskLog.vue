<script setup lang="ts">
import ModulePlaceholder from '@/components/ModulePlaceholder.vue'
</script>

<template>
  <ModulePlaceholder
    title="任务执行日志"
    subtitle="沉淀任务执行链路，支持问题追踪、重放与执行质量评估。"
    tag="Task Logs"
    :features="[
      '任务执行日志列表与多条件检索',
      '执行耗时、结果状态与错误栈展示',
      '日志详情重放与失败重试入口',
      '按任务/租户/时间维度统计分析',
      '日志归档与保留策略配置'
    ]"
  />
</template>
