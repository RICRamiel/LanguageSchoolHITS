package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.SolutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "team_id"}))
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private Boolean isCaptain;

    private Integer mark;
    private Double averageMark;
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private User student;

    @OneToMany(mappedBy = "participation", fetch = FetchType.LAZY)
    private List<Attachment> attachmentList;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Team team;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolutionStatus solutionStatus;

    @OneToMany(mappedBy = "participation", fetch = FetchType.LAZY)
    private List<Vote> vote;
}
