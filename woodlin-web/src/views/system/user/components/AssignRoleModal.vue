<!--
  @file AssignRoleModal.vue
  @description 用户分配角色模态框
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { ref, reactive, type Ref } from 'vue'
import {
  NModal,
  NCard,
  NCheckboxGroup,
  NCheckbox,
  NSpace,
  NButton,
  NSpin,
  useMessage,
} from 'naive-ui'
import { pageRoles, type SysRole } from '@/api/system/role'
import { assignUserRoles, type SysUser } from '@/api/system/user'

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)

const currentUser: Ref<SysUser | null> = ref(null)
const allRoles: Ref<SysRole[]> = ref([])
const selectedRoleIds = ref<number[]>([])

/**
 * 打开模态框
 * @param user - 用户对象
 */
function open(user: SysUser): void {
  visible.value = true
  currentUser.value = user
  selectedRoleIds.value = user.roleIds || []
  loadRoles()
}

/** 加载角色列表 */
async function loadRoles(): Promise<void> {
  loading.value = true
  try {
    const res = await pageRoles({ page: 1, size: 100 })
    allRoles.value = res.records
  } catch (error: any) {
    message.error(error?.message || '加载角色失败')
  } finally {
    loading.value = false
  }
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  if (!currentUser.value) return
  submitting.value = true
  try {
    await assignUserRoles(currentUser.value.id!, selectedRoleIds.value)
    message.success('分配角色成功')
    visible.value = false
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '分配角色失败')
  } finally {
    submitting.value = false
  }
}

/** 取消 */
function handleCancel(): void {
  visible.value = false
}

defineExpose({ open })
</script>

<template>
  <NModal
    v-model:show="visible"
    preset="card"
    title="分配角色"
    :style="{ width: '480px' }"
    :bordered="false"
    :segmented="{ content: true }"
  >
    <NSpin :show="loading">
      <div style="min-height: 200px">
        <NCheckboxGroup v-model:value="selectedRoleIds">
          <NSpace vertical>
            <NCheckbox
              v-for="role in allRoles"
              :key="role.id"
              :value="role.id"
              :label="role.roleName"
            >
              {{ role.roleName }}
              <span v-if="role.roleCode" style="color: #999; margin-left: 8px">
                ({{ role.roleCode }})
              </span>
            </NCheckbox>
          </NSpace>
        </NCheckboxGroup>
      </div>
    </NSpin>

    <template #footer>
      <NSpace justify="end">
        <NButton @click="handleCancel">取消</NButton>
        <NButton type="primary" :loading="submitting" @click="handleSubmit">确定</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
