<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">500</div>
      <div class="error-title">抱歉，服务器出错了</div>
      <div class="error-description">
        服务器遇到了一个错误，请稍后再试或联系管理员。
      </div>
      <div class="error-actions">
        <n-space>
          <n-button type="primary" @click="goHome">返回首页</n-button>
          <n-button @click="reload">刷新页面</n-button>
        </n-space>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton, NSpace } from 'naive-ui'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const reload = () => {
  window.location.reload()
}
</script>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #f5222d;
  line-height: 1;
}

.error-title {
  font-size: 24px;
  font-weight: 500;
  margin: 20px 0;
  color: #333;
}

.error-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.error-actions {
  display: flex;
  justify-content: center;
}
</style>
