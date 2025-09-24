import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Naive UI
import { 
  create, 
  NMessageProvider, 
  NDialogProvider, 
  NConfigProvider, 
  NLoadingBarProvider
} from 'naive-ui'

import App from './App.vue'
import router from './router'

const naive = create({
  components: [NMessageProvider, NDialogProvider, NConfigProvider, NLoadingBarProvider]
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(naive)

app.mount('#app')

// 打印前端启动信息
const showStartupInfo = () => {
  const version = '1.0.0'
  const buildTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const nodeVersion = 'Unknown' // 在实际构建中会被替换
  
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
