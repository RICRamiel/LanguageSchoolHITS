package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.GroupAnswerDTO;
import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public UserDTO userToUserDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                mapCourses(user),
                user.getRole()
        );
    }

    public UserFullDTO userToUserFullDto(User user) {
        UserFullDTO dto = new UserFullDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setGroups(mapCourses(user));
        dto.setRole(user.getRole());
        return dto;
    }

    private List<GroupAnswerDTO> mapCourses(User user) {
        if (user.getCourse() == null) {
            return List.of();
        }

        return user.getCourse().stream()
                .map(StudentsInCourse::getCourse)
                .distinct()
                .map(this::mapCourse)
                .toList();
    }

    private GroupAnswerDTO mapCourse(Course course) {
        return GroupAnswerDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .language(course.getLanguage() == null ? null : new LanguageDTO(course.getLanguage().getId(), course.getLanguage().getName()))
                .build();
    }
}
