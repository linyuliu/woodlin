<template>
  <div class="authorization-page">
    <n-tabs type="line" animated>
      <n-tab-pane name="decision" tab="决策测试器">
        <n-grid :cols="24" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item :span="24" :lg="14">
            <n-card title="决策测试器" :bordered="false">
              <n-form label-placement="top">
                <n-form-item label="Subject">
                  <n-input v-model:value="subjectJson" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" />
                </n-form-item>
                <n-form-item label="Action">
                  <n-input v-model:value="action" placeholder="system:user:add" />
                </n-form-item>
                <n-form-item label="Resource">
                  <n-input v-model:value="resourceJson" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" />
                </n-form-item>
                <n-form-item label="Context">
                  <n-input v-model:value="contextJson" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" />
                </n-form-item>
                <n-space>
                  <n-button type="primary" :loading="decisionLoading" @click="runDecision">
                    <template #icon>
                      <WIcon icon="vicons:antd:PlayCircleOutlined" />
                    </template>
                    执行决策
                  </n-button>
                  <n-button :loading="constraintsLoading" @click="runConstraints">
                    <template #icon>
                      <WIcon icon="vicons:antd:FilterOutlined" />
                    </template>
                    查询约束
                  </n-button>
                </n-space>
              </n-form>
            </n-card>
          </n-grid-item>

          <n-grid-item :span="24" :lg="10">
            <n-space vertical :size="16">
              <n-card title="Decision" :bordered="false">
                <n-result
                  v-if="decision"
                  :status="decision.effect === 'ALLOW' ? 'success' : 'error'"
                  :title="decision.effect"
                  :description="decision.reason"
                />
                <n-empty v-else description="暂无结果" />
                <n-code
                  v-if="decision"
                  class="authorization-code"
                  :code="formatJson(decision)"
                  language="json"
                  word-wrap
                />
              </n-card>

              <n-card title="Constraints" :bordered="false">
                <n-code
                  v-if="constraints"
                  class="authorization-code"
                  :code="formatJson(constraints)"
                  language="json"
                  word-wrap
                />
                <n-empty v-else description="暂无约束" />
              </n-card>
            </n-space>
          </n-grid-item>
        </n-grid>
      </n-tab-pane>

      <n-tab-pane name="policy" tab="策略 JSON">
        <n-card :bordered="false">
          <n-data-table :columns="policyColumns" :data="policies" :loading="loadingCatalog" :pagination="{ pageSize: 10 }" />
        </n-card>
      </n-tab-pane>

      <n-tab-pane name="capability" tab="Capability / Scope">
        <n-grid :cols="24" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item :span="24" :lg="12">
            <n-card title="Capability" :bordered="false">
              <n-data-table :columns="capabilityColumns" :data="capabilities" :loading="loadingCatalog" />
            </n-card>
          </n-grid-item>
          <n-grid-item :span="24" :lg="12">
            <n-card title="Scope" :bordered="false">
              <n-data-table :columns="scopeColumns" :data="scopes" :loading="loadingCatalog" />
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-tab-pane>

      <n-tab-pane name="quota" tab="Grant / Quota">
        <n-grid :cols="24" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item :span="24" :lg="12">
            <n-card title="Subject Grant" :bordered="false">
              <n-data-table :columns="grantColumns" :data="grants" :loading="loadingCatalog" />
            </n-card>
          </n-grid-item>
          <n-grid-item :span="24" :lg="12">
            <n-card title="Quota" :bordered="false">
              <n-data-table :columns="quotaColumns" :data="quotas" :loading="loadingCatalog" />
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import {
  getAuthorizationConstraints,
  getAuthorizationDecision,
  listAuthCapabilities,
  listAuthGrants,
  listAuthPolicies,
  listAuthQuotas,
  listAuthScopes,
  type AuthCapability,
  type AuthPolicy,
  type AuthQuotaPolicy,
  type AuthScope,
  type AuthSubjectGrant,
  type AuthorizationConstraint,
  type AuthorizationDecision,
  type AuthorizationRequest,
  type AuthorizationResource,
  type AuthorizationSubject,
  type AuthorizationContext,
} from '@/api/authorization'

const message = useMessage()

const subjectJson = ref(formatJson({ type: 'user', id: '1' }))
const action = ref('system:user:add')
const resourceJson = ref(formatJson({ type: 'permission', id: 'system:user:add', attributes: {} }))
const contextJson = ref(formatJson({ tenantId: '', deptId: undefined, attributes: {} }))
const decision = ref<AuthorizationDecision>()
const constraints = ref<AuthorizationConstraint>()
const decisionLoading = ref(false)
const constraintsLoading = ref(false)
const loadingCatalog = ref(false)
const policies = ref<AuthPolicy[]>([])
const capabilities = ref<AuthCapability[]>([])
const scopes = ref<AuthScope[]>([])
const grants = ref<AuthSubjectGrant[]>([])
const quotas = ref<AuthQuotaPolicy[]>([])

const policyColumns = [
  { title: '编码', key: 'policyCode', minWidth: 180 },
  { title: '名称', key: 'policyName', minWidth: 160 },
  { title: '类型', key: 'policyType', width: 110 },
  { title: 'Effect', key: 'effect', width: 100 },
  { title: '优先级', key: 'priority', width: 90 },
  { title: '启用', key: 'enabled', width: 80 },
]

const capabilityColumns = [
  { title: '编码', key: 'capabilityCode', minWidth: 180 },
  { title: '名称', key: 'capabilityName', minWidth: 160 },
  { title: '资源', key: 'resourceType', width: 100 },
  { title: '模式', key: 'resourcePattern', minWidth: 160 },
  { title: '启用', key: 'enabled', width: 80 },
]

const scopeColumns = [
  { title: '编码', key: 'scopeCode', minWidth: 180 },
  { title: '名称', key: 'scopeName', minWidth: 160 },
  { title: '能力ID', key: 'capabilityId', width: 120 },
  { title: 'Actions', key: 'actions', minWidth: 180 },
  { title: '启用', key: 'enabled', width: 80 },
]

const grantColumns = [
  { title: '主体', key: 'subjectType', width: 100 },
  { title: '主体ID', key: 'subjectId', minWidth: 120 },
  { title: '能力ID', key: 'capabilityId', width: 120 },
  { title: 'Scope ID', key: 'scopeId', width: 120 },
  { title: '状态', key: 'status', width: 80 },
]

const quotaColumns = [
  { title: '主体', key: 'subjectType', width: 100 },
  { title: '主体ID', key: 'subjectId', minWidth: 120 },
  { title: '窗口秒', key: 'windowSeconds', width: 100 },
  { title: '限制', key: 'limitCount', width: 100 },
  { title: '启用', key: 'enabled', width: 80 },
]

function parseJson<T>(value: string, field: string): T {
  try {
    return JSON.parse(value) as T
  } catch {
    throw new Error(`${field} JSON 格式不正确`)
  }
}

function buildRequest(): AuthorizationRequest {
  return {
    subject: parseJson<AuthorizationSubject>(subjectJson.value, 'Subject'),
    action: action.value,
    resource: parseJson<AuthorizationResource>(resourceJson.value, 'Resource'),
    context: parseJson<AuthorizationContext>(contextJson.value, 'Context'),
  }
}

function formatJson(value: unknown): string {
  return JSON.stringify(value, null, 2)
}

async function runDecision(): Promise<void> {
  decisionLoading.value = true
  try {
    decision.value = await getAuthorizationDecision(buildRequest())
  } catch (error) {
    message.error(error instanceof Error ? error.message : '执行决策失败')
  } finally {
    decisionLoading.value = false
  }
}

async function runConstraints(): Promise<void> {
  constraintsLoading.value = true
  try {
    constraints.value = await getAuthorizationConstraints(buildRequest())
  } catch (error) {
    message.error(error instanceof Error ? error.message : '查询约束失败')
  } finally {
    constraintsLoading.value = false
  }
}

async function loadCatalog(): Promise<void> {
  loadingCatalog.value = true
  try {
    const [policyData, capabilityData, scopeData, grantData, quotaData] = await Promise.all([
      listAuthPolicies(),
      listAuthCapabilities(),
      listAuthScopes(),
      listAuthGrants(),
      listAuthQuotas(),
    ])
    policies.value = policyData
    capabilities.value = capabilityData
    scopes.value = scopeData
    grants.value = grantData
    quotas.value = quotaData
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载授权配置失败')
  } finally {
    loadingCatalog.value = false
  }
}

onMounted(() => {
  void loadCatalog()
})
</script>

<style scoped>
.authorization-page {
  padding: 16px;
}

.authorization-code {
  display: block;
  margin-top: 12px;
  max-height: 420px;
  overflow: auto;
}
</style>
