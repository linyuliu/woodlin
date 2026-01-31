<script setup lang="ts">
import {ref} from 'vue'
import {
  NButton,
  NCard,
  NCheckbox,
  NDivider,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  useMessage
} from 'naive-ui'
import {LockClosedOutline, PersonOutline} from '@vicons/ionicons5'
import {useRoute, useRouter} from 'vue-router'
import {type LoginRequest} from '@/api/auth'
import {useAuthStore} from '@/stores'
import PasswordChangeDialog from '@/components/PasswordChangeDialog.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()

const loginForm = ref({
  username: 'admin',
  password: 'Passw0rd',
  rememberMe: false
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
      password: loginForm.value.password,
      rememberMe: loginForm.value.rememberMe
    }

    const data = await authStore.doLogin(loginRequest)

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

      // 跳转到重定向地址或首页
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else {
      // 正常登录
      message.success('登录成功')

      // 跳转到重定向地址或首页
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    }

  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string }
    if (err.response?.data?.message) {
      message.error(err.response.data.message)
    } else if (err.message) {
      message.error(err.message)
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
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }, 1000)
}

// 如果不是强制修改密码，允许取消
const handlePasswordChangeCancel = () => {
  if (!passwordChangeRequired.value) {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }
}
</script>

<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌展示 -->
      <div class="brand-section">
        <div class="brand-content">
          <img alt="Woodlin" class="brand-logo" src="@/assets/logo.svg"/>
          <h1 class="brand-title">Woodlin</h1>
          <p class="brand-subtitle">多租户中后台管理系统</p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">✨</span>
              <span>现代化技术栈</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>企业级安全防护</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🏢</span>
              <span>完善的多租户支持</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <NCard :bordered="false" class="login-card">
          <div class="card-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账户</p>
          </div>

          <NForm :model="loginForm" class="login-form" size="large">
            <NFormItem>
              <NInput
                v-model:value="loginForm.username"
                :maxlength="50"
                placeholder="用户名"
              >
                <template #prefix>
                  <NIcon color="var(--text-color-tertiary)">
                    <PersonOutline/>
                  </NIcon>
                </template>
              </NInput>
            </NFormItem>
            <NFormItem>
              <NInput
                v-model:value="loginForm.password"
                :maxlength="50"
                placeholder="密码"
                show-password-on="click"
                type="password"
                @keydown.enter="handleLogin"
              >
                <template #prefix>
                  <NIcon color="var(--text-color-tertiary)">
                    <LockClosedOutline/>
                  </NIcon>
                </template>
              </NInput>
            </NFormItem>
            <NFormItem>
              <div class="form-options">
                <NCheckbox v-model:checked="loginForm.rememberMe">
                  记住我
                </NCheckbox>
                <a class="forgot-link" href="#">忘记密码?</a>
              </div>
            </NFormItem>
            <NFormItem>
              <NButton
                :loading="loading"
                block
                class="login-btn"
                size="large"
                type="primary"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </NButton>
            </NFormItem>
          </NForm>

          <div class="login-footer">
            <NDivider>
              <span class="divider-text">演示账号</span>
            </NDivider>
            <div class="demo-account">
              <span>用户名: <code>admin</code></span>
              <span>密码: <code>Passw0rd</code></span>
            </div>
          </div>
        </NCard>
      </div>
    </div>

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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  left: -200px;
  animation: float 20s ease-in-out infinite;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -100px;
  animation: float 15s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 10%;
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, 30px) scale(1.05);
  }
}

/* 登录包装器 */
.login-wrapper {
  display: flex;
  max-width: 900px;
  width: 100%;
  background: var(--bg-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, var(--primary-color) 0%, #36ad6a 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-content {
  text-align: center;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 24px;
  filter: brightness(0) invert(1);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: -1px;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 32px 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.95;
}

.feature-icon {
  font-size: 18px;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  padding: 48px;
  display: flex;
  align-items: center;
}

.login-card {
  width: 100%;
  background: transparent !important;
  box-shadow: none !important;
}

.card-header {
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0 0 8px 0;
}

.card-header p {
  font-size: 14px;
  color: var(--text-color-tertiary);
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-form :deep(.n-input) {
  border-radius: var(--radius-md) !important;
  height: 48px;
}

.login-form :deep(.n-input__input-el) {
  height: 48px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.forgot-link {
  font-size: 13px;
  color: var(--primary-color);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.forgot-link:hover {
  color: var(--primary-color-hover);
}

.login-btn {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-md) !important;
}

.login-footer {
  margin-top: 24px;
}

.divider-text {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.demo-account {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.demo-account code {
  background: var(--bg-color-tertiary);
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  font-family: monospace;
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }

  .brand-section {
    padding: 32px;
  }

  .brand-logo {
    width: 60px;
    height: 60px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 32px;
  }
}
</style>
