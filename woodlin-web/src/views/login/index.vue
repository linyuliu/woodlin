<!--
  @file views/login/index.vue
  @description 登录页
  @author yulin
  @since 2026-05-04
-->
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/modules/user'
import { useRouteStore } from '@/stores/modules/route'
import { settings } from '@/config/settings'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const routeStore = useRouteStore()
const message = useMessage()

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Passw0rd',
})

/** 提交登录 */
async function handleSubmit(): Promise<void> {
  if (!form.username || !form.password) {
    message.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await userStore.login({ ...form })
    routeStore.resetRoutes()
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
  } catch (err) {
    console.error('[login] failed', err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <NCard class="login__card" :title="settings.title">
      <template #header-extra>
        <img :src="settings.logo" alt="logo" width="32" height="32" />
      </template>
      <NForm label-placement="top" :show-feedback="false" @submit.prevent="handleSubmit">
        <NFormItem label="用户名">
          <NInput v-model:value="form.username" placeholder="请输入用户名" clearable />
        </NFormItem>
        <NFormItem label="密码" style="margin-top: 16px">
          <NInput
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            @keyup.enter="handleSubmit"
          />
        </NFormItem>
        <NButton
          type="primary"
          block
          attr-type="submit"
          :loading="loading"
          style="margin-top: 24px"
          @click="handleSubmit"
        >
          登录
        </NButton>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #e0eaff 0%, #f5f7fa 100%);
}
.login__card {
  width: 360px;
}
</style>
