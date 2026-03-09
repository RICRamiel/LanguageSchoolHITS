package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskStudentDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private TaskStatus taskStatus;
    private UserDTO teacher;
}
