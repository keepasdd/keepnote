import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router/index.js'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useUserStore } from './stores/user'
import { initTheme } from './utils/theme'
import 'element-plus/theme-chalk/dark/css-vars.css'

async function bootstrap() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(ElementPlus)
  app.use(router)

  const userStore = useUserStore()
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // 认证失效由请求拦截器统一处理，这里避免阻塞应用启动
    }
  }

  // initialize theme from local storage so CSS variables are set
  initTheme()

  app.mount('#app')
}

bootstrap()
