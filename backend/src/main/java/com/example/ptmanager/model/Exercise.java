package com.example.ptmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_type")
    private String exerciseType;

    private String name;
    private Integer sets;
    private Integer reps;
    private String rest;
    private String equipment;
    private String weight;
    private Integer rpe;
    private String note;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    @JsonIgnore
    private Schedule schedule;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }
    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }
    public String getRest() { return rest; }
    public void setRest(String rest) { this.rest = rest; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public Integer getRpe() { return rpe; }
    public void setRpe(Integer rpe) { this.rpe = rpe; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
}
