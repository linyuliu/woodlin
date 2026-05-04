<!--
  @file views/system/dept/index.vue
  @description 部门管理：左侧树 + 右侧详情/编辑表单
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { onMounted, reactive, ref, type Ref } from 'vue'
import {
  NButton,
  NCard,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPopconfirm,
  NSelect,
  NSpace,
  NSplit,
  NTree,
  useDialog,
  useMessage,
  type FormInst,
  type FormRules,
  type SelectOption,
  type TreeOption,
} from 'naive-ui'
import {
  createDept,
  deleteDept,
  getDeptTree,
  updateDept,
  type SysDept,
} from '@/api/system/dept'

const message = useMessage()
const dialog = useDialog()

const treeData: Ref<TreeOption[]> = ref([])
const expanded = ref<Array<string | number>>([])
const selectedKeys = ref<Array<string | number>>([])
const loading = ref(false)

const formRef = ref<FormInst | null>(null)
const submitLoading = ref(false)
const mode = ref<'view' | 'edit' | 'create'>('view')

function defaultForm(): SysDept {
  return {
    deptId: undefined,
    parentId: 0,
    deptName: '',
    leader: '',
    phone: '',
    email: '',
    status: '1',
    sort: 0,
  }
}

const formData = reactive<SysDept>(defaultForm())

const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

const statusOptions: SelectOption[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
]

/** 部门 -> TreeOption */
function mapTree(list: SysDept[]): TreeOption[] {
  return list.map((d) => ({
    key: (d.deptId ?? d.id ?? 0) as number,
    label: d.deptName,
    raw: d,
    children: d.children && d.children.length ? mapTree(d.children) : undefined,
  }))
}

/** 收集所有 key 用于展开 */
function collectKeys(list: TreeOption[], acc: Array<string | number> = []): Array<string | number> {
  list.forEach((n) => {
    acc.push(n.key as number)
    if (n.children) {collectKeys(n.children as TreeOption[], acc)}
  })
  return acc
}

/** 拉取树 */
async function refresh(): Promise<void> {
  loading.value = true
  try {
    const res = await getDeptTree()
    treeData.value = mapTree(res ?? [])
    expanded.value = collectKeys(treeData.value)
  } finally {
    loading.value = false
  }
}

/** 选中节点 */
function handleSelect(_keys: Array<string | number>, option: Array<TreeOption | null>): void {
  const opt = option[0]
  if (!opt) {
    mode.value = 'view'
    return
  }
  const dept = (opt.raw as SysDept) ?? null
  if (!dept) {return}
  Object.assign(formData, defaultForm(), dept)
  mode.value = 'edit'
}

/** 新增根部门 */
function addRoot(): void {
  selectedKeys.value = []
  Object.assign(formData, defaultForm(), { parentId: 0 })
  mode.value = 'create'
}

/** 新增子部门 */
function addChild(): void {
  if (!formData.deptId && !formData.id) {
    message.warning('请先选择上级部门')
    return
  }
  const parentId = (formData.deptId ?? formData.id) as number
  Object.assign(formData, defaultForm(), { parentId })
  mode.value = 'create'
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const id = formData.deptId ?? formData.id
    if (mode.value === 'edit' && id) {
      await updateDept(id, formData)
      message.success('更新成功')
    } else {
      await createDept(formData)
      message.success('新增成功')
    }
    void refresh()
    mode.value = 'view'
  } finally {
    submitLoading.value = false
  }
}

/** 删除部门 */
function handleDelete(): void {
  const id = formData.deptId ?? formData.id
  if (!id) {return}
  dialog.warning({
    title: '提示',
    content: `确认删除部门 ${formData.deptName} ？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDept(id)
      message.success('删除成功')
      mode.value = 'view'
      Object.assign(formData, defaultForm())
      void refresh()
    },
  })
}

onMounted(() => {
  void refresh()
})
</script>

<template>
  <n-card size="small" class="page-dept">
    <n-split direction="horizontal" :default-size="0.32" :max="0.6" :min="0.2">
      <template #1>
        <div class="left">
          <div class="toolbar">
            <n-button size="small" type="primary" @click="addRoot">新增根部门</n-button>
          </div>
          <n-tree
            v-model:selected-keys="selectedKeys"
            v-model:expanded-keys="expanded"
            :data="treeData"
            block-line
            :loading="loading"
            @update:selected-keys="handleSelect"
          />
        </div>
      </template>
      <template #2>
        <div class="right">
          <n-empty v-if="mode === 'view'" description="请选择左侧部门进行编辑" />
          <template v-else>
            <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
              <n-form-item label="部门名称" path="deptName">
                <n-input v-model:value="formData.deptName" />
              </n-form-item>
              <n-form-item label="负责人" path="leader">
                <n-input v-model:value="formData.leader" />
              </n-form-item>
              <n-form-item label="联系电话" path="phone">
                <n-input v-model:value="formData.phone" />
              </n-form-item>
              <n-form-item label="邮箱" path="email">
                <n-input v-model:value="formData.email" />
              </n-form-item>
              <n-form-item label="排序" path="sort">
                <n-input-number v-model:value="formData.sort" :min="0" />
              </n-form-item>
              <n-form-item label="状态" path="status">
                <n-select v-model:value="formData.status" :options="statusOptions" />
              </n-form-item>
            </n-form>
            <n-space>
              <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
                保存
              </n-button>
              <n-button v-if="mode === 'edit'" @click="addChild">新增子部门</n-button>
              <n-popconfirm v-if="mode === 'edit'" @positive-click="handleDelete">
                <template #trigger>
                  <n-button type="error">删除</n-button>
                </template>
                确认删除？
              </n-popconfirm>
            </n-space>
          </template>
        </div>
      </template>
    </n-split>
  </n-card>
</template>

<style scoped>
.page-dept {
  min-height: 600px;
}
.left {
  padding-right: 12px;
}
.right {
  padding-left: 16px;
}
.toolbar {
  margin-bottom: 12px;
}
</style>
