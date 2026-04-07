package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.SolutionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private Boolean isCaptain;

    @NotBlank
    private Integer mark;

    @ManyToOne
    private User student;

    @OneToMany(mappedBy = "participation")
    private List<Attachment> attachmentList;

    @ManyToOne
    private Team team;

    private SolutionStatus solutionStatus;

    @OneToMany(mappedBy = "participation")
    private List<Vote> vote;
}