<script setup lang="ts">
import ModulePlaceholder from '@/components/ModulePlaceholder.vue'
</script>

<template>
  <ModulePlaceholder
    title="存储策略配置"
    subtitle="统一管理本地与对象存储策略，支持容量配额与多环境切换。"
    tag="Storage Strategy"
    :features="[
      '本地/MinIO/OSS/S3 等存储源配置',
      '存储桶、目录策略与访问密钥管理',
      '租户配额、阈值告警与扩容建议',
      '自动清理策略与归档转储任务',
      '链路健康检测与可用性报表'
    ]"
  />
</template>
