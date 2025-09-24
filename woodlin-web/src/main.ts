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
