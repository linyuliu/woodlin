<!--
  @file views/system/menu/index.vue
  @module 菜单管理
  @description 生产级菜单管理：树形表格+新增编辑+图标选择+条件字段+展开收起
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { h, onMounted, ref, computed, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NPopconfirm,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import {
  getMenuTree,
  deleteMenu,
  type SysMenuNode,
} from '@/api/system/menu'
import MenuFormDrawer from './components/MenuFormDrawer.vue'
import WIcon from '@/components/WIcon/index.vue'
import { hasPermission } from '@/utils/permission'

const message = useMessage()

const treeData: Ref<SysMenuNode[]> = ref([])
const loading = ref(false)
const expandAll = ref(true)
const expandedRowKeys = ref<Array<string | number>>([])

const searchKeyword = ref('')
const searchStatus = ref<string>()
const searchVisible = ref<string>()

const menuFormDrawerRef = ref<InstanceType<typeof MenuFormDrawer> | null>(null)

const statusOptions: SelectOption[] = [
  { label: '全部', value: '' },
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

const visibleOptions: SelectOption[] = [
  { label: '全部', value: '' },
  { label: '显示', value: '1' },
  { label: '隐藏', value: '0' },
]

const menuTypeMap: Record<number, { text: string; type: 'info' | 'success' | 'warning' }> = {
  1: { text: '目录', type: 'info' },
  2: { text: '菜单', type: 'success' },
  3: { text: '按钮', type: 'warning' },
}

/** 过滤后的树数据 */
const filteredTreeData = computed(() => {
  let result = treeData.value
  if (searchKeyword.value) {
    result = filterTreeByKeyword(result, searchKeyword.value)
  }
  if (searchStatus.value) {
    result = filterTreeByStatus(result, searchStatus.value)
  }
  if (searchVisible.value) {
    result = filterTreeByVisible(result, searchVisible.value)
  }
  return result
})

/** 按关键词过滤树 */
function filterTreeByKeyword(list: SysMenuNode[], keyword: string): SysMenuNode[] {
  const filtered: SysMenuNode[] = []
  for (const item of list) {
    const match = item.title?.toLowerCase().includes(keyword.toLowerCase()) ||
                  item.permission?.toLowerCase().includes(keyword.toLowerCase()) ||
                  item.path?.toLowerCase().includes(keyword.toLowerCase()) ||
                  item.component?.toLowerCase().includes(keyword.toLowerCase())
    const children = item.children ? filterTreeByKeyword(item.children, keyword) : []
    if (match || children.length > 0) {
      filtered.push({ ...item, children: children.length > 0 ? children : item.children })
    }
  }
  return filtered
}

/** 按状态过滤树 */
function filterTreeByStatus(list: SysMenuNode[], status: string): SysMenuNode[] {
  const filtered: SysMenuNode[] = []
  for (const item of list) {
    const match = item.status === status
    const children = item.children ? filterTreeByStatus(item.children, status) : []
    if (match || children.length > 0) {
      filtered.push({ ...item, children: children.length > 0 ? children : item.children })
    }
  }
  return filtered
}

/** 按显示状态过滤树 */
function filterTreeByVisible(list: SysMenuNode[], visible: string): SysMenuNode[] {
  const filtered: SysMenuNode[] = []
  for (const item of list) {
    const match = item.visible === visible
    const children = item.children ? filterTreeByVisible(item.children, visible) : []
    if (match || children.length > 0) {
      filtered.push({ ...item, children: children.length > 0 ? children : item.children })
    }
  }
  return filtered
}

const columns = computed<DataTableColumns<SysMenuNode>>(() => [
  {
    key: 'title',
    title: '菜单名称',
    width: 240,
    fixed: 'left',
    render: (row: SysMenuNode) =>
      h(
        NSpace,
        { size: 4, align: 'center' },
        {
          default: () => [
            row.icon ? h(WIcon, { icon: row.icon, size: 16 }) : null,
            h('span', {}, row.title),
          ],
        }
      ),
  },
  {
    key: 'type',
    title: '类型',
    width: 80,
    render: (row: SysMenuNode) => {
      const config = menuTypeMap[row.type]
      return h(NTag, { size: 'small', type: config.type }, { default: () => config.text })
    },
  },
  {
    key: 'permission',
    title: '权限标识',
    width: 180,
  },
  {
    key: 'path',
    title: '路由路径',
    width: 180,
  },
  {
    key: 'component',
    title: '组件路径',
    width: 200,
  },
  {
    key: 'sort',
    title: '排序',
    width: 70,
  },
  {
    key: 'isHidden',
    title: '显示',
    width: 80,
    render: (row: SysMenuNode) =>
      h(NTag, { size: 'small', type: row.visible === '1' ? 'success' : 'default' }, { default: () => (row.visible === '1' ? '显示' : '隐藏') }),
  },
  {
    key: 'status',
    title: '状态',
    width: 80,
    render: (row: SysMenuNode) => h(NTag, { size: 'small', type: row.status === '1' ? 'success' : 'default' }, { default: () => (row.status === '1' ? '启用' : '禁用') }),
  },
  {
    key: 'action',
    title: '操作',
    width: 240,
    fixed: 'right',
    render: (row: SysMenuNode) =>
      h(
        NSpace,
        { size: 4 },
        {
          default: () => [
            row.type !== 3 && hasPermission('system:menu:add')
              ? h(NButton, { text: true, type: 'success', size: 'small', onClick: () => handleAddChild(row) }, { default: () => '新增子菜单' })
              : null,
            hasPermission('system:menu:edit')
              ? h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' })
              : null,
            hasPermission('system:menu:remove') ? h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row.id) },
              {
                trigger: () => h(NButton, { text: true, type: 'error', size: 'small' }, { default: () => '删除' }),
                default: () => '确认删除该菜单及其子菜单？',
              }
            ) : null,
          ].filter(Boolean),
        }
      ),
  },
])

/** 搜索 */
function handleSearch(): void {
  // 搜索时自动展开所有
  if (searchKeyword.value || searchStatus.value) {
    expandAll.value = true
    updateExpandedKeys()
  }
}

/** 重置搜索 */
function handleReset(): void {
  searchKeyword.value = ''
  searchStatus.value = undefined
  searchVisible.value = undefined
  expandAll.value = true
  updateExpandedKeys()
}

/** 切换展开/收起 */
function handleToggleExpand(): void {
  expandAll.value = !expandAll.value
  updateExpandedKeys()
}

/** 更新展开的keys */
function updateExpandedKeys(): void {
  if (expandAll.value) {
    expandedRowKeys.value = collectAllKeys(treeData.value)
  } else {
    expandedRowKeys.value = []
  }
}

/** 收集所有节点key */
function collectAllKeys(list: SysMenuNode[]): number[] {
  const keys: number[] = []
  for (const item of list) {
    keys.push(item.id)
    if (item.children) {
      keys.push(...collectAllKeys(item.children))
    }
  }
  return keys
}

/** 加载数据 */
async function loadData(): Promise<void> {
  loading.value = true
  try {
    const data = await getMenuTree()
    treeData.value = data
    updateExpandedKeys()
  } catch (error: any) {
    message.error(error?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

/** 新增 */
function handleAdd(): void {
  menuFormDrawerRef.value?.open()
}

/** 新增子菜单 */
function handleAddChild(parent: SysMenuNode): void {
  menuFormDrawerRef.value?.open(undefined, parent.id)
}

/** 编辑 */
function handleEdit(row: SysMenuNode): void {
  menuFormDrawerRef.value?.open(row)
}

/** 删除 */
async function handleDelete(id: number): Promise<void> {
  try {
    await deleteMenu(id)
    message.success('删除成功')
    loadData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

/** 表单成功回调 */
function handleFormSuccess(): void {
  loadData()
}

function handleExpandedKeysChange(keys: Array<string | number>): void {
  expandedRowKeys.value = keys
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="w-page">
    <WSearchForm @search="handleSearch" @reset="handleReset">
        <NInput
          v-model:value="searchKeyword"
          placeholder="菜单名称"
          clearable
          class="w-page-search__item--md"
        />
        <NSelect
          v-model:value="searchStatus"
          :options="statusOptions"
          placeholder="状态"
          clearable
          class="w-page-search__item--sm"
        />
        <NSelect
          v-model:value="searchVisible"
          :options="visibleOptions"
          placeholder="显示状态"
          clearable
          class="w-page-search__item--sm"
        />
    </WSearchForm>

    <NCard class="w-page-card">
      <NSpace vertical :size="16">
        <RightToolbar @refresh="loadData">
          <NButton @click="handleToggleExpand">
            <template #icon>
              <WIcon :icon="expandAll ? 'vicons:antd:DownOutlined' : 'vicons:antd:RightOutlined'" />
            </template>
            {{ expandAll ? '收起全部' : '展开全部' }}
          </NButton>
          <PermissionButton permission="system:menu:add" type="primary" @click="handleAdd">
            <template #icon>
              <WIcon icon="vicons:antd:PlusOutlined" />
            </template>
            新增
          </PermissionButton>
        </RightToolbar>

        <NDataTable
          :columns="columns"
          :data="filteredTreeData"
          :loading="loading"
          :row-key="(row: SysMenuNode) => row.id"
          :expanded-row-keys="expandedRowKeys"
          children-key="children"
          :scroll-x="1500"
          size="small"
          striped
          @update:expanded-row-keys="handleExpandedKeysChange"
        />
      </NSpace>
    </NCard>
  </div>

  <MenuFormDrawer ref="menuFormDrawerRef" @success="handleFormSuccess" />
</template>
