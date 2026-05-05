<!--
  @file layouts/components/TenantSwitch/index.vue
  @description 租户切换器：加载租户列表并写入 tenant store，供请求头透传租户上下文
  @author yulin
  @since 2026-05-05
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NSelect, useMessage, type SelectOption } from 'naive-ui'
import { getTenantPage, type SysTenant } from '@/api/tenant'
import { useTenantStore } from '@/stores/modules/tenant'

const tenantStore = useTenantStore()
const message = useMessage()
const options = ref<SelectOption[]>([])

const tenantValue = computed<string | null>({
  get: () => tenantStore.tenantId,
  set: (value) => {
    const target = options.value.find((item) => item.value === value)
    tenantStore.setTenant(value, typeof target?.label === 'string' ? target.label : null)
  },
})

async function loadTenants(): Promise<void> {
  try {
    const res = await getTenantPage({ page: 1, size: 100 })
    options.value = (res.records ?? []).map((item: SysTenant) => ({
      label: item.tenantName,
      value: String(item.tenantId ?? item.id ?? ''),
    })).filter((item) => item.value)
    if (!tenantStore.tenantId && options.value.length > 0) {
      const first = options.value[0]!
      tenantStore.setTenant(String(first.value), String(first.label))
    }
  } catch (error) {
    console.warn('[tenant-switch] 加载租户失败', error)
  }
}

function handleUpdate(value: string | null): void {
  tenantValue.value = value
  if (value) {
    message.success('租户上下文已切换')
  }
}

onMounted(() => {
  void loadTenants()
})
</script>

<template>
  <NSelect
    :value="tenantValue ?? undefined"
    :options="options"
    placeholder="选择租户"
    size="small"
    clearable
    filterable
    style="width: 160px"
    @update:value="handleUpdate"
  />
</template>
