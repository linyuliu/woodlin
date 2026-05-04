/**
 * @file main.ts
 * @description 应用入口：装配 Pinia / Router / I18n / Directives 与全局样式
 * @author yulin
 * @since 2026-05-04
 */
import { createApp } from 'vue'
import App from './App.vue'
import { setupStore } from './stores'
import { setupRouter } from './router'
import { setupI18n } from './locales'
import { setupDirectives } from './directives'
import './styles/global.css'

const app = createApp(App)
setupStore(app)
const router = setupRouter(app)
setupI18n(app)
setupDirectives(app)
void import('./router/guard').then(({ setupGuard }) => setupGuard(router))
app.mount('#app')
