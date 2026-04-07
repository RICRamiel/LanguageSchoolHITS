package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.CourseDTO;
import com.hits.language_school_back.model.Course;

public class CourseMapper {
    public static CourseDTO courseToCourseDTO(Course course) {
        return CourseDTO.builder()
                .name(course.getName())
                .description(course.getDescription())
                .satisfactorilyMarkThreshold(course.getSatisfactorilyMarkThreshold())
                .goodMarkThreshold(course.getGoodMarkThreshold())
                .excellentMarkThreshold(course.getExcellentMarkThreshold())
                .build();
    }
}
