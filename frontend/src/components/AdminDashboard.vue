<template>
  <div class="dashboard-container">
    <div class="header-action">
      <h1 class="page-title" style="border: none; margin: 0; padding: 0;">Trang Tổng Quản Lý</h1>
      <div style="display: flex; gap: 10px;">
        <button class="btn-primary" style="background:#1976d2" @click="sendMailReminder">📧 Gửi Mail Nhắc</button>
        <button class="btn-primary" style="background:#2e7d32" @click="exportExcel">📊 Xuất Excel</button>
        <button class="btn-primary" @click="exportPDF">📥 Xuất PDF Tuần</button>
      </div>
    </div>

    <h2 class="section-title">📊 Doanh thu Khối Hệ Thống (300,000đ / Ca)</h2>
    <div class="stats-grid">
      <div class="stat-card outline-red">
        <div class="stat-icon dollar-icon">💶</div>
        <div class="stat-info">
          <h3>Tổng Doanh Thu Tuần này</h3>
          <p class="stat-number">{{ scheduleStore.revenueThisWeek.toLocaleString('vi-VN') }}đ</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-info">
          <h3>Tổng Số ca thực dạy (Tuần)</h3>
          <p class="stat-number">{{ scheduleStore.sessionsThisWeek }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏃‍♂️</div>
        <div class="stat-info">
          <h3>Số Khách Active (Toàn phòng Gym)</h3>
          <p class="stat-number">{{ scheduleStore.uniqueClientsThisWeek }}</p>
        </div>
      </div>
    </div>

    <h2 class="section-title" style="margin-top: 40px;">📅 Master Weekly Schedule (Tất cả PT)</h2>
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
                <div class="sch-student">
                  PT: <strong>{{ getPtName(sch.ptId) }}</strong><br/>
                  Khách: {{ getStudentName(sch.student?.id || sch.studentId) }}
                </div>
                <div class="sch-status" v-if="sch.status === 'cancelled'">✖ Hủy: {{ sch.cancelReason }}</div>
                <div class="sch-status" v-if="sch.status === 'completed'" style="color:#2e7d32">✔ Done</div>
             </div>
           </template>
           <div v-else class="empty-day">Trống lịch</div>
        </div>
      </div>
    </div>
    
    <h2 class="section-title" style="margin-top: 40px;">⚠️ Giám sát Ca Hủy & Lý Do Hủy</h2>
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>HLV Phụ trách</th>
            <th>Khách Hàng</th>
            <th>Khung Giờ Đã Hủy</th>
            <th style="min-width: 300px; color: #b30404;">Lý Do Báo Hủy</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="sch in cancelledSchedules" :key="'cancel-'+sch.id">
            <td><strong>{{ getPtName(sch.ptId) }}</strong></td>
            <td>{{ getStudentName(sch.student?.id || sch.studentId) }}</td>
            <td>{{ sch.date }} <br/> <span style="font-size: 13px; color: #666;">({{ sch.startTime }} - {{ sch.endTime }})</span></td>
            <td style="color: #b30404; font-weight: 500; font-style: italic;">{{ sch.cancelReason }}</td>
          </tr>
          <tr v-if="cancelledSchedules.length === 0">
            <td colspan="4" style="text-align: center; color: #666;">Hệ thống chưa ghi nhận ca báo hủy nào.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <h2 class="section-title" style="margin-top: 40px;">🔮 Chi tiết Ca Tương Lai (Sắp diễn ra)</h2>
    <div class="table-responsive" style="margin-bottom: 50px;">
      <table class="data-table">
        <thead>
          <tr>
            <th>HLV Phụ trách</th>
            <th>Học viên</th>
            <th>Ngày Dạy</th>
            <th>Giờ Lên Lớp</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="sch in futureSchedules" :key="'future-'+sch.id">
            <td><strong>{{ getPtName(sch.ptId) }}</strong></td>
            <td>{{ getStudentName(sch.student?.id || sch.studentId) }}</td>
            <td>{{ sch.date }}</td>
            <td><span class="time-badge">{{ sch.startTime }} - {{ sch.endTime }}</span></td>
            <td><span class="status-badge status-gray">Sắp diễn ra</span></td>
          </tr>
          <tr v-if="futureSchedules.length === 0">
            <td colspan="5" style="text-align: center; color: #666;">Chưa có ca tương lai nào được lên lịch.</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Bản in ẩn PDF -->
    <div style="position: absolute; left: 0; top: 0; z-index: -1; opacity: 0; pointer-events: none;">
      <div id="pdf-report-template" style="padding: 30px; background: white; width: 700px; box-sizing: border-box; color: black; font-family: Arial, sans-serif;">
        <div style="text-align: center; margin-bottom: 30px; border-bottom: 2px solid #b30404; padding-bottom: 20px;">
           <h1 style="color: #b30404; margin: 0;">BÁO CÁO KẾT QUẢ ĐÀO TẠO TUẦN</h1>
           <p style="font-size: 16px; margin: 5px 0 0 0;">Từ ngày {{ weeklyReportData?.startDate }} đến {{ weeklyReportData?.endDate }}</p>
        </div>
        
        <h3 style="color: #333; margin-top: 30px; font-size: 18px;">1. Tổng quan Khối Hệ Thống</h3>
        <table style="width: 100%; border-collapse: collapse; margin-top: 15px;">
          <tr>
            <td style="border: 1px solid #ccc; padding: 12px; font-weight: bold; background: #f9f9f9; width: 50%;">Tổng Lịch Được Lên Kế Hoạch</td>
            <td style="border: 1px solid #ccc; padding: 12px;">{{ weeklyReportData?.total }} ca</td>
          </tr>
          <tr>
            <td style="border: 1px solid #ccc; padding: 12px; font-weight: bold; background: #f9f9f9;">Thực Dạy Thành Công</td>
            <td style="border: 1px solid #ccc; padding: 12px; color: #2e7d32; font-weight: bold;">{{ weeklyReportData?.completed }} ca</td>
          </tr>
          <tr>
            <td style="border: 1px solid #ccc; padding: 12px; font-weight: bold; background: #f9f9f9;">Bị Hủy Theo Lịch</td>
            <td style="border: 1px solid #ccc; padding: 12px; color: #b30404;">{{ weeklyReportData?.cancelled }} ca</td>
          </tr>
          <tr>
            <td style="border: 1px solid #ccc; padding: 12px; font-weight: bold; background: #f9f9f9;">Tổng Doanh Thu Của Tuần</td>
            <td style="border: 1px solid #ccc; padding: 12px; color: #b30404; font-size: 18px; font-weight: bold;">{{ weeklyReportData?.revenueTotal?.toLocaleString('vi-VN') }} VNĐ</td>
          </tr>
        </table>
        
        <h3 style="color: #333; margin-top: 40px; font-size: 18px;">2. Thống kê theo Huấn Luyện Viên</h3>
        <table style="width: 100%; border-collapse: collapse; margin-top: 15px;">
          <thead>
            <tr style="background: #b30404; color: white;">
               <th style="border: 1px solid #ccc; padding: 12px; text-align: left;">Cá nhân PT</th>
               <th style="border: 1px solid #ccc; padding: 12px; text-align: center;">Số ca hoàn thành</th>
               <th style="border: 1px solid #ccc; padding: 12px; text-align: right;">Doanh thu mang về</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(pt, idx) in weeklyReportData?.ptList" :key="idx">
               <td style="border: 1px solid #ccc; padding: 12px; font-weight: bold;">{{ pt.name }}</td>
               <td style="border: 1px solid #ccc; padding: 12px; text-align: center;">{{ pt.count }}</td>
               <td style="border: 1px solid #ccc; padding: 12px; text-align: right;">{{ pt.revenue.toLocaleString('vi-VN') }} đ</td>
            </tr>
            <tr v-if="!weeklyReportData || weeklyReportData.ptList.length === 0">
               <td colspan="3" style="border: 1px solid #ccc; padding:12px; text-align:center;">Tuần này chưa ghi nhận doanh thu phát sinh.</td>
            </tr>
          </tbody>
        </table>
        
        <div style="margin-top: 60px; text-align: right; font-style: italic;">
          <p>Ngày xuất báo cáo: {{ new Date().toLocaleDateString('vi-VN') }}</p>
          <p style="margin-top: 50px;">_______________________</p>
          <p style="font-weight: bold; margin-right: 30px;">Chữ ký Quản Lý</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import html2pdf from 'html2pdf.js'
import { useScheduleStore } from '../stores/schedules'
import { useStudentStore } from '../stores/students'
import { useAuthStore } from '../stores/auth'

const scheduleStore = useScheduleStore()
const studentStore = useStudentStore()
const authStore = useAuthStore()

onMounted(async () => {
  await studentStore.fetchStudents()
  await scheduleStore.fetchSchedules()
  await scheduleStore.fetchStats()
})

function getStudentName(id) {
  const stu = studentStore.allStudents.find(s => s.id === id)
  return stu ? stu.name : 'Unknown'
}

function getPtName(ptId) {
  const user = authStore.users.find(u => u.id === ptId)
  return user ? user.name : 'Hoàng Phan'
}

const todayIso = computed(() => {
  const t = new Date();
  return `${t.getFullYear()}-${(t.getMonth()+1).toString().padStart(2,'0')}-${t.getDate().toString().padStart(2,'0')}`;
})

const cancelledSchedules = computed(() => {
   return scheduleStore.schedules
      .filter(s => s.status === 'cancelled')
      .sort((a,b) => new Date(`${b.date}T${b.startTime}`) - new Date(`${a.date}T${a.startTime}`))
})

const futureSchedules = computed(() => {
   const now = new Date()
   return scheduleStore.schedules
      .filter(s => s.status === 'pending' && new Date(`${s.date}T${s.endTime}`) >= now)
      .sort((a,b) => new Date(`${a.date}T${a.startTime}`) - new Date(`${b.date}T${b.startTime}`))
})

const weeklyReportData = computed(() => {
  if (weekDays.value.length === 0) return null
  const startDate = weekDays.value[0].dateStr
  const endDate = weekDays.value[6].dateStr
  
  const schedulesInWeek = scheduleStore.schedules.filter(s => {
    return s.date >= startDate && s.date <= endDate
  })

  let total = schedulesInWeek.length
  let completed = 0
  let cancelled = 0
  let revenueTotal = 0
  
  const ptStats = {} 

  schedulesInWeek.forEach(sch => {
    if (sch.status === 'completed') {
       completed++
       revenueTotal += 300000
       
       const ptName = getPtName(sch.ptId)
       if (!ptStats[ptName]) ptStats[ptName] = { name: ptName, count: 0, revenue: 0 }
       ptStats[ptName].count++
       ptStats[ptName].revenue += 300000
    }
    if (sch.status === 'cancelled') {
       cancelled++
    }
  })

  return { startDate, endDate, total, completed, cancelled, revenueTotal, ptList: Object.values(ptStats) }
})

async function sendMailReminder() {
  try {
    const res = await fetch('http://localhost:8081/api/schedules/remind-today', { method: 'POST' })
    if (res.ok) {
        alert('Đã chốt gửi email nhắc lịch dạy thành công vào hòm thư Admin!')
    } else {
        const errData = await res.json()
        alert('LỖI BỊ TỪ CHỐI TỪ BACKEND: ' + errData.error)
    }
  } catch (err) {
    alert('Lỗi kết nối tới Server Backend (Hoặc chưa chạy backend)')
  }
}

async function exportExcel() {
  try {
    const res = await fetch('http://localhost:8081/api/schedules/export-excel')
    if (!res.ok) throw new Error('Loi tai file')
    const blob = await res.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'Bao_Cao_Tong_The_Lich_Day.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    alert('Lỗi khi bóc tách file Excel từ Backend!')
  }
}

function exportPDF() {
  const element = document.getElementById('pdf-report-template')
  const opt = {
    margin:       10,
    filename:     `Bao_Cao_Tuan_${weeklyReportData.value?.startDate}.pdf`,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2, windowWidth: 800 },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
  }
  html2pdf().set(opt).from(element).save()
}

const weekDays = computed(() => {
   const today = new Date()
   const distanceToMonday = today.getDay() === 0 ? 6 : today.getDay() - 1
   const monday = new Date(today); monday.setDate(today.getDate() - distanceToMonday); monday.setHours(0,0,0,0)

   const days = []
   for (let i = 0; i < 7; i++) {
      const d = new Date(monday); d.setDate(monday.getDate() + i)
      const dateStr = `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')}`
      const label = i === 6 ? 'Chủ Nhật' : `Thứ ${i+2}`
      const dateNum = `${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}`
      
      const daySchs = scheduleStore.schedules
        .filter(s => s.date === dateStr)
        .sort((a,b) => a.startTime.localeCompare(b.startTime))
      days.push({ dateStr, label, dateNum, schedules: daySchs })
   }
   return days
})
</script>

<style scoped>
.dashboard-container { padding: 40px; max-width: 1200px; margin: 0 auto; }
.header-action { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 3px solid #b30404; padding-bottom: 10px; }
.page-title { color: #b30404; text-align: left; }
.btn-primary { background: #b30404; color: #fff; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; margin-bottom: 0px;}
.section-title { color: #333; font-size: 20px; margin-bottom: 20px; text-align: left; }

.weekly-calendar { display: grid; grid-template-columns: repeat(7, 1fr); gap: 12px; background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.calendar-day { border: 1px solid #eaeaea; border-radius: 8px; overflow: hidden; display: flex; flex-direction: column; min-height: 250px; }
.day-header { background: #f9f9f9; padding: 12px 10px; text-align: center; border-bottom: 1px solid #eaeaea; }
.day-header.is-today { background: #b30404; color: white; }
.day-name { display: block; font-weight: bold; font-size: 15px;}
.day-date { display: block; font-size: 13px; opacity: 0.9;}

.day-content { padding: 10px; flex: 1; background: #fafafa; display: flex; flex-direction: column; gap: 10px; }
.empty-day { color: #bbb; text-align: center; font-size: 14px; margin-top: 25px; font-style: italic; }
.calendar-card { background: #fff; border-left: 4px solid #1976d2; padding: 8px 10px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); font-size: 13px; text-align: left; }
.calendar-card.completed { border-left-color: #388e3c; } 
.calendar-card.cancelled { border-left-color: #d32f2f; background: #fff5f5;}

.sch-time { font-weight: 700; color: #444; font-size: 12px;}
.sch-student { color: #222; margin-top: 4px; font-size: 12px;}
.sch-status { font-style: italic; font-size: 11px; margin-top: 4px;}

.stats-grid { display: flex; gap: 30px; justify-content: flex-start; flex-wrap: wrap; }
.stat-card { background: #fff; border-left: 6px solid #b30404; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); padding: 24px; display: flex; align-items: center; gap: 20px; width: 320px; box-sizing: border-box;}
.stat-icon { font-size: 40px; background: #ffeeee; color: #b30404; padding: 10px 15px; border-radius: 12px; }
.dollar-icon { background: #e8f5e9; color: #2e7d32; }
.stat-info h3 { color: #666; margin: 0 0 5px 0; font-size: 16px; }
.stat-number { font-size: 32px; font-weight: bold; color: #b30404; margin: 0; }

.table-responsive { width: 100%; overflow-x: auto; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-radius: 8px; }
.data-table { width: 100%; min-width: 650px; border-collapse: collapse; background: #fff; font-size: 15px; }
.data-table th { background: #b30404; color: #fff; text-align: left; padding: 15px; }
.data-table td { padding: 15px; border-bottom: 1px solid #eee; }
.time-badge { background: #f5f5f5; color: #333; padding: 5px 10px; border-radius: 12px; font-weight: bold; display: inline-block;}
.status-badge { padding: 5px 12px; border-radius: 20px; font-size: 13px; font-weight: bold; }
.status-gray { background: #f5f5f5; color: #666; }

@media (max-width: 820px) {
  .dashboard-container { padding: 20px; }
  .page-title { font-size: 24px; }
  .section-title { font-size: 18px; margin-top: 30px !important; }
  .weekly-calendar { display: flex; overflow-x: auto; padding-bottom: 15px; -webkit-overflow-scrolling: touch; }
  .calendar-day { flex: 0 0 calc(40% - 10px); min-width: 160px; min-height: 200px; }
  .stats-grid { gap: 15px; }
  .stat-card { width: calc(50% - 8px); padding: 15px; gap: 10px; }
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
