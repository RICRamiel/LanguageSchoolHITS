package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "peer_review_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"task_id", "reviewed_team_id"}),
                @UniqueConstraint(columnNames = {"task_id", "reviewer_team_id"}),
                @UniqueConstraint(columnNames = {"assessment_id"})
        }
)
public class PeerReviewAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team reviewerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Team reviewedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Participation targetParticipation;

    @OneToOne(fetch = FetchType.LAZY)
    private Assessment assessment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeerReviewAssignmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User teacherEditor;

    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime teacherEditedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PeerReviewAssignmentStatus.ASSIGNED;
        }
    }
}
