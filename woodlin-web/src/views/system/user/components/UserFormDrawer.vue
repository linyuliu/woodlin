<!--
  @file UserFormDrawer.vue  
  @description 用户新增/编辑抽屉表单
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { reactive, ref, type Ref } from 'vue'
import {
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NTreeSelect,
  NRadioGroup,
  NRadioButton,
  NSwitch,
  NButton,
  NSpace,
  useMessage,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeSelectOption,
} from 'naive-ui'
import { createUser, updateUser, type SysUser } from '@/api/system/user'
import { pageRoles } from '@/api/system/role'
import { getDeptTree, type SysDept } from '@/api/system/dept'

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive<SysUser>({
  id: undefined,
  username: '',
  nickname: '',
  mobile: '',
  email: '',
  deptId: undefined,
  gender: '0',
  status: '1',
  password: '',
  roleIds: [],
  remark: '',
})

const roleOptions: Ref<SelectOption[]> = ref([])
const deptOptions: Ref<TreeSelectOption[]> = ref([])

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
  ],
  mobile: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur',
    },
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱',
      trigger: 'blur',
    },
  ],
}

/**
 * 打开抽屉
 * @param record - 编辑时传入用户对象
 */
function open(record?: SysUser): void {
  visible.value = true
  isEdit.value = !!record
  if (record) {
    Object.assign(formData, {
      ...record,
      password: '',
    })
  } else {
    resetForm()
  }
  loadOptions()
}

/** 重置表单 */
function resetForm(): void {
  Object.assign(formData, {
    id: undefined,
    username: '',
    nickname: '',
    mobile: '',
    email: '',
    deptId: undefined,
    gender: '0',
    status: '1',
    password: '',
    roleIds: [],
    remark: '',
  })
  formRef.value?.restoreValidation()
}

/** 加载下拉选项 */
async function loadOptions(): Promise<void> {
  try {
    const [roles, depts] = await Promise.all([
      pageRoles({ page: 1, size: 100 }),
      getDeptTree(),
    ])
    roleOptions.value = roles.records.map((r) => ({
      label: r.roleName,
      value: r.id!,
    }))
    deptOptions.value = transformDeptTree(depts)
  } catch (error: any) {
    message.error(error?.message || '加载选项失败')
  }
}

/** 转换部门树 */
function transformDeptTree(list: SysDept[]): TreeSelectOption[] {
  return list.map((item) => ({
    key: item.deptId || item.id,
    label: item.deptName,
    value: item.deptId || item.id,
    children: item.children ? transformDeptTree(item.children) : undefined,
  }))
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await updateUser(formData.id!, formData)
      message.success('修改成功')
    } else {
      await createUser(formData)
      message.success('新增成功')
    }
    visible.value = false
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

/** 取消 */
function handleCancel(): void {
  visible.value = false
}

defineExpose({ open })
</script>

<template>
  <NDrawer
    v-model:show="visible"
    :width="560"
    placement="right"
  >
    <NDrawerContent
      :title="isEdit ? '编辑用户' : '新增用户'"
      closable
    >
      <NForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="80"
        require-mark-placement="left"
      >
        <NFormItem label="用户名" path="username">
          <NInput
            v-model:value="formData.username"
            :disabled="isEdit"
            placeholder="请输入用户名"
            clearable
          />
        </NFormItem>

        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="formData.nickname" placeholder="请输入昵称" clearable />
        </NFormItem>

        <NFormItem v-if="!isEdit" label="密码" path="password">
          <NInput
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            clearable
          />
        </NFormItem>

        <NFormItem label="部门" path="deptId">
          <NTreeSelect
            v-model:value="formData.deptId"
            :options="deptOptions"
            placeholder="请选择部门"
            clearable
            filterable
          />
        </NFormItem>

        <NFormItem label="性别" path="gender">
          <NRadioGroup v-model:value="formData.gender">
            <NRadioButton value="0">男</NRadioButton>
            <NRadioButton value="1">女</NRadioButton>
            <NRadioButton value="2">未知</NRadioButton>
          </NRadioGroup>
        </NFormItem>

        <NFormItem label="手机号" path="mobile">
          <NInput v-model:value="formData.mobile" placeholder="请输入手机号" clearable />
        </NFormItem>

        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="formData.email" placeholder="请输入邮箱" clearable />
        </NFormItem>

        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="formData.status" checked-value="1" unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </NSwitch>
        </NFormItem>

        <NFormItem label="角色" path="roleIds">
          <NSelect
            v-model:value="formData.roleIds"
            :options="roleOptions"
            multiple
            placeholder="请选择角色"
            clearable
            filterable
          />
        </NFormItem>

        <NFormItem label="备注" path="remark">
          <NInput
            v-model:value="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            clearable
          />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleCancel">取消</NButton>
          <NButton type="primary" :loading="loading" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
