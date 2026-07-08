package com.example.ptmanager.repository;

import com.example.ptmanager.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByScheduleIdAndExerciseType(Long scheduleId, String exerciseType);
    void deleteByScheduleIdAndExerciseType(Long scheduleId, String exerciseType);
}
