package com.hits.language_school_back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @Column(nullable = false)
    private Integer commandMark = 0;

    private Double averageMark;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Participation> participationList;

    @OneToOne(fetch = FetchType.LAZY)
    private Participation solutionParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;
}
