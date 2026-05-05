<!--
  @file IconPicker/index.vue
  @description 图标选择器：支持搜索、弹出式网格选择，输出 vicons:antd:IconName 格式
  @author yulin
  @since 2026-05
-->
<script setup lang="ts">
import { computed, ref } from 'vue'
import { NInput, NPopover, NGrid, NGridItem, NIcon, NScrollbar, NEmpty } from 'naive-ui'
import * as AntdIcons from '@vicons/antd'

const props = withDefaults(
  defineProps<{
    /** 当前图标值，格式：vicons:antd:UserOutlined */
    modelValue?: string
    /** 弹出层宽度 */
    width?: number
  }>(),
  { modelValue: '', width: 420 }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const searchKeyword = ref('')
const popoverVisible = ref(false)

/** 常用图标列表 (130个) */
const iconNames = [
  'UserOutlined', 'TeamOutlined', 'UserAddOutlined', 'UserDeleteOutlined', 'IdcardOutlined',
  'SolutionOutlined', 'ContactsOutlined', 'SafetyOutlined', 'LockOutlined', 'UnlockOutlined',
  'KeyOutlined', 'SettingOutlined', 'ToolOutlined', 'ApiOutlined', 'HomeOutlined',
  'DashboardOutlined', 'AppstoreOutlined', 'FolderOutlined', 'FolderOpenOutlined', 'FileOutlined',
  'FileTextOutlined', 'FileAddOutlined', 'FileDoneOutlined', 'FileExcelOutlined', 'FilePdfOutlined',
  'FileImageOutlined', 'FileZipOutlined', 'FileMarkdownOutlined', 'TableOutlined', 'OrderedListOutlined',
  'UnorderedListOutlined', 'BarsOutlined', 'MenuOutlined', 'MenuFoldOutlined', 'MenuUnfoldOutlined',
  'MailOutlined', 'MessageOutlined', 'NotificationOutlined', 'BellOutlined', 'PhoneOutlined',
  'MobileOutlined', 'CalendarOutlined', 'ClockCircleOutlined', 'HistoryOutlined', 'SearchOutlined',
  'ZoomInOutlined', 'ZoomOutOutlined', 'PlusOutlined', 'MinusOutlined', 'EditOutlined',
  'DeleteOutlined', 'CopyOutlined', 'SaveOutlined', 'UploadOutlined', 'DownloadOutlined',
  'ImportOutlined', 'ExportOutlined', 'CheckOutlined', 'CloseOutlined', 'CheckCircleOutlined',
  'CloseCircleOutlined', 'InfoCircleOutlined', 'QuestionCircleOutlined', 'WarningOutlined', 'ExclamationCircleOutlined',
  'PlusCircleOutlined', 'MinusCircleOutlined', 'LeftOutlined', 'RightOutlined', 'UpOutlined',
  'DownOutlined', 'DoubleLeftOutlined', 'DoubleRightOutlined', 'CaretUpOutlined', 'CaretDownOutlined',
  'EyeOutlined', 'EyeInvisibleOutlined', 'PlayCircleOutlined', 'PauseCircleOutlined', 'StopOutlined',
  'ReloadOutlined', 'SyncOutlined', 'CloudUploadOutlined', 'CloudDownloadOutlined', 'CloudOutlined',
  'DatabaseOutlined', 'HddOutlined', 'GlobalOutlined', 'LinkOutlined', 'ShareAltOutlined',
  'HeartOutlined', 'StarOutlined', 'LikeOutlined', 'DislikeOutlined', 'BookOutlined',
  'ReadOutlined', 'FormOutlined', 'ProfileOutlined', 'AuditOutlined', 'BarChartOutlined',
  'LineChartOutlined', 'PieChartOutlined', 'AreaChartOutlined', 'FundOutlined', 'ShoppingCartOutlined',
  'ShopOutlined', 'GiftOutlined', 'TrophyOutlined', 'CrownOutlined', 'RocketOutlined',
  'BulbOutlined', 'ThunderboltOutlined', 'FireOutlined', 'SkinOutlined', 'TagOutlined',
  'TagsOutlined', 'PushpinOutlined', 'FlagOutlined', 'CompassOutlined', 'EnvironmentOutlined',
  'PictureOutlined', 'CameraOutlined', 'VideoCameraOutlined', 'SoundOutlined', 'BgColorsOutlined',
  'FormatPainterOutlined', 'PrinterOutlined', 'ScanOutlined', 'QrcodeOutlined', 'BarcodeOutlined',
]

const AntdIconMap = AntdIcons as Record<string, any>

const filteredIcons = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  if (!keyword) {return iconNames}
  return iconNames.filter((name) => name.toLowerCase().includes(keyword))
})

const selectedIconName = computed(() => {
  if (!props.modelValue) {return ''}
  const parts = props.modelValue.split(':')
  if (parts.length === 3 && parts[0] === 'vicons') {return parts[2]}
  return ''
})

function selectIcon(name: string): void {
  emit('update:modelValue', `vicons:antd:${name}`)
  popoverVisible.value = false
  searchKeyword.value = ''
}

function clearIcon(): void {
  emit('update:modelValue', '')
  popoverVisible.value = false
}

function renderIcon(name: string) {
  return AntdIconMap[name] || null
}
</script>

<template>
  <NPopover v-model:show="popoverVisible" trigger="click" placement="bottom-start" :width="width">
    <template #trigger>
      <div class="icon-picker-trigger" :class="{ 'has-icon': !!modelValue }">
        <NIcon v-if="modelValue" :size="18">
          <component :is="renderIcon(selectedIconName)" />
        </NIcon>
        <span v-else class="placeholder">选择图标</span>
        <span class="arrow">▼</span>
      </div>
    </template>
    <div class="icon-picker-content">
      <div class="search-bar">
        <NInput v-model:value="searchKeyword" placeholder="搜索图标..." clearable size="small" />
      </div>
      <NScrollbar style="max-height: 320px">
        <NGrid v-if="filteredIcons.length > 0" :cols="8" x-gap="8" y-gap="8">
          <NGridItem v-for="name in filteredIcons" :key="name">
            <div class="icon-item" :class="{ selected: selectedIconName === name }" :title="name" @click="selectIcon(name)">
              <NIcon :size="20">
                <component :is="renderIcon(name)" />
              </NIcon>
            </div>
          </NGridItem>
        </NGrid>
        <NEmpty v-else description="无匹配图标" size="small" style="padding: 20px 0" />
      </NScrollbar>
      <div class="footer-actions">
        <a class="clear-link" @click="clearIcon">清除</a>
      </div>
    </div>
  </NPopover>
</template>

<style scoped>
.icon-picker-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border: 1px solid #e0e0e6;
  border-radius: 4px;
  cursor: pointer;
  min-width: 120px;
  height: 34px;
  background: #fff;
  transition: all 0.2s;
}
.icon-picker-trigger:hover { border-color: #36ad6a; }
.icon-picker-trigger.has-icon { color: #333; }
.icon-picker-trigger .placeholder { color: #999; font-size: 14px; }
.icon-picker-trigger .arrow { margin-left: auto; font-size: 10px; color: #999; }
.icon-picker-content { padding: 8px; }
.search-bar { margin-bottom: 8px; }
.icon-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 40px;
  border: 1px solid #e0e0e6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.icon-item:hover { border-color: #36ad6a; color: #36ad6a; transform: scale(1.05); }
.icon-item.selected { border-color: #36ad6a; background: #f0f9ff; color: #36ad6a; }
.footer-actions { margin-top: 8px; padding-top: 8px; border-top: 1px solid #e0e0e6; text-align: center; }
.clear-link { color: #666; font-size: 13px; cursor: pointer; text-decoration: none; }
.clear-link:hover { color: #36ad6a; text-decoration: underline; }
</style>
