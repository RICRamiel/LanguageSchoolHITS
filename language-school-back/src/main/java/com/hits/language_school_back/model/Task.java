package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Comment> commentList;

    //TODO: связь с ответом на задание когда вася сделает сущность

}
