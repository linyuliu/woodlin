<script setup lang="ts">
import ModulePlaceholder from '@/components/ModulePlaceholder.vue'
</script>

<template>
  <ModulePlaceholder
    title="任务调度中心"
    subtitle="统一管理定时任务、执行状态和调度策略，支持后续编排与告警联动。"
    tag="Task Scheduler"
    :features="[
      '任务列表与状态筛选（运行/暂停/异常）',
      'Cron 表达式可视化配置与校验',
      '任务启停、手动触发、并发控制',
      '失败重试策略与告警通知配置',
      '多租户任务隔离与权限控制'
    ]"
  />
</template>
