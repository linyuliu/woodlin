<!--
  @file views/monitor/server/index.vue
  @description 服务器监控：CPU/内存环形进度 + JVM/系统描述 + 磁盘条形进度
  @author yulin
  @since 2026-01-01
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NGi,
  NGrid,
  NProgress,
  NSpace,
  NSpin,
  useMessage,
} from 'naive-ui'
import { getServerInfo, type ServerInfo } from '@/api/monitor'

const message = useMessage()
const loading = ref(false)
const info = ref<ServerInfo>({})

async function refresh(): Promise<void> {
  loading.value = true
  try {
    info.value = (await getServerInfo()) ?? {}
  } catch {
    message.error('获取服务器信息失败')
  } finally {
    loading.value = false
  }
}

function pct(used?: number, total?: number): number {
  if (!used || !total || total <= 0) {return 0}
  return Math.round((used / total) * 10000) / 100
}

const cpuUsed = computed(() => {
  const cpu = info.value.cpu
  if (!cpu) {return 0}
  if (cpu.used !== undefined && cpu.used !== null) {return Math.round(cpu.used * 100) / 100}
  if (cpu.sys !== undefined && cpu.sys !== null && cpu.free !== undefined && cpu.free !== null) {
    return Math.round((100 - cpu.free) * 100) / 100
  }
  return 0
})

const memUsage = computed(() => {
  const mem = info.value.mem
  if (!mem) {return 0}
  if (mem.usage !== undefined && mem.usage !== null) {return Math.round(mem.usage * 100) / 100}
  return pct(mem.used, mem.total)
})

const jvmUsage = computed(() => {
  const jvm = info.value.jvm
  if (!jvm) {return 0}
  if (jvm.usage !== undefined && jvm.usage !== null) {return Math.round(jvm.usage * 100) / 100}
  return pct(jvm.used, jvm.total)
})

onMounted(() => {
  void refresh()
})
</script>

<template>
  <div class="page-server">
    <n-card size="small">
      <template #header>
        <n-space align="center">
          <span>服务器监控</span>
        </n-space>
      </template>
      <template #header-extra>
        <n-button type="primary" :loading="loading" @click="refresh">刷新</n-button>
      </template>

      <n-spin :show="loading">
        <n-grid :cols="3" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
          <n-gi span="3 m:1">
            <n-card title="CPU 使用率" size="small">
              <div class="ring">
                <n-progress
                  type="circle"
                  :percentage="cpuUsed"
                  :status="cpuUsed > 80 ? 'error' : 'success'"
                />
              </div>
              <n-descriptions
                v-if="info.cpu"
                label-placement="left"
                :column="1"
                size="small"
                style="margin-top: 12px"
              >
                <n-descriptions-item label="核心数">
                  {{ info.cpu.cpuNum ?? '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="用户使用率">
                  {{ info.cpu.used ?? '-' }}%
                </n-descriptions-item>
                <n-descriptions-item label="系统使用率">
                  {{ info.cpu.sys ?? '-' }}%
                </n-descriptions-item>
                <n-descriptions-item label="当前空闲率">
                  {{ info.cpu.free ?? '-' }}%
                </n-descriptions-item>
              </n-descriptions>
            </n-card>
          </n-gi>

          <n-gi span="3 m:1">
            <n-card title="内存使用率" size="small">
              <div class="ring">
                <n-progress
                  type="circle"
                  :percentage="memUsage"
                  :status="memUsage > 80 ? 'error' : 'success'"
                />
              </div>
              <n-descriptions
                v-if="info.mem"
                label-placement="left"
                :column="1"
                size="small"
                style="margin-top: 12px"
              >
                <n-descriptions-item label="总内存">
                  {{ info.mem.total ?? '-' }} GB
                </n-descriptions-item>
                <n-descriptions-item label="已用内存">
                  {{ info.mem.used ?? '-' }} GB
                </n-descriptions-item>
                <n-descriptions-item label="剩余内存">
                  {{ info.mem.free ?? '-' }} GB
                </n-descriptions-item>
              </n-descriptions>
            </n-card>
          </n-gi>

          <n-gi span="3 m:1">
            <n-card title="JVM 使用率" size="small">
              <div class="ring">
                <n-progress
                  type="circle"
                  :percentage="jvmUsage"
                  :status="jvmUsage > 80 ? 'error' : 'success'"
                />
              </div>
              <n-descriptions
                v-if="info.jvm"
                label-placement="left"
                :column="1"
                size="small"
                style="margin-top: 12px"
              >
                <n-descriptions-item label="总内存">
                  {{ info.jvm.total ?? '-' }} MB
                </n-descriptions-item>
                <n-descriptions-item label="最大内存">
                  {{ info.jvm.max ?? '-' }} MB
                </n-descriptions-item>
                <n-descriptions-item label="已用内存">
                  {{ info.jvm.used ?? '-' }} MB
                </n-descriptions-item>
                <n-descriptions-item label="JDK 版本">
                  {{ info.jvm.version ?? '-' }}
                </n-descriptions-item>
              </n-descriptions>
            </n-card>
          </n-gi>

          <n-gi span="3">
            <n-card title="服务器信息" size="small">
              <n-descriptions
                v-if="info.sys"
                label-placement="left"
                :column="2"
                size="small"
                bordered
              >
                <n-descriptions-item label="服务器名称">
                  {{ info.sys.computerName ?? '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="服务器 IP">
                  {{ info.sys.computerIp ?? '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="操作系统">
                  {{ info.sys.osName ?? '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="系统架构">
                  {{ info.sys.osArch ?? '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="项目路径" :span="2">
                  {{ info.sys.userDir ?? '-' }}
                </n-descriptions-item>
              </n-descriptions>
              <n-empty v-else description="暂无数据" />
            </n-card>
          </n-gi>

          <n-gi span="3">
            <n-card title="磁盘使用情况" size="small">
              <div v-if="info.disk && info.disk.length">
                <div v-for="(d, idx) in info.disk" :key="idx" class="disk-row">
                  <div class="disk-label">
                    <strong>{{ d.dirName }}</strong>
                    <span>
                      ({{ d.sysTypeName }} / {{ d.typeName }}) — {{ d.used }} / {{ d.total }}
                    </span>
                  </div>
                  <n-progress
                    type="line"
                    :percentage="d.usage ?? 0"
                    :status="(d.usage ?? 0) > 80 ? 'error' : 'success'"
                    indicator-placement="inside"
                    :height="18"
                  />
                </div>
              </div>
              <n-empty v-else description="暂无磁盘数据" />
            </n-card>
          </n-gi>
        </n-grid>
      </n-spin>
    </n-card>
  </div>
</template>

<style scoped>
.page-server {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.ring {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0;
}
.disk-row {
  margin-bottom: 16px;
}
.disk-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--n-text-color, #333);
}
</style>
