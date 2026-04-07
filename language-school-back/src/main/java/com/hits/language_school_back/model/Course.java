package com.hits.language_school_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@RequiredArgsConstructor
@Data

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;
    @NotBlank
    private String description;

    @NotBlank
    private Integer satisfactorilyMarkThreshold;
    @NotBlank
    private Integer goodMarkThreshold;
    @NotBlank
    private Integer excellentMarkThreshold;

    @ManyToOne
    @NotBlank
    private Language language;

    @OneToOne
    @NotBlank
    private User teacher;

    @OneToMany(mappedBy = "course")
    List<StudentsInCourse> students;
}
