import { defineStore } from 'pinia'

const API = 'http://localhost:8081/api/students'

export const useStudentStore = defineStore('students', {
  state: () => ({
    allStudents: [],
    loading: false
  }),
  getters: {
    students: (state) => state.allStudents
  },
  actions: {
    async fetchStudents() {
      this.loading = true
      try {
        const res = await fetch(API)
        this.allStudents = await res.json()
      } catch (e) {
        console.error('Lỗi tải học viên:', e)
      } finally {
        this.loading = false
      }
    },
    async addStudent(student) {
      try {
        const res = await fetch(API, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(student)
        })
        const created = await res.json()
        this.allStudents.push(created)
      } catch (e) {
        console.error('Lỗi thêm học viên:', e)
      }
    },
    async updateStudent(id, updatedData) {
      try {
        const res = await fetch(`${API}/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updatedData)
        })
        const updated = await res.json()
        const idx = this.allStudents.findIndex(s => s.id === id)
        if (idx !== -1) this.allStudents[idx] = updated
      } catch (e) {
        console.error('Lỗi cập nhật:', e)
      }
    },
    async deleteStudent(id) {
      try {
        await fetch(`${API}/${id}`, { method: 'DELETE' })
        this.allStudents = this.allStudents.filter(s => s.id !== id)
      } catch (e) {
        console.error('Lỗi xóa:', e)
      }
    },
    async getHistory(id) {
      try {
        const res = await fetch(`${API}/${id}/history`)
        return await res.json()
      } catch (e) {
        console.error('Lỗi lấy lịch sử:', e)
        return []
      }
    }
  }
})
