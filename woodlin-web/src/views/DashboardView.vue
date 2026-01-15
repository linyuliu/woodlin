<script setup lang="ts">
/**
 * 仪表板视图
 *
 * @author mumu
 * @description 系统仪表板，展示系统关键指标和概览信息
 * @since 2025-01-01
 */
import {NCard, NGrid, NGridItem, NIcon, NProgress, NSpace, NStatistic} from 'naive-ui'
import {h} from 'vue'
import {
  BusinessOutline,
  CloudOutline,
  PeopleOutline,
  PersonOutline,
  ServerOutline,
  ShieldCheckmarkOutline,
  TimeOutline,
  TrendingUpOutline
} from '@vicons/ionicons5'

/**
 * 统计数据配置
 */
const stats = [
  {
    label: '总用户数',
    value: 1250,
    color: '#18a058',
    bgColor: 'rgba(24, 160, 88, 0.1)',
    icon: PeopleOutline,
    suffix: '人',
    trend: '+12%',
    trendUp: true
  },
  {
    label: '在线用户',
    value: 86,
    color: '#2080f0',
    bgColor: 'rgba(32, 128, 240, 0.1)',
    icon: PersonOutline,
    suffix: '人',
    trend: '+5%',
    trendUp: true
  },
  {
    label: '租户数量',
    value: 15,
    color: '#f0a020',
    bgColor: 'rgba(240, 160, 32, 0.1)',
    icon: BusinessOutline,
    suffix: '个',
    trend: '+2',
    trendUp: true
  },
  {
    label: '今日访问',
    value: 3280,
    color: '#d03050',
    bgColor: 'rgba(208, 48, 80, 0.1)',
    icon: TrendingUpOutline,
    suffix: '次',
    trend: '+18%',
    trendUp: true
  },
]

/**
 * 系统状态数据
 */
const systemStatus = [
  {label: 'CPU 使用率', value: 45, color: '#18a058'},
  {label: '内存使用率', value: 68, color: '#2080f0'},
  {label: '磁盘使用率', value: 32, color: '#f0a020'},
]

/**
 * 快捷操作
 */
const quickActions = [
  {label: '用户管理', icon: PeopleOutline, path: '/user', color: '#18a058'},
  {label: '角色管理', icon: ShieldCheckmarkOutline, path: '/role', color: '#2080f0'},
  {label: '系统设置', icon: ServerOutline, path: '/system-settings', color: '#f0a020'},
  {label: '文件管理', icon: CloudOutline, path: '/file', color: '#d03050'},
]

/**
 * 渲染统计图标
 */
const renderIcon = (icon: any, color: string, bgColor: string) => {
  return h(
    'div',
    {
      style: {
        width: '48px',
        height: '48px',
        borderRadius: '12px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: bgColor,
      }
    },
    h(NIcon, {size: 24, color: color}, {default: () => h(icon)})
  )
}
</script>

<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <NGrid cols="1 s:2 m:2 l:4" responsive="screen" x-gap="20" y-gap="20">
      <NGridItem v-for="stat in stats" :key="stat.label">
        <NCard
          :bordered="false"
          class="stat-card"
          content-style="padding: 20px;"
        >
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-label">{{ stat.label }}</span>
              <div class="stat-value-row">
                <NStatistic :value="stat.value" class="stat-number">
                  <template #suffix>
                    <span class="stat-suffix">{{ stat.suffix }}</span>
                  </template>
                </NStatistic>
              </div>
              <div :class="{ 'trend-up': stat.trendUp, 'trend-down': !stat.trendUp }"
                   class="stat-trend">
                <span>{{ stat.trend }}</span>
                <span class="trend-text">较昨日</span>
              </div>
            </div>
            <component :is="renderIcon(stat.icon, stat.color, stat.bgColor)"/>
          </div>
        </NCard>
      </NGridItem>
    </NGrid>

    <!-- 主内容区域 -->
    <NGrid class="main-grid" cols="1 l:3" responsive="screen" x-gap="20" y-gap="20">
      <!-- 左侧区域 -->
      <NGridItem :span="2">
        <NCard
          :bordered="false"
          class="overview-card"
          content-style="padding: 24px;"
          title="系统概览"
        >
          <template #header-extra>
            <NIcon color="var(--text-color-tertiary)" size="18">
              <TimeOutline/>
            </NIcon>
          </template>

          <div class="overview-content">
            <div class="welcome-section">
              <h3>👋 欢迎使用 Woodlin 多租户管理系统！</h3>
              <p class="description">
                基于 Spring Boot 3 + Vue 3 + TypeScript + Naive UI 构建的现代化企业级多租户中后台管理系统框架。
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
                  <div class="feature-icon">🏠</div>
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
              </div>
            </div>
          </div>
        </NCard>
      </NGridItem>

      <!-- 右侧区域 -->
      <NGridItem>
        <NSpace :size="20" vertical>
          <!-- 系统状态 -->
          <NCard
            :bordered="false"
            class="status-card"
            content-style="padding: 20px;"
            title="系统状态"
          >
            <div class="status-list">
              <div v-for="item in systemStatus" :key="item.label" class="status-item">
                <div class="status-header">
                  <span class="status-label">{{ item.label }}</span>
                  <span class="status-value">{{ item.value }}%</span>
                </div>
                <NProgress
                  :border-radius="4"
                  :color="item.color"
                  :height="8"
                  :percentage="item.value"
                  :rail-color="'rgba(0,0,0,0.05)'"
                  :show-indicator="false"
                  type="line"
                />
              </div>
            </div>
          </NCard>

          <!-- 快捷操作 -->
          <NCard
            :bordered="false"
            class="quick-card"
            content-style="padding: 16px;"
            title="快捷操作"
          >
            <div class="quick-actions">
              <router-link
                v-for="action in quickActions"
                :key="action.label"
                :to="action.path"
                class="quick-item"
              >
                <div :style="{ background: action.color + '15' }" class="quick-icon">
                  <NIcon :color="action.color" :size="20">
                    <component :is="action.icon"/>
                  </NIcon>
                </div>
                <span class="quick-label">{{ action.label }}</span>
              </router-link>
            </div>
          </NCard>
        </NSpace>
      </NGridItem>
    </NGrid>
  </div>
</template>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
}

/* 统计卡片样式 */
.stat-card {
  cursor: default;
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-color-secondary);
}

.stat-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-number :deep(.n-statistic-value__content) {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.stat-suffix {
  font-size: 14px;
  color: var(--text-color-tertiary);
  font-weight: 400;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 4px;
}

.stat-trend.trend-up {
  color: var(--success-color);
}

.stat-trend.trend-down {
  color: var(--error-color);
}

.trend-text {
  color: var(--text-color-tertiary);
}

/* 主内容区域 */
.main-grid {
  margin-top: 20px;
}

/* 系统概览卡片 */
.overview-card {
  height: 100%;
}

.overview-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.welcome-section h3 {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.welcome-section .description {
  font-size: 14px;
  color: var(--text-color-secondary);
  line-height: 1.7;
  margin: 0;
}

.features-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 20px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--bg-color-secondary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
}

.feature-item:hover {
  background: var(--primary-color-light);
  transform: translateX(4px);
}

.feature-icon {
  font-size: 28px;
  flex-shrink: 0;
  line-height: 1;
}

.feature-text h5 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 12px;
  color: var(--text-color-tertiary);
  margin: 0;
  line-height: 1.5;
}

/* 系统状态卡片 */
.status-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 13px;
  color: var(--text-color-secondary);
}

.status-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-color-primary);
}

/* 快捷操作卡片 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-fast);
  background: var(--bg-color-secondary);
}

.quick-item:hover {
  background: var(--primary-color-light);
  transform: translateY(-2px);
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-label {
  font-size: 12px;
  color: var(--text-color-secondary);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .welcome-section h3 {
    font-size: 18px;
  }
}
</style>
