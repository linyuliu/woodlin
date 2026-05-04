<!--
  @file MenuPermissionDrawer.vue
  @description 角色菜单权限分配抽屉
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { ref, type Ref } from 'vue'
import {
  NDrawer,
  NDrawerContent,
  NTree,
  NButton,
  NSpace,
  NSpin,
  useMessage,
  type TreeOption,
} from 'naive-ui'
import { getRoleMenus, assignRoleMenus, type SysRole } from '@/api/system/role'
import { getMenuTree } from '@/api/system/menu'
import type { RouteItem } from '@/types/global'

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)

const currentRole: Ref<SysRole | null> = ref(null)
const menuTreeData: Ref<TreeOption[]> = ref([])
const checkedKeys = ref<Array<string | number>>([])

/**
 * 打开抽屉
 */
function open(role: SysRole): void {
  visible.value = true
  currentRole.value = role
  checkedKeys.value = []
  loadData()
}

/** 加载数据 */
async function loadData(): Promise<void> {
  if (!currentRole.value) return
  loading.value = true
  try {
    const [menuTree, assignedIds] = await Promise.all([
      getMenuTree(),
      getRoleMenus(currentRole.value.id!),
    ])
    menuTreeData.value = transformMenuTree(menuTree)
    checkedKeys.value = assignedIds
  } catch (error: any) {
    message.error(error?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

/** 转换菜单树 */
function transformMenuTree(list: RouteItem[]): TreeOption[] {
  return list.map((item) => ({
    key: item.id,
    label: item.title,
    children: item.children ? transformMenuTree(item.children) : undefined,
  }))
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  if (!currentRole.value) return
  submitting.value = true
  try {
    await assignRoleMenus(currentRole.value.id!, checkedKeys.value as number[])
    message.success('菜单权限分配成功')
    visible.value = false
  } catch (error: any) {
    message.error(error?.message || '操作失败')
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
  <NDrawer v-model:show="visible" :width="520" placement="right">
    <NDrawerContent title="菜单权限分配" closable>
      <NSpin :show="loading">
        <div style="min-height: 300px">
          <NTree
            :data="menuTreeData"
            :checked-keys="checkedKeys"
            checkable
            cascade
            block-line
            @update:checked-keys="(keys) => (checkedKeys = keys)"
          />
        </div>
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
