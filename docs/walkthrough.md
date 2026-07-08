# 🔍 Walkthrough — Chi tiết từng File đã tạo/sửa

## Phần A: Backend (16 file mới)

---

### A1. `database.sql` — Script tạo MySQL Database

| Đoạn code | Tác dụng |
|---|---|
| `CREATE DATABASE pt_manager` | Tạo DB mới tên `pt_manager` |
| `CREATE TABLE students (...)` | Bảng lưu thông tin học viên |
| `CREATE TABLE edit_history (...)` | Bảng lưu VẾT thay đổi cân nặng/mục tiêu |
| `CREATE TABLE schedules (...)` | Bảng lưu lịch dạy + trạng thái ca |
| `CREATE TABLE exercises (...)` | Bảng lưu bài tập (giáo án + nhật ký) |
| `FOREIGN KEY ... ON DELETE CASCADE` | **Xóa học viên → tự xóa hết lịch & bài tập liên quan** |
| `INSERT INTO students ...` | 3 học viên mẫu để test |
| `INSERT INTO schedules ...` | 6 ca tập mẫu (3 completed + 3 pending) |
| `INSERT INTO exercises ...` | 5 bài tập mẫu (3 LOG + 2 PLANNED) |

---

### A2. `pom.xml` — Maven Dependencies

| Dependency | Tại sao cần? |
|---|---|
| `spring-boot-starter-data-jpa` | Kết nối DB, tạo Entity, tự sinh SQL query |
| `spring-boot-starter-webmvc` | Tạo REST API controller, xử lý HTTP request |
| `spring-boot-devtools` | Auto restart khi code thay đổi (dev mode) |
| `mysql-connector-j` | Driver kết nối MySQL |
| `spring-boot-starter-validation` | Validate input data |

---

### A3. `application.properties`

```properties
server.port=8081                          # Chạy backend ở port 8081
spring.datasource.url=jdbc:mysql://...    # Đường dẫn tới MySQL database
spring.jpa.hibernate.ddl-auto=update      # Tự tạo/update bảng nếu thiếu cột
spring.jpa.show-sql=true                  # In SQL ra console để debug
```

---

### A4. Giải thích Entity (Model)

#### `Student.java` — Annotation từng dòng

```java
@Entity                          // Đánh dấu: class này = 1 bảng trong DB
@Table(name = "students")        // Tên bảng trong MySQL
public class Student {
    @Id                          // Đây là cột PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự tăng (AUTO_INCREMENT)
    private Long id;

    // Các field = các cột trong bảng, Spring tự map theo tên
    private String name;
    private Integer age;
    // ...

    @OneToMany(mappedBy = "student",      // 1 Student có NHIỀU EditHistory
               cascade = CascadeType.ALL,  // Xóa Student → xóa hết history
               fetch = FetchType.LAZY)     // Chỉ load khi cần (tiết kiệm RAM)
    private List<EditHistory> editHistory;
}
```

#### `EditHistory.java` — Quan hệ ngược

```java
@ManyToOne                        // NHIỀU EditHistory thuộc 1 Student
@JoinColumn(name = "student_id")  // Cột foreign key trong DB
@JsonIgnore                       // KHÔNG trả field này trong JSON (tránh vòng lặp)
private Student student;
```

#### `Schedule.java` — Quan hệ kép

```java
@ManyToOne → Student              // Nhiều ca tập thuộc 1 học viên
@OneToMany → List<Exercise>       // 1 ca tập có nhiều bài tập
// orphanRemoval = true: Xóa exercise khỏi list → tự xóa trong DB
```

#### `Exercise.java` — Dual Purpose

```java
@Column(name = "exercise_type")
private String exerciseType;      // "PLANNED" = giáo án, "LOG" = nhật ký thực tế
```

---

### A5. Giải thích Repository

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    // JpaRepository cho sẵn: save(), findById(), findAll(), deleteById()
    // Không cần viết gì thêm!
}

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // Spring đọc tên method → tự tạo SQL:

    // "findBy" + "StudentId" + "OrderBy" + "Date" + "Desc"
    // → SELECT * FROM schedules WHERE student_id = ? ORDER BY schedule_date DESC
    List<Schedule> findByStudentIdOrderByDateDesc(Long studentId);

    // "findBy" + "Date" + "And" + "Status" + "Not"
    // → SELECT * FROM schedules WHERE schedule_date = ? AND status != ?
    List<Schedule> findByDateAndStatusNot(LocalDate date, String status);
}
```

---

### A6. Giải thích Service (Logic nghiệp vụ)

#### `StudentService.java` — Method `update()` (Quan trọng nhất)

```
Bước 1: Tìm học viên cũ → old = studentRepo.findById(id)

Bước 2: Chụp lại dữ liệu CŨ trước khi ghi đè
         snapshot.weight = old.weight  (75kg)
         snapshot.height = old.height  (175cm)
         snapshot.goal   = old.goal    ("Tăng cơ")
         → Lưu snapshot vào bảng edit_history

Bước 3: GHI ĐÈ dữ liệu MỚI lên bảng students
         old.weight = 72kg  (đã giảm 3kg!)
         old.goal   = "Giữ form"
         → studentRepo.save(old)

KẾT QUẢ: Không mất dữ liệu cũ, luôn theo dõi được tiến trình!
```

#### `ScheduleService.java` — Method `validateSchedule()` (Logic chống đè lịch)

```
Input: date = "2026-04-06", startTime = "17:00"

Bước 1: Chuyển thời gian → phút
   startMins = 17*60 + 0 = 1020
   endMins   = 1020 + 90 = 1110  (kết thúc lúc 18:30)

Bước 2: Kiểm tra khung giờ hợp lệ
   1020 >= 360 (06:00)? ✅
   1110 <= 1320 (22:00)? ✅

Bước 3: Lấy tất cả ca cùng ngày (trừ cancelled)
   Ca đã có: 18:00-19:30 (1080-1170)

Bước 4: Kiểm tra overlap
   startMins(1020) < exEnd(1170)?   → true
   endMins(1110)   > exStart(1080)? → true
   → CẢ HAI true = BỊ ĐÈ! ❌ Trả lỗi!

   Nếu đổi thành 15:00-16:30:
   startMins(900) < exEnd(1170)?   → true
   endMins(990)   > exStart(1080)? → false ←
   → KHÔNG đè! ✅ Cho tạo!
```

#### `ScheduleService.java` — Method `getDashboardStats()`

```
1. Xác định tuần hiện tại: Monday → Sunday
2. Lặp qua TẤT CẢ schedules:
   - Bỏ qua ca "cancelled"
   - Nếu ca nằm trong tuần → sessionsWeek++, revWeek += 300,000
   - Nếu ca = hôm nay → revToday += 300,000
   - Nếu ca cùng tháng → revMonth += 300,000
   - Ghi studentId vào Set → đếm unique clients
3. Trả Map<String, Object> chứa 5 con số thống kê
```

---

### A7. Giải thích Controller (REST API)

#### `StudentController.java`

| Annotation | URL | Tác dụng |
|---|---|---|
| `@GetMapping` | `GET /api/students` | Trả danh sách tất cả học viên |
| `@GetMapping("/{id}")` | `GET /api/students/3` | Trả 1 học viên theo id |
| `@PostMapping` | `POST /api/students` | Tạo học viên mới (JSON body) |
| `@PutMapping("/{id}")` | `PUT /api/students/3` | Sửa học viên + tự lưu snapshot |
| `@DeleteMapping("/{id}")` | `DELETE /api/students/3` | Xóa vĩnh viễn (cascade xóa hết) |
| `@GetMapping("/{id}/history")` | `GET /api/students/3/history` | Xem lịch sử thay đổi |

#### `ScheduleController.java`

| Annotation | URL | Tác dụng |
|---|---|---|
| `@PostMapping` | `POST /api/schedules` | Tạo lịch + giáo án (có validate) |
| `@PutMapping("/{id}/reschedule")` | `PUT /api/schedules/5/reschedule` | Đổi ngày/giờ (có validate) |
| `@PutMapping("/{id}/cancel")` | `PUT /api/schedules/5/cancel` | Hủy ca (bắt buộc lý do) |
| `@PutMapping("/{id}/complete")` | `PUT /api/schedules/5/complete` | Ghi log bài tập + chốt ca |
| `@GetMapping("/stats")` | `GET /api/schedules/stats` | Lấy thống kê dashboard |

---

## Phần B: Frontend (4 file đã sửa)

### B1. `stores/students.js` — Thay đổi chính

**TRƯỚC (Mock Data)**
```javascript
state: () => ({
  allStudents: [
    { id: 1, name: 'Trần Văn A', ... },  // Hardcode trong code
    { id: 2, name: 'Lê Huyền A', ... },
  ]
})

addStudent(student) {
  this.allStudents.push({ id: Date.now(), ...student })  // Chỉ lưu RAM
}
```

**SAU (API Call)**
```javascript
state: () => ({
  allStudents: [],  // Rỗng, sẽ fetch từ DB
})

async fetchStudents() {
  const res = await fetch('http://localhost:8081/api/students')
  this.allStudents = await res.json()  // Lấy từ MySQL
}

async addStudent(student) {
  const res = await fetch(API, {
    method: 'POST',
    body: JSON.stringify(student)       // Gửi lên DB
  })
  const created = await res.json()
  this.allStudents.push(created)        // Server trả về kèm id thực
}
```

### B2. `stores/schedules.js` — Thay đổi chính

**TRƯỚC: Stats tính ở Frontend**
```javascript
// Phải lặp qua mảng, tính ngày, đếm...
sessionsThisWeek() {
  return this.schedules.filter(sch => ...).length
}
revenueThisMonth() {
  return count * PRICE_PER_SESSION  // 300,000
}
```

**SAU: Stats lấy từ Backend**
```javascript
// Chỉ cần gọi 1 API, backend tính sẵn
async fetchStats() {
  const res = await fetch(`${API}/stats`)
  this.stats = await res.json()
  // { sessionsWeek: 6, clientsWeek: 3, revToday: 300000, ... }
}

// Getter chỉ đọc từ stats
sessionsThisWeek() { return this.stats?.sessionsWeek || 0 }
revenueThisMonth() { return this.stats?.revMonth || 0 }
```

### B3. Components — Thêm `onMounted`

Tất cả 4 component đều thêm đoạn này để **load data khi mở trang**:

```javascript
import { onMounted } from 'vue'

onMounted(async () => {
  await studentStore.fetchStudents()    // Gọi API lấy học viên
  await scheduleStore.fetchSchedules()  // Gọi API lấy lịch dạy
  await scheduleStore.fetchStats()      // Gọi API lấy thống kê
})
```

Và tất cả action functions đều chuyển sang `async/await`:

```javascript
// TRƯỚC: Đồng bộ, chỉ thay đổi RAM
function saveStudent() {
  studentStore.addStudent({ ...form.value })
}

// SAU: Bất đồng bộ, gửi lên server
async function saveStudent() {
  await studentStore.addStudent({ ...form.value })
}
```

---

## Phần C: Tổng kết

### Những gì đã hoàn thành ✅

| Hạng mục | Chi tiết |
|---|---|
| **Database** | 4 bảng MySQL, foreign key cascade, data mẫu |
| **Backend** | 16 file Java: 4 Entity, 4 Repository, 2 Service, 2 Controller |
| **CORS** | Frontend (8080) ↔ Backend (8081) kết nối thành công |
| **Frontend** | 6 file sửa: 2 store + 4 component → async API calls |
| **Logic** | Validate 06-22h, chống đè lịch, auto snapshot, dashboard stats |

### Kiểm chứng

- ✅ `http://localhost:8081/api/students` trả JSON 3 học viên
- ✅ `http://localhost:8081/api/schedules` trả JSON 6 lịch dạy
- ✅ `http://localhost:8081/api/schedules/stats` trả JSON thống kê
- ✅ Frontend load data từ MySQL thay vì mock data
- ✅ CRUD operations lưu vĩnh viễn vào DB (refresh vẫn còn)
