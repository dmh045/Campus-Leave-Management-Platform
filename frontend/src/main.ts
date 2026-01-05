// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia' // 👈 新增：引入 Pinia
import './style.css'
import App from './App.vue'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 如果你需要使用 Element Plus 图标库，可以取消下面注释并安装 @element-plus/icons-vue
// import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 2. 引入路由
import router from './router'

const app = createApp(App)
const pinia = createPinia() // 👈 新增：创建 Pinia 实例

// 3. 注册插件
app.use(pinia) // 👈 新增：注册 Pinia (必须在 router 之前或之后都可以，建议放前面)
app.use(ElementPlus)
app.use(router)

// 注册 Element Plus 所有图标 (可选，如果你项目中用了 <el-icon><Plus /></el-icon> 这种写法)
// for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
//   app.component(key, component)
// }

app.mount('#app')