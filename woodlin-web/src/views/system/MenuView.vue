<script setup lang="ts">
/**
 * 菜单管理（前端 mock，支持目录/菜单/按钮）
 */
import { h, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NTree,
  useMessage,
  type FormInst,
  type TreeOption
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  RefreshOutline,
  TrashOutline,
  ListOutline
} from '@vicons/ionicons5'
import {
  fetchMenuTree,
  createMenu,
  updateMenu,
  deleteMenu,
  type MenuNode
} from '@/api/mock/rbac'

const message = useMessage()
const loading = ref(false)

const tree = ref<TreeOption[]>([])
const selectedKeys = ref<(string | number)[]>([])

const modalShow = ref(false)
const formRef = ref<FormInst | null>(null)
const form = ref<Partial<MenuNode>>({
  menuName: '',
  parentId: null,
  type: 'M',
  orderNum: 1,
  icon: 'list-outline',
  path: '',
  component: '',
  perms: '',
  visible: '1',
  status: '1'
})

const typeOptions = [
  { label: '目录', value: 'C' },
  { label: '菜单', value: 'M' },
  { label: '按钮', value: 'F' }
]

const visOptions = [
  { label: '显示', value: '1' },
  { label: '隐藏', value: '0' }
]

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const iconOptions = [
  'settings-outline',
  'people-outline',
  'shield-outline',
  'business-outline',
  'list-outline',
  'home-outline',
  'apps-outline',
  'cube-outline',
  'server-outline',
  'folder-outline',
  'code-slash-outline',
  'key-outline'
].map(i => ({ label: i, value: i }))

const buildTree = (nodes: MenuNode[]): TreeOption[] =>
  nodes.map(n => ({
    key: n.menuId,
    label: `${n.menuName} (${n.type === 'C' ? '目录' : n.type === 'M' ? '菜单' : '按钮'})`,
    icon: () => h(NIcon, null, { default: () => h(ListOutline) }),
    children: n.children ? buildTree(n.children) : undefined
  }))

const flattenOptions = (nodes: MenuNode[], list: { label: string; value: number }[] = []) => {
  nodes.forEach(n => {
    list.push({ label: n.menuName, value: n.menuId })
    if (n.children) {
      flattenOptions(n.children, list)
    }
  })
  return list
}

const parentOptions = ref<{ label: string; value: number }[]>([])

const load = async () => {
  loading.value = true
  try {
    const data = await fetchMenuTree()
    tree.value = buildTree(data)
    parentOptions.value = flattenOptions(data)
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  form.value = {
    menuId: undefined,
    menuName: '',
    parentId: selectedKeys.value[0] ? Number(selectedKeys.value[0]) : null,
    type: 'M',
    orderNum: 1,
    icon: 'list-outline',
    path: '',
    component: '',
    perms: '',
    visible: '1',
    status: '1'
  }
  modalShow.value = true
}

const openEdit = () => {
  const id = selectedKeys.value[0]
  if (!id) {
    message.warning('请先选择菜单')
    return
  }
  // 简化：重新从树选项找到 label -> menuName 不包含其他字段，直接设置 id 供编辑
  form.value = { menuId: Number(id) }
  modalShow.value = true
}

const submit = async () => {
  await formRef.value?.validate?.()
  loading.value = true
  try {
    if (form.value.menuId) {
      await updateMenu(form.value as MenuNode)
      message.success('更新成功')
    } else {
      await createMenu(form.value)
      message.success('新增成功')
    }
    modalShow.value = false
    await load()
  } finally {
    loading.value = false
  }
}

const handleDelete = () => {
  const id = selectedKeys.value[0]
  if (!id) {
    message.warning('请先选择菜单')
    return
  }
  deleteMenu(Number(id)).then(() => {
    message.success('删除成功')
    selectedKeys.value = []
    load()
  })
}

onMounted(load)
</script>

<template>
  <div class="menu-page">
    <NSpace vertical :size="16">
      <NCard :bordered="false">
        <NSpace wrap>
          <NButton type="primary" @click="openAdd">
            <template #icon>
              <NIcon><AddOutline /></NIcon>
            </template>
            新增
          </NButton>
          <NButton @click="openEdit">
            <template #icon>
              <NIcon><CreateOutline /></NIcon>
            </template>
            编辑
          </NButton>
          <NPopconfirm @positive-click="handleDelete">
            <template #trigger>
              <NButton type="error">
                <template #icon>
                  <NIcon><TrashOutline /></NIcon>
                </template>
                删除
              </NButton>
            </template>
            确定删除该菜单及其子菜单？
          </NPopconfirm>
          <NButton @click="load" :loading="loading">
            <template #icon>
              <NIcon><RefreshOutline /></NIcon>
            </template>
            刷新
          </NButton>
        </NSpace>
      </NCard>

      <NCard title="菜单树" :bordered="false">
        <NTree
          :data="tree"
          :selected-keys="selectedKeys"
          block-line
          expand-on-click
          @update:selected-keys="(keys) => selectedKeys = keys"
        />
      </NCard>
    </NSpace>

    <NModal
      v-model:show="modalShow"
      preset="card"
      :title="form.menuId ? '编辑菜单' : '新增菜单'"
      style="width: 560px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="formRef" :model="form" label-placement="left" label-width="100">
        <NFormItem label="名称" path="menuName">
          <NInput v-model:value="form.menuName" placeholder="请输入名称" />
        </NFormItem>
        <NFormItem label="类型">
          <NSelect v-model:value="form.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="上级">
          <NSelect v-model:value="form.parentId" :options="parentOptions" clearable placeholder="根节点留空" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="form.orderNum" :min="0" />
        </NFormItem>
        <NFormItem label="图标">
          <NSelect v-model:value="form.icon" filterable :options="iconOptions" placeholder="Ionicons 名称" />
        </NFormItem>
        <NFormItem v-if="form.type !== 'C'" label="路由路径">
          <NInput v-model:value="form.path" placeholder="/system/user" />
        </NFormItem>
        <NFormItem v-if="form.type === 'M'" label="前端组件">
          <NInput v-model:value="form.component" placeholder="system/UserView" />
        </NFormItem>
        <NFormItem v-if="form.type !== 'C'" label="权限标识">
          <NInput v-model:value="form.perms" placeholder="system:user:view" />
        </NFormItem>
        <NFormItem label="显示">
          <NSelect v-model:value="form.visible" :options="visOptions" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="form.status" :options="statusOptions" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalShow = false">取消</NButton>
          <NButton type="primary" :loading="loading" @click="submit">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.menu-page {
  padding: 12px 0;
}
</style>
