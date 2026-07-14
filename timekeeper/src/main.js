/**
 * 应用入口文件
 *
 * 按顺序注册：
 * 1. Pinia — 全局状态管理
 * 2. Vue Router — 路由
 * 3. Element Plus — UI 组件库
 * 4. Element Plus Icons — 图标（全局注册，模板中直接使用）
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

// 挂载插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 全局注册所有 Element Plus 图标，模板中可直接 <el-icon><Timer /></el-icon>
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
