import { computed, isRef, ref, watch, type Ref } from 'vue'
import { exec, parse, ParseError, type EvalContext } from '@/utils/expr-engine'

export interface ExpressionResult<T = unknown> {
  value: T | null
  error: string | null
  isValid: boolean
}

type MaybeRef<T> = Ref<T> | T

function toReactiveRef<T>(value: MaybeRef<T> | undefined, fallback: T): Ref<T> {
  if (isRef(value)) {
    return value as Ref<T>
  }

  return ref((value ?? fallback) as T) as Ref<T>
}

function resolveErrorMessage(error: unknown): string {
  if (error instanceof ParseError || error instanceof Error) {
    return error.message
  }

  return '未知错误'
}

function createResult<T>(value: T | null, error: string | null): ExpressionResult<T> {
  return {
    value,
    error,
    isValid: error === null
  }
}

function evaluateExpression<T>(expression: string, context: EvalContext): ExpressionResult<T> {
  try {
    return createResult(exec(expression, context) as T, null)
  } catch (error) {
    return createResult<T>(null, resolveErrorMessage(error))
  }
}

export function useExpression<T = unknown>(
  expression: MaybeRef<string>,
  context?: MaybeRef<EvalContext>
) {
  const exprRef = toReactiveRef(expression, '')
  const contextRef = toReactiveRef(context, {})
  const parseError = ref<string | null>(null)
  const result = computed<ExpressionResult<T>>(() => {
    const evaluation = evaluateExpression<T>(exprRef.value, contextRef.value)
    parseError.value = evaluation.error
    return evaluation
  })
  const evaluate = (expr?: string, ctx?: EvalContext): T | null => {
    const evaluation = evaluateExpression<T>(expr ?? exprRef.value, ctx ?? contextRef.value)
    return evaluation.value
  }

  return {
    result,
    value: computed(() => result.value.value),
    error: computed(() => result.value.error ?? parseError.value),
    isValid: computed(() => result.value.isValid),
    evaluate,
    setExpression: (expr: string) => {
      exprRef.value = expr
    },
    setContext: (ctx: EvalContext) => {
      contextRef.value = ctx
    }
  }
}

export function useExpressions(
  expressions: Record<string, string>,
  context?: MaybeRef<EvalContext>
) {
  const contextRef = toReactiveRef(context, {})
  const results = computed(() => {
    const output: Record<string, ExpressionResult> = {}

    for (const [key, expr] of Object.entries(expressions)) {
      output[key] = evaluateExpression(expr, contextRef.value)
    }

    return output
  })

  return {
    results,
    isAllValid: computed(() => Object.values(results.value).every((resultItem) => resultItem.isValid)),
    setContext: (ctx: EvalContext) => {
      contextRef.value = ctx
    }
  }
}

export function useConditionalVisibility(
  condition: MaybeRef<string>,
  context?: MaybeRef<EvalContext>
) {
  const { value, isValid } = useExpression<boolean>(condition, context)
  const isVisible = computed(() => (isValid.value ? Boolean(value.value) : false))

  return {
    isVisible,
    isValid
  }
}

export function useExpressionValidator(expression: MaybeRef<string>) {
  const exprRef = toReactiveRef(expression, '')
  const validation = computed(() => {
    if (!exprRef.value || exprRef.value.trim() === '') {
      return {
        isValid: false,
        error: '表达式不能为空'
      }
    }

    try {
      parse(exprRef.value)
      return {
        isValid: true,
        error: null
      }
    } catch (error) {
      return {
        isValid: false,
        error: resolveErrorMessage(error)
      }
    }
  })

  return {
    isValid: computed(() => validation.value.isValid),
    error: computed(() => validation.value.error),
    validate: () => validation.value
  }
}

export function useExpressionWatch<T = unknown>(
  expression: MaybeRef<string>,
  context: MaybeRef<EvalContext>,
  callback: (value: T | null, isValid: boolean) => void
) {
  const { value, isValid } = useExpression<T>(expression, context)

  watch([value, isValid], ([newValue, newIsValid]) => {
    callback(newValue, newIsValid)
  }, { immediate: true })

  return {
    value,
    isValid
  }
}

export default {
  useExpression,
  useExpressions,
  useConditionalVisibility,
  useExpressionValidator,
  useExpressionWatch
}
