<!--
  @file DataScopeDrawer.vue
  @description 角色数据权限配置抽屉
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { ref, reactive, computed, type Ref } from 'vue'
import {
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NSelect,
  NTree,
  NButton,
  NSpace,
  NSpin,
  useMessage,
  type SelectOption,
  type TreeOption,
} from 'naive-ui'
import {
  assignRoleDataScope,
  getRoleDataScope,
  type SysRole,
  type DataScopeRequest,
} from '@/api/system/role'
import { getDeptTree, type SysDept } from '@/api/system/dept'

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)

const currentRole: Ref<SysRole | null> = ref(null)
const deptTreeData: Ref<TreeOption[]> = ref([])
const formData = reactive<DataScopeRequest>({
  dataScope: '1',
  deptIds: [],
})

const dataScopeOptions: SelectOption[] = [
  { label: '全部数据', value: '1' },
  { label: '自定义数据', value: '2' },
  { label: '本部门数据', value: '3' },
  { label: '本部门及以下数据', value: '4' },
  { label: '仅本人数据', value: '5' },
]

const showDeptTree = computed(() => formData.dataScope === '2')

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback
}

/**
 * 打开抽屉
 */
function open(role: SysRole): void {
  visible.value = true
  currentRole.value = role
  formData.dataScope = role.dataScope || '1'
  formData.deptIds = []
  void initialize()
}

async function initialize(): Promise<void> {
  if (!currentRole.value?.id) {return}
  await Promise.all([loadDeptTree(), loadCurrentDataScope(currentRole.value.id)])
}

/** 加载部门树 */
async function loadDeptTree(): Promise<void> {
  loading.value = true
  try {
    const data = await getDeptTree()
    deptTreeData.value = transformDeptTree(data)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '加载部门树失败'))
  } finally {
    loading.value = false
  }
}

async function loadCurrentDataScope(roleId: number): Promise<void> {
  try {
    const data = await getRoleDataScope(roleId)
    formData.dataScope = data.dataScope || '1'
    formData.deptIds = data.deptIds ?? []
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '加载角色数据权限失败'))
  }
}

/** 转换部门树 */
function transformDeptTree(list: SysDept[]): TreeOption[] {
  return list.map((item) => ({
    key: item.deptId || item.id,
    label: item.deptName,
    children: item.children ? transformDeptTree(item.children) : undefined,
  }))
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  const roleId = currentRole.value?.id
  if (roleId === undefined) {return}
  submitting.value = true
  try {
    await assignRoleDataScope(roleId, formData)
    message.success('数据权限配置成功')
    visible.value = false
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '操作失败'))
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
  <NDrawer v-model:show="visible" :width="480" placement="right">
    <NDrawerContent title="数据权限配置" closable>
      <NSpin :show="loading">
        <NForm label-placement="top">
          <NFormItem label="数据权限范围">
            <NSelect
              v-model:value="formData.dataScope"
              :options="dataScopeOptions"
              placeholder="请选择数据权限范围"
            />
          </NFormItem>

          <NFormItem v-if="showDeptTree" label="选择部门">
            <NTree
              :data="deptTreeData"
              :checked-keys="formData.deptIds"
              checkable
              cascade
              block-line
              style="border: 1px solid #e0e0e6; border-radius: 4px; padding: 8px"
              @update:checked-keys="(keys) => (formData.deptIds = keys as number[])"
            />
          </NFormItem>
        </NForm>
      </NSpin>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleCancel">取消</NButton>
          <NButton type="primary" :loading="submitting" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
