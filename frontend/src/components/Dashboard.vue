<template>
  <div class="dashboard-container">
    <h1 class="page-title">Bảng Điều Khiển PT</h1>

    <!-- TÍNH NĂNG MỚI: LỊCH DẠY TUẦN NÀY -->
    <h2 class="section-title">📅 Thời khóa biểu Tuần này</h2>
    <div class="weekly-calendar">
      <div v-for="day in weekDays" :key="day.dateStr" class="calendar-day">
        <div class="day-header" :class="{ 'is-today': day.dateStr === todayIso }">
          <span class="day-name">{{ day.label }}</span>
          <span class="day-date">{{ day.dateNum }}</span>
        </div>
        <div class="day-content">
           <template v-if="day.schedules.length > 0">
             <div v-for="sch in day.schedules" :key="sch.id" class="calendar-card" :class="sch.status">
                <div class="sch-time">{{ sch.startTime }} - {{ sch.endTime }}</div>
                <div class="sch-student">Học viên: <strong>{{ getStudentName(sch.student?.id || sch.studentId) }}</strong></div>
                <div class="sch-status" v-if="sch.status === 'cancelled'">✖ Bị hủy</div>
                <div class="sch-status" v-if="sch.status === 'completed'" style="color:#2e7d32">✔ Hoàn thành</div>
             </div>
           </template>
           <div v-else class="empty-day">Trống lịch</div>
        </div>
      </div>
    </div>
    
    <h2 class="section-title" style="margin-top: 40px;">📊 Tổng quan Khách & Ca dạy</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-info">
          <h3>Số ca dạy tuần này</h3>
          <p class="stat-number">{{ scheduleStore.sessionsThisWeek }}</p>
        </div>
      </div>
      <div class="stat-card outline-red">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <h3>Khách hàng phục vụ/tuần</h3>
          <p class="stat-number">{{ scheduleStore.uniqueClientsThisWeek }}</p>
        </div>
      </div>
    </div>

    <h2 class="section-title" style="margin-top: 50px;">💵 Báo cáo Doanh thu (300,000đ / Ca hợp lệ)</h2>
    <div class="stats-grid">
      <div class="stat-card outline-red">
        <div class="stat-icon dollar-icon">💵</div>
        <div class="stat-info">
          <h3>Hôm nay</h3>
          <p class="stat-number">{{ scheduleStore.revenueToday.toLocaleString('vi-VN') }}đ</p>
        </div>
      </div>
      <div class="stat-card outline-red">
        <div class="stat-icon dollar-icon">💶</div>
        <div class="stat-info">
          <h3>Tuần này</h3>
          <p class="stat-number">{{ scheduleStore.revenueThisWeek.toLocaleString('vi-VN') }}đ</p>
        </div>
      </div>
      <div class="stat-card outline-red">
        <div class="stat-icon dollar-icon">💎</div>
        <div class="stat-info">
          <h3>Tháng này</h3>
          <p class="stat-number">{{ scheduleStore.revenueThisMonth.toLocaleString('vi-VN') }}đ</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PtDashboard'
}
</script>

<script setup>
import { computed, onMounted } from 'vue'
import { useScheduleStore } from '../stores/schedules'
import { useStudentStore } from '../stores/students'

const scheduleStore = useScheduleStore()
const studentStore = useStudentStore()

onMounted(async () => {
  await studentStore.fetchStudents()
  await scheduleStore.fetchSchedules()
  await scheduleStore.fetchStats()
})

function getStudentName(id) {
  const stu = studentStore.students.find(s => s.id === id)
  return stu ? stu.name : 'Unknown'
}

// Tính ngày hôm nay format chuẩn theo timezone hiện tại
const todayIso = computed(() => {
  const t = new Date();
  return `${t.getFullYear()}-${(t.getMonth()+1).toString().padStart(2,'0')}-${t.getDate().toString().padStart(2,'0')}`;
})

// Tính toán 7 ngày trong tuần
const weekDays = computed(() => {
   const today = new Date()
   const distanceToMonday = today.getDay() === 0 ? 6 : today.getDay() - 1
   const monday = new Date(today)
   monday.setDate(today.getDate() - distanceToMonday)
   monday.setHours(0,0,0,0)

   const days = []
   for (let i = 0; i < 7; i++) {
      const d = new Date(monday)
      d.setDate(monday.getDate() + i)
      
      const dateStr = `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')}`
      const label = i === 6 ? 'Chủ Nhật' : `Thứ ${i+2}`
      const dateNum = `${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}`
      
      // Lấy lịch của ngày đó và sort theo thời gian tăng dần
      const daySchs = scheduleStore.schedules
        .filter(s => s.date === dateStr)
        .sort((a,b) => a.startTime.localeCompare(b.startTime))
      
      days.push({
         dateStr,
         label,
         dateNum,
         schedules: daySchs
      })
   }
   return days
})
</script>

<style scoped>
.dashboard-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-title {
  color: #b30404;
  text-align: left;
  margin-bottom: 20px;
  border-bottom: 3px solid #b30404;
  padding-bottom: 10px;
}
.section-title {
  color: #333;
  font-size: 20px;
  margin-bottom: 20px;
  text-align: left;
}

/* LỊCH TUẦN NÀY */
.weekly-calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.calendar-day {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 250px;
}
.day-header {
  background: #f9f9f9;
  padding: 12px 10px;
  text-align: center;
  border-bottom: 1px solid #eaeaea;
}
.day-header.is-today {
  background: #b30404;
  color: white;
}
.day-name { display: block; font-weight: bold; font-size: 15px;}
.day-date { display: block; font-size: 13px; opacity: 0.9;}

.day-content {
  padding: 10px;
  flex: 1;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.empty-day {
  color: #bbb;
  text-align: center;
  font-size: 14px;
  margin-top: 25px;
  font-style: italic;
}
.calendar-card {
  background: #fff;
  border-left: 4px solid #1976d2; /* Pending = blue */
  padding: 8px 10px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 13px;
  text-align: left;
}
.calendar-card.completed { border-left-color: #388e3c; } 
.calendar-card.cancelled { border-left-color: #d32f2f; background: #fff5f5; opacity: 0.8;}

.sch-time { font-weight: 700; color: #444; font-size: 12px;}
.sch-student { color: #222; margin-top: 4px; font-size: 13px;}
.sch-status { font-style: italic; font-size: 12px; margin-top: 4px;}

/* STATS */
.stats-grid {
  display: flex;
  gap: 30px;
  justify-content: flex-start;
  flex-wrap: wrap;
}
.stat-card {
  background: #fff;
  border-left: 6px solid #b30404;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  width: 300px;
}
.stat-icon {
  font-size: 40px;
  background: #ffeeee;
  color: #b30404;
  padding: 10px 15px;
  border-radius: 12px;
}
.dollar-icon {
  background: #e8f5e9;
  color: #2e7d32;
}
.stat-info h3 {
  color: #666;
  margin: 0 0 5px 0;
  font-size: 16px;
}
.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #b30404;
  margin: 0;
}

/* Tinh chỉnh Reponsive cho Mobile/Tablet */
@media (max-width: 820px) {
  .dashboard-container { padding: 20px; }
  .page-title { font-size: 24px; }
  .section-title { font-size: 18px; margin-top: 30px !important; }
  .weekly-calendar { display: flex; overflow-x: auto; padding-bottom: 15px; -webkit-overflow-scrolling: touch; }
  .calendar-day { flex: 0 0 calc(40% - 10px); min-width: 160px; min-height: 200px; }
  .stats-grid { gap: 15px; }
  .stat-card { width: calc(50% - 8px); padding: 15px; gap: 10px; box-sizing: border-box; }
  .stat-icon { font-size: 30px; padding: 10px; }
  .stat-number { font-size: 24px; }
}
@media (max-width: 430px) {
  .dashboard-container { padding: 12px; }
  .page-title { font-size: 20px; margin-bottom: 15px; }
  .section-title { font-size: 16px; margin-bottom: 10px; margin-top: 20px !important; }
  .weekly-calendar { gap: 8px; }
  .calendar-day { flex: 0 0 70%; min-width: 130px; }
  .sch-time { font-size: 11px; }
  .sch-student { font-size: 12px; }
  .stat-card { width: 100%; padding: 15px; }
  .stat-icon { font-size: 26px; padding: 8px; }
  .stat-info h3 { font-size: 14px; }
  .stat-number { font-size: 22px; }
}
</style>
