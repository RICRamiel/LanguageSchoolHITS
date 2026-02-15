package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Group studentGroup;
    private Role userRole;
}