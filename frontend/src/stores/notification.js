import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    message: '',
    type: 'success', // 'success' | 'error'
    show: false
  }),
  actions: {
    notify(msg, type = 'success') {
      this.message = msg;
      this.type = type;
      this.show = true;
    },
    close() {
      this.show = false;
    }
  }
})
