package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseEditDTO {
    String name;
    String description;
    Integer satisfactorilyMarkThreshold;
    Integer goodMarkThreshold;
    Integer excellentMarkThreshold;
}
