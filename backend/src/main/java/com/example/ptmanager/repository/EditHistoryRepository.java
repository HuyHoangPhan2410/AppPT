package com.example.ptmanager.repository;

import com.example.ptmanager.model.EditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EditHistoryRepository extends JpaRepository<EditHistory, Long> {
    List<EditHistory> findByStudentIdOrderByDateDesc(Long studentId);
}
