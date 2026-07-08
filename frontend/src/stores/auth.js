import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    currentUser: null,
    users: [
      { id: 'admin1', username: 'admin', password: '123', name: 'Master Manager', role: 'admin' },
      { id: 'pt1', username: 'hoangphan', password: '123', name: 'Hoàng Phan', role: 'pt' },
      { id: 'pt2', username: 'ptnguyen', password: '123', name: 'Nguyễn Gym', role: 'pt' }
    ] // Hardcoded login mocks
  }),
  actions: {
    login(username, password) {
      const user = this.users.find(u => u.username === username && u.password === password)
      if (user) {
        this.currentUser = { id: user.id, username: user.username, name: user.name, role: user.role }
        return true
      }
      return false
    },
    logout() {
      this.currentUser = null
    }
  }
})
