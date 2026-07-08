package com.example.ptmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "edit_history")
public class EditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date")
    private LocalDate date;

    private Double weight;
    private Double height;
    private String goal;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}
