<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton, NCard, NResult, NSpace } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <div class="runtime-complete">
    <NCard class="complete-card">
      <NResult status="success" title="提交成功" description="您的答案已成功提交，感谢您的参与！">
        <template #footer>
          <NSpace justify="center">
            <NButton @click="router.push('/')">返回首页</NButton>
          </NSpace>
        </template>
      </NResult>
    </NCard>
  </div>
</template>

<style scoped>
.runtime-complete {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.complete-card {
  max-width: 480px;
  width: 100%;
}
</style>
