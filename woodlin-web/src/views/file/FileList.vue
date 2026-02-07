<script setup lang="ts">
import ModulePlaceholder from '@/components/ModulePlaceholder.vue'
</script>

<template>
  <ModulePlaceholder
    title="文件资产库"
    subtitle="管理上传文件、访问权限和生命周期，服务业务附件与数据产物沉淀。"
    tag="File Assets"
    :features="[
      '文件上传、分片续传与版本管理',
      '文件预览、下载审计与权限校验',
      '按类型/标签/业务线检索过滤',
      '批量删除、回收站与恢复能力',
      '生命周期策略与冷热分层管理'
    ]"
  />
</template>
