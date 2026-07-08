package com.example.ptmanager.repository;

import com.example.ptmanager.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStudentIdOrderByDateDesc(Long studentId);
    List<Schedule> findByDateAndStatusNot(LocalDate date, String status);
    List<Schedule> findByDateBetween(LocalDate start, LocalDate end);
}
