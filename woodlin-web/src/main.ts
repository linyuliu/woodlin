/**
 * Woodlin 前端应用入口文件
 * 
 * @author mumu
 * @description 应用的主要入口文件，负责初始化Vue应用、配置路由、状态管理和UI组件
 * @since 2025-01-01
 */

import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 引入 Naive UI 组件库
import { 
  create, 
  NMessageProvider, 
  NDialogProvider, 
  NConfigProvider, 
  NLoadingBarProvider
} from 'naive-ui'

import App from './App.vue'
import router from './router'

/**
 * 创建 Naive UI 实例
 * 注册应用级别的组件提供者，用于消息通知、对话框、配置和加载条功能
 */
const naive = create({
  components: [NMessageProvider, NDialogProvider, NConfigProvider, NLoadingBarProvider]
})

// 创建Vue应用实例
const app = createApp(App)

// 注册状态管理器
app.use(createPinia())
// 注册路由
app.use(router)
// 注册UI组件库
app.use(naive)

// 挂载应用到DOM
app.mount('#app')

/**
 * 显示应用启动信息
 * 在控制台输出Woodlin系统的ASCII艺术logo和版本信息
 */
const showStartupInfo = () => {
  // 应用版本信息
  const version = '1.0.0'
  const buildTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const nodeVersion = 'Unknown' // 在实际构建中会被替换为真实版本
  
  // ASCII艺术logo
  console.log(`
%c██╗    ██╗ ██████╗  ██████╗ ██████╗ ██╗     ██╗███╗   ██╗
██║    ██║██╔═══██╗██╔═══██╗██╔══██╗██║     ██║████╗  ██║
██║ █╗ ██║██║   ██║██║   ██║██║  ██║██║     ██║██╔██╗ ██║
██║███╗██║██║   ██║██║   ██║██║  ██║██║     ██║██║╚██╗██║
╚███╔███╔╝╚██████╔╝╚██████╔╝██████╔╝███████╗██║██║ ╚████║
 ╚══╝╚══╝  ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═╝  ╚═══╝

🌍 Woodlin 前端系统启动成功！
📦 版本: v${version}
🔧 构建时间: ${buildTime}
🚀 Node.js版本: ${nodeVersion}
🌐 环境: ${import.meta.env.MODE || 'development'}
⏰ 启动时间: ${new Date().toLocaleString()}
  `, 'color: #2d8cf0; font-weight: bold;')
}

// 显示启动信息
showStartupInfo()
