<template>
  <header class="pt-header" v-if="authStore.currentUser">
    <div class="navbar">
      <div class="logo">💪 PT MANAGER</div>
      
      <!-- Lọc thanh Nav theo Quyền -->
      <nav class="nav-links" v-if="authStore.currentUser.role === 'pt'">
        <router-link to="/">Bảng Điều Khiển</router-link>
        <router-link to="/students">Học Viên</router-link>
        <router-link to="/schedule">Lịch Dạy</router-link>
      </nav>
      
      <nav class="nav-links" v-if="authStore.currentUser.role === 'admin'">
        <router-link to="/admin-dashboard">Tổng Quản Lý Trụ Sở</router-link>
      </nav>
      
      <div class="user-profile">
        <span class="avatar">{{ authStore.currentUser.role === 'admin' ? 'BOSS' : 'HLV' }}</span>
        <span>{{ authStore.currentUser.name }}</span>
        <button class="btn-logout" @click="handleLogout">Thoát</button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.pt-header { background: #b30404; box-shadow: 0 4px 12px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; }
.navbar { display: flex; align-items: center; justify-content: space-between; padding: 15px 50px; max-width: 1200px; margin: 0 auto; }
.logo { font-size: 24px; color: #fff; font-weight: 800; letter-spacing: 1px; }
.nav-links { display: flex; gap: 30px; }
.nav-links a { color: #ffeaea; text-decoration: none; font-weight: 600; font-size: 16px; padding: 8px 16px; border-radius: 8px; transition: all 0.3s ease; }
.nav-links a:hover { background: rgba(255, 255, 255, 0.15); color: #fff; }
.nav-links a.router-link-active { background: #fff; color: #b30404; }
.user-profile { display: flex; align-items: center; gap: 12px; color: #fff; font-weight: 600; }
.avatar { background: #fff; color: #b30404; padding: 8px; border-radius: 50%; font-size: 12px; }
.btn-logout { background: transparent; border: 1px solid white; color: white; padding: 5px 10px; border-radius: 6px; cursor: pointer; margin-left: 10px; font-weight: bold;}
.btn-logout:hover { background: white; color: #b30404; }

/* Mobile & Tablet responsive */
@media (max-width: 820px) {
  .navbar { padding: 12px 20px; }
  .logo { font-size: 20px; }
  .nav-links { gap: 15px; }
}
@media (max-width: 430px) {
  .navbar { flex-direction: column; gap: 10px; padding: 12px 10px; }
  .nav-links { gap: 8px; }
  .nav-links a { font-size: 13px; padding: 5px 10px; }
}
</style>
