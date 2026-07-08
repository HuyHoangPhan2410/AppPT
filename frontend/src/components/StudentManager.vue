<template>
  <div class="manager-container">
    <div class="header-action">
      <h1 class="page-title">Quản lý Học viên</h1>
      <button class="btn-primary" @click="showForm = true">Thêm Học viên mới</button>
    </div>

    <!-- Danh sách Học viên Responsive -->
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Họ tên</th>
            <th>Thông tin (Cân/Cao)</th>
            <th>Gói Tập (Còn lại)</th>
            <th>Mục tiêu</th>
            <th style="min-width: 250px;">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in studentStore.students" :key="student.id">
            <td><strong>{{ student.name }}</strong></td>
            <td>{{ student.age }} tuổi - {{ student.gender }}<br/>{{ student.weight }}kg - {{ student.height }}cm</td>
            <td>
               <span style="font-weight: bold; font-size: 16px;" :style="{ color: student.remainingSessions <= 3 ? '#b30404' : '#2e7d32' }">
                  {{ student.remainingSessions }} / {{ student.totalSessions }}
               </span> buổi
               <br/><span v-if="student.remainingSessions <= 3" style="color: #b30404; font-size: 12px; font-weight: bold;">(Sắp hết hạn!)</span>
            </td>
            <td>{{ student.goal }}</td>
            <td>
              <div class="action-grid">
                 <button class="btn-primary small-btn outline-btn" @click="openEditStudent(student)">Sửa hồ sơ</button>
                 <button class="btn-secondary small-btn outline-btn" @click="openProgress(student)">Tiến độ Cân nặng</button>
                 <button class="btn-secondary small-btn outline-btn" @click="openHistory(student)">Lịch học</button>
                 <button class="btn-danger small-btn outline-btn" @click="deleteStudentRec(student.id)">Xóa</button>
                 <button class="btn-primary small-btn outline-btn" style="background:#e3f2fd; color:#1976d2; border:1px solid #1976d2; grid-column: span 2;" @click="openPayment(student)">💳 Thu Tiền (QR)</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Form Thêm học viên -->
    <div class="modal-overlay" v-if="showForm" @click.self="closeForm">
      <div class="modal-content">
        <h2>Thêm Học viên</h2>
        <form @submit.prevent="saveStudent" class="pt-form">
          <input v-model="form.name" placeholder="Họ và tên" required />
          <div class="input-row">
            <input v-model.number="form.age" type="number" placeholder="Tuổi" required />
            <select v-model="form.gender">
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </select>
          </div>
          <div class="input-row">
            <input v-model.number="form.weight" type="number" placeholder="Cân nặng (kg)" required />
            <input v-model.number="form.height" type="number" placeholder="Chiều cao (cm)" required />
          </div>
          <textarea v-model="form.goal" placeholder="Mục tiêu tập luyện" required></textarea>
          <textarea v-model="form.background" placeholder="Nền tảng tập luyện"></textarea>
          <textarea v-model="form.notes" placeholder="Ghi chú (Bệnh lý, dị ứng...)"></textarea>

          <div class="input-row">
            <input v-model.number="form.totalSessions" type="number" placeholder="Tổng số buổi mua" required />
            <input v-model.number="form.remainingSessions" type="number" placeholder="Số buổi hiện còn" required />
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="closeForm">Hủy</button>
            <button type="submit" class="btn-primary">Lưu Học viên</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Sửa Học Viên (Với hệ thống lưu Track) -->
    <div class="modal-overlay" v-if="showEditModal" @click.self="showEditModal = false">
      <div class="modal-content">
        <h2>Sửa Thông Tin & Cập Nhật Chỉ Số</h2>
        <form @submit.prevent="submitEditStudent" class="pt-form">
          <input v-model="editForm.name" placeholder="Họ và tên" required />
          <div class="input-row">
            <input v-model.number="editForm.age" type="number" placeholder="Tuổi" required />
            <select v-model="editForm.gender">
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </select>
          </div>
          <div class="input-row">
            <input v-model.number="editForm.weight" type="number" placeholder="Cân nặng (kg) MỚI" required />
            <input v-model.number="editForm.height" type="number" placeholder="Chiều cao (cm) MỚI" required />
          </div>
          <textarea v-model="editForm.goal" placeholder="Mục tiêu hiện tại" required></textarea>
          <textarea v-model="editForm.background" placeholder="Nền tảng tập luyện"></textarea>
          <textarea v-model="editForm.notes" placeholder="Ghi chú bệnh lý (CẬP NHẬT)"></textarea>
          
          <div class="input-row">
            <input v-model.number="editForm.totalSessions" type="number" placeholder="Tổng số buổi mua" required />
            <input v-model.number="editForm.remainingSessions" type="number" placeholder="Số buổi còn lại" required />
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showEditModal = false">Hủy</button>
            <button type="submit" class="btn-primary">Lưu Cập Nhật (Đưa thông số cũ vào Lịch sử)</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Lịch sử Thay đổi (Tiến độ) -->
    <div class="modal-overlay" v-if="showProgressModal" @click.self="showProgressModal = false">
      <div class="modal-content large-history">
        <h2 style="color: #b30404; margin-bottom: 15px;">Tiến độ chỉ số: {{ targetStudent?.name }}</h2>
        
        <div class="current-stats">
           <h4>Thông số Khách hàng (Hiện tại)</h4>
           <p><strong>Cân nặng:</strong> {{ targetStudent?.weight }} kg | <strong>Chiều cao:</strong> {{ targetStudent?.height }} cm</p>
           <p><strong>Mục tiêu:</strong> {{ targetStudent?.goal }}</p>
           <p><strong>Bệnh lý/Ghi chú:</strong> {{ targetStudent?.notes || 'Không có' }}</p>
        </div>
        
        <h4 style="margin-top: 20px; color: #333;">Lịch sử Thay đổi (Các mốc cũ)</h4>
        <div class="table-responsive" v-if="targetStudent?.editHistory && targetStudent.editHistory.length > 0">
           <table class="data-table" style="min-width: 100%;">
             <thead>
               <tr>
                 <th>Chỉ số Cũ ở Ngày</th>
                 <th>Cân nặng</th>
                 <th>Ghi chú Bệnh Lý cũ</th>
               </tr>
             </thead>
             <tbody>
               <tr v-for="(hist, idx) in targetStudent.editHistory.slice().reverse()" :key="idx">
                 <td><strong>{{ hist.date }}</strong></td>
                 <td>{{ hist.weight }} kg</td>
                 <td>{{ hist.notes }}<br/><em>Mục tiêu: {{ hist.goal }}</em></td>
               </tr>
             </tbody>
          </table>
        </div>
        <div v-else style="text-align: center; color: #999; padding: 20px;">
           Khách hàng này chưa có bất kỳ lịch sử biến đổi hay cập nhật thông tin nào. (Cân nặng không thay đổi).
        </div>
        <button class="btn-secondary mt-2" @click="showProgressModal = false" style="width: 100%; margin-top: 15px;">Đóng Lịch Sử Chỉnh Sửa</button>
      </div>
    </div>

    <!-- Modal Lịch sử Tập luyện -->
    <div class="modal-overlay" v-if="showHistoryModal" @click.self="showHistoryModal = false">
      <div class="modal-content large-history">
        <h2 style="color: #b30404; margin-bottom: 20px;">Lịch sử Lên Lớp: {{ targetStudent?.name }}</h2>
        
        <div class="history-list">
           <div v-for="sch in studentSchedules" :key="sch.id" :class="['history-card', sch.status]">
              <div class="history-info">
                 <strong>{{ sch.date }}</strong> | {{ sch.startTime }} - {{ sch.endTime }}
                 <p class="status-label">
                    Trạng thái: 
                    <span v-if="sch.status === 'completed'" style="background: #e8f5e9; color: #2e7d32; padding: 2px 6px; border-radius: 4px;">Đã Tập</span>
                    <span v-if="sch.status === 'pending'" style="background: #f5f5f5; color: #757575; padding: 2px 6px; border-radius: 4px;">Sắp Tập</span>
                    <span v-if="sch.status === 'cancelled'" style="background: #ffebee; color: #c62828; padding: 2px 6px; border-radius: 4px;">Bị Hủy</span>
                    <span v-if="sch.isPaid" style="margin-left: 8px; background: #e3f2fd; color: #1976d2; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-weight: bold;">[Đã Nạp Tiền]</span>
                 </p>
                 <p v-if="sch.status === 'cancelled' && sch.cancelReason" class="reason">Lý do hủy: {{ sch.cancelReason }}</p>
                 <p v-if="sch.status === 'completed'" class="reason">Dặn dò PT: {{ sch.sessionNote }}</p>
              </div>
              
              <div class="history-actions" v-if="sch.status === 'pending'">
                 <button class="btn-secondary small-btn" @click="openReschedule(sch)">Đổi Giờ</button>
                 <button class="btn-danger small-btn" @click="openCancel(sch)">Khách Hủy Ca</button>
              </div>
           </div>
           <div v-if="studentSchedules.length === 0" style="text-align: center; color: #999;">
             Học viên này chưa có bất kỳ lịch sử xếp ca nào.
           </div>
        </div>
        <button class="btn-secondary mt-2" @click="showHistoryModal = false" style="width: 100%; margin-top: 15px;">Đóng Bảng Lịch Sự</button>
      </div>
    </div>

    <!-- Modal Thanh Toán QR -->
    <div class="modal-overlay" v-if="showPaymentModal" @click.self="showPaymentModal = false">
      <div class="modal-content" style="width: 400px; text-align: center;">
        <h2 style="color: #1976d2; margin-bottom: 5px;">Thanh toán Mã Khách Hàng</h2>
        <h3 style="margin-top:0;">{{ targetStudent?.name }}</h3>
        
        <div v-if="paymentInfo.count > 0">
           <div class="current-stats" style="border-left-color: #1976d2; background: #e3f2fd;">
              <p>Số ca hóa đơn (Tương lai): <strong>{{ paymentInfo.count }} ca</strong></p>
              <p style="font-size: 18px; color: #b30404; font-weight: bold;">Tổng tiền: {{ paymentInfo.amount.toLocaleString('vi-VN') }} VND</p>
           </div>
           
           <div style="border: 2px dashed #ccc; padding: 10px; border-radius: 8px; display: inline-block;">
             <img :src="paymentInfo.qrUrl" alt="VietQR Code" style="width: 100%; max-width: 300px; border-radius: 8px;" />
           </div>
           <p style="font-style: italic; font-size: 13px; color: #666; margin-top: 10px;">Vui lòng dùng ứng dụng Ngân hàng để quét.</p>
        </div>
        <div v-if="paymentInfo.count > 0" style="margin-top: 15px;">
           <button class="btn-primary" style="width: 100%; font-size: 16px; background: #2e7d32; padding: 12px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);" @click="confirmPayment">✅ Xác Nhận Đã Chuyển Khoản</button>
        </div>

        <div v-else style="padding: 20px; color: #2e7d32; font-weight: bold;">
           Khách hàng này đã thanh toán đủ hoặc chưa có ca học nào trên lịch.
        </div>
        
        <button class="btn-secondary mt-2" @click="showPaymentModal = false" style="width: 100%; margin-top: 10px;">Đóng Bảng Thanh Toán</button>
      </div>
    </div>

    <!-- Mini Modal: Đổi giờ -->
    <div class="modal-overlay" v-if="showRescheduleModal" @click.self="showRescheduleModal = false">
      <div class="modal-content" style="width: 350px;">
        <h3>Đổi thời gian ca học</h3>
        <form @submit.prevent="submitReschedule" class="pt-form">
          <label>Ngày mới:</label>
          <input type="date" v-model="rescheduleForm.date" :min="todayIso" required>
          <label>Giờ mới:</label>
          <input type="time" v-model="rescheduleForm.startTime" min="06:00" max="22:00" required>
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showRescheduleModal = false; scheduleStore.errorMsg=''">Thoát</button>
            <button type="submit" class="btn-primary">Lưu Thay Đổi</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Mini Modal: Hủy ca (Có yêu cầu Lý do) -->
    <div class="modal-overlay" v-if="showCancelModal" @click.self="showCancelModal = false">
      <div class="modal-content" style="width: 350px;">
        <h3>Hủy ca học của khách này</h3>
        <form @submit.prevent="submitCancel" class="pt-form" style="margin-top: 15px;">
          <label>Cập nhật lý do hủy ca (Bắt buộc):</label>
          <textarea v-model="cancelReason" required placeholder="Gõ lý do để còn kiểm tra đối soát..."></textarea>
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showCancelModal = false">Quay lại</button>
            <button type="submit" :class="cancelReason.trim() ? 'btn-red-solid' : 'btn-disabled'" :disabled="!cancelReason.trim()" style="border:none; border-radius:6px; padding: 10px 20px;">Lưu Lý Do Hủy Lịch</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStudentStore } from '../stores/students'
import { useScheduleStore } from '../stores/schedules'
import { useNotificationStore } from '../stores/notification'

const studentStore = useStudentStore()
const scheduleStore = useScheduleStore()
const notifyStore = useNotificationStore()

onMounted(async () => {
  if (!window.confetti) {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/canvas-confetti@1.6.0/dist/confetti.browser.min.js'
    document.head.appendChild(script)
  }

  await studentStore.fetchStudents()
  await scheduleStore.fetchSchedules()
})

const showForm = ref(false)
const form = ref({ name: '', age: '', gender: 'Nam', weight: '', height: '', goal: '', background: '', notes: '', totalSessions: 100, remainingSessions: 100 })

// Edit & History Progression state
const showEditModal = ref(false)
const showProgressModal = ref(false)
const editForm = ref({})

// History state
const showHistoryModal = ref(false)
const showPaymentModal = ref(false)
const targetStudent = ref(null)
const paymentInfo = ref({ count: 0, amount: 0, qrUrl: '' })

const studentSchedules = computed(() => {
  if (!targetStudent.value) return []
  return scheduleStore.schedules
    .filter(s => (s.student?.id || s.studentId) === targetStudent.value.id)
    .sort((a,b) => new Date(`${b.date}T${b.startTime}`) - new Date(`${a.date}T${a.startTime}`))
})

// Sub Modals State
const showRescheduleModal = ref(false)
const showCancelModal = ref(false)
const currentSchToEdit = ref(null)

const todayIso = computed(() => {
  const t = new Date()
  return `${t.getFullYear()}-${(t.getMonth()+1).toString().padStart(2,'0')}-${t.getDate().toString().padStart(2,'0')}`
})

const rescheduleForm = ref({ date: '', startTime: '' })
const cancelReason = ref('')

function closeForm() {
  showForm.value = false
  form.value = { name: '', age: '', gender: 'Nam', weight: '', height: '', goal: '', background: '', notes: '', totalSessions: 100, remainingSessions: 100 }
}
async function saveStudent() {
  await studentStore.addStudent({ ...form.value })
  closeForm()
  notifyStore.notify('Thêm học viên mới thành công!', 'success')
}

// Update capabilities
function openEditStudent(stu) {
  editForm.value = JSON.parse(JSON.stringify(stu))
  showEditModal.value = true
}

async function submitEditStudent() {
  await studentStore.updateStudent(editForm.value.id, editForm.value)
  showEditModal.value = false
  notifyStore.notify('Đã cập nhật Thông tin Học viên và ghi nhận Lịch sử Biến thiên!', 'success')
}

function openProgress(stu) {
  targetStudent.value = stu
  showProgressModal.value = true
}

async function deleteStudentRec(id) {
  await studentStore.deleteStudent(id)
  notifyStore.notify('Xóa hồ sơ học viên vĩnh viễn!', 'success')
}

function openHistory(student) {
  targetStudent.value = student
  showHistoryModal.value = true
}

function openReschedule(sch) {
  currentSchToEdit.value = sch
  rescheduleForm.value = { date: sch.date, startTime: sch.startTime }
  scheduleStore.errorMsg = ''
  showRescheduleModal.value = true
}

async function submitReschedule() {
  const success = await scheduleStore.updateScheduleTime(currentSchToEdit.value.id, rescheduleForm.value.date, rescheduleForm.value.startTime)
  if (success) {
    showRescheduleModal.value = false
    notifyStore.notify('Cập nhật giờ dạy thành công!', 'success')
  } else {
    notifyStore.notify(scheduleStore.errorMsg, 'error')
  }
}

function openCancel(sch) {
  currentSchToEdit.value = sch
  cancelReason.value = ''
  showCancelModal.value = true
}

async function submitCancel() {
  await scheduleStore.cancelSchedule(currentSchToEdit.value.id, cancelReason.value)
  showCancelModal.value = false
  notifyStore.notify('Đã đưa ca này vào Hồ sơ Hủy Lịch thành công!', 'success')
}

// Xử lý loại bỏ dấu Tiếng Việt cho mô tả chuyển khoản
function removeAccents(str) {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D')
}

function openPayment(stu) {
  targetStudent.value = stu
  const pendingSchedules = scheduleStore.schedules.filter(s => {
      const dbId = s.student?.id || s.studentId
      return dbId === stu.id && s.status === 'pending' && !s.isPaid
  })
  
  const count = pendingSchedules.length
  const amount = count * 300000
  
  const cleanName = removeAccents(stu.name)
  const addInfo = encodeURIComponent(`Tien tap voi PT Hoang Phan cua ${cleanName}`)
  const url = `https://img.vietqr.io/image/TCB-19036231787017-compact2.png?amount=${amount}&addInfo=${addInfo}&accountName=Phan%20Huy%20Hoang`
  
  paymentInfo.value = { count, amount, qrUrl: url }
  showPaymentModal.value = true
}

async function confirmPayment() {
  if (!targetStudent.value) return
  const success = await scheduleStore.markPaid(targetStudent.value.id)
  if (success) {
     showPaymentModal.value = false
     if (window.confetti) {
       window.confetti({ particleCount: 150, spread: 80, origin: { y: 0.6 } })
     }
     notifyStore.notify('Gạch nợ thành công! Các ca học đã chuyển sang Đã Thanh Toán.', 'success')
  } else {
     alert('Lỗi xác nhận thanh toán!')
  }
}
</script>

<style scoped>
.manager-container { padding: 40px; max-width: 1000px; margin: 0 auto; box-sizing: border-box; }
.header-action { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 3px solid #b30404; padding-bottom: 10px; }
.page-title { color: #b30404; margin: 0; }
.btn-primary { background: #b30404; color: #fff; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-secondary { background: #ccc; color: #333; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
.btn-danger { background: #fee; color: #b30404; border: 1px solid #b30404; padding: 6px 12px; border-radius: 4px; cursor: pointer; }

.small-btn { padding: 6px 12px; font-size: 13px; margin: 0;}
.outline-btn { width: 100%; border-radius: 4px;}

.action-grid { display: grid; gap: 8px; grid-template-columns: 1fr 1fr; }

/* Responsive Wrappers */
.table-responsive { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-radius: 8px;}
.data-table { width: 100%; min-width: 650px; border-collapse: collapse; background: #fff; }
.data-table th { background: #b30404; color: #fff; text-align: left; padding: 15px; }
.data-table td { padding: 15px; border-bottom: 1px solid #eee; }

.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index: 1000; overflow-y: auto; padding: 20px; box-sizing: border-box;}
.modal-content { background: #fff; padding: 30px; border-radius: 12px; width: 500px; max-width: 100%; box-shadow: 0 10px 30px rgba(0,0,0,0.2); max-height: 95vh; overflow-y: auto; }
.large-history { width: 700px;}

.history-list { display: flex; flex-direction: column; gap: 15px; }
.history-card { 
  display: flex; justify-content: space-between; align-items: flex-start; 
  padding: 15px; border-radius: 8px; border-left: 6px solid #ccc;
  background: #f9f9f9;
}
.history-card.completed { border-color: #2e7d32; background: #f1f8e9; }
.history-card.pending { border-color: #9e9e9e; background: #f5f5f5; }
.history-card.cancelled { border-color: #c62828; background: #ffebee; }

.history-info strong { font-size: 16px; }
.status-label { margin: 5px 0; font-size: 14px; font-weight: 500;}
.reason { margin: 5px 0 0 0; font-size: 14px; font-style: italic; }

.history-actions { display: flex; gap: 8px; flex-direction: column; }

.current-stats { background: #ffeeee; border-left: 4px solid #b30404; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.current-stats h4 { margin: 0 0 10px 0; color: #b30404; font-size: 16px; }
.current-stats p { margin: 5px 0; font-size: 14px; color: #333; }

.alert-error { background: #ffeeee; color: #b30404; padding: 10px; border-radius: 6px; font-size: 14px; }
.pt-form { display: flex; flex-direction: column; gap: 15px; margin-top: 15px;}
.pt-form input, .pt-form select, .pt-form textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; outline: none; box-sizing: border-box; }
.input-row { display: flex; gap: 10px; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.btn-red-solid { background: #b30404; color: white; cursor: pointer; font-weight: bold; transition: background 0.2s; }
.btn-red-solid:hover { background: #900000; }
.btn-disabled { background: #e0e0e0; color: #9a9a9a; cursor: not-allowed; font-weight: bold; border: 1px solid #ccc; }

/* Tablet & iPad Air (820px) */
@media (max-width: 820px) {
  .manager-container { padding: 20px; }
  .page-title { font-size: 22px; }
  .btn-primary, .btn-secondary, .btn-danger { padding: 8px 15px; font-size: 14px; }
  .data-table th, .data-table td { padding: 10px; font-size: 14px; }
  .modal-content { padding: 20px; }
}
/* Mobile & iPhone 14 Pro Max (430px) */
@media (max-width: 430px) {
  .manager-container { padding: 10px; }
  .page-title { font-size: 18px; margin-bottom: 5px; border-bottom: none;}
  .header-action { flex-direction: column; align-items: stretch; gap: 10px; padding-bottom: 5px; border-bottom: 2px solid #b30404; }
  .header-action button { width: 100%; }
  .data-table th, .data-table td { padding: 8px; font-size: 12px; }
  .small-btn { padding: 4px 8px; font-size: 12px; }
  .modal-content { padding: 15px; }
  h2, h3 { font-size: 18px; margin-top: 0; }
  .pt-form input, .pt-form select, .pt-form textarea { padding: 8px; font-size: 13px; }
  .input-row { flex-direction: column; gap: 10px; }
  .input-row > * { width: 100% !important; }
  .history-card { flex-direction: column; gap: 12px; padding: 10px;}
  .history-info strong { font-size: 14px; }
  .history-actions { flex-direction: row; width: 100%; justify-content: flex-end; }
  .action-grid { grid-template-columns: 1fr; }
}
</style>
