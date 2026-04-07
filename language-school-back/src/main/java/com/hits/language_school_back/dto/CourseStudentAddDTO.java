package com.hits.language_school_back.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseStudentAddDTO {
    private UUID CourseId;
    private List<UUID> StudentIds;
}
