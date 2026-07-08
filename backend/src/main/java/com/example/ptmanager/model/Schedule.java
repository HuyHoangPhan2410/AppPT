package com.example.ptmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_date")
    private LocalDate date;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    private String status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "is_paid", columnDefinition = "boolean default false")
    private Boolean isPaid = false;

    @Column(name = "session_note")
    private String sessionNote;

    @Column(name = "workout_types")
    private String workoutTypes;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Exercise> exercises;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    public String getSessionNote() { return sessionNote; }
    public void setSessionNote(String sessionNote) { this.sessionNote = sessionNote; }
    public String getWorkoutTypes() { return workoutTypes; }
    public void setWorkoutTypes(String workoutTypes) { this.workoutTypes = workoutTypes; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}
