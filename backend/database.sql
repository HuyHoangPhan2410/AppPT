CREATE DATABASE pt_manager;
USE pt_manager;

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    gender VARCHAR(10),
    weight DOUBLE,
    height DOUBLE,
    goal VARCHAR(255),
    background VARCHAR(255),
    notes VARCHAR(500)
);

CREATE TABLE edit_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    record_date DATE,
    weight DOUBLE,
    height DOUBLE,
    goal VARCHAR(255),
    notes VARCHAR(500),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    cancel_reason VARCHAR(500),
    session_note VARCHAR(1000),
    workout_types VARCHAR(500),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    exercise_type VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    sets INT,
    reps INT,
    rest VARCHAR(50),
    equipment VARCHAR(100),
    weight VARCHAR(50),
    rpe INT,
    note VARCHAR(500),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

INSERT INTO students (name, age, gender, weight, height, goal, background, notes) VALUES
('Trần Văn A', 25, 'Nam', 75, 175, 'Tăng cơ, giảm mỡ', 'Tập gym 1 năm', 'Đau mỏi vai gáy'),
('Lê Huyền A', 30, 'Nữ', 80, 180, 'Giảm cân', 'Chưa từng tập', 'Dị ứng lactose'),
('Dương Lạc', 22, 'Nam', 65, 170, 'Tăng cường sức mạnh', 'Calisthenics', 'Bị thoái hóa C4');

INSERT INTO schedules (student_id, schedule_date, start_time, end_time, status, session_note, workout_types) VALUES
(1, CURDATE() - INTERVAL 1 DAY, '18:00', '19:30', 'completed', 'Khách tập rất sung.', 'Ngực,Tay'),
(2, CURDATE() - INTERVAL 2 DAY, '18:00', '19:30', 'completed', 'Tốt, cần tăng tạ.', 'Chân,Lưng'),
(3, CURDATE() - INTERVAL 3 DAY, '18:00', '19:30', 'completed', 'Thể lực ổn.', 'Vai,Bụng'),
(1, CURDATE(), '17:00', '18:30', 'pending', NULL, NULL),
(2, CURDATE() + INTERVAL 1 DAY, '17:00', '18:30', 'pending', NULL, NULL),
(3, CURDATE() + INTERVAL 2 DAY, '17:00', '18:30', 'pending', NULL, NULL);

INSERT INTO exercises (schedule_id, exercise_type, name, sets, reps, rest, equipment, weight, rpe, note) VALUES
(1, 'LOG', 'Đẩy ngực tạ đòn', 4, 10, '90s', 'Tạ đòn', '60kg', 8, 'Form tốt'),
(2, 'LOG', 'Squat', 4, 12, '60s', 'Tạ đòn', '50kg', 7, 'Cần giữ lưng thẳng'),
(3, 'LOG', 'Plank', 3, 1, '30s', 'Bodyweight', 'Bodyweight', 6, 'Giữ 60s mỗi set'),
(4, 'PLANNED', 'Squat', 4, 10, '60s', 'Tạ đòn', NULL, NULL, NULL),
(5, 'PLANNED', 'Deadlift', 4, 8, '90s', 'Tạ đòn', NULL, NULL, NULL);
