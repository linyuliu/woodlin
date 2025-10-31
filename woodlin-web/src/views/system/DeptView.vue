<script setup lang="ts">
/**
 * 部门管理视图
 * 
 * @author mumu
 * @description 部门管理页面，包含部门树形结构展示、添加、编辑、删除等功能
 * @since 2025-01-01
 */
import { ref, h } from 'vue'
import { 
  NCard, NButton, NTree, NSpace, NInput, NForm, NFormItem,
  NIcon, NPopconfirm, NEmpty,
  type TreeOption
} from 'naive-ui'
import { 
  AddOutline, 
  SearchOutline, 
  RefreshOutline,
  CreateOutline,
  TrashOutline,
  BusinessOutline
} from '@vicons/ionicons5'

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

/**
 * 部门树形数据
 */
const deptTreeData = ref<TreeOption[]>([
  {
    key: '1',
    label: 'Woodlin科技有限公司',
    children: [
      {
        key: '1-1',
        label: '技术部',
        children: [
          {
            key: '1-1-1',
            label: '前端组'
          },
          {
            key: '1-1-2',
            label: '后端组'
          },
          {
            key: '1-1-3',
            label: '测试组'
          }
        ]
      },
      {
        key: '1-2',
        label: '产品部',
        children: [
          {
            key: '1-2-1',
            label: '产品一组'
          },
          {
            key: '1-2-2',
            label: '产品二组'
          }
        ]
      },
      {
        key: '1-3',
        label: '市场部'
      },
      {
        key: '1-4',
        label: '人力资源部'
      }
    ]
  }
])

/**
 * 选中的部门
 */
const selectedKeys = ref<string[]>([])

/**
 * 搜索部门
 */
const handleSearch = () => {
  loading.value = true
  console.log('搜索部门:', searchForm.value)
  setTimeout(() => {
    loading.value = false
  }, 500)
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
  console.log('添加部门')
}

/**
 * 编辑部门
 */
const handleEdit = () => {
  if (selectedKeys.value.length === 0) {
    console.log('请先选择要编辑的部门')
    return
  }
  console.log('编辑部门:', selectedKeys.value[0])
}

/**
 * 删除部门
 */
const handleDelete = () => {
  if (selectedKeys.value.length === 0) {
    console.log('请先选择要删除的部门')
    return
  }
  console.log('删除部门:', selectedKeys.value[0])
}

/**
 * 树节点前缀
 */
const renderPrefix = () => {
  return h(NIcon, { size: 18, style: { marginRight: '8px' } }, {
    default: () => h(BusinessOutline)
  })
}
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
            v-if="deptTreeData.length > 0"
            block-line
            checkable
            selectable
            expand-on-click
            :data="deptTreeData"
            :default-expanded-keys="['1']"
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