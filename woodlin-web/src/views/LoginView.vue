<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  NButton,
  NCard,
  NCheckbox,
  NDivider,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NTag,
  useMessage
} from 'naive-ui'
import {
  GitNetworkOutline,
  LockClosedOutline,
  MoonOutline,
  PersonOutline,
  ServerOutline,
  ShieldCheckmarkOutline,
  SunnyOutline
} from '@vicons/ionicons5'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { type LoginRequest } from '@/api/auth'
import { useAppStore, useAuthStore } from '@/stores'
import PasswordChangeDialog from '@/components/PasswordChangeDialog.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()
const appStore = useAppStore()
const { isDarkMode } = storeToRefs(appStore)

const loginForm = ref({
  username: 'admin',
  password: '12345678',
  rememberMe: true
})

const loading = ref(false)
const showPasswordChange = ref(false)
const passwordChangeRequired = ref(false)
const passwordChangeMessage = ref('')
const passwordChangeMessageType = ref<'info' | 'warning' | 'error'>('info')

const heroItems = [
  { label: '元数据治理', desc: '按库/Schema/表/字段完整采集', icon: ServerOutline },
  { label: 'ETL 编排', desc: '可复用字段映射与转换策略', icon: GitNetworkOutline },
  { label: '企业级权限', desc: '多租户与分层权限可审计', icon: ShieldCheckmarkOutline }
]

const loginStatus = computed(() => (loading.value ? '认证中...' : '输入账号密码继续'))

const handleToggleTheme = () => {
  appStore.toggleThemeMode()
}

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

    if (data.requirePasswordChange) {
      passwordChangeRequired.value = true
      passwordChangeMessage.value = data.message || '需要修改密码'
      passwordChangeMessageType.value = 'warning'
      showPasswordChange.value = true
    } else if (data.passwordExpiringSoon) {
      message.warning(`${data.message}，建议及时修改密码`)
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else {
      message.success('登录成功')
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

const handlePasswordChangeSuccess = () => {
  message.success('密码修改成功，正在跳转...')
  setTimeout(() => {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }, 1000)
}

const handlePasswordChangeCancel = () => {
  if (!passwordChangeRequired.value) {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  }
}
</script>

<template>
  <div class="login-page">
    <div class="atmosphere">
      <span class="orb orb-a"></span>
      <span class="orb orb-b"></span>
      <span class="orb orb-c"></span>
    </div>

    <div class="top-actions">
      <n-button tertiary size="small" @click="handleToggleTheme">
        <template #icon>
          <n-icon>
            <sunny-outline v-if="isDarkMode" />
            <moon-outline v-else />
          </n-icon>
        </template>
        {{ isDarkMode ? '浅色' : '深色' }}
      </n-button>
    </div>

    <div class="login-shell">
      <section class="brand-panel">
        <n-tag size="small" :bordered="false" type="success">Woodlin Data Hub</n-tag>
        <h1>数据中台控制台</h1>
        <p>统一接入数据源，完善元数据语义，为 ETL 与 CDC 提供可追踪基础。</p>

        <div class="hero-list">
          <div v-for="item in heroItems" :key="item.label" class="hero-item">
            <n-icon size="18"><component :is="item.icon" /></n-icon>
            <div>
              <h3>{{ item.label }}</h3>
              <span>{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <n-card :bordered="false" class="login-card">
          <div class="form-head">
            <h2>账号登录</h2>
            <span>{{ loginStatus }}</span>
          </div>

          <n-form :model="loginForm" class="login-form" size="large">
            <n-form-item>
              <n-input v-model:value="loginForm.username" :maxlength="50" placeholder="用户名">
                <template #prefix>
                  <n-icon color="var(--text-color-tertiary)">
                    <person-outline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>
            <n-form-item>
              <n-input
                v-model:value="loginForm.password"
                :maxlength="50"
                placeholder="密码"
                show-password-on="click"
                type="password"
                @keydown.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon color="var(--text-color-tertiary)">
                    <lock-closed-outline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>
            <n-form-item>
              <div class="form-options">
                <n-checkbox v-model:checked="loginForm.rememberMe">记住我</n-checkbox>
                <a class="forgot-link" href="#">忘记密码?</a>
              </div>
            </n-form-item>
            <n-form-item>
              <n-button :loading="loading" block class="login-btn" size="large" type="primary" @click="handleLogin">
                {{ loading ? '登录中...' : '登录系统' }}
              </n-button>
            </n-form-item>
          </n-form>

          <div class="login-footer">
            <n-divider><span class="divider-text">默认账号</span></n-divider>
            <div class="demo-account">
              <span>用户名: <code>admin</code></span>
              <span>密码: <code>12345678</code></span>
            </div>
          </div>
        </n-card>
      </section>
    </div>

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
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-gradient);
  color: var(--text-color-primary);
  position: relative;
  overflow: hidden;
  padding: 28px;
}

.atmosphere {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}

.orb-a {
  width: 540px;
  height: 540px;
  left: -200px;
  top: -140px;
  background: rgba(14, 165, 233, 0.18);
}

.orb-b {
  width: 380px;
  height: 380px;
  right: -100px;
  top: 6%;
  background: rgba(16, 185, 129, 0.22);
}

.orb-c {
  width: 260px;
  height: 260px;
  left: 18%;
  bottom: -130px;
  background: rgba(56, 189, 248, 0.18);
}

.top-actions {
  position: absolute;
  right: 24px;
  top: 24px;
  z-index: 2;
}

.login-shell {
  width: min(1100px, 100%);
  min-height: 620px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: color-mix(in srgb, var(--bg-color) 84%, transparent);
  border: 1px solid color-mix(in srgb, var(--border-color-light) 72%, transparent);
  box-shadow: var(--shadow-xl);
  backdrop-filter: blur(10px);
  z-index: 1;
}

.brand-panel {
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    radial-gradient(circle at 80% 10%, rgba(45, 212, 191, 0.16), transparent 40%),
    linear-gradient(145deg, color-mix(in srgb, var(--primary-color) 22%, transparent), transparent 55%);
  border-right: 1px solid color-mix(in srgb, var(--border-color-light) 64%, transparent);
}

.brand-panel h1 {
  margin-top: 14px;
  font-size: clamp(32px, 3vw, 44px);
  line-height: 1.1;
  letter-spacing: -0.5px;
}

.brand-panel p {
  margin-top: 14px;
  color: var(--text-color-secondary);
  max-width: 460px;
}

.hero-list {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid color-mix(in srgb, var(--border-color-light) 56%, transparent);
  background: color-mix(in srgb, var(--bg-color) 78%, transparent);
}

.hero-item h3 {
  margin: 0;
  font-size: 15px;
}

.hero-item span {
  margin-top: 2px;
  display: inline-block;
  font-size: 13px;
  color: var(--text-color-tertiary);
}

.form-panel {
  padding: 48px;
  display: flex;
  align-items: center;
}

.login-card {
  width: 100%;
}

.form-head {
  margin-bottom: 26px;
}

.form-head h2 {
  margin: 0;
  font-size: 28px;
}

.form-head span {
  display: inline-block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-color-tertiary);
}

.login-form :deep(.n-input) {
  border-radius: var(--radius-md) !important;
  height: 46px;
}

.login-form :deep(.n-input__input-el) {
  height: 46px;
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
}

.login-btn {
  height: 46px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-md) !important;
}

.login-footer {
  margin-top: 18px;
}

.divider-text {
  font-size: 12px;
  color: var(--text-color-tertiary);
}

.demo-account {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.demo-account code {
  background: var(--bg-color-tertiary);
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  color: var(--primary-color);
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    border-right: none;
    border-bottom: 1px solid color-mix(in srgb, var(--border-color-light) 64%, transparent);
    padding: 28px;
  }

  .form-panel {
    padding: 28px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 14px;
  }

  .top-actions {
    right: 12px;
    top: 12px;
  }

  .hero-list {
    display: none;
  }

  .demo-account {
    flex-direction: column;
    gap: 6px;
    align-items: center;
  }
}
</style>
