<script setup lang="ts">
/**
 * 权限管理视图
 * 
 * @author mumu
 * @description 复杂的权限管理页面，支持菜单权限、数据权限、用户组和部门权限配置
 * @since 2025-01-01
 */
import { ref, h, computed } from 'vue'
import {
  NCard,
  NButton,
  NSpace,
  NInput,
  NForm,
  NFormItem,
  NCheckbox,
  NCheckboxGroup,
  NTree,
  NModal,
  NSelect,
  NGrid,
  NGridItem,
  NRadioGroup,
  NRadio,
  NDivider,
  useMessage,
  type TreeOption
} from 'naive-ui'
import {
  SaveOutline,
  CloseOutline,
  AddOutline,
  RefreshOutline
} from '@vicons/ionicons5'

const message = useMessage()

/**
 * 权限配置表单
 */
interface PermissionForm {
  menuName: string
  menuCode: string
  parentId?: number
  functionPermissions: string[]
  dataPermissionType: string
  userGroups: string[]
  departments: string[]
}

/**
 * 显示权限配置对话框
 */
const showModal = ref(false)
const loading = ref(false)

/**
 * 权限表单数据
 */
const formData = ref<PermissionForm>({
  menuName: '',
  menuCode: '',
  functionPermissions: [],
  dataPermissionType: 'custom',
  userGroups: [],
  departments: []
})

/**
 * 系统菜单树形数据
 */
const menuTreeData = ref<TreeOption[]>([
  {
    key: 'system',
    label: '系统管理',
    children: [
      {
        key: 'system-user',
        label: '账号管理'
      },
      {
        key: 'system-role',
        label: '角色管理'
      },
      {
        key: 'system-dept',
        label: '部门管理'
      },
      {
        key: 'system-menu',
        label: '菜单管理'
      },
      {
        key: 'system-dict',
        label: '字典管理'
      },
      {
        key: 'system-config',
        label: '参数管理'
      },
      {
        key: 'system-client',
        label: '客户端管理'
      },
      {
        key: 'system-log',
        label: '部门管理'
      },
      {
        key: 'system-notice',
        label: '通知管理'
      },
      {
        key: 'system-permission',
        label: '数据权限'
      },
      {
        key: 'system-file',
        label: '文件管理'
      },
      {
        key: 'system-backup',
        label: '模板文件管理'
      },
      {
        key: 'system-log-login',
        label: '登陆日志'
      }
    ]
  },
  {
    key: 'tool',
    label: '工具箱',
    children: [
      {
        key: 'tool-gen',
        label: '代码生成'
      },
      {
        key: 'tool-count',
        label: '教师统计'
      }
    ]
  }
])

/**
 * 选中的菜单节点
 */
const selectedMenuKeys = ref<string[]>([])

/**
 * 功能权限选项
 */
const functionPermissionOptions = [
  { label: '查询账号', value: 'query' },
  { label: '新增账号', value: 'add' },
  { label: '编辑账号', value: 'edit' },
  { label: '删除账号', value: 'delete' },
  { label: '设置角色', value: 'setRole' },
  { label: '解锁', value: 'unlock' },
  { label: '查看密码', value: 'viewPassword' },
  { label: '设置密码', value: 'setPassword' },
  { label: '设置数据角色', value: 'setDataRole' }
]

/**
 * 数据权限类型选项
 */
const dataPermissionTypeOptions = [
  { label: '自定义', value: 'custom' },
  { label: '全部', value: 'all' },
  { label: '本部门', value: 'dept' },
  { label: '本部门及以下', value: 'deptAndSub' }
]

/**
 * 用户组选项
 */
const userGroupOptions = [
  { label: '系统管理员', value: 'system-admin' },
  { label: '测试用户', value: 'test-user' },
  { label: '测试1-仅查及以下', value: 'test1' },
  { label: '测试2-仅本部', value: 'test2' },
  { label: '测试3-仅本人', value: 'test3' },
  { label: '测试4-自定义', value: 'test4' }
]

/**
 * 部门树形数据
 */
const departmentTreeData = ref<TreeOption[]>([
  {
    key: 'tech',
    label: '技术部',
    children: [
      {
        key: 'tech-dev',
        label: '研发团队',
        children: [
          { key: 'tech-dev-mobile', label: '移动组' },
          { key: 'tech-dev-base', label: '基础组' },
          { key: 'tech-dev-frontend', label: '前端组' },
          { key: 'tech-dev-backend', label: '后端组' },
          { key: 'tech-dev-arch', label: '架构组' }
        ]
      },
      {
        key: 'tech-test',
        label: '测试团队'
      },
      {
        key: 'tech-ops',
        label: '运维团队'
      }
    ]
  },
  {
    key: 'operation',
    label: '运营部',
    children: [
      { key: 'operation-product', label: '产品运营' },
      { key: 'operation-user', label: '用户运营' }
    ]
  }
])

/**
 * 选中的部门
 */
const selectedDepartments = ref<string[]>([])

/**
 * 处理菜单选择
 */
const handleMenuSelect = (keys: string[]) => {
  selectedMenuKeys.value = keys
  if (keys.length > 0) {
    // 模拟加载菜单权限数据
    loadMenuPermission(keys[0])
  }
}

/**
 * 加载菜单权限配置
 */
const loadMenuPermission = (menuKey: string) => {
  showModal.value = true
  // 模拟数据
  formData.value = {
    menuName: '账号管理',
    menuCode: 'teacher_statics_menu',
    functionPermissions: ['query', 'add', 'edit'],
    dataPermissionType: 'custom',
    userGroups: [],
    departments: ['tech-dev']
  }
  selectedDepartments.value = ['tech-dev']
}

/**
 * 保存权限配置
 */
const handleSave = async () => {
  loading.value = true
  
  try {
    // TODO: 调用API保存权限配置
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.success('权限配置保存成功')
    showModal.value = false
  } catch (error) {
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

/**
 * 取消配置
 */
const handleCancel = () => {
  showModal.value = false
}

/**
 * 新增菜单
 */
const handleAddMenu = () => {
  formData.value = {
    menuName: '',
    menuCode: '',
    functionPermissions: [],
    dataPermissionType: 'custom',
    userGroups: [],
    departments: []
  }
  showModal.value = true
}

/**
 * 刷新菜单树
 */
const handleRefresh = () => {
  message.info('刷新菜单树')
}

/**
 * 展开/收起所有节点
 */
const expandAll = ref(false)
const handleExpandAll = () => {
  expandAll.value = !expandAll.value
}
</script>

<template>
  <div class="permission-management-container">
    <NSpace vertical :size="16">
      <!-- 工具栏 -->
      <NCard :bordered="false" class="toolbar-card">
        <NSpace>
          <NButton type="primary" @click="handleAddMenu">
            <template #icon>
              <AddOutline />
            </template>
            新增菜单
          </NButton>
          <NButton @click="handleExpandAll">
            {{ expandAll ? '全部收起' : '全部展开' }}
          </NButton>
          <NButton @click="handleRefresh">
            <template #icon>
              <RefreshOutline />
            </template>
            刷新
          </NButton>
        </NSpace>
      </NCard>

      <!-- 主内容区域 -->
      <NCard :bordered="false" title="权限管理" class="content-card">
        <NGrid :cols="24" :x-gap="16">
          <!-- 左侧菜单树 -->
          <NGridItem :span="6">
            <div class="menu-tree-container">
              <NTree
                :data="menuTreeData"
                :selectable="true"
                :selected-keys="selectedMenuKeys"
                @update:selected-keys="handleMenuSelect"
                block-line
                :default-expanded-keys="expandAll ? ['system', 'tool'] : []"
              />
            </div>
          </NGridItem>

          <!-- 右侧权限配置提示 -->
          <NGridItem :span="18">
            <div class="permission-config-hint">
              <p style="color: #999; text-align: center; padding: 100px 0;">
                请从左侧选择菜单进行权限配置
              </p>
            </div>
          </NGridItem>
        </NGrid>
      </NCard>
    </NSpace>

    <!-- 权限配置对话框 -->
    <NModal
      v-model:show="showModal"
      preset="card"
      title="设置权限"
      style="width: 900px"
      :bordered="false"
      :segmented="{ content: true, footer: true }"
    >
      <NForm :model="formData" label-placement="left" label-width="100">
        <!-- 基本信息 -->
        <NFormItem label="菜单名称" required>
          <NInput v-model:value="formData.menuName" placeholder="请输入菜单名称" />
        </NFormItem>
        
        <NFormItem label="标识">
          <NInput v-model:value="formData.menuCode" placeholder="teacher_statics_menu" />
        </NFormItem>

        <NDivider />

        <!-- 功能权限 -->
        <NFormItem label="菜单">
          <div style="width: 100%">
            <div style="margin-bottom: 12px; color: #666">
              <span style="font-weight: 500">菜单：账号管理（系统管理）</span>
            </div>
            <div style="margin-bottom: 8px; color: #999">功能权限：</div>
            <NCheckboxGroup v-model:value="formData.functionPermissions">
              <NSpace>
                <NCheckbox
                  v-for="option in functionPermissionOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </NSpace>
            </NCheckboxGroup>
          </div>
        </NFormItem>

        <NDivider />

        <!-- 数据权限 -->
        <NFormItem label="数据权限">
          <div style="width: 100%">
            <div style="margin-bottom: 12px">
              <span style="font-weight: 500">权限范围：</span>
              <NRadioGroup v-model:value="formData.dataPermissionType">
                <NSpace>
                  <NRadio
                    v-for="option in dataPermissionTypeOptions"
                    :key="option.value"
                    :value="option.value"
                    :label="option.label"
                  />
                </NSpace>
              </NRadioGroup>
            </div>

            <NGrid :cols="2" :x-gap="16" style="margin-top: 16px">
              <!-- 用户组 -->
              <NGridItem>
                <div style="margin-bottom: 8px; font-weight: 500">用户组维度</div>
                <NCheckboxGroup v-model:value="formData.userGroups">
                  <NSpace vertical>
                    <NCheckbox
                      v-for="option in userGroupOptions"
                      :key="option.value"
                      :value="option.value"
                      :label="option.label"
                    />
                  </NSpace>
                </NCheckboxGroup>
              </NGridItem>

              <!-- 部门 -->
              <NGridItem>
                <div style="margin-bottom: 8px; font-weight: 500">部门维度</div>
                <div style="border: 1px solid #e0e0e6; border-radius: 4px; padding: 12px; max-height: 300px; overflow-y: auto">
                  <NTree
                    :data="departmentTreeData"
                    checkable
                    :checked-keys="selectedDepartments"
                    @update:checked-keys="(keys) => selectedDepartments = keys"
                    cascade
                    :default-expanded-keys="['tech', 'operation']"
                  />
                </div>
              </NGridItem>
            </NGrid>
          </div>
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleCancel">
            <template #icon>
              <CloseOutline />
            </template>
            取消
          </NButton>
          <NButton type="primary" @click="handleSave" :loading="loading">
            <template #icon>
              <SaveOutline />
            </template>
            确定
          </NButton>
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

.toolbar-card {
  background: #fff;
}

.content-card {
  background: #fff;
  min-height: 600px;
}

.menu-tree-container {
  border-right: 1px solid #e0e0e6;
  padding-right: 16px;
  min-height: 500px;
}

.permission-config-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 500px;
}

/* 自定义复选框样式 */
:deep(.n-checkbox) {
  margin-right: 8px;
}

/* 树形控件样式优化 */
:deep(.n-tree-node-content) {
  padding: 4px 0;
}

:deep(.n-tree-node-content__text) {
  font-size: 14px;
}
</style>
