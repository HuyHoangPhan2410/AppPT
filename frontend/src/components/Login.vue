<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="emoji-logo">💪🏋️‍♂️</div>
      <h1>Hệ Thống Quản Lý PT Manager</h1>
      <p class="subtitle">Đăng nhập tài khoản Nhân sự</p>
      
      <div v-if="error" class="error-msg">{{ error }}</div>
      
      <form @submit.prevent="handleLogin" class="pt-form">
        <input v-model="username" placeholder="Tên đăng nhập (admin/hoangphan)" required />
        <input type="password" v-model="password" placeholder="Mật khẩu (123)" required />
        <button type="submit" class="btn-primary" style="margin-top: 15px;">ĐĂNG NHẬP NGAY</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserLogin'
}
</script>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()
const authStore = useAuthStore()

function handleLogin() {
  const success = authStore.login(username.value.trim(), password.value.trim())
  if (success) {
     if (authStore.currentUser.role === 'admin') router.push('/admin-dashboard')
     else router.push('/')
  } else {
     error.value = 'Sai thông tin đăng nhập! (Thử: admin/123 hoặc hoangphan/123)'
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #7a0000 0%, #b30404 100%);
  padding: 20px;
}
.login-card {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
  text-align: center;
  width: 100%;
  max-width: 400px;
}
.emoji-logo {
  font-size: 70px;
  margin-bottom: 0px;
  line-height: 1;
}
h1 { color: #b30404; font-size: 24px; margin-bottom: 5px; }
.subtitle { color: #666; margin-bottom: 25px; }
.error-msg { background: #ffeeee; color: #b30404; padding: 10px; border-radius: 6px; margin-bottom: 15px; font-weight: bold;}
.pt-form { display: flex; flex-direction: column; gap: 15px; }
.pt-form input { padding: 12px; border-radius: 8px; border: 1px solid #ccc; font-size: 15px; }
.btn-primary { background: #b30404; color: white; border: none; padding: 15px; border-radius: 8px; font-size: 16px; font-weight: bold; cursor: pointer; transition: 0.3s; }
.btn-primary:hover { background: #900000; }
</style>
