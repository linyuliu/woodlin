<!--
  @file views/user/profile/index.vue
  @description 个人中心：基本信息 / 修改密码 / 安全设置 三 Tab
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  type FormInst,
  type FormRules,
  NAvatar,
  NButton,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NForm,
  NFormItem,
  NInput,
  NRadio,
  NRadioGroup,
  NSpace,
  NTabPane,
  NTabs,
  NUpload,
  type UploadFileInfo,
  useMessage,
} from 'naive-ui'
import { changePassword } from '@/api/auth'
import {
  getProfile,
  updateProfile,
  type UserProfile,
} from '@/api/system/user'
import { useUserStore } from '@/stores/modules/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const activeTab = ref<'info' | 'password' | 'security'>('info')

const profile = reactive<UserProfile>({
  nickname: '',
  email: '',
  mobile: '',
  gender: 0,
  avatar: '',
  remark: '',
  lastLoginTime: '',
  lastLoginIp: '',
})

const infoFormRef = ref<FormInst | null>(null)
const infoRules: FormRules = {
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' },
  email: {
    trigger: 'blur',
    validator: (_r, v: string) =>
      !v || /^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(v) || new Error('邮箱格式不正确'),
  },
  mobile: {
    trigger: 'blur',
    validator: (_r, v: string) =>
      !v || /^1[3-9]\d{9}$/.test(v) || new Error('手机号格式不正确'),
  },
}
const infoLoading = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdFormRef = ref<FormInst | null>(null)
const pwdRules: FormRules = {
  oldPassword: { required: true, message: '请输入原密码', trigger: 'blur' },
  newPassword: {
    required: true,
    trigger: 'blur',
    validator: (_r, v: string) =>
      (!!v && v.length >= 8) || new Error('新密码至少 8 位'),
  },
  confirmPassword: {
    required: true,
    trigger: 'blur',
    validator: (_r, v: string) =>
      v === pwdForm.newPassword || new Error('两次输入的密码不一致'),
  },
}
const pwdLoading = ref(false)

async function loadProfile(): Promise<void> {
  try {
    const data = await getProfile()
    Object.assign(profile, data)
  } catch {
    /* 已由全局拦截器提示 */
  }
}

async function handleUpdateInfo(): Promise<void> {
  try {
    await infoFormRef.value?.validate()
  } catch {
    return
  }
  infoLoading.value = true
  try {
    await updateProfile({
      nickname: profile.nickname,
      email: profile.email,
      mobile: profile.mobile,
      gender: profile.gender,
      remark: profile.remark,
      avatar: profile.avatar,
    })
    message.success('保存成功')
    if (userStore.userInfo) {
      userStore.userInfo.nickname = profile.nickname ?? userStore.userInfo.nickname
      userStore.userInfo.avatar = profile.avatar
    }
  } finally {
    infoLoading.value = false
  }
}

async function handleUpdatePassword(): Promise<void> {
  try {
    await pwdFormRef.value?.validate()
  } catch {
    return
  }
  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
      confirmPassword: pwdForm.confirmPassword,
    })
    message.success('密码修改成功，请重新登录')
    userStore.reset()
    await router.replace('/login')
  } finally {
    pwdLoading.value = false
  }
}

function handleAvatarChange(options: { fileList: UploadFileInfo[] }): void {
  const file = options.fileList[0]
  const url = file?.url || (file?.file ? URL.createObjectURL(file.file) : '')
  if (url) {profile.avatar = url}
}

onMounted(loadProfile)
</script>

<template>
  <div class="profile">
    <NCard>
      <NTabs v-model:value="activeTab" type="line" animated>
        <NTabPane name="info" tab="基本信息">
          <NSpace align="start" :size="24" :wrap="false">
            <div class="profile__avatar">
              <NAvatar :size="96" :src="profile.avatar" round />
              <NUpload
                :show-file-list="false"
                :default-upload="false"
                accept="image/*"
                @change="handleAvatarChange"
              >
                <NButton size="small" style="margin-top: 12px">更换头像</NButton>
              </NUpload>
            </div>
            <NForm
              ref="infoFormRef"
              :model="profile"
              :rules="infoRules"
              label-placement="left"
              label-width="80"
              style="flex: 1"
            >
              <NFormItem label="昵称" path="nickname">
                <NInput v-model:value="profile.nickname" placeholder="请输入昵称" />
              </NFormItem>
              <NFormItem label="邮箱" path="email">
                <NInput v-model:value="profile.email" placeholder="请输入邮箱" />
              </NFormItem>
              <NFormItem label="手机号" path="mobile">
                <NInput v-model:value="profile.mobile" placeholder="请输入手机号" />
              </NFormItem>
              <NFormItem label="性别" path="gender">
                <NRadioGroup v-model:value="profile.gender">
                  <NRadio :value="1">男</NRadio>
                  <NRadio :value="2">女</NRadio>
                  <NRadio :value="0">未知</NRadio>
                </NRadioGroup>
              </NFormItem>
              <NFormItem label="备注" path="remark">
                <NInput
                  v-model:value="profile.remark"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </NFormItem>
              <NButton type="primary" :loading="infoLoading" @click="handleUpdateInfo">
                保存
              </NButton>
            </NForm>
          </NSpace>
        </NTabPane>

        <NTabPane name="password" tab="修改密码">
          <NForm
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-placement="left"
            label-width="100"
            style="max-width: 480px"
          >
            <NFormItem label="原密码" path="oldPassword">
              <NInput
                v-model:value="pwdForm.oldPassword"
                type="password"
                show-password-on="click"
                placeholder="请输入原密码"
              />
            </NFormItem>
            <NFormItem label="新密码" path="newPassword">
              <NInput
                v-model:value="pwdForm.newPassword"
                type="password"
                show-password-on="click"
                placeholder="至少 8 位"
              />
            </NFormItem>
            <NFormItem label="确认新密码" path="confirmPassword">
              <NInput
                v-model:value="pwdForm.confirmPassword"
                type="password"
                show-password-on="click"
                placeholder="请再次输入新密码"
              />
            </NFormItem>
            <NButton type="primary" :loading="pwdLoading" @click="handleUpdatePassword">
              确认修改
            </NButton>
          </NForm>
        </NTabPane>

        <NTabPane name="security" tab="安全设置">
          <NDescriptions label-placement="left" bordered :column="1">
            <NDescriptionsItem label="账号">
              {{ userStore.userInfo?.username ?? '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="昵称">
              {{ userStore.userInfo?.nickname ?? '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="所属部门">
              {{ userStore.userInfo?.deptName ?? '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="最近登录时间">
              {{ profile.lastLoginTime || '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="最近登录 IP">
              {{ profile.lastLoginIp || '-' }}
            </NDescriptionsItem>
          </NDescriptions>
        </NTabPane>
      </NTabs>
    </NCard>
  </div>
</template>

<style scoped>
.profile {
  padding: 16px;
}
.profile__avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 140px;
}
</style>
