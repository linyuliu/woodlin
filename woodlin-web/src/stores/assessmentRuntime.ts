import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import type {
  AnswerItemDTO,
  RuntimeItemVO,
  RuntimePayloadVO,
  RuntimeSectionVO
} from '@/api/assessment'

export const useAssessmentRuntimeStore = defineStore('assessmentRuntime', () => {
  const payload = ref<RuntimePayloadVO | null>(null)
  const currentSectionIndex = ref(0)
  const answers = ref<Record<string, AnswerItemDTO>>({})
  const elapsedSeconds = ref(0)
  const timerHandle = ref<ReturnType<typeof setInterval> | null>(null)

  const currentSection = computed<RuntimeSectionVO | null>(
    () => payload.value?.sections[currentSectionIndex.value] ?? null
  )

  const totalSections = computed(() => payload.value?.sections.length ?? 0)

  const answeredCount = computed(() => Object.keys(answers.value).length)

  const progress = computed(() => {
    const total = payload.value?.totalItems ?? 0
    return total > 0 ? Math.round((answeredCount.value / total) * 100) : 0
  })

  function setPayload(p: RuntimePayloadVO) {
    payload.value = p
    elapsedSeconds.value = p.session.elapsedSeconds ?? 0
    answers.value = {...(p.answerSnapshot ?? {})}
    if (p.session.currentSectionCode) {
      const idx = p.sections.findIndex(section => section.sectionCode === p.session.currentSectionCode)
      if (idx >= 0) {
        currentSectionIndex.value = idx
      }
    }
  }

  function setAnswer(itemCode: string, answer: Partial<AnswerItemDTO>) {
    answers.value[itemCode] = { ...answers.value[itemCode], itemCode, ...answer }
  }

  function getAnswer(itemCode: string): AnswerItemDTO | undefined {
    return answers.value[itemCode]
  }

  function getItem(itemCode: string): RuntimeItemVO | undefined {
    return payload.value?.sections.flatMap(section => section.items).find(item => item.itemCode === itemCode)
  }

  function goToSection(index: number) {
    if (index >= 0 && index < totalSections.value) {
      currentSectionIndex.value = index
    }
  }

  function nextSection() {
    goToSection(currentSectionIndex.value + 1)
  }

  function prevSection() {
    goToSection(currentSectionIndex.value - 1)
  }

  function startTimer() {
    if (timerHandle.value) {
      return
    }
    timerHandle.value = setInterval(() => {
      elapsedSeconds.value += 1
    }, 1000)
  }

  function stopTimer() {
    if (timerHandle.value) {
      clearInterval(timerHandle.value)
      timerHandle.value = null
    }
  }

  function buildAnswerList(): AnswerItemDTO[] {
    return Object.values(answers.value)
  }

  function reset() {
    stopTimer()
    payload.value = null
    currentSectionIndex.value = 0
    answers.value = {}
    elapsedSeconds.value = 0
  }

  return {
    payload,
    currentSectionIndex,
    answers,
    elapsedSeconds,
    currentSection,
    totalSections,
    answeredCount,
    progress,
    setPayload,
    setAnswer,
    getAnswer,
    getItem,
    goToSection,
    nextSection,
    prevSection,
    startTimer,
    stopTimer,
    buildAnswerList,
    reset
  }
})
