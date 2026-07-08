package com.example.ptmanager.service;

import com.example.ptmanager.model.EditHistory;
import com.example.ptmanager.model.Student;
import com.example.ptmanager.repository.EditHistoryRepository;
import com.example.ptmanager.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private EditHistoryRepository historyRepo;

    public List<Student> getAll() {
        return studentRepo.findAll();
    }

    public Student getById(Long id) {
        return studentRepo.findById(id).orElse(null);
    }

    public Student create(Student student) {
        return studentRepo.save(student);
    }

    public Student update(Long id, Student updated) {
        Student old = studentRepo.findById(id).orElse(null);
        if (old == null) return null;

        EditHistory snapshot = new EditHistory();
        snapshot.setDate(LocalDate.now());
        snapshot.setWeight(old.getWeight());
        snapshot.setHeight(old.getHeight());
        snapshot.setGoal(old.getGoal());
        snapshot.setNotes(old.getNotes());
        snapshot.setStudent(old);
        historyRepo.save(snapshot);

        old.setName(updated.getName());
        old.setAge(updated.getAge());
        old.setGender(updated.getGender());
        old.setWeight(updated.getWeight());
        old.setHeight(updated.getHeight());
        old.setGoal(updated.getGoal());
        old.setBackground(updated.getBackground());
        old.setNotes(updated.getNotes());
        return studentRepo.save(old);
    }

    public void delete(Long id) {
        studentRepo.deleteById(id);
    }

    public List<EditHistory> getHistory(Long studentId) {
        return historyRepo.findByStudentIdOrderByDateDesc(studentId);
    }
}
