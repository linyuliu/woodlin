<!--
  @file views/user/pwd-expired/index.vue
  @description 密码过期强制修改页（占位实现）
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  type FormInst,
  type FormRules,
  NButton,
  NCard,
  NForm,
  NFormItem,
  NInput,
  useMessage,
} from 'naive-ui'

const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirm: '',
})

const rules: FormRules = {
  oldPassword: { required: true, message: '请输入原密码', trigger: 'blur' },
  newPassword: {
    required: true,
    message: '请输入新密码（至少 8 位）',
    trigger: 'blur',
    validator: (_r, v: string) => !!v && v.length >= 8,
  },
  confirm: {
    required: true,
    trigger: 'blur',
    validator: (_r, v: string) =>
      v === form.newPassword || new Error('两次输入的密码不一致'),
  },
}

/** 提交修改密码（占位：当前仅做表单校验后跳转登录页） */
async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    // TODO: 调用 /auth/change-password 接口
    message.success('密码修改成功，请重新登录')
    void router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="pwd-expired">
    <NCard title="请修改密码" class="pwd-expired__card">
      <p class="pwd-expired__tip">
        您的密码已过期或为初始密码，为保障账号安全请立即修改。
      </p>
      <NForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
        size="large"
      >
        <NFormItem label="原密码" path="oldPassword">
          <NInput
            v-model:value="form.oldPassword"
            type="password"
            show-password-on="click"
            placeholder="请输入原密码"
          />
        </NFormItem>
        <NFormItem label="新密码" path="newPassword">
          <NInput
            v-model:value="form.newPassword"
            type="password"
            show-password-on="click"
            placeholder="至少 8 位，建议包含大小写、数字与符号"
          />
        </NFormItem>
        <NFormItem label="确认新密码" path="confirm">
          <NInput
            v-model:value="form.confirm"
            type="password"
            show-password-on="click"
            placeholder="请再次输入新密码"
          />
        </NFormItem>
        <NButton
          type="primary"
          block
          :loading="loading"
          style="margin-top: 8px"
          @click="handleSubmit"
        >
          确认修改
        </NButton>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.pwd-expired {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
}
.pwd-expired__card {
  width: 100%;
  max-width: 480px;
}
.pwd-expired__tip {
  margin: 0 0 16px;
  color: #8c95a6;
  font-size: 13px;
}
</style>
