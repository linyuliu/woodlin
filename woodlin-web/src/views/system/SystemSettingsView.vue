<template>
  <div class="system-settings">
    <n-card title="系统设置" :bordered="false">
      <n-tabs type="line" animated>
        <!-- API加密配置 -->
        <n-tab-pane name="api-encryption" tab="API加密配置">
          <n-form
            ref="apiEncryptionFormRef"
            :model="apiEncryptionConfig"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
          >
            <n-divider title-placement="left">基础配置</n-divider>
            
            <n-form-item label="启用API加密" path="enabled">
              <n-switch v-model:value="apiEncryptionConfig.enabled" />
            </n-form-item>
            
            <n-form-item label="加密算法" path="algorithm">
              <n-select
                v-model:value="apiEncryptionConfig.algorithm"
                :options="algorithmOptions"
                placeholder="请选择加密算法"
              />
            </n-form-item>
            
            <template v-if="apiEncryptionConfig.algorithm === 'AES'">
              <n-divider title-placement="left">AES配置</n-divider>
              
              <n-form-item label="AES密钥" path="aesKey">
                <n-input
                  v-model:value="apiEncryptionConfig.aesKey"
                  type="textarea"
                  placeholder="Base64编码的AES密钥"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </n-form-item>
              
              <n-form-item label="AES向量(IV)" path="aesIv">
                <n-input
                  v-model:value="apiEncryptionConfig.aesIv"
                  type="textarea"
                  placeholder="Base64编码的初始化向量"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </n-form-item>
              
              <n-form-item label="加密模式" path="aesMode">
                <n-select
                  v-model:value="apiEncryptionConfig.aesMode"
                  :options="aesModeOptions"
                  placeholder="请选择加密模式"
                />
              </n-form-item>
              
              <n-form-item label="填充方式" path="aesPadding">
                <n-select
                  v-model:value="apiEncryptionConfig.aesPadding"
                  :options="aesPaddingOptions"
                  placeholder="请选择填充方式"
                />
              </n-form-item>
            </template>
            
            <template v-if="apiEncryptionConfig.algorithm === 'RSA'">
              <n-divider title-placement="left">RSA配置</n-divider>
              
              <n-form-item label="RSA公钥" path="rsaPublicKey">
                <n-input
                  v-model:value="apiEncryptionConfig.rsaPublicKey"
                  type="textarea"
                  placeholder="Base64编码的RSA公钥"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                />
              </n-form-item>
              
              <n-form-item label="RSA私钥" path="rsaPrivateKey">
                <n-input
                  v-model:value="apiEncryptionConfig.rsaPrivateKey"
                  type="textarea"
                  placeholder="Base64编码的RSA私钥"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                />
              </n-form-item>
              
              <n-form-item label="密钥长度" path="rsaKeySize">
                <n-select
                  v-model:value="apiEncryptionConfig.rsaKeySize"
                  :options="rsaKeySizeOptions"
                  placeholder="请选择密钥长度"
                />
              </n-form-item>
            </template>
            
            <template v-if="apiEncryptionConfig.algorithm === 'SM4'">
              <n-divider title-placement="left">SM4配置（国密标准）</n-divider>
              
              <n-form-item label="SM4密钥" path="sm4Key">
                <n-input
                  v-model:value="apiEncryptionConfig.sm4Key"
                  type="textarea"
                  placeholder="Base64编码的SM4密钥"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </n-form-item>
              
              <n-form-item label="SM4向量(IV)" path="sm4Iv">
                <n-input
                  v-model:value="apiEncryptionConfig.sm4Iv"
                  type="textarea"
                  placeholder="Base64编码的初始化向量"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </n-form-item>
              
              <n-form-item label="加密模式" path="sm4Mode">
                <n-select
                  v-model:value="apiEncryptionConfig.sm4Mode"
                  :options="sm4ModeOptions"
                  placeholder="请选择加密模式"
                />
              </n-form-item>
            </template>
            
            <n-divider title-placement="left">接口配置</n-divider>
            
            <n-form-item label="包含路径" path="includePatterns">
              <n-input
                v-model:value="apiEncryptionConfig.includePatterns"
                type="textarea"
                placeholder="需要加密的接口路径，多个用逗号分隔，如：/api/user/**,/api/payment/**"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </n-form-item>
            
            <n-form-item label="排除路径" path="excludePatterns">
              <n-input
                v-model:value="apiEncryptionConfig.excludePatterns"
                type="textarea"
                placeholder="排除加密的接口路径，多个用逗号分隔，如：/auth/login,/auth/logout"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </n-form-item>
            
            <n-form-item label="加密请求体" path="encryptRequest">
              <n-switch v-model:value="apiEncryptionConfig.encryptRequest" />
            </n-form-item>
            
            <n-form-item label="加密响应体" path="encryptResponse">
              <n-switch v-model:value="apiEncryptionConfig.encryptResponse" />
            </n-form-item>
            
            <n-form-item>
              <n-space>
                <n-button type="primary" @click="handleSaveApiEncryption" :loading="saving">
                  保存配置
                </n-button>
                <n-button @click="handleResetApiEncryption">重置</n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        
        <!-- 密码策略配置 -->
        <n-tab-pane name="password-policy" tab="密码策略配置">
          <n-form
            ref="passwordPolicyFormRef"
            :model="passwordPolicyConfig"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
          >
            <n-divider title-placement="left">基础配置</n-divider>
            
            <n-form-item label="启用密码策略" path="enabled">
              <n-switch v-model:value="passwordPolicyConfig.enabled" />
            </n-form-item>
            
            <n-form-item label="首次登录修改密码" path="requireChangeOnFirstLogin">
              <n-switch v-model:value="passwordPolicyConfig.requireChangeOnFirstLogin" />
            </n-form-item>
            
            <n-form-item label="密码过期天数" path="expireDays">
              <n-input-number
                v-model:value="passwordPolicyConfig.expireDays"
                :min="0"
                placeholder="0表示永不过期"
                style="width: 200px"
              />
              <span style="margin-left: 10px">天（0表示永不过期）</span>
            </n-form-item>
            
            <n-form-item label="提醒天数" path="warningDays">
              <n-input-number
                v-model:value="passwordPolicyConfig.warningDays"
                :min="1"
                placeholder="密码过期前提醒天数"
                style="width: 200px"
              />
              <span style="margin-left: 10px">天</span>
            </n-form-item>
            
            <n-divider title-placement="left">安全配置</n-divider>
            
            <n-form-item label="最大错误次数" path="maxErrorCount">
              <n-input-number
                v-model:value="passwordPolicyConfig.maxErrorCount"
                :min="1"
                placeholder="超过将锁定账号"
                style="width: 200px"
              />
              <span style="margin-left: 10px">次</span>
            </n-form-item>
            
            <n-form-item label="锁定时长" path="lockDurationMinutes">
              <n-input-number
                v-model:value="passwordPolicyConfig.lockDurationMinutes"
                :min="1"
                placeholder="账号锁定时长"
                style="width: 200px"
              />
              <span style="margin-left: 10px">分钟</span>
            </n-form-item>
            
            <n-divider title-placement="left">强密码策略</n-divider>
            
            <n-form-item label="启用强密码策略" path="strongPasswordRequired">
              <n-switch v-model:value="passwordPolicyConfig.strongPasswordRequired" />
            </n-form-item>
            
            <n-form-item label="最小密码长度" path="minLength">
              <n-input-number
                v-model:value="passwordPolicyConfig.minLength"
                :min="1"
                :max="20"
                style="width: 200px"
              />
            </n-form-item>
            
            <n-form-item label="最大密码长度" path="maxLength">
              <n-input-number
                v-model:value="passwordPolicyConfig.maxLength"
                :min="6"
                :max="50"
                style="width: 200px"
              />
            </n-form-item>
            
            <n-form-item label="要求包含数字" path="requireDigits">
              <n-switch v-model:value="passwordPolicyConfig.requireDigits" />
            </n-form-item>
            
            <n-form-item label="要求包含小写字母" path="requireLowercase">
              <n-switch v-model:value="passwordPolicyConfig.requireLowercase" />
            </n-form-item>
            
            <n-form-item label="要求包含大写字母" path="requireUppercase">
              <n-switch v-model:value="passwordPolicyConfig.requireUppercase" />
            </n-form-item>
            
            <n-form-item label="要求包含特殊字符" path="requireSpecialChars">
              <n-switch v-model:value="passwordPolicyConfig.requireSpecialChars" />
            </n-form-item>
            
            <n-form-item>
              <n-space>
                <n-button type="primary" @click="handleSavePasswordPolicy" :loading="saving">
                  保存配置
                </n-button>
                <n-button @click="handleResetPasswordPolicy">重置</n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        
        <!-- 活动监控配置 -->
        <n-tab-pane name="activity-monitoring" tab="活动监控配置">
          <n-form
            ref="activityMonitoringFormRef"
            :model="activityMonitoringConfig"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
          >
            <n-form-item label="启用活动监控" path="enabled">
              <n-switch v-model:value="activityMonitoringConfig.enabled" />
            </n-form-item>
            
            <n-form-item label="超时时间" path="timeoutSeconds">
              <n-input-number
                v-model:value="activityMonitoringConfig.timeoutSeconds"
                :min="-1"
                placeholder="-1表示不限制"
                style="width: 200px"
              />
              <span style="margin-left: 10px">秒（-1表示不限制）</span>
            </n-form-item>
            
            <n-form-item label="检查间隔" path="checkIntervalSeconds">
              <n-input-number
                v-model:value="activityMonitoringConfig.checkIntervalSeconds"
                :min="10"
                placeholder="监控检查间隔"
                style="width: 200px"
              />
              <span style="margin-left: 10px">秒</span>
            </n-form-item>
            
            <n-form-item label="监控API请求" path="monitorApiRequests">
              <n-switch v-model:value="activityMonitoringConfig.monitorApiRequests" />
            </n-form-item>
            
            <n-form-item label="监控用户交互" path="monitorUserInteractions">
              <n-switch v-model:value="activityMonitoringConfig.monitorUserInteractions" />
            </n-form-item>
            
            <n-form-item label="警告提前时间" path="warningBeforeTimeoutSeconds">
              <n-input-number
                v-model:value="activityMonitoringConfig.warningBeforeTimeoutSeconds"
                :min="60"
                placeholder="超时前提前警告时间"
                style="width: 200px"
              />
              <span style="margin-left: 10px">秒</span>
            </n-form-item>
            
            <n-form-item>
              <n-space>
                <n-button type="primary" @click="handleSaveActivityMonitoring" :loading="saving">
                  保存配置
                </n-button>
                <n-button @click="handleResetActivityMonitoring">重置</n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NCard, NTabs, NTabPane, NForm, NFormItem, NInput, NInputNumber, NSwitch, NSelect, NButton, NSpace, NDivider, useMessage } from 'naive-ui'
import { getConfigsByCategory, batchUpdateConfig, type ConfigUpdateDto } from '../../api/config'

const message = useMessage()

// 加载状态
const saving = ref(false)

// API加密配置
const apiEncryptionConfig = reactive({
  enabled: false,
  algorithm: 'AES',
  aesKey: '',
  aesIv: '',
  aesMode: 'CBC',
  aesPadding: 'PKCS5Padding',
  rsaPublicKey: '',
  rsaPrivateKey: '',
  rsaKeySize: '2048',
  sm4Key: '',
  sm4Iv: '',
  sm4Mode: 'CBC',
  includePatterns: '',
  excludePatterns: '',
  encryptRequest: true,
  encryptResponse: true
})

// 密码策略配置
const passwordPolicyConfig = reactive({
  enabled: true,
  requireChangeOnFirstLogin: false,
  expireDays: 0,
  warningDays: 7,
  maxErrorCount: 5,
  lockDurationMinutes: 30,
  strongPasswordRequired: false,
  minLength: 6,
  maxLength: 20,
  requireDigits: false,
  requireLowercase: false,
  requireUppercase: false,
  requireSpecialChars: false
})

// 活动监控配置
const activityMonitoringConfig = reactive({
  enabled: true,
  timeoutSeconds: 1800,
  checkIntervalSeconds: 60,
  monitorApiRequests: true,
  monitorUserInteractions: true,
  warningBeforeTimeoutSeconds: 300
})

// 加密算法选项
const algorithmOptions = [
  { label: 'AES（推荐用于大数据量）', value: 'AES' },
  { label: 'RSA（推荐用于敏感信息）', value: 'RSA' },
  { label: 'SM4（国密标准）', value: 'SM4' }
]

// AES模式选项
const aesModeOptions = [
  { label: 'CBC', value: 'CBC' },
  { label: 'ECB', value: 'ECB' },
  { label: 'CFB', value: 'CFB' },
  { label: 'OFB', value: 'OFB' },
  { label: 'CTR', value: 'CTR' }
]

// AES填充选项
const aesPaddingOptions = [
  { label: 'PKCS5Padding', value: 'PKCS5Padding' },
  { label: 'PKCS7Padding', value: 'PKCS7Padding' },
  { label: 'NoPadding', value: 'NoPadding' }
]

// RSA密钥长度选项
const rsaKeySizeOptions = [
  { label: '1024位', value: '1024' },
  { label: '2048位（推荐）', value: '2048' },
  { label: '4096位', value: '4096' }
]

// SM4模式选项
const sm4ModeOptions = [
  { label: 'CBC', value: 'CBC' },
  { label: 'ECB', value: 'ECB' }
]

// 加载API加密配置
async function loadApiEncryptionConfig() {
  try {
    const res: any = await getConfigsByCategory('api.encryption')
    if (res.code === 200 && res.data) {
      const configs = res.data
      apiEncryptionConfig.enabled = configs['api.encryption.enabled'] === 'true'
      apiEncryptionConfig.algorithm = configs['api.encryption.algorithm'] || 'AES'
      apiEncryptionConfig.aesKey = configs['api.encryption.aes-key'] || ''
      apiEncryptionConfig.aesIv = configs['api.encryption.aes-iv'] || ''
      apiEncryptionConfig.aesMode = configs['api.encryption.aes-mode'] || 'CBC'
      apiEncryptionConfig.aesPadding = configs['api.encryption.aes-padding'] || 'PKCS5Padding'
      apiEncryptionConfig.rsaPublicKey = configs['api.encryption.rsa-public-key'] || ''
      apiEncryptionConfig.rsaPrivateKey = configs['api.encryption.rsa-private-key'] || ''
      apiEncryptionConfig.rsaKeySize = configs['api.encryption.rsa-key-size'] || '2048'
      apiEncryptionConfig.sm4Key = configs['api.encryption.sm4-key'] || ''
      apiEncryptionConfig.sm4Iv = configs['api.encryption.sm4-iv'] || ''
      apiEncryptionConfig.sm4Mode = configs['api.encryption.sm4-mode'] || 'CBC'
      apiEncryptionConfig.includePatterns = configs['api.encryption.include-patterns'] || ''
      apiEncryptionConfig.excludePatterns = configs['api.encryption.exclude-patterns'] || ''
      apiEncryptionConfig.encryptRequest = configs['api.encryption.encrypt-request'] === 'true'
      apiEncryptionConfig.encryptResponse = configs['api.encryption.encrypt-response'] === 'true'
    }
  } catch (error) {
    console.error('加载API加密配置失败:', error)
  }
}

// 加载密码策略配置
async function loadPasswordPolicyConfig() {
  try {
    const res: any = await getConfigsByCategory('password.policy')
    if (res.code === 200 && res.data) {
      const configs = res.data
      passwordPolicyConfig.enabled = configs['password.policy.enabled'] === 'true'
      passwordPolicyConfig.requireChangeOnFirstLogin = configs['password.policy.require-change-on-first-login'] === 'true'
      passwordPolicyConfig.expireDays = parseInt(configs['password.policy.expire-days'] || '0')
      passwordPolicyConfig.warningDays = parseInt(configs['password.policy.warning-days'] || '7')
      passwordPolicyConfig.maxErrorCount = parseInt(configs['password.policy.max-error-count'] || '5')
      passwordPolicyConfig.lockDurationMinutes = parseInt(configs['password.policy.lock-duration-minutes'] || '30')
      passwordPolicyConfig.strongPasswordRequired = configs['password.policy.strong-password-required'] === 'true'
      passwordPolicyConfig.minLength = parseInt(configs['password.policy.min-length'] || '6')
      passwordPolicyConfig.maxLength = parseInt(configs['password.policy.max-length'] || '20')
      passwordPolicyConfig.requireDigits = configs['password.policy.require-digits'] === 'true'
      passwordPolicyConfig.requireLowercase = configs['password.policy.require-lowercase'] === 'true'
      passwordPolicyConfig.requireUppercase = configs['password.policy.require-uppercase'] === 'true'
      passwordPolicyConfig.requireSpecialChars = configs['password.policy.require-special-chars'] === 'true'
    }
  } catch (error) {
    console.error('加载密码策略配置失败:', error)
  }
}

// 加载活动监控配置
async function loadActivityMonitoringConfig() {
  try {
    const res: any = await getConfigsByCategory('activity.monitoring')
    if (res.code === 200 && res.data) {
      const configs = res.data
      activityMonitoringConfig.enabled = configs['activity.monitoring.enabled'] === 'true'
      activityMonitoringConfig.timeoutSeconds = parseInt(configs['activity.monitoring.timeout-seconds'] || '1800')
      activityMonitoringConfig.checkIntervalSeconds = parseInt(configs['activity.monitoring.check-interval-seconds'] || '60')
      activityMonitoringConfig.monitorApiRequests = configs['activity.monitoring.monitor-api-requests'] === 'true'
      activityMonitoringConfig.monitorUserInteractions = configs['activity.monitoring.monitor-user-interactions'] === 'true'
      activityMonitoringConfig.warningBeforeTimeoutSeconds = parseInt(configs['activity.monitoring.warning-before-timeout-seconds'] || '300')
    }
  } catch (error) {
    console.error('加载活动监控配置失败:', error)
  }
}

// 保存API加密配置
async function handleSaveApiEncryption() {
  saving.value = true
  try {
    const updateData: ConfigUpdateDto = {
      category: 'api.encryption',
      configs: {
        'api.encryption.enabled': String(apiEncryptionConfig.enabled),
        'api.encryption.algorithm': apiEncryptionConfig.algorithm,
        'api.encryption.aes-key': apiEncryptionConfig.aesKey,
        'api.encryption.aes-iv': apiEncryptionConfig.aesIv,
        'api.encryption.aes-mode': apiEncryptionConfig.aesMode,
        'api.encryption.aes-padding': apiEncryptionConfig.aesPadding,
        'api.encryption.rsa-public-key': apiEncryptionConfig.rsaPublicKey,
        'api.encryption.rsa-private-key': apiEncryptionConfig.rsaPrivateKey,
        'api.encryption.rsa-key-size': apiEncryptionConfig.rsaKeySize,
        'api.encryption.sm4-key': apiEncryptionConfig.sm4Key,
        'api.encryption.sm4-iv': apiEncryptionConfig.sm4Iv,
        'api.encryption.sm4-mode': apiEncryptionConfig.sm4Mode,
        'api.encryption.include-patterns': apiEncryptionConfig.includePatterns,
        'api.encryption.exclude-patterns': apiEncryptionConfig.excludePatterns,
        'api.encryption.encrypt-request': String(apiEncryptionConfig.encryptRequest),
        'api.encryption.encrypt-response': String(apiEncryptionConfig.encryptResponse)
      }
    }
    
    const res: any = await batchUpdateConfig(updateData)
    if (res.code === 200) {
      message.success('API加密配置保存成功')
    } else {
      message.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存API加密配置失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存密码策略配置
async function handleSavePasswordPolicy() {
  saving.value = true
  try {
    const updateData: ConfigUpdateDto = {
      category: 'password.policy',
      configs: {
        'password.policy.enabled': String(passwordPolicyConfig.enabled),
        'password.policy.require-change-on-first-login': String(passwordPolicyConfig.requireChangeOnFirstLogin),
        'password.policy.expire-days': String(passwordPolicyConfig.expireDays),
        'password.policy.warning-days': String(passwordPolicyConfig.warningDays),
        'password.policy.max-error-count': String(passwordPolicyConfig.maxErrorCount),
        'password.policy.lock-duration-minutes': String(passwordPolicyConfig.lockDurationMinutes),
        'password.policy.strong-password-required': String(passwordPolicyConfig.strongPasswordRequired),
        'password.policy.min-length': String(passwordPolicyConfig.minLength),
        'password.policy.max-length': String(passwordPolicyConfig.maxLength),
        'password.policy.require-digits': String(passwordPolicyConfig.requireDigits),
        'password.policy.require-lowercase': String(passwordPolicyConfig.requireLowercase),
        'password.policy.require-uppercase': String(passwordPolicyConfig.requireUppercase),
        'password.policy.require-special-chars': String(passwordPolicyConfig.requireSpecialChars)
      }
    }
    
    const res: any = await batchUpdateConfig(updateData)
    if (res.code === 200) {
      message.success('密码策略配置保存成功')
    } else {
      message.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存密码策略配置失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存活动监控配置
async function handleSaveActivityMonitoring() {
  saving.value = true
  try {
    const updateData: ConfigUpdateDto = {
      category: 'activity.monitoring',
      configs: {
        'activity.monitoring.enabled': String(activityMonitoringConfig.enabled),
        'activity.monitoring.timeout-seconds': String(activityMonitoringConfig.timeoutSeconds),
        'activity.monitoring.check-interval-seconds': String(activityMonitoringConfig.checkIntervalSeconds),
        'activity.monitoring.monitor-api-requests': String(activityMonitoringConfig.monitorApiRequests),
        'activity.monitoring.monitor-user-interactions': String(activityMonitoringConfig.monitorUserInteractions),
        'activity.monitoring.warning-before-timeout-seconds': String(activityMonitoringConfig.warningBeforeTimeoutSeconds)
      }
    }
    
    const res: any = await batchUpdateConfig(updateData)
    if (res.code === 200) {
      message.success('活动监控配置保存成功')
    } else {
      message.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存活动监控配置失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置API加密配置
function handleResetApiEncryption() {
  loadApiEncryptionConfig()
  message.info('已重置为当前保存的配置')
}

// 重置密码策略配置
function handleResetPasswordPolicy() {
  loadPasswordPolicyConfig()
  message.info('已重置为当前保存的配置')
}

// 重置活动监控配置
function handleResetActivityMonitoring() {
  loadActivityMonitoringConfig()
  message.info('已重置为当前保存的配置')
}

// 组件挂载时加载配置
onMounted(() => {
  loadApiEncryptionConfig()
  loadPasswordPolicyConfig()
  loadActivityMonitoringConfig()
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

:deep(.n-form-item-label) {
  font-weight: 500;
}
</style>
