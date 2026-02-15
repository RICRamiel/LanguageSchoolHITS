package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.Group;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFullDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Group studentGroup;
    private Role userRole;
}