import {beforeEach, describe, expect, it} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {useAssessmentRuntimeStore} from '@/stores/assessmentRuntime'
import type {RuntimePayloadVO} from '@/api/assessment'

describe('assessmentRuntime store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('hydrates answer snapshot when payload is restored', () => {
    const store = useAssessmentRuntimeStore()
    const payload: RuntimePayloadVO = {
      publish: {
        publishId: '1',
        formId: '10',
        versionId: '100',
        publishCode: 'PUB-1',
        publishName: '测评',
        status: 'published',
        timeLimitMinutes: 30,
        maxAttempts: 1,
        allowAnonymous: true,
        allowResume: true,
        randomStrategy: 'random_both',
        showResultImmediately: false
      },
      session: {
        sessionId: '99',
        publishId: '1',
        formId: '10',
        versionId: '100',
        status: 'in_progress',
        displaySeed: 123,
        startedAt: '2026-04-22T00:00:00',
        elapsedSeconds: 18,
        attemptNumber: 1,
        currentSectionCode: 'SEC_002'
      },
      sections: [
        {
          sectionCode: 'DEMOGRAPHIC',
          sectionTitle: '人口学前置',
          displayMode: 'paged',
          sortOrder: 1,
          isRequired: true,
          items: []
        },
        {
          sectionCode: 'SEC_002',
          sectionTitle: '正式题',
          displayMode: 'paged',
          sortOrder: 2,
          isRequired: true,
          items: []
        }
      ],
      totalItems: 1,
      answerSnapshot: {
        Q001: {
          itemCode: 'Q001',
          selectedOptionCodes: ['A'],
          rawAnswer: '"A"',
          isSkipped: false
        }
      }
    }

    store.setPayload(payload)

    expect(store.elapsedSeconds).toBe(18)
    expect(store.currentSectionIndex).toBe(1)
    expect(store.answers.Q001?.selectedOptionCodes).toEqual(['A'])
  })
})
