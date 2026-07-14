/**
 * 路由配置
 *
 * 路由结构：
 * - / → 重定向到计时器（首页）
 * - /login, /register → 无布局（meta.noLayout = true）
 * - 其余页面 → MainLayout 包裹
 *
 * 所有页面使用懒加载（动态 import），按需加载减小首屏体积
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/timer'
  },
  // ---- 免布局页面 ----
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { noLayout: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { noLayout: true }
  },
  // ---- 主功能页面 ----
  {
    path: '/timer',
    name: 'Timer',
    component: () => import('@/views/TimerView.vue')
  },
  {
    path: '/records',
    name: 'Records',
    component: () => import('@/views/RecordsView.vue')
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/StatisticsView.vue')
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: () => import('@/views/CalendarView.vue')
  },
  {
    path: '/goals',
    name: 'Goals',
    component: () => import('@/views/GoalsView.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫：登录状态检查
 *
 * 白名单路由（/login, /register）直接放行
 * 其他路由需要登录，未登录则重定向到 /login
 *
 * 检查当前用户是否存在于 localStorage（auth store 初始化时会从 localStorage 恢复）
 */
const PUBLIC_ROUTES = ['/login', '/register']

router.beforeEach((to, from, next) => {
  // 白名单路由放行
  if (PUBLIC_ROUTES.includes(to.path)) {
    return next()
  }

  // 检查 JWT token（auth store 登录后存储）
  const token = localStorage.getItem('timekeeper_token')
  if (token) {
    return next()
  }

  // 未登录，跳转登录页
  next('/login')
})

export default router
