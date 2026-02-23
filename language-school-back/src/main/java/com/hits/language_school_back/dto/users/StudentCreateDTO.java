package com.hits.language_school_back.dto.users;

import com.hits.language_school_back.model.Group;
import lombok.Data;

import java.util.List;

@Data
public class StudentCreateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private List<Group> groups;
}