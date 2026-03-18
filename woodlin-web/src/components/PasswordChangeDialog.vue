<template>
  <NModal 
    v-model:show="visible" 
    preset="dialog" 
    title="修改密码"
    positive-text="确认"
    negative-text="取消"
    :closable="!required"
    :close-on-esc="!required"
    :mask-closable="!required"
    @positive-click="handleConfirm"
    @negative-click="handleCancel"
  >
    <div class="password-change-form">
      <div v-if="messageText" class="message" :class="messageType">
        <NIcon :component="InformationCircle" />
        {{ messageText }}
      </div>
      
      <NForm ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="auto">
        <NFormItem label="旧密码" path="oldPassword">
          <NInput 
            v-model:value="form.oldPassword" 
            type="password" 
            placeholder="请输入旧密码"
            :maxlength="100"
          />
        </NFormItem>
        
        <NFormItem label="新密码" path="newPassword">
          <NInput 
            v-model:value="form.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            :maxlength="20"
          />
        </NFormItem>
        
        <NFormItem label="确认密码" path="confirmPassword">
          <NInput 
            v-model:value="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            :maxlength="20"
          />
        </NFormItem>
      </NForm>
      
      <div v-if="passwordPolicy.strongPasswordRequired" class="password-tips">
        <h4>密码要求：</h4>
        <ul>
          <li v-if="passwordPolicy.minLength">至少 {{ passwordPolicy.minLength }} 位字符</li>
          <li v-if="passwordPolicy.requireDigits">包含数字</li>
          <li v-if="passwordPolicy.requireLowercase">包含小写字母</li>
          <li v-if="passwordPolicy.requireUppercase">包含大写字母</li>
          <li v-if="passwordPolicy.requireSpecialChars">包含特殊字符</li>
        </ul>
      </div>
    </div>
  </NModal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { NModal, NForm, NFormItem, NInput, NIcon, useMessage, type FormInst } from 'naive-ui'
import { InformationCircle } from '@vicons/ionicons5'
import { changePassword } from '@/api/auth'

interface Props {
  show: boolean
  required?: boolean
  messageText?: string
  messageType?: 'info' | 'warning' | 'error'
  daysUntilExpiration?: number
}

interface Emits {
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  required: false,
  messageType: 'info',
  messageText: '',
  daysUntilExpiration: 0
})

const emit = defineEmits<Emits>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码策略配置（可以从API获取）
const passwordPolicy = reactive({
  strongPasswordRequired: false,
  minLength: 6,
  maxLength: 20,
  requireDigits: false,
  requireLowercase: false,
  requireUppercase: false,
  requireSpecialChars: false
})

/**
 * 检查密码基本要求
 */
const checkBasicPasswordRequirements = (value: string): Error | null => {
  if (!value) {
    return new Error('请输入新密码')
  }
  if (value.length < passwordPolicy.minLength) {
    return new Error(`密码长度不能少于${passwordPolicy.minLength}位`)
  }
  if (value.length > passwordPolicy.maxLength) {
    return new Error(`密码长度不能超过${passwordPolicy.maxLength}位`)
  }
  return null
}

/**
 * 检查强密码要求
 */
const checkStrongPasswordRequirements = (value: string): Error | null => {
  if (!passwordPolicy.strongPasswordRequired) {
    return null
  }
  
  if (passwordPolicy.requireDigits && !/\d/.test(value)) {
    return new Error('密码必须包含数字')
  }
  if (passwordPolicy.requireLowercase && !/[a-z]/.test(value)) {
    return new Error('密码必须包含小写字母')
  }
  if (passwordPolicy.requireUppercase && !/[A-Z]/.test(value)) {
    return new Error('密码必须包含大写字母')
  }
  if (passwordPolicy.requireSpecialChars && !/[!@#$%^&*()_+\-=\[\]{};':"|,.<>?]/.test(value)) {
    return new Error('密码必须包含特殊字符')
  }
  
  return null
}

const rules = {
  oldPassword: {
    required: true,
    message: '请输入旧密码',
    trigger: ['input', 'blur']
  },
  newPassword: {
    required: true,
    message: '请输入新密码',
    trigger: ['input', 'blur'],
    validator: (_rule: unknown, value: string) => {
      const basicError = checkBasicPasswordRequirements(value)
      if (basicError) {
        return basicError
      }
      
      const strongError = checkStrongPasswordRequirements(value)
      if (strongError) {
        return strongError
      }
      
      return true
    }
  },
  confirmPassword: {
    required: true,
    message: '请确认新密码',
    trigger: ['input', 'blur'],
    validator: (_rule: unknown, value: string) => {
      if (value !== form.newPassword) {
        return new Error('两次输入的密码不一致')
      }
      return true
    }
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

/**
 * 确认修改密码
 */
const handleConfirm = async () => {
  try {
    await formRef.value?.validate()

    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })

    message.success('密码修改成功，请重新登录')
    resetForm()
    emit('success')
    visible.value = false
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : '密码修改失败，请重试'
    message.error(errorMessage)
  }
}

// 取消修改
const handleCancel = () => {
  if (!props.required) {
    resetForm()
    emit('cancel')
    visible.value = false
  }
}

// 监听对话框显示状态，重置表单
watch(visible, (newVisible) => {
  if (newVisible) {
    resetForm()
  }
})
</script>

<style scoped>
.password-change-form {
  padding: 16px 0;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 20px;
  border-radius: var(--radius-md);
  border: 1px solid;
  font-size: 14px;
}

.message.info {
  background-color: var(--message-info-bg);
  border-color: var(--message-info-border);
  color: var(--message-info-text);
}

.message.warning {
  background-color: var(--message-warning-bg);
  border-color: var(--message-warning-border);
  color: var(--message-warning-text);
}

.message.error {
  background-color: var(--message-error-bg);
  border-color: var(--message-error-border);
  color: var(--message-error-text);
}

.password-tips {
  margin-top: 16px;
  padding: 12px 16px;
  background-color: var(--bg-color-tertiary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
}

.password-tips h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  color: var(--text-color-secondary);
}

.password-tips li {
  margin: 4px 0;
  font-size: 12px;
}
</style>
