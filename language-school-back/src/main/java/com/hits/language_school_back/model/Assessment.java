package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
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
@Table(name = "assessments", uniqueConstraints = @UniqueConstraint(columnNames = {"participation_id", "type"}))
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Participation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private User assessor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status;

    @NotNull
    private Integer totalPoints;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY)
    private List<AssessmentItem> items;
}
