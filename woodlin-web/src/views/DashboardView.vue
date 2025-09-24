<script setup lang="ts">
import { NCard, NGrid, NGridItem, NStatistic, NSpace } from 'naive-ui'

const stats = [
  { label: '总用户数', value: 1250, color: '#18a058' },
  { label: '在线用户', value: 86, color: '#2080f0' },
  { label: '租户数量', value: 15, color: '#f0a020' },
  { label: '今日访问', value: 3280, color: '#d03050' },
]
</script>

<template>
  <div>
    <NSpace vertical size="large">
      <NGrid x-gap="12" y-gap="12" cols="1 s:2 m:4">
        <NGridItem v-for="stat in stats" :key="stat.label">
          <NCard>
            <NStatistic
              :label="stat.label"
              :value="stat.value"
              :color="stat.color"
            />
          </NCard>
        </NGridItem>
      </NGrid>
      
      <NCard title="系统概览">
        <div style="padding: 20px;">
          <h3>欢迎使用 Woodlin 多租户管理系统！</h3>
          <p>这是一个高质量的多租户中后台管理系统框架。</p>
          <br />
          <h4>功能特性：</h4>
          <ul>
            <li>🔐 完善的用户权限管理</li>
            <li>🏢 灵活的部门组织架构</li>
            <li>🏘️ 多租户数据隔离</li>
            <li>📊 实时数据监控</li>
            <li>🛡️ 安全防护机制</li>
          </ul>
        </div>
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
ul {
  padding-left: 20px;
}
li {
  margin: 8px 0;
}
</style>