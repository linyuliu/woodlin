<script setup lang="ts">
/**
 * 部门管理视图
 * 
 * @author mumu
 * @description 部门管理（前端 mock，RBAC1 数据范围）
 * @since 2025-01-01 (rev 2026-01-31)
 */
import { ref, h } from 'vue'
import { 
  NCard, NButton, NTree, NSpace, NInput, NForm, NFormItem,
  NIcon, NPopconfirm, NEmpty, NModal, NInputNumber, NSelect,
  useMessage, type TreeOption, type FormInst
} from 'naive-ui'
import { 
  AddOutline, 
  SearchOutline, 
  RefreshOutline,
  CreateOutline,
  TrashOutline,
  BusinessOutline
} from '@vicons/ionicons5'
import {
  fetchDeptTree,
  createDept,
  updateDept,
  deleteDept,
  type DeptNode
} from '@/api/mock/rbac'

/**
 * 搜索表单数据
 */
const searchForm = ref({
  name: ''
})

/**
 * 加载状态
 */
const loading = ref(false)
const message = useMessage()

const deptTree = ref<TreeOption[]>([])
const flatDepts = ref<{ label: string; value: number }[]>([])
const selectedKeys = ref<string[]>([])

const modalShow = ref(false)
const formRef = ref<FormInst | null>(null)
const form = ref<Partial<DeptNode>>({
  deptName: '',
  parentId: null,
  orderNum: 1,
  status: '1'
})

const rules = {
  deptName: { required: true, message: '请输入部门名称', trigger: 'blur' }
}

/**
 * 部门树形数据
 */
const buildTree = (nodes: DeptNode[]): TreeOption[] =>
  nodes.map(n => ({
    key: n.deptId,
    label: n.deptName,
    children: n.children ? buildTree(n.children) : undefined
  }))

const rebuildFlat = (nodes: DeptNode[], list: { label: string; value: number }[] = []) => {
  nodes.forEach(n => {
    list.push({ label: n.deptName, value: n.deptId })
    if (n.children) rebuildFlat(n.children, list)
  })
  return list
}

/**
 * 搜索部门
 */
const handleSearch = () => {
  // 前端本地树，不做额外请求
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.value = {
    name: ''
  }
  selectedKeys.value = []
}

/**
 * 添加部门
 */
const handleAdd = () => {
  form.value = {
    deptId: undefined,
    deptName: '',
    parentId: selectedKeys.value.length ? Number(selectedKeys.value[0]) : null,
    orderNum: 1,
    status: '1'
  }
  modalShow.value = true
}

/**
 * 编辑部门
 */
const handleEdit = () => {
  const id = selectedKeys.value[0]
  if (!id) {
    message.warning('请先选择要编辑的部门')
    return
  }
  // 从树中取出
  const find = (list: any[]): DeptNode | undefined => {
    for (const n of list) {
      if (n.deptId === Number(id)) return n
      if (n.children) {
        const hit = find(n.children)
        if (hit) return hit
      }
    }
  }
  const current = find((deptTree.value as any[]).map(x => ({
    deptId: x.key,
    deptName: x.label,
    children: (x as any).children?.map((c: any) => ({ deptId: c.key, deptName: c.label, children: c.children }))
  })))
  form.value = current ? { ...current } : { deptId: Number(id) }
  modalShow.value = true
}

/**
 * 删除部门
 */
const handleDelete = () => {
  if (selectedKeys.value.length === 0) {
    message.warning('请先选择要删除的部门')
    return
  }
  const id = Number(selectedKeys.value[0])
  deleteDept(id).then(() => {
    message.success('删除成功')
    loadDept()
  })
}

/**
 * 树节点前缀
 */
const renderPrefix = () => {
  return h(NIcon, { size: 18, style: { marginRight: '8px' } }, {
    default: () => h(BusinessOutline)
  })
}

const submit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    if (form.value.deptId) {
      await updateDept(form.value as DeptNode)
      message.success('更新成功')
    } else {
      await createDept(form.value)
      message.success('新增成功')
    }
    modalShow.value = false
    await loadDept()
  } finally {
    loading.value = false
  }
}

const loadDept = async () => {
  loading.value = true
  try {
    const list = await fetchDeptTree()
    deptTree.value = buildTree(list)
    flatDepts.value = rebuildFlat(list)
  } finally {
    loading.value = false
  }
}

loadDept()
</script>

<template>
  <div class="dept-management-container">
    <NSpace vertical :size="16">
      <!-- 搜索区域 -->
      <NCard :bordered="false" class="search-card">
        <NForm inline :model="searchForm" label-placement="left">
          <NFormItem label="部门名称">
            <NInput 
              v-model:value="searchForm.name" 
              placeholder="请输入部门名称"
              clearable
              style="width: 300px"
            />
          </NFormItem>
          <NFormItem>
            <NSpace>
              <NButton type="primary" @click="handleSearch" :loading="loading">
                <template #icon>
                  <NIcon>
                    <SearchOutline />
                  </NIcon>
                </template>
                搜索
              </NButton>
              <NButton @click="handleReset">
                <template #icon>
                  <NIcon>
                    <RefreshOutline />
                  </NIcon>
                </template>
                重置
              </NButton>
            </NSpace>
          </NFormItem>
        </NForm>
      </NCard>

      <!-- 部门树 -->
      <NCard 
        title="部门结构" 
        :bordered="false" 
        :segmented="{ content: true }"
        class="tree-card"
      >
        <template #header-extra>
          <NSpace>
            <NButton type="primary" @click="handleAdd">
              <template #icon>
                <NIcon>
                  <AddOutline />
                </NIcon>
              </template>
              添加部门
            </NButton>
            <NButton 
              type="info" 
              @click="handleEdit"
              :disabled="selectedKeys.length === 0"
            >
              <template #icon>
                <NIcon>
                  <CreateOutline />
                </NIcon>
              </template>
              编辑
            </NButton>
            <NPopconfirm
              @positive-click="handleDelete"
            >
              <template #trigger>
                <NButton 
                  type="error" 
                  :disabled="selectedKeys.length === 0"
                >
                  <template #icon>
                    <NIcon>
                      <TrashOutline />
                    </NIcon>
                  </template>
                  删除
                </NButton>
              </template>
              确定要删除选中的部门吗？
            </NPopconfirm>
          </NSpace>
        </template>
        
        <div class="tree-container">
          <NTree
            v-if="deptTree.length > 0"
            block-line
            checkable
            selectable
            expand-on-click
            :data="deptTree"
            :default-expanded-keys="[1,10,11]"
            v-model:selected-keys="selectedKeys"
            :render-prefix="renderPrefix"
          />
          <NEmpty 
            v-else
            description="暂无部门数据"
            style="padding: 40px 0"
          />
        </div>
      </NCard>
    </NSpace>

    <NModal
      v-model:show="modalShow"
      preset="card"
      :title="form.deptId ? '编辑部门' : '新增部门'"
      style="width: 520px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="formRef" :model="form" :rules="rules" label-width="90" label-placement="left">
        <NFormItem label="部门名称" path="deptName">
          <NInput v-model:value="form.deptName" placeholder="请输入部门名称" />
        </NFormItem>
        <NFormItem label="上级部门">
          <NSelect
            v-model:value="form.parentId"
            :options="flatDepts"
            clearable
            placeholder="根部门留空"
          />
        </NFormItem>
        <NFormItem label="排序值">
          <NInputNumber v-model:value="form.orderNum" :min="0" />
        </NFormItem>
        <NFormItem label="负责人">
          <NInput v-model:value="form.leader" placeholder="可选填" />
        </NFormItem>
        <NFormItem label="联系电话">
          <NInput v-model:value="form.phone" placeholder="可选填" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect
            v-model:value="form.status"
            :options="[
              { label: '启用', value: '1' },
              { label: '禁用', value: '0' }
            ]"
          />
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
.dept-management-container {
  width: 100%;
  height: 100%;
}

.search-card {
  background: #fff;
}

.tree-card {
  background: #fff;
}

.tree-container {
  padding: 16px;
  min-height: 400px;
}
</style>
