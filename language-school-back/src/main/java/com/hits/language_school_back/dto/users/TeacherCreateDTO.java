package com.hits.language_school_back.dto.users;

import lombok.Data;

@Data
public class TeacherCreateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}