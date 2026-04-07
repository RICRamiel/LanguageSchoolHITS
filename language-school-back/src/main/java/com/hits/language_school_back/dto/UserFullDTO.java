package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.Group;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private List<GroupAnswerDTO> groups;
    private Role role;
}