<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { login, type LoginRequest } from '@/api/auth'
import PasswordChangeDialog from '@/components/PasswordChangeDialog.vue'

const router = useRouter()
const message = useMessage()

const loginForm = ref({
  username: 'admin',
  password: 'Passw0rd'
})

const loading = ref(false)
const showPasswordChange = ref(false)
const passwordChangeRequired = ref(false)
const passwordChangeMessage = ref('')
const passwordChangeMessageType = ref<'info' | 'warning' | 'error'>('info')

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    message.error('请输入用户名和密码')
    return
  }
  
  loading.value = true
  
  try {
    const loginRequest: LoginRequest = {
      loginType: 'password',
      username: loginForm.value.username,
      password: loginForm.value.password
    }
    
    const data = await login(loginRequest)
    
    // 存储token
    localStorage.setItem('token', data.token)
    
    // 处理密码策略
    if (data.requirePasswordChange) {
      // 需要强制修改密码
      passwordChangeRequired.value = true
      passwordChangeMessage.value = data.message || '需要修改密码'
      passwordChangeMessageType.value = 'warning'
      showPasswordChange.value = true
    } else if (data.passwordExpiringSoon) {
      // 密码即将过期，提醒修改
      message.warning(`${data.message}，建议及时修改密码`)
      router.push('/')
    } else {
      // 正常登录
      message.success('登录成功')
      router.push('/')
    }
    
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    if (err.response?.data?.message) {
      message.error(err.response.data.message)
    } else {
      message.error('登录失败，请检查用户名和密码')
    }
  } finally {
    loading.value = false
  }
}

// 密码修改成功后的处理
const handlePasswordChangeSuccess = () => {
  message.success('密码修改成功，正在跳转...')
  setTimeout(() => {
    router.push('/')
  }, 1000)
}

// 如果不是强制修改密码，允许取消
const handlePasswordChangeCancel = () => {
  if (!passwordChangeRequired.value) {
    router.push('/')
  }
}
</script>

<template>
  <div class="login-container">
    <NCard class="login-card" title="🌲 Woodlin 管理系统">
      <template #header-extra>
        <span style="color: #999; font-size: 14px;">多租户中后台管理系统</span>
      </template>
      
      <NForm :model="loginForm" size="large">
        <NFormItem>
          <NInput 
            v-model:value="loginForm.username" 
            placeholder="用户名"
            :maxlength="50"
          />
        </NFormItem>
        <NFormItem>
          <NInput 
            v-model:value="loginForm.password" 
            type="password"
            placeholder="密码"
            :maxlength="50"
            @keydown.enter="handleLogin"
          />
        </NFormItem>
        <NFormItem>
          <NButton 
            type="primary" 
            block 
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </NButton>
        </NFormItem>
      </NForm>
      
      <div class="login-footer">
        <p style="text-align: center; color: #999; margin-top: 20px;">
          默认账号: admin / Passw0rd
        </p>
      </div>
    </NCard>
    
    <!-- 密码修改对话框 -->
    <PasswordChangeDialog
      v-model:show="showPasswordChange"
      :required="passwordChangeRequired"
      :message="passwordChangeMessage"
      :messageType="passwordChangeMessageType"
      @success="handlePasswordChangeSuccess"
      @cancel="handlePasswordChangeCancel"
    />
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰性背景元素 */
.login-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  top: -200px;
  left: -200px;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  bottom: -150px;
  right: -150px;
}

.login-card {
  width: 420px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-footer {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    width: 90%;
    max-width: 400px;
  }
}
</style>