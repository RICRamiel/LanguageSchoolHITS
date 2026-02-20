package com.hits.language_school_back.dto;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDTO {
    private String name;
    private String description;
    private LocalDate deadline;
    private String groupName;
}
