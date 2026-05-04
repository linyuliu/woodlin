<!--
  @file views/system/menu/index.vue
  @description 菜单 / 权限管理：树形展示 + 新增/编辑抽屉
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { h, onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPopconfirm,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NSwitch,
  NTreeSelect,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeSelectOption,
} from 'naive-ui'
import {
  createMenu,
  deleteMenu,
  getMenuTree,
  updateMenu,
} from '@/api/system/menu'
import type { RouteItem } from '@/types/global'

interface MenuForm {
  id?: number
  parentId: number
  type: 1 | 2 | 3
  title: string
  name: string
  path: string
  component: string
  icon: string
  sort: number
  isHidden: boolean
  isCache: boolean
  isFrame: boolean
  showInTabs: boolean
  activeMenu: string
  redirect: string
  permission: string
}

const message = useMessage()
const dialog = useDialog()

const treeData: Ref<RouteItem[]> = ref([])
const loading = ref(false)

const drawerVisible = ref(false)
const drawerTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)

function defaultForm(): MenuForm {
  return {
    id: undefined,
    parentId: 0,
    type: 2,
    title: '',
    name: '',
    path: '',
    component: '',
    icon: '',
    sort: 0,
    isHidden: false,
    isCache: false,
    isFrame: false,
    showInTabs: true,
    activeMenu: '',
    redirect: '',
    permission: '',
  }
}

const formData = reactive<MenuForm>(defaultForm())

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', type: 'number' as never }],
}

const typeOptions: SelectOption[] = [
  { label: '目录', value: 1 },
  { label: '菜单', value: 2 },
  { label: '按钮', value: 3 },
]

/** 树 -> n-tree-select options（含根） */
function mapToSelect(list: RouteItem[]): TreeSelectOption[] {
  return list.map((m) => ({
    key: m.id,
    label: m.title,
    children: m.children && m.children.length ? mapToSelect(m.children) : undefined,
  }))
}

const parentOptions = ref<TreeSelectOption[]>([])

/** 拉取树 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await getMenuTree()
    treeData.value = res ?? []
    parentOptions.value = [
      { key: 0, label: '根节点', children: mapToSelect(treeData.value) },
    ]
  } finally {
    loading.value = false
  }
}

/** 打开新增 */
function openAdd(parent?: RouteItem): void {
  isEdit.value = false
  drawerTitle.value = '新增菜单'
  Object.assign(formData, defaultForm(), { parentId: parent?.id ?? 0 })
  drawerVisible.value = true
}

/** 打开编辑 */
function openEdit(row: RouteItem): void {
  isEdit.value = true
  drawerTitle.value = '编辑菜单'
  Object.assign(formData, defaultForm(), {
    id: row.id,
    parentId: row.parentId ?? 0,
    type: row.type,
    title: row.title,
    name: row.name,
    path: row.path,
    component: row.component,
    icon: row.icon ?? '',
    sort: row.sort ?? 0,
    isHidden: !!row.isHidden,
    isCache: !!row.isCache,
    isFrame: !!row.isFrame,
    showInTabs: row.showInTabs ?? true,
    activeMenu: row.activeMenu ?? '',
    redirect: row.redirect ?? '',
    permission: row.permission ?? '',
  })
  drawerVisible.value = true
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload: Partial<RouteItem> = {
      parentId: formData.parentId,
      type: formData.type,
      title: formData.title,
      name: formData.name,
      path: formData.path,
      component: formData.component,
      icon: formData.icon,
      sort: formData.sort,
      isHidden: formData.isHidden,
      isCache: formData.isCache,
      isFrame: formData.isFrame,
      showInTabs: formData.showInTabs,
      activeMenu: formData.activeMenu,
      redirect: formData.redirect,
      permission: formData.permission,
    }
    if (isEdit.value && formData.id) {
      await updateMenu(formData.id, payload)
      message.success('更新成功')
    } else {
      await createMenu(payload)
      message.success('新增成功')
    }
    drawerVisible.value = false
    void refresh()
  } finally {
    submitLoading.value = false
  }
}

/** 删除菜单 */
function handleDelete(row: RouteItem): void {
  dialog.warning({
    title: '提示',
    content: `确认删除菜单 ${row.title} 及其子项？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteMenu(row.id)
      message.success('删除成功')
      void refresh()
    },
  })
}

const columns: DataTableColumns<RouteItem> = [
  { title: '名称', key: 'title', width: 220, tree: true },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: (row) => (row.type === 1 ? '目录' : row.type === 2 ? '菜单' : '按钮'),
  },
  { title: '路由', key: 'path', width: 180 },
  { title: '组件', key: 'component', width: 200 },
  { title: '权限标识', key: 'permission', width: 180 },
  { title: '排序', key: 'sort', width: 70 },
  {
    title: '操作',
    key: 'action',
    width: 220,
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(
          NButton,
          { size: 'small', text: true, type: 'primary', onClick: () => openAdd(row) },
          { default: () => '新增子项' },
        ),
        h(
          NButton,
          { size: 'small', text: true, type: 'info', onClick: () => openEdit(row) },
          { default: () => '编辑' },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row) },
          {
            default: () => '确认删除？',
            trigger: () =>
              h(NButton, { size: 'small', text: true, type: 'error' }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-menu">
    <n-card size="small">
      <div class="toolbar">
        <n-button v-permission="'system:menu:add'" type="primary" @click="openAdd()">
          新增根菜单
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="treeData"
        :loading="loading"
        :row-key="(row: RouteItem) => row.id"
        default-expand-all
        :scroll-x="1200"
        striped
      />
    </n-card>

    <n-drawer v-model:show="drawerVisible" :width="600">
      <n-drawer-content :title="drawerTitle" closable>
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
          <n-form-item label="上级菜单" path="parentId">
            <n-tree-select
              v-model:value="formData.parentId"
              :options="parentOptions"
              default-expand-all
            />
          </n-form-item>
          <n-form-item label="类型" path="type">
            <n-radio-group v-model:value="formData.type">
              <n-radio-button :value="1">目录</n-radio-button>
              <n-radio-button :value="2">菜单</n-radio-button>
              <n-radio-button :value="3">按钮</n-radio-button>
            </n-radio-group>
          </n-form-item>
          <n-form-item label="标题" path="title">
            <n-input v-model:value="formData.title" />
          </n-form-item>
          <n-form-item v-if="formData.type !== 3" label="路由 name" path="name">
            <n-input v-model:value="formData.name" />
          </n-form-item>
          <n-form-item v-if="formData.type !== 3" label="路由 path" path="path">
            <n-input v-model:value="formData.path" />
          </n-form-item>
          <n-form-item v-if="formData.type === 2" label="组件路径" path="component">
            <n-input v-model:value="formData.component" placeholder="如 system/user/index" />
          </n-form-item>
          <n-form-item v-if="formData.type !== 3" label="图标" path="icon">
            <n-input v-model:value="formData.icon" placeholder="如 vicons:antd:UserOutlined" />
          </n-form-item>
          <n-form-item label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" />
          </n-form-item>
          <n-form-item v-if="formData.type !== 3" label="是否显示" path="isHidden">
            <n-switch v-model:value="formData.isHidden" />
            <span class="tip">开启即隐藏</span>
          </n-form-item>
          <n-form-item v-if="formData.type === 2" label="是否缓存" path="isCache">
            <n-switch v-model:value="formData.isCache" />
          </n-form-item>
          <n-form-item v-if="formData.type !== 3" label="是否外链" path="isFrame">
            <n-switch v-model:value="formData.isFrame" />
          </n-form-item>
          <n-form-item v-if="formData.type === 2" label="显示在 Tabs" path="showInTabs">
            <n-switch v-model:value="formData.showInTabs" />
          </n-form-item>
          <n-form-item v-if="formData.type === 2" label="激活菜单" path="activeMenu">
            <n-input v-model:value="formData.activeMenu" />
          </n-form-item>
          <n-form-item v-if="formData.type === 1" label="重定向" path="redirect">
            <n-input v-model:value="formData.redirect" />
          </n-form-item>
          <n-form-item label="权限标识" path="permission">
            <n-input v-model:value="formData.permission" placeholder="如 system:user:add" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="drawerVisible = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
              确定
            </n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>
