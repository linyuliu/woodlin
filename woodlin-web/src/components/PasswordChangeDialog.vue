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
      <div v-if="message" class="message" :class="messageType">
        <NIcon :component="InfoCircleOutlined" />
        {{ message }}
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
import { NModal, NForm, NFormItem, NInput, NIcon, useMessage } from 'naive-ui'
import { InfoCircleOutlined } from '@vicons/antd'
import axios from 'axios'

interface Props {
  show: boolean
  required?: boolean // 是否强制修改密码
  message?: string
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
  messageType: 'info'
})

const emit = defineEmits<Emits>()

const message = useMessage()
const formRef = ref()

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
    validator: (rule: any, value: string) => {
      if (!value) return new Error('请输入新密码')
      if (value.length < passwordPolicy.minLength) {
        return new Error(`密码长度不能少于${passwordPolicy.minLength}位`)
      }
      if (value.length > passwordPolicy.maxLength) {
        return new Error(`密码长度不能超过${passwordPolicy.maxLength}位`)
      }
      
      if (passwordPolicy.strongPasswordRequired) {
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
      }
      
      return true
    }
  },
  confirmPassword: {
    required: true,
    message: '请确认新密码',
    trigger: ['input', 'blur'],
    validator: (rule: any, value: string) => {
      if (value !== form.newPassword) {
        return new Error('两次输入的密码不一致')
      }
      return true
    }
  }
}

// 重置表单
const resetForm = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

// 确认修改密码
const handleConfirm = async () => {
  try {
    await formRef.value?.validate()
    
    const response = await axios.post('/api/auth/change-password', {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    
    message.success('密码修改成功')
    resetForm()
    emit('success')
    visible.value = false
  } catch (error: any) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('密码修改失败，请重试')
    }
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
  align-items: center;
  gap: 8px;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
  font-size: 14px;
}

.message.info {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
}

.message.warning {
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  color: #fa8c16;
}

.message.error {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  color: #f5222d;
}

.password-tips {
  margin-top: 16px;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;
}

.password-tips h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  font-size: 12px;
  color: #666;
}

.password-tips li {
  margin-bottom: 4px;
}
</style>