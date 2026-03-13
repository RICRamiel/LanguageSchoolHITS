package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Data
@Table(name = "task_student")
public class TaskStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long taskId;

    @OneToMany
    private List<Attachment> attachmentList;
    private TaskStatus taskStatus;
}
