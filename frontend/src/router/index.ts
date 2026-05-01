import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Home from '@/views/Home.vue'
import About from '@/views/About.vue'
import LoginPage from '@/views/login/LoginPage.vue'
import { tokenStorage } from '@/utils/request'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: LoginPage, meta: { public: true, hideLayout: true } },
  { path: '/', name: 'Home', component: Home },
  { path: '/about', name: 'About', component: About }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：未登录跳转到 /login
router.beforeEach((to, _from, next) => {
  const isPublic = to.meta?.public === true
  const hasToken = !!tokenStorage.get()
  if (!isPublic && !hasToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && hasToken) {
    next('/')
  } else {
    next()
  }
})

export default router
