<template>
  <n-card title="问卷表单示例 - 条件显示逻辑">
    <n-form>
      <n-space vertical :size="20">
        <!-- 问题 1: 年龄 -->
        <n-form-item label="您的年龄">
          <n-input-number
            v-model:value="answers.age"
            :min="0"
            :max="120"
            placeholder="请输入年龄"
          />
        </n-form-item>

        <!-- 问题 2: 国家 -->
        <n-form-item label="您的国家">
          <n-select
            v-model:value="answers.country"
            :options="countryOptions"
            placeholder="请选择国家"
          />
        </n-form-item>

        <!-- 问题 3: 条件显示 - 只有年龄 >= 18 才显示 -->
        <n-form-item
          v-if="showDriverLicenseQuestion.isVisible"
          label="您有驾照吗？"
        >
          <n-radio-group v-model:value="answers.hasDriverLicense">
            <n-space>
              <n-radio :value="true">是</n-radio>
              <n-radio :value="false">否</n-radio>
            </n-space>
          </n-radio-group>
          <template #feedback>
            <n-text depth="3">
              (此问题仅显示给 18 岁及以上的用户)
            </n-text>
          </template>
        </n-form-item>

        <!-- 问题 4: 复杂条件 - 年龄 >= 21 且有驾照 -->
        <n-form-item
          v-if="showAdvancedQuestion.isVisible"
          label="您的驾龄（年）"
        >
          <n-input-number
            v-model:value="answers.drivingYears"
            :min="0"
            placeholder="请输入驾龄"
          />
          <template #feedback>
            <n-text depth="3">
              (此问题仅显示给 21 岁及以上且有驾照的用户)
            </n-text>
          </template>
        </n-form-item>

        <!-- 问题 5: 爱好选择 -->
        <n-form-item label="您的爱好">
          <n-checkbox-group v-model:value="answers.hobbies">
            <n-space>
              <n-checkbox value="reading">阅读</n-checkbox>
              <n-checkbox value="sports">运动</n-checkbox>
              <n-checkbox value="music">音乐</n-checkbox>
              <n-checkbox value="travel">旅行</n-checkbox>
            </n-space>
          </n-checkbox-group>
        </n-form-item>

        <!-- 问题 6: 基于爱好数量的条件显示 -->
        <n-form-item
          v-if="showHobbyDetail.isVisible"
          label="请详细描述您最喜欢的爱好"
        >
          <n-input
            v-model:value="answers.hobbyDetail"
            type="textarea"
            placeholder="请描述..."
            :autosize="{ minRows: 3 }"
          />
          <template #feedback>
            <n-text depth="3">
              (您选择了 {{ answers.hobbies.length }} 个爱好)
            </n-text>
          </template>
        </n-form-item>

        <!-- 调试信息 -->
        <n-divider />
        <n-collapse>
          <n-collapse-item title="调试信息" name="debug">
            <n-space vertical>
              <n-text>
                <strong>当前答案:</strong>
                <pre>{{ JSON.stringify(answers, null, 2) }}</pre>
              </n-text>

              <n-text>
                <strong>表达式评估结果:</strong>
                <ul>
                  <li>显示驾照问题: {{ showDriverLicenseQuestion.isVisible }}</li>
                  <li>显示高级问题: {{ showAdvancedQuestion.isVisible }}</li>
                  <li>显示爱好详情: {{ showHobbyDetail.isVisible }}</li>
                </ul>
              </n-text>

              <n-text>
                <strong>使用的表达式:</strong>
                <ul>
                  <li>驾照问题: <code>{{ expressions.driverLicense }}</code></li>
                  <li>高级问题: <code>{{ expressions.advanced }}</code></li>
                  <li>爱好详情: <code>{{ expressions.hobbyDetail }}</code></li>
                </ul>
              </n-text>
            </n-space>
          </n-collapse-item>
        </n-collapse>

        <!-- 提交按钮 -->
        <n-button type="primary" @click="handleSubmit">
          提交问卷
        </n-button>
      </n-space>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  NCard,
  NForm,
  NFormItem,
  NInputNumber,
  NSelect,
  NRadioGroup,
  NRadio,
  NCheckboxGroup,
  NCheckbox,
  NInput,
  NButton,
  NSpace,
  NText,
  NDivider,
  NCollapse,
  NCollapseItem,
  useMessage
} from 'naive-ui';
import { useConditionalVisibility } from '@/composables/useExpressionEngine';

// 消息提示
const message = useMessage();

// 问卷答案
const answers = ref({
  age: null as number | null,
  country: '',
  hasDriverLicense: false,
  drivingYears: null as number | null,
  hobbies: [] as string[],
  hobbyDetail: ''
});

// 国家选项
const countryOptions = [
  { label: '美国', value: 'US' },
  { label: '中国', value: 'CN' },
  { label: '英国', value: 'UK' },
  { label: '日本', value: 'JP' },
  { label: '其他', value: 'OTHER' }
];

// 表达式定义
const expressions = {
  // 问题 3: 年龄 >= 18 才显示驾照问题
  driverLicense: 'age >= 18',
  
  // 问题 4: 年龄 >= 21 且有驾照才显示高级问题
  advanced: 'age >= 21 && hasDriverLicense == true',
  
  // 问题 6: 选择了 2 个或以上爱好才显示详情
  hobbyDetail: 'count(hobbies) >= 2'
};

// 使用条件可见性
const showDriverLicenseQuestion = useConditionalVisibility(
  computed(() => expressions.driverLicense),
  answers
);

const showAdvancedQuestion = useConditionalVisibility(
  computed(() => expressions.advanced),
  answers
);

const showHobbyDetail = useConditionalVisibility(
  computed(() => expressions.hobbyDetail),
  answers
);

// 提交处理
function handleSubmit() {
  // 验证必填字段
  if (!answers.value.age) {
    message.warning('请输入年龄');
    return;
  }
  
  if (!answers.value.country) {
    message.warning('请选择国家');
    return;
  }
  
  // 根据条件验证
  if (showAdvancedQuestion.isVisible && !answers.value.drivingYears) {
    message.warning('请输入驾龄');
    return;
  }
  
  if (showHobbyDetail.isVisible && !answers.value.hobbyDetail) {
    message.warning('请详细描述您的爱好');
    return;
  }
  
  // 提交成功
  message.success('问卷提交成功！');
  // eslint-disable-next-line no-console
  console.log('提交的答案:', answers.value);
}
</script>

<style scoped>
pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}

code {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 2px;
  font-family: monospace;
}

ul {
  margin: 10px 0;
  padding-left: 20px;
}
</style>
