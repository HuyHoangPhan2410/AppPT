# 📘 Implementation Plan — Spring Boot Backend cho PT Manager

## 1. Tổng quan Kiến trúc

Hệ thống gồm 2 phần chạy **độc lập**: Vue Frontend (port 8080) giao tiếp với Spring Boot Backend (port 8081) thông qua **REST API** (JSON).

```
  🖥️ Vue Frontend (port 8080)
         │
         │  fetch() gửi JSON
         ▼
  🔌 REST Controller ─── Nhận request, trả response
         │
         ▼
  ⚙️ Service Layer ───── Xử lý logic nghiệp vụ
         │
         ▼
  📦 Repository ──────── JPA tự tạo SQL query
         │
         ▼
  🗃️ MySQL Database ──── pt_manager (port 3306)
```

---

## 2. Database Schema (4 bảng)

```
  ┌──────────────┐       ┌──────────────────┐
  │   students   │───1:N─│   edit_history    │
  │──────────────│       │──────────────────│
  │ id (PK)      │       │ id (PK)          │
  │ name         │       │ student_id (FK)  │
  │ age          │       │ record_date      │
  │ gender       │       │ weight (CŨ)      │
  │ weight       │       │ height (CŨ)      │
  │ height       │       │ goal (CŨ)        │
  │ goal         │       │ notes (CŨ)       │
  │ background   │       └──────────────────┘
  │ notes        │
  └──────┬───────┘
         │
         │ 1:N
         ▼
  ┌──────────────────┐       ┌──────────────────┐
  │    schedules     │───1:N─│    exercises      │
  │──────────────────│       │──────────────────│
  │ id (PK)          │       │ id (PK)          │
  │ student_id (FK)  │       │ schedule_id (FK) │
  │ schedule_date    │       │ exercise_type    │
  │ start_time       │       │   PLANNED / LOG  │
  │ end_time         │       │ name             │
  │ status           │       │ sets, reps, rest │
  │   pending /      │       │ equipment        │
  │   completed /    │       │ weight, rpe      │
  │   cancelled      │       │ note             │
  │ cancel_reason    │       └──────────────────┘
  │ session_note     │
  │ workout_types    │
  └──────────────────┘
```

> **exercise_type** phân biệt 2 loại bài tập:
> - `PLANNED` = Giáo án chuẩn bị trước (khi tạo lịch)
> - `LOG` = Nhật ký thực tế (khi hoàn thành ca)

---

## 3. Giải thích 4 Layer (Từ dưới lên)

### Layer 1: Model (Entity) — Định nghĩa bảng DB

Mỗi file trong `model/` tương ứng **1 bảng** trong MySQL:

| File | Bảng | Annotation quan trọng |
|---|---|---|
| `Student.java` | `students` | `@OneToMany` → liên kết `editHistory` và `schedules` |
| `EditHistory.java` | `edit_history` | `@ManyToOne` + `@JsonIgnore` → tránh vòng lặp JSON |
| `Schedule.java` | `schedules` | `@ManyToOne` → Student, `@OneToMany` → Exercises |
| `Exercise.java` | `exercises` | `@ManyToOne` + `@JsonIgnore` → tránh vòng lặp JSON |

> **`@JsonIgnore`** rất quan trọng! Nếu không có, khi trả JSON:
> `Exercise` → chứa `Schedule` → chứa `List<Exercise>` → **vòng lặp vô hạn** → crash!

---

### Layer 2: Repository — Tự động tạo SQL

Spring Data JPA **tự tạo SQL** dựa theo tên method:

```java
// JPA đọc tên method và tự sinh ra:
// SELECT * FROM edit_history WHERE student_id = ? ORDER BY record_date DESC
List<EditHistory> findByStudentIdOrderByDateDesc(Long studentId);

// SELECT * FROM schedules WHERE schedule_date = ? AND status != ?
List<Schedule> findByDateAndStatusNot(LocalDate date, String status);
```

> Bạn **không cần viết SQL** — chỉ cần đặt tên method đúng quy tắc là JPA tự hiểu!

---

### Layer 3: Service — Logic nghiệp vụ

Đây là **bộ não**, chứa toàn bộ logic quan trọng:

#### 3a. StudentService — Auto Snapshot

```
Khi PT sửa thông tin Khách hàng:
1. Lấy dữ liệu CŨ của học viên
2. Tạo 1 bản snapshot (chụp lại weight, height, goal, notes CŨ)
3. Lưu snapshot vào bảng edit_history
4. MỚI ghi đè dữ liệu mới lên bảng students
→ Không bao giờ mất dữ liệu cũ!
```

#### 3b. ScheduleService — 3 Logic chính

**Logic 1: Validate khung giờ 06:00 - 22:00**
```
Chuyển startTime thành phút: "18:00" → 1080 phút
Cộng thêm 90 phút → endTime = 1170 phút = "19:30"
Kiểm tra: startMins >= 360 (06:00) VÀ endMins <= 1320 (22:00)
```

**Logic 2: Chống đè lịch (Overlap Prevention)**
```
Lấy tất cả ca cùng ngày (trừ ca đã hủy)
Với mỗi ca đã có (exStart, exEnd):
   Nếu startMins < exEnd VÀ endMins > exStart → BỊ ĐÈ!

Ví dụ: Ca cũ 17:00-18:30, tạo ca mới 18:00-19:30
   18:00 < 18:30 (true) VÀ 19:30 > 17:00 (true) → BỊ ĐÈ ❌
```

**Logic 3: Dashboard Stats**
```
Lặp qua toàn bộ schedules:
- Đếm ca trong tuần (Thứ 2 → Chủ Nhật)
- Đếm khách unique bằng Set<Long>
- Tính doanh thu = số ca × 300,000đ
```

---

### Layer 4: Controller — REST API endpoints

Controller chỉ làm **2 việc**:
1. **Nhận request** từ frontend (JSON body, URL params)
2. **Gọi Service** xử lý rồi **trả response** (JSON)

```java
// Ví dụ: POST /api/schedules
@PostMapping
public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
    // 1. Lấy data từ request
    Long studentId = Long.valueOf(body.get("studentId").toString());

    // 2. Gọi Service validate
    String error = scheduleService.validateSchedule(date, startTime, null);
    if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));

    // 3. Gọi Service tạo mới
    Schedule created = scheduleService.create(studentId, date, startTime, planned);
    return ResponseEntity.ok(created);  // Trả JSON cho frontend
}
```

---

## 4. CORS — Cầu nối Vue ↔ Spring Boot

```
Vue (port 8080) → gọi fetch("http://localhost:8081/api/...")
                                    ↓
Browser chặn! (Same-Origin Policy)
                                    ↓
CorsConfig.java cho phép: "port 5500, 5501 được gọi API"
                                    ↓
Browser cho phép request đi qua ✅
```

---

## 5. Data Flow — Ví dụ: Thêm Học Viên mới

```
  👤 User: Nhập tên, tuổi, cân nặng... → Bấm "Lưu"
     │
     ▼
  🖥️ Vue Frontend: fetch(POST /api/students, JSON body)
     │
     ▼
  📡 StudentController: nhận @RequestBody Student
     │
     ▼
  ⚙️ StudentService: studentRepo.save(student)
     │
     ▼
  📦 StudentRepository: JPA tự tạo INSERT INTO students...
     │
     ▼
  🗃️ MySQL: INSERT INTO students (name, age...) VALUES ('Trần Văn A', 25...)
     │                            Trả về id = 4 (tự tăng)
     ▼
  📡 Controller: return ResponseEntity.ok(student) → JSON 200 OK
     │
     ▼
  🖥️ Vue: allStudents.push(created) → UI tự cập nhật
     │
     ▼
  👤 User: Hiện toast "Thêm thành công!"
```

---

## 6. Cấu trúc thư mục hoàn chỉnh

```
my-project/
├── src/                              ← Vue Frontend
│   ├── components/
│   │   ├── Dashboard.vue             (Trang PT)
│   │   ├── AdminDashboard.vue        (Trang Manager)
│   │   ├── StudentManager.vue        (CRUD Học viên)
│   │   └── ScheduleManager.vue       (CRUD Lịch dạy)
│   └── stores/
│       ├── students.js               (fetch → /api/students)
│       └── schedules.js              (fetch → /api/schedules)
│
└── backend/                          ← Spring Boot Backend
    ├── database.sql                  (Tạo DB trong MySQL)
    ├── pom.xml                       (Maven dependencies)
    └── src/main/java/.../ptmanager/
        ├── PtManagerApplication.java (Main class)
        ├── config/CorsConfig.java    (Cho phép CORS)
        ├── model/                    (4 Entity = 4 bảng DB)
        ├── repository/               (4 Interface JPA)
        ├── service/                  (2 Service = logic)
        └── controllers/             (2 Controller = API)
```
