<template>
  <div class="notify-overlay" v-if="notificationStore.show" @click.self="notificationStore.close()">
    <div class="notify-box" :class="notificationStore.type">
       <h3 class="notify-title">
          <span v-if="notificationStore.type === 'success'">✅ Thành công!</span>
          <span v-else>❌ Lỗi hệ thống!</span>
       </h3>
       <p class="notify-msg">{{ notificationStore.message }}</p>
       <button class="notify-btn" @click="notificationStore.close()">OK</button>
    </div>
  </div>
</template>

<script setup>
import { useNotificationStore } from '../stores/notification'
const notificationStore = useNotificationStore()
</script>

<style scoped>
.notify-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4); z-index: 9999;
  display: flex; justify-content: center; align-items: flex-start;
  padding-top: 60px;
}
.notify-box {
  background: white; padding: 25px 30px; border-radius: 10px;
  width: 320px; text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  animation: slideDown 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.notify-box.success { border-top: 6px solid #4CAF50; }
.notify-box.error { border-top: 6px solid #F44336; }

.notify-title { margin-top: 0; color: #333; font-size: 18px; margin-bottom: 10px;}
.notify-msg { color: #555; margin-bottom: 25px; line-height: 1.5; font-size: 15px;}

.notify-btn {
  background: #333; color: white; border: none; padding: 10px 25px;
  border-radius: 6px; cursor: pointer; font-weight: bold; width: 100%;
  transition: opacity 0.2s;
}
.notify-btn:hover { opacity: 0.9; }

.notify-box.success .notify-btn { background: #4CAF50; }
.notify-box.error .notify-btn { background: #F44336; }

@keyframes slideDown {
  from { transform: translateY(-50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
