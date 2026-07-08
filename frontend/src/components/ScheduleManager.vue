<template>
  <div class="manager-container">
    <div class="header-action">
      <h1 class="page-title">Quản lý Lịch & Nền tảng Log Dạy</h1>
      <button class="btn-primary" @click="openCreateForm">Tạo Lịch Dạy Mới</button>
    </div>

    <div v-if="scheduleStore.errorMsg" class="alert-error">
      Cảnh báo hệ thống: {{ scheduleStore.errorMsg }}
    </div>

    <!-- Danh sách Lịch Responsive -->
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Học viên</th>
            <th>Ngày tập</th>
            <th>Khung Giờ</th>
            <th>Trạng thái</th>
            <th style="min-width: 180px;">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="sch in sortedSchedules" :key="sch.id">
            <td><strong>{{ getStudentName(sch.student?.id || sch.studentId) }}</strong></td>
            <td>{{ sch.date }}</td>
            <td><span class="time-badge">{{ sch.startTime }} - {{ sch.endTime }}</span></td>
            <td>
               <span v-if="sch.status==='completed'" class="status-badge status-green">Đã tập xong</span>
               <span v-else-if="sch.status==='cancelled'" class="status-badge status-red">Đã hủy</span>
               <span v-else class="status-badge status-gray">Tương lai</span>
            </td>
            <td>
              <button v-if="sch.status==='pending' && isPast(sch)" class="btn-primary small-btn" @click="openLogForm(sch)">Cập nhật Log</button>
              <button v-if="sch.status==='completed'" class="btn-secondary small-btn" @click="openViewLog(sch)">Xem Log</button>
              <button v-if="sch.status==='pending'" class="btn-secondary small-btn ml-1" @click="openReschedule(sch)">Sửa</button>
              <button v-if="sch.status==='pending'" class="btn-danger small-btn ml-1" @click="openCancel(sch)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Form Đặt Lịch (Kèm Bảng Lên Lớp Bài Tập) -->
    <div class="modal-overlay" v-if="showForm" @click.self="showForm = false">
      <div class="modal-content log-modal">
        <h2>Đặt lịch dạy & Lên giáo án</h2>
        <form @submit.prevent="saveSchedule" class="pt-form">
          <div class="input-row">
            <div style="flex:1">
              <label>B1: Chọn Khách Hàng</label>
              <select v-model="form.studentId" required style="width: 100%">
                <option disabled value="">--- Lựa chọn hồ sơ ---</option>
                <option v-for="stu in studentStore.students" :key="stu.id" :value="stu.id">
                  {{ stu.name }} ({{ stu.goal }})
                </option>
              </select>
            </div>
            <div style="flex:1">
              <label>B2: Chọn Ngày</label>
              <input type="date" v-model="form.date" :min="todayIso" required style="width: 100%"/>
            </div>
            <div style="flex:1">
              <label>B3: Chốt Giờ</label>
              <input type="time" v-model="form.startTime" min="06:00" max="22:00" required style="width: 100%"/>
            </div>
          </div>

          <label style="margin-top: 20px; margin-bottom: 10px; display: block;">B4: Bảng bài tập chuẩn bị trước (Tùy chọn)</label>
          <div class="exercise-list">
             <div v-for="(ex, idx) in form.plannedExercises" :key="'plan-'+idx" class="exercise-item">
                <input v-model="ex.name" placeholder="Tên bài tập (Bắt buộc)" required />
                <div class="input-row">
                  <input type="number" v-model="ex.sets" placeholder="Số Sets" style="width: 25%" required />
                  <input type="number" v-model="ex.reps" placeholder="Reps" style="width: 25%" required />
                  <input type="text" v-model="ex.rest" placeholder="Rest (60s)" style="width: 25%" required />
                  <select v-model="ex.equipment" style="width: 25%">
                    <option v-for="eq in equipmentOptions" :key="eq" :value="eq">{{eq}}</option>
                  </select>
                </div>
                <button type="button" @click="form.plannedExercises.splice(idx, 1)" class="btn-danger small-btn mt-2">Dỡ bỏ bài</button>
             </div>
          </div>
          <button type="button" class="btn-secondary add-btn" @click="addPlannedExercise">+ Thêm một bài vào Giáo án này</button>
          
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showForm = false">Thoát Form</button>
            <button type="submit" class="btn-primary">Đặt Lịch Ngay</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Mini Modal: Đổi giờ / Chỉnh sửa ca dạy -->
    <div class="modal-overlay" v-if="showRescheduleModal" @click.self="showRescheduleModal = false">
      <div class="modal-content" style="width: 350px;">
        <h3>Sửa thời gian ca học</h3>
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

    <!-- Mini Modal: Hủy ca có lý do theo yêu cầu nghiêm ngặt -->
    <div class="modal-overlay" v-if="showCancelModal" @click.self="showCancelModal = false">
      <div class="modal-content" style="width: 350px;">
        <h3>Bạn đang thao tác Xóa Ca</h3>
        <form @submit.prevent="submitCancel" class="pt-form" style="margin-top: 15px;">
          <label>Lý do hủy (Bắt buộc):</label>
          <textarea v-model="cancelReason" required placeholder="Gõ lý do để đối soát với học viên..."></textarea>
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showCancelModal = false">Hủy bỏ thao tác</button>
            <button type="submit" :class="cancelReason.trim() ? 'btn-red-solid' : 'btn-disabled'" :disabled="!cancelReason.trim()" style="border:none; border-radius:6px; padding: 10px 20px;">Chốt Xóa Ca Dạy</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal GHI LOG BUỔI TẬP -->
    <div class="modal-overlay" v-if="showLogModal" @click.self="showLogModal = false">
      <div class="modal-content log-modal">
        <h2>{{ logFormReadOnly ? 'Hồ sơ Buổi tập' : 'Hoàn thành Ca dạy' }} : {{ getStudentName(currentSchToLog.student?.id || currentSchToLog.studentId) }}</h2>
        <p class="subtitle">Ca dạy lúc: {{ currentSchToLog.startTime }} - {{ currentSchToLog.endTime }} ngày {{ currentSchToLog.date }}</p>
        
        <form @submit.prevent="submitLog" class="pt-form">
          <label>Nhóm cơ/Loại hình (Chọn nhiều):</label>
          <div class="checkbox-group">
            <label v-for="type in workoutTypeOptions" :key="type">
              <input type="checkbox" :value="type" v-model="logForm.workoutTypes" :disabled="logFormReadOnly"> {{ type }}
            </label>
          </div>

          <label>Nhật ký Bài Tập:</label>
          <div class="exercise-list">
             <div v-for="(ex, idx) in logForm.exercises" :key="idx" class="exercise-item">
                <input v-model="ex.name" placeholder="Tên bài tập (Bắt buộc)" required :disabled="logFormReadOnly" />
                <div class="input-row">
                  <input type="number" v-model="ex.sets" placeholder="Số Sets" style="width: 20%" required :disabled="logFormReadOnly"/>
                  <input type="number" v-model="ex.reps" placeholder="Reps/Set" style="width: 20%" required :disabled="logFormReadOnly"/>
                  <input type="text" v-model="ex.rest" placeholder="Rest (60s)" style="width: 30%" required :disabled="logFormReadOnly"/>
                  <input type="number" v-model="ex.rpe" min="1" max="10" placeholder="RPE 1-10" style="width: 30%" title="RPE Độ khó (1-10)" required :disabled="logFormReadOnly"/>
                </div>
                <div class="input-row">
                  <input type="text" v-model="ex.weight" placeholder="Mức Tạ (VD: 50kg / Bodyweight)" style="width: 50%" required :disabled="logFormReadOnly">
                  <select v-model="ex.equipment" style="width: 50%" :disabled="logFormReadOnly">
                    <option disabled value="">- Chọn Thiết bị -</option>
                    <option v-for="eq in equipmentOptions" :key="eq" :value="eq">{{eq}}</option>
                  </select>
                </div>
                <input v-model="ex.note" placeholder="Note kỹ thuật cho bài tập này (nếu có)" :disabled="logFormReadOnly" />
                
                <button v-if="!logFormReadOnly" type="button" @click="logForm.exercises.splice(idx, 1)" class="btn-danger small-btn mt-2">Xóa bài này</button>
             </div>
          </div>
          <button v-if="!logFormReadOnly" type="button" class="btn-secondary add-btn" @click="addExercise">+ Thêm một bài tập</button>

          <label style="margin-top:20px">Đánh giá chung Buổi tập:</label>
          <textarea v-model="logForm.sessionNote" placeholder="Khách tập tốt không? Thể lực ra sao?" required :disabled="logFormReadOnly"></textarea>

          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showLogModal = false">Thoát</button>
            <button v-if="!logFormReadOnly" type="submit" class="btn-primary">Lưu Log & Chốt Ca</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useScheduleStore } from '../stores/schedules'
import { useStudentStore } from '../stores/students'
import { useNotificationStore } from '../stores/notification'

const scheduleStore = useScheduleStore()
const studentStore = useStudentStore()
const notifyStore = useNotificationStore()

onMounted(async () => {
  await studentStore.fetchStudents()
  await scheduleStore.fetchSchedules()
})

const todayIso = computed(() => {
  const t = new Date()
  return `${t.getFullYear()}-${(t.getMonth()+1).toString().padStart(2,'0')}-${t.getDate().toString().padStart(2,'0')}`
})

const showForm = ref(false)
const form = ref({ studentId: '', date: '', startTime: '', plannedExercises: [] })

// Edit (Reschedule) logic
const showRescheduleModal = ref(false)
const currentSchToEdit = ref(null)
const rescheduleForm = ref({ date: '', startTime: '' })

// Cancel logic
const showCancelModal = ref(false)
const currentSchToCancel = ref(null)
const cancelReason = ref('')

// Logs vars
const workoutTypeOptions = ['Thân trên', 'Ngực', 'Lưng', 'Chân', 'Tay', 'Vai', 'Bụng', 'Cardio', 'Giãn cơ', 'Chức năng']
const equipmentOptions = ['Tạ đơn', 'Tạ đòn', 'Tạ ấm', 'Dây kháng lực', 'Máy', 'Cable', 'Bodyweight']
const showLogModal = ref(false)
const logFormReadOnly = ref(false)
const currentSchToLog = ref(null)
const logForm = ref({ workoutTypes: [], exercises: [], sessionNote: '' })

const sortedSchedules = computed(() => {
  return [...scheduleStore.schedules].sort((a,b) => new Date(`${b.date}T${b.startTime}`) - new Date(`${a.date}T${a.startTime}`))
})

function getStudentName(id) {
  const stu = studentStore.students.find(s => s.id === id)
  return stu ? stu.name : 'Unknown'
}

function openCreateForm() {
  scheduleStore.errorMsg = ''
  showForm.value = true
  form.value = { studentId: '', date: '', startTime: '', plannedExercises: [] }
}

function addPlannedExercise() {
   form.value.plannedExercises.push({ name:'', sets:'', reps:'', rest:'', equipment:'Tạ đơn' })
}

async function saveSchedule() {
  const isSuccess = await scheduleStore.addSchedule({ ...form.value })
  if (isSuccess) {
     showForm.value = false
     notifyStore.notify('Đã tạo thành công Lịch dạy & Giáo án!', 'success')
  } else {
     notifyStore.notify(scheduleStore.errorMsg, 'error')
  }
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
    notifyStore.notify('Chỉnh sửa giờ dạy thành công!', 'success')
  } else {
    notifyStore.notify(scheduleStore.errorMsg, 'error')
  }
}

function openCancel(sch) {
   currentSchToCancel.value = sch
   cancelReason.value = ''
   showCancelModal.value = true
}

async function submitCancel() {
   await scheduleStore.cancelSchedule(currentSchToCancel.value.id, cancelReason.value)
   showCancelModal.value = false
   notifyStore.notify('Đã xóa thành công!', 'success')
}

function isPast(sch) {
  const schTime = new Date(`${sch.date}T${sch.endTime}`)
  return schTime < new Date()
}

function openLogForm(sch) {
   currentSchToLog.value = sch
   logFormReadOnly.value = false
   
   // Pre-fill with planned exercises if available
   const baseExercises = (sch.plannedExercises && sch.plannedExercises.length > 0)
     ? sch.plannedExercises.map(pe => ({ ...pe, weight: '', rpe: '', note: '' }))
     : [{ name:'', sets:'', reps:'', rest:'', weight:'', equipment:'Tạ đơn', rpe:'', note:'' }]

   logForm.value = { workoutTypes: [], exercises: baseExercises, sessionNote: '' }
   showLogModal.value = true
}

function openViewLog(sch) {
   currentSchToLog.value = sch
   logFormReadOnly.value = true
   logForm.value = {
     workoutTypes: sch.workoutTypes ? sch.workoutTypes.split(',') : [],
     sessionNote: sch.sessionNote || '',
     exercises: sch.exercises ? JSON.parse(JSON.stringify(sch.exercises.filter(e => e.exerciseType === 'LOG'))) : []
   }
   showLogModal.value = true
}

function addExercise() {
   logForm.value.exercises.push({ name:'', sets:'', reps:'', rest:'', weight:'', equipment:'Tạ đơn', rpe:'', note:'' })
}

async function submitLog() {
   await scheduleStore.completeSchedule(currentSchToLog.value.id, logForm.value)
   showLogModal.value = false
   notifyStore.notify('Lưu Log và Chốt ca hoàn tất!', 'success')
}
</script>

<style scoped>
.manager-container { padding: 40px; max-width: 1100px; margin: 0 auto; box-sizing: border-box; }
.header-action { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 3px solid #b30404; padding-bottom: 10px; }
.page-title { color: #b30404; margin: 0; }
.btn-primary { background: #b30404; color: #fff; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-secondary { background: #ccc; color: #333; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
.btn-danger { background: #fee; color: #b30404; border: 1px solid #b30404; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
.small-btn { padding: 6px 12px; font-size: 13px; }
.ml-1 { margin-left: 8px; }
.add-btn { width: 100%; border: 2px dashed #999; text-align: center; }

.alert-error { background: #ffeeee; color: #b30404; border: 2px solid #b30404; padding: 15px; border-radius: 8px; font-weight: bold; margin-bottom: 20px; }

/* Responsive wrappers */
.table-responsive { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-radius: 8px;}
.data-table { width: 100%; min-width: 650px; border-collapse: collapse; background: #fff; font-size: 15px; }
.data-table th { background: #b30404; color: #fff; text-align: left; padding: 15px; }
.data-table td { padding: 15px; border-bottom: 1px solid #eee; }

.time-badge { background: #ffeeee; color: #b30404; padding: 5px 10px; border-radius: 12px; font-weight: bold; }
.status-badge { padding: 5px 12px; border-radius: 20px; font-size: 13px; font-weight: bold; }
.status-green { background: #e8f5e9; color: #2e7d32; }
.status-orange { background: #fff3e0; color: #ef6c00; }
.status-gray { background: #f5f5f5; color: #666; }
.status-red { background: #ffebee; color: #c62828; }

.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index: 1000; overflow-y: auto;}
.modal-content { background: #fff; padding: 30px; border-radius: 12px; max-width: 90vw; width: 100%; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }
.log-modal { width: 700px; max-height: 90vh; overflow-y: auto; }
.subtitle { margin-top: -10px; color: #666; font-style: italic; margin-bottom: 20px; }

.pt-form { display: flex; flex-direction: column; gap: 15px; margin-top: 15px;}
.pt-form label { font-weight: bold; color: #333; margin-bottom: -5px; }
.pt-form input, .pt-form select, .pt-form textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; outline: none; box-sizing: border-box; }
.pt-form input:focus, .pt-form select:focus, .pt-form textarea:focus { border-color: #b30404; }

.input-row { display: flex; gap: 10px; margin-top: 8px; }
.mt-2 { margin-top: 8px; }

.checkbox-group { display: flex; flex-wrap: wrap; gap: 12px; }
.checkbox-group label { display: flex; align-items: center; gap: 6px; font-weight: normal; font-size: 15px; cursor: pointer;}
.checkbox-group input { width: 18px; margin: 0; }

.exercise-list { display: flex; flex-direction: column; gap: 15px; }
.exercise-item { background: #fafafa; padding: 15px; border-radius: 8px; border: 1px solid #eee; }

.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; border-top: 1px solid #eee; padding-top: 20px;}
.btn-red-solid { background: #b30404; color: white; cursor: pointer; font-weight: bold; transition: background 0.2s; }
.btn-red-solid:hover { background: #900000; }
.btn-disabled { background: #e0e0e0; color: #9a9a9a; cursor: not-allowed; font-weight: bold; border: 1px solid #ccc; }

/* Tablet & iPad Air (820px) */
@media (max-width: 820px) {
  .manager-container { padding: 20px; }
  .page-title { font-size: 22px; }
  .header-action { gap: 15px; }
  .btn-primary, .btn-secondary, .btn-danger { padding: 8px 15px; font-size: 14px; }
  .data-table th, .data-table td { padding: 10px; font-size: 14px; }
  .modal-content { padding: 20px; width: 90vw; max-height: 90vh; }
}
/* Mobile & iPhone 14 Pro Max (430px) */
@media (max-width: 430px) {
  .manager-container { padding: 10px; }
  .page-title { font-size: 18px; margin-bottom: 5px; border-bottom: none;}
  .header-action { flex-direction: column; align-items: stretch; gap: 10px; padding-bottom: 5px; border-bottom: 2px solid #b30404; }
  .header-action button { width: 100%; }
  .data-table th, .data-table td { padding: 8px; font-size: 12px; }
  .time-badge { padding: 4px 8px; font-size: 11px; }
  .status-badge { padding: 4px 8px; font-size: 11px; }
  .small-btn { padding: 4px 8px; font-size: 12px; }
  .modal-content { padding: 15px; }
  h2, h3 { font-size: 18px; margin-top: 0; }
  .pt-form input, .pt-form select, .pt-form textarea { padding: 8px; font-size: 13px; }
  .input-row { flex-direction: column; gap: 10px; }
  .input-row > * { width: 100% !important; }
}
</style>
