<!--
  @file MenuPermissionDrawer.vue
  @description 角色菜单权限分配抽屉，支持搜索、全选、展开/收起
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, ref, type Ref } from 'vue'
import {
  NButton,
  NDrawer,
  NDrawerContent,
  NInput,
  NSpace,
  NSpin,
  NTag,
  NTree,
  useMessage,
  type TreeOption,
} from 'naive-ui'
import { getRoleMenus, assignRoleMenus, type SysRole } from '@/api/system/role'
import { getMenuTree, type SysMenuNode } from '@/api/system/menu'

const message = useMessage()
const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const expandedKeys = ref<Array<string | number>>([])

const currentRole: Ref<SysRole | null> = ref(null)
const menuTreeData: Ref<SysMenuNode[]> = ref([])
const checkedKeys = ref<Array<string | number>>([])

const filteredTreeData = computed(() => filterTree(menuTreeData.value, keyword.value.trim().toLowerCase()))
const allMenuIds = computed(() => collectAllKeys(menuTreeData.value))

function open(role: SysRole): void {
  visible.value = true
  currentRole.value = role
  checkedKeys.value = []
  keyword.value = ''
  void loadData()
}

async function loadData(): Promise<void> {
  if (!currentRole.value) {
    return
  }
  loading.value = true
  try {
    const [menuTree, assignedIds] = await Promise.all([
      getMenuTree(),
      getRoleMenus(currentRole.value.id!),
    ])
    menuTreeData.value = menuTree
    checkedKeys.value = assignedIds
    expandedKeys.value = collectAllKeys(menuTree)
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '加载数据失败'
    message.error(messageText)
  } finally {
    loading.value = false
  }
}

function filterTree(list: SysMenuNode[], search: string): SysMenuNode[] {
  if (!search) {
    return list
  }
  return list.reduce<SysMenuNode[]>((result, item) => {
    const children = filterTree(item.children ?? [], search)
    const matched = [item.title, item.permission, item.path, item.component]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(search))
    if (matched || children.length > 0) {
      result.push({ ...item, children })
    }
    return result
  }, [])
}

function collectAllKeys(list: SysMenuNode[]): number[] {
  const keys: number[] = []
  for (const item of list) {
    keys.push(item.id)
    if (item.children?.length) {
      keys.push(...collectAllKeys(item.children))
    }
  }
  return keys
}

function transformMenuTree(list: SysMenuNode[]): TreeOption[] {
  return list.map((item) => ({
    key: item.id,
    label: `${item.title}${item.permission ? ` (${item.permission})` : ''}`,
    children: item.children?.length ? transformMenuTree(item.children) : undefined,
  }))
}

function handleCheckUpdate(keys: Array<string | number>): void {
  checkedKeys.value = keys
}

function handleExpandUpdate(keys: Array<string | number>): void {
  expandedKeys.value = keys
}

function handleExpandAll(): void {
  expandedKeys.value = collectAllKeys(menuTreeData.value)
}

function handleCollapseAll(): void {
  expandedKeys.value = []
}

function handleCheckAll(): void {
  checkedKeys.value = allMenuIds.value
}

function handleClearAll(): void {
  checkedKeys.value = []
}

async function handleSubmit(): Promise<void> {
  if (!currentRole.value) {
    return
  }
  submitting.value = true
  try {
    await assignRoleMenus(currentRole.value.id!, checkedKeys.value.map((item) => Number(item)))
    message.success('菜单权限分配成功')
    visible.value = false
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '操作失败'
    message.error(messageText)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<template>
  <NDrawer v-model:show="visible" :width="680" placement="right">
    <NDrawerContent :title="`菜单权限分配 - ${currentRole?.roleName ?? ''}`" closable>
      <NSpace vertical :size="16">
        <NSpace justify="space-between" align="center">
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="按菜单名称 / 权限标识 / 路径搜索"
            style="width: 320px"
          />
          <NSpace>
            <NTag size="small" type="info">已选 {{ checkedKeys.length }} 项</NTag>
            <NButton quaternary @click="handleExpandAll">展开</NButton>
            <NButton quaternary @click="handleCollapseAll">收起</NButton>
            <NButton quaternary @click="handleCheckAll">全选</NButton>
            <NButton quaternary @click="handleClearAll">清空</NButton>
          </NSpace>
        </NSpace>

        <NSpin :show="loading">
          <div style="min-height: 420px">
            <NTree
              :data="transformMenuTree(filteredTreeData)"
              :checked-keys="checkedKeys"
              :expanded-keys="expandedKeys"
              checkable
              cascade
              block-line
              check-on-click
              @update:checked-keys="handleCheckUpdate"
              @update:expanded-keys="handleExpandUpdate"
            />
          </div>
        </NSpin>
      </NSpace>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="visible = false">取消</NButton>
          <NButton type="primary" :loading="submitting" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
