<script setup lang="ts">
/**
 * 仪表板视图
 * 
 * @author mumu
 * @description 系统仪表板，展示系统关键指标和概览信息
 * @since 2025-01-01
 */
import { NCard, NGrid, NGridItem, NStatistic, NSpace, NIcon } from 'naive-ui'
import { h } from 'vue'
import { 
  PeopleOutline, 
  PersonOutline,
  BusinessOutline,
  EyeOutline
} from '@vicons/ionicons5'

/**
 * 统计数据配置
 */
const stats = [
  { 
    label: '总用户数', 
    value: 1250, 
    color: '#18a058',
    icon: PeopleOutline,
    suffix: '人'
  },
  { 
    label: '在线用户', 
    value: 86, 
    color: '#2080f0',
    icon: PersonOutline,
    suffix: '人'
  },
  { 
    label: '租户数量', 
    value: 15, 
    color: '#f0a020',
    icon: BusinessOutline,
    suffix: '个'
  },
  { 
    label: '今日访问', 
    value: 3280, 
    color: '#d03050',
    icon: EyeOutline,
    suffix: '次'
  },
]

/**
 * 渲染统计图标
 */
const renderIcon = (icon: any, color: string) => {
  return h(
    NIcon,
    { 
      size: 32, 
      color: color,
      style: { marginBottom: '12px' }
    },
    { default: () => h(icon) }
  )
}
</script>

<template>
  <div class="dashboard-container">
    <NSpace vertical :size="24">
      <!-- 统计卡片 -->
      <NGrid x-gap="16" y-gap="16" cols="1 s:2 m:2 l:4" responsive="screen">
        <NGridItem v-for="stat in stats" :key="stat.label">
          <NCard 
            :bordered="false" 
            class="stat-card"
            :segmented="{ content: true }"
          >
            <div class="stat-content">
              <component :is="renderIcon(stat.icon, stat.color)" />
              <NStatistic
                :label="stat.label"
                :value="stat.value"
              >
                <template #suffix>
                  <span class="stat-suffix">{{ stat.suffix }}</span>
                </template>
              </NStatistic>
            </div>
          </NCard>
        </NGridItem>
      </NGrid>
      
      <!-- 系统概览 -->
      <NCard 
        title="系统概览" 
        :bordered="false"
        :segmented="{ content: true }"
        class="overview-card"
      >
        <div class="overview-content">
          <div class="welcome-section">
            <h3>👋 欢迎使用 Woodlin 多租户管理系统！</h3>
            <p class="description">
              这是一个基于 Spring Boot 3 + Vue 3 + TypeScript + Naive UI 构建的现代化企业级多租户中后台管理系统框架。
            </p>
          </div>
          
          <div class="features-section">
            <h4>✨ 核心功能特性</h4>
            <div class="features-grid">
              <div class="feature-item">
                <div class="feature-icon">🔐</div>
                <div class="feature-text">
                  <h5>用户权限管理</h5>
                  <p>完善的RBAC权限控制体系</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">🏢</div>
                <div class="feature-text">
                  <h5>组织架构管理</h5>
                  <p>灵活的部门层级结构</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">🏘️</div>
                <div class="feature-text">
                  <h5>多租户隔离</h5>
                  <p>完整的租户数据隔离方案</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">📊</div>
                <div class="feature-text">
                  <h5>实时监控</h5>
                  <p>系统运行状态实时追踪</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">🛡️</div>
                <div class="feature-text">
                  <h5>安全防护</h5>
                  <p>多层次安全防护机制</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">⚙️</div>
                <div class="feature-text">
                  <h5>系统配置</h5>
                  <p>灵活的系统参数配置</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </NCard>
    </NSpace>
  </div>
</template>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
}

/* 统计卡片样式 */
.stat-card {
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 8px 0;
}

.stat-suffix {
  font-size: 14px;
  margin-left: 4px;
  color: #999;
}

/* 系统概览卡片 */
.overview-card {
  min-height: 400px;
}

.overview-content {
  padding: 12px;
}

.welcome-section {
  margin-bottom: 32px;
}

.welcome-section h3 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.welcome-section .description {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.features-section h4 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.feature-icon {
  font-size: 32px;
  flex-shrink: 0;
  line-height: 1;
}

.feature-text h5 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  line-height: 1.4;
}

.feature-text p {
  font-size: 14px;
  color: #999;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-section h3 {
    font-size: 20px;
  }
  
  .features-section h4 {
    font-size: 16px;
  }
}
</style>