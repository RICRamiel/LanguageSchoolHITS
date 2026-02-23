package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO userToUserDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getGroups(),
                user.getRole()
        );
    }
    public UserFullDTO userToUserFullDto(User user) {
        UserFullDTO dto = new UserFullDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setUserRole(user.getRole());
        dto.setGroups(user.getGroups());
        return dto;
    }
}