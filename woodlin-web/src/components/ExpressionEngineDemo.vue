<template>
  <n-card title="表达式引擎演示">
    <n-space vertical :size="20">
      <!-- 表达式输入 -->
      <n-form-item label="表达式">
        <n-input
          v-model:value="expression"
          placeholder="输入表达式，例如: x + y * 2"
          :status="expressionValidation.isValid ? 'success' : 'error'"
        />
        <template v-if="!expressionValidation.isValid" #feedback>
          {{ expressionValidation.error }}
        </template>
      </n-form-item>

      <!-- 上下文输入 -->
      <n-form-item label="上下文 (JSON)">
        <n-input
          v-model:value="contextJson"
          type="textarea"
          placeholder='{"x": 10, "y": 20}'
          :autosize="{ minRows: 3, maxRows: 10 }"
        />
      </n-form-item>

      <!-- 评估结果 -->
      <n-form-item label="结果">
        <n-alert
          v-if="result.isValid"
          type="success"
          :title="`结果: ${JSON.stringify(result.value)}`"
        />
        <n-alert
          v-else
          type="error"
          :title="result.error || '评估失败'"
        />
      </n-form-item>

      <!-- 示例表达式 -->
      <n-divider />
      <n-text strong>示例表达式</n-text>
      <n-space>
        <n-button
          v-for="example in examples"
          :key="example.expr"
          size="small"
          @click="loadExample(example)"
        >
          {{ example.name }}
        </n-button>
      </n-space>
    </n-space>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { NCard, NSpace, NFormItem, NInput, NAlert, NDivider, NText, NButton } from 'naive-ui';
import { useExpression, useExpressionValidator } from '@/composables/useExpressionEngine';

// 表达式和上下文
const expression = ref('x + y * 2');
const contextJson = ref('{"x": 10, "y": 20}');

// 解析上下文 JSON
const context = computed(() => {
  try {
    return JSON.parse(contextJson.value);
  } catch {
    return {};
  }
});

// 使用表达式评估
const { result } = useExpression(expression, context);

// 使用表达式验证
const { isValid: isExprValid, error: exprError } = useExpressionValidator(expression);

const expressionValidation = computed(() => ({
  isValid: isExprValid.value,
  error: exprError.value
}));

// 示例表达式
interface Example {
  name: string;
  expr: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  context: Record<string, any>;
}

const examples: Example[] = [
  {
    name: '算术运算',
    expr: '(x + y) * z',
    context: { x: 5, y: 10, z: 2 }
  },
  {
    name: '比较运算',
    expr: 'age >= 18 && country == "US"',
    context: { age: 25, country: 'US' }
  },
  {
    name: '三元表达式',
    expr: 'score >= 90 ? "A" : score >= 80 ? "B" : "C"',
    context: { score: 85 }
  },
  {
    name: '数组函数',
    expr: 'sum(scores) / count(scores)',
    context: { scores: [85, 92, 78, 95, 88] }
  },
  {
    name: 'Lambda 表达式',
    expr: 'countIf(numbers, x => x > 5)',
    context: { numbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] }
  },
  {
    name: '属性访问',
    expr: 'user.age > 18 && user.name == "Alice"',
    context: { user: { name: 'Alice', age: 25 } }
  },
  {
    name: '数组索引',
    expr: 'items[0] + items[2]',
    context: { items: [10, 20, 30, 40] }
  }
];

// 加载示例
function loadExample(example: Example) {
  expression.value = example.expr;
  contextJson.value = JSON.stringify(example.context, null, 2);
}
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
