<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace } from 'naive-ui'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginForm = ref({
  username: 'admin',
  password: '123456'
})

const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('登录:', loginForm.value)
  router.push('/')
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
            @click="handleLogin"
          >
            登录
          </NButton>
        </NFormItem>
      </NForm>
      
      <div class="login-footer">
        <p style="text-align: center; color: #999; margin-top: 20px;">
          默认账号: admin / 123456
        </p>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-footer {
  margin-top: 16px;
}
</style>