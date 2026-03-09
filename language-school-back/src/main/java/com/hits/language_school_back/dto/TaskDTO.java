package com.hits.language_school_back.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class TaskDTO {
    private String name;
    private String description;
    private LocalDate deadline;
    private String groupName;
}
