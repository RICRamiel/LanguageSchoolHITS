package com.hits.language_school_back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task_criteria")
public class TaskCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Task task;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull
    private Integer maxPoints;

    private String sectionName;

    private Integer orderIndex;

    @NotNull
    private Boolean active = true;
}
