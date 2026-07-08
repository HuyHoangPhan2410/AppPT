package com.example.ptmanager.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String gender;
    private Double weight;
    private Double height;
    private String goal;
    private String background;
    private String notes;

    @Column(name = "total_sessions", columnDefinition = "int default 100")
    private Integer totalSessions = 100;

    @Column(name = "remaining_sessions", columnDefinition = "int default 100")
    private Integer remainingSessions = 100;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EditHistory> editHistory;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Schedule> schedules;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<EditHistory> getEditHistory() { return editHistory; }
    public void setEditHistory(List<EditHistory> editHistory) { this.editHistory = editHistory; }
    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
    public Integer getRemainingSessions() { return remainingSessions; }
    public void setRemainingSessions(Integer remainingSessions) { this.remainingSessions = remainingSessions; }
}
