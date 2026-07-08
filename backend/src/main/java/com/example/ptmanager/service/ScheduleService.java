package com.example.ptmanager.service;

import com.example.ptmanager.model.Exercise;
import com.example.ptmanager.model.Schedule;
import com.example.ptmanager.model.Student;
import com.example.ptmanager.repository.ExerciseRepository;
import com.example.ptmanager.repository.ScheduleRepository;
import com.example.ptmanager.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepo;
    @Autowired
    private ExerciseRepository exerciseRepo;
    @Autowired
    private StudentRepository studentRepo;

    private static final int PRICE_PER_SESSION = 300000;

    public List<Schedule> getAll() {
        return scheduleRepo.findAll();
    }

    public Schedule getById(Long id) {
        return scheduleRepo.findById(id).orElse(null);
    }

    public List<Schedule> getByStudent(Long studentId) {
        return scheduleRepo.findByStudentIdOrderByDateDesc(studentId);
    }

    private int timeToMinutes(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private String calcEndTime(String start) {
        int m = timeToMinutes(start) + 90;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public String validateSchedule(LocalDate date, String startTime, Long excludeId) {
        int startMins = timeToMinutes(startTime);
        int endMins = startMins + 90;

        if (startMins < timeToMinutes("06:00") || endMins > timeToMinutes("22:00")) {
            return "Ca tập phải nằm hoàn toàn trong khoảng 06:00 đến 22:00 (1.5 tiếng/ca).";
        }

        List<Schedule> sameDaySchs = scheduleRepo.findByDateAndStatusNot(date, "cancelled");
        for (Schedule ex : sameDaySchs) {
            if (excludeId != null && ex.getId().equals(excludeId)) continue;
            int exStart = timeToMinutes(ex.getStartTime());
            int exEnd = timeToMinutes(ex.getEndTime());
            if (startMins < exEnd && endMins > exStart) {
                return "Bị đè lịch! Trùng ca " + ex.getStartTime() + " - " + ex.getEndTime() + ".";
            }
        }
        return null;
    }

    @Transactional
    public Schedule create(Long studentId, LocalDate date, String startTime, List<Exercise> plannedExercises) {
        Student student = studentRepo.findById(studentId).orElse(null);
        if (student == null) return null;

        Schedule sch = new Schedule();
        sch.setStudent(student);
        sch.setDate(date);
        sch.setStartTime(startTime);
        sch.setEndTime(calcEndTime(startTime));
        sch.setStatus("pending");
        Schedule saved = scheduleRepo.save(sch);

        if (plannedExercises != null) {
            for (Exercise ex : plannedExercises) {
                ex.setExerciseType("PLANNED");
                ex.setSchedule(saved);
                exerciseRepo.save(ex);
            }
        }
        return saved;
    }

    @Transactional
    public Schedule reschedule(Long id, LocalDate newDate, String newStart) {
        Schedule sch = scheduleRepo.findById(id).orElse(null);
        if (sch == null) return null;
        sch.setDate(newDate);
        sch.setStartTime(newStart);
        sch.setEndTime(calcEndTime(newStart));
        return scheduleRepo.save(sch);
    }

    @Transactional
    public Schedule cancel(Long id, String reason) {
        Schedule sch = scheduleRepo.findById(id).orElse(null);
        if (sch == null) return null;
        sch.setStatus("cancelled");
        sch.setCancelReason(reason);
        return scheduleRepo.save(sch);
    }

    @Transactional
    public Schedule complete(Long id, String sessionNote, String workoutTypes, List<Exercise> logExercises) {
        Schedule sch = scheduleRepo.findById(id).orElse(null);
        if (sch == null) return null;
        sch.setStatus("completed");
        sch.setSessionNote(sessionNote);
        sch.setWorkoutTypes(workoutTypes);
        scheduleRepo.save(sch);

        Student student = sch.getStudent();
        if (student != null && student.getRemainingSessions() != null && student.getRemainingSessions() > 0) {
            student.setRemainingSessions(student.getRemainingSessions() - 1);
            studentRepo.save(student);
        }

        if (logExercises != null) {
            for (Exercise ex : logExercises) {
                ex.setExerciseType("LOG");
                ex.setSchedule(sch);
                exerciseRepo.save(ex);
            }
        }
        return sch;
    }

    @Transactional
    public int markSchedulesPaid(Long studentId) {
        List<Schedule> studentSchedules = scheduleRepo.findByStudentIdOrderByDateDesc(studentId);
        int count = 0;
        for (Schedule sch : studentSchedules) {
            if ("pending".equals(sch.getStatus()) && (sch.getIsPaid() == null || !sch.getIsPaid())) {
                sch.setIsPaid(true);
                scheduleRepo.save(sch);
                count++;
            }
        }
        return count;
    }

    public Map<String, Object> getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        String monthStr = today.toString().substring(0, 7);

        List<Schedule> allSchedules = scheduleRepo.findAll();

        int sessionsWeek = 0;
        int revToday = 0;
        int revWeek = 0;
        int revMonth = 0;
        Set<Long> clientsSet = new java.util.HashSet<>();

        for (Schedule sch : allSchedules) {
            if ("cancelled".equals(sch.getStatus())) continue;
            LocalDate d = sch.getDate();
            if (d.equals(today)) revToday += PRICE_PER_SESSION;
            if (d.toString().startsWith(monthStr)) revMonth += PRICE_PER_SESSION;
            if (!d.isBefore(monday) && !d.isAfter(sunday)) {
                sessionsWeek++;
                revWeek += PRICE_PER_SESSION;
                clientsSet.add(sch.getStudent().getId());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("sessionsWeek", sessionsWeek);
        stats.put("clientsWeek", clientsSet.size());
        stats.put("revToday", revToday);
        stats.put("revWeek", revWeek);
        stats.put("revMonth", revMonth);
        return stats;
    }
}
