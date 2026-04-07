package com.hits.language_school_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@RequiredArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Integer satisfactorilyMarkThreshold;

    @NotNull
    private Integer goodMarkThreshold;

    @NotNull
    private Integer excellentMarkThreshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Language language;

    @OneToOne(fetch = FetchType.LAZY)
    @NotNull
    private User teacher;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<StudentsInCourse> students;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<Task> tasks;
}
