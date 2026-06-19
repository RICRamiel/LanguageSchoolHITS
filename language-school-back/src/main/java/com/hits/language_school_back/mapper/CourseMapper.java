package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.CourseDTO;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.User;

public class CourseMapper {
    public static CourseDTO courseToCourseDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .teacher(userToUserDTO(course.getTeacher()))
                .satisfactorilyMarkThreshold(course.getSatisfactorilyMarkThreshold())
                .goodMarkThreshold(course.getGoodMarkThreshold())
                .excellentMarkThreshold(course.getExcellentMarkThreshold())
                .build();
    }

    private static UserDTO userToUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                null,
                user.getRole()
        );
    }
}
