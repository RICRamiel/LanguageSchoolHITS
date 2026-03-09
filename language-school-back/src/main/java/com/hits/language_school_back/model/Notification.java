package com.hits.language_school_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    private LocalDate creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;
}
