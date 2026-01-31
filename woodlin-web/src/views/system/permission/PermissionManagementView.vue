<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDivider,
  NForm,
  NFormItem,
  NGrid,
  NGridItem,
  NInput,
  NInputNumber,
  NModal,
  NSpace,
  NTag,
  NTree,
  useDialog,
  useMessage,
  type FormInst,
  type TreeOption
} from 'naive-ui'
import {
  addRole,
  deleteRole,
  getRoleAllPermissions,
  getRoleById,
  getRoleTree,
  updateRole,
  type RoleTreeNode,
  type SysRole
} from '@/api/role'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const treeLoading = ref(false)
const roleTree = ref<TreeOption[]>([])
const selectedKeys = ref<(string | number)[]>([])
const selectedRole = ref<SysRole | null>(null)
const permissions = ref<string[]>([])

const roleModalVisible = ref(false)
const roleFormRef = ref<FormInst | null>(null)
const roleForm = reactive<SysRole>({
  roleName: '',
  roleCode: '',
  sortOrder: 0,
  status: '1',
  parentRoleId: null,
  remark: ''
})
const roleRules = {
  roleName: { required: true, message: '请输入角色名称', trigger: 'blur' },
  roleCode: { required: true, message: '请输入角色编码', trigger: 'blur' }
}
const isEditRole = computed(() => !!roleForm.roleId)

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const buildTreeOptions = (nodes: RoleTreeNode[]): TreeOption[] =>
  nodes.map(node => ({
    key: node.roleId,
    label: `${node.roleName} (${node.roleCode})`,
    children: node.children ? buildTreeOptions(node.children) : undefined
  }))

const loadRoleTree = async () => {
  treeLoading.value = true
  try {
    const tree = await getRoleTree()
    roleTree.value = buildTreeOptions(tree)
  } catch (error) {
    console.error(error)
    message.error('加载角色树失败')
  } finally {
    treeLoading.value = false
  }
}

const loadRoleDetail = async (roleId: number) => {
  loading.value = true
  try {
    selectedRole.value = await getRoleById(roleId)
    permissions.value = await getRoleAllPermissions(roleId)
  } catch (error) {
    console.error(error)
    message.error('加载角色详情失败')
  } finally {
    loading.value = false
  }
}

const handleSelectRole = (keys: (string | number)[]) => {
  selectedKeys.value = keys
  const roleId = Number(keys[0])
  if (roleId) {
    loadRoleDetail(roleId)
  }
}

const openAddRootRole = () => {
  Object.assign(roleForm, {
    roleId: undefined,
    roleName: '',
    roleCode: '',
    sortOrder: 0,
    status: '1',
    parentRoleId: null,
    remark: ''
  })
  roleModalVisible.value = true
}

const openAddChildRole = () => {
  if (!selectedRole.value) {
    message.warning('请先选择父角色')
    return
  }
  Object.assign(roleForm, {
    roleId: undefined,
    roleName: '',
    roleCode: '',
    sortOrder: 0,
    status: '1',
    parentRoleId: selectedRole.value.roleId,
    remark: ''
  })
  roleModalVisible.value = true
}

const openEditRole = () => {
  if (!selectedRole.value) {
    message.warning('请先选择角色')
    return
  }
  Object.assign(roleForm, selectedRole.value)
  // 兼容字段名
  roleForm.sortOrder = (selectedRole.value as any).sortOrder ?? selectedRole.value.roleSort ?? 0
  roleModalVisible.value = true
}

const submitRole = async () => {
  await roleFormRef.value?.validate()
  loading.value = true
  try {
    const payload = { ...roleForm, sortOrder: roleForm.sortOrder ?? roleForm.roleSort }
    if (isEditRole.value) {
      await updateRole(payload)
      message.success('角色更新成功')
    } else {
      await addRole(payload)
      message.success('角色新增成功')
    }
    roleModalVisible.value = false
    await loadRoleTree()
    if (payload.parentRoleId) {
      await loadRoleDetail(payload.parentRoleId)
      selectedKeys.value = [payload.parentRoleId]
    } else if (payload.roleId) {
      await loadRoleDetail(payload.roleId)
      selectedKeys.value = [payload.roleId]
    }
  } catch (error) {
    console.error(error)
    message.error('保存角色失败')
  } finally {
    loading.value = false
  }
}

const handleDeleteRole = () => {
  if (!selectedRole.value?.roleId) {
    message.warning('请先选择角色')
    return
  }
  dialog.warning({
    title: '删除角色',
    content: '删除后不可恢复，确认继续？',
    positiveText: '删除',
    negativeText: '取消',
    async onPositiveClick() {
      try {
        await deleteRole(String(selectedRole.value?.roleId))
        message.success('删除成功')
        selectedRole.value = null
        selectedKeys.value = []
        permissions.value = []
        await loadRoleTree()
      } catch (error) {
        console.error(error)
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  loadRoleTree()
})
</script>

<template>
  <div class="permission-management-container">
    <NSpace vertical :size="16">
      <NCard :bordered="false">
        <NSpace wrap>
          <NButton type="primary" @click="openAddRootRole">新增根角色</NButton>
          <NButton :disabled="!selectedRole" @click="openAddChildRole">新增子角色</NButton>
          <NButton :disabled="!selectedRole" @click="openEditRole">编辑角色</NButton>
          <NButton :disabled="!selectedRole" type="error" @click="handleDeleteRole">删除</NButton>
          <NButton :loading="treeLoading" @click="loadRoleTree">刷新树</NButton>
        </NSpace>
      </NCard>

      <NGrid :cols="24" :x-gap="16">
        <NGridItem :span="8">
          <NCard title="角色树" :bordered="false">
            <NTree
              :data="roleTree"
              :loading="treeLoading"
              block-line
              expand-on-click
              selectable
              :selected-keys="selectedKeys"
              @update:selected-keys="handleSelectRole"
            />
          </NCard>
        </NGridItem>

        <NGridItem :span="16">
          <NCard title="角色详情" :bordered="false">
            <template v-if="selectedRole">
              <NSpace vertical :size="12">
                <div class="role-header">
                  <div class="title">
                    {{ selectedRole.roleName }}
                    <NTag size="small" type="info" style="margin-left: 8px">{{ selectedRole.roleCode }}</NTag>
                  </div>
                  <NTag :type="selectedRole.status === '1' ? 'success' : 'warning'">
                    {{ selectedRole.status === '1' ? '启用' : '禁用' }}
                  </NTag>
                </div>
                <div class="info-row">
                  <span>排序：{{ selectedRole.sortOrder ?? selectedRole.roleSort ?? 0 }}</span>
                  <span>父角色：{{ selectedRole.parentRoleId ?? '无' }}</span>
                </div>
                <div>备注：{{ selectedRole.remark || '—' }}</div>
                <NDivider title-placement="left">权限列表（含继承）</NDivider>
                <div v-if="permissions.length > 0" class="perm-wrap">
                  <NTag v-for="item in permissions" :key="item" size="small" type="info" class="perm-tag">
                    {{ item }}
                  </NTag>
                </div>
                <div v-else class="placeholder">暂未返回权限数据</div>
              </NSpace>
            </template>
            <template v-else>
              <div class="placeholder">请在左侧选择一个角色查看详情</div>
            </template>
          </NCard>
        </NGridItem>
      </NGrid>
    </NSpace>

    <NModal
      v-model:show="roleModalVisible"
      preset="card"
      :title="isEditRole ? '编辑角色' : '新增角色'"
      style="width: 520px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="90" label-placement="left">
        <NFormItem label="角色名称" path="roleName">
          <NInput v-model:value="roleForm.roleName" placeholder="例如：管理员" />
        </NFormItem>
        <NFormItem label="角色编码" path="roleCode">
          <NInput v-model:value="roleForm.roleCode" placeholder="例如：ADMIN" />
        </NFormItem>
        <NFormItem label="父角色">
          <NInputNumber v-model:value="roleForm.parentRoleId" :disabled="true" placeholder="根角色默认空" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="roleForm.sortOrder" :min="0" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="roleForm.status" :options="statusOptions" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="roleForm.remark" type="textarea" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="roleModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="loading" @click="submitRole">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.permission-management-container {
  width: 100%;
  height: 100%;
}

.role-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.info-row {
  display: flex;
  gap: 16px;
  color: #666;
}

.perm-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.perm-tag {
  margin-bottom: 4px;
}

.placeholder {
  padding: 24px 0;
  color: #999;
  text-align: center;
}
</style>
