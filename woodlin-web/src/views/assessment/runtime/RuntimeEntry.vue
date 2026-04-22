<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NSpace,
  NSpin,
  useMessage
} from 'naive-ui'
import { getRuntimePublishInfo, startOrResumeSession, type RuntimePublishVO } from '@/api/assessment'
import { useAssessmentRuntimeStore } from '@/stores/assessmentRuntime'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const runtimeStore = useAssessmentRuntimeStore()

const publishId = ref<string>(String(route.params.publishId || route.query.publishId || ''))
const publishInfo = ref<RuntimePublishVO | null>(null)
const loading = ref(false)
const starting = ref(false)

onMounted(async () => {
  if (!publishId.value) {
    message.error('缺少发布ID')
    return
  }

  loading.value = true
  try {
    publishInfo.value = await getRuntimePublishInfo(publishId.value)
  } catch {
    message.error('加载测评信息失败')
  } finally {
    loading.value = false
  }
})

async function handleStart() {
  if (!publishInfo.value) {
    return
  }

  starting.value = true
  try {
    const payload = await startOrResumeSession({
      publishId: publishId.value,
      deviceType: /Mobi/i.test(navigator.userAgent) ? 'mobile' : 'pc',
      userAgent: navigator.userAgent.substring(0, 200)
    })
    runtimeStore.reset()
    runtimeStore.setPayload(payload)
    await router.push({ name: 'AssessmentRuntimePlayer', params: { sessionId: payload.session.sessionId } })
  } catch {
    message.error('启动作答失败，请稍后重试')
  } finally {
    starting.value = false
  }
}
</script>

<template>
  <div class="runtime-entry">
    <NSpin :show="loading">
      <NCard v-if="publishInfo" :title="publishInfo.publishName" class="entry-card">
        <NDescriptions bordered label-placement="left" :column="1">
          <NDescriptionsItem label="状态">{{ publishInfo.status }}</NDescriptionsItem>
          <NDescriptionsItem v-if="publishInfo.timeLimitMinutes > 0" label="时间限制">
            {{ publishInfo.timeLimitMinutes }} 分钟
          </NDescriptionsItem>
          <NDescriptionsItem v-if="publishInfo.maxAttempts > 0" label="最大次数">
            {{ publishInfo.maxAttempts }} 次
          </NDescriptionsItem>
          <NDescriptionsItem label="断点续答">{{ publishInfo.allowResume ? '支持' : '不支持' }}</NDescriptionsItem>
        </NDescriptions>
        <NAlert v-if="publishInfo.allowResume" type="info" class="entry-alert">
          本测评支持断点续答，关闭后可从上次进度继续作答。
        </NAlert>
        <NSpace justify="center" class="entry-actions">
          <NButton type="primary" size="large" :loading="starting" @click="handleStart">
            开始作答
          </NButton>
        </NSpace>
      </NCard>
      <NCard v-else-if="!loading" title="提示" class="entry-card">
        <p>未找到测评信息，请确认链接是否正确。</p>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped>
.runtime-entry {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.entry-card {
  max-width: 640px;
  margin: 40px auto;
}

.entry-alert {
  margin-top: 12px;
}

.entry-actions {
  margin-top: 24px;
}
</style>
