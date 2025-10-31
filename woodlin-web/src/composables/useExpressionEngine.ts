/**
 * Vue Composable for Expression Engine
 * 
 * Provides reactive expression evaluation for Vue components
 */

import { ref, computed, watch, type Ref } from 'vue';
import { exec, parse, type EvalContext, ParseError } from '@/utils/expr-engine';

/**
 * 表达式评估结果
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface ExpressionResult<T = any> {
  value: T | null;
  error: string | null;
  isValid: boolean;
}

/**
 * Use Expression Engine Composable
 * 
 * @param expression - 表达式字符串 (可以是响应式的)
 * @param context - 评估上下文 (可以是响应式的)
 * @returns 包含评估结果和方法的对象
 */
export function useExpression<T = any>(
  expression: Ref<string> | string,
  context?: Ref<EvalContext> | EvalContext
) {
  const exprRef = ref(expression) as Ref<string>;
  const contextRef = ref(context || {}) as Ref<EvalContext>;
  
  // 解析错误
  const parseError = ref<string | null>(null);
  
  // 评估结果
  const result = computed<ExpressionResult<T>>(() => {
    try {
      const value = exec(exprRef.value, contextRef.value) as T;
      parseError.value = null;
      return {
        value,
        error: null,
        isValid: true
      };
    } catch (error) {
      const errorMsg = error instanceof ParseError 
        ? error.message 
        : error instanceof Error 
          ? error.message 
          : '未知错误';
      parseError.value = errorMsg;
      return {
        value: null,
        error: errorMsg,
        isValid: false
      };
    }
  });
  
  // 手动评估函数
  const evaluate = (expr?: string, ctx?: EvalContext): T | null => {
    try {
      const evalExpr = expr || exprRef.value;
      const evalCtx = ctx || contextRef.value;
      return exec(evalExpr, evalCtx) as T;
    } catch {
      return null;
    }
  };
  
  // 检查表达式是否有效
  const isValid = computed(() => result.value.isValid);
  
  return {
    result,
    value: computed(() => result.value.value),
    error: computed(() => result.value.error),
    isValid,
    evaluate,
    // 允许外部更新表达式和上下文
    setExpression: (expr: string) => { exprRef.value = expr; },
    setContext: (ctx: EvalContext) => { contextRef.value = ctx; }
  };
}

/**
 * Use Multiple Expressions
 * 
 * 管理多个表达式的评估
 */
export function useExpressions(
  expressions: Record<string, string>,
  context?: Ref<EvalContext> | EvalContext
) {
  const contextRef = ref(context || {}) as Ref<EvalContext>;
  
  const results = computed(() => {
    const output: Record<string, ExpressionResult> = {};
    
    for (const [key, expr] of Object.entries(expressions)) {
      try {
        const value = exec(expr, contextRef.value);
        output[key] = {
          value,
          error: null,
          isValid: true
        };
      } catch (error) {
        const errorMsg = error instanceof ParseError 
          ? error.message 
          : error instanceof Error 
            ? error.message 
            : '未知错误';
        output[key] = {
          value: null,
          error: errorMsg,
          isValid: false
        };
      }
    }
    
    return output;
  });
  
  return {
    results,
    isAllValid: computed(() => 
      Object.values(results.value).every(r => r.isValid)
    ),
    setContext: (ctx: EvalContext) => { contextRef.value = ctx; }
  };
}

/**
 * Use Conditional Visibility
 * 
 * 根据表达式控制元素可见性
 */
export function useConditionalVisibility(
  condition: Ref<string> | string,
  context?: Ref<EvalContext> | EvalContext
) {
  const { value, isValid } = useExpression<boolean>(condition, context);
  
  const isVisible = computed(() => {
    if (!isValid.value) {return false;}
    return Boolean(value.value);
  });
  
  return {
    isVisible,
    isValid
  };
}

/**
 * Use Expression Validator
 * 
 * 验证表达式语法
 */
export function useExpressionValidator(expression: Ref<string> | string) {
  const exprRef = ref(expression) as Ref<string>;
  
  const validation = computed(() => {
    if (!exprRef.value || exprRef.value.trim() === '') {
      return {
        isValid: false,
        error: '表达式不能为空'
      };
    }
    
    try {
      parse(exprRef.value);
      return {
        isValid: true,
        error: null
      };
    } catch (error) {
      const errorMsg = error instanceof ParseError 
        ? error.message 
        : error instanceof Error 
          ? error.message 
          : '未知错误';
      return {
        isValid: false,
        error: errorMsg
      };
    }
  });
  
  return {
    isValid: computed(() => validation.value.isValid),
    error: computed(() => validation.value.error),
    validate: () => validation.value
  };
}

/**
 * Use Expression with Watch
 * 
 * 监听表达式变化并执行回调
 */
export function useExpressionWatch<T = any>(
  expression: Ref<string> | string,
  context: Ref<EvalContext> | EvalContext,
  callback: (value: T | null, isValid: boolean) => void
) {
  const { value, isValid } = useExpression<T>(expression, context);
  
  watch([value, isValid], ([newValue, newIsValid]) => {
    callback(newValue, newIsValid);
  }, { immediate: true });
  
  return {
    value,
    isValid
  };
}

export default {
  useExpression,
  useExpressions,
  useConditionalVisibility,
  useExpressionValidator,
  useExpressionWatch
};
