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
  BusinessOutline,
  CreateOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  addDept,
  deleteDept,
  getDeptById,
  getDeptTree,
  updateDept,
  type SysDept
} from '@/api/dept'
import { useUserStore } from '@/stores'
import {PERMISSIONS} from '@/constants/permission-keys'
import { logger } from '@/utils/logger'

interface DeptSearchForm {
  deptName: string
}

interface DeptFormData {
  deptId?: number
  parentId: number | null
  deptName: string
  deptCode: string
  sortOrder: number
  leader: string
  phone: string
  email: string
  status: string
  remark: string
}

const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)

const searchForm = ref<DeptSearchForm>({
  deptName: ''
})

const deptTree = ref<SysDept[]>([])
const selectedKeys = ref<Array<string | number>>([])

const modalVisible = ref(false)
const formRef = ref<FormInst | null>(null)
const form = ref<DeptFormData>({
  parentId: null,
  deptName: '',
  deptCode: '',
  sortOrder: 0,
  leader: '',
  phone: '',
  email: '',
  status: '1',
  remark: ''
})

const formRules = {
  deptName: { required: true, message: '请输入部门名称', trigger: 'blur' }
}

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const selectedDeptId = computed(() => {
  const key = selectedKeys.value[0]
  if (key === undefined || key === null) {
    return null
  }
  return Number(key)
})

const treeData = computed<TreeOption[]>(() => buildTreeOptions(deptTree.value))
const parentOptions = computed<SelectOption[]>(() => buildParentOptions(deptTree.value))
const totalDepts = computed(() => countNodes(deptTree.value))
const canViewDept = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_DEPT_LIST))
const canCreateDept = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_DEPT_ADD))
const canUpdateDept = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_DEPT_EDIT))
const canDeleteDept = computed(() => userStore.hasPermission(PERMISSIONS.ACTION.SYSTEM_DEPT_REMOVE))

const selectedDeptInfo = computed(() => {
  if (!selectedDeptId.value) {
    return null
  }
  return findNodeById(deptTree.value, selectedDeptId.value)
})

const toText = (value: string | undefined | null): string => value ?? ''

const loadDeptTree = async () => {
  if (!canViewDept.value) {
    deptTree.value = []
    selectedKeys.value = []
    return
  }
  loading.value = true
  try {
    const params: Record<string, string> = {}
    if (searchForm.value.deptName.trim()) {
      params.deptName = searchForm.value.deptName.trim()
    }
    deptTree.value = await getDeptTree(params)
    if (selectedDeptId.value && !findNodeById(deptTree.value, selectedDeptId.value)) {
      selectedKeys.value = []
    }
  } catch (error) {
    logger.error('加载部门树失败', error)
    message.error('加载部门树失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadDeptTree()
}

const handleReset = () => {
  searchForm.value = {
    deptName: ''
  }
  loadDeptTree()
}

const openAdd = () => {
  if (!canCreateDept.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_ADD} 权限`)
    return
  }
  form.value = {
    parentId: selectedDeptId.value,
    deptName: '',
    deptCode: '',
    sortOrder: 0,
    leader: '',
    phone: '',
    email: '',
    status: '1',
    remark: ''
  }
  modalVisible.value = true
}

const openEdit = async () => {
  if (!canUpdateDept.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_EDIT} 权限`)
    return
  }
  if (!selectedDeptId.value) {
    message.warning('请先选择部门')
    return
  }

  loading.value = true
  try {
    const detail = await getDeptById(selectedDeptId.value)
    form.value = {
      deptId: detail.deptId,
      parentId: detail.parentId ?? null,
      deptName: detail.deptName,
      deptCode: toText(detail.deptCode),
      sortOrder: detail.sortOrder ?? 0,
      leader: toText(detail.leader),
      phone: toText(detail.phone),
      email: toText(detail.email),
      status: detail.status ?? '1',
      remark: toText(detail.remark)
    }
    modalVisible.value = true
  } catch (error) {
    logger.error('加载部门详情失败', error)
    message.error('加载部门详情失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  if (!canDeleteDept.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_REMOVE} 权限`)
    return
  }
  if (!selectedDeptId.value) {
    message.warning('请先选择部门')
    return
  }

  loading.value = true
  try {
    await deleteDept(selectedDeptId.value)
    message.success('删除成功')
    selectedKeys.value = []
    await loadDeptTree()
  } catch (error) {
    logger.error('删除部门失败', error)
    message.error('删除失败')
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  await formRef.value?.validate()

  const payload: SysDept = {
    deptId: form.value.deptId,
    parentId: form.value.parentId,
    deptName: form.value.deptName.trim(),
    deptCode: form.value.deptCode.trim(),
    sortOrder: form.value.sortOrder,
    leader: form.value.leader.trim(),
    phone: form.value.phone.trim(),
    email: form.value.email.trim(),
    status: form.value.status,
    remark: form.value.remark.trim()
  }

  loading.value = true
  try {
    if (payload.deptId) {
      if (!canUpdateDept.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_EDIT} 权限`)
        return
      }
      await updateDept(payload)
      message.success('修改成功')
    } else {
      if (!canCreateDept.value) {
        message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_ADD} 权限`)
        return
      }
      await addDept(payload)
      message.success('新增成功')
    }
    modalVisible.value = false
    await loadDeptTree()
  } catch (error) {
    logger.error('保存部门失败', error)
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

const buildTreeOptions = (nodes: SysDept[]): TreeOption[] =>
  nodes.map(node => ({
    key: node.deptId || 0,
    label: node.deptName,
    prefix: () => h(NIcon, null, { default: () => h(BusinessOutline) }),
    children: node.children ? buildTreeOptions(node.children) : undefined
  }))

const buildParentOptions = (nodes: SysDept[], list: SelectOption[] = [], depth = 0): SelectOption[] => {
  const prefix = depth > 0 ? `${'  '.repeat(depth)}└ ` : ''

  for (const node of nodes) {
    if (node.deptId) {
      list.push({
        label: `${prefix}${node.deptName}`,
        value: node.deptId
      })
    }
    if (node.children && node.children.length > 0) {
      buildParentOptions(node.children, list, depth + 1)
    }
  }

  return list
}

const countNodes = (nodes: SysDept[]): number =>
  nodes.reduce((total, item) => total + 1 + countNodes(item.children || []), 0)

const findNodeById = (nodes: SysDept[], id: number): SysDept | null => {
  for (const node of nodes) {
    if (node.deptId === id) {
      return node
    }
    const child = findNodeById(node.children || [], id)
    if (child) {
      return child
    }
  }
  return null
}

onMounted(() => {
  if (!canViewDept.value) {
    message.warning(`缺少 ${PERMISSIONS.ACTION.SYSTEM_DEPT_LIST} 权限`)
    return
  }
  loadDeptTree()
})
</script>

<template>
  <div class="dept-page">
    <NCard :bordered="false" class="hero-card">
      <div class="hero-content">
        <div>
          <h2>组织架构管理</h2>
          <p>维护部门层级、负责人和基础联系方式，用户所属部门会直接引用这里的数据。</p>
        </div>
        <NTag type="info" size="small">共 {{ totalDepts }} 个部门</NTag>
      </div>
    </NCard>

    <NCard :bordered="false" class="toolbar-card">
      <NForm inline :model="searchForm" label-placement="left">
        <NFormItem label="部门名称">
          <NInput v-model:value="searchForm.deptName" clearable placeholder="请输入部门名称" style="width: 220px" />
        </NFormItem>
        <NFormItem>
          <NSpace>
            <NButton type="primary" :loading="loading" :disabled="!canViewDept" @click="handleSearch">
              <template #icon>
                <NIcon><SearchOutline /></NIcon>
              </template>
              搜索
            </NButton>
            <NButton :disabled="!canViewDept" @click="handleReset">
              <template #icon>
                <NIcon><RefreshOutline /></NIcon>
              </template>
              重置
            </NButton>
            <NButton v-if="canCreateDept" type="primary" secondary @click="openAdd">
              <template #icon>
                <NIcon><AddOutline /></NIcon>
              </template>
              新增
            </NButton>
            <NButton v-if="canUpdateDept" :disabled="!selectedDeptId" @click="openEdit">
              <template #icon>
                <NIcon><CreateOutline /></NIcon>
              </template>
              编辑
            </NButton>
            <NPopconfirm v-if="canDeleteDept" @positive-click="handleDelete">
              <template #trigger>
                <NButton type="error" :disabled="!selectedDeptId">
                  <template #icon>
                    <NIcon><TrashOutline /></NIcon>
                  </template>
                  删除
                </NButton>
              </template>
              确认删除当前部门？
            </NPopconfirm>
          </NSpace>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" title="部门树" class="tree-card">
      <template #header-extra>
        <NTag v-if="selectedDeptInfo" size="small" type="success">
          当前：{{ selectedDeptInfo.deptName }}
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
      :title="form.deptId ? '编辑部门' : '新增部门'"
      style="width: 620px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="formRef" :model="form" :rules="formRules" label-placement="left" label-width="100">
        <NFormItem label="部门名称" path="deptName">
          <NInput v-model:value="form.deptName" placeholder="请输入部门名称" />
        </NFormItem>
        <NFormItem label="上级部门">
          <NSelect v-model:value="form.parentId" :options="parentOptions" clearable placeholder="根部门可留空" />
        </NFormItem>
        <NFormItem label="部门编码">
          <NInput v-model:value="form.deptCode" placeholder="可选，例如 RD_CENTER" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="负责人">
          <NInput v-model:value="form.leader" placeholder="可选" />
        </NFormItem>
        <NFormItem label="联系电话">
          <NInput v-model:value="form.phone" placeholder="可选" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="form.email" placeholder="可选" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="form.status" :options="statusOptions" />
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
            :disabled="(form.deptId && !canUpdateDept) || (!form.deptId && !canCreateDept)"
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
.dept-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  background: linear-gradient(120deg, #1f3c88 0%, #2d63c8 52%, #5f8fe6 100%);
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
