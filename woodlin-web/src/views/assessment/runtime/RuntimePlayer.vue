<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NProgress,
  NSpace,
  NSpin,
  NText,
  useDialog,
  useMessage
} from 'naive-ui'
import { loadSessionPayload, saveSnapshot, submitSession } from '@/api/assessment'
import { useAssessmentRuntimeStore } from '@/stores/assessmentRuntime'
import RuntimeItemCard from './components/RuntimeItemCard.vue'
import RuntimeSectionNav from './components/RuntimeSectionNav.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const runtimeStore = useAssessmentRuntimeStore()

const sessionId = ref<string>(String(route.params.sessionId || ''))
const loading = ref(false)
const submitting = ref(false)
const autosaveHandle = ref<ReturnType<typeof setInterval> | null>(null)
const lastAutoSave = ref<Date | null>(null)

const sections = computed(() => runtimeStore.payload?.sections ?? [])
const currentSection = computed(() => runtimeStore.currentSection)
const currentIndex = computed(() => runtimeStore.currentSectionIndex)
const isLastSection = computed(() => currentIndex.value === sections.value.length - 1)
const isFirstSection = computed(() => currentIndex.value === 0)
const answeredItemCodes = computed(() => Object.keys(runtimeStore.answers))

onMounted(async () => {
  if (!sessionId.value) {
    message.error('缺少会话ID')
    return
  }

  if (!runtimeStore.payload || runtimeStore.payload.session.sessionId !== sessionId.value) {
    loading.value = true
    try {
      const payload = await loadSessionPayload(sessionId.value)
      runtimeStore.reset()
      runtimeStore.setPayload(payload)
    } catch {
      message.error('加载作答数据失败')
    } finally {
      loading.value = false
    }
  }

  if (!runtimeStore.payload) {
    return
  }

  runtimeStore.startTimer()
  startAutosave()
})

onUnmounted(() => {
  stopAutosave()
  runtimeStore.stopTimer()
})

function startAutosave() {
  if (autosaveHandle.value) {
    return
  }
  autosaveHandle.value = setInterval(async () => {
    await doSaveSnapshot(false)
  }, 30000)
}

function stopAutosave() {
  if (autosaveHandle.value) {
    clearInterval(autosaveHandle.value)
    autosaveHandle.value = null
  }
}

async function doSaveSnapshot(showMessage = true) {
  if (!runtimeStore.payload) {
    return
  }

  try {
    await saveSnapshot({
      sessionId: sessionId.value,
      currentSectionCode: currentSection.value?.sectionCode,
      currentItemCode: currentSection.value?.items[0]?.itemCode,
      answeredCache: JSON.stringify(runtimeStore.answers),
      elapsedSeconds: runtimeStore.elapsedSeconds
    })
    lastAutoSave.value = new Date()
    if (showMessage) {
      message.success('已保存进度')
    }
  } catch {
    if (showMessage) {
      message.warning('保存进度失败，请检查网络')
    }
  }
}

function handleAnswer(itemCode: string, value: string[] | string | null, rawAnswer: string) {
  const item = runtimeStore.getItem(itemCode)
  const isOptionItem = Boolean(item?.options.length)

  runtimeStore.setAnswer(itemCode, {
    selectedOptionCodes: Array.isArray(value) ? value : typeof value === 'string' && isOptionItem ? [value] : [],
    textAnswer: typeof value === 'string' && !isOptionItem ? value : undefined,
    rawAnswer,
    isSkipped: false,
    displayOrder: currentSection.value?.items.findIndex(currentItem => currentItem.itemCode === itemCode) ?? 0
  })
}

function handleNavigate(index: number) {
  runtimeStore.goToSection(index)
}

function handleNext() {
  runtimeStore.nextSection()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handlePrev() {
  runtimeStore.prevSection()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleSubmit() {
  const unanswered: string[] = []
  for (const section of sections.value) {
    for (const item of section.items) {
      if (item.isRequired && !runtimeStore.answers[item.itemCode]) {
        unanswered.push(item.itemCode)
      }
    }
  }

  if (unanswered.length > 0) {
    dialog.warning({
      title: '有必答题未完成',
      content: `还有 ${unanswered.length} 道必答题未作答，确认提交吗？`,
      positiveText: '确认提交',
      negativeText: '继续作答',
      onPositiveClick: () => doSubmit()
    })
    return
  }

  dialog.info({
    title: '确认提交',
    content: '确认提交所有答案吗？提交后无法修改。',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => doSubmit()
  })
}

async function doSubmit() {
  submitting.value = true
  stopAutosave()

  try {
    await submitSession({
      sessionId: sessionId.value,
      answers: runtimeStore.buildAnswerList(),
      elapsedSeconds: runtimeStore.elapsedSeconds
    })
    runtimeStore.stopTimer()
    message.success('提交成功！')
    await router.push({ name: 'AssessmentRuntimeComplete', query: { sessionId: sessionId.value } })
  } catch {
    message.error('提交失败，请稍后重试')
    startAutosave()
  } finally {
    submitting.value = false
  }
}

const elapsedFormatted = computed(() => {
  const totalSeconds = runtimeStore.elapsedSeconds
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const lastAutoSaveText = computed(() => {
  if (!lastAutoSave.value) {
    return '尚未自动保存'
  }
  return `最近保存：${lastAutoSave.value.toLocaleTimeString()}`
})
</script>

<template>
  <NLayout class="runtime-layout">
    <NLayoutHeader class="runtime-header">
      <NSpace justify="space-between" align="center">
        <NText strong class="runtime-title">{{ runtimeStore.payload?.publish.publishName ?? '作答中...' }}</NText>
        <NSpace align="center">
          <NText depth="3">用时 {{ elapsedFormatted }}</NText>
          <NText depth="3">已答 {{ runtimeStore.answeredCount }} / {{ runtimeStore.payload?.totalItems ?? 0 }}</NText>
          <NText depth="3">{{ lastAutoSaveText }}</NText>
          <NButton size="small" @click="doSaveSnapshot(true)">保存进度</NButton>
        </NSpace>
      </NSpace>
      <NProgress
        type="line"
        :percentage="runtimeStore.progress"
        :show-indicator="false"
        class="runtime-progress"
      />
    </NLayoutHeader>

    <NLayoutContent class="runtime-content">
      <NSpin :show="loading">
        <div v-if="runtimeStore.payload" class="runtime-body">
          <NCard v-if="sections.length > 1" class="runtime-block">
            <RuntimeSectionNav
              :sections="sections"
              :current-index="currentIndex"
              :answered-item-codes="answeredItemCodes"
              @navigate="handleNavigate"
            />
          </NCard>

          <NCard v-if="currentSection" :title="currentSection.sectionTitle" class="runtime-block">
            <template v-if="currentSection.sectionDesc" #header-extra>
              <NText depth="3">{{ currentSection.sectionDesc }}</NText>
            </template>
            <RuntimeItemCard
              v-for="(item, idx) in currentSection.items"
              :key="item.itemCode"
              :item="item"
              :index="idx"
              :answer="runtimeStore.getAnswer(item.itemCode)"
              @answer="handleAnswer"
            />
          </NCard>

          <NCard>
            <NSpace justify="space-between">
              <NButton :disabled="isFirstSection" @click="handlePrev">上一节</NButton>
              <NSpace>
                <NButton v-if="!isLastSection" type="primary" @click="handleNext">下一节</NButton>
                <NButton v-else type="primary" :loading="submitting" @click="handleSubmit">提交答案</NButton>
              </NSpace>
            </NSpace>
          </NCard>
        </div>
      </NSpin>
    </NLayoutContent>
  </NLayout>
</template>

<style scoped>
.runtime-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.runtime-header {
  background: #fff;
  padding: 12px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.runtime-title {
  font-size: 16px;
}

.runtime-progress {
  margin-top: 8px;
}

.runtime-content {
  padding: 24px;
}

.runtime-body {
  max-width: 800px;
  margin: 0 auto;
}

.runtime-block {
  margin-bottom: 16px;
}
</style>
