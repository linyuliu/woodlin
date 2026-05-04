<!--
  @file MenuFormDrawer.vue
  @description 菜单新增/编辑抽屉表单，支持图标选择、条件显示字段
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { reactive, ref, computed, type Ref } from 'vue'
import {
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NRadioGroup,
  NRadioButton,
  NTreeSelect,
  NSwitch,
  NButton,
  NSpace,
  useMessage,
  type FormInst,
  type FormRules,
  type TreeSelectOption,
} from 'naive-ui'
import { createMenu, updateMenu, getMenuTree } from '@/api/system/menu'
import type { RouteItem } from '@/types/global'
import IconPicker from '@/components/IconPicker/index.vue'

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive<Partial<RouteItem>>({
  id: undefined,
  parentId: 0,
  type: 2,
  title: '',
  name: '',
  path: '',
  component: '',
  icon: '',
  sort: 0,
  permission: '',
  isFrame: false,
  isCache: false,
  isHidden: false,
  showInTabs: true,
  redirect: '',
})

const menuTreeOptions: Ref<TreeSelectOption[]> = ref([])

const rules: FormRules = {
  title: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', type: 'number' }],
  name: [{ required: true, message: '请输入路由名称', trigger: 'blur' }],
}

/** 是否显示路由字段 (目录和菜单) */
const showRouteFields = computed(() => formData.type === 1 || formData.type === 2)

/** 是否显示组件字段 (仅菜单) */
const showComponentField = computed(() => formData.type === 2)

/** 是否显示权限字段 (菜单和按钮) */
const showPermissionField = computed(() => formData.type === 2 || formData.type === 3)

/**
 * 打开抽屉
 * @param record - 编辑时的菜单对象
 * @param parentId - 新增子菜单时的父ID
 */
function open(record?: RouteItem, parentId?: number): void {
  visible.value = true
  isEdit.value = !!record
  if (record) {
    Object.assign(formData, record)
  } else {
    resetForm()
    if (parentId !== undefined) {
      formData.parentId = parentId
    }
  }
  loadMenuTree()
}

/** 重置表单 */
function resetForm(): void {
  Object.assign(formData, {
    id: undefined,
    parentId: 0,
    type: 2,
    title: '',
    name: '',
    path: '',
    component: '',
    icon: '',
    sort: 0,
    permission: '',
    isFrame: false,
    isCache: false,
    isHidden: false,
    showInTabs: true,
    redirect: '',
  })
  formRef.value?.restoreValidation()
}

/** 加载菜单树 */
async function loadMenuTree(): Promise<void> {
  try {
    const data = await getMenuTree()
    menuTreeOptions.value = [
      { key: 0, label: '顶级菜单', value: 0 },
      ...transformMenuTree(data),
    ]
  } catch (error: any) {
    message.error(error?.message || '加载菜单树失败')
  }
}

/** 转换菜单树 */
function transformMenuTree(list: RouteItem[]): TreeSelectOption[] {
  return list.map((item) => ({
    key: item.id,
    label: item.title,
    value: item.id,
    children: item.children ? transformMenuTree(item.children) : undefined,
  }))
}

/** 提交 */
async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await updateMenu(formData.id!, formData)
      message.success('修改成功')
    } else {
      await createMenu(formData)
      message.success('新增成功')
    }
    visible.value = false
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

/** 取消 */
function handleCancel(): void {
  visible.value = false
}

defineExpose({ open })
</script>

<template>
  <NDrawer v-model:show="visible" :width="600" placement="right">
    <NDrawerContent :title="isEdit ? '编辑菜单' : '新增菜单'" closable>
      <NForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="90"
        require-mark-placement="left"
      >
        <NFormItem label="上级菜单" path="parentId">
          <NTreeSelect
            v-model:value="formData.parentId"
            :options="menuTreeOptions"
            placeholder="请选择上级菜单"
            clearable
            filterable
          />
        </NFormItem>

        <NFormItem label="菜单类型" path="type">
          <NRadioGroup v-model:value="formData.type">
            <NRadioButton :value="1">目录</NRadioButton>
            <NRadioButton :value="2">菜单</NRadioButton>
            <NRadioButton :value="3">按钮</NRadioButton>
          </NRadioGroup>
        </NFormItem>

        <NFormItem label="菜单名称" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入菜单名称" clearable />
        </NFormItem>

        <NFormItem label="路由名称" path="name">
          <NInput v-model:value="formData.name" placeholder="如: SystemUser" clearable />
        </NFormItem>

        <NFormItem label="菜单图标" path="icon">
          <IconPicker v-model="formData.icon" :width="480" />
        </NFormItem>

        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" style="width: 100%" />
        </NFormItem>

        <NFormItem v-if="showRouteFields" label="路由地址" path="path">
          <NInput v-model:value="formData.path" placeholder="如: /system/user" clearable />
        </NFormItem>

        <NFormItem v-if="showComponentField" label="组件路径" path="component">
          <NInput
            v-model:value="formData.component"
            placeholder="如: system/user/index"
            clearable
          />
        </NFormItem>

        <NFormItem v-if="showPermissionField" label="权限标识" path="permission">
          <NInput
            v-model:value="formData.permission"
            placeholder="如: system:user:list"
            clearable
          />
        </NFormItem>

        <NFormItem v-if="showRouteFields" label="外链" path="isFrame">
          <NSwitch v-model:value="formData.isFrame">
            <template #checked>是</template>
            <template #unchecked>否</template>
          </NSwitch>
        </NFormItem>

        <NFormItem v-if="showRouteFields" label="缓存" path="isCache">
          <NSwitch v-model:value="formData.isCache">
            <template #checked>是</template>
            <template #unchecked>否</template>
          </NSwitch>
        </NFormItem>

        <NFormItem label="显示" path="isHidden">
          <NSwitch v-model:value="formData.isHidden">
            <template #checked>隐藏</template>
            <template #unchecked>显示</template>
          </NSwitch>
        </NFormItem>

        <NFormItem v-if="showRouteFields" label="标签页" path="showInTabs">
          <NSwitch v-model:value="formData.showInTabs">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </NSwitch>
        </NFormItem>

        <NFormItem v-if="formData.type === 1" label="重定向" path="redirect">
          <NInput v-model:value="formData.redirect" placeholder="默认跳转路由" clearable />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleCancel">取消</NButton>
          <NButton type="primary" :loading="loading" @click="handleSubmit">确定</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
