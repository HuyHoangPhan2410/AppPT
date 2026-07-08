import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../components/Dashboard.vue'
import StudentManager from '../components/StudentManager.vue'
import ScheduleManager from '../components/ScheduleManager.vue'
import Login from '../components/Login.vue'
import AdminDashboard from '../components/AdminDashboard.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/admin-dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { requiresAuth: true, role: 'admin' } },
  { path: '/', name: 'PtDashboard', component: Dashboard, meta: { requiresAuth: true, role: 'pt' } },
  { path: '/students', name: 'StudentManager', component: StudentManager, meta: { requiresAuth: true, role: 'pt' } },
  { path: '/schedule', name: 'ScheduleManager', component: ScheduleManager, meta: { requiresAuth: true, role: 'pt' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = !!authStore.currentUser
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.name === 'Login' && isAuthenticated) {
    if (authStore.currentUser.role === 'admin') next('/admin-dashboard')
    else next('/')
  } else if (to.meta.role && authStore.currentUser && to.meta.role !== authStore.currentUser.role) {
    // Ngăn Admin nhảy vào trang PT và ngược lại
    if (authStore.currentUser.role === 'admin') next('/admin-dashboard')
    else next('/')
  } else {
    next()
  }
})

export default router