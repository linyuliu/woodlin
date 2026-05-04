<!--
  @file views/login/index.vue
  @description 登录页：渐变背景 + 卡片式表单，含校验、记住我、加载态、登出提示
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  type FormInst,
  type FormRules,
  NButton,
  NCard,
  NCheckbox,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSpace,
  NText,
  useMessage,
  useNotification,
} from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'
import { settings } from '@/config/settings'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()
const notification = useNotification()

const REMEMBER_KEY = 'woodlin_login_remember'

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  remember: false,
})

const rules: FormRules = {
  username: {
    required: true,
    trigger: ['blur', 'input'],
    message: '请输入用户名',
  },
  password: {
    required: true,
    trigger: ['blur', 'input'],
    message: '请输入密码',
  },
}

/** 启动时：恢复记住的用户名 / 处理登出提示 */
onMounted(() => {
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    form.username = remembered
    form.remember = true
  } else {
    form.username = 'admin'
    form.password = 'Passw0rd'
  }
  if (route.query.logout === 'true') {
    notification.info({
      title: '已退出登录',
      content: '您已安全退出，请重新登录。',
      duration: 3000,
    })
  }
})

/** 持久化 / 清除记住的用户名 */
function persistRemember(): void {
  if (form.remember) {
    localStorage.setItem(REMEMBER_KEY, form.username)
  } else {
    localStorage.removeItem(REMEMBER_KEY)
  }
}

/** 提交登录 */
async function handleSubmit(e?: Event): Promise<void> {
  e?.preventDefault()
  errorMsg.value = ''
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await userStore.login({
      username: form.username,
      password: form.password,
    })
    persistRemember()
    message.success(res.message || '登录成功')
    if (res.requirePasswordChange) {
      message.warning('密码已过期，请先修改密码')
      void router.push('/user/pwd-expired')
      return
    }
    if (res.passwordExpiringSoon) {
      message.info('密码即将过期，请尽快修改')
    }
    const redirect = (route.query.redirect as string) || settings.homePath
    void router.push(redirect)
  } catch (err: unknown) {
    const msg =
      (err as { message?: string })?.message || '登录失败，请检查用户名和密码'
    errorMsg.value = msg
    console.error('[login] failed', err)
  } finally {
    loading.value = false
  }
}

/** 忘记密码：弹出对话框，由用户提交账号信息后给出指引 */
const forgotVisible = ref(false)
const forgotSubmitting = ref(false)
const forgotForm = reactive({
  account: '',
})
const forgotRules: FormRules = {
  account: {
    required: true,
    trigger: ['blur', 'input'],
    message: '请输入用户名或邮箱',
  },
}
const forgotFormRef = ref<FormInst | null>(null)

function handleForgotPassword(): void {
  forgotForm.account = form.username
  forgotVisible.value = true
}

async function handleForgotSubmit(): Promise<void> {
  try {
    await forgotFormRef.value?.validate()
  } catch {
    return
  }
  forgotSubmitting.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 400))
    notification.info({
      title: '已提交重置申请',
      content: `账号「${forgotForm.account}」的密码重置请求已记录，请联系系统管理员协助重置。`,
      duration: 5000,
    })
    forgotVisible.value = false
  } finally {
    forgotSubmitting.value = false
  }
}
</script>

<template>
  <div class="login">
    <NCard class="login__card" :bordered="false">
      <div class="login__header">
        <img class="login__logo" :src="settings.logo" alt="logo" />
        <h1 class="login__title">{{ settings.title }}</h1>
        <p class="login__subtitle">企业级多租户管理平台</p>
      </div>
      <NForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
        :show-label="false"
        size="large"
        @submit="handleSubmit"
      >
        <NFormItem path="username" :show-feedback="false">
          <NInput
            v-model:value="form.username"
            placeholder="请输入用户名"
            clearable
            autocomplete="username"
          />
        </NFormItem>
        <NFormItem path="password" style="margin-top: 18px" :show-feedback="false">
          <NInput
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            autocomplete="current-password"
            @keyup.enter="handleSubmit"
          />
        </NFormItem>
        <div class="login__row">
          <NCheckbox v-model:checked="form.remember">记住我</NCheckbox>
          <a class="login__link" @click="handleForgotPassword">忘记密码?</a>
        </div>
        <div v-if="errorMsg" class="login__error">{{ errorMsg }}</div>
        <NButton
          type="primary"
          block
          attr-type="submit"
          :loading="loading"
          style="margin-top: 12px"
          @click="handleSubmit"
        >
          登 录
        </NButton>
      </NForm>
      <div class="login__footer">© {{ new Date().getFullYear() }} Woodlin</div>
    </NCard>

    <NModal
      v-model:show="forgotVisible"
      preset="card"
      title="找回密码"
      style="width: 420px"
      :mask-closable="!forgotSubmitting"
      :close-on-esc="!forgotSubmitting"
    >
      <NSpace vertical size="large">
        <NText depth="3">
          请输入您的账号。出于安全考虑，密码重置操作需由系统管理员审核处理。
        </NText>
        <NForm
          ref="forgotFormRef"
          :model="forgotForm"
          :rules="forgotRules"
          label-placement="top"
          :show-label="false"
        >
          <NFormItem path="account">
            <NInput
              v-model:value="forgotForm.account"
              placeholder="请输入用户名或邮箱"
              clearable
            />
          </NFormItem>
        </NForm>
        <NSpace justify="end">
          <NButton :disabled="forgotSubmitting" @click="forgotVisible = false">
            取消
          </NButton>
          <NButton
            type="primary"
            :loading="forgotSubmitting"
            @click="handleForgotSubmit"
          >
            提交申请
          </NButton>
        </NSpace>
      </NSpace>
    </NModal>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #4f8bff 0%, #6f5cff 50%, #8a4fff 100%);
  position: relative;
  overflow: hidden;
}
.login::before,
.login::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  filter: blur(60px);
}
.login::before {
  width: 420px;
  height: 420px;
  top: -120px;
  left: -120px;
}
.login::after {
  width: 360px;
  height: 360px;
  bottom: -100px;
  right: -100px;
}
.login__card {
  position: relative;
  width: 400px;
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px);
}
.login__header {
  text-align: center;
  margin-bottom: 24px;
}
.login__logo {
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
}
.login__title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--n-title-text-color, #1f2937);
}
.login__subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #8c95a6;
}
.login__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 18px 0 4px;
  font-size: 13px;
}
.login__link {
  color: #4f8bff;
  cursor: pointer;
  user-select: none;
}
.login__link:hover {
  text-decoration: underline;
}
.login__error {
  color: #d03050;
  font-size: 13px;
  margin: 8px 0 0;
}
.login__footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #b0b6c2;
}
</style>
