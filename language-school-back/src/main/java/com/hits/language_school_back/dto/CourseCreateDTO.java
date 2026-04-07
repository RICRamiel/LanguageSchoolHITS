package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseCreateDTO {
    String name;
    String description;
    Integer satisfactorilyMarkThreshold;
    Integer goodMarkThreshold;
    Integer excellentMarkThreshold;
    UUID teacherId;
    UUID languageId;
}
