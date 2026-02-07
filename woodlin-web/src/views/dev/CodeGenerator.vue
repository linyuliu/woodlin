<script setup lang="ts">
import ModulePlaceholder from '@/components/ModulePlaceholder.vue'
</script>

<template>
  <ModulePlaceholder
    title="代码生成工坊"
    subtitle="基于元数据生成后端与前端脚手架，缩短业务模块交付周期。"
    tag="Generator"
    :features="[
      '按数据表批量生成实体、Mapper、Service、Controller',
      '支持 Vue 页面、API、路由与权限代码模板',
      '模板变量与命名策略可视化配置',
      '生成预览、差异对比与下载打包',
      '支持自定义模板仓库与版本治理'
    ]"
  />
</template>
