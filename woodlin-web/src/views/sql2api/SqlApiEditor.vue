<template>
  <div class="sql-editor-container">
    <n-space vertical :size="16">
      <!-- Header -->
      <n-card title="SQL API 配置">
        <template #header-extra>
          <n-space>
            <n-button type="primary" @click="handleSave">
              <template #icon>
                <n-icon><SaveOutline /></n-icon>
              </template>
              保存
            </n-button>
            <n-button @click="handleTest">
              <template #icon>
                <n-icon><PlayOutline /></n-icon>
              </template>
              测试
            </n-button>
          </n-space>
        </template>

        <!-- API基本信息 -->
        <n-form ref="formRef" :model="apiConfig" :rules="rules" label-placement="left" label-width="120px">
          <n-grid :cols="2" :x-gap="24">
            <n-gi>
              <n-form-item label="API名称" path="apiName">
                <n-input v-model:value="apiConfig.apiName" placeholder="请输入API名称" />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="API路径" path="apiPath">
                <n-input v-model:value="apiConfig.apiPath" placeholder="/api/example" />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="请求方法" path="httpMethod">
                <n-select v-model:value="apiConfig.httpMethod" :options="httpMethods" />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="数据源" path="datasourceName">
                <n-select v-model:value="apiConfig.datasourceName" :options="datasources" />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="SQL类型" path="sqlType">
                <n-select v-model:value="apiConfig.sqlType" :options="sqlTypes" />
              </n-form-item>
            </n-gi>
            <n-gi>
              <n-form-item label="返回类型" path="resultType">
                <n-select v-model:value="apiConfig.resultType" :options="resultTypes" />
              </n-form-item>
            </n-gi>
          </n-grid>

          <!-- SQL编辑器 -->
          <n-form-item label="SQL语句" path="sqlContent">
            <div class="sql-editor-wrapper">
              <n-card>
                <n-space vertical>
                  <!-- 数据库树形结构 -->
                  <n-collapse>
                    <n-collapse-item title="数据库结构" name="1">
                      <n-tree
                        :data="databaseTree"
                        :node-props="nodeProps"
                        block-line
                        @update:selected-keys="handleTableSelect"
                      />
                    </n-collapse-item>
                  </n-collapse>

                  <!-- SQL输入框 -->
                  <n-input
                    v-model:value="apiConfig.sqlContent"
                    type="textarea"
                    :rows="12"
                    placeholder="输入SQL语句，支持动态参数:
                    
示例:
SELECT * FROM users 
WHERE status = #{status}
<if test='username != null'>
  AND username LIKE CONCAT('%', #{username}, '%')
</if>"
                  />

                  <!-- 参数提示 -->
                  <n-alert v-if="detectedParams.length > 0" type="info" closable>
                    <template #header>
                      检测到参数: {{ detectedParams.join(', ') }}
                    </template>
                    点击下方添加参数配置
                  </n-alert>
                </n-space>
              </n-card>
            </div>
          </n-form-item>

          <!-- 参数配置 -->
          <n-form-item label="参数配置">
            <n-space vertical style="width: 100%">
              <n-button @click="handleAddParam" type="dashed" block>
                <template #icon>
                  <n-icon><AddOutline /></n-icon>
                </template>
                添加参数
              </n-button>
              
              <n-card v-for="(param, index) in apiConfig.params" :key="index" size="small">
                <template #header>
                  参数 {{ index + 1 }}
                  <n-button text type="error" @click="handleRemoveParam(index)" style="float: right">
                    <template #icon>
                      <n-icon><TrashOutline /></n-icon>
                    </template>
                  </n-button>
                </template>
                
                <n-grid :cols="3" :x-gap="12">
                  <n-gi>
                    <n-input v-model:value="param.name" placeholder="参数名" />
                  </n-gi>
                  <n-gi>
                    <n-select v-model:value="param.type" :options="paramTypes" placeholder="类型" />
                  </n-gi>
                  <n-gi>
                    <n-checkbox v-model:checked="param.required">必填</n-checkbox>
                  </n-gi>
                  <n-gi :span="3">
                    <n-input v-model:value="param.description" placeholder="参数描述" />
                  </n-gi>
                  <n-gi :span="3">
                    <n-input v-model:value="param.validation" placeholder="验证规则(正则表达式)" />
                  </n-gi>
                </n-grid>
              </n-card>
            </n-space>
          </n-form-item>

          <!-- 高级配置 -->
          <n-collapse>
            <n-collapse-item title="高级配置" name="advanced">
              <n-grid :cols="2" :x-gap="24">
                <n-gi>
                  <n-form-item label="启用缓存">
                    <n-switch v-model:value="apiConfig.cacheEnabled" />
                  </n-form-item>
                </n-gi>
                <n-gi v-if="apiConfig.cacheEnabled">
                  <n-form-item label="缓存时间(秒)">
                    <n-input-number v-model:value="apiConfig.cacheExpire" :min="0" />
                  </n-form-item>
                </n-gi>
                <n-gi>
                  <n-form-item label="启用加密">
                    <n-switch v-model:value="apiConfig.encryptEnabled" />
                  </n-form-item>
                </n-gi>
                <n-gi v-if="apiConfig.encryptEnabled">
                  <n-form-item label="加密算法">
                    <n-select v-model:value="apiConfig.encryptAlgorithm" :options="encryptAlgorithms" />
                  </n-form-item>
                </n-gi>
                <n-gi>
                  <n-form-item label="需要认证">
                    <n-switch v-model:value="apiConfig.authRequired" />
                  </n-form-item>
                </n-gi>
                <n-gi v-if="apiConfig.authRequired">
                  <n-form-item label="认证类型">
                    <n-select v-model:value="apiConfig.authType" :options="authTypes" />
                  </n-form-item>
                </n-gi>
                <n-gi>
                  <n-form-item label="流控限制(QPS)">
                    <n-input-number v-model:value="apiConfig.flowLimit" :min="0" placeholder="0表示不限制" />
                  </n-form-item>
                </n-gi>
              </n-grid>
            </n-collapse-item>
          </n-collapse>

          <!-- API描述 -->
          <n-form-item label="API描述">
            <n-input
              v-model:value="apiConfig.apiDesc"
              type="textarea"
              :rows="3"
              placeholder="请输入API功能描述"
            />
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 测试结果 -->
      <n-card v-if="testResult" title="测试结果">
        <n-code :code="JSON.stringify(testResult, null, 2)" language="json" />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { SaveOutline, PlayOutline, AddOutline, TrashOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'

const message = useMessage()

interface ParamConfig {
  name: string
  type: string
  required: boolean
  defaultValue: string
  description: string
  validation?: string
}

/**
 * API配置
 */
const apiConfig = ref({
  apiName: '',
  apiPath: '',
  httpMethod: 'GET',
  datasourceName: 'master',
  sqlType: 'SELECT',
  sqlContent: '',
  params: [] as ParamConfig[],
  resultType: 'list',
  cacheEnabled: false,
  cacheExpire: 300,
  encryptEnabled: false,
  encryptAlgorithm: 'AES',
  authRequired: true,
  authType: 'TOKEN',
  flowLimit: 0,
  apiDesc: '',
  enabled: true
})

// 表单验证规则
const rules = {
  apiName: { required: true, message: '请输入API名称', trigger: 'blur' },
  apiPath: { required: true, message: '请输入API路径', trigger: 'blur' },
  sqlContent: { required: true, message: '请输入SQL语句', trigger: 'blur' }
}

// 下拉选项
const httpMethods = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' }
]

const sqlTypes = [
  { label: 'SELECT', value: 'SELECT' },
  { label: 'INSERT', value: 'INSERT' },
  { label: 'UPDATE', value: 'UPDATE' },
  { label: 'DELETE', value: 'DELETE' }
]

const resultTypes = [
  { label: '单条记录', value: 'single' },
  { label: '列表', value: 'list' },
  { label: '分页', value: 'page' }
]

const paramTypes = [
  { label: 'String', value: 'String' },
  { label: 'Integer', value: 'Integer' },
  { label: 'Long', value: 'Long' },
  { label: 'Double', value: 'Double' },
  { label: 'Boolean', value: 'Boolean' },
  { label: 'Date', value: 'Date' }
]

const encryptAlgorithms = [
  { label: 'AES', value: 'AES' },
  { label: 'RSA', value: 'RSA' },
  { label: 'SM4', value: 'SM4' }
]

const authTypes = [
  { label: 'Token', value: 'TOKEN' },
  { label: 'API Key', value: 'API_KEY' },
  { label: '无', value: 'NONE' }
]

const datasources = [
  { label: '主数据源', value: 'master' }
]

// 数据库树形结构（示例）
const databaseTree = ref([
  {
    key: 'tables',
    label: '表',
    children: [
      { key: 'sys_user', label: 'sys_user (系统用户)' },
      { key: 'sys_role', label: 'sys_role (系统角色)' },
      { key: 'sys_dept', label: 'sys_dept (部门)' }
    ]
  }
])

// 检测SQL中的参数
const detectedParams = computed(() => {
  const sql = apiConfig.value.sqlContent
  const paramPattern = /#\{([^}]+)\}|\$\{([^}]+)\}|:([a-zA-Z0-9_]+)/g
  const params = new Set<string>()
  
  let match
  while ((match = paramPattern.exec(sql)) !== null) {
    const param = match[1] || match[2] || match[3]
    if (param) {params.add(param)}
  }
  
  return Array.from(params)
})

// 处理表选择
const handleTableSelect = (keys: string[]) => {
  if (keys.length > 0) {
    const tableName = keys[0]
    if (tableName !== 'tables') {
      // 插入表名到SQL
      apiConfig.value.sqlContent += ` ${tableName}`
    }
  }
}

// 树节点属性
const nodeProps = () => ({
  onClick() {
    // 节点点击事件
  }
})

// 添加参数
/**
 * 添加参数
 */
const handleAddParam = () => {
  apiConfig.value.params.push({
    name: '',
    type: 'String',
    required: false,
    defaultValue: '',
    description: '',
    validation: ''
  })
}

/**
 * 删除参数
 */
const handleRemoveParam = (index: number) => {
  apiConfig.value.params.splice(index, 1)
}

/**
 * 保存配置
 */
const handleSave = () => {
  message.success('保存成功')
}

interface TestResult {
  code: number
  message: string
  data: unknown[]
}

/**
 * 测试结果
 */
const testResult = ref<TestResult | null>(null)

/**
 * 测试API
 */
const handleTest = () => {
  testResult.value = {
    code: 200,
    message: 'Success',
    data: []
  }
  message.info('正在测试...')
}
</script>

<style scoped>
.sql-editor-container {
  padding: 20px;
}

.sql-editor-wrapper {
  width: 100%;
}
</style>
