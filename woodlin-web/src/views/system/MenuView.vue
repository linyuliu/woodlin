<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
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
  NTag,
  NTree,
  useMessage,
  type FormInst,
  type SelectOption,
  type TreeOption
} from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  ListOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  addMenu,
  deleteMenu,
  getMenuById,
  getMenuTree,
  updateMenu,
  type SysMenu
} from '@/api/menu'
import { useUserStore } from '@/stores'
import {PERMISSIONS} from '@/constants/permission-keys'

interface MenuSearchForm {
  menuName: string
  permissionCode: string
}

interface MenuFormData {
  menuId?: number
  parentId: number | null
  menuName: string
  permissionCode: string
  permissionType: 'M' | 'C' | 'F'
  path: string
  component: string
  icon: string
  sortOrder: number
  status: string
  visible: string
  isFrame: string
  isCache: string
  remark: string
}

const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)

const searchForm = ref<MenuSearchForm>({
  menuName: '',
  permissionCode: ''
})

const menuTree = ref<SysMenu[]>([])
const selectedKeys = ref<Array<string | number>>([])

const modalVisible = ref(false)
const formRef = ref<FormInst | null>(null)
const form = ref<MenuFormData>({
  parentId: null,
  menuName: '',
  permissionCode: '',
  permissionType: 'C',
  path: '',
  component: '',
  icon: 'list-outline',
  sortOrder: 0,
  status: '1',
  visible: '1',
  isFrame: '0',
  isCache: '0',
  remark: ''
})

const formRules = {
  menuName: { required: true, message: '请输入名称', trigger: 'blur' },
  permissionType: { required: true, message: '请选择类型', trigger: 'change' }
}

const menuTypeOptions = [
  { label: '目录', value: 'M' },
  { label: '菜单', value: 'C' },
  { label: '按钮', value: 'F' }
]

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const visibleOptions = [
  { label: '显示', value: '1' },
  { label: '隐藏', value: '0' }
]

const yesNoOptions = [
  { label: '是', value: '1' },
  { label: '否', value: '0' }
]

const selectedMenuId = computed(() => {
  const key = selectedKeys.value[0]
  if (key === undefined || key === null) {
    return null
  }
  return Number(key)
})

const treeData = computed<TreeOption[]>(() => buildTreeOptions(menuTree.value))
const parentOptions = computed<SelectOption[]>(() => buildParentOptions(menuTree.value))

const totalMenus = computed(() => countNodes(menuTree.value))
const canViewMenu = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_MENU_LIST))
const canCreateMenu = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_MENU_ADD))
const canUpdateMenu = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_MENU_EDIT))
const canDeleteMenu = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_MENU_REMOVE))

const selectedMenuInfo = computed(() => {
  if (!selectedMenuId.value) {
    return null
  }
  return findNodeById(menuTree.value, selectedMenuId.value)
})

const toText = (value: string | undefined | null): string => value ?? ''

const loadMenuTree = async () => {
  if (!canViewMenu.value) {
    menuTree.value = []
    selectedKeys.value = []
    return
  }
  loading.value = true
  try {
    const params: Record<string, string> = {}
    if (searchForm.value.menuName.trim()) {
      params.menuName = searchForm.value.menuName.trim()
    }
    if (searchForm.value.permissionCode.trim()) {
      params.permissionCode = searchForm.value.permissionCode.trim()
    }
    menuTree.value = await getMenuTree(params)
    if (selectedMenuId.value && !findNodeById(menuTree.value, selectedMenuId.value)) {
      selectedKeys.value = []
    }
  } catch (error) {
    console.error(error)
    message.error('加载菜单树失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadMenuTree()
}

const handleReset = () => {
  searchForm.value = {
    menuName: '',
    permissionCode: ''
  }
  loadMenuTree()
}

const openAdd = () => {
  if (!canCreateMenu.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_ADD} 权限`)
    return
  }
  form.value = {
    parentId: selectedMenuId.value,
    menuName: '',
    permissionCode: '',
    permissionType: 'C',
    path: '',
    component: '',
    icon: 'list-outline',
    sortOrder: 0,
    status: '1',
    visible: '1',
    isFrame: '0',
    isCache: '0',
    remark: ''
  }
  modalVisible.value = true
}

// eslint-disable-next-line complexity
const openEdit = async () => {
  if (!canUpdateMenu.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_EDIT} 权限`)
    return
  }
  if (!selectedMenuId.value) {
    message.warning('请先选择菜单')
    return
  }

  loading.value = true
  try {
    const detail = await getMenuById(selectedMenuId.value)
    form.value = {
      menuId: detail.menuId,
      parentId: detail.parentId ?? null,
      menuName: detail.menuName,
      permissionCode: toText(detail.permissionCode),
      permissionType: detail.permissionType,
      path: toText(detail.path),
      component: toText(detail.component),
      icon: detail.icon ?? 'list-outline',
      sortOrder: detail.sortOrder ?? 0,
      status: detail.status ?? '1',
      visible: detail.visible ?? '1',
      isFrame: detail.isFrame ?? '0',
      isCache: detail.isCache ?? '0',
      remark: toText(detail.remark)
    }
    modalVisible.value = true
  } catch (error) {
    console.error(error)
    message.error('加载菜单详情失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  if (!canDeleteMenu.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_REMOVE} 权限`)
    return
  }
  if (!selectedMenuId.value) {
    message.warning('请先选择菜单')
    return
  }

  loading.value = true
  try {
    await deleteMenu(selectedMenuId.value)
    message.success('删除成功')
    selectedKeys.value = []
    await loadMenuTree()
  } catch (error) {
    console.error(error)
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

// eslint-disable-next-line max-lines-per-function
const submit = async () => {
  await formRef.value?.validate()

  const payload: SysMenu = {
    menuId: form.value.menuId,
    parentId: form.value.parentId,
    menuName: form.value.menuName.trim(),
    permissionCode: form.value.permissionCode.trim(),
    permissionType: form.value.permissionType,
    path: form.value.path.trim(),
    component: form.value.component.trim(),
    icon: form.value.icon.trim(),
    sortOrder: form.value.sortOrder,
    status: form.value.status,
    visible: form.value.visible,
    isFrame: form.value.isFrame,
    isCache: form.value.isCache,
    remark: form.value.remark.trim()
  }

  if (payload.permissionType === 'F') {
    payload.path = ''
    payload.component = ''
  }

  loading.value = true
  try {
    if (payload.menuId) {
      if (!canUpdateMenu.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_EDIT} 权限`)
        return
      }
      await updateMenu(payload)
      message.success('修改成功')
    } else {
      if (!canCreateMenu.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_ADD} 权限`)
        return
      }
      await addMenu(payload)
      message.success('新增成功')
    }
    modalVisible.value = false
    await loadMenuTree()
  } catch (error) {
    console.error(error)
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

const buildTreeOptions = (nodes: SysMenu[]): TreeOption[] =>
  nodes.map(node => ({
    key: node.menuId || 0,
    label: `${node.menuName} (${getTypeLabel(node.permissionType)})`,
    prefix: () => h(NIcon, null, { default: () => h(ListOutline) }),
    children: node.children ? buildTreeOptions(node.children) : undefined
  }))

const buildParentOptions = (nodes: SysMenu[], list: SelectOption[] = []): SelectOption[] => {
  for (const node of nodes) {
    if (node.menuId) {
      list.push({
        label: `${node.menuName} (${getTypeLabel(node.permissionType)})`,
        value: node.menuId
      })
    }
    if (node.children && node.children.length > 0) {
      buildParentOptions(node.children, list)
    }
  }
  return list
}

const countNodes = (nodes: SysMenu[]): number =>
  nodes.reduce((total, item) => total + 1 + countNodes(item.children || []), 0)

const findNodeById = (nodes: SysMenu[], id: number): SysMenu | null => {
  for (const node of nodes) {
    if (node.menuId === id) {
      return node
    }
    const child = findNodeById(node.children || [], id)
    if (child) {
      return child
    }
  }
  return null
}

const getTypeLabel = (type?: string): string => {
  if (type === 'M') {
    return '目录'
  }
  if (type === 'C') {
    return '菜单'
  }
  if (type === 'F') {
    return '按钮'
  }
  return '未知'
}

onMounted(() => {
  if (!canViewMenu.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_MENU_LIST} 权限`)
    return
  }
  loadMenuTree()
})
</script>

<template>
  <div class="menu-page">
    <NCard :bordered="false" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>菜单与按钮权限</h2>
          <p>维护系统导航、页面入口和按钮权限编码，角色授权会基于这里的节点执行。</p>
        </div>
        <NTag type="info" size="small">共 {{ totalMenus }} 个节点</NTag>
      </div>
    </NCard>

    <NCard :bordered="false" class="toolbar-card">
      <NForm inline :model="searchForm" label-placement="left">
        <NFormItem label="名称">
          <NInput v-model:value="searchForm.menuName" clearable placeholder="菜单名称" style="width: 180px" />
        </NFormItem>
        <NFormItem label="权限码">
          <NInput v-model:value="searchForm.permissionCode" clearable placeholder="system:menu:list" style="width: 200px" />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" :loading="loading" :disabled="!canViewMenu" @click="handleSearch">
              <template #icon>
                <NIcon><SearchOutline /></NIcon>
              </template>
              搜索
            </NButton>
            <NButton :disabled="!canViewMenu" @click="handleReset">
              <template #icon>
                <NIcon><RefreshOutline /></NIcon>
              </template>
              重置
            </NButton>
            <NButton v-if="canCreateMenu" type="primary" secondary @click="openAdd">
              <template #icon>
                <NIcon><AddOutline /></NIcon>
              </template>
              新增
            </NButton>
            <NButton v-if="canUpdateMenu" :disabled="!selectedMenuId" @click="openEdit">
              <template #icon>
                <NIcon><CreateOutline /></NIcon>
              </template>
              编辑
            </NButton>
            <NPopconfirm v-if="canDeleteMenu" @positive-click="handleDelete">
              <template #trigger>
                <NButton type="error" :disabled="!selectedMenuId">
                  <template #icon>
                    <NIcon><TrashOutline /></NIcon>
                  </template>
                  删除
                </NButton>
              </template>
              确认删除当前节点？
            </NPopconfirm>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" title="菜单树" class="tree-card">
      <template #header-extra>
        <NTag v-if="selectedMenuInfo" size="small" type="success">
          当前：{{ selectedMenuInfo.menuName }}
        </NTag>
      </template>
      <NTree
        :data="treeData"
        block-line
        expand-on-click
        selectable
        :selected-keys="selectedKeys"
        @update:selected-keys="(keys) => selectedKeys = keys"
      />
    </NCard>

    <NModal
      v-model:show="modalVisible"
      preset="card"
      :title="form.menuId ? '编辑菜单' : '新增菜单'"
      style="width: 620px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="formRef" :model="form" :rules="formRules" label-placement="left" label-width="100">
        <NFormItem label="名称" path="menuName">
          <NInput v-model:value="form.menuName" placeholder="请输入菜单名称" />
        </NFormItem>
        <NFormItem label="类型" path="permissionType">
          <NSelect v-model:value="form.permissionType" :options="menuTypeOptions" />
        </NFormItem>
        <NFormItem label="上级菜单">
          <NSelect
            v-model:value="form.parentId"
            :options="parentOptions"
            clearable
            placeholder="根节点可留空"
          />
        </NFormItem>
        <NFormItem label="权限编码">
          <NInput v-model:value="form.permissionCode" placeholder="例如 system:user:list" />
        </NFormItem>
        <NFormItem v-if="form.permissionType !== 'F'" label="路由地址">
          <NInput v-model:value="form.path" placeholder="例如 /system/user" />
        </NFormItem>
        <NFormItem v-if="form.permissionType === 'C'" label="组件路径">
          <NInput v-model:value="form.component" placeholder="例如 system/UserView" />
        </NFormItem>
        <NFormItem label="图标">
          <NInput v-model:value="form.icon" placeholder="Ionicons 名称，如 people-outline" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="form.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="显示">
          <NSelect v-model:value="form.visible" :options="visibleOptions" />
        </NFormItem>
        <NFormItem label="是否外链">
          <NSelect v-model:value="form.isFrame" :options="yesNoOptions" />
        </NFormItem>
        <NFormItem label="是否缓存">
          <NSelect v-model:value="form.isCache" :options="yesNoOptions" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="form.remark" type="textarea" placeholder="可选" />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton
            type="primary"
            :loading="loading"
            :disabled="(form.menuId && !canUpdateMenu) || (!form.menuId && !canCreateMenu)"
            @click="submit"
          >
            保存
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.menu-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #215f00 0%, #508d4e 48%, #80af81 100%);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-color-inverse);
}

.hero-content h2 {
  margin: 0 0 6px;
  color: var(--text-color-inverse);
}

.hero-content p {
  margin: 0;
  color: color-mix(in srgb, var(--text-color-inverse) 84%, transparent);
}

.toolbar-card,
.tree-card {
  background: var(--bg-color);
}

:deep(.n-tree-node-content__text) {
  font-weight: 500;
}
</style>
