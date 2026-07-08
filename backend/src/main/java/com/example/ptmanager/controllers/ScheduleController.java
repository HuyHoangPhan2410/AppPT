package com.example.ptmanager.controllers;

import com.example.ptmanager.model.Exercise;
import com.example.ptmanager.model.Schedule;
import com.example.ptmanager.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.example.ptmanager.service.ExcelExportService;
import com.example.ptmanager.service.EmailReminderService;

import java.io.IOException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private ExcelExportService excelExportService;
    
    @Autowired
    private EmailReminderService emailReminderService;

    @GetMapping
    public List<Schedule> getAll() {
        return scheduleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getById(@PathVariable Long id) {
        Schedule s = scheduleService.getById(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @GetMapping("/student/{studentId}")
    public List<Schedule> getByStudent(@PathVariable Long studentId) {
        return scheduleService.getByStudent(studentId);
    }

    @PutMapping("/student/{studentId}/mark-paid")
    public ResponseEntity<?> markPaid(@PathVariable Long studentId) {
        int count = scheduleService.markSchedulesPaid(studentId);
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu thanh toán " + count + " ca.", "count", count));
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());
        String startTime = body.get("startTime").toString();

        String error = scheduleService.validateSchedule(date, startTime, null);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        List<Exercise> planned = null;
        if (body.containsKey("plannedExercises") && body.get("plannedExercises") != null) {
            List<Map<String, Object>> exList = (List<Map<String, Object>>) body.get("plannedExercises");
            planned = exList.stream().map(m -> {
                Exercise ex = new Exercise();
                ex.setName(m.getOrDefault("name", "").toString());
                ex.setSets(toInt(m.get("sets")));
                ex.setReps(toInt(m.get("reps")));
                ex.setRest(m.getOrDefault("rest", "").toString());
                ex.setEquipment(m.getOrDefault("equipment", "").toString());
                return ex;
            }).toList();
        }

        Schedule created = scheduleService.create(studentId, date, startTime, planned);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(@PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate newDate = LocalDate.parse(body.get("date"));
        String newStart = body.get("startTime");

        String error = scheduleService.validateSchedule(newDate, newStart, id);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        Schedule updated = scheduleService.reschedule(id, newDate, newStart);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phải ghi lý do hủy ca."));
        }
        Schedule cancelled = scheduleService.cancel(id, reason);
        return ResponseEntity.ok(cancelled);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sessionNote = body.getOrDefault("sessionNote", "").toString();
        String workoutTypes = body.getOrDefault("workoutTypes", "").toString();

        List<Exercise> logExercises = null;
        if (body.containsKey("exercises") && body.get("exercises") != null) {
            List<Map<String, Object>> exList = (List<Map<String, Object>>) body.get("exercises");
            logExercises = exList.stream().map(m -> {
                Exercise ex = new Exercise();
                ex.setName(m.getOrDefault("name", "").toString());
                ex.setSets(toInt(m.get("sets")));
                ex.setReps(toInt(m.get("reps")));
                ex.setRest(m.getOrDefault("rest", "").toString());
                ex.setEquipment(m.getOrDefault("equipment", "").toString());
                ex.setWeight(m.getOrDefault("weight", "").toString());
                ex.setRpe(toInt(m.get("rpe")));
                ex.setNote(m.getOrDefault("note", "").toString());
                return ex;
            }).toList();
        }

        Schedule completed = scheduleService.complete(id, sessionNote, workoutTypes, logExercises);
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return scheduleService.getDashboardStats();
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] data = excelExportService.exportAllSchedules();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Bao_Cao_Tong_The_Lich_Day.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/remind-today")
    public ResponseEntity<?> sendReminderToday() {
        try {
            emailReminderService.sendReminderNow();
            return ResponseEntity.ok(Map.of("message", "Đã gửi email thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
