import { defineStore } from 'pinia'

const API = 'http://localhost:8081/api/schedules'

export function calculateEndTime(startTimeStr) {
  const [hours, minutes] = startTimeStr.split(':').map(Number)
  const endMins = hours * 60 + minutes + 90
  const h = Math.floor(endMins / 60)
  const m = endMins % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`
}

export const useScheduleStore = defineStore('schedules', {
  state: () => ({
    allSchedules: [],
    errorMsg: '',
    loading: false,
    stats: null
  }),
  getters: {
    schedules: (state) => state.allSchedules,
    sessionsThisWeek() { return this.stats?.sessionsWeek || 0 },
    uniqueClientsThisWeek() { return this.stats?.clientsWeek || 0 },
    revenueToday() { return this.stats?.revToday || 0 },
    revenueThisWeek() { return this.stats?.revWeek || 0 },
    revenueThisMonth() { return this.stats?.revMonth || 0 }
  },
  actions: {
    async fetchSchedules() {
      this.loading = true
      try {
        const res = await fetch(API)
        this.allSchedules = await res.json()
      } catch (e) {
        console.error('Lỗi tải lịch dạy:', e)
      } finally {
        this.loading = false
      }
    },
    async fetchStats() {
      try {
        const res = await fetch(`${API}/stats`)
        this.stats = await res.json()
      } catch (e) {
        console.error('Lỗi tải thống kê:', e)
      }
    },
    async addSchedule(schedule) {
      this.errorMsg = ''
      try {
        const res = await fetch(API, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            studentId: schedule.studentId,
            date: schedule.date,
            startTime: schedule.startTime,
            plannedExercises: schedule.plannedExercises || []
          })
        })
        if (!res.ok) {
          const err = await res.json()
          this.errorMsg = err.error || 'Lỗi tạo lịch'
          return false
        }
        const created = await res.json()
        this.allSchedules.push(created)
        return true
      } catch (e) {
        this.errorMsg = 'Không kết nối được server'
        return false
      }
    },
    async updateScheduleTime(id, newDate, newStartTime) {
      this.errorMsg = ''
      try {
        const res = await fetch(`${API}/${id}/reschedule`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ date: newDate, startTime: newStartTime })
        })
        if (!res.ok) {
          const err = await res.json()
          this.errorMsg = err.error || 'Lỗi sửa lịch'
          return false
        }
        const updated = await res.json()
        const idx = this.allSchedules.findIndex(s => s.id === id)
        if (idx !== -1) this.allSchedules[idx] = updated
        return true
      } catch (e) {
        this.errorMsg = 'Không kết nối được server'
        return false
      }
    },
    async cancelSchedule(id, reason) {
      try {
        const res = await fetch(`${API}/${id}/cancel`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ reason })
        })
        const updated = await res.json()
        const idx = this.allSchedules.findIndex(s => s.id === id)
        if (idx !== -1) this.allSchedules[idx] = updated
      } catch (e) {
        console.error('Lỗi hủy ca:', e)
      }
    },
    async completeSchedule(id, logData) {
      try {
        const res = await fetch(`${API}/${id}/complete`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            sessionNote: logData.sessionNote,
            workoutTypes: (logData.workoutTypes || []).join(','),
            exercises: logData.exercises || []
          })
        })
        const updated = await res.json()
        const idx = this.allSchedules.findIndex(s => s.id === id)
        if (idx !== -1) this.allSchedules[idx] = updated
      } catch (e) {
        console.error('Lỗi ghi log:', e)
      }
    },
    async deleteSchedule(id) {
      try {
        await fetch(`${API}/${id}/cancel`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ reason: 'Xóa bởi admin' })
        })
        const idx = this.allSchedules.findIndex(s => s.id === id)
        if (idx !== -1) this.allSchedules[idx].status = 'cancelled'
      } catch (e) {
        console.error('Lỗi xóa:', e)
      }
    },
    async fetchByStudent(studentId) {
      try {
        const res = await fetch(`${API}/student/${studentId}`)
        return await res.json()
      } catch (e) {
        console.error('Lỗi lấy lịch sử:', e)
        return []
      }
    },
    async markPaid(studentId) {
      try {
        const res = await fetch(`${API}/student/${studentId}/mark-paid`, {
          method: 'PUT'
        })
        if (res.ok) {
           await this.fetchSchedules() // reload to get new isPaid status
           return true
        }
        return false
      } catch (e) {
        console.error('Lỗi mark paid:', e)
        return false
      }
    }
  }
})
