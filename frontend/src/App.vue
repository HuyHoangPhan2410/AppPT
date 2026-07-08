<template>
  <div class="app-wrapper">
    <HeaderCom />
    <main class="main-content">
      <router-view />
    </main>
    <FooterCom v-if="authStore.currentUser" />
    <NotificationModal />
  </div>
</template>

<script setup>
import HeaderCom from './components/HeaderCom.vue'
import FooterCom from './components/FooterCom.vue'
import NotificationModal from './components/NotificationModal.vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
</script>

<script>
export default {
  name: 'App'
}
</script>

<style>
body {
  margin: 0;
  background-color: #f7f9fc;
}
#app {
  font-family: 'Inter', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.main-content {
  flex: 1;
}
</style>
