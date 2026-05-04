<!--
  @file RoleFormDrawer.vue
  @description 角色新增/编辑抽屉表单
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
  NInputNumber,
  NSelect,
  NTreeSelect,
  NSwitch,
  NButton,
  NSpace,
  useMessage,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeSelectOption,
} from 'naive-ui'
import { createRole, updateRole, getRoleTree, type SysRole, type RoleTreeNode } from '@/api/system/role'

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive<SysRole & { parentRoleId?: number }>({
  id: undefined,
  roleName: '',
  roleCode: '',
  status: '1',
  remark: '',
  sort: 0,
  parentRoleId: undefined,
  dataScope: '2',
})

const dataScopeOptions: SelectOption[] = [
  { label: '全部数据', value: '1' },
  { label: '本部门数据', value: '2' },
  { label: '本部门及以下数据', value: '3' },
  { label: '仅本人数据', value: '4' },
  { label: '自定义数据', value: '5' },
]

const roleTreeOptions: Ref<TreeSelectOption[]> = ref([])

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      pattern: /^[A-Z_]+$/,
      message: '角色编码只能包含大写字母和下划线',
      trigger: 'blur',
    },
  ],
}

/**
 * 打开抽屉
 */
function open(record?: SysRole): void {
  visible.value = true
  isEdit.value = !!record
  if (record) {
    Object.assign(formData, record)
  } else {
    resetForm()
  }
  loadRoleTree()
}

/** 重置表单 */
function resetForm(): void {
  Object.assign(formData, {
    id: undefined,
    roleName: '',
    roleCode: '',
    status: '1',
    remark: '',
    sort: 0,
    parentRoleId: undefined,
    dataScope: '2',
  })
  formRef.value?.restoreValidation()
}

/** 加载角色树 */
async function loadRoleTree(): Promise<void> {
  try {
    const data = await getRoleTree()
    roleTreeOptions.value = transformRoleTree(data)
  } catch (error: any) {
    message.error(error?.message || '加载角色树失败')
  }
}

/** 转换角色树 */
function transformRoleTree(list: RoleTreeNode[]): TreeSelectOption[] {
  return list.map((item) => ({
    key: item.id,
    label: item.label,
    value: item.id,
    children: item.children ? transformRoleTree(item.children) : undefined,
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
      await updateRole(formData.id!, formData)
      message.success('修改成功')
    } else {
      await createRole(formData)
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
  <NDrawer v-model:show="visible" :width="560" placement="right">
    <NDrawerContent :title="isEdit ? '编辑角色' : '新增角色'" closable>
      <NForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="100"
        require-mark-placement="left"
      >
        <NFormItem label="角色名称" path="roleName">
          <NInput v-model:value="formData.roleName" placeholder="请输入角色名称" clearable />
        </NFormItem>

        <NFormItem label="角色编码" path="roleCode">
          <NInput v-model:value="formData.roleCode" placeholder="如: ADMIN" clearable />
        </NFormItem>

        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" style="width: 100%" />
        </NFormItem>

        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="formData.status" checked-value="1" unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </NSwitch>
        </NFormItem>

        <NFormItem label="数据权限" path="dataScope">
          <NSelect v-model:value="formData.dataScope" :options="dataScopeOptions" placeholder="请选择数据权限范围" />
        </NFormItem>

        <NFormItem label="上级角色" path="parentRoleId">
          <NTreeSelect
            v-model:value="formData.parentRoleId"
            :options="roleTreeOptions"
            placeholder="选择上级角色（可选）"
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
